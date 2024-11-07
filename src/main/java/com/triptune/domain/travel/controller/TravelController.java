package com.triptune.domain.travel.controller;

import com.triptune.domain.travel.dto.response.PlaceDetailResponse;
import com.triptune.domain.travel.dto.request.PlaceLocationRequest;
import com.triptune.domain.travel.dto.response.PlaceDistanceResponse;
import com.triptune.domain.travel.dto.request.PlaceSearchRequest;
import com.triptune.domain.travel.service.TravelService;
import com.triptune.global.response.pagination.ApiPageResponse;
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
public class TravelController {

    private final TravelService travelService;

    @PostMapping("")
    @Operation(summary = "현재 위치와 가까운 여행지 목록 조회", description = "여행지 탐색 메뉴에서 사용자 현재 위치와 가까운 여행지 목록을 제공한다.")
    public ApiPageResponse<PlaceDistanceResponse> getNearByTravelPlaces(@RequestBody @Valid PlaceLocationRequest placeLocationRequest, @RequestParam int page){
        Page<PlaceDistanceResponse> response = travelService.getNearByTravelPlaces(placeLocationRequest, page);
        return ApiPageResponse.dataResponse(response);
    }


    @PostMapping("/search")
    @Operation(summary = "여행지 검색", description = "여행지 탐색 메뉴에서 여행지를 검색하며 검색 결과는 현재 위치와 가까운 순으로 제공된다.")
    public ApiPageResponse<PlaceDistanceResponse> searchTravelPlaces(@RequestBody @Valid PlaceSearchRequest placeSearchRequest, @RequestParam int page){
        Page<PlaceDistanceResponse> response = travelService.searchTravelPlaces(placeSearchRequest, page);
        return ApiPageResponse.dataResponse(response);
    }


    @GetMapping("/{placeId}")
    @Operation(summary = "여행지 상세조회", description = "여행지에 대한 자세한 정보를 조회한다.")
    public ApiResponse<PlaceDetailResponse> getTravelPlaceDetails(@PathVariable("placeId") Long placeId){
        PlaceDetailResponse response = travelService.getTravelPlaceDetails(placeId);
        return ApiResponse.dataResponse(response);
    }


}
