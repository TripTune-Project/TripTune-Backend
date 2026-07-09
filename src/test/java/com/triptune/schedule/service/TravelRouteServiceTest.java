package com.triptune.schedule.service;

import com.triptune.common.entity.*;
import com.triptune.common.fixture.*;
import com.triptune.global.s3.S3ObjectManager;
import com.triptune.member.entity.Member;
import com.triptune.member.fixture.MemberFixture;
import com.triptune.profile.entity.ProfileImage;
import com.triptune.profile.fixture.ProfileImageFixture;
import com.triptune.schedule.fixture.TravelAttendeeFixture;
import com.triptune.schedule.fixture.TravelRouteFixture;
import com.triptune.schedule.fixture.TravelScheduleFixture;
import com.triptune.schedule.dto.request.RouteCreateRequest;
import com.triptune.schedule.dto.request.RouteRequest;
import com.triptune.schedule.dto.response.RouteResponse;
import com.triptune.schedule.entity.TravelAttendee;
import com.triptune.schedule.entity.TravelRoute;
import com.triptune.schedule.entity.TravelSchedule;
import com.triptune.schedule.enums.AttendeePermission;
import com.triptune.schedule.exception.ForbiddenScheduleException;
import com.triptune.schedule.repository.TravelAttendeeRepository;
import com.triptune.schedule.repository.TravelRouteRepository;
import com.triptune.schedule.repository.TravelScheduleRepository;
import com.triptune.schedule.repository.dto.RouteQueryDto;
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
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
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
        place2Thumb = TravelImageFixture.createTravelImage(place2WithThumb, "test1", true);
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

        schedule = TravelScheduleFixture.createTravelScheduleWithId(1L, "테스트1");

        author = TravelAttendeeFixture.createAuthorTravelAttendee(schedule, member1);
        guest = TravelAttendeeFixture.createGuestTravelAttendee(schedule, member2, AttendeePermission.READ);
    }


    @Test
    @DisplayName("여행 루트 조회")
    void getTravelRoutes(){
        // given
        TravelRoute route1 = TravelRouteFixture.createTravelRoute(schedule, place1WithThumb, 1);
        TravelRoute route2 = TravelRouteFixture.createTravelRoute(schedule, place1WithThumb, 2);
        TravelRoute route3 = TravelRouteFixture.createTravelRoute(schedule, place2WithThumb, 3);

        Pageable pageable = PageUtils.defaultPageable(1);
        List<RouteQueryDto> routes = List.of(
                TravelRouteFixture.createRouteQueryDto(route1, place1Thumb.getS3ObjectKey()),
                TravelRouteFixture.createRouteQueryDto(route2, place1Thumb.getS3ObjectKey()),
                TravelRouteFixture.createRouteQueryDto(route3, place2Thumb.getS3ObjectKey())
        );
        Page<RouteQueryDto> routePage = PageUtils.createPage(routes, pageable, routes.size());

        when(travelRouteRepository.findAllByScheduleId(pageable, schedule.getScheduleId()))
                .thenReturn(routePage);
        when(s3ObjectManager.generateS3ObjectUrl(place1Thumb.getS3ObjectKey())).thenReturn(place1ThumbUrl);
        when(s3ObjectManager.generateS3ObjectUrl(place2Thumb.getS3ObjectKey())).thenReturn(place2ThumbUrl);

        // when
        Page<RouteResponse> response = travelRouteService.getTravelRoutes(schedule.getScheduleId(), 1);

        // then
        List<RouteResponse> content = response.getContent();
        assertThat(response.getTotalElements()).isEqualTo(schedule.getTravelRoutes().size());
        assertThat(content.get(0).getPlaceName()).isEqualTo(route1.getTravelPlace().getPlaceName());
        assertThat(content.get(0).getThumbnailUrl()).isEqualTo(place1ThumbUrl);
        assertThat(content.get(1).getPlaceName()).isEqualTo(route2.getTravelPlace().getPlaceName());
        assertThat(content.get(1).getThumbnailUrl()).isEqualTo(place1ThumbUrl);
        assertThat(content.get(2).getPlaceName()).isEqualTo(route3.getTravelPlace().getPlaceName());
        assertThat(content.get(2).getThumbnailUrl()).isEqualTo(place2ThumbUrl);
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

        when(travelRouteRepository.findAllByScheduleId(pageable, schedule.getScheduleId()))
                .thenReturn(PageUtils.createPage(Collections.emptyList(), pageable, 0));

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
        TravelRouteFixture.createTravelRoute(schedule, place1WithThumb, 1);
        TravelRouteFixture.createTravelRoute(schedule, place2WithThumb, 2);
        TravelRouteFixture.createTravelRoute(schedule, place2WithThumb, 3);

        RouteCreateRequest request = TravelRouteFixture.createRouteCreateRequest(place3WithoutThumb.getPlaceId());

        when(travelScheduleRepository.findById(anyLong())).thenReturn(Optional.of(schedule));
        when(travelAttendeeRepository.findByTravelSchedule_ScheduleIdAndMember_MemberId(anyLong(), anyLong()))
                .thenReturn(Optional.of(author));
        when(travelPlaceRepository.findById(anyLong())).thenReturn(Optional.of(place3WithoutThumb));

        // when
        assertDoesNotThrow(() -> travelRouteService.createLastRoute(1L, 1L, request));

        // then
        List<TravelRoute> routes = schedule.getTravelRoutes();

        assertThat(routes.size()).isEqualTo(4);
        assertThat(routes.get(routes.size() - 1).getTravelPlace().getPlaceName())
                .isEqualTo(place3WithoutThumb.getPlaceName());

    }

    @Test
    @DisplayName("여행 루트 마지막 루트에 여행지 추가 시 저장된 여행 루트가 존재하지 않는 경우")
    void createLastRoute_emptyRoutes(){
        // given
        RouteCreateRequest request = TravelRouteFixture.createRouteCreateRequest(place3WithoutThumb.getPlaceId());

        when(travelScheduleRepository.findById(anyLong())).thenReturn(Optional.of(schedule));
        when(travelAttendeeRepository.findByTravelSchedule_ScheduleIdAndMember_MemberId(anyLong(), anyLong()))
                .thenReturn(Optional.of(author));
        when(travelPlaceRepository.findById(anyLong())).thenReturn(Optional.of(place3WithoutThumb));

        // when
        assertDoesNotThrow(() -> travelRouteService.createLastRoute(1L, 1L, request));

        // then
        assertThat(schedule.getTravelRoutes().size()).isEqualTo(1);
        assertThat(schedule.getTravelRoutes().get(0).getTravelPlace().getPlaceName()).isEqualTo(place3WithoutThumb.getPlaceName());
    }


    @Test
    @DisplayName("여행 루트 마지막 루트에 여행지 추가 시 일정 데이터 없어 예외 발생")
    void createLastRoute_scheduleNotFound(){
        // given
        RouteCreateRequest request = TravelRouteFixture.createRouteCreateRequest(place3WithoutThumb.getPlaceId());

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
        RouteCreateRequest request = TravelRouteFixture.createRouteCreateRequest(place3WithoutThumb.getPlaceId());

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
        RouteCreateRequest request = TravelRouteFixture.createRouteCreateRequest(place3WithoutThumb.getPlaceId());

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
        RouteCreateRequest request = TravelRouteFixture.createRouteCreateRequest(place3WithoutThumb.getPlaceId());

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
        TravelRouteFixture.createTravelRoute(schedule, place1WithThumb, 1);
        TravelRouteFixture.createTravelRoute(schedule, place1WithThumb, 2);
        TravelRouteFixture.createTravelRoute(schedule, place2WithThumb, 3);

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
        TravelSchedule schedule = TravelScheduleFixture.createTravelSchedule("테스트");

        List<RouteRequest> routes = new ArrayList<>(List.of(
                TravelRouteFixture.createRouteRequest(1, place1WithThumb.getPlaceId()),
                TravelRouteFixture.createRouteRequest(2, place2WithThumb.getPlaceId())
        ));

        when(travelPlaceRepository.findById(anyLong()))
                .thenReturn(Optional.ofNullable(place1WithThumb))
                .thenReturn(Optional.empty());

        // when
        DataNotFoundException fail = assertThrows(DataNotFoundException.class,
                () -> travelRouteService.updateTravelRouteInSchedule(schedule, routes));

        // then
        assertThat(fail.getErrorCode()).isEqualTo(ErrorCode.PLACE_NOT_FOUND);
    }

}
