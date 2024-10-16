package com.triptune.domain.travel.controller;

import com.triptune.domain.travel.dto.TravelDetailResponse;
import com.triptune.domain.travel.dto.TravelLocationRequest;
import com.triptune.domain.travel.dto.TravelResponse;
import com.triptune.domain.travel.dto.TravelSearchRequest;
import com.triptune.domain.travel.service.TravelService;
import com.triptune.global.response.ApiPageResponse;
import com.triptune.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/travels")
@Tag(name = "Travel Place", description = "여행지 탐색 관련 API")
@RequiredArgsConstructor
public class TravelApiController {

    private final TravelService travelService;

    @PostMapping("")
    @Operation(summary = "현재 위치와 가까운 여행지 목록 조회", description = "여행지 탐색 메뉴에서 사용자 현재 위치와 가까운 여행지 목록을 제공한다.")
    public ApiPageResponse<TravelResponse> getNearByTravelPlaces(@RequestBody @Valid TravelLocationRequest travelLocationRequest, @RequestParam int page){
        Page<TravelResponse> response = travelService.getNearByTravelPlaces(travelLocationRequest, page);
        return ApiPageResponse.okResponse(response);
    }


    @PostMapping("/search")
    @Operation(summary = "여행지 검색", description = "여행지 탐색 메뉴에서 여행지를 검색한다.")
    public ApiPageResponse<TravelResponse> searchTravelPlaces(@RequestBody @Valid TravelSearchRequest travelSearchRequest, @RequestParam int page){
        Page<TravelResponse> response = travelService.searchTravelPlaces(travelSearchRequest, page);
        return ApiPageResponse.okResponse(response);
    }


    @GetMapping("/{placeId}")
    @Operation(summary = "여행지 상세조회", description = "여행지에 대한 자세한 정보를 조회한다.")
    public ApiResponse<TravelDetailResponse> getTravelPlaceDetails(@PathVariable("placeId") Long placeId){
        TravelDetailResponse response = travelService.getTravelPlaceDetails(placeId);
        return ApiResponse.dataResponse(response);
    }


}
