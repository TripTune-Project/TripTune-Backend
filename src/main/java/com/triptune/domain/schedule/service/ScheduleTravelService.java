package com.triptune.domain.schedule.service;

import com.triptune.domain.travel.dto.response.PlaceResponse;
import com.triptune.domain.travel.entity.TravelPlace;
import com.triptune.domain.travel.repository.TravelPlaceRepository;
import com.triptune.global.util.PageUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional
public class ScheduleTravelService {

    private final TravelPlaceRepository travelPlaceRepository;

    public Page<PlaceResponse> getTravelPlaces(int page) {
        Pageable pageable = PageUtil.travelPageable(page);

        return travelPlaceRepository.findAllByAreaData(pageable, "대한민국", "서울", "중구")
                .map(PlaceResponse::from);
    }

    public Page<PlaceResponse> searchTravelPlaces(int page, String keyword) {
        Pageable pageable = PageUtil.travelPageable(page);
        Page<TravelPlace> travelPlaces = travelPlaceRepository.searchTravelPlaces(pageable, keyword);

        return travelPlaces.map(PlaceResponse::from);
    }

}
