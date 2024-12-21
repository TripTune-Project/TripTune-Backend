package com.triptune.domain.travel.service;

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
import com.triptune.global.util.PageUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional
public class TravelService {

    private final TravelPlaceRepository travelPlaceRepository;
    private final TravelImageRepository travelImageRepository;

    private static final int RADIUS_SIZE = 5;

    public Page<PlaceDistanceResponse> getNearByTravelPlaces(PlaceLocationRequest placeLocationRequest, int page) {
        Pageable pageable = PageUtil.defaultPageable(page);
        Page<PlaceLocation> travelPlacePage = travelPlaceRepository.findNearByTravelPlaces(pageable, placeLocationRequest, RADIUS_SIZE);

        if (travelPlacePage.getTotalElements() != 0){
            for(PlaceLocation location: travelPlacePage){
                location.setTravelImageList(travelImageRepository.findByTravelPlacePlaceId(location.getPlaceId()));
            }
        }

        return travelPlacePage.map(PlaceDistanceResponse::from);
    }


    public Page<PlaceDistanceResponse> searchTravelPlaces(PlaceSearchRequest placeSearchRequest, int page) {
        Pageable pageable = PageUtil.defaultPageable(page);

        Page<PlaceLocation> travelPlacePage = travelPlaceRepository.searchTravelPlacesWithLocation(pageable, placeSearchRequest);

        for(PlaceLocation response: travelPlacePage){
            response.setTravelImageList(travelImageRepository.findByTravelPlacePlaceId(response.getPlaceId()));
        }

        return travelPlacePage.map(PlaceDistanceResponse::from);
    }


    public PlaceDetailResponse getTravelPlaceDetails(Long placeId) {
        TravelPlace travelPlace = travelPlaceRepository.findByPlaceId(placeId)
                .orElseThrow(()-> new DataNotFoundException(ErrorCode.DATA_NOT_FOUND));

        return PlaceDetailResponse.from(travelPlace);
    }


}
