package com.triptune.domain.schedule.controller;

import com.triptune.domain.schedule.dto.CreateScheduleRequest;
import com.triptune.domain.schedule.dto.CreateScheduleResponse;
import com.triptune.domain.schedule.dto.ScheduleResponse;
import com.triptune.domain.schedule.service.ScheduleService;
import com.triptune.domain.travel.dto.TravelSimpleResponse;
import com.triptune.global.response.ApiPageResponse;
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

    @PostMapping
    @Operation(summary = "일정 생성", description = "여행 이름, 날짜를 선택해 일정을 생성합니다.")
    public ApiResponse<CreateScheduleResponse> createSchedule(@Valid @RequestBody CreateScheduleRequest createScheduleRequest){
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();

        CreateScheduleResponse response = scheduleService.createSchedule(createScheduleRequest, userId);
        return ApiResponse.dataResponse(response);
    }


    @GetMapping("/{scheduleId}")
    @Operation(summary = "일정 조회", description = "생성한 일정을 조회합니다.")
    public ApiResponse<ScheduleResponse> getSchedule(@PathVariable(name = "scheduleId") Long scheduleId, @RequestParam int page){
         ScheduleResponse response = scheduleService.getSchedule(scheduleId, page);

         return ApiResponse.dataResponse(response);
    }

    @GetMapping("/{scheduleId}/travels")
    @Operation(summary = "여행지 조회", description = "여행지 탭 중 여행지를 제공합니다.")
    public ApiPageResponse<TravelSimpleResponse> getTravelPlaces(@PathVariable(name = "scheduleId") Long scheduleId, @RequestParam int page){
        Page<TravelSimpleResponse> response = scheduleService.getTravelPlaces(scheduleId, page);

        return ApiPageResponse.okResponse(response);
    }
}
