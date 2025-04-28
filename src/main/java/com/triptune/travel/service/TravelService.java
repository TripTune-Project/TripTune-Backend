package com.triptune.travel.service;

import com.triptune.bookmark.repository.BookmarkRepository;
import com.triptune.global.enumclass.ErrorCode;
import com.triptune.global.exception.DataNotFoundException;
import com.triptune.global.util.PageUtils;
import com.triptune.travel.dto.PlaceLocation;
import com.triptune.travel.dto.request.PlaceLocationRequest;
import com.triptune.travel.dto.request.PlaceSearchRequest;
import com.triptune.travel.dto.response.PlaceDetailResponse;
import com.triptune.travel.dto.response.PlaceResponse;
import com.triptune.travel.dto.response.PlaceSimpleResponse;
import com.triptune.travel.entity.TravelPlace;
import com.triptune.travel.enumclass.CityType;
import com.triptune.travel.enumclass.ThemeType;
import com.triptune.travel.repository.TravelPlaceRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class TravelService {
    private static final int RADIUS_SIZE = 5;

    private final TravelPlaceRepository travelPlaceRepository;
    private final BookmarkRepository bookmarkRepository;


    public Page<PlaceLocation> getNearByTravelPlaces(int page, Long memberId, PlaceLocationRequest placeLocationRequest) {
        Pageable pageable = PageUtils.defaultPageable(page);
        Page<PlaceLocation> placeResponsePage = travelPlaceRepository.findNearByTravelPlaces(pageable, placeLocationRequest, RADIUS_SIZE);

        markBookmarkedTravelPlaces(placeResponsePage.getContent(), memberId);
        return placeResponsePage;
    }

    public Page<PlaceLocation> searchTravelPlacesWithLocation(int page, Long memberId, PlaceSearchRequest placeSearchRequest) {
        Pageable pageable = PageUtils.defaultPageable(page);
        Page<PlaceLocation> placeResponsePage = travelPlaceRepository.searchTravelPlacesWithLocation(pageable, placeSearchRequest);

        markBookmarkedTravelPlaces(placeResponsePage.getContent(), memberId);
        return placeResponsePage;
    }

    public Page<PlaceLocation> searchTravelPlacesWithoutLocation(int page, Long memberId, PlaceSearchRequest placeSearchRequest) {
        Pageable pageable = PageUtils.defaultPageable(page);
        Page<PlaceResponse> placeResponsePage = travelPlaceRepository.searchTravelPlaces(pageable, placeSearchRequest.getKeyword());

        Page<PlaceLocation> placeLocationPage = placeResponsePage.map(PlaceLocation::from);
        markBookmarkedTravelPlaces(placeLocationPage.getContent(), memberId);
        return placeLocationPage;
    }

    public void markBookmarkedTravelPlaces(List<PlaceLocation> placeResponses, Long memberId){
        if (memberId != null){
            placeResponses.forEach(placeResponse -> updateBookmarkStatus(memberId, placeResponse));
        }
    }

    public void updateBookmarkStatus(Long memberId, PlaceLocation placeResponse){
        boolean isBookmark = bookmarkRepository.existsByMember_MemberIdAndTravelPlace_PlaceId(memberId, placeResponse.getPlaceId());

        if (isBookmark){
            placeResponse.updateBookmarkStatusTrue();
        }
    }

    public PlaceDetailResponse getTravelPlaceDetails(Long placeId, Long memberId) {
        TravelPlace travelPlace = travelPlaceRepository.findById(placeId)
                .orElseThrow(()-> new DataNotFoundException(ErrorCode.DATA_NOT_FOUND));

        boolean isBookmark = false;

        if (memberId != null){
            isBookmark = bookmarkRepository.existsByMember_MemberIdAndTravelPlace_PlaceId(memberId, placeId);
        }

        return PlaceDetailResponse.from(travelPlace, isBookmark);
    }

    public Page<PlaceResponse> getTravelPlacesByJungGu(int page) {
        Pageable pageable = PageUtils.travelPageable(page);
        return travelPlaceRepository.findAllByAreaData(pageable, "대한민국", "서울", "중구");
    }


    public Page<PlaceResponse> searchTravelPlaces(int page, String keyword) {
        Pageable pageable = PageUtils.travelPageable(page);
        return travelPlaceRepository.searchTravelPlaces(pageable, keyword);
    }

    public List<PlaceSimpleResponse> getPopularTravelPlacesByCity(CityType cityType) {
        return travelPlaceRepository.findPopularTravelPlacesByCity(cityType);
    }

    public List<PlaceSimpleResponse> getRecommendTravelPlacesByTheme(ThemeType themeType) {
        return travelPlaceRepository.findRecommendTravelPlacesByTheme(themeType);
    }
}
