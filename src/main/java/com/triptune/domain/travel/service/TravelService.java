package com.triptune.domain.travel.service;

import com.triptune.global.exception.DataNotFoundException;
import com.triptune.domain.travel.dto.*;
import com.triptune.domain.travel.entity.TravelPlace;
import com.triptune.domain.travel.repository.TravelImageRepository;
import com.triptune.domain.travel.repository.TravelPlacePlaceRepository;
import com.triptune.global.enumclass.ErrorCode;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional
public class TravelService {

    private final TravelPlacePlaceRepository travelPlaceRepository;
    private final TravelImageRepository travelImageRepository;

    /**
     * 현재 위치에 따른 여행지 목록 제공
     * @param placeLocationRequest
     * @param page
     * @return Page<TravelResponse>
     */
    public Page<PlaceResponse> getNearByTravelPlaces(PlaceLocationRequest placeLocationRequest, int page) {
        Pageable pageable = PageRequest.of(page - 1, 5);

        Page<PlaceLocation> travelPlacePage = travelPlaceRepository.findNearByTravelPlaces(pageable, placeLocationRequest, 5);

        if (travelPlacePage.getTotalElements() != 0){
            for(PlaceLocation response: travelPlacePage){
                response.setTravelImageFileList(travelImageRepository.findByTravelPlacePlaceId(response.getPlaceId()));
            }
        }

        return travelPlacePage.map(PlaceResponse::entityToLocationDto);
    }

    /**
     * 여행지 검색
     * @param placeSearchRequest
     * @param page
     * @return Page<TravelResponse>
     */
    public Page<PlaceResponse> searchTravelPlaces(PlaceSearchRequest placeSearchRequest, int page) {
        Pageable pageable = PageRequest.of(page - 1, 5);

        Page<PlaceLocation> travelPlacePage = travelPlaceRepository.searchTravelPlacesWithLocation(pageable, placeSearchRequest);

        for(PlaceLocation response: travelPlacePage){
            response.setTravelImageFileList(travelImageRepository.findByTravelPlacePlaceId(response.getPlaceId()));
        }

        return travelPlacePage.map(PlaceResponse::entityToLocationDto);
    }

    /**
     * 여행지 상세보기
     * @param placeId
     * @return TravelDetailResponse
     */
    public PlaceDetailResponse getTravelPlaceDetails(Long placeId) {
        TravelPlace travelPlace = travelPlaceRepository.findByPlaceId(placeId)
                .orElseThrow(()-> new DataNotFoundException(ErrorCode.DATA_NOT_FOUND));

        return PlaceDetailResponse.entityToDto(travelPlace);
    }
}
