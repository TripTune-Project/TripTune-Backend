package com.triptune.travel.controller;

import com.triptune.travel.dto.PlaceLocation;
import com.triptune.travel.dto.response.PlaceDetailResponse;
import com.triptune.travel.dto.request.PlaceLocationRequest;
import com.triptune.travel.dto.response.PlaceDistanceResponse;
import com.triptune.travel.dto.request.PlaceSearchRequest;
import com.triptune.travel.dto.response.PlaceResponse;
import com.triptune.travel.service.TravelService;
import com.triptune.global.response.pagination.ApiPageResponse;
import com.triptune.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/travels")
@Tag(name = "Travel Place", description = "여행지 탐색 관련 API")
@RequiredArgsConstructor
public class TravelController {

    private final TravelService travelService;

    @PostMapping
    @Operation(summary = "현재 위치와 가까운 여행지 목록 조회", description = "여행지 탐색 메뉴에서 사용자 현재 위치와 가까운 여행지 목록을 제공한다.")
    public ApiPageResponse<PlaceLocation> getNearByTravelPlaces(@RequestBody @Valid PlaceLocationRequest placeLocationRequest, @RequestParam int page){
        String userId = authenticateUserId();
        Page<PlaceLocation> response = travelService.getNearByTravelPlaces(page, userId, placeLocationRequest);
        return ApiPageResponse.dataResponse(response);
    }


    @PostMapping("/search")
    @Operation(summary = "여행지 검색", description = "여행지 탐색 메뉴에서 여행지를 검색한다.")
    public ApiPageResponse<PlaceLocation> searchTravelPlaces(@RequestBody @Valid PlaceSearchRequest placeSearchRequest, @RequestParam int page){
        String userId = authenticateUserId();

        boolean hasLocation = placeSearchRequest.getLongitude() != null && placeSearchRequest.getLatitude() != null;

        Page<PlaceLocation> response = hasLocation
                ? travelService.searchTravelPlacesWithLocation(page, userId, placeSearchRequest)
                : travelService.searchTravelPlacesWithoutLocation(page, userId, placeSearchRequest);

        return ApiPageResponse.dataResponse(response);
    }


    @GetMapping("/{placeId}")
    @Operation(summary = "여행지 상세조회", description = "여행지에 대한 자세한 정보를 조회한다.")
    public ApiResponse<PlaceDetailResponse> getTravelPlaceDetails(@PathVariable("placeId") Long placeId){
        String userId = authenticateUserId();
        PlaceDetailResponse response = travelService.getTravelPlaceDetails(placeId, userId);
        return ApiResponse.dataResponse(response);
    }

    public String authenticateUserId(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()){
            return authentication.getName();
        }

        return null;
    }

}
