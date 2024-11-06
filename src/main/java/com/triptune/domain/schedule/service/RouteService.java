package com.triptune.domain.schedule.service;

import com.triptune.domain.schedule.dto.response.RouteResponse;
import com.triptune.domain.schedule.entity.TravelRoute;
import com.triptune.domain.schedule.repository.TravelRouteRepository;
import com.triptune.domain.schedule.repository.TravelScheduleRepository;
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

    /**
     * 여행 루트 조회
     * @param scheduleId: 일정 인덱스
     * @param page: 페이지 수
     * @return Page<RouteResponse>: 여행 루트 정보로 구성된 페이지 dto
     */
    public Page<RouteResponse> getTravelRoutes(Long scheduleId, int page) {
        Pageable pageable = PageUtil.defaultPageable(page);
        Page<TravelRoute> travelRoutes = travelRouteRepository.findAllByTravelSchedule_ScheduleId(pageable, scheduleId);

        return travelRoutes.map(RouteResponse::entityToDto);
    }
}
