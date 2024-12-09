package com.triptune.domain.schedule.service;

import com.triptune.domain.common.entity.ApiCategory;
import com.triptune.domain.common.entity.City;
import com.triptune.domain.common.entity.Country;
import com.triptune.domain.common.entity.District;
import com.triptune.domain.member.entity.Member;
import com.triptune.domain.member.entity.ProfileImage;
import com.triptune.domain.member.repository.MemberRepository;
import com.triptune.domain.schedule.ScheduleTest;
import com.triptune.domain.schedule.dto.response.RouteResponse;
import com.triptune.domain.schedule.entity.TravelAttendee;
import com.triptune.domain.schedule.entity.TravelRoute;
import com.triptune.domain.schedule.entity.TravelSchedule;
import com.triptune.domain.schedule.enumclass.AttendeePermission;
import com.triptune.domain.schedule.enumclass.AttendeeRole;
import com.triptune.domain.schedule.repository.TravelAttendeeRepository;
import com.triptune.domain.schedule.repository.TravelRouteRepository;
import com.triptune.domain.schedule.repository.TravelScheduleRepository;
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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class RouteServiceTest extends ScheduleTest {

    @InjectMocks
    private RouteService routeService;

    @Mock
    private TravelRouteRepository travelRouteRepository;

    private TravelSchedule schedule1;
    private TravelPlace travelPlace1;

    @BeforeEach
    void setUp(){
        Country country = createCountry();
        City city = createCity(country);
        District district = createDistrict(city, "중구");
        ApiCategory apiCategory = createApiCategory();
        travelPlace1 = createTravelPlace(1L, country, city, district, apiCategory);
        TravelImage travelImage1 = createTravelImage(travelPlace1, "test1", true);
        TravelImage travelImage2 = createTravelImage(travelPlace1, "test2", false);
        List<TravelImage> travelImageList1 = new ArrayList<>(List.of(travelImage1, travelImage2));
        travelPlace1.setTravelImageList(travelImageList1);

        TravelPlace travelPlace2 = createTravelPlace(2L, country, city, district, apiCategory);
        TravelImage travelImage3 = createTravelImage(travelPlace2, "test1", true);
        TravelImage travelImage4 = createTravelImage(travelPlace2, "test2", false);
        List<TravelImage> travelImageList2 = new ArrayList<>(List.of(travelImage3, travelImage4));
        travelPlace2.setTravelImageList(travelImageList2);

        schedule1 = createTravelSchedule(1L, "테스트1");

        TravelRoute route1 = createTravelRoute(schedule1, travelPlace1, 1);
        TravelRoute route2 = createTravelRoute(schedule1, travelPlace1, 2);
        TravelRoute route3 = createTravelRoute(schedule1, travelPlace2, 3);
        schedule1.setTravelRouteList(new ArrayList<>(List.of(route1, route2, route3)));
    }



    @Test
    @DisplayName("여행 루트 조회")
    void getTravelRoutes(){
        // given
        Pageable pageable = PageUtil.defaultPageable(1);

        when(travelRouteRepository.findAllByTravelSchedule_ScheduleId(pageable, schedule1.getScheduleId()))
                .thenReturn(PageUtil.createPage(schedule1.getTravelRouteList(), pageable, schedule1.getTravelRouteList().size()));


        // when
        Page<RouteResponse> response = routeService.getTravelRoutes(schedule1.getScheduleId(), 1);

        // then
        List<RouteResponse> content = response.getContent();
        assertEquals(response.getTotalElements(), schedule1.getTravelRouteList().size());
        assertEquals(content.get(0).getAddress(), travelPlace1.getAddress());
        assertEquals(content.get(0).getRouteOrder(), schedule1.getTravelRouteList().get(0).getRouteOrder());
        assertEquals(content.get(1).getRouteOrder(), schedule1.getTravelRouteList().get(1).getRouteOrder());
        assertEquals(content.get(2).getRouteOrder(), schedule1.getTravelRouteList().get(2).getRouteOrder());
    }

    @Test
    @DisplayName("여행 루트 조회 시 저장된 여행 루트 데이터 없는 경우")
    void getTravelRoutesWithoutData(){
        // given
        Pageable pageable = PageUtil.defaultPageable(1);

        when(travelRouteRepository.findAllByTravelSchedule_ScheduleId(pageable, schedule1.getScheduleId()))
                .thenReturn(PageUtil.createPage(new ArrayList<>(), pageable, 0));


        // when
        Page<RouteResponse> response = routeService.getTravelRoutes(schedule1.getScheduleId(), 1);

        // then
        assertEquals(response.getTotalElements(), 0);
        assertTrue(response.getContent().isEmpty());
    }

}
