package com.triptune.route.repository;

import com.triptune.route.entity.TravelRoute;
import com.triptune.route.repository.dto.RouteQueryDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface TravelRouteRepositoryCustom {
    Page<RouteQueryDto> findAllByScheduleId(Pageable pageable, Long scheduleId);
    List<TravelRoute> findRoutesAfterOrder(Long scheduleId, Integer routeOrder);
}
