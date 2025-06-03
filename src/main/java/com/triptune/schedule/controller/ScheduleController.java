package com.triptune.schedule.controller;

import com.triptune.schedule.dto.request.ScheduleCreateRequest;
import com.triptune.schedule.dto.request.ScheduleUpdateRequest;
import com.triptune.schedule.dto.response.OverviewScheduleResponse;
import com.triptune.schedule.dto.response.ScheduleCreateResponse;
import com.triptune.schedule.dto.response.ScheduleDetailResponse;
import com.triptune.schedule.dto.response.ScheduleInfoResponse;
import com.triptune.schedule.enums.ScheduleSearchType;
import com.triptune.schedule.service.ScheduleService;
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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/schedules")
@RequiredArgsConstructor
@Tag(name = "Schedule", description = "일정 만들기 관련 API")
public class ScheduleController {
    private final ScheduleService scheduleService;

    @GetMapping
    @Operation(summary = "전체 일정 목록 조회", description = "작성한 전체 일정을 조회합니다.")
    public ApiSchedulePageResponse<ScheduleInfoResponse> getAllSchedules(@AuthenticationPrincipal(expression = "memberId") Long memberId,
                                                                         @RequestParam(name = "page") int page){
        SchedulePageResponse<ScheduleInfoResponse> response = scheduleService.getAllSchedules(page, memberId);

        return ApiSchedulePageResponse.dataResponse(response);
    }

    @GetMapping("/shared")
    @Operation(summary = "공유된 일정 목록 조회", description = "작성한 일정 중 공유된 일정을 조회합니다.")
    public ApiSchedulePageResponse<ScheduleInfoResponse> getSharedSchedules(@AuthenticationPrincipal(expression = "memberId") Long memberId,
                                                                            @RequestParam(name = "page") int page){
        SchedulePageResponse<ScheduleInfoResponse> response = scheduleService.getSharedSchedules(page, memberId);

        return ApiSchedulePageResponse.dataResponse(response);
    }

    @GetMapping("/edit")
    @Operation(summary = "수정 권한 있는 일정 목록 조회", description = "작성한 일정 중 수정 권한이 있는 일정을 필요 데이터로 구성해 조회합니다.")
    public ApiPageResponse<OverviewScheduleResponse> getEnableEditSchedule(@AuthenticationPrincipal(expression = "memberId") Long memberId,
                                                                           @RequestParam(name = "page") int page){
        Page<OverviewScheduleResponse> response = scheduleService.getEnableEditSchedule(page, memberId);

        return ApiPageResponse.dataResponse(response);
    }

    @GetMapping("/search")
    @Operation(summary = "일정 검색", description = "작성한 전체 일정 중 검색합니다.")
    public ApiSchedulePageResponse<ScheduleInfoResponse> searchSchedules(@AuthenticationPrincipal(expression = "memberId") Long memberId,
                                                                         @RequestParam(name = "page") int page,
                                                                         @RequestParam(name = "keyword") String keyword,
                                                                         @RequestParam(name = "type") String type){
        ScheduleSearchType searchType = ScheduleSearchType.from(type);

        SchedulePageResponse<ScheduleInfoResponse> response =
                searchType.isAll()
                ? scheduleService.searchAllSchedules(page, keyword, memberId)
                : scheduleService.searchSharedSchedules(page, keyword, memberId);

        return ApiSchedulePageResponse.dataResponse(response);
    }


    @PostMapping
    @Operation(summary = "일정 생성", description = "여행 이름, 날짜를 선택해 일정을 생성합니다.")
    public ApiResponse<ScheduleCreateResponse> createSchedule(@AuthenticationPrincipal(expression = "memberId") Long memberId,
                                                              @Valid @RequestBody ScheduleCreateRequest scheduleCreateRequest){
        ScheduleCreateResponse response = scheduleService.createSchedule(scheduleCreateRequest, memberId);
        return ApiResponse.dataResponse(response);
    }


    @AttendeeCheck
    @GetMapping("/{scheduleId}")
    @Operation(summary = "일정 상세 조회", description = "생성한 일정을 조회합니다.")
    public ApiResponse<ScheduleDetailResponse> getScheduleDetail(@PathVariable(name = "scheduleId") Long scheduleId,
                                                                 @RequestParam int page){
         ScheduleDetailResponse response = scheduleService.getScheduleDetail(scheduleId, page);

         return ApiResponse.dataResponse(response);
    }

    @AttendeeCheck
    @PatchMapping("/{scheduleId}")
    @Operation(summary = "일정 수정", description = "일정 상세 화면에서 저장 버튼을 누르면 해당 일정이 수정됩니다. 회원은 저장 작업으로 보지만, 실제로는 일정 수정 작업입니다.")
    public ApiResponse<?> updateSchedule(@AuthenticationPrincipal(expression = "memberId") Long memberId,
                                         @PathVariable(name = "scheduleId") Long scheduleId,
                                         @Valid @RequestBody ScheduleUpdateRequest scheduleUpdateRequest){
        scheduleService.updateSchedule(memberId, scheduleId, scheduleUpdateRequest);
        return ApiResponse.okResponse();
    }

    @AttendeeCheck
    @DeleteMapping("/{scheduleId}")
    @Operation(summary = "일정 삭제", description = "일정을 삭제합니다.")
    public ApiResponse<?> deleteSchedule(@AuthenticationPrincipal(expression = "memberId") Long memberId,
                                         @PathVariable(name = "scheduleId") Long scheduleId){
        scheduleService.deleteSchedule(scheduleId, memberId);
        return ApiResponse.okResponse();
    }


}
