package com.triptune.domain.travel.service;

import com.triptune.global.exception.DataNotFoundException;
import com.triptune.domain.travel.dto.*;
import com.triptune.domain.travel.entity.TravelPlace;
import com.triptune.domain.travel.repository.TravelImageRepository;
import com.triptune.domain.travel.repository.TravelRepository;
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

    private final TravelRepository travelRepository;
    private final TravelImageRepository travelImageRepository;

    /**
     * 현재 위치에 따른 여행지 목록 제공
     * @param travelLocationRequest
     * @param page
     * @return Page<TravelResponse>
     */
    public Page<TravelResponse> getNearByTravelPlaces(TravelLocationRequest travelLocationRequest, int page) {
        Pageable pageable = PageRequest.of(page - 1, 5);

        Page<TravelLocation> travelPlacePage = travelRepository.findNearByTravelPlaces(pageable, travelLocationRequest, 5);

        if (travelPlacePage.getTotalElements() != 0){
            for(TravelLocation response: travelPlacePage){
                response.setTravelImageFileList(travelImageRepository.findByTravelPlacePlaceId(response.getPlaceId()));
            }
        }

        return travelPlacePage.map(TravelResponse::entityToLocationDto);
    }

    /**
     * 여행지 검색
     * @param travelSearchRequest
     * @param page
     * @return Page<TravelResponse>
     */
    public Page<TravelResponse> searchTravelPlaces(TravelSearchRequest travelSearchRequest, int page) {
        Pageable pageable = PageRequest.of(page - 1, 5);

        Page<TravelLocation> travelPlacePage = travelRepository.searchTravelPlaces(pageable, travelSearchRequest);

        for(TravelLocation response: travelPlacePage){
            response.setTravelImageFileList(travelImageRepository.findByTravelPlacePlaceId(response.getPlaceId()));
        }

        return travelPlacePage.map(TravelResponse::entityToLocationDto);
    }

    /**
     * 여행지 상세보기
     * @param placeId
     * @return TravelDetailResponse
     */
    public TravelDetailResponse getTravelPlaceDetails(Long placeId) {
        TravelPlace travelPlace = travelRepository.findByPlaceId(placeId)
                .orElseThrow(()-> new DataNotFoundException(ErrorCode.DATA_NOT_FOUND));

        return TravelDetailResponse.entityToDto(travelPlace);
    }
}
