package com.triptune.domain.schedule.service;

import com.triptune.domain.schedule.dto.request.RouteCreateRequest;
import com.triptune.domain.schedule.dto.response.RouteResponse;
import com.triptune.domain.schedule.entity.TravelAttendee;
import com.triptune.domain.schedule.entity.TravelRoute;
import com.triptune.domain.schedule.entity.TravelSchedule;
import com.triptune.domain.schedule.exception.ForbiddenScheduleException;
import com.triptune.domain.schedule.repository.TravelAttendeeRepository;
import com.triptune.domain.schedule.repository.TravelRouteRepository;
import com.triptune.domain.schedule.repository.TravelScheduleRepository;
import com.triptune.domain.travel.entity.TravelPlace;
import com.triptune.domain.travel.repository.TravelPlaceRepository;
import com.triptune.global.enumclass.ErrorCode;
import com.triptune.global.exception.DataNotFoundException;
import com.triptune.global.util.PageUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@Transactional
@RequiredArgsConstructor
public class RouteService {

    private final TravelRouteRepository travelRouteRepository;
    private final TravelScheduleRepository travelScheduleRepository;
    private final TravelAttendeeRepository travelAttendeeRepository;
    private final TravelPlaceRepository travelPlaceRepository;

    public Page<RouteResponse> getTravelRoutes(Long scheduleId, int page) {
        Pageable pageable = PageUtil.defaultPageable(page);
        Page<TravelRoute> travelRoutes = travelRouteRepository.findAllByTravelSchedule_ScheduleId(pageable, scheduleId);

        return travelRoutes.map(RouteResponse::from);
    }

    public void createLastRoute(Long scheduleId, String userId, RouteCreateRequest routeCreateRequest) {
        TravelSchedule schedule = findTravelScheduleByScheduleId(scheduleId);

        validateEnableEdit(scheduleId, userId);

        TravelPlace place = findTravelPlaceByPlaceId(routeCreateRequest.getPlaceId());
        TravelRoute route = TravelRoute.of(schedule, place, schedule.getTravelRouteList().size() + 1);

        travelRouteRepository.save(route);
    }

    public TravelSchedule findTravelScheduleByScheduleId(Long scheduleId){
        return travelScheduleRepository.findByScheduleId(scheduleId)
                .orElseThrow(() -> new DataNotFoundException(ErrorCode.SCHEDULE_NOT_FOUND));
    }

    public void validateEnableEdit(Long scheduleId, String userId){
        TravelAttendee attendee = travelAttendeeRepository.findByTravelSchedule_ScheduleIdAndMember_UserId(scheduleId, userId)
                .orElseThrow(() -> new DataNotFoundException(ErrorCode.ATTENDEE_NOT_FOUND));

        if (!attendee.getPermission().isEnableEdit()){
            throw new ForbiddenScheduleException(ErrorCode.FORBIDDEN_EDIT_SCHEDULE);
        }

    }

    public TravelPlace findTravelPlaceByPlaceId(Long placeId){
        return travelPlaceRepository.findByPlaceId(placeId)
                .orElseThrow(() -> new DataNotFoundException(ErrorCode.PLACE_NOT_FOUND));
    }


}
