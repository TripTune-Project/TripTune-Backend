package com.triptune.schedule.controller;

import com.triptune.global.util.SecurityUtils;
import com.triptune.schedule.dto.request.AttendeePermissionRequest;
import com.triptune.schedule.dto.response.AttendeeResponse;
import com.triptune.schedule.dto.request.AttendeeRequest;
import com.triptune.schedule.service.AttendeeService;
import com.triptune.global.aop.AttendeeCheck;
import com.triptune.global.aop.ScheduleCheck;
import com.triptune.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/schedules/{scheduleId}/attendees")
@RequiredArgsConstructor
@Tag(name = "Schedule - Attendee", description = "일정 만들기 중 참석자 관련 API")
public class AttendeeController {

    private final AttendeeService attendeeService;

    @AttendeeCheck
    @GetMapping
    @Operation(summary = "일정 참석자 조회", description = "일정 참석자를 조회합니다.")
    public ApiResponse<List<AttendeeResponse>> getAttendees(@PathVariable(name = "scheduleId") Long scheduleId){
        List<AttendeeResponse> response = attendeeService.getAttendeesByScheduleId(scheduleId);

        return ApiResponse.dataResponse(response);
    }

    @PostMapping
    @Operation(summary = "일정 참석자 추가", description = "일정을 공유함으로 참석자를 추가합니다.")
    public ApiResponse<Void> createAttendee(@PathVariable(name = "scheduleId") Long scheduleId,
                                      @Valid @RequestBody AttendeeRequest attendeeRequest){
        String email = SecurityUtils.getCurrentEmail();
        attendeeService.createAttendee(scheduleId, email, attendeeRequest);

        return ApiResponse.okResponse();
    }

    @ScheduleCheck
    @PatchMapping("/{attendeeId}")
    @Operation(summary = "일정 참석자 접근 권한 수정", description = "일정 참석자 접근 권한을 수정합니다.")
    public ApiResponse<Void> updateAttendeePermission(@PathVariable(name = "scheduleId") Long scheduleId,
                                                        @PathVariable(name = "attendeeId") Long attendeeId,
                                                        @Valid @RequestBody AttendeePermissionRequest attendeePermissionRequest){
        String email = SecurityUtils.getCurrentEmail();
        attendeeService.updateAttendeePermission(scheduleId, email, attendeeId, attendeePermissionRequest);

        return ApiResponse.okResponse();
    }


    @ScheduleCheck
    @DeleteMapping
    @Operation(summary = "일정 나가기", description = "일정에 참석자 목록에서 삭제됩니다.")
    public ApiResponse<Void> leaveAttendee(@PathVariable(name = "scheduleId") Long scheduleId){
        String email = SecurityUtils.getCurrentEmail();
        attendeeService.leaveAttendee(scheduleId, email);
        return ApiResponse.okResponse();
    }

    @ScheduleCheck
    @DeleteMapping("/{attendeeId}")
    @Operation(summary = "일정 내보내기", description = "작성자가 일정 참석자를 내보냅니다.")
    public ApiResponse<Void> removeAttendee(@PathVariable(name = "scheduleId") Long scheduleId, @PathVariable(name = "attendeeId") Long attendeeId){
        String email = SecurityUtils.getCurrentEmail();
        attendeeService.removeAttendee(scheduleId, email, attendeeId);
        return ApiResponse.okResponse();
    }

}
