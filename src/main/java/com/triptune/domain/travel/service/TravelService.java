package com.triptune.domain.travel.service;

import com.triptune.domain.travel.dto.TravelLocationRequest;
import com.triptune.domain.travel.dto.TravelResponse;
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

    public Page<TravelResponse> travelPlaceList(TravelLocationRequest travelLocationRequest, Pageable pageable) {
        Page<TravelResponse> travelPlaceList = travelRepository.findAllByCountryCountryNameAndCityCityNameAndDistrictDistrictName(pageable, "대한민국", "서울", "중구")
                .map(TravelResponse::fromEntity);

        return travelPlaceList;
    }
}
