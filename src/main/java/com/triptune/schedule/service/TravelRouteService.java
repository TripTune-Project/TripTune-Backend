package com.triptune.schedule.service;

import com.triptune.schedule.dto.request.RouteCreateRequest;
import com.triptune.schedule.dto.request.RouteRequest;
import com.triptune.schedule.dto.response.RouteResponse;
import com.triptune.schedule.entity.TravelAttendee;
import com.triptune.schedule.entity.TravelRoute;
import com.triptune.schedule.entity.TravelSchedule;
import com.triptune.schedule.exception.ForbiddenScheduleException;
import com.triptune.schedule.repository.TravelAttendeeRepository;
import com.triptune.schedule.repository.TravelRouteRepository;
import com.triptune.schedule.repository.TravelScheduleRepository;
import com.triptune.travel.entity.TravelPlace;
import com.triptune.travel.repository.TravelPlaceRepository;
import com.triptune.global.response.enums.ErrorCode;
import com.triptune.global.exception.DataNotFoundException;
import com.triptune.global.util.PageUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class TravelRouteService {

    private final TravelRouteRepository travelRouteRepository;
    private final TravelScheduleRepository travelScheduleRepository;
    private final TravelAttendeeRepository travelAttendeeRepository;
    private final TravelPlaceRepository travelPlaceRepository;

    public Page<RouteResponse> getTravelRoutes(Long scheduleId, int page) {
        Pageable pageable = PageUtils.defaultPageable(page);
        Page<TravelRoute> travelRoutes = travelRouteRepository.findAllByTravelSchedule_ScheduleId(pageable, scheduleId);

        return travelRoutes.map(RouteResponse::from);
    }

    @Transactional
    public void createLastRoute(Long scheduleId, Long memberId, RouteCreateRequest routeCreateRequest) {
        TravelSchedule schedule = findTravelScheduleByScheduleId(scheduleId);

        validateEnableEdit(scheduleId, memberId);

        TravelPlace place = findTravelPlaceByPlaceId(routeCreateRequest.getPlaceId());
        TravelRoute route = TravelRoute.of(schedule, place, schedule.getTravelRoutes().size() + 1);

        travelRouteRepository.save(route);
    }

    public TravelSchedule findTravelScheduleByScheduleId(Long scheduleId){
        return travelScheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new DataNotFoundException(ErrorCode.SCHEDULE_NOT_FOUND));
    }

    public void validateEnableEdit(Long scheduleId, Long memberId){
        TravelAttendee attendee = travelAttendeeRepository.findByTravelSchedule_ScheduleIdAndMember_MemberId(scheduleId, memberId)
                .orElseThrow(() -> new DataNotFoundException(ErrorCode.ATTENDEE_NOT_FOUND));

        if (!attendee.getPermission().isEnableEdit()){
            throw new ForbiddenScheduleException(ErrorCode.FORBIDDEN_EDIT_SCHEDULE);
        }

    }

    public TravelPlace findTravelPlaceByPlaceId(Long placeId){
        return travelPlaceRepository.findById(placeId)
                .orElseThrow(() -> new DataNotFoundException(ErrorCode.PLACE_NOT_FOUND));
    }

    @Transactional
    public void updateTravelRouteInSchedule(TravelSchedule schedule, List<RouteRequest> routeRequests){
        travelRouteRepository.deleteAllByTravelSchedule_ScheduleId(schedule.getScheduleId());

        List<TravelRoute> routes = schedule.getTravelRoutes();
        if (routes != null && !routes.isEmpty()) {
            while (!routes.isEmpty()) {
                routes.remove(0);
            }
        }

        if (routeRequests != null && !routeRequests.isEmpty()){
            for(RouteRequest routeRequest : routeRequests){
                TravelPlace place = findTravelPlaceByPlaceId(routeRequest.getPlaceId());
                TravelRoute route = TravelRoute.of(schedule, place, routeRequest.getRouteOrder());
                schedule.addTravelRoutes(route);
                travelRouteRepository.save(route);
            }
        }
    }


}
