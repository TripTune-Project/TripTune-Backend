package com.triptune.schedule.service;

import com.triptune.common.entity.*;
import com.triptune.member.entity.Member;
import com.triptune.profile.entity.ProfileImage;
import com.triptune.schedule.ScheduleTest;
import com.triptune.schedule.dto.request.RouteCreateRequest;
import com.triptune.schedule.dto.request.RouteRequest;
import com.triptune.schedule.dto.request.ScheduleUpdateRequest;
import com.triptune.schedule.dto.response.RouteResponse;
import com.triptune.schedule.entity.TravelAttendee;
import com.triptune.schedule.entity.TravelRoute;
import com.triptune.schedule.entity.TravelSchedule;
import com.triptune.schedule.enums.AttendeePermission;
import com.triptune.schedule.exception.ForbiddenScheduleException;
import com.triptune.schedule.repository.TravelAttendeeRepository;
import com.triptune.schedule.repository.TravelRouteRepository;
import com.triptune.schedule.repository.TravelScheduleRepository;
import com.triptune.travel.entity.TravelImage;
import com.triptune.travel.entity.TravelPlace;
import com.triptune.travel.enums.ThemeType;
import com.triptune.travel.repository.TravelPlaceRepository;
import com.triptune.global.response.enums.ErrorCode;
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

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TravelRouteServiceTest extends ScheduleTest {

    @InjectMocks private TravelRouteService travelRouteService;
    @Mock private TravelRouteRepository travelRouteRepository;
    @Mock private TravelScheduleRepository travelScheduleRepository;
    @Mock private TravelPlaceRepository travelPlaceRepository;
    @Mock private TravelAttendeeRepository travelAttendeeRepository;

    private TravelSchedule schedule;
    private TravelPlace place1;
    private TravelPlace place2;
    private TravelPlace place3;
    private TravelAttendee author;
    private TravelAttendee guest;
    private Member member1;
    private Member member2;

    @BeforeEach
    void setUp(){
        Country country = createCountry();
        City city = createCity(country, "서울");
        District district = createDistrict(city, "중구");
        ApiCategory apiCategory = createApiCategory();
        ApiContentType apiContentType = createApiContentType(ThemeType.ATTRACTIONS);

        place1 = createTravelPlaceWithId(
                1L,
                country,
                city,
                district,
                apiCategory,
                apiContentType,
                "여행지1"
        );
        createTravelImage(place1, "test1", true);
        createTravelImage(place1, "test2", false);

        place2 = createTravelPlaceWithId(
                2L,
                country,
                city,
                district,
                apiCategory,
                apiContentType,
                "여행지2"
        );
        createTravelImage(place2, "test1", true);
        createTravelImage(place2, "test2", false);

        place3 = createTravelPlaceWithId(
                3L,
                country,
                city,
                district,
                apiCategory,
                apiContentType,
                "여행지3"
        );

        ProfileImage profileImage1 = createProfileImage("member1Image");
        member1 = createNativeTypeMember("member1@email.com", profileImage1);

        ProfileImage profileImage2 = createProfileImage("member2Image");
        member2 = createNativeTypeMember("member2@email.com", profileImage2);

        schedule = createTravelScheduleWithId(1L, "테스트1");

        author = createAuthorTravelAttendee(schedule, member1);
        guest = createGuestTravelAttendee(schedule, member2, AttendeePermission.READ);
    }



    @Test
    @DisplayName("여행 루트 조회")
    void getTravelRoutes(){
        // given
        TravelRoute route1 = createTravelRoute(schedule, place1, 1);
        TravelRoute route2 = createTravelRoute(schedule, place1, 2);
        TravelRoute route3 = createTravelRoute(schedule, place2, 3);

        Pageable pageable = PageUtils.defaultPageable(1);

        when(travelRouteRepository.findAllByTravelSchedule_ScheduleId(pageable, schedule.getScheduleId()))
                .thenReturn(PageUtils.createPage(schedule.getTravelRoutes(), pageable, schedule.getTravelRoutes().size()));

        // when
        Page<RouteResponse> response = travelRouteService.getTravelRoutes(schedule.getScheduleId(), 1);

        // then
        List<RouteResponse> content = response.getContent();
        assertThat(response.getTotalElements()).isEqualTo(schedule.getTravelRoutes().size());
        assertThat(content.get(0).getAddress()).isEqualTo(place1.getAddress());
        assertThat(content)
                .extracting(RouteResponse::getRouteOrder)
                .containsExactly(
                        route1.getRouteOrder(),
                        route2.getRouteOrder(),
                        route3.getRouteOrder()
                );


    }

    @Test
    @DisplayName("여행 루트 조회 시 저장된 여행 루트 데이터 없는 경우")
    void getTravelRoutesWithoutData(){
        // given
        Pageable pageable = PageUtils.defaultPageable(1);

        when(travelRouteRepository.findAllByTravelSchedule_ScheduleId(pageable, schedule.getScheduleId()))
                .thenReturn(PageUtils.createPage(new ArrayList<>(), pageable, 0));


        // when
        Page<RouteResponse> response = travelRouteService.getTravelRoutes(schedule.getScheduleId(), 1);

        // then
        assertThat(response.getTotalElements()).isEqualTo(0);
        assertThat(response.getContent()).isEmpty();
    }

    @Test
    @DisplayName("여행 루트 마지막 루트에 여행지 추가")
    void createLastRoute(){
        // given
        createTravelRoute(schedule, place1, 1);
        createTravelRoute(schedule, place2, 2);
        createTravelRoute(schedule, place2, 3);

        RouteCreateRequest request = createRouteCreateRequest(place3.getPlaceId());

        when(travelScheduleRepository.findById(anyLong())).thenReturn(Optional.of(schedule));
        when(travelAttendeeRepository.findByTravelSchedule_ScheduleIdAndMember_MemberId(anyLong(), anyLong()))
                .thenReturn(Optional.of(author));
        when(travelPlaceRepository.findById(anyLong())).thenReturn(Optional.of(place3));

        // when
        assertDoesNotThrow(() -> travelRouteService.createLastRoute(1L, 1L, request));

        // then
        List<TravelRoute> routes = schedule.getTravelRoutes();

        assertThat(routes.size()).isEqualTo(4);
        assertThat(routes.get(routes.size() - 1).getTravelPlace().getPlaceName())
                .isEqualTo(place3.getPlaceName());

    }

    @Test
    @DisplayName("여행 루트 마지막 루트에 여행지 추가 시 저장된 여행 루트가 존재하지 않는 경우")
    void createLastRoute_emptyRoutes(){
        // given
        RouteCreateRequest request = createRouteCreateRequest(place3.getPlaceId());

        when(travelScheduleRepository.findById(anyLong())).thenReturn(Optional.of(schedule));
        when(travelAttendeeRepository.findByTravelSchedule_ScheduleIdAndMember_MemberId(anyLong(), anyLong()))
                .thenReturn(Optional.of(author));
        when(travelPlaceRepository.findById(anyLong())).thenReturn(Optional.of(place3));

        // when
        assertDoesNotThrow(() -> travelRouteService.createLastRoute(1L, 1L, request));

        // then
        assertThat(schedule.getTravelRoutes().size()).isEqualTo(1);
        assertThat(schedule.getTravelRoutes().get(0).getTravelPlace().getPlaceName()).isEqualTo(place3.getPlaceName());
    }


    @Test
    @DisplayName("여행 루트 마지막 루트에 여행지 추가 시 일정 데이터 없어 예외 발생")
    void createLastRoute_scheduleNotFound(){
        // given
        RouteCreateRequest request = createRouteCreateRequest(place3.getPlaceId());

        when(travelScheduleRepository.findById(anyLong())).thenReturn(Optional.empty());

        // when
        DataNotFoundException fail = assertThrows(DataNotFoundException.class,
                () -> travelRouteService.createLastRoute(0L, member1.getMemberId(), request));

        // then
        assertThat(fail.getErrorCode()).isEqualTo(ErrorCode.SCHEDULE_NOT_FOUND);

    }

    @Test
    @DisplayName("여행 루트 마지막 루트에 여행지 추가 시 참석자 정보가 없어 예외 발생")
    void createLastRoute_attendeeNotFound(){
        // given
        RouteCreateRequest request = createRouteCreateRequest(place3.getPlaceId());

        when(travelScheduleRepository.findById(anyLong())).thenReturn(Optional.of(schedule));
        when(travelAttendeeRepository.findByTravelSchedule_ScheduleIdAndMember_MemberId(anyLong(), anyLong()))
                .thenReturn(Optional.empty());


        // when
        DataNotFoundException fail = assertThrows(DataNotFoundException.class,
                () -> travelRouteService.createLastRoute(schedule.getScheduleId(), 0L, request));

        // then
        assertThat(fail.getErrorCode()).isEqualTo(ErrorCode.ATTENDEE_NOT_FOUND);

    }

    @Test
    @DisplayName("여행 루트 마지막 루트에 여행지 추가 시 편집 권한이 없어 예외 발생")
    void createLastRoute_forbiddenEdit(){
        // given
        RouteCreateRequest request = createRouteCreateRequest(place3.getPlaceId());

        when(travelScheduleRepository.findById(anyLong())).thenReturn(Optional.of(schedule));
        when(travelAttendeeRepository.findByTravelSchedule_ScheduleIdAndMember_MemberId(anyLong(), anyLong()))
                .thenReturn(Optional.of(guest));

        // when
        ForbiddenScheduleException fail = assertThrows(ForbiddenScheduleException.class,
                () -> travelRouteService.createLastRoute(1L, 2L, request));

        // then
        assertThat(fail.getErrorCode()).isEqualTo(ErrorCode.FORBIDDEN_EDIT_SCHEDULE);

    }


    @Test
    @DisplayName("여행 루트 마지막 루트에 여행지 추가 시 여행지 데이터 없어 예외 발생")
    void createLastRoute_placeNotFound(){
        // given
        RouteCreateRequest request = createRouteCreateRequest(place3.getPlaceId());

        when(travelScheduleRepository.findById(anyLong())).thenReturn(Optional.of(schedule));
        when(travelAttendeeRepository.findByTravelSchedule_ScheduleIdAndMember_MemberId(anyLong(), anyLong()))
                .thenReturn(Optional.of(author));
        when(travelPlaceRepository.findById(anyLong())).thenReturn(Optional.empty());

        // when
        DataNotFoundException fail = assertThrows(DataNotFoundException.class,
                () -> travelRouteService.createLastRoute(1L, 1L, request));

        // then
        assertThat(fail.getErrorCode()).isEqualTo(ErrorCode.PLACE_NOT_FOUND);

    }


    @Test
    @DisplayName("여행 루트 수정 시 기존에 저장된 여행 루트가 존재하는 경우")
    void updateTravelRoute_existedTravelRoute(){
        createTravelRoute(schedule, place1, 1);
        createTravelRoute(schedule, place1, 2);
        createTravelRoute(schedule, place2, 3);

        RouteRequest routeRequest1 = createRouteRequest(1, place1.getPlaceId());
        RouteRequest routeRequest2 = createRouteRequest(2, place2.getPlaceId());

        List<RouteRequest> routeRequests = new ArrayList<>(List.of(
                routeRequest1,
                routeRequest2
        ));

        when(travelPlaceRepository.findById(anyLong()))
                .thenReturn(Optional.of(place1))
                .thenReturn(Optional.of(place2));

        // when
        assertDoesNotThrow(
                () -> travelRouteService.updateTravelRouteInSchedule(schedule, routeRequests));

        // then
        assertThat(schedule.getTravelRoutes()).hasSize(2);
        assertThat(schedule.getTravelRoutes())
                .extracting(TravelRoute::getRouteOrder)
                .containsExactly(1, 2);
    }


    @Test
    @DisplayName("여행 루트에 저장된 여행지 데이터가 없어 예외 발생")
    void updateSchedule_placeNotFoundInTravelRoute(){
        // given
        TravelSchedule schedule = createTravelSchedule("테스트");

        List<RouteRequest> routes = new ArrayList<>(List.of(
                createRouteRequest(1, place1.getPlaceId()),
                createRouteRequest(2, place2.getPlaceId())
        ));

        when(travelPlaceRepository.findById(anyLong()))
                .thenReturn(Optional.ofNullable(place1))
                .thenReturn(Optional.empty());

        // when
        DataNotFoundException fail = assertThrows(DataNotFoundException.class,
                () -> travelRouteService.updateTravelRouteInSchedule(schedule, routes));

        // then
        assertThat(fail.getErrorCode()).isEqualTo(ErrorCode.PLACE_NOT_FOUND);
    }

}
