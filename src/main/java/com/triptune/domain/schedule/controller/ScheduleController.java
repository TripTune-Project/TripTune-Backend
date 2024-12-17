package com.triptune.domain.schedule.controller;

import com.triptune.domain.schedule.dto.request.ScheduleCreateRequest;
import com.triptune.domain.schedule.dto.request.ScheduleUpdateRequest;
import com.triptune.domain.schedule.dto.response.OverviewScheduleResponse;
import com.triptune.domain.schedule.dto.response.ScheduleCreateResponse;
import com.triptune.domain.schedule.dto.response.ScheduleDetailResponse;
import com.triptune.domain.schedule.dto.response.ScheduleInfoResponse;
import com.triptune.domain.schedule.enumclass.ScheduleType;
import com.triptune.domain.schedule.service.ScheduleService;
import com.triptune.global.aop.AttendeeCheck;
import com.triptune.global.response.ApiResponse;
import com.triptune.global.response.pagination.ApiPageResponse;
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
    public ApiSchedulePageResponse<ScheduleInfoResponse> getAllSchedulesByUserId(@RequestParam(name = "page") int page){
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        SchedulePageResponse<ScheduleInfoResponse> response = scheduleService.getAllSchedulesByUserId(page, userId);

        return ApiSchedulePageResponse.dataResponse(response);
    }

    @GetMapping("/shared")
    @Operation(summary = "공유된 일정 목록 조회", description = "작성한 일정 중 공유된 일정을 조회합니다.")
    public ApiSchedulePageResponse<ScheduleInfoResponse> getSharedSchedulesByUserId(@RequestParam(name = "page") int page){
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        SchedulePageResponse<ScheduleInfoResponse> response = scheduleService.getSharedSchedulesByUserId(page, userId);

        return ApiSchedulePageResponse.dataResponse(response);
    }

    @GetMapping("/overview")
    @Operation(summary = "간단한 일정 목록 조회", description = "작성한 전체 일정을 필요 데이터로 구성해 조회합니다.")
    public ApiPageResponse<OverviewScheduleResponse> getOverviewScheduleByUserId(@RequestParam(name = "page") int page){
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        Page<OverviewScheduleResponse> response = scheduleService.getOverviewScheduleByUserId(page, userId);

        return ApiPageResponse.dataResponse(response);
    }

    @GetMapping("/search")
    @Operation(summary = "일정 검색", description = "작성한 전체 일정 중 검색합니다.")
    public ApiSchedulePageResponse<ScheduleInfoResponse> searchSchedules(@RequestParam(name = "page") int page,
                                                                         @RequestParam(name = "keyword") String keyword,
                                                                         @RequestParam(name = "type") ScheduleType type){
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        SchedulePageResponse<ScheduleInfoResponse> response;

        if (type == ScheduleType.all){
            response = scheduleService.searchAllSchedules(page, keyword, userId);
        } else {
            response = scheduleService.searchSharedSchedules(page, keyword, userId);
        }

        return ApiSchedulePageResponse.dataResponse(response);
    }


    @PostMapping
    @Operation(summary = "일정 생성", description = "여행 이름, 날짜를 선택해 일정을 생성합니다.")
    public ApiResponse<ScheduleCreateResponse> createSchedule(@Valid @RequestBody ScheduleCreateRequest scheduleCreateRequest){
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();

        ScheduleCreateResponse response = scheduleService.createSchedule(scheduleCreateRequest, userId);
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
    public ApiResponse<?> updateSchedule(@PathVariable(name = "scheduleId") Long scheduleId, @Valid @RequestBody ScheduleUpdateRequest scheduleUpdateRequest){
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();

        scheduleService.updateSchedule(userId, scheduleId, scheduleUpdateRequest);
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
