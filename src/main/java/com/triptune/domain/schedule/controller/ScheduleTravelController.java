package com.triptune.domain.schedule.controller;

import com.triptune.domain.schedule.service.ScheduleTravelService;
import com.triptune.domain.travel.dto.response.PlaceResponse;
import com.triptune.global.aop.AttendeeCheck;
import com.triptune.global.response.pagination.ApiPageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/schedules/{scheduleId}")
@RequiredArgsConstructor
@Tag(name = "Schedule - Travel Place", description = "일정 만들기 중 여행지 관련 API")
public class ScheduleTravelController {

    private final ScheduleTravelService scheduleTravelService;

    @AttendeeCheck
    @GetMapping("/travels")
    @Operation(summary = "여행지 조회", description = "여행지 탭에서 여행지를 제공합니다.")
    public ApiPageResponse<PlaceResponse> getTravelPlaces(@PathVariable(name = "scheduleId") Long scheduleId, @RequestParam int page){
        Page<PlaceResponse> response = scheduleTravelService.getTravelPlaces(page);

        return ApiPageResponse.dataResponse(response);
    }

    @AttendeeCheck
    @GetMapping("/travels/search")
    @Operation(summary = "여행지 검색", description = "여행지 탭에서 여행지를 검색합니다.")
    public ApiPageResponse<PlaceResponse> searchTravelPlaces(@PathVariable(name = "scheduleId") Long scheduleId,
                                                             @RequestParam(name = "page") int page,
                                                             @RequestParam(name = "keyword") String keyword){

        Page<PlaceResponse> response = scheduleTravelService.searchTravelPlaces(page, keyword);
        return ApiPageResponse.dataResponse(response);
    }

}
