package com.triptune.route.controller;

import com.triptune.global.aop.AttendeeCheck;
import com.triptune.global.response.ApiResponse;
import com.triptune.global.response.page.RoutePageResponse;
import com.triptune.route.dto.request.RouteCreateRequest;
import com.triptune.route.dto.request.RouteUpdateRequest;
import com.triptune.route.dto.response.RouteCreateResponse;
import com.triptune.route.dto.response.RouteResponse;
import com.triptune.route.dto.response.RouteVersionResponse;
import com.triptune.route.service.TravelRouteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
    @Operation(summary = "여행 루트 조회", description = "저장되어 있는 여행 루트를 조회합니다.")
    public ApiResponse<RoutePageResponse<RouteResponse>> getTravelRoutes(@PathVariable(name = "scheduleId") Long scheduleId,
                                                                   @RequestParam(name = "page") int page){
        RoutePageResponse<RouteResponse> response = travelRouteService.getTravelRoutes(scheduleId, page);

        return ApiResponse.routePageResponse(response);
    }


    @PostMapping("/routes")
    @Operation(summary = "여행 루트 추가", description = "여행 루트의 마지막에 여행지를 추가합니다.")
    public ApiResponse<RouteCreateResponse> createTravelRoute(@AuthenticationPrincipal(expression = "memberId") Long memberId,
                                                              @PathVariable(name = "scheduleId") Long scheduleId,
                                                              @Valid @RequestBody RouteCreateRequest routeCreateRequest){
        RouteCreateResponse response = travelRouteService.createTravelRoute(scheduleId, memberId, routeCreateRequest);
        return ApiResponse.dataResponse(response);
    }

    @PatchMapping("/routes")
    @Operation(summary = "여행 루트 수정", description = "여행 루트 순서를 수정합니다.")
    public ApiResponse<RouteVersionResponse> updateTravelRoute(@PathVariable(name = "scheduleId") Long scheduleId,
                                                               @Valid @RequestBody RouteUpdateRequest routeUpdateRequest){
        RouteVersionResponse response = travelRouteService.updateTravelRoute(scheduleId, routeUpdateRequest);
        return ApiResponse.dataResponse(response);
    }

    @DeleteMapping("/routes/{routeId}")
    @Operation(summary = "여행 루트 삭제", description = "특정 여행 루트를 삭제합니다.")
    public ApiResponse<RouteVersionResponse> deleteTravelRoute(@PathVariable(name = "scheduleId") Long scheduleId,
                                                               @PathVariable(name = "routeId") Long routeId,
                                                               @RequestParam("routeVersion") int routeVersion){
        RouteVersionResponse response = travelRouteService.deleteTravelRoute(scheduleId, routeId, routeVersion);
        return ApiResponse.dataResponse(response);
    }

}
