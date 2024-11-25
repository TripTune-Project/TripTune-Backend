package com.triptune.domain.schedule.controller;

import com.triptune.domain.schedule.dto.response.AttendeeResponse;
import com.triptune.domain.schedule.dto.request.CreateAttendeeRequest;
import com.triptune.domain.schedule.service.AttendeeService;
import com.triptune.global.aop.AttendeeCheck;
import com.triptune.global.response.ApiResponse;
import com.triptune.global.response.pagination.ApiPageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/schedules/{scheduleId}")
@RequiredArgsConstructor
@Tag(name = "Schedule - Attendee", description = "일정 만들기 중 참석자 관련 API")
public class AttendeeController {

    private final AttendeeService attendeeService;

    @AttendeeCheck
    @GetMapping("/attendees")
    @Operation(summary = "일정 참석자 조회", description = "일정 참석자를 조회합니다.")
    public ApiPageResponse<AttendeeResponse> getAttendees(@PathVariable(name = "scheduleId") Long scheduleId,
                                        @RequestParam(name = "page") int page){
        Page<AttendeeResponse> response = attendeeService.getAttendees(scheduleId, page);

        return ApiPageResponse.dataResponse(response);
    }

    @PostMapping("/attendees")
    @Operation(summary = "일정 참석자 추가", description = "일정을 공유함으로 참석자를 추가합니다..")
    public ApiResponse<?> createAttendee(@PathVariable(name = "scheduleId") Long scheduleId,
                                      @Valid @RequestBody CreateAttendeeRequest createAttendeeRequest){
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        attendeeService.createAttendee(scheduleId, userId, createAttendeeRequest);

        return ApiResponse.okResponse();
    }


    @DeleteMapping("/attendees")
    @Operation(summary = "일정 나가기", description = "일정에 참석자 목록에서 삭제됩니다.")
    public ApiResponse<?> leaveScheduleAsGuest(@PathVariable(name = "scheduleId") Long scheduleId){
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();

        attendeeService.leaveScheduleAsGuest(scheduleId, userId);
        return ApiResponse.okResponse();
    }

}
