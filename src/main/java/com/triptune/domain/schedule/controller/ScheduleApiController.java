package com.triptune.domain.schedule.controller;

import com.triptune.domain.schedule.dto.*;
import com.triptune.domain.schedule.service.ScheduleService;
import com.triptune.domain.travel.dto.PlaceResponse;
import com.triptune.global.response.pagination.ApiPageResponse;
import com.triptune.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/schedules")
@RequiredArgsConstructor
@Tag(name = "Schedule", description = "일정 만들기 관련 API")
public class ScheduleApiController {

    private final ScheduleService scheduleService;

    @GetMapping("")
    @Operation(summary = "일정 목록 조회", description = "작성한 일정 목록을 조회합니다.")
    public ApiPageResponse<ScheduleOverviewResponse> getSchedules(@RequestParam(name = "page") int page){
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();

        Page<ScheduleOverviewResponse> response = scheduleService.getSchedules(page, userId);
        return ApiPageResponse.dataResponse(response);
    }

    @PostMapping
    @Operation(summary = "일정 생성", description = "여행 이름, 날짜를 선택해 일정을 생성합니다.")
    public ApiResponse<CreateScheduleResponse> createSchedule(@Valid @RequestBody CreateScheduleRequest createScheduleRequest){
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();

        CreateScheduleResponse response = scheduleService.createSchedule(createScheduleRequest, userId);
        return ApiResponse.dataResponse(response);
    }


    @GetMapping("/{scheduleId}")
    @Operation(summary = "일정 상세 조회", description = "생성한 일정을 조회합니다.")
    public ApiResponse<ScheduleResponse> getScheduleDetail(@PathVariable(name = "scheduleId") Long scheduleId, @RequestParam int page){
         ScheduleResponse response = scheduleService.getScheduleDetail(scheduleId, page);

         return ApiResponse.dataResponse(response);
    }

    @GetMapping("/{scheduleId}/travels")
    @Operation(summary = "여행지 조회", description = "여행지 탭에서 여행지를 제공합니다.")
    public ApiPageResponse<PlaceResponse> getTravelPlaces(@PathVariable(name = "scheduleId") Long scheduleId, @RequestParam int page){
        Page<PlaceResponse> response = scheduleService.getTravelPlaces(scheduleId, page);

        return ApiPageResponse.dataResponse(response);
    }

    @GetMapping("/{scheduleId}/travels/search")
    @Operation(summary = "여행지 검색", description = "여행지 탭에서 여행지를 검색합니다.")
    public ApiPageResponse<PlaceResponse> searchTravelPlaces(@PathVariable(name = "scheduleId") Long scheduleId,
                                                             @RequestParam(name = "page") int page,
                                                             @RequestParam(name = "keyword") String keyword){

        Page<PlaceResponse> response = scheduleService.searchTravelPlaces(scheduleId, page, keyword);

        return ApiPageResponse.dataResponse(response);
    }

    @GetMapping("{scheduleId}/routes")
    @Operation(summary = "여행 루트 조회", description = "저장되어 있는 여행 루트를 조회한다.")
    public ApiPageResponse<RouteResponse> getTravelRoutes(@PathVariable(name = "scheduleId") Long scheduleId, @RequestParam(name = "page") int page){
        Page<RouteResponse> response = scheduleService.getTravelRoutes(scheduleId, page);

        return ApiPageResponse.dataResponse(response);
    }


}
