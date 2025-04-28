package com.triptune.schedule.controller;

import com.triptune.travel.dto.response.PlaceResponse;
import com.triptune.global.aop.AttendeeCheck;
import com.triptune.global.response.pagination.ApiPageResponse;
import com.triptune.travel.service.TravelService;
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

    private final TravelService travelService;

    @AttendeeCheck
    @GetMapping("/travels")
    @Operation(summary = "여행지 조회", description = "일정 상세보기 여행지 탭에서 여행지를 제공합니다.")
    public ApiPageResponse<PlaceResponse> getTravelPlaces(@PathVariable(name = "scheduleId") Long scheduleId,
                                                          @RequestParam int page){
        Page<PlaceResponse> response = travelService.getTravelPlacesByJungGu(page);

        return ApiPageResponse.dataResponse(response);
    }

    @AttendeeCheck
    @GetMapping("/travels/search")
    @Operation(summary = "여행지 검색", description = "일정 상세보기 여행지 탭에서 여행지를 검색합니다.")
    public ApiPageResponse<PlaceResponse> searchTravelPlaces(@PathVariable(name = "scheduleId") Long scheduleId,
                                                             @RequestParam(name = "page") int page,
                                                             @RequestParam(name = "keyword") String keyword){

        Page<PlaceResponse> response = travelService.searchTravelPlaces(page, keyword);
        return ApiPageResponse.dataResponse(response);
    }

}
