package com.triptune.domain.travel.service;

import com.triptune.domain.travel.dto.TravelLocationRequest;
import com.triptune.domain.travel.dto.TravelLocationResponse;
import com.triptune.domain.travel.dto.TravelResponse;
import com.triptune.domain.travel.dto.TravelSearchRequest;
import com.triptune.domain.travel.repository.TravelImageFileRepository;
import com.triptune.domain.travel.repository.TravelRepository;
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
    private final TravelImageFileRepository travelImageFileRepository;

    /**
     * 현재 위치에 따른 여행지 목록 제공
     * @param travelLocationRequest
     * @param page
     * @return Page<TravelResponse>
     */
    public Page<TravelResponse> findNearByTravelPlaces(TravelLocationRequest travelLocationRequest, int page) {
        Pageable pageable = PageRequest.of(page - 1, 5);

        Page<TravelLocationResponse> travelPlacePage = travelRepository.findNearByTravelPlaces(pageable, travelLocationRequest, 5);

        if (travelPlacePage.getTotalElements() != 0){
            for(TravelLocationResponse response: travelPlacePage){
                response.setTravelImageFileList(travelImageFileRepository.findByTravelPlacePlaceId(response.getPlaceId()));
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

        Page<TravelLocationResponse> travelPlacePage = travelRepository.searchTravelPlaces(pageable, travelSearchRequest);

        for(TravelLocationResponse response: travelPlacePage){
            response.setTravelImageFileList(travelImageFileRepository.findByTravelPlacePlaceId(response.getPlaceId()));
        }

        return travelPlacePage.map(TravelResponse::entityToLocationDto);
    }
}
