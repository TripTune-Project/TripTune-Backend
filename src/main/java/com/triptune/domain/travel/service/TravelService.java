package com.triptune.domain.travel.service;

import com.triptune.domain.common.exception.DataNotFoundException;
import com.triptune.domain.travel.dto.TravelLocationRequest;
import com.triptune.domain.travel.dto.TravelLocationResponse;
import com.triptune.domain.travel.dto.TravelResponse;
import com.triptune.domain.travel.entity.TravelImageFile;
import com.triptune.domain.travel.entity.TravelPlace;
import com.triptune.domain.travel.repository.TravelImageFileRepository;
import com.triptune.domain.travel.repository.TravelRepository;
import com.triptune.global.enumclass.ErrorCode;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class TravelService {

    private final TravelRepository travelRepository;
    private final TravelImageFileRepository travelImageFileRepository;

    public Page<TravelResponse> findNearByTravelPlaceList(TravelLocationRequest travelLocationRequest, int page) {
        Pageable pageable = PageRequest.of(page - 1, 5);

        Page<TravelLocationResponse> travelPlacePage = travelRepository.findNearByTravelPlaceList(pageable, travelLocationRequest, 5);

        if (travelPlacePage.getTotalElements() == 0){
            throw new DataNotFoundException(ErrorCode.NO_SEARCH_RESULTS_FOUND);
        }

        for(TravelLocationResponse response: travelPlacePage){
            response.setTravelImageFileList(travelImageFileRepository.findByTravelPlacePlaceId(response.getPlaceId()));
        }

        return travelPlacePage.map(TravelResponse::entityToLocationDto);
    }

    public Page<TravelResponse> searchTravelPlace(String type, String keyword, int page) {
        Pageable pageable = PageRequest.of(page - 1, 5);

        Page<TravelPlace> travelPlacePage = travelRepository.searchTravelPlace(pageable, type, keyword);

        if (travelPlacePage.getTotalElements() == 0){
            throw new DataNotFoundException(ErrorCode.NO_SEARCH_RESULTS_FOUND);
        }

        return travelPlacePage.map(TravelResponse::entityToDto);
    }
}
