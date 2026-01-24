package com.triptune.schedule.controller;

import com.triptune.global.response.page.PageResponse;
import com.triptune.schedule.dto.request.RouteCreateRequest;
import com.triptune.schedule.dto.response.RouteResponse;
import com.triptune.schedule.service.TravelRouteService;
import com.triptune.global.aop.AttendeeCheck;
import com.triptune.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/schedules/{scheduleId}")
@RequiredArgsConstructor
@Tag(name = "Schedule - Route", description = "일정 만들기 중 여행 루트 관련 API")
public class TravelRouteController {

    private final TravelRouteService travelRouteService;

    @AttendeeCheck
    @GetMapping("/routes")
    @Operation(summary = "여행 루트 조회", description = "저장되어 있는 여행 루트를 조회한다.")
    public ApiResponse<PageResponse<RouteResponse>> getTravelRoutes(@PathVariable(name = "scheduleId") Long scheduleId,
                                                                   @RequestParam(name = "page") int page){
        Page<RouteResponse> response = travelRouteService.getTravelRoutes(scheduleId, page);

        return ApiResponse.pageResponse(response);
    }


    @PostMapping("/routes")
    @Operation(summary = "여행 루트 마지막에 여행지 추가", description = "여행 루트의 마지막에 여행지를 추가한다.")
    public ApiResponse<Void> createLastRoute(@AuthenticationPrincipal(expression = "memberId") Long memberId,
                                          @PathVariable(name = "scheduleId") Long scheduleId,
                                          @Valid @RequestBody RouteCreateRequest routeCreateRequest){
        travelRouteService.createLastRoute(scheduleId, memberId, routeCreateRequest);
        return ApiResponse.okResponse();
    }

}
