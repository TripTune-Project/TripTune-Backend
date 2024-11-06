package com.triptune.domain.schedule.controller;

import com.triptune.domain.schedule.dto.response.RouteResponse;
import com.triptune.domain.schedule.service.RouteService;
import com.triptune.global.aop.AttendeeCheck;
import com.triptune.global.response.pagination.ApiPageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/schedules/{scheduleId}")
@RequiredArgsConstructor
@Tag(name = "Schedule - Route", description = "일정 만들기 중 여행 루트 관련 API")
public class RouteController {

    private final RouteService routeService;

    @AttendeeCheck
    @GetMapping("/routes")
    @Operation(summary = "여행 루트 조회", description = "저장되어 있는 여행 루트를 조회한다.")
    public ApiPageResponse<RouteResponse> getTravelRoutes(@PathVariable(name = "scheduleId") Long scheduleId, @RequestParam(name = "page") int page){
        Page<RouteResponse> response = routeService.getTravelRoutes(scheduleId, page);

        return ApiPageResponse.dataResponse(response);
    }

}