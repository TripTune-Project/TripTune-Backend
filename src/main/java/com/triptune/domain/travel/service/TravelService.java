package com.triptune.domain.travel.service;

import com.triptune.domain.bookmark.repository.BookmarkRepository;
import com.triptune.domain.travel.dto.request.PlaceLocationRequest;
import com.triptune.domain.travel.dto.request.PlaceSearchRequest;
import com.triptune.domain.travel.dto.response.PlaceDetailResponse;
import com.triptune.domain.travel.dto.response.PlaceDistanceResponse;
import com.triptune.global.exception.DataNotFoundException;
import com.triptune.domain.travel.dto.*;
import com.triptune.domain.travel.entity.TravelPlace;
import com.triptune.domain.travel.repository.TravelImageRepository;
import com.triptune.domain.travel.repository.TravelPlaceRepository;
import com.triptune.global.enumclass.ErrorCode;
import com.triptune.global.util.PageUtils;
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
    private final TravelImageRepository travelImageRepository;
    private final BookmarkRepository bookmarkRepository;


    public Page<PlaceDistanceResponse> getNearByTravelPlaces(int page, String userId, PlaceLocationRequest placeLocationRequest) {
        Pageable pageable = PageUtils.defaultPageable(page);
        Page<PlaceLocation> travelPlacePage = travelPlaceRepository.findNearByTravelPlaces(pageable, placeLocationRequest, RADIUS_SIZE);

        setPlaceLocations(travelPlacePage.getContent(), userId);
        return travelPlacePage.map(PlaceDistanceResponse::from);
    }

    public Page<PlaceDistanceResponse> searchTravelPlaces(int page, String userId, PlaceSearchRequest placeSearchRequest) {
        Pageable pageable = PageUtils.defaultPageable(page);
        Page<PlaceLocation> travelPlacePage = travelPlaceRepository.searchTravelPlacesWithLocation(pageable, placeSearchRequest);

        setPlaceLocations(travelPlacePage.getContent(), userId);
        return travelPlacePage.map(PlaceDistanceResponse::from);
    }

    public void setPlaceLocations(List<PlaceLocation> placeLocations, String userId){
        placeLocations.forEach(this::updateThumbnailUrl);

        if (userId != null){
            placeLocations.forEach(location -> updateBookmarkStatus(userId, location));
        }

    }

    public void updateThumbnailUrl(PlaceLocation location){
        location.updateThumbnailUrl(travelImageRepository.findThumbnailUrlByPlaceId(location.getPlaceId()));
    }


    public void updateBookmarkStatus(String userId, PlaceLocation location){
        boolean isBookmark = bookmarkRepository.existsByMember_UserIdAndTravelPlace_PlaceId(userId, location.getPlaceId());

        if (isBookmark){
            location.updateBookmarkStatusTrue();
        }
    }

    public PlaceDetailResponse getTravelPlaceDetails(Long placeId, String userId) {
        TravelPlace travelPlace = travelPlaceRepository.findByPlaceId(placeId)
                .orElseThrow(()-> new DataNotFoundException(ErrorCode.DATA_NOT_FOUND));

        boolean isBookmark = false;

        if (userId != null){
            isBookmark = bookmarkRepository.existsByMember_UserIdAndTravelPlace_PlaceId(userId, placeId);
        }

        return PlaceDetailResponse.from(travelPlace, isBookmark);
    }


}
