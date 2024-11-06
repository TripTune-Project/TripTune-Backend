package com.triptune.domain.schedule.controller;

import com.triptune.domain.schedule.service.AttendeeService;
import com.triptune.global.aop.AttendeeCheck;
import com.triptune.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/schedules/{scheduleId}")
@RequiredArgsConstructor
@Tag(name = "Schedule - Attendee", description = "일정 만들기 중 참석자 관련 API")
public class AttendeeController {

    private final AttendeeService attendeeService;

    @AttendeeCheck
    @DeleteMapping("/attendees")
    @Operation(summary = "일정 나가기", description = "일정에 참석자 목록에서 삭제됩니다.")
    public ApiResponse<?> removeAttendee(@PathVariable(name = "scheduleId") Long scheduleId){
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();

        attendeeService.removeAttendee(scheduleId, userId);
        return ApiResponse.okResponse();
    }

}
