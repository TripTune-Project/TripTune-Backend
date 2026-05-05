package com.triptune.schedule.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.triptune.schedule.repository.dto.RouteQueryDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TravelRouteRepositoryCustom {
    Page<RouteQueryDto> findAllByScheduleId(Pageable pageable, Long scheduleId);
    Integer countTotalElements(BooleanExpression expression);
}
