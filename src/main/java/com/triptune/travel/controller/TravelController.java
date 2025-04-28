package com.triptune.travel.controller;

import com.triptune.travel.dto.PlaceLocation;
import com.triptune.travel.dto.response.PlaceDetailResponse;
import com.triptune.travel.dto.request.PlaceLocationRequest;
import com.triptune.travel.dto.request.PlaceSearchRequest;
import com.triptune.travel.dto.response.PlaceSimpleResponse;
import com.triptune.travel.enumclass.CityType;
import com.triptune.travel.enumclass.ThemeType;
import com.triptune.travel.service.TravelService;
import com.triptune.global.response.pagination.ApiPageResponse;
import com.triptune.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/travels")
@Tag(name = "Travel Place", description = "여행지 탐색 관련 API")
@RequiredArgsConstructor
public class TravelController {

    private final TravelService travelService;

    @PostMapping
    @Operation(summary = "현재 위치와 가까운 여행지 목록 조회", description = "여행지 탐색 메뉴에서 사용자 현재 위치와 가까운 여행지 목록을 제공한다.")
    public ApiPageResponse<PlaceLocation> getNearByTravelPlaces(@RequestBody @Valid PlaceLocationRequest placeLocationRequest,
                                                                @RequestParam int page){
        Long memberId = getAuthenticateMemberId();
        Page<PlaceLocation> response = travelService.getNearByTravelPlaces(page, memberId, placeLocationRequest);
        return ApiPageResponse.dataResponse(response);
    }


    @PostMapping("/search")
    @Operation(summary = "여행지 검색", description = "여행지 탐색 메뉴에서 여행지를 검색한다.")
    public ApiPageResponse<PlaceLocation> searchTravelPlaces(@RequestBody @Valid PlaceSearchRequest placeSearchRequest,
                                                             @RequestParam int page){
        Long memberId = getAuthenticateMemberId();
        boolean hasLocation = placeSearchRequest.getLongitude() != null && placeSearchRequest.getLatitude() != null;

        Page<PlaceLocation> response = hasLocation
                ? travelService.searchTravelPlacesWithLocation(page, memberId, placeSearchRequest)
                : travelService.searchTravelPlacesWithoutLocation(page, memberId, placeSearchRequest);

        return ApiPageResponse.dataResponse(response);
    }


    @GetMapping("/{placeId}")
    @Operation(summary = "여행지 상세조회", description = "여행지에 대한 자세한 정보를 조회한다.")
    public ApiResponse<PlaceDetailResponse> getTravelPlaceDetails(@PathVariable("placeId") Long placeId){
        Long memberId = getAuthenticateMemberId();
        PlaceDetailResponse response = travelService.getTravelPlaceDetails(placeId, memberId);
        return ApiResponse.dataResponse(response);
    }

    @GetMapping("/popular")
    @Operation(summary = "지역별 인기 여행지 조회", description = "지역에 따른 인기 여행지 목록을 조회한다.")
    public ApiResponse<List<PlaceSimpleResponse>> getPopularTravelPlacesByCity(@RequestParam("city") String city){
        CityType cityType = CityType.from(city);
        List<PlaceSimpleResponse> response = travelService.getPopularTravelPlacesByCity(cityType);
        return ApiResponse.dataResponse(response);
    }

    @GetMapping("/recommend")
    @Operation(summary = "추천 테마별 여행지 조회", description = "여행 테마에 따른 여행지 목록을 조회한다.")
    public ApiResponse<List<PlaceSimpleResponse>> getRecommendTravelPlacesByTheme(@RequestParam("theme") String theme){
        ThemeType themeType = ThemeType.from(theme);
        List<PlaceSimpleResponse> response = travelService.getRecommendTravelPlacesByTheme(themeType);
        return ApiResponse.dataResponse(response);
    }

    public Long getAuthenticateMemberId(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && !(authentication instanceof AnonymousAuthenticationToken)){
            return Long.parseLong(authentication.getName());
        }

        return null;
    }

}
