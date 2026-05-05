package com.triptune.travel.service;

import com.triptune.bookmark.repository.BookmarkRepository;
import com.triptune.global.message.ErrorCode;
import com.triptune.global.exception.DataNotFoundException;
import com.triptune.global.s3.S3ObjectManager;
import com.triptune.global.util.PageUtils;
import com.triptune.travel.dto.response.*;
import com.triptune.travel.dto.request.PlaceLocationRequest;
import com.triptune.travel.dto.request.PlaceSearchRequest;
import com.triptune.travel.entity.TravelPlace;
import com.triptune.travel.enums.CityType;
import com.triptune.travel.enums.ThemeType;
import com.triptune.travel.repository.TravelPlaceRepository;
import com.triptune.travel.repository.dto.PlaceDistanceQueryDto;
import com.triptune.travel.repository.dto.PlaceQueryDto;
import com.triptune.travel.repository.dto.PlaceSimpleQueryDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class TravelService {
    private static final int RADIUS_SIZE = 5;

    private final TravelPlaceRepository travelPlaceRepository;
    private final BookmarkRepository bookmarkRepository;
    private final S3ObjectManager s3ObjectManager;


    public Page<PlaceDistanceResponse> getNearByTravelPlaces(int page, Long memberId, PlaceLocationRequest placeLocationRequest) {
        Pageable pageable = PageUtils.defaultPageable(page);
        Page<PlaceDistanceQueryDto> placePage = travelPlaceRepository.findNearByTravelPlaces(pageable, placeLocationRequest, RADIUS_SIZE);
        return toPlaceDistanceResponse(placePage, memberId);

    }


    public Page<PlaceDistanceResponse> searchTravelPlacesWithLocation(int page, Long memberId, PlaceSearchRequest placeSearchRequest) {
        Pageable pageable = PageUtils.defaultPageable(page);
        Page<PlaceDistanceQueryDto> placePage = travelPlaceRepository.searchTravelPlacesWithLocation(pageable, placeSearchRequest);
        return toPlaceDistanceResponse(placePage, memberId);
    }


    public Page<PlaceDistanceResponse> searchTravelPlacesWithoutLocation(int page, Long memberId, PlaceSearchRequest placeSearchRequest) {
        Pageable pageable = PageUtils.defaultPageable(page);
        Page<PlaceDistanceQueryDto> placePage = travelPlaceRepository.searchTravelPlacesWithoutLocation(pageable, placeSearchRequest.getKeyword());
        return toPlaceDistanceResponse(placePage, memberId);
    }


    private Page<PlaceDistanceResponse> toPlaceDistanceResponse(Page<PlaceDistanceQueryDto> placePage, Long memberId){
        List<PlaceDistanceResponse> placeResponses = placePage.getContent().stream()
                .map(place -> {
                    String thumbnailUrl = s3ObjectManager.generateS3ObjectUrl(place.getThumbnailS3ObjectKey());
                    return PlaceDistanceResponse.of(place, thumbnailUrl);
                }).toList();

        markBookmarkedTravelPlaces(placeResponses, memberId);
        return PageUtils.createPage(placeResponses, placePage.getPageable(), placePage.getTotalElements());
    }


    public void markBookmarkedTravelPlaces(List<PlaceDistanceResponse> placeResponses, Long memberId){
        if (memberId != null){
            placeResponses.forEach(placeResponse -> markBookmarkedPlace(memberId, placeResponse));
        }
    }

    public void markBookmarkedPlace(Long memberId, PlaceDistanceResponse placeResponse){
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

        List<TravelImageResponse> travelImageResponses = travelPlace.getTravelImages().stream()
                .map(image -> {
                    String imageUrl = s3ObjectManager.generateS3ObjectUrl(image.getS3ObjectKey());
                    return TravelImageResponse.of(image, imageUrl);
                }).toList();

        return PlaceDetailResponse.of(travelPlace, travelImageResponses, isBookmark);
    }

    public Page<PlaceResponse> getTravelPlacesByJungGu(int page) {
        Pageable pageable = PageUtils.travelPageable(page);
        Page<PlaceQueryDto> placePage = travelPlaceRepository.findNearbyTravelPlacesFromJungGu(pageable);
        return toPlaceResponse(placePage);
    }

    public Page<PlaceResponse> searchTravelPlaces(int page, String keyword) {
        Pageable pageable = PageUtils.travelPageable(page);
        Page<PlaceQueryDto> placePage = travelPlaceRepository.searchTravelPlaces(pageable, keyword);
        return toPlaceResponse(placePage);
    }

    private Page<PlaceResponse> toPlaceResponse(Page<PlaceQueryDto> placePage){
        List<PlaceResponse> placeResponses = placePage.getContent().stream()
                .map(place -> {
                    String thumbnailUrl = s3ObjectManager.generateS3ObjectUrl(place.getThumbnailS3ObjectKey());
                    return PlaceResponse.of(place, thumbnailUrl);
                }).toList();

        return PageUtils.createPage(placeResponses, placePage.getPageable(), placePage.getTotalElements());
    }

    public List<PlaceSimpleResponse> getPopularTravelPlacesByCity(CityType cityType) {
        List<PlaceSimpleQueryDto> places = travelPlaceRepository.findPopularTravelPlaces(cityType);
        return toPlaceSimpleResponse(places);
    }

    public List<PlaceSimpleResponse> getRecommendTravelPlacesByTheme(ThemeType themeType) {
        List<PlaceSimpleQueryDto> places = travelPlaceRepository.findRecommendTravelPlaces(themeType);
        return toPlaceSimpleResponse(places);
    }

    private List<PlaceSimpleResponse> toPlaceSimpleResponse(List<PlaceSimpleQueryDto> places) {
        return places.stream()
                .map(place -> {
                    String thumbnailUrl = s3ObjectManager.generateS3ObjectUrl(place.getThumbnailS3ObjectKey());
                    return PlaceSimpleResponse.of(place, thumbnailUrl);
                }).toList();
    }

}
