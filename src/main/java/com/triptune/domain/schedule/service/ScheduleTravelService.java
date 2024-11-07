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

    /**
     * 중구를 기준으로 여행지 정보 조회
     * @param page : 페이지 수
     * @return Page<PlaceResponse>: 여행지 정보로 구성된 페이지 dto
     */
    public Page<PlaceResponse> getTravelPlaces(int page) {
        Pageable pageable = PageUtil.defaultPageable(page);

        return travelPlaceRepository.findAllByAreaData(pageable, "대한민국", "서울", "중구")
                .map(PlaceResponse::from);

    }


    /**
     * 여행지 검색
     * @param page: 페이지 수
     * @param keyword: 검색 키워드
     * @return Page<PlaceSimpleResponse>: 여행지 정보로 구성된 페이지 dto
     */
    public Page<PlaceResponse> searchTravelPlaces(int page, String keyword) {
        Pageable pageable = PageUtil.defaultPageable(page);
        Page<TravelPlace> travelPlaces = travelPlaceRepository.searchTravelPlaces(pageable, keyword);

        return travelPlaces.map(PlaceResponse::from);
    }

}
