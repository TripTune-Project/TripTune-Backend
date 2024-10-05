package com.triptune.domain.schedule.controller;

import com.triptune.domain.schedule.dto.CreateScheduleRequest;
import com.triptune.domain.schedule.dto.CreateScheduleResponse;
import com.triptune.domain.schedule.service.ScheduleService;
import com.triptune.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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


}
