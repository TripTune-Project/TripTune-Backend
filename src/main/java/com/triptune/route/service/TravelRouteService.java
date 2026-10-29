package com.triptune.route.service;

import com.triptune.global.exception.CustomBadRequestException;
import com.triptune.global.response.page.RoutePageResponse;
import com.triptune.global.s3.S3ObjectManager;
import com.triptune.global.websocket.event.EventType;
import com.triptune.global.websocket.event.ScheduleEvent;
import com.triptune.route.dto.request.RouteCreateRequest;
import com.triptune.route.dto.request.RouteUpdateRequest;
import com.triptune.route.dto.response.RouteCreateResponse;
import com.triptune.route.dto.response.RouteVersionEventResponse;
import com.triptune.route.dto.response.RouteVersionResponse;
import com.triptune.route.dto.response.RouteResponse;
import com.triptune.attendee.entity.TravelAttendee;
import com.triptune.route.entity.TravelRoute;
import com.triptune.route.exception.ConflictRouteVersionException;
import com.triptune.schedule.entity.TravelSchedule;
import com.triptune.schedule.exception.ForbiddenScheduleException;
import com.triptune.attendee.repository.TravelAttendeeRepository;
import com.triptune.route.repository.TravelRouteRepository;
import com.triptune.schedule.repository.TravelScheduleRepository;
import com.triptune.route.repository.dto.RouteQueryDto;
import com.triptune.travel.entity.TravelPlace;
import com.triptune.travel.repository.TravelPlaceRepository;
import com.triptune.global.message.ErrorCode;
import com.triptune.global.exception.DataNotFoundException;
import com.triptune.global.util.PageUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static com.triptune.global.websocket.event.EventType.*;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class TravelRouteService {

    private final TravelRouteRepository travelRouteRepository;
    private final TravelScheduleRepository travelScheduleRepository;
    private final TravelAttendeeRepository travelAttendeeRepository;
    private final TravelPlaceRepository travelPlaceRepository;
    private final S3ObjectManager s3ObjectManager;
    private final ApplicationEventPublisher publisher;

    public RoutePageResponse<RouteResponse> getTravelRoutes(Long scheduleId, int page) {
        Integer routeVersion = travelScheduleRepository.findRouteVersionById(scheduleId)
                .orElseThrow(() -> new DataNotFoundException(ErrorCode.SCHEDULE_NOT_FOUND));

        Pageable pageable = PageUtils.defaultPageable(page);
        Page<RouteQueryDto> routePage = travelRouteRepository.findAllByScheduleId(pageable, scheduleId);

        Page<RouteResponse> pageResponse = routePage.map(route -> {
                    String thumbnailUrl = s3ObjectManager.generateS3ObjectUrl(route.getThumbnailS3ObjectKey());
                    return RouteResponse.of(route, thumbnailUrl);
        });

        return RoutePageResponse.of(routeVersion, pageResponse);
    }

    @Transactional
    public RouteCreateResponse createTravelRoute(Long scheduleId, Long memberId, RouteCreateRequest createRequest) {
        TravelSchedule schedule = getSchedule(scheduleId);

        validateEnableEdit(scheduleId, memberId);
        validateRouteVersion(schedule, createRequest.getRouteVersion());

        TravelPlace place = getPlace(createRequest.getPlaceId());

        int newRouteVersion = createRequest.getRouteVersion() + 1;
        increaseRouteVersion(scheduleId, createRequest.getRouteVersion());

        int lastOrder = travelRouteRepository.findLastRouteOrder(scheduleId);
        TravelRoute route = TravelRoute.createTravelRoute(schedule, place, lastOrder + 1);
        travelRouteRepository.save(route);

        publishRouteEvent(scheduleId, ROUTE_ADDED, newRouteVersion);

        return RouteCreateResponse.of(route.getRouteId(), newRouteVersion);
    }


    private void validateEnableEdit(Long scheduleId, Long memberId){
        TravelAttendee attendee = getAttendee(scheduleId, memberId);

        if (!attendee.getPermission().isEnableEdit()){
            throw new ForbiddenScheduleException(ErrorCode.FORBIDDEN_EDIT_SCHEDULE);
        }

    }

    private TravelAttendee getAttendee(Long scheduleId, Long memberId) {
        return travelAttendeeRepository.findByTravelSchedule_ScheduleIdAndMember_MemberId(scheduleId, memberId)
                .orElseThrow(() -> new DataNotFoundException(ErrorCode.ATTENDEE_NOT_FOUND));
    }

    private TravelPlace getPlace(Long placeId){
        return travelPlaceRepository.findById(placeId)
                .orElseThrow(() -> new DataNotFoundException(ErrorCode.PLACE_NOT_FOUND));
    }


    @Transactional
    public RouteVersionResponse updateTravelRoute(Long scheduleId, RouteUpdateRequest updateRequest){
        TravelSchedule schedule = getSchedule(scheduleId);
        validateRouteVersion(schedule, updateRequest.getRouteVersion());

        List<Long> routeIds = updateRequest.getOrderedRouteIds();

        List<TravelRoute> routes = travelRouteRepository.findAllByTravelSchedule_ScheduleId(scheduleId);
        Map<Long, TravelRoute> routeMap = createRouteMap(routes);

        validateRoutesIds(routeIds, routeMap.keySet());

        int newRouteVersion = updateRequest.getRouteVersion() + 1;
        increaseRouteVersion(scheduleId, updateRequest.getRouteVersion());

        for (int i = 0; i < routeIds.size(); i++) {
            routeMap.get(routeIds.get(i)).changeRouteOrder(i+1);
        }

        publishRouteEvent(scheduleId, ROUTE_UPDATED, newRouteVersion);

        return RouteVersionResponse.of(newRouteVersion);
    }


    private Map<Long, TravelRoute> createRouteMap(List<TravelRoute> routes){
        Map<Long, TravelRoute> map = new HashMap<>();
        for (TravelRoute route : routes) {
            map.put(route.getRouteId(), route);
        }

        return map;
    }

    private void validateRoutesIds(List<Long> routeIds, Set<Long> routeMapKeySet) {
        Set<Long> idSet = new HashSet<>(routeIds);

        if (routeIds.isEmpty() || idSet.size() != routeIds.size() || !idSet.equals(routeMapKeySet)){
            throw new CustomBadRequestException(ErrorCode.ROUTE_MISMATCH);
        }
    }

    @Transactional
    public RouteVersionResponse deleteTravelRoute(Long scheduleId, Long routeId, Integer routeVersion) {
        TravelSchedule schedule = getSchedule(scheduleId);
        validateRouteVersion(schedule, routeVersion);

        TravelRoute deleteRoute = getRoute(scheduleId, routeId);

        int newRouteVersion = routeVersion + 1;
        increaseRouteVersion(scheduleId, routeVersion);

        int deleteRouteOrder = deleteRoute.getRouteOrder();
        travelRouteRepository.delete(deleteRoute);

        List<TravelRoute> routes = travelRouteRepository.findRoutesAfterOrder(scheduleId, deleteRouteOrder);
        for (TravelRoute route : routes) {
            route.decreaseRouteOrder();
        }

        publishRouteEvent(scheduleId, ROUTE_DELETED, newRouteVersion);

        return RouteVersionResponse.of(newRouteVersion);
    }

    private TravelRoute getRoute(Long scheduleId, Long routeId){
        return travelRouteRepository.findByRouteIdAndTravelSchedule_ScheduleId(routeId, scheduleId)
                .orElseThrow(() -> new DataNotFoundException(ErrorCode.ROUTE_NOT_FOUND));
    }


    private TravelSchedule getSchedule(Long scheduleId){
        return travelScheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new DataNotFoundException(ErrorCode.SCHEDULE_NOT_FOUND));
    }


    private void validateRouteVersion(TravelSchedule schedule, int requestRouteVersion) {
        if(!schedule.isValidRouteVersion(requestRouteVersion)){
            throw new ConflictRouteVersionException(ErrorCode.CONFLICT_ROUTE_VERSION);
        }
    }


    private void increaseRouteVersion(Long scheduleId, Integer routeVersion) {
        int updated = travelScheduleRepository.increaseRouteVersion(scheduleId, routeVersion);
        if (updated == 0) {
            throw new ConflictRouteVersionException(ErrorCode.CONFLICT_ROUTE_VERSION);
        }
    }


    private void publishRouteEvent(Long scheduleId, EventType eventType, Integer routeVersion){
        publisher.publishEvent(ScheduleEvent.of(
                scheduleId,
                eventType,
                new RouteVersionEventResponse(routeVersion)
        ));
    }

}
