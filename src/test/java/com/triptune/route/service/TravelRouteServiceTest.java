package com.triptune.route.service;

import com.triptune.common.entity.*;
import com.triptune.common.fixture.*;
import com.triptune.global.response.page.RoutePageResponse;
import com.triptune.global.s3.S3ObjectManager;
import com.triptune.member.entity.Member;
import com.triptune.member.fixture.MemberFixture;
import com.triptune.profile.entity.ProfileImage;
import com.triptune.profile.fixture.ProfileImageFixture;
import com.triptune.attendee.fixture.TravelAttendeeFixture;
import com.triptune.route.dto.response.RouteCreateResponse;
import com.triptune.route.fixture.TravelRouteFixture;
import com.triptune.schedule.fixture.TravelScheduleFixture;
import com.triptune.route.dto.request.RouteCreateRequest;
import com.triptune.route.dto.request.RouteRequest;
import com.triptune.route.dto.response.RouteResponse;
import com.triptune.attendee.entity.TravelAttendee;
import com.triptune.route.entity.TravelRoute;
import com.triptune.schedule.entity.TravelSchedule;
import com.triptune.attendee.enums.AttendeePermission;
import com.triptune.schedule.exception.ForbiddenScheduleException;
import com.triptune.attendee.repository.TravelAttendeeRepository;
import com.triptune.route.repository.TravelRouteRepository;
import com.triptune.schedule.repository.TravelScheduleRepository;
import com.triptune.route.repository.dto.RouteQueryDto;
import com.triptune.travel.entity.TravelImage;
import com.triptune.travel.entity.TravelPlace;
import com.triptune.travel.enums.ThemeType;
import com.triptune.travel.fixture.TravelImageFixture;
import com.triptune.travel.fixture.TravelPlaceFixture;
import com.triptune.travel.repository.TravelPlaceRepository;
import com.triptune.global.message.ErrorCode;
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

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TravelRouteServiceTest {
    @InjectMocks private TravelRouteService travelRouteService;
    @Mock private TravelRouteRepository travelRouteRepository;
    @Mock private TravelScheduleRepository travelScheduleRepository;
    @Mock private TravelPlaceRepository travelPlaceRepository;
    @Mock private TravelAttendeeRepository travelAttendeeRepository;
    @Mock private S3ObjectManager s3ObjectManager;

    private TravelSchedule schedule;

    private TravelPlace place1WithThumb;
    private TravelPlace place2WithThumb;
    private TravelPlace place3WithoutThumb;

    private TravelImage place1Thumb;
    private TravelImage place2Thumb;

    private String place1ThumbUrl;
    private String place2ThumbUrl;


    private TravelAttendee author;
    private TravelAttendee guest;

    private Member member1;
    private Member member2;

    @BeforeEach
    void setUp(){
        Country country = CountryFixture.createCountry();
        City city = CityFixture.createSeoul(country);
        District district = DistrictFixture.createDistrict(city, "중구");
        ApiContentType apiContentType = ApiContentTypeFixture.createApiContentType(ThemeType.ATTRACTIONS);

        place1WithThumb = TravelPlaceFixture.createTravelPlaceWithId(
                1L,
                country,
                city,
                district,
                apiContentType,
                "여행지1"
        );
        place1Thumb = TravelImageFixture.createTravelImage(place1WithThumb, "test1", true);
        TravelImageFixture.createTravelImage(place1WithThumb, "test2", false);
        place1ThumbUrl = S3Fixture.createS3ObjectUrl(place1Thumb.getS3ObjectKey());

        place2WithThumb = TravelPlaceFixture.createTravelPlaceWithId(
                2L,
                country,
                city,
                district,
                apiContentType,
                "여행지2"
        );
        place2Thumb = TravelImageFixture.createTravelImage(place2WithThumb, "test2", true);
        TravelImageFixture.createTravelImage(place2WithThumb, "test2", false);
        place2ThumbUrl = S3Fixture.createS3ObjectUrl(place1Thumb.getS3ObjectKey());

        place3WithoutThumb = TravelPlaceFixture.createTravelPlaceWithId(
                3L,
                country,
                city,
                district,
                apiContentType,
                "여행지3"
        );

        ProfileImage profileImage1 = ProfileImageFixture.createProfileImage("member1Image");
        member1 = MemberFixture.createNativeTypeMember("member1@email.com", profileImage1);

        ProfileImage profileImage2 = ProfileImageFixture.createProfileImage("member2Image");
        member2 = MemberFixture.createNativeTypeMember("member2@email.com", profileImage2);

        schedule = TravelScheduleFixture.createScheduleWithIdAndVersion(
                1L,
                "테스트1",
                1,
                2
        );

        author = TravelAttendeeFixture.createAuthorAttendee(schedule, member1);
        guest = TravelAttendeeFixture.createGuestAttendee(schedule, member2, AttendeePermission.READ);
    }


    @Test
    @DisplayName("여행 루트 조회")
    void getTravelRoutes(){
        // given
        TravelRoute route1 = TravelRouteFixture.createRoute(schedule, place1WithThumb, 1);
        TravelRoute route2 = TravelRouteFixture.createRoute(schedule, place1WithThumb, 2);
        TravelRoute route3 = TravelRouteFixture.createRoute(schedule, place2WithThumb, 3);

        Pageable pageable = PageUtils.defaultPageable(1);
        List<RouteQueryDto> routes = List.of(
                TravelRouteFixture.createRouteQueryDto(route1, place1Thumb.getS3ObjectKey()),
                TravelRouteFixture.createRouteQueryDto(route2, place1Thumb.getS3ObjectKey()),
                TravelRouteFixture.createRouteQueryDto(route3, place2Thumb.getS3ObjectKey())
        );

        Page<RouteQueryDto> pageResponse = PageUtils.createPage(routes, pageable, routes.size());

        when(travelScheduleRepository.findRouteVersionById(eq(schedule.getScheduleId())))
                .thenReturn(Optional.of(schedule.getRouteVersion()));
        when(travelRouteRepository.findAllByScheduleId(eq(pageable), eq(schedule.getScheduleId())))
                .thenReturn(pageResponse);
        when(s3ObjectManager.generateS3ObjectUrl(eq(place1Thumb.getS3ObjectKey())))
                .thenReturn(place1ThumbUrl);
        when(s3ObjectManager.generateS3ObjectUrl(eq(place2Thumb.getS3ObjectKey())))
                .thenReturn(place2ThumbUrl);

        // when
        RoutePageResponse<RouteResponse> response = travelRouteService.getTravelRoutes(schedule.getScheduleId(), 1);

        // then
        assertThat(response.getTotalElements()).isEqualTo(schedule.getTravelRoutes().size());
        assertThat(response.getRouteVersion()).isEqualTo(schedule.getRouteVersion());
        assertThat(response.getContent())
                .extracting(
                        RouteResponse::getPlaceName,
                        RouteResponse::getThumbnailUrl,
                        RouteResponse::getRouteOrder
                )
                .containsExactly(
                        tuple(route1.getTravelPlace().getPlaceName(), place1ThumbUrl, route1.getRouteOrder()),
                        tuple(route2.getTravelPlace().getPlaceName(), place1ThumbUrl, route2.getRouteOrder()),
                        tuple(route3.getTravelPlace().getPlaceName(), place2ThumbUrl, route3.getRouteOrder())
                );
    }


    @Test
    @DisplayName("여행 루트 조회 시 여행지 데이터 없는 경우")
    void getTravelRoutes_scheduleNotFound(){
        // given
        when(travelScheduleRepository.findRouteVersionById(eq(schedule.getScheduleId())))
                .thenReturn(Optional.empty());

        // when
        DataNotFoundException fail = assertThrows(DataNotFoundException.class, () -> travelRouteService.getTravelRoutes(schedule.getScheduleId(), 1));

        // then
        assertThat(fail.getErrorCode()).isEqualTo(ErrorCode.SCHEDULE_NOT_FOUND);
    }

    @Test
    @DisplayName("여행 루트 조회 시 저장된 여행 루트 데이터 없는 경우")
    void getTravelRoutes_emptyResult(){
        // given
        Pageable pageable = PageUtils.defaultPageable(1);

        when(travelScheduleRepository.findRouteVersionById(eq(schedule.getScheduleId())))
                .thenReturn(Optional.of(schedule.getRouteVersion()));
        when(travelRouteRepository.findAllByScheduleId(eq(pageable), eq(schedule.getScheduleId())))
                .thenReturn(PageUtils.createPage(Collections.emptyList(), pageable, 0));

        // when
        RoutePageResponse<RouteResponse> response = travelRouteService.getTravelRoutes(schedule.getScheduleId(), 1);

        // then
        assertThat(response.getTotalElements()).isEqualTo(0);
        assertThat(response.getContent()).isEmpty();
    }

    @Test
    @DisplayName("여행 루트 마지막 루트에 여행지 추가")
    void createLastTravelRoute(){
        // given
        TravelRouteFixture.createRoute(schedule, place1WithThumb, 1);
        TravelRouteFixture.createRoute(schedule, place2WithThumb, 2);
        TravelRouteFixture.createRoute(schedule, place2WithThumb, 3);

        RouteCreateRequest request = TravelRouteFixture.createRouteCreateRequest(
                place3WithoutThumb.getPlaceId(),
                schedule.getRouteVersion()
        );

        when(travelScheduleRepository.findById(anyLong()))
                .thenReturn(Optional.of(schedule));
        when(travelAttendeeRepository.findByTravelSchedule_ScheduleIdAndMember_MemberId(anyLong(), anyLong()))
                .thenReturn(Optional.of(author));
        when(travelPlaceRepository.findById(anyLong()))
                .thenReturn(Optional.of(place3WithoutThumb));

        // when
        RouteCreateResponse response = travelRouteService.createLastTravelRoute(1L, 1L, request);

        // then
        List<TravelRoute> routes = schedule.getTravelRoutes();

        assertThat(routes.size()).isEqualTo(4);
        assertThat(routes.get(routes.size() - 1).getTravelPlace().getPlaceName()).isEqualTo(place3WithoutThumb.getPlaceName());
        assertThat(response.getRouteVersion()).isEqualTo(3);
    }

    @Test
    @DisplayName("여행 루트 마지막 루트에 여행지 추가 시 처음 저장인 경우")
    void createLastTravelRoute_first(){
        // given
        RouteCreateRequest request = TravelRouteFixture.createRouteCreateRequest(
                place3WithoutThumb.getPlaceId(),
                schedule.getRouteVersion()
        );

        when(travelScheduleRepository.findById(anyLong()))
                .thenReturn(Optional.of(schedule));
        when(travelAttendeeRepository.findByTravelSchedule_ScheduleIdAndMember_MemberId(anyLong(), anyLong()))
                .thenReturn(Optional.of(author));
        when(travelPlaceRepository.findById(anyLong()))
                .thenReturn(Optional.of(place3WithoutThumb));

        // when
        RouteCreateResponse response = travelRouteService.createLastTravelRoute(1L, 1L, request);

        // then
        assertThat(schedule.getTravelRoutes().size()).isEqualTo(1);
        assertThat(schedule.getTravelRoutes().get(0).getTravelPlace().getPlaceName()).isEqualTo(place3WithoutThumb.getPlaceName());
        assertThat(response.getRouteVersion()).isEqualTo(3);
    }


    @Test
    @DisplayName("여행 루트 마지막 루트에 여행지 추가 시 일정 데이터 없어 예외 발생")
    void createLastTravelRoute_scheduleNotFound(){
        // given
        RouteCreateRequest request = TravelRouteFixture.createRouteCreateRequest(
                place3WithoutThumb.getPlaceId(),
                schedule.getRouteVersion()
        );

        when(travelScheduleRepository.findById(anyLong()))
                .thenReturn(Optional.empty());

        // when
        DataNotFoundException fail = assertThrows(DataNotFoundException.class,
                () -> travelRouteService.createLastTravelRoute(0L, member1.getMemberId(), request));

        // then
        assertThat(fail.getErrorCode()).isEqualTo(ErrorCode.SCHEDULE_NOT_FOUND);

    }

    @Test
    @DisplayName("여행 루트 마지막 루트에 여행지 추가 시 참석자 정보가 없어 예외 발생")
    void createLastTravelRoute_attendeeNotFound(){
        // given
        RouteCreateRequest request = TravelRouteFixture.createRouteCreateRequest(
                place3WithoutThumb.getPlaceId(),
                schedule.getRouteVersion()
        );

        when(travelScheduleRepository.findById(anyLong()))
                .thenReturn(Optional.of(schedule));
        when(travelAttendeeRepository.findByTravelSchedule_ScheduleIdAndMember_MemberId(anyLong(), anyLong()))
                .thenReturn(Optional.empty());


        // when
        DataNotFoundException fail = assertThrows(DataNotFoundException.class,
                () -> travelRouteService.createLastTravelRoute(schedule.getScheduleId(), 0L, request));

        // then
        assertThat(fail.getErrorCode()).isEqualTo(ErrorCode.ATTENDEE_NOT_FOUND);

    }

    @Test
    @DisplayName("여행 루트 마지막 루트에 여행지 추가 시 편집 권한이 없어 예외 발생")
    void createLastTravelRoute_forbiddenEdit(){
        // given
        RouteCreateRequest request = TravelRouteFixture.createRouteCreateRequest(
                place3WithoutThumb.getPlaceId(),
                schedule.getRouteVersion()
        );

        when(travelScheduleRepository.findById(anyLong()))
                .thenReturn(Optional.of(schedule));
        when(travelAttendeeRepository.findByTravelSchedule_ScheduleIdAndMember_MemberId(anyLong(), anyLong()))
                .thenReturn(Optional.of(guest));

        // when
        ForbiddenScheduleException fail = assertThrows(ForbiddenScheduleException.class,
                () -> travelRouteService.createLastTravelRoute(1L, 2L, request));

        // then
        assertThat(fail.getErrorCode()).isEqualTo(ErrorCode.FORBIDDEN_EDIT_SCHEDULE);

    }


    @Test
    @DisplayName("여행 루트 마지막 루트에 여행지 추가 시 여행지 데이터 없어 예외 발생")
    void createLastTravelRoute_placeNotFound(){
        // given
        RouteCreateRequest request = TravelRouteFixture.createRouteCreateRequest(
                place3WithoutThumb.getPlaceId(),
                schedule.getRouteVersion()
        );

        when(travelScheduleRepository.findById(anyLong()))
                .thenReturn(Optional.of(schedule));
        when(travelAttendeeRepository.findByTravelSchedule_ScheduleIdAndMember_MemberId(anyLong(), anyLong()))
                .thenReturn(Optional.of(author));
        when(travelPlaceRepository.findById(anyLong()))
                .thenReturn(Optional.empty());

        // when
        DataNotFoundException fail = assertThrows(DataNotFoundException.class,
                () -> travelRouteService.createLastTravelRoute(1L, 1L, request));

        // then
        assertThat(fail.getErrorCode()).isEqualTo(ErrorCode.PLACE_NOT_FOUND);

    }


    @Test
    @DisplayName("여행 루트 수정 시 기존에 저장된 여행 루트가 존재하는 경우")
    void updateTravelRoute_existedTravelRoute(){
        TravelRouteFixture.createRoute(schedule, place1WithThumb, 1);
        TravelRouteFixture.createRoute(schedule, place1WithThumb, 2);
        TravelRouteFixture.createRoute(schedule, place2WithThumb, 3);

        RouteRequest routeRequest1 = TravelRouteFixture.createRouteRequest(1, place1WithThumb.getPlaceId());
        RouteRequest routeRequest2 = TravelRouteFixture.createRouteRequest(2, place2WithThumb.getPlaceId());

        List<RouteRequest> routeRequests = new ArrayList<>(List.of(
                routeRequest1,
                routeRequest2
        ));

        when(travelPlaceRepository.findById(anyLong()))
                .thenReturn(Optional.of(place1WithThumb))
                .thenReturn(Optional.of(place2WithThumb));

        // when
        assertDoesNotThrow(() -> travelRouteService.updateRouteInSchedule(schedule, routeRequests));

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
        TravelSchedule schedule = TravelScheduleFixture.createSchedule("테스트");

        List<RouteRequest> routes = new ArrayList<>(List.of(
                TravelRouteFixture.createRouteRequest(1, place1WithThumb.getPlaceId()),
                TravelRouteFixture.createRouteRequest(2, place2WithThumb.getPlaceId())
        ));

        when(travelPlaceRepository.findById(anyLong()))
                .thenReturn(Optional.ofNullable(place1WithThumb))
                .thenReturn(Optional.empty());

        // when
        DataNotFoundException fail = assertThrows(DataNotFoundException.class,
                () -> travelRouteService.updateRouteInSchedule(schedule, routes));

        // then
        assertThat(fail.getErrorCode()).isEqualTo(ErrorCode.PLACE_NOT_FOUND);
    }

}
