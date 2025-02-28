package com.triptune.schedule.service;

import com.triptune.common.entity.ApiCategory;
import com.triptune.common.entity.City;
import com.triptune.common.entity.Country;
import com.triptune.common.entity.District;
import com.triptune.member.entity.Member;
import com.triptune.schedule.ScheduleTest;
import com.triptune.schedule.dto.request.RouteCreateRequest;
import com.triptune.schedule.dto.response.RouteResponse;
import com.triptune.schedule.entity.TravelAttendee;
import com.triptune.schedule.entity.TravelRoute;
import com.triptune.schedule.entity.TravelSchedule;
import com.triptune.schedule.enumclass.AttendeePermission;
import com.triptune.schedule.enumclass.AttendeeRole;
import com.triptune.schedule.exception.ForbiddenScheduleException;
import com.triptune.schedule.repository.TravelAttendeeRepository;
import com.triptune.schedule.repository.TravelRouteRepository;
import com.triptune.schedule.repository.TravelScheduleRepository;
import com.triptune.travel.entity.TravelImage;
import com.triptune.travel.entity.TravelPlace;
import com.triptune.travel.repository.TravelPlaceRepository;
import com.triptune.global.enumclass.ErrorCode;
import com.triptune.global.exception.DataNotFoundException;
import com.triptune.global.util.PageUtils;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class RouteServiceTest extends ScheduleTest {

    @InjectMocks
    private RouteService routeService;

    @Mock
    private TravelRouteRepository travelRouteRepository;

    @Mock
    private TravelScheduleRepository travelScheduleRepository;

    @Mock
    private TravelPlaceRepository travelPlaceRepository;

    @Mock
    private TravelAttendeeRepository travelAttendeeRepository;

    private TravelSchedule schedule1;
    private TravelPlace travelPlace1;
    private TravelPlace travelPlace2;
    private TravelPlace travelPlace3;
    private TravelAttendee attendee1;
    private TravelAttendee attendee2;
    private Member member1;
    private Member member2;

    @BeforeEach
    void setUp(){
        Country country = createCountry();
        City city = createCity(country);
        District district = createDistrict(city, "중구");
        ApiCategory apiCategory = createApiCategory();
        TravelImage travelImage1 = createTravelImage(travelPlace1, "test1", true);
        TravelImage travelImage2 = createTravelImage(travelPlace1, "test2", false);
        travelPlace1 = createTravelPlace(1L, country, city, district, apiCategory, List.of(travelImage1, travelImage2));

        TravelImage travelImage3 = createTravelImage(travelPlace2, "test1", true);
        TravelImage travelImage4 = createTravelImage(travelPlace2, "test2", false);
        travelPlace2 = createTravelPlace(2L, country, city, district, apiCategory, List.of(travelImage3, travelImage4));
        travelPlace3 = createTravelPlace(3L, country, city, district, apiCategory, new ArrayList<>());

        member1 = createMember(1L, "member1");
        member2 = createMember(2L, "member2");

        schedule1 = createTravelSchedule(1L, "테스트1");

        TravelRoute route1 = createTravelRoute(schedule1, travelPlace1, 1);
        TravelRoute route2 = createTravelRoute(schedule1, travelPlace1, 2);
        TravelRoute route3 = createTravelRoute(schedule1, travelPlace2, 3);
        schedule1.setTravelRouteList(new ArrayList<>(List.of(route1, route2, route3)));

        attendee1 = createTravelAttendee(1L, member1, schedule1, AttendeeRole.AUTHOR, AttendeePermission.ALL);
        attendee2 = createTravelAttendee(2L, member2, schedule1, AttendeeRole.GUEST, AttendeePermission.READ);
        schedule1.setTravelAttendeeList(new ArrayList<>(List.of(attendee1, attendee2)));
    }



    @Test
    @DisplayName("여행 루트 조회")
    void getTravelRoutes(){
        // given
        Pageable pageable = PageUtils.defaultPageable(1);

        when(travelRouteRepository.findAllByTravelSchedule_ScheduleId(pageable, schedule1.getScheduleId()))
                .thenReturn(PageUtils.createPage(schedule1.getTravelRouteList(), pageable, schedule1.getTravelRouteList().size()));


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
        Pageable pageable = PageUtils.defaultPageable(1);

        when(travelRouteRepository.findAllByTravelSchedule_ScheduleId(pageable, schedule1.getScheduleId()))
                .thenReturn(PageUtils.createPage(new ArrayList<>(), pageable, 0));


        // when
        Page<RouteResponse> response = routeService.getTravelRoutes(schedule1.getScheduleId(), 1);

        // then
        assertEquals(response.getTotalElements(), 0);
        assertTrue(response.getContent().isEmpty());
    }

    @Test
    @DisplayName("여행 루트 마지막 루트에 여행지 추가")
    void createLastRoute(){
        // given
        RouteCreateRequest request = createRouteCreateRequest(travelPlace3.getPlaceId());

        when(travelScheduleRepository.findByScheduleId(anyLong())).thenReturn(Optional.of(schedule1));
        when(travelAttendeeRepository.findByTravelSchedule_ScheduleIdAndMember_UserId(anyLong(), anyString())).thenReturn(Optional.of(attendee1));
        when(travelPlaceRepository.findByPlaceId(anyLong())).thenReturn(Optional.of(travelPlace3));

        // when
        assertDoesNotThrow(() -> routeService.createLastRoute(schedule1.getScheduleId(), member1.getUserId(), request));
    }

    @Test
    @DisplayName("여행 루트 마지막 루트에 여행지 추가 시 저장된 여행 루트가 존재하지 않는 경우")
    void createLastRoute_emptyRouteList(){
        // given
        schedule1.setTravelRouteList(new ArrayList<>());
        RouteCreateRequest request = createRouteCreateRequest(travelPlace3.getPlaceId());

        when(travelScheduleRepository.findByScheduleId(anyLong())).thenReturn(Optional.of(schedule1));
        when(travelAttendeeRepository.findByTravelSchedule_ScheduleIdAndMember_UserId(anyLong(), anyString())).thenReturn(Optional.of(attendee1));
        when(travelPlaceRepository.findByPlaceId(anyLong())).thenReturn(Optional.of(travelPlace3));

        // when
        assertDoesNotThrow(() -> routeService.createLastRoute(schedule1.getScheduleId(), member1.getUserId(), request));
    }


    @Test
    @DisplayName("여행 루트 마지막 루트에 여행지 추가 시 일정 데이터 없어 예외 발생")
    void createLastRoute_scheduleDataNotFoundException(){
        // given
        RouteCreateRequest request = createRouteCreateRequest(travelPlace3.getPlaceId());

        when(travelScheduleRepository.findByScheduleId(anyLong())).thenReturn(Optional.empty());

        // when
        DataNotFoundException fail = assertThrows(DataNotFoundException.class, () -> routeService.createLastRoute(0L, member1.getUserId(), request));

        // then
        assertThat(fail.getHttpStatus()).isEqualTo(ErrorCode.SCHEDULE_NOT_FOUND.getStatus());
        assertThat(fail.getMessage()).isEqualTo(ErrorCode.SCHEDULE_NOT_FOUND.getMessage());

    }

    @Test
    @DisplayName("여행 루트 마지막 루트에 여행지 추가 시 참석자 정보가 없어 예외 발생")
    void createLastRoute_attendeeDataNotFoundException(){
        // given
        RouteCreateRequest request = createRouteCreateRequest(travelPlace3.getPlaceId());

        when(travelScheduleRepository.findByScheduleId(anyLong())).thenReturn(Optional.of(schedule1));
        when(travelAttendeeRepository.findByTravelSchedule_ScheduleIdAndMember_UserId(anyLong(), anyString())).thenReturn(Optional.empty());


        // when
        DataNotFoundException fail = assertThrows(DataNotFoundException.class, () -> routeService.createLastRoute(schedule1.getScheduleId(), "notMember", request));

        // then
        assertThat(fail.getHttpStatus()).isEqualTo(ErrorCode.ATTENDEE_NOT_FOUND.getStatus());
        assertThat(fail.getMessage()).isEqualTo(ErrorCode.ATTENDEE_NOT_FOUND.getMessage());

    }

    @Test
    @DisplayName("여행 루트 마지막 루트에 여행지 추가 시 편집 권한이 없어 예외 발생")
    void createLastRoute_forbiddenScheduleException(){
        // given
        RouteCreateRequest request = createRouteCreateRequest(travelPlace3.getPlaceId());

        when(travelScheduleRepository.findByScheduleId(anyLong())).thenReturn(Optional.of(schedule1));
        when(travelAttendeeRepository.findByTravelSchedule_ScheduleIdAndMember_UserId(anyLong(), anyString())).thenReturn(Optional.of(attendee2));

        // when
        ForbiddenScheduleException fail = assertThrows(ForbiddenScheduleException.class, () -> routeService.createLastRoute(schedule1.getScheduleId(), member2.getUserId(), request));

        // then
        assertThat(fail.getHttpStatus()).isEqualTo(ErrorCode.FORBIDDEN_EDIT_SCHEDULE.getStatus());
        assertThat(fail.getMessage()).isEqualTo(ErrorCode.FORBIDDEN_EDIT_SCHEDULE.getMessage());

    }


    @Test
    @DisplayName("여행 루트 마지막 루트에 여행지 추가 시 여행지 데이터 없어 예외 발생")
    void createLastRoute_placeDataNotFoundException(){
        // given
        RouteCreateRequest request = createRouteCreateRequest(travelPlace3.getPlaceId());

        when(travelScheduleRepository.findByScheduleId(anyLong())).thenReturn(Optional.of(schedule1));
        when(travelAttendeeRepository.findByTravelSchedule_ScheduleIdAndMember_UserId(anyLong(), anyString())).thenReturn(Optional.of(attendee1));
        when(travelPlaceRepository.findByPlaceId(anyLong())).thenReturn(Optional.empty());

        // when
        DataNotFoundException fail = assertThrows(DataNotFoundException.class, () -> routeService.createLastRoute(schedule1.getScheduleId(), member1.getUserId(), request));

        // then
        assertThat(fail.getHttpStatus()).isEqualTo(ErrorCode.PLACE_NOT_FOUND.getStatus());
        assertThat(fail.getMessage()).isEqualTo(ErrorCode.PLACE_NOT_FOUND.getMessage());

    }

}
