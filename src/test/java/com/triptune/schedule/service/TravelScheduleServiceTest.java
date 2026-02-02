package com.triptune.schedule.service;

import com.triptune.common.entity.*;
import com.triptune.common.fixture.*;
import com.triptune.member.entity.Member;
import com.triptune.member.fixture.MemberFixture;
import com.triptune.profile.entity.ProfileImage;
import com.triptune.member.repository.MemberRepository;
import com.triptune.profile.fixture.ProfileImageFixture;
import com.triptune.schedule.fixture.ChatMessageFixture;
import com.triptune.schedule.fixture.TravelAttendeeFixture;
import com.triptune.schedule.fixture.TravelRouteFixture;
import com.triptune.schedule.fixture.TravelScheduleFixture;
import com.triptune.schedule.service.dto.AuthorDTO;
import com.triptune.schedule.dto.request.ScheduleCreateRequest;
import com.triptune.schedule.dto.request.RouteRequest;
import com.triptune.schedule.dto.request.ScheduleUpdateRequest;
import com.triptune.schedule.dto.response.OverviewScheduleResponse;
import com.triptune.schedule.dto.response.ScheduleCreateResponse;
import com.triptune.schedule.dto.response.ScheduleDetailResponse;
import com.triptune.schedule.dto.response.ScheduleInfoResponse;
import com.triptune.schedule.entity.ChatMessage;
import com.triptune.schedule.entity.TravelAttendee;
import com.triptune.schedule.entity.TravelSchedule;
import com.triptune.schedule.enums.AttendeePermission;
import com.triptune.schedule.enums.AttendeeRole;
import com.triptune.schedule.exception.ForbiddenScheduleException;
import com.triptune.schedule.repository.ChatMessageRepository;
import com.triptune.schedule.repository.TravelAttendeeRepository;
import com.triptune.schedule.repository.TravelRouteRepository;
import com.triptune.schedule.repository.TravelScheduleRepository;
import com.triptune.travel.dto.response.PlaceResponse;
import com.triptune.travel.entity.TravelPlace;
import com.triptune.travel.enums.ThemeType;
import com.triptune.travel.fixture.TravelImageFixture;
import com.triptune.travel.fixture.TravelPlaceFixture;
import com.triptune.travel.repository.TravelPlaceRepository;
import com.triptune.global.message.ErrorCode;
import com.triptune.global.exception.DataNotFoundException;
import com.triptune.global.response.page.SchedulePageResponse;
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
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TravelScheduleServiceTest {
    @InjectMocks private TravelScheduleService travelScheduleService;
    @Mock private TravelScheduleRepository travelScheduleRepository;
    @Mock private MemberRepository memberRepository;
    @Mock private TravelAttendeeRepository travelAttendeeRepository;
    @Mock private TravelPlaceRepository travelPlaceRepository;
    @Mock private TravelRouteRepository travelRouteRepository;
    @Mock private ChatMessageRepository chatMessageRepository;
    @Mock private TravelRouteService travelRouteService;

    private TravelPlace placeWithThumbnail1;
    private TravelPlace placeWithThumbnail2;
    private TravelPlace placeWithoutThumbnail;

    private Member member1;
    private Member member2;

    private ProfileImage defaultImage;

    @BeforeEach
    void setUp(){
        Country country = CountryFixture.createCountry();
        City city = CityFixture.createCity(country, "서울");
        District district = DistrictFixture.createDistrict(city, "중구");
        ApiCategory apiCategory = ApiCategoryFixture.createApiCategory();
        ApiContentType apiContentType = ApiContentTypeFixture.createApiContentType(ThemeType.ATTRACTIONS);

        ProfileImage profileImage1 = ProfileImageFixture.createProfileImage("member1Image");
        member1 = MemberFixture.createNativeTypeMember("member1@email.com", profileImage1);
        ProfileImage profileImage2 = ProfileImageFixture.createProfileImage("member2Image");
        member2 = MemberFixture.createNativeTypeMember("member2@email.com", profileImage2);

        defaultImage = ProfileImageFixture.createProfileImage("defaultImage");

        placeWithThumbnail1 = TravelPlaceFixture.createTravelPlace(
                country,
                city,
                district,
                apiCategory,
                apiContentType,
                "여행지1"
        );
        TravelImageFixture.createTravelImage(placeWithThumbnail1, "test1", true);
        TravelImageFixture.createTravelImage(placeWithThumbnail1, "test2", false);

        placeWithThumbnail2 = TravelPlaceFixture.createTravelPlace(
                country,
                city,
                district,
                apiCategory,
                apiContentType,
                "여행지3"
        );
        TravelImageFixture.createTravelImage(placeWithThumbnail2, "test1", true);
        TravelImageFixture.createTravelImage(placeWithThumbnail2, "test2", false);

        placeWithoutThumbnail = TravelPlaceFixture.createTravelPlace(
                country,
                city,
                district,
                apiCategory,
                apiContentType,
                "여행지2"
        );
    }


    @Test
    @DisplayName("내 일정 목록 조회")
    void getAllSchedules(){
        // given
        Member authorMember = MemberFixture.createNativeTypeMemberWithId(1L, "authorMember@email.com", defaultImage);
        Member guestMember = MemberFixture.createNativeTypeMemberWithId(2L, "guestMember@email.com", defaultImage);

        TravelSchedule schedule1 = TravelScheduleFixture.createTravelSchedule("당일 여행");
        TravelAttendeeFixture.createAuthorTravelAttendee(schedule1, authorMember);
        TravelAttendeeFixture.createGuestTravelAttendee(schedule1, guestMember, AttendeePermission.READ);
        TravelRouteFixture.createTravelRoute(schedule1, placeWithThumbnail1, 1);
        TravelRouteFixture.createTravelRoute(schedule1, placeWithoutThumbnail, 2);
        TravelRouteFixture.createTravelRoute(schedule1, placeWithThumbnail2, 3);

        TravelSchedule schedule2 = TravelScheduleFixture.createTravelSchedule("도보 여행");
        TravelAttendeeFixture.createAuthorTravelAttendee(schedule2, guestMember);
        TravelAttendeeFixture.createGuestTravelAttendee(schedule2, authorMember, AttendeePermission.CHAT);

        TravelSchedule schedule3 = TravelScheduleFixture.createTravelSchedule("역사 여행");
        TravelAttendeeFixture.createAuthorTravelAttendee(schedule3, authorMember);

        Pageable pageable = PageUtils.schedulePageable(1);
        List<TravelSchedule> schedules = List.of(schedule1, schedule2, schedule3);
        Page<TravelSchedule> schedulePage = PageUtils.createPage(schedules, PageUtils.schedulePageable(1), schedules.size());

        when(travelScheduleRepository.findTravelSchedules(pageable, 1L)).thenReturn(schedulePage);
        when(travelScheduleRepository.countSharedTravelSchedules(anyLong())).thenReturn(2);

        // when
        SchedulePageResponse<ScheduleInfoResponse> response = travelScheduleService.getAllSchedules(1, authorMember.getMemberId());

        // then
        assertThat(response.getTotalElements()).isEqualTo(3);
        assertThat(response.getTotalSharedElements()).isEqualTo(2);
        assertThat(response.getContent()).hasSize(3);

        ScheduleInfoResponse first = response.getContent().get(0);
        ScheduleInfoResponse second = response.getContent().get(1);
        ScheduleInfoResponse third = response.getContent().get(2);

        assertAll(
                () -> assertThat(first.getScheduleName()).isEqualTo(schedule1.getScheduleName()),
                () -> assertThat(first.getSinceUpdate()).isNotNull(),
                () -> assertThat(first.getThumbnailUrl()).isEqualTo(placeWithThumbnail2.getThumbnailUrl()),
                () -> assertThat(first.getAuthor().getNickname()).isEqualTo(authorMember.getNickname()),
                () -> assertThat(first.getRole()).isEqualTo(AttendeeRole.AUTHOR)
        );
        assertAll(
                () -> assertThat(second.getScheduleName()).isEqualTo(schedule2.getScheduleName()),
                () -> assertThat(second.getThumbnailUrl()).isNull(),
                () -> assertThat(second.getAuthor().getNickname()).isEqualTo(guestMember.getNickname()),
                () -> assertThat(second.getRole()).isEqualTo(AttendeeRole.GUEST)
        );
        assertAll(
                () -> assertThat(third.getScheduleName()).isEqualTo(schedule3.getScheduleName()),
                () -> assertThat(third.getThumbnailUrl()).isNull(),
                () -> assertThat(third.getAuthor().getNickname()).isEqualTo(authorMember.getNickname()),
                () -> assertThat(third.getRole()).isEqualTo(AttendeeRole.AUTHOR)
        );
    }

    @Test
    @DisplayName("내 일정 목록 조회 시 공유된 일정이 없는 경우")
    void getAllSchedules_noSharedScheduleData(){
        // given
        Member authorMember = MemberFixture.createNativeTypeMemberWithId(1L, "authorMember@email.com", defaultImage);

        TravelSchedule schedule = TravelScheduleFixture.createTravelSchedule("역사 여행");
        TravelAttendeeFixture.createAuthorTravelAttendee(schedule, authorMember);

        Pageable pageable = PageUtils.schedulePageable(1);
        List<TravelSchedule> schedules = List.of(schedule);
        Page<TravelSchedule> schedulePage = PageUtils.createPage(schedules, pageable, schedules.size());

        when(travelScheduleRepository.findTravelSchedules(pageable, authorMember.getMemberId())).thenReturn(schedulePage);
        when(travelScheduleRepository.countSharedTravelSchedules(anyLong())).thenReturn(0);

        // when
        SchedulePageResponse<ScheduleInfoResponse> response = travelScheduleService.getAllSchedules(1, authorMember.getMemberId());

        // then
        List<ScheduleInfoResponse> content = response.getContent();
        assertThat(response.getTotalElements()).isEqualTo(1);
        assertThat(response.getTotalSharedElements()).isEqualTo(0);
        assertThat(content).hasSize(1);
        assertThat(content.get(0).getScheduleName()).isEqualTo(schedule.getScheduleName());
        assertThat(content.get(0).getSinceUpdate()).isNotNull();
        assertThat(content.get(0).getAuthor().getNickname()).isEqualTo(authorMember.getNickname());
    }

    @Test
    @DisplayName("내 일정 목록 조회 시 일정 데이터 없는 경우")
    void getAllSchedules_noScheduleData(){
        // given
        Pageable pageable = PageUtils.schedulePageable(1);
        Page<TravelSchedule> emptySchedulePage = PageUtils.createPage(Collections.emptyList(), pageable, 0);

        when(travelScheduleRepository.findTravelSchedules(pageable, 1L)).thenReturn(emptySchedulePage);
        when(travelScheduleRepository.countSharedTravelSchedules(anyLong())).thenReturn(2);

        // when
        SchedulePageResponse<ScheduleInfoResponse> response = travelScheduleService.getAllSchedules(1, 1L);

        // then
        assertThat(response.getTotalElements()).isEqualTo(0);
        assertThat(response.getTotalSharedElements()).isEqualTo(2);
        assertThat(response.getContent()).hasSize(0);
        assertThat(response.getContent()).isEmpty();
        verify(travelRouteRepository, times(0)).findAllByTravelSchedule_ScheduleId(any(), any());
    }

    @Test
    @DisplayName("내 일정 목록 조회 시 이미지 썸네일 없는 경우")
    void getAllSchedules_noImageThumbnail(){
        // given
        Member authorMember = MemberFixture.createNativeTypeMemberWithId(1L, "authorMember@email.com", defaultImage);
        Member guestMember = MemberFixture.createNativeTypeMemberWithId(2L, "guestMember@email.com", defaultImage);

        TravelSchedule schedule1 = TravelScheduleFixture.createTravelSchedule("당일 여행");
        TravelAttendeeFixture.createAuthorTravelAttendee(schedule1, authorMember);
        TravelAttendeeFixture.createGuestTravelAttendee(schedule1, guestMember, AttendeePermission.READ);
        TravelRouteFixture.createTravelRoute(schedule1, placeWithoutThumbnail, 1);

        TravelSchedule schedule2 = TravelScheduleFixture.createTravelSchedule("도보 여행");
        TravelAttendeeFixture.createAuthorTravelAttendee(schedule2, guestMember);
        TravelRouteFixture.createTravelRoute(schedule2, placeWithoutThumbnail, 1);

        TravelAttendeeFixture.createGuestTravelAttendee(schedule2, authorMember, AttendeePermission.CHAT);
        TravelSchedule schedule3 = TravelScheduleFixture.createTravelSchedule("역사 여행");
        TravelAttendeeFixture.createAuthorTravelAttendee(schedule3, authorMember);

        Pageable pageable = PageUtils.schedulePageable(1);
        List<TravelSchedule> schedules = List.of(schedule1, schedule2, schedule3);
        Page<TravelSchedule> schedulePage = PageUtils.createPage(schedules, pageable, schedules.size());

        when(travelScheduleRepository.findTravelSchedules(pageable, authorMember.getMemberId())).thenReturn(schedulePage);
        when(travelScheduleRepository.countSharedTravelSchedules(anyLong())).thenReturn(2);

        // when
        SchedulePageResponse<ScheduleInfoResponse> response = travelScheduleService.getAllSchedules(1, authorMember.getMemberId());

        // then
        assertThat(response.getTotalElements()).isEqualTo(3);
        assertThat(response.getTotalSharedElements()).isEqualTo(2);
        assertThat(response.getContent()).hasSize(3);

        ScheduleInfoResponse first = response.getContent().get(0);
        ScheduleInfoResponse second = response.getContent().get(1);
        ScheduleInfoResponse third = response.getContent().get(2);

        assertAll(
                () -> assertThat(first.getScheduleName()).isEqualTo(schedule1.getScheduleName()),
                () -> assertThat(first.getSinceUpdate()).isNotNull(),
                () -> assertThat(first.getThumbnailUrl()).isNull(),
                () -> assertThat(first.getAuthor().getNickname()).isEqualTo(authorMember.getNickname()),
                () -> assertThat(first.getRole()).isEqualTo(AttendeeRole.AUTHOR)
        );
        assertAll(
                () -> assertThat(second.getScheduleName()).isEqualTo(schedule2.getScheduleName()),
                () -> assertThat(second.getThumbnailUrl()).isNull(),
                () -> assertThat(second.getAuthor().getNickname()).isEqualTo(guestMember.getNickname()),
                () -> assertThat(second.getRole()).isEqualTo(AttendeeRole.GUEST)
        );
        assertAll(
                () -> assertThat(third.getScheduleName()).isEqualTo(schedule3.getScheduleName()),
                () -> assertThat(third.getThumbnailUrl()).isNull(),
                () -> assertThat(third.getAuthor().getNickname()).isEqualTo(authorMember.getNickname()),
                () -> assertThat(third.getRole()).isEqualTo(AttendeeRole.AUTHOR)
        );

    }


    @Test
    @DisplayName("내 일정 목록 조회 시 여행 루트 없는 경우")
    void getAllSchedules_noTravelRoute(){
        // given
        Member authorMember = MemberFixture.createNativeTypeMemberWithId(1L, "authorMember@email.com", defaultImage);
        Member guestMember = MemberFixture.createNativeTypeMemberWithId(2L, "guestMember@email.com", defaultImage);

        TravelSchedule schedule1 = TravelScheduleFixture.createTravelSchedule("당일 여행");
        TravelAttendeeFixture.createAuthorTravelAttendee(schedule1, authorMember);
        TravelAttendeeFixture.createGuestTravelAttendee(schedule1, guestMember, AttendeePermission.READ);

        TravelSchedule schedule2 = TravelScheduleFixture.createTravelSchedule("도보 여행");
        TravelAttendeeFixture.createAuthorTravelAttendee(schedule2, guestMember);
        TravelAttendeeFixture.createGuestTravelAttendee(schedule2, authorMember, AttendeePermission.CHAT);

        TravelSchedule schedule3 = TravelScheduleFixture.createTravelSchedule("역사 여행");
        TravelAttendeeFixture.createAuthorTravelAttendee(schedule3, authorMember);

        Pageable pageable = PageUtils.schedulePageable(1);

        List<TravelSchedule> schedules = List.of(schedule1, schedule2, schedule3);
        Page<TravelSchedule> schedulePage = PageUtils.createPage(schedules, pageable, schedules.size());

        when(travelScheduleRepository.findTravelSchedules(pageable, authorMember.getMemberId())).thenReturn(schedulePage);
        when(travelScheduleRepository.countSharedTravelSchedules(anyLong())).thenReturn(2);

        // when
        SchedulePageResponse<ScheduleInfoResponse> response = travelScheduleService.getAllSchedules(1, authorMember.getMemberId());

        // then
        assertThat(response.getTotalElements()).isEqualTo(3);
        assertThat(response.getTotalSharedElements()).isEqualTo(2);
        assertThat(response.getContent()).hasSize(3);

        ScheduleInfoResponse first = response.getContent().get(0);
        ScheduleInfoResponse second = response.getContent().get(1);
        ScheduleInfoResponse third = response.getContent().get(2);

        assertAll(
                () -> assertThat(first.getScheduleName()).isEqualTo(schedule1.getScheduleName()),
                () -> assertThat(first.getSinceUpdate()).isNotNull(),
                () -> assertThat(first.getThumbnailUrl()).isNull(),
                () -> assertThat(first.getAuthor().getNickname()).isEqualTo(authorMember.getNickname()),
                () -> assertThat(first.getRole()).isEqualTo(AttendeeRole.AUTHOR)
        );
        assertAll(
                () -> assertThat(second.getScheduleName()).isEqualTo(schedule2.getScheduleName()),
                () -> assertThat(second.getThumbnailUrl()).isNull(),
                () -> assertThat(second.getAuthor().getNickname()).isEqualTo(guestMember.getNickname()),
                () -> assertThat(second.getRole()).isEqualTo(AttendeeRole.GUEST)
        );
        assertAll(
                () -> assertThat(third.getScheduleName()).isEqualTo(schedule3.getScheduleName()),
                () -> assertThat(third.getThumbnailUrl()).isNull(),
                () -> assertThat(third.getAuthor().getNickname()).isEqualTo(authorMember.getNickname()),
                () -> assertThat(third.getRole()).isEqualTo(AttendeeRole.AUTHOR)
        );

    }

    @Test
    @DisplayName("공유된 일정 목록 조회")
    void getSharedSchedules(){
        // given
        Member authorMember = MemberFixture.createNativeTypeMemberWithId(1L, "authorMember@email.com", defaultImage);
        Member guestMember = MemberFixture.createNativeTypeMemberWithId(2L, "guestMember@email.com", defaultImage);

        TravelSchedule schedule1 = TravelScheduleFixture.createTravelSchedule("당일 여행");
        TravelAttendeeFixture.createAuthorTravelAttendee(schedule1, authorMember);
        TravelAttendeeFixture.createGuestTravelAttendee(schedule1, guestMember, AttendeePermission.READ);
        TravelRouteFixture.createTravelRoute(schedule1, placeWithThumbnail1, 1);
        TravelRouteFixture.createTravelRoute(schedule1, placeWithoutThumbnail, 2);
        TravelRouteFixture.createTravelRoute(schedule1, placeWithThumbnail2, 3);

        TravelSchedule schedule2 = TravelScheduleFixture.createTravelSchedule("도보 여행");
        TravelAttendeeFixture.createAuthorTravelAttendee(schedule2, guestMember);
        TravelAttendeeFixture.createGuestTravelAttendee(schedule2, authorMember, AttendeePermission.CHAT);

        TravelSchedule schedule3 = TravelScheduleFixture.createTravelSchedule("역사 여행");
        TravelAttendeeFixture.createAuthorTravelAttendee(schedule3, authorMember);


        Pageable pageable = PageUtils.schedulePageable(1);
        List<TravelSchedule> schedules = List.of(schedule1, schedule2);
        Page<TravelSchedule> schedulePage = PageUtils.createPage(schedules, pageable, schedules.size());

        when(travelScheduleRepository.findSharedTravelSchedules(pageable, 1L)).thenReturn(schedulePage);
        when(travelScheduleRepository.countTravelSchedules(anyLong())).thenReturn(3);

        // when
        SchedulePageResponse<ScheduleInfoResponse> response = travelScheduleService.getSharedSchedules(1, authorMember.getMemberId());

        // then
        assertThat(response.getTotalElements()).isEqualTo(3);
        assertThat(response.getTotalSharedElements()).isEqualTo(2);
        assertThat(response.getContent()).hasSize(2);

        ScheduleInfoResponse first = response.getContent().get(0);
        ScheduleInfoResponse second = response.getContent().get(1);

        assertAll(
                () -> assertThat(first.getScheduleName()).isEqualTo(schedule1.getScheduleName()),
                () -> assertThat(first.getSinceUpdate()).isNotNull(),
                () -> assertThat(first.getThumbnailUrl()).isEqualTo(placeWithThumbnail2.getThumbnailUrl()),
                () -> assertThat(first.getAuthor().getNickname()).isEqualTo(authorMember.getNickname()),
                () -> assertThat(first.getRole()).isEqualTo(AttendeeRole.AUTHOR)
        );
        assertAll(
                () -> assertThat(second.getScheduleName()).isEqualTo(schedule2.getScheduleName()),
                () -> assertThat(second.getThumbnailUrl()).isNull(),
                () -> assertThat(second.getAuthor().getNickname()).isEqualTo(guestMember.getNickname()),
                () -> assertThat(second.getRole()).isEqualTo(AttendeeRole.GUEST)
        );
    }


    @Test
    @DisplayName("공유된 일정 목록 조회 시 일정 데이터 없는 경우")
    void getSharedSchedules_noScheduleData(){
        // given
        Pageable pageable = PageUtils.schedulePageable(1);
        Page<TravelSchedule> emptySchedulePage = PageUtils.createPage(Collections.emptyList(), pageable, 0);

        when(travelScheduleRepository.findSharedTravelSchedules(pageable, 1L)).thenReturn(emptySchedulePage);
        when(travelScheduleRepository.countTravelSchedules(anyLong())).thenReturn(2);

        // when
        SchedulePageResponse<ScheduleInfoResponse> response = travelScheduleService.getSharedSchedules(1, 1L);

        // then
        assertThat(response.getTotalElements()).isEqualTo(2);
        assertThat(response.getTotalSharedElements()).isEqualTo(0);
        assertThat(response.getContent()).hasSize(0);
        assertThat(response.getContent()).isEmpty();
    }

    @Test
    @DisplayName("공유된 일정 목록 조회 시 이미지 썸네일 데이터 없는 경우")
    void getSharedSchedules_noImageThumbnail(){
        // given
        Member authorMember = MemberFixture.createNativeTypeMemberWithId(1L, "authorMember@email.com", defaultImage);
        Member guestMember = MemberFixture.createNativeTypeMemberWithId(2L, "guestMember@email.com", defaultImage);

        TravelSchedule schedule1 = TravelScheduleFixture.createTravelSchedule("당일 여행");
        TravelAttendeeFixture.createAuthorTravelAttendee(schedule1, authorMember);
        TravelAttendeeFixture.createGuestTravelAttendee(schedule1, guestMember, AttendeePermission.READ);
        TravelRouteFixture.createTravelRoute(schedule1, placeWithoutThumbnail, 1);

        TravelSchedule schedule2 = TravelScheduleFixture.createTravelSchedule("도보 여행");
        TravelAttendeeFixture.createAuthorTravelAttendee(schedule2, guestMember);
        TravelAttendeeFixture.createGuestTravelAttendee(schedule2, authorMember, AttendeePermission.CHAT);
        TravelRouteFixture.createTravelRoute(schedule2, placeWithoutThumbnail, 1);

        TravelSchedule schedule3 = TravelScheduleFixture.createTravelSchedule("역사 여행");
        TravelAttendeeFixture.createAuthorTravelAttendee(schedule3, authorMember);
        TravelRouteFixture.createTravelRoute(schedule3, placeWithoutThumbnail, 1);

        Pageable pageable = PageUtils.schedulePageable(1);
        List<TravelSchedule> schedules = List.of(schedule1, schedule2);
        Page<TravelSchedule> schedulePage = PageUtils.createPage(schedules, pageable, schedules.size());

        when(travelScheduleRepository.findSharedTravelSchedules(pageable, authorMember.getMemberId())).thenReturn(schedulePage);
        when(travelScheduleRepository.countTravelSchedules(anyLong())).thenReturn(3);

        // when
        SchedulePageResponse<ScheduleInfoResponse> response = travelScheduleService.getSharedSchedules(1, authorMember.getMemberId());

        // then
        assertThat(response.getTotalElements()).isEqualTo(3);
        assertThat(response.getTotalSharedElements()).isEqualTo(2);
        assertThat(response.getContent()).hasSize(2);

        ScheduleInfoResponse first = response.getContent().get(0);
        ScheduleInfoResponse second = response.getContent().get(1);

        assertAll(
                () -> assertThat(first.getScheduleName()).isEqualTo(schedule1.getScheduleName()),
                () -> assertThat(first.getSinceUpdate()).isNotNull(),
                () -> assertThat(first.getThumbnailUrl()).isNull(),
                () -> assertThat(first.getAuthor().getNickname()).isEqualTo(authorMember.getNickname()),
                () -> assertThat(first.getRole()).isEqualTo(AttendeeRole.AUTHOR)
        );
        assertAll(
                () -> assertThat(second.getScheduleName()).isEqualTo(schedule2.getScheduleName()),
                () -> assertThat(second.getThumbnailUrl()).isNull(),
                () -> assertThat(second.getAuthor().getNickname()).isEqualTo(guestMember.getNickname()),
                () -> assertThat(second.getRole()).isEqualTo(AttendeeRole.GUEST)
        );
    }


    @Test
    @DisplayName("공유된 일정 목록 조회 시 여행 루트 없는 경우")
    void getSharedSchedules_noTravelRoutes(){
        // given
        Member authorMember = MemberFixture.createNativeTypeMemberWithId(1L, "authorMember@email.com", defaultImage);
        Member guestMember = MemberFixture.createNativeTypeMemberWithId(2L, "guestMember@email.com", defaultImage);

        TravelSchedule schedule1 = TravelScheduleFixture.createTravelSchedule("당일 여행");
        TravelAttendeeFixture.createAuthorTravelAttendee(schedule1, authorMember);
        TravelAttendeeFixture.createGuestTravelAttendee(schedule1, guestMember, AttendeePermission.READ);

        TravelSchedule schedule2 = TravelScheduleFixture.createTravelSchedule("도보 여행");
        TravelAttendeeFixture.createAuthorTravelAttendee(schedule2, guestMember);
        TravelAttendeeFixture.createGuestTravelAttendee(schedule2, authorMember, AttendeePermission.CHAT);

        TravelSchedule schedule3 = TravelScheduleFixture.createTravelSchedule("역사 여행");
        TravelAttendeeFixture.createAuthorTravelAttendee(schedule3, authorMember);


        Pageable pageable = PageUtils.schedulePageable(1);
        List<TravelSchedule> schedules = List.of(schedule1, schedule2);
        Page<TravelSchedule> schedulePage = PageUtils.createPage(schedules, pageable, schedules.size());

        when(travelScheduleRepository.findSharedTravelSchedules(pageable, authorMember.getMemberId())).thenReturn(schedulePage);
        when(travelScheduleRepository.countTravelSchedules(anyLong())).thenReturn(3);

        // when
        SchedulePageResponse<ScheduleInfoResponse> response = travelScheduleService.getSharedSchedules(1, authorMember.getMemberId());

        // then
        assertThat(response.getTotalElements()).isEqualTo(3);
        assertThat(response.getTotalSharedElements()).isEqualTo(2);
        assertThat(response.getContent()).hasSize(2);

        ScheduleInfoResponse first = response.getContent().get(0);
        ScheduleInfoResponse second = response.getContent().get(1);

        assertAll(
                () -> assertThat(first.getScheduleName()).isEqualTo(schedule1.getScheduleName()),
                () -> assertThat(first.getSinceUpdate()).isNotNull(),
                () -> assertThat(first.getThumbnailUrl()).isNull(),
                () -> assertThat(first.getAuthor().getNickname()).isEqualTo(authorMember.getNickname()),
                () -> assertThat(first.getRole()).isEqualTo(AttendeeRole.AUTHOR)
        );
        assertAll(
                () -> assertThat(second.getScheduleName()).isEqualTo(schedule2.getScheduleName()),
                () -> assertThat(second.getThumbnailUrl()).isNull(),
                () -> assertThat(second.getAuthor().getNickname()).isEqualTo(guestMember.getNickname()),
                () -> assertThat(second.getRole()).isEqualTo(AttendeeRole.GUEST)
        );

    }

    @Test
    @DisplayName("수정 권한 있는 내 일정 목록 조회")
    void getEnableEditSchedule(){
        // given
        TravelSchedule schedule1 = TravelScheduleFixture.createTravelScheduleWithId(1L, "당일 여행");
        TravelAttendeeFixture.createAuthorTravelAttendee(schedule1, member1);
        TravelAttendeeFixture.createGuestTravelAttendee(schedule1, member2, AttendeePermission.READ);
        TravelRouteFixture.createTravelRoute(schedule1, placeWithThumbnail1, 1);
        TravelRouteFixture.createTravelRoute(schedule1, placeWithoutThumbnail, 2);
        TravelRouteFixture.createTravelRoute(schedule1, placeWithThumbnail2, 3);

        TravelSchedule schedule2 = TravelScheduleFixture.createTravelScheduleWithId(2L, "도보 여행");
        TravelAttendeeFixture.createAuthorTravelAttendee(schedule2, member2);
        TravelAttendeeFixture.createGuestTravelAttendee(schedule2, member1, AttendeePermission.CHAT);

        TravelSchedule schedule3 = TravelScheduleFixture.createTravelScheduleWithId(3L, "역사 여행");
        TravelAttendeeFixture.createAuthorTravelAttendee(schedule3, member1);


        Pageable pageable = PageUtils.scheduleModalPageable(1);
        List<TravelSchedule> schedules = List.of(schedule1);
        Page<TravelSchedule> schedulePage = PageUtils.createPage(schedules, pageable, schedules.size());

        when(travelScheduleRepository.findEnableEditTravelSchedules(any(), anyLong())).thenReturn(schedulePage);
        when(travelAttendeeRepository.findAuthorNicknameByScheduleId(anyLong())).thenReturn(member1.getNickname());

        // when
        Page<OverviewScheduleResponse> response = travelScheduleService.getEnableEditSchedule(1, 1L);

        // then
        List<OverviewScheduleResponse> content = response.getContent();
        assertThat(response.getTotalElements()).isEqualTo(1);
        assertThat(content.get(0).getScheduleName()).isEqualTo(schedule1.getScheduleName());
        assertThat(content.get(0).getStartDate()).isEqualTo(schedule1.getStartDate());
        assertThat(content.get(0).getAuthor()).isEqualTo(member1.getNickname());
    }

    @Test
    @DisplayName("수정 권한 있는 내 일정 목록 조회 시 일정 데이터 존재하지 않는 경우")
    void getEnableEditSchedule_noScheduleData(){
        // given
        TravelSchedule schedule1 = TravelScheduleFixture.createTravelSchedule("당일 여행");
        TravelAttendeeFixture.createAuthorTravelAttendee(schedule1, member1);
        TravelAttendeeFixture.createGuestTravelAttendee(schedule1, member2, AttendeePermission.READ);
        TravelRouteFixture.createTravelRoute(schedule1, placeWithThumbnail1, 1);
        TravelRouteFixture.createTravelRoute(schedule1, placeWithoutThumbnail, 2);
        TravelRouteFixture.createTravelRoute(schedule1, placeWithThumbnail2, 3);

        TravelSchedule schedule2 = TravelScheduleFixture.createTravelSchedule("도보 여행");
        TravelAttendeeFixture.createAuthorTravelAttendee(schedule2, member2);
        TravelAttendeeFixture.createGuestTravelAttendee(schedule2, member1, AttendeePermission.CHAT);

        TravelSchedule schedule3 = TravelScheduleFixture.createTravelSchedule("역사 여행");
        TravelAttendeeFixture.createAuthorTravelAttendee(schedule3, member1);

        Pageable pageable = PageUtils.scheduleModalPageable(1);
        Page<TravelSchedule> schedulePage = PageUtils.createPage(Collections.emptyList(), pageable, 0);

        when(travelScheduleRepository.findEnableEditTravelSchedules(any(), anyLong())).thenReturn(schedulePage);

        // when
        Page<OverviewScheduleResponse> response = travelScheduleService.getEnableEditSchedule(1, 1L);

        // then
        assertThat(response.getTotalElements()).isZero();
        assertThat(response.getContent()).isEmpty();
    }

    @Test
    @DisplayName("내 일정 검색")
    void searchAllSchedules(){
        // given
        Member authorMember = MemberFixture.createNativeTypeMemberWithId(1L, "authorMember@email.com", defaultImage);
        Member guestMember = MemberFixture.createNativeTypeMemberWithId(2L, "guestMember@email.com", defaultImage);

        TravelSchedule schedule1 = TravelScheduleFixture.createTravelSchedule("당일 여행");
        TravelAttendeeFixture.createAuthorTravelAttendee(schedule1, authorMember);
        TravelAttendeeFixture.createGuestTravelAttendee(schedule1, guestMember, AttendeePermission.READ);
        TravelRouteFixture.createTravelRoute(schedule1, placeWithThumbnail1, 1);
        TravelRouteFixture.createTravelRoute(schedule1, placeWithoutThumbnail, 2);
        TravelRouteFixture.createTravelRoute(schedule1, placeWithThumbnail2, 3);

        String keyword = "서울";

        Pageable pageable = PageUtils.schedulePageable(1);
        List<TravelSchedule> schedules = List.of(schedule1);
        Page<TravelSchedule> schedulePage = PageUtils.createPage(schedules, pageable, schedules.size());

        when(travelScheduleRepository.searchTravelSchedules(pageable, keyword, authorMember.getMemberId()))
                .thenReturn(schedulePage);
        when(travelScheduleRepository.countTravelSchedules(anyLong())).thenReturn(2);
        when(travelScheduleRepository.countSharedTravelSchedules(anyLong())).thenReturn(2);

        // when
        SchedulePageResponse<ScheduleInfoResponse> response
                = travelScheduleService.searchAllSchedules(1, keyword, authorMember.getMemberId());

        // then
        assertThat(response.getTotalElements()).isEqualTo(2);
        assertThat(response.getTotalSharedElements()).isEqualTo(2);
        assertThat(response.getContent()).hasSize(1);

        ScheduleInfoResponse first = response.getContent().get(0);

        assertAll(
                () -> assertThat(first.getScheduleName()).isEqualTo(schedule1.getScheduleName()),
                () -> assertThat(first.getSinceUpdate()).isNotNull(),
                () -> assertThat(first.getThumbnailUrl()).isEqualTo(placeWithThumbnail2.getThumbnailUrl()),
                () -> assertThat(first.getAuthor().getNickname()).isEqualTo(authorMember.getNickname()),
                () -> assertThat(first.getRole()).isEqualTo(AttendeeRole.AUTHOR)
        );

    }

    @Test
    @DisplayName("내 일정 검색 시 공유된 일정이 없는 경우")
    void searchAllSchedules_noSharedSchedule(){
        // given
        Member authorMember = MemberFixture.createNativeTypeMemberWithId(1L, "authorMember@email.com", defaultImage);

        TravelSchedule schedule1 = TravelScheduleFixture.createTravelSchedule("당일 여행");
        TravelAttendeeFixture.createAuthorTravelAttendee(schedule1, authorMember);
        TravelRouteFixture.createTravelRoute(schedule1, placeWithThumbnail1, 1);
        TravelRouteFixture.createTravelRoute(schedule1, placeWithoutThumbnail, 2);
        TravelRouteFixture.createTravelRoute(schedule1, placeWithThumbnail2, 3);

        String keyword = "서울";
        Pageable pageable = PageUtils.schedulePageable(1);

        List<TravelSchedule> schedules = List.of(schedule1);
        Page<TravelSchedule> schedulePage = PageUtils.createPage(schedules, pageable, schedules.size());

        when(travelScheduleRepository.searchTravelSchedules(pageable, keyword, authorMember.getMemberId()))
                .thenReturn(schedulePage);
        when(travelScheduleRepository.countTravelSchedules(anyLong())).thenReturn(1);
        when(travelScheduleRepository.countSharedTravelSchedules(anyLong())).thenReturn(0);

        // when
        SchedulePageResponse<ScheduleInfoResponse> response
                = travelScheduleService.searchAllSchedules(1, keyword, authorMember.getMemberId());

        // then
        List<ScheduleInfoResponse> content = response.getContent();
        assertThat(response.getTotalElements()).isEqualTo(1);
        assertThat(response.getTotalSharedElements()).isEqualTo(0);
        assertThat(content).hasSize(1);
        assertThat(content.get(0).getScheduleName()).isEqualTo(schedule1.getScheduleName());
        assertThat(content.get(0).getSinceUpdate()).isNotNull();
        assertThat(content.get(0).getAuthor().getNickname()).isEqualTo(authorMember.getNickname());
    }

    @Test
    @DisplayName("내 일정 검색 시 검색 결과가 없는 경우")
    void searchAllSchedules_emptyResult(){
        // given
        String keyword = "ㅁㄴㅇㄹ";
        Pageable pageable = PageUtils.schedulePageable(1);
        Page<TravelSchedule> emptySchedulePage = PageUtils.createPage(Collections.emptyList(), pageable, 0);

        when(travelScheduleRepository.searchTravelSchedules(pageable, keyword, 1L)).thenReturn(emptySchedulePage);
        when(travelScheduleRepository.countSharedTravelSchedules(anyLong())).thenReturn(2);

        // when
        SchedulePageResponse<ScheduleInfoResponse> response = travelScheduleService.searchAllSchedules(1, keyword, 1L);

        // then
        assertThat(response.getTotalElements()).isEqualTo(0);
        assertThat(response.getTotalSharedElements()).isEqualTo(2);
        assertThat(response.getContent()).isEmpty();
    }

    @Test
    @DisplayName("내 일정 검색 시 이미지 썸네일 없는 경우")
    void searchAllSchedules_noImageThumbnail(){
        // given
        Member authorMember = MemberFixture.createNativeTypeMemberWithId(1L, "authorMember@email.com", defaultImage);
        Member guestMember = MemberFixture.createNativeTypeMemberWithId(2L, "guestMember@email.com", defaultImage);

        TravelSchedule schedule1 = TravelScheduleFixture.createTravelSchedule("당일 여행");
        TravelAttendeeFixture.createAuthorTravelAttendee(schedule1, authorMember);
        TravelAttendeeFixture.createGuestTravelAttendee(schedule1, guestMember, AttendeePermission.READ);
        TravelRouteFixture.createTravelRoute(schedule1, placeWithoutThumbnail, 1);

        TravelSchedule schedule2 = TravelScheduleFixture.createTravelSchedule("도보 여행");
        TravelAttendeeFixture.createAuthorTravelAttendee(schedule2, guestMember);
        TravelAttendeeFixture.createGuestTravelAttendee(schedule2, authorMember, AttendeePermission.CHAT);
        TravelRouteFixture.createTravelRoute(schedule2, placeWithoutThumbnail, 1);

        String keyword = "여행";

        Pageable pageable = PageUtils.schedulePageable(1);
        List<TravelSchedule> schedules = List.of(schedule1, schedule2);
        Page<TravelSchedule> schedulePage = PageUtils.createPage(schedules, pageable, schedules.size());

        when(travelScheduleRepository.searchTravelSchedules(pageable, keyword, authorMember.getMemberId()))
                .thenReturn(schedulePage);
        when(travelScheduleRepository.countTravelSchedules(anyLong())).thenReturn(2);
        when(travelScheduleRepository.countSharedTravelSchedules(anyLong())).thenReturn(2);

        // when
        SchedulePageResponse<ScheduleInfoResponse> response
                = travelScheduleService.searchAllSchedules(1, keyword, authorMember.getMemberId());

        // then
        assertThat(response.getTotalElements()).isEqualTo(2);
        assertThat(response.getTotalSharedElements()).isEqualTo(2);
        assertThat(response.getContent()).hasSize(2);

        ScheduleInfoResponse first = response.getContent().get(0);
        ScheduleInfoResponse second = response.getContent().get(1);

        assertAll(
                () -> assertThat(first.getScheduleName()).isEqualTo(schedule1.getScheduleName()),
                () -> assertThat(first.getSinceUpdate()).isNotNull(),
                () -> assertThat(first.getThumbnailUrl()).isNull(),
                () -> assertThat(first.getAuthor().getNickname()).isEqualTo(authorMember.getNickname()),
                () -> assertThat(first.getRole()).isEqualTo(AttendeeRole.AUTHOR)
        );
        assertAll(
                () -> assertThat(second.getScheduleName()).isEqualTo(schedule2.getScheduleName()),
                () -> assertThat(second.getThumbnailUrl()).isNull(),
                () -> assertThat(second.getAuthor().getNickname()).isEqualTo(guestMember.getNickname()),
                () -> assertThat(second.getRole()).isEqualTo(AttendeeRole.GUEST)
        );
    }


    @Test
    @DisplayName("내 일정 검색 시 여행 루트 없는 경우")
    void searchAllSchedules_noTravelRoutes(){
        // given
        Member authorMember = MemberFixture.createNativeTypeMemberWithId(1L, "authorMember@email.com", defaultImage);
        Member guestMember = MemberFixture.createNativeTypeMemberWithId(2L, "guestMember@email.com", defaultImage);

        TravelSchedule schedule1 = TravelScheduleFixture.createTravelSchedule( "당일 여행");
        TravelAttendeeFixture.createAuthorTravelAttendee(schedule1, authorMember);
        TravelAttendeeFixture.createGuestTravelAttendee(schedule1, guestMember, AttendeePermission.READ);

        String keyword = "서울";

        Pageable pageable = PageUtils.schedulePageable(1);
        List<TravelSchedule> schedules = List.of(schedule1);
        Page<TravelSchedule> schedulePage = PageUtils.createPage(schedules, pageable, schedules.size());

        when(travelScheduleRepository.searchTravelSchedules(pageable, keyword, authorMember.getMemberId())).thenReturn(schedulePage);
        when(travelScheduleRepository.countTravelSchedules(anyLong())).thenReturn(2);
        when(travelScheduleRepository.countSharedTravelSchedules(anyLong())).thenReturn(2);

        // when
        SchedulePageResponse<ScheduleInfoResponse> response
                = travelScheduleService.searchAllSchedules(1, keyword, authorMember.getMemberId());

        // then
        List<ScheduleInfoResponse> content = response.getContent();
        assertThat(response.getTotalElements()).isEqualTo(2);
        assertThat(response.getTotalSharedElements()).isEqualTo(2);
        assertThat(response.getContent()).hasSize(1);
        assertThat(content.get(0).getScheduleName()).isEqualTo(schedule1.getScheduleName());
        assertThat(content.get(0).getSinceUpdate()).isNotNull();
        assertThat(content.get(0).getThumbnailUrl()).isNull();

    }

    @Test
    @DisplayName("공유된 일정 검색")
    void searchSharedSchedules(){
        // given
        Member authorMember = MemberFixture.createNativeTypeMemberWithId(1L, "authorMember@email.com", defaultImage);
        Member guestMember = MemberFixture.createNativeTypeMemberWithId(2L, "guestMember@email.com", defaultImage);

        TravelSchedule schedule1 = TravelScheduleFixture.createTravelSchedule("당일 여행");
        TravelAttendeeFixture.createAuthorTravelAttendee(schedule1, authorMember);
        TravelAttendeeFixture.createGuestTravelAttendee(schedule1, guestMember, AttendeePermission.READ);
        TravelRouteFixture.createTravelRoute(schedule1, placeWithThumbnail1, 1);
        TravelRouteFixture.createTravelRoute(schedule1, placeWithoutThumbnail, 2);
        TravelRouteFixture.createTravelRoute(schedule1, placeWithThumbnail2, 3);

        TravelSchedule schedule2 = TravelScheduleFixture.createTravelSchedule("도보 여행");
        TravelAttendeeFixture.createAuthorTravelAttendee(schedule2, guestMember);
        TravelAttendeeFixture.createGuestTravelAttendee(schedule2, authorMember, AttendeePermission.CHAT);

        String keyword = "여행";

        Pageable pageable = PageUtils.schedulePageable(1);
        List<TravelSchedule> schedules = List.of(schedule1, schedule2);
        Page<TravelSchedule> schedulePage = PageUtils.createPage(schedules, pageable, schedules.size());

        when(travelScheduleRepository.searchSharedTravelSchedules(pageable, keyword, authorMember.getMemberId())).thenReturn(schedulePage);
        when(travelScheduleRepository.countTravelSchedules(anyLong())).thenReturn(3);
        when(travelScheduleRepository.countSharedTravelSchedules(anyLong())).thenReturn(2);

        // when
        SchedulePageResponse<ScheduleInfoResponse> response
                = travelScheduleService.searchSharedSchedules(1, keyword, authorMember.getMemberId());

        // then
        assertThat(response.getTotalElements()).isEqualTo(3);
        assertThat(response.getTotalSharedElements()).isEqualTo(2);
        assertThat(response.getContent()).hasSize(2);

        ScheduleInfoResponse first = response.getContent().get(0);
        ScheduleInfoResponse second = response.getContent().get(1);

        assertAll(
                () -> assertThat(first.getScheduleName()).isEqualTo(schedule1.getScheduleName()),
                () -> assertThat(first.getSinceUpdate()).isNotNull(),
                () -> assertThat(first.getThumbnailUrl()).isEqualTo(placeWithThumbnail2.getThumbnailUrl()),
                () -> assertThat(first.getAuthor().getNickname()).isEqualTo(authorMember.getNickname()),
                () -> assertThat(first.getRole()).isEqualTo(AttendeeRole.AUTHOR)
        );
        assertAll(
                () -> assertThat(second.getScheduleName()).isEqualTo(schedule2.getScheduleName()),
                () -> assertThat(second.getThumbnailUrl()).isNull(),
                () -> assertThat(second.getAuthor().getNickname()).isEqualTo(guestMember.getNickname()),
                () -> assertThat(second.getRole()).isEqualTo(AttendeeRole.GUEST)
        );

    }

    @Test
    @DisplayName("공유된 일정 검색 시 일정 데이터 없는 경우")
    void searchSharedSchedules_noScheduleData(){
        // given
        String keyword = "테스트";
        Pageable pageable = PageUtils.schedulePageable(1);
        Page<TravelSchedule> emptySchedulePage = PageUtils.createPage(Collections.emptyList(), pageable, 0);

        when(travelScheduleRepository.searchSharedTravelSchedules(pageable, keyword, 1L)).thenReturn(emptySchedulePage);
        when(travelScheduleRepository.countTravelSchedules(anyLong())).thenReturn(2);

        // when
        SchedulePageResponse<ScheduleInfoResponse> response = travelScheduleService.searchSharedSchedules(1, keyword, 1L);

        // then
        assertThat(response.getTotalElements()).isEqualTo(2);
        assertThat(response.getTotalSharedElements()).isEqualTo(0);
        assertThat(response.getContent()).isEmpty();
    }

    @Test
    @DisplayName("공유된 일정 검색 시 이미지 썸네일 없는 경우")
    void searchSharedSchedules_noImageThumbnail(){
        // given
        Member authorMember = MemberFixture.createNativeTypeMemberWithId(1L, "authorMember@email.com", defaultImage);
        Member guestMember = MemberFixture.createNativeTypeMemberWithId(2L, "guestMember@email.com", defaultImage);

        TravelSchedule schedule1 = TravelScheduleFixture.createTravelSchedule("당일 여행");
        TravelAttendeeFixture.createAuthorTravelAttendee(schedule1, authorMember);
        TravelAttendeeFixture.createGuestTravelAttendee(schedule1, guestMember, AttendeePermission.READ);
        TravelRouteFixture.createTravelRoute(schedule1, placeWithoutThumbnail, 1);

        TravelSchedule schedule2 = TravelScheduleFixture.createTravelSchedule("도보 여행");
        TravelAttendeeFixture.createAuthorTravelAttendee(schedule2, guestMember);
        TravelRouteFixture.createTravelRoute(schedule2, placeWithoutThumbnail, 1);
        TravelAttendeeFixture.createGuestTravelAttendee(schedule2, authorMember, AttendeePermission.CHAT);

        String keyword = "여행";

        Pageable pageable = PageUtils.schedulePageable(1);
        List<TravelSchedule> schedules = List.of(schedule1, schedule2);
        Page<TravelSchedule> schedulePage = PageUtils.createPage(schedules, pageable, schedules.size());

        when(travelScheduleRepository.searchSharedTravelSchedules(pageable, keyword, authorMember.getMemberId())).thenReturn(schedulePage);
        when(travelScheduleRepository.countTravelSchedules(anyLong())).thenReturn(3);
        when(travelScheduleRepository.countSharedTravelSchedules(anyLong())).thenReturn(2);

        // when
        SchedulePageResponse<ScheduleInfoResponse> response
                = travelScheduleService.searchSharedSchedules(1, keyword, authorMember.getMemberId());

        // then
        assertThat(response.getTotalElements()).isEqualTo(3);
        assertThat(response.getTotalSharedElements()).isEqualTo(2);
        assertThat(response.getContent()).hasSize(2);

        ScheduleInfoResponse first = response.getContent().get(0);
        ScheduleInfoResponse second = response.getContent().get(1);

        assertAll(
                () -> assertThat(first.getScheduleName()).isEqualTo(schedule1.getScheduleName()),
                () -> assertThat(first.getSinceUpdate()).isNotNull(),
                () -> assertThat(first.getThumbnailUrl()).isNull(),
                () -> assertThat(first.getAuthor().getNickname()).isEqualTo(authorMember.getNickname()),
                () -> assertThat(first.getRole()).isEqualTo(AttendeeRole.AUTHOR)
        );
        assertAll(
                () -> assertThat(second.getScheduleName()).isEqualTo(schedule2.getScheduleName()),
                () -> assertThat(second.getThumbnailUrl()).isNull(),
                () -> assertThat(second.getAuthor().getNickname()).isEqualTo(guestMember.getNickname()),
                () -> assertThat(second.getRole()).isEqualTo(AttendeeRole.GUEST)
        );

    }


    @Test
    @DisplayName("공유된 일정 검색 시 여행 루트 없는 경우")
    void searchSharedSchedules_noTravelRoutes(){
        // given
        Member authorMember = MemberFixture.createNativeTypeMemberWithId(1L, "authorMember@email.com", defaultImage);
        Member guestMember = MemberFixture.createNativeTypeMemberWithId(2L, "guestMember@email.com", defaultImage);

        TravelSchedule schedule1 = TravelScheduleFixture.createTravelSchedule("당일 여행");
        TravelAttendeeFixture.createAuthorTravelAttendee(schedule1, authorMember);
        TravelAttendeeFixture.createGuestTravelAttendee(schedule1, guestMember, AttendeePermission.READ);

        String keyword = "여행";

        Pageable pageable = PageUtils.schedulePageable(1);
        List<TravelSchedule> schedules = List.of(schedule1);
        Page<TravelSchedule> schedulePage = PageUtils.createPage(schedules, pageable, 2);

        when(travelScheduleRepository.searchSharedTravelSchedules(pageable, keyword, authorMember.getMemberId()))
                .thenReturn(schedulePage);
        when(travelScheduleRepository.countTravelSchedules(anyLong())).thenReturn(3);
        when(travelScheduleRepository.countSharedTravelSchedules(anyLong())).thenReturn(2);

        // when
        SchedulePageResponse<ScheduleInfoResponse> response
                = travelScheduleService.searchSharedSchedules(1, keyword, authorMember.getMemberId());

        // then
        List<ScheduleInfoResponse> content = response.getContent();
        assertThat(response.getTotalElements()).isEqualTo(3);
        assertThat(response.getTotalSharedElements()).isEqualTo(2);
        assertThat(response.getContent()).hasSize(1);
        assertThat(content.get(0).getScheduleName()).isEqualTo(schedule1.getScheduleName());
        assertThat(content.get(0).getSinceUpdate()).isNotNull();
        assertThat(content.get(0).getThumbnailUrl()).isNull();

    }

    @Test
    @DisplayName("TravelSchedule 를 ScheduleInfoResponse 로 변경")
    void createScheduleInfoResponse(){
        // given
        Member authorMember = MemberFixture.createNativeTypeMemberWithId(1L, "authorMember@email.com", defaultImage);
        Member guestMember = MemberFixture.createNativeTypeMemberWithId(2L, "guestMember@email.com", defaultImage);

        TravelSchedule schedule1 = TravelScheduleFixture.createTravelSchedule("당일 여행");
        TravelAttendeeFixture.createAuthorTravelAttendee(schedule1, authorMember);
        TravelAttendeeFixture.createGuestTravelAttendee(schedule1, guestMember, AttendeePermission.READ);
        TravelRouteFixture.createTravelRoute(schedule1, placeWithThumbnail1, 1);
        TravelRouteFixture.createTravelRoute(schedule1, placeWithoutThumbnail, 2);
        TravelRouteFixture.createTravelRoute(schedule1, placeWithThumbnail2, 3);

        TravelSchedule schedule2 = TravelScheduleFixture.createTravelSchedule("도보 여행");
        TravelAttendeeFixture.createAuthorTravelAttendee(schedule2, guestMember);
        TravelAttendeeFixture.createGuestTravelAttendee(schedule2, authorMember, AttendeePermission.CHAT);

        TravelSchedule schedule3 = TravelScheduleFixture.createTravelSchedule("역사 여행");
        TravelAttendeeFixture.createAuthorTravelAttendee(schedule3, authorMember);


        List<TravelSchedule> travelSchedules = List.of(schedule1);
        Page<TravelSchedule> schedulePage = PageUtils.createPage(travelSchedules, PageUtils.schedulePageable(1), travelSchedules.size());

        // when
        List<ScheduleInfoResponse> response = travelScheduleService.createScheduleInfoResponse(schedulePage, authorMember.getMemberId());

        // then
        assertThat(response).hasSize(1);
        assertThat(response.get(0).getScheduleName()).isEqualTo(schedule1.getScheduleName());
        assertThat(response.get(0).getSinceUpdate()).isNotNull();
        assertThat(response.get(0).getThumbnailUrl()).isNotNull();
        assertThat(response.get(0).getAuthor().getNickname()).isEqualTo(authorMember.getNickname());
    }

    @Test
    @DisplayName("TravelSchedule 를 ScheduleInfoResponse 로 변경 시 썸네일 없는 경우")
    void createScheduleInfoResponse_noImageThumbnail(){
        // given
        Member authorMember = MemberFixture.createNativeTypeMemberWithId(1L, "authorMember@email.com", defaultImage);
        Member guestMember = MemberFixture.createNativeTypeMemberWithId(2L, "guestMember@email.com", defaultImage);

        TravelSchedule schedule1 = TravelScheduleFixture.createTravelSchedule("당일 여행");
        TravelAttendeeFixture.createAuthorTravelAttendee(schedule1, authorMember);
        TravelAttendeeFixture.createGuestTravelAttendee(schedule1, guestMember, AttendeePermission.READ);
        TravelRouteFixture.createTravelRoute(schedule1, placeWithoutThumbnail, 1);
        TravelRouteFixture.createTravelRoute(schedule1, placeWithoutThumbnail, 2);
        TravelRouteFixture.createTravelRoute(schedule1, placeWithoutThumbnail, 3);

        TravelSchedule schedule2 = TravelScheduleFixture.createTravelSchedule("도보 여행");
        TravelAttendeeFixture.createAuthorTravelAttendee(schedule2, guestMember);
        TravelAttendeeFixture.createGuestTravelAttendee(schedule2, authorMember, AttendeePermission.CHAT);

        TravelSchedule schedule3 = TravelScheduleFixture.createTravelSchedule("역사 여행");
        TravelAttendeeFixture.createAuthorTravelAttendee(schedule3, authorMember);

        List<TravelSchedule> travelSchedules = List.of(schedule1, schedule2, schedule3);
        Page<TravelSchedule> schedulePage = PageUtils.createPage(travelSchedules, PageUtils.schedulePageable(1), travelSchedules.size());


        // when
        List<ScheduleInfoResponse> response = travelScheduleService.createScheduleInfoResponse(schedulePage, authorMember.getMemberId());

        // then
        assertThat(response).hasSize(3);

        ScheduleInfoResponse first = response.get(0);
        ScheduleInfoResponse second = response.get(1);
        ScheduleInfoResponse third = response.get(2);

        assertAll(
                () -> assertThat(first.getScheduleName()).isEqualTo(schedule1.getScheduleName()),
                () -> assertThat(first.getSinceUpdate()).isNotNull(),
                () -> assertThat(first.getThumbnailUrl()).isNull(),
                () -> assertThat(first.getAuthor().getNickname()).isEqualTo(authorMember.getNickname()),
                () -> assertThat(first.getRole()).isEqualTo(AttendeeRole.AUTHOR)
        );
        assertAll(
                () -> assertThat(second.getScheduleName()).isEqualTo(schedule2.getScheduleName()),
                () -> assertThat(second.getThumbnailUrl()).isNull(),
                () -> assertThat(second.getAuthor().getNickname()).isEqualTo(guestMember.getNickname()),
                () -> assertThat(second.getRole()).isEqualTo(AttendeeRole.GUEST)
        );
        assertAll(
                () -> assertThat(third.getScheduleName()).isEqualTo(schedule3.getScheduleName()),
                () -> assertThat(third.getThumbnailUrl()).isNull(),
                () -> assertThat(third.getAuthor().getNickname()).isEqualTo(authorMember.getNickname()),
                () -> assertThat(third.getRole()).isEqualTo(AttendeeRole.AUTHOR)
        );
    }

    @Test
    @DisplayName("TravelSchedule 를 ScheduleInfoResponse 로 변경 시 작성자가 없어 예외 발생")
    void createScheduleInfoResponse_authorNotFound(){
        // given
        Member guestMember = MemberFixture.createNativeTypeMemberWithId(1L, "guestMember@email.com", defaultImage);

        TravelSchedule schedule = TravelScheduleFixture.createTravelSchedule("당일 여행");
        TravelAttendeeFixture.createGuestTravelAttendee(schedule, guestMember, AttendeePermission.ALL);

        TravelRouteFixture.createTravelRoute(schedule, placeWithThumbnail1, 1);
        TravelRouteFixture.createTravelRoute(schedule, placeWithoutThumbnail, 2);
        TravelRouteFixture.createTravelRoute(schedule, placeWithThumbnail2, 3);

        List<TravelSchedule> travelSchedules = List.of(schedule);
        Page<TravelSchedule> schedulePage = PageUtils.createPage(travelSchedules, PageUtils.schedulePageable(1), travelSchedules.size());


        // when
        DataNotFoundException fail = assertThrows(DataNotFoundException.class,
                () -> travelScheduleService.createScheduleInfoResponse(schedulePage, guestMember.getMemberId()));

        // then
        assertThat(fail.getErrorCode()).isEqualTo(ErrorCode.AUTHOR_NOT_FOUND);

    }

    @Test
    @DisplayName("TravelSchedule 를 ScheduleInfoResponse 로 변경 시 접근 권한이 없어 예외 발생")
    void createScheduleInfoResponse_forbiddenSchedule(){
        // given
        TravelSchedule schedule1 = TravelScheduleFixture.createTravelSchedule("당일 여행");
        TravelAttendeeFixture.createAuthorTravelAttendee(schedule1, member1);
        TravelAttendeeFixture.createGuestTravelAttendee(schedule1, member2, AttendeePermission.READ);
        TravelRouteFixture.createTravelRoute(schedule1, placeWithThumbnail1, 1);
        TravelRouteFixture.createTravelRoute(schedule1, placeWithoutThumbnail, 2);
        TravelRouteFixture.createTravelRoute(schedule1, placeWithThumbnail2, 3);

        TravelSchedule schedule2 = TravelScheduleFixture.createTravelSchedule("도보 여행");
        TravelAttendeeFixture.createAuthorTravelAttendee(schedule2, member2);
        TravelAttendeeFixture.createGuestTravelAttendee(schedule2, member1, AttendeePermission.CHAT);

        TravelSchedule schedule3 = TravelScheduleFixture.createTravelSchedule("역사 여행");
        TravelAttendeeFixture.createAuthorTravelAttendee(schedule3, member1);


        List<TravelSchedule> travelSchedules = List.of(schedule3);
        Page<TravelSchedule> schedulePage = PageUtils.createPage(travelSchedules, PageUtils.schedulePageable(1), travelSchedules.size());

        // when
        ForbiddenScheduleException fail = assertThrows(ForbiddenScheduleException.class,
                () -> travelScheduleService.createScheduleInfoResponse(schedulePage, 2L));

        // then
        assertThat(fail.getErrorCode()).isEqualTo(ErrorCode.FORBIDDEN_ACCESS_SCHEDULE);

    }

    @Test
    @DisplayName("작성자 조회해서 MemberProfileDTO 생성")
    void createAuthorDTO(){
        // given
        TravelSchedule schedule = TravelScheduleFixture.createTravelSchedule("당일 여행");

        TravelAttendeeFixture.createAuthorTravelAttendee(schedule, member1);
        TravelAttendeeFixture.createGuestTravelAttendee(schedule, member2, AttendeePermission.READ);

        TravelRouteFixture.createTravelRoute(schedule, placeWithThumbnail1, 1);
        TravelRouteFixture.createTravelRoute(schedule, placeWithoutThumbnail, 2);
        TravelRouteFixture.createTravelRoute(schedule, placeWithThumbnail2, 3);

        // when
        AuthorDTO response = travelScheduleService.createAuthorDTO(schedule);

        // then
        assertThat(response.getNickname()).isEqualTo(member1.getNickname());
        assertThat(response.getProfileUrl()).isEqualTo(member1.getProfileImage().getS3ObjectUrl());

    }

    @Test
    @DisplayName("작성자 조회해서 MemberProfileDTO 생성 시 작성자가 없어 예외 발생")
    void createAuthorDTO_authorNotFound(){
        // given
        TravelSchedule schedule = TravelScheduleFixture.createTravelSchedule("당일 여행");

        TravelAttendeeFixture.createGuestTravelAttendee(schedule, member2, AttendeePermission.READ);

        TravelRouteFixture.createTravelRoute(schedule, placeWithThumbnail1, 1);
        TravelRouteFixture.createTravelRoute(schedule, placeWithoutThumbnail, 2);
        TravelRouteFixture.createTravelRoute(schedule, placeWithThumbnail2, 3);


        // when
        DataNotFoundException fail = assertThrows(DataNotFoundException.class,
                () -> travelScheduleService.createAuthorDTO(schedule));

        // then
        assertThat(fail.getErrorCode()).isEqualTo(ErrorCode.AUTHOR_NOT_FOUND);

    }



    @Test
    @DisplayName("일정 생성")
    void createSchedule(){
        // given
        TravelSchedule schedule = TravelScheduleFixture.createTravelSchedule("당일 여행");
        TravelAttendeeFixture.createAuthorTravelAttendee(schedule, member1);

        ScheduleCreateRequest request = TravelScheduleFixture.createScheduleRequest(
                "테스트",
                LocalDate.now(),
                LocalDate.now().plusDays(1)
        );

        when(memberRepository.findById(anyLong())).thenReturn(Optional.of(member1));
        when(travelScheduleRepository.save(any())).thenReturn(schedule);

        // when
        ScheduleCreateResponse response = travelScheduleService.createSchedule(request, 1L);

        // then
        verify(memberRepository, times(1)).findById(anyLong());
        verify(travelScheduleRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("일정 생성 시 저장된 회원 정보 없어 예외 발생")
    void createSchedule_memberNotFound(){
        // given
        ScheduleCreateRequest request = TravelScheduleFixture.createScheduleRequest(
                "테스트",
                LocalDate.now(),
                LocalDate.now().plusDays(1)
        );

        when(memberRepository.findById(anyLong())).thenReturn(Optional.empty());

        // when
        DataNotFoundException fail = assertThrows(DataNotFoundException.class,
                () -> travelScheduleService.createSchedule(request, 1000L));

        // then
        assertThat(fail.getErrorCode()).isEqualTo(ErrorCode.MEMBER_NOT_FOUND);

    }

    @Test
    @DisplayName("일정 상세 조회")
    void getScheduleDetail(){
        // given
        TravelSchedule schedule = TravelScheduleFixture.createTravelSchedule("당일 여행");

        TravelAttendeeFixture.createAuthorTravelAttendee(schedule, member1);
        TravelAttendeeFixture.createGuestTravelAttendee(schedule, member2, AttendeePermission.READ);

        TravelRouteFixture.createTravelRoute(schedule, placeWithThumbnail1, 1);
        TravelRouteFixture.createTravelRoute(schedule, placeWithoutThumbnail, 2);
        TravelRouteFixture.createTravelRoute(schedule, placeWithThumbnail2, 3);

        List<PlaceResponse> places = List.of(PlaceResponse.from(placeWithThumbnail1), PlaceResponse.from(placeWithoutThumbnail));
        Pageable pageable = PageUtils.defaultPageable(1);

        when(travelScheduleRepository.findById(any())).thenReturn(Optional.of(schedule));
        when(travelPlaceRepository.findDefaultTravelPlacesByJungGu(any()))
                .thenReturn(PageUtils.createPage(places, pageable, 1));

        // when
        ScheduleDetailResponse response = travelScheduleService.getScheduleDetail(1L, 1);

        // then
        assertThat(response.getScheduleName()).isEqualTo(schedule.getScheduleName());
        assertThat(response.getCreatedAt()).isEqualTo(schedule.getCreatedAt());
        assertThat(response.getPlaceList().getTotalElements()).isEqualTo(places.size());
        assertThat(response.getPlaceList().getContent().get(0).getPlaceName()).isEqualTo(places.get(0).getPlaceName());
        assertThat(response.getPlaceList().getContent().get(0).getThumbnailUrl()).isEqualTo(placeWithThumbnail2.getThumbnailUrl());
    }

    @Test
    @DisplayName("일정 상세 조회 시 여행지 데이터 없는 경우")
    void getScheduleDetail_emptyResult(){
        // given
        TravelSchedule schedule = TravelScheduleFixture.createTravelSchedule( "당일 여행");

        TravelAttendeeFixture.createAuthorTravelAttendee(schedule, member1);
        TravelAttendeeFixture.createGuestTravelAttendee(schedule, member2, AttendeePermission.READ);

        TravelRouteFixture.createTravelRoute(schedule, placeWithThumbnail1, 1);
        TravelRouteFixture.createTravelRoute(schedule, placeWithoutThumbnail, 2);
        TravelRouteFixture.createTravelRoute(schedule, placeWithThumbnail2, 3);

        Pageable pageable = PageUtils.defaultPageable(1);

        when(travelScheduleRepository.findById(any())).thenReturn(Optional.of(schedule));
        when(travelPlaceRepository.findDefaultTravelPlacesByJungGu(any()))
                .thenReturn(PageUtils.createPage(Collections.emptyList(), pageable, 0));

        // when
        ScheduleDetailResponse response = travelScheduleService.getScheduleDetail(1L, 1);

        // then
        assertThat(response.getScheduleName()).isEqualTo(schedule.getScheduleName());
        assertThat(response.getCreatedAt()).isEqualTo(schedule.getCreatedAt());
        assertThat(response.getPlaceList().getTotalElements()).isEqualTo(0);
        assertThat(response.getPlaceList().getContent()).isEmpty();
    }

    @Test
    @DisplayName("일정 상세 조회 시 일정을 찾을 수 없어 예외 발생")
    void getScheduleDetail_scheduleNotFound(){
        // given
        when(travelScheduleRepository.findById(any())).thenReturn(Optional.empty());

        // when
        DataNotFoundException fail = assertThrows(DataNotFoundException.class,
                () -> travelScheduleService.getScheduleDetail(0L, 1));

        // then
        assertThat(fail.getErrorCode()).isEqualTo(ErrorCode.SCHEDULE_NOT_FOUND);
    }

    @Test
    @DisplayName("일정 수정")
    void updateSchedule(){
        // given
        Member authorMember = MemberFixture.createNativeTypeMemberWithId(1L, "authorMember@email.com", defaultImage);
        Member guestMember = MemberFixture.createNativeTypeMemberWithId(2L, "guestMember@email.com", defaultImage);

        TravelSchedule schedule = TravelScheduleFixture.createTravelScheduleWithId(1L, "당일 여행");

        TravelAttendeeFixture.createAuthorTravelAttendee(schedule, authorMember);
        TravelAttendeeFixture.createGuestTravelAttendee(schedule, guestMember, AttendeePermission.READ);

        TravelRouteFixture.createTravelRoute(schedule, placeWithThumbnail1, 1);
        TravelRouteFixture.createTravelRoute(schedule, placeWithoutThumbnail, 2);
        TravelRouteFixture.createTravelRoute(schedule, placeWithThumbnail2, 3);

        RouteRequest routeRequest1 = TravelRouteFixture.createRouteRequest(1, placeWithThumbnail2.getPlaceId());
        RouteRequest routeRequest2 = TravelRouteFixture.createRouteRequest(2, placeWithoutThumbnail.getPlaceId());

        ScheduleUpdateRequest request = TravelScheduleFixture.createUpdateScheduleRequest(
                "수정 테스트",
                LocalDate.now(),
                LocalDate.now().plusDays(5),
                routeRequest1,
                routeRequest2
        );

        when(travelScheduleRepository.findById(anyLong())).thenReturn(Optional.of(schedule));


        // when
        assertDoesNotThrow(
                () -> travelScheduleService.updateSchedule(request, authorMember.getMemberId(), schedule.getScheduleId()));

        // then
        assertThat(schedule.getScheduleName()).isEqualTo(request.getScheduleName());
        assertThat(schedule.getStartDate()).isEqualTo(request.getStartDate());
        assertThat(schedule.getEndDate()).isEqualTo(request.getEndDate());
        verify(travelRouteService).updateTravelRouteInSchedule(eq(schedule), eq(request.getTravelRoutes()));

    }

    @Test
    @DisplayName("일정 수정 시 요청에 여행루트 없는 경우")
    void updateSchedule_emptyRequestRoutes(){
        Member authorMember = MemberFixture.createNativeTypeMemberWithId(1L, "authorMember@email.com", defaultImage);
        Member guestMember = MemberFixture.createNativeTypeMemberWithId(2L, "guestMember@email.com", defaultImage);

        TravelSchedule schedule = TravelScheduleFixture.createTravelScheduleWithId(1L, "당일 여행");

        TravelAttendeeFixture.createAuthorTravelAttendee(schedule, authorMember);
        TravelAttendeeFixture.createGuestTravelAttendee(schedule, guestMember, AttendeePermission.READ);

        TravelRouteFixture.createTravelRoute(schedule, placeWithThumbnail1, 1);
        TravelRouteFixture.createTravelRoute(schedule, placeWithoutThumbnail, 2);
        TravelRouteFixture.createTravelRoute(schedule, placeWithThumbnail2, 3);

        ScheduleUpdateRequest request = TravelScheduleFixture.createUpdateScheduleRequest(
                "수정 테스트",
                LocalDate.now(),
                LocalDate.now().plusDays(5)
        );

        when(travelScheduleRepository.findById(anyLong())).thenReturn(Optional.of(schedule));

        // when
        assertDoesNotThrow(
                () -> travelScheduleService.updateSchedule(request, authorMember.getMemberId(), schedule.getScheduleId()));

        // then
        assertThat(schedule.getScheduleName()).isEqualTo(request.getScheduleName());
        assertThat(schedule.getStartDate()).isEqualTo(request.getStartDate());
        assertThat(schedule.getEndDate()).isEqualTo(request.getEndDate());
        verify(travelRouteService).updateTravelRouteInSchedule(eq(schedule), eq(request.getTravelRoutes()));
    }

    @Test
    @DisplayName("일정 수정 중 여행 루트 삭제에서 기존에 저장된 여행 루트가 없을 경우")
    void updateSchedule_noSavedTravelRouteList(){
        // given
        Member authorMember = MemberFixture.createNativeTypeMemberWithId(1L, "authorMember@email.com", defaultImage);
        Member guestMember = MemberFixture.createNativeTypeMemberWithId(2L, "guestMember@email.com", defaultImage);

        TravelSchedule schedule = TravelScheduleFixture.createTravelScheduleWithId(1L, "당일 여행");

        TravelAttendeeFixture.createAuthorTravelAttendee(schedule, authorMember);
        TravelAttendeeFixture.createGuestTravelAttendee(schedule, guestMember, AttendeePermission.READ);

        RouteRequest routeRequest1 = TravelRouteFixture.createRouteRequest(1, placeWithThumbnail1.getPlaceId());
        RouteRequest routeRequest2 = TravelRouteFixture.createRouteRequest(2, placeWithThumbnail2.getPlaceId());

        ScheduleUpdateRequest request = TravelScheduleFixture.createUpdateScheduleRequest(
                "수정 테스트",
                LocalDate.now(),
                LocalDate.now().plusDays(5),
                routeRequest1,
                routeRequest2
        );

        when(travelScheduleRepository.findById(anyLong())).thenReturn(Optional.of(schedule));

        // when
        assertDoesNotThrow(
                () -> travelScheduleService.updateSchedule(request, authorMember.getMemberId(), schedule.getScheduleId()));

        // then
        assertThat(schedule.getScheduleName()).isEqualTo(request.getScheduleName());
        assertThat(schedule.getStartDate()).isEqualTo(request.getStartDate());
        verify(travelRouteService).updateTravelRouteInSchedule(eq(schedule), eq(request.getTravelRoutes()));
    }


    @Test
    @DisplayName("일정 수정 시 일정 데이터 없어 예외 발생")
    void updateSchedule_scheduleNotFound(){
        // given
        RouteRequest routeRequest1 = TravelRouteFixture.createRouteRequest(1, placeWithThumbnail1.getPlaceId());
        RouteRequest routeRequest2 = TravelRouteFixture.createRouteRequest(2, placeWithThumbnail2.getPlaceId());

        ScheduleUpdateRequest request = TravelScheduleFixture.createUpdateScheduleRequest(
                "수정 테스트",
                LocalDate.now(),
                LocalDate.now().plusDays(5),
                routeRequest1,
                routeRequest2
        );

        when(travelScheduleRepository.findById(anyLong())).thenReturn(Optional.empty());

        // when
        DataNotFoundException fail = assertThrows(DataNotFoundException.class,
                () -> travelScheduleService.updateSchedule(request, 1L, 1000L));

        // then
        assertThat(fail.getErrorCode()).isEqualTo(ErrorCode.SCHEDULE_NOT_FOUND);

    }

    @Test
    @DisplayName("일정 수정 시 요청 회원에게 접근 권한이 없어 예외 발생")
    void updateSchedule_forbiddenSchedule(){
        // given
        TravelSchedule schedule = TravelScheduleFixture.createTravelScheduleWithId(1L, "당일 여행");

        TravelAttendeeFixture.createAuthorTravelAttendee(schedule, member1);

        TravelRouteFixture.createTravelRoute(schedule, placeWithThumbnail1, 1);
        TravelRouteFixture.createTravelRoute(schedule, placeWithoutThumbnail, 2);
        TravelRouteFixture.createTravelRoute(schedule, placeWithThumbnail2, 3);

        RouteRequest routeRequest1 = TravelRouteFixture.createRouteRequest(1, placeWithThumbnail1.getPlaceId());
        RouteRequest routeRequest2 = TravelRouteFixture.createRouteRequest(2, placeWithThumbnail2.getPlaceId());

        ScheduleUpdateRequest request = TravelScheduleFixture.createUpdateScheduleRequest(
                "수정 테스트",
                LocalDate.now(),
                LocalDate.now().plusDays(5),
                routeRequest1,
                routeRequest2
        );


        when(travelScheduleRepository.findById(anyLong())).thenReturn(Optional.of(schedule));

        // when
        ForbiddenScheduleException fail = assertThrows(ForbiddenScheduleException.class,
                () -> travelScheduleService.updateSchedule(request, 2L, 1L));

        // then
        assertThat(fail.getErrorCode()).isEqualTo(ErrorCode.FORBIDDEN_ACCESS_SCHEDULE);

    }

    @Test
    @DisplayName("일정 수정 시 요청 회원에게 수정 권한이 없어 예외 발생")
    void updateSchedule_forbiddenEdit(){
        // given
        Member authorMember = MemberFixture.createNativeTypeMemberWithId(1L, "authorMember@email.com", defaultImage);
        Member guestMember = MemberFixture.createNativeTypeMemberWithId(2L, "guestMember@email.com", defaultImage);

        TravelSchedule schedule = TravelScheduleFixture.createTravelScheduleWithId(1L, "당일 여행");

        TravelAttendeeFixture.createAuthorTravelAttendee(schedule, authorMember);
        TravelAttendeeFixture.createGuestTravelAttendee(schedule, guestMember, AttendeePermission.READ);

        TravelRouteFixture.createTravelRoute(schedule, placeWithThumbnail1, 1);
        TravelRouteFixture.createTravelRoute(schedule, placeWithoutThumbnail, 2);
        TravelRouteFixture.createTravelRoute(schedule, placeWithThumbnail2, 3);

        RouteRequest routeRequest1 = TravelRouteFixture.createRouteRequest(1, placeWithThumbnail1.getPlaceId());
        RouteRequest routeRequest2 = TravelRouteFixture.createRouteRequest(2, placeWithThumbnail2.getPlaceId());

        ScheduleUpdateRequest request = TravelScheduleFixture.createUpdateScheduleRequest(
                "수정 테스트",
                LocalDate.now(),
                LocalDate.now().plusDays(5),
                routeRequest1,
                routeRequest2
        );

        when(travelScheduleRepository.findById(anyLong())).thenReturn(Optional.of(schedule));

        // when
        ForbiddenScheduleException fail = assertThrows(ForbiddenScheduleException.class,
                () -> travelScheduleService.updateSchedule(request, guestMember.getMemberId(), schedule.getScheduleId()));

        // then
        assertThat(fail.getErrorCode()).isEqualTo(ErrorCode.FORBIDDEN_EDIT_SCHEDULE);
        assertThat(fail.getMessage()).isEqualTo(ErrorCode.FORBIDDEN_EDIT_SCHEDULE.getMessage());
    }


    @Test
    @DisplayName("참가자 정보 조회")
    void getAttendeeInfo(){
        // given
        Member authorMember = MemberFixture.createNativeTypeMemberWithId(1L, "authorMember@email.com", defaultImage);
        Member guestMember = MemberFixture.createNativeTypeMemberWithId(2L, "guestMember@email.com", defaultImage);

        TravelSchedule schedule = TravelScheduleFixture.createTravelSchedule("당일 여행");
        TravelAttendeeFixture.createAuthorTravelAttendee(schedule, authorMember);
        TravelAttendeeFixture.createGuestTravelAttendee(schedule, guestMember, AttendeePermission.READ);

        TravelRouteFixture.createTravelRoute(schedule, placeWithThumbnail1, 1);
        TravelRouteFixture.createTravelRoute(schedule, placeWithoutThumbnail, 2);
        TravelRouteFixture.createTravelRoute(schedule, placeWithThumbnail2, 3);

        // when
        TravelAttendee response = travelScheduleService.getAttendeeInfo(schedule, authorMember.getMemberId());

        // then
        assertThat(response.getMember().getEmail()).isEqualTo(authorMember.getEmail());
        assertThat(response.getTravelSchedule().getScheduleName()).isEqualTo(schedule.getScheduleName());
    }

    @Test
    @DisplayName("참가자 정보 조회 시 데이터 존재하지 않는 경우")
    void getAttendeeInfo_emptyResult(){
        // given
        TravelSchedule schedule = TravelScheduleFixture.createTravelSchedule("역사 여행");

        TravelRouteFixture.createTravelRoute(schedule, placeWithThumbnail1, 1);
        TravelRouteFixture.createTravelRoute(schedule, placeWithoutThumbnail, 2);
        TravelRouteFixture.createTravelRoute(schedule, placeWithThumbnail2, 3);

        // when
        ForbiddenScheduleException fail = assertThrows(ForbiddenScheduleException.class,
                () -> travelScheduleService.getAttendeeInfo(schedule, 1L));

        // then
        assertThat(fail.getErrorCode()).isEqualTo(ErrorCode.FORBIDDEN_ACCESS_SCHEDULE);

    }

    @Test
    @DisplayName("일정 수정 회원 권한 체크 ALL")
    void checkSchedule_editPermissionALL(){
        // given
        TravelSchedule schedule = TravelScheduleFixture.createTravelSchedule("당일 여행");

        TravelAttendee author = TravelAttendeeFixture.createAuthorTravelAttendee(schedule, member1);
        TravelAttendeeFixture.createGuestTravelAttendee(schedule, member2, AttendeePermission.READ);

        TravelRouteFixture.createTravelRoute(schedule, placeWithThumbnail1, 1);
        TravelRouteFixture.createTravelRoute(schedule, placeWithoutThumbnail, 2);
        TravelRouteFixture.createTravelRoute(schedule, placeWithThumbnail2, 3);


        // when, then
        assertDoesNotThrow(() -> travelScheduleService.checkScheduleEditPermission(author));
    }

    @Test
    @DisplayName("일정 수정 회원 권한 체크 EDIT")
    void checkSchedule_editPermissionEdit(){
        // given
        TravelSchedule schedule = TravelScheduleFixture.createTravelSchedule("도보 여행");

        TravelAttendeeFixture.createAuthorTravelAttendee(schedule, member1);
        TravelAttendee guest = TravelAttendeeFixture.createGuestTravelAttendee(schedule, member2, AttendeePermission.EDIT);

        TravelRouteFixture.createTravelRoute(schedule, placeWithThumbnail1, 1);
        TravelRouteFixture.createTravelRoute(schedule, placeWithoutThumbnail, 2);
        TravelRouteFixture.createTravelRoute(schedule, placeWithThumbnail2, 3);


        // when, then
        assertDoesNotThrow(() -> travelScheduleService.checkScheduleEditPermission(guest));
    }

    @Test
    @DisplayName("일정 수정 회원 권한 체크 중 CHAT 권한으로 예외 발생")
    void checkSchedule_editPermissionCHAT_forbiddenSchedule(){
        // given
        TravelSchedule schedule = TravelScheduleFixture.createTravelSchedule("도보 여행");

        TravelAttendeeFixture.createAuthorTravelAttendee(schedule, member1);
        TravelAttendee guest = TravelAttendeeFixture.createGuestTravelAttendee(schedule, member2, AttendeePermission.CHAT);

        TravelRouteFixture.createTravelRoute(schedule, placeWithThumbnail1, 1);
        TravelRouteFixture.createTravelRoute(schedule, placeWithoutThumbnail, 2);
        TravelRouteFixture.createTravelRoute(schedule, placeWithThumbnail2, 3);

        // when
        ForbiddenScheduleException fail = assertThrows(ForbiddenScheduleException.class,
                () -> travelScheduleService.checkScheduleEditPermission(guest));

        // then
        assertThat(fail.getErrorCode()).isEqualTo(ErrorCode.FORBIDDEN_EDIT_SCHEDULE);
    }

    @Test
    @DisplayName("일정 수정 회원 권한 체크 시 READ 권한으로 예외 발생")
    void checkSchedule_editPermissionREAD_forbiddenSchedule(){
        // given
        TravelSchedule schedule = TravelScheduleFixture.createTravelSchedule("당일 여행");

        TravelAttendeeFixture.createAuthorTravelAttendee(schedule, member1);
        TravelAttendee guest = TravelAttendeeFixture.createGuestTravelAttendee(schedule, member2, AttendeePermission.READ);

        TravelRouteFixture.createTravelRoute(schedule, placeWithThumbnail1, 1);
        TravelRouteFixture.createTravelRoute(schedule, placeWithoutThumbnail, 2);
        TravelRouteFixture.createTravelRoute(schedule, placeWithThumbnail2, 3);


        // when
        ForbiddenScheduleException fail = assertThrows(ForbiddenScheduleException.class,
                () -> travelScheduleService.checkScheduleEditPermission(guest));

        // then
        assertThat(fail.getErrorCode()).isEqualTo(ErrorCode.FORBIDDEN_EDIT_SCHEDULE);
    }

    @Test
    @DisplayName("일정 삭제")
    void deleteSchedule(){
        // given
        TravelSchedule schedule = TravelScheduleFixture.createTravelScheduleWithId(1L, "당일 여행");

        TravelAttendee author = TravelAttendeeFixture.createAuthorTravelAttendee(schedule, member1);
        TravelAttendeeFixture.createGuestTravelAttendee(schedule, member2, AttendeePermission.READ);

        TravelRouteFixture.createTravelRoute(schedule, placeWithThumbnail1, 1);
        TravelRouteFixture.createTravelRoute(schedule, placeWithoutThumbnail, 2);
        TravelRouteFixture.createTravelRoute(schedule, placeWithThumbnail2, 3);

        ChatMessage message1 = ChatMessageFixture.createChatMessage(1L, 1L, "hello1");
        ChatMessage message2 = ChatMessageFixture.createChatMessage(1L, 1L, "hello2");
        ChatMessage message3 = ChatMessageFixture.createChatMessage(1L, 2L, "hello3");
        List<ChatMessage> chatMessages = List.of(message1, message2, message3);

        when(travelAttendeeRepository.findByTravelSchedule_ScheduleIdAndMember_MemberId(anyLong(), anyLong()))
                .thenReturn(Optional.of(author));
        when(chatMessageRepository.findAllByScheduleId(anyLong())).thenReturn(chatMessages);

        // when
        assertDoesNotThrow(() -> travelScheduleService.deleteSchedule(1L, 1L));

        // then
        verify(chatMessageRepository, times(1)).deleteAllByScheduleId(1L);
    }

    @Test
    @DisplayName("일정 삭제 시 채팅 메시지 없는 경우")
    void deleteSchedule_noChatMessageData(){
        // given
        TravelSchedule schedule = TravelScheduleFixture.createTravelScheduleWithId(1L, "당일 여행");

        TravelAttendee author = TravelAttendeeFixture.createAuthorTravelAttendee(schedule, member1);
        TravelAttendeeFixture.createGuestTravelAttendee(schedule, member2, AttendeePermission.READ);

        TravelRouteFixture.createTravelRoute(schedule, placeWithThumbnail1, 1);
        TravelRouteFixture.createTravelRoute(schedule, placeWithoutThumbnail, 2);
        TravelRouteFixture.createTravelRoute(schedule, placeWithThumbnail2, 3);

        when(travelAttendeeRepository.findByTravelSchedule_ScheduleIdAndMember_MemberId(anyLong(), anyLong()))
                .thenReturn(Optional.of(author));
        when(chatMessageRepository.findAllByScheduleId(anyLong())).thenReturn(Collections.emptyList());

        // when
        assertDoesNotThrow(() -> travelScheduleService.deleteSchedule(1L, 1L));

        // then
        verify(chatMessageRepository, times(0)).deleteAllByScheduleId(1L);
    }

    @Test
    @DisplayName("일정 삭제 시 작성자가 아닌 회원의 삭제 요청으로 인해 예외 발생")
    void deleteSchedule_notAuthor_forbiddenSchedule(){
        // given
        TravelSchedule schedule = TravelScheduleFixture.createTravelScheduleWithId(1L, "당일 여행");

        TravelAttendeeFixture.createAuthorTravelAttendee(schedule, member1);
        TravelAttendee guest = TravelAttendeeFixture.createGuestTravelAttendee(schedule, member2, AttendeePermission.READ);

        TravelRouteFixture.createTravelRoute(schedule, placeWithThumbnail1, 1);
        TravelRouteFixture.createTravelRoute(schedule, placeWithoutThumbnail, 2);
        TravelRouteFixture.createTravelRoute(schedule, placeWithThumbnail2, 3);

        when(travelAttendeeRepository.findByTravelSchedule_ScheduleIdAndMember_MemberId(anyLong(), anyLong()))
                .thenReturn(Optional.of(guest));

        // when
        ForbiddenScheduleException fail = assertThrows(ForbiddenScheduleException.class,
                () -> travelScheduleService.deleteSchedule(1L, 2L));

        assertThat(fail.getErrorCode()).isEqualTo(ErrorCode.FORBIDDEN_DELETE_SCHEDULE);
    }


}
