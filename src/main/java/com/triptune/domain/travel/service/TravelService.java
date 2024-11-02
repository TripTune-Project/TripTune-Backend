package com.triptune.domain.travel.service;

import com.triptune.domain.travel.dto.request.PlaceLocationRequest;
import com.triptune.domain.travel.dto.request.PlaceSearchRequest;
import com.triptune.domain.travel.dto.response.PlaceDetailResponse;
import com.triptune.domain.travel.dto.response.PlaceDistanceResponse;
import com.triptune.global.exception.DataNotFoundException;
import com.triptune.domain.travel.dto.*;
import com.triptune.domain.travel.entity.TravelPlace;
import com.triptune.domain.travel.repository.TravelImageRepository;
import com.triptune.domain.travel.repository.TravelPlacePlaceRepository;
import com.triptune.global.enumclass.ErrorCode;
import com.triptune.global.util.PageUtil;
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

    private static final int RADIUS_SIZE = 5;

    /**
     * 현재 위치에 따른 여행지 목록 제공
     * @param placeLocationRequest: 사용자의 현재 위치인 위도 경도가 포함되어 있는 dto
     * @param page: 페이지 수
     * @return Page<TravelResponse>: 사용자 현재 위치 기준 여행지 정보가 담긴 Page 객체
     */
    public Page<PlaceDistanceResponse> getNearByTravelPlaces(PlaceLocationRequest placeLocationRequest, int page) {
        Pageable pageable = PageUtil.defaultPageable(page);
        Page<PlaceLocation> travelPlacePage = travelPlaceRepository.findNearByTravelPlaces(pageable, placeLocationRequest, RADIUS_SIZE);

        if (travelPlacePage.getTotalElements() != 0){
            for(PlaceLocation location: travelPlacePage){
                location.setTravelImageList(travelImageRepository.findByTravelPlacePlaceId(location.getPlaceId()));
            }
        }

        return travelPlacePage.map(PlaceDistanceResponse::entityToLocationDto);
    }

    /**
     * 여행지 검색
     * @param placeSearchRequest: 사용자의 현재 위치 정보와 검색 키워드가 담긴 dto
     * @param page: 페이지 수
     * @return Page<TravelResponse>: 현재 위치와 키워드 기준으로 검색한 여행지 정보 담긴 Page 객체
     */
    public Page<PlaceDistanceResponse> searchTravelPlaces(PlaceSearchRequest placeSearchRequest, int page) {
        Pageable pageable = PageUtil.defaultPageable(page);

        Page<PlaceLocation> travelPlacePage = travelPlaceRepository.searchTravelPlacesWithLocation(pageable, placeSearchRequest);

        for(PlaceLocation response: travelPlacePage){
            response.setTravelImageList(travelImageRepository.findByTravelPlacePlaceId(response.getPlaceId()));
        }

        return travelPlacePage.map(PlaceDistanceResponse::entityToLocationDto);
    }

    /**
     * 여행지 상세보기
     * @param placeId: 여행지 인덱스
     * @return TravelDetailResponse: 여행지 상세정보 담긴 dto
     */
    public PlaceDetailResponse getTravelPlaceDetails(Long placeId) {
        TravelPlace travelPlace = travelPlaceRepository.findByPlaceId(placeId)
                .orElseThrow(()-> new DataNotFoundException(ErrorCode.DATA_NOT_FOUND));

        return PlaceDetailResponse.entityToDto(travelPlace);
    }


}
