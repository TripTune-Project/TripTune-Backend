package com.triptune.domain.schedule.controller;

import com.triptune.domain.member.entity.Member;
import com.triptune.domain.schedule.dto.ScheduleRequest;
import com.triptune.domain.schedule.service.ScheduleService;
import com.triptune.global.response.ApiResponse;
import com.triptune.global.service.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.file.attribute.UserPrincipal;

@RestController
@RequestMapping("/api/schedules")
@RequiredArgsConstructor
@Tag(name = "Schedule", description = "일정 만들기 관련 API")
public class ScheduleApiController {

    private final ScheduleService scheduleService;

    @PostMapping
    @Operation(summary = "일정 생성", description = "여행 이름, 날짜를 선택해 일정을 생성합니다.")
    public ApiResponse<?> createSchedule(@Valid @RequestBody ScheduleRequest scheduleRequest){
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();

        scheduleService.createSchedule(scheduleRequest, userId);
        return ApiResponse.okResponse();
    }
}
