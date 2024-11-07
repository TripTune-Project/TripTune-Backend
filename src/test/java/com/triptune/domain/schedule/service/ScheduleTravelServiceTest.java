package com.triptune.domain.schedule.service;

import com.triptune.domain.common.entity.ApiCategory;
import com.triptune.domain.common.entity.City;
import com.triptune.domain.common.entity.Country;
import com.triptune.domain.common.entity.District;
import com.triptune.domain.member.entity.Member;
import com.triptune.domain.member.entity.ProfileImage;
import com.triptune.domain.member.repository.MemberRepository;
import com.triptune.domain.schedule.ScheduleTest;
import com.triptune.domain.schedule.entity.TravelAttendee;
import com.triptune.domain.schedule.entity.TravelRoute;
import com.triptune.domain.schedule.entity.TravelSchedule;
import com.triptune.domain.schedule.enumclass.AttendeePermission;
import com.triptune.domain.schedule.enumclass.AttendeeRole;
import com.triptune.domain.schedule.repository.TravelAttendeeRepository;
import com.triptune.domain.schedule.repository.TravelRouteRepository;
import com.triptune.domain.schedule.repository.TravelScheduleRepository;
import com.triptune.domain.travel.dto.response.PlaceResponse;
import com.triptune.domain.travel.entity.TravelImage;
import com.triptune.domain.travel.entity.TravelPlace;
import com.triptune.domain.travel.repository.TravelPlaceRepository;
import com.triptune.global.enumclass.ErrorCode;
import com.triptune.global.exception.DataNotFoundException;
import com.triptune.global.util.PageUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ScheduleTravelServiceTest extends ScheduleTest {
    @InjectMocks
    private ScheduleTravelService scheduleTravelService;

    @Mock
    private TravelPlaceRepository travelPlaceRepository;


    private TravelPlace travelPlace1;
    private TravelPlace travelPlace2;

    @BeforeEach
    void setUp(){
        Country country = createCountry();
        City city = createCity(country);
        District district = createDistrict(city, "중구");
        ApiCategory apiCategory = createApiCategory();
        travelPlace1 = createTravelPlace(1L, country, city, district, apiCategory);
        TravelImage travelImage1 = createTravelImage(travelPlace1, "test1", true);
        TravelImage travelImage2 = createTravelImage(travelPlace1, "test2", false);
        travelPlace1.setTravelImageList(new ArrayList<>(List.of(travelImage1, travelImage2)));

        travelPlace2 = createTravelPlace(2L, country, city, district, apiCategory);
        TravelImage travelImage3 = createTravelImage(travelPlace2, "test1", true);
        TravelImage travelImage4 = createTravelImage(travelPlace2, "test2", false);
        travelPlace2.setTravelImageList(new ArrayList<>(List.of(travelImage3, travelImage4)));
    }



    @Test
    @DisplayName("getTravelPlaces(): 여행지 조회 성공")
    void getTravelPlaces(){
        // given
        List<TravelPlace> placeList = new ArrayList<>(List.of(travelPlace1, travelPlace2));
        Pageable pageable = PageUtil.defaultPageable(1);

        when(travelPlaceRepository.findAllByAreaData(any(), anyString(), anyString(), anyString()))
                .thenReturn(PageUtil.createPage(placeList, pageable, 1));

        // when
        Page<PlaceResponse> response = scheduleTravelService.getTravelPlaces(1);

        // then
        assertEquals(response.getTotalElements(), placeList.size());
        assertEquals(response.getContent().get(0).getPlaceName(), placeList.get(0).getPlaceName());
        assertNotNull(response.getContent().get(0).getThumbnailUrl());
    }

    @Test
    @DisplayName("getTravelPlaces(): 여행지 조회 시 여행지 데이터 없는 경우")
    void getTravelPlacesWithoutData(){
        // given
        Pageable pageable = PageUtil.defaultPageable(1);

        when(travelPlaceRepository.findAllByAreaData(any(), anyString(), anyString(), anyString()))
                .thenReturn(PageUtil.createPage(new ArrayList<>(), pageable, 0));

        // when
        Page<PlaceResponse> response = scheduleTravelService.getTravelPlaces(1);

        // then
        assertEquals(response.getTotalElements(), 0);
        assertTrue(response.getContent().isEmpty());
    }


    @Test
    @DisplayName("searchTravelPlaces(): 여행지 검색 성공")
    void searchTravelPlaces(){
        // given
        String keyword = "중구";
        Pageable pageable = PageUtil.defaultPageable(1);

        List<TravelPlace> travelPlaceList = new ArrayList<>(List.of(travelPlace1, travelPlace2));
        Page<TravelPlace> travelPlacePage = PageUtil.createPage(travelPlaceList, pageable, travelPlaceList.size());

        when(travelPlaceRepository.searchTravelPlaces(pageable, keyword)).thenReturn(travelPlacePage);

        // when
        Page<PlaceResponse> response = scheduleTravelService.searchTravelPlaces(1, keyword);


        // then
        List<PlaceResponse> content = response.getContent();
        assertEquals(content.get(0).getPlaceName(), travelPlace1.getPlaceName());
        assertEquals(content.get(0).getAddress(), travelPlace1.getAddress());
        assertNotNull(content.get(0).getThumbnailUrl());
    }

    @Test
    @DisplayName("searchTravelPlaces(): 여행지 검색 시 검색 결과 존재하지 않는 경우")
    void searchTravelPlacesWithoutData(){
        // given
        String keyword = "ㅁㄴㅇㄹ";
        Pageable pageable = PageUtil.defaultPageable(1);
        Page<TravelPlace> travelPlacePage = PageUtil.createPage(new ArrayList<>(), pageable, 0);

        when(travelPlaceRepository.searchTravelPlaces(pageable, keyword)).thenReturn(travelPlacePage);

        // when
        Page<PlaceResponse> response = scheduleTravelService.searchTravelPlaces(1, keyword);


        // then
        assertEquals(response.getTotalElements(), 0);
    }

}
