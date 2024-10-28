package com.triptune.domain.schedule.controller;

import com.triptune.domain.schedule.dto.request.CreateScheduleRequest;
import com.triptune.domain.schedule.dto.request.UpdateScheduleRequest;
import com.triptune.domain.schedule.dto.response.CreateScheduleResponse;
import com.triptune.domain.schedule.dto.response.RouteResponse;
import com.triptune.domain.schedule.dto.response.ScheduleDetailResponse;
import com.triptune.domain.schedule.dto.response.ScheduleInfoResponse;
import com.triptune.domain.schedule.service.ScheduleService;
import com.triptune.domain.travel.dto.response.PlaceResponse;
import com.triptune.global.response.SuccessResponse;
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
public class ScheduleApiController {

    private final ScheduleService scheduleService;

    @GetMapping("")
    @Operation(summary = "일정 목록 조회", description = "작성한 일정 목록을 조회합니다.")
    public ApiSchedulePageResponse<ScheduleInfoResponse> getSchedules(@RequestParam(name = "page") int page){
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();

        SchedulePageResponse<ScheduleInfoResponse> response = scheduleService.getSchedules(page, userId);

        return ApiSchedulePageResponse.dataResponse(response);
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
    public ApiResponse<ScheduleDetailResponse> getScheduleDetail(@PathVariable(name = "scheduleId") Long scheduleId, @RequestParam int page){
         ScheduleDetailResponse response = scheduleService.getScheduleDetail(scheduleId, page);

         return ApiResponse.dataResponse(response);
    }

    @PatchMapping("/{scheduleId}")
    @Operation(summary = "일정 수정", description = "일정 상세 화면에서 저장 버튼을 누르면 해당 일정이 수정됩니다. 사용자는 저장 작업으로 보지만, 실제로는 일정 수정 작업입니다.")
    public ApiResponse<?> updateSchedule(@PathVariable(name = "scheduleId") Long scheduleId, @RequestBody UpdateScheduleRequest updateScheduleRequest){
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();

        scheduleService.updateSchedule(userId, scheduleId, updateScheduleRequest);
        return ApiResponse.okResponse();
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
