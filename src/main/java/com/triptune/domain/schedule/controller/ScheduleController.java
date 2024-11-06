package com.triptune.domain.schedule.controller;

import com.triptune.domain.schedule.dto.request.CreateScheduleRequest;
import com.triptune.domain.schedule.dto.request.UpdateScheduleRequest;
import com.triptune.domain.schedule.dto.response.CreateScheduleResponse;
import com.triptune.domain.schedule.dto.response.RouteResponse;
import com.triptune.domain.schedule.dto.response.ScheduleDetailResponse;
import com.triptune.domain.schedule.dto.response.ScheduleInfoResponse;
import com.triptune.domain.schedule.service.ScheduleService;
import com.triptune.domain.travel.dto.response.PlaceResponse;
import com.triptune.global.aop.AttendeeCheck;
import com.triptune.global.response.pagination.ApiPageResponse;
import com.triptune.global.response.ApiResponse;
import com.triptune.global.response.pagination.ApiSchedulePageResponse;
import com.triptune.global.response.pagination.SchedulePageResponse;
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
public class ScheduleController {

    private final ScheduleService scheduleService;

    @GetMapping
    @Operation(summary = "전체 일정 목록 조회", description = "작성한 전체 일정을 조회합니다.")
    public ApiSchedulePageResponse<ScheduleInfoResponse> getSchedules(@RequestParam(name = "page") int page){
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        SchedulePageResponse<ScheduleInfoResponse> response = scheduleService.getSchedules(page, userId);

        return ApiSchedulePageResponse.dataResponse(response);
    }

    @GetMapping("/shared")
    @Operation(summary = "공유된 일정 목록 조회", description = "작성한 일정 중 공유된 일정을 조회합니다.")
    public ApiSchedulePageResponse<ScheduleInfoResponse> getSharedSchedules(@RequestParam(name = "page") int page){
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        SchedulePageResponse<ScheduleInfoResponse> response = scheduleService.getSharedSchedules(page, userId);

        return ApiSchedulePageResponse.dataResponse(response);
    }

    @PostMapping
    @Operation(summary = "일정 생성", description = "여행 이름, 날짜를 선택해 일정을 생성합니다.")
    public ApiResponse<CreateScheduleResponse> createSchedule(@Valid @RequestBody CreateScheduleRequest createScheduleRequest){
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();

        CreateScheduleResponse response = scheduleService.createSchedule(createScheduleRequest, userId);
        return ApiResponse.dataResponse(response);
    }


    @AttendeeCheck
    @GetMapping("/{scheduleId}")
    @Operation(summary = "일정 상세 조회", description = "생성한 일정을 조회합니다.")
    public ApiResponse<ScheduleDetailResponse> getScheduleDetail(@PathVariable(name = "scheduleId") Long scheduleId, @RequestParam int page){
         ScheduleDetailResponse response = scheduleService.getScheduleDetail(scheduleId, page);

         return ApiResponse.dataResponse(response);
    }

    @AttendeeCheck
    @PatchMapping("/{scheduleId}")
    @Operation(summary = "일정 수정", description = "일정 상세 화면에서 저장 버튼을 누르면 해당 일정이 수정됩니다. 사용자는 저장 작업으로 보지만, 실제로는 일정 수정 작업입니다.")
    public ApiResponse<?> updateSchedule(@PathVariable(name = "scheduleId") Long scheduleId, @Valid @RequestBody UpdateScheduleRequest updateScheduleRequest){
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();

        scheduleService.updateSchedule(userId, scheduleId, updateScheduleRequest);
        return ApiResponse.okResponse();
    }

    @AttendeeCheck
    @DeleteMapping("/{scheduleId}")
    @Operation(summary = "일정 삭제", description = "일정을 삭제합니다.")
    public ApiResponse<?> deleteSchedule(@PathVariable(name = "scheduleId") Long scheduleId){
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();

        scheduleService.deleteSchedule(scheduleId, userId);
        return ApiResponse.okResponse();
    }


}
