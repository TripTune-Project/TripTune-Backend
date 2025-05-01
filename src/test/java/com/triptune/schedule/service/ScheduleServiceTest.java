package com.triptune.schedule.service;

import com.triptune.common.entity.ApiCategory;
import com.triptune.common.entity.City;
import com.triptune.common.entity.Country;
import com.triptune.common.entity.District;
import com.triptune.member.entity.Member;
import com.triptune.profile.entity.ProfileImage;
import com.triptune.member.repository.MemberRepository;
import com.triptune.schedule.ScheduleTest;
import com.triptune.schedule.dto.AuthorDTO;
import com.triptune.schedule.dto.request.ScheduleCreateRequest;
import com.triptune.schedule.dto.request.RouteRequest;
import com.triptune.schedule.dto.request.ScheduleUpdateRequest;
import com.triptune.schedule.dto.response.OverviewScheduleResponse;
import com.triptune.schedule.dto.response.ScheduleCreateResponse;
import com.triptune.schedule.dto.response.ScheduleDetailResponse;
import com.triptune.schedule.dto.response.ScheduleInfoResponse;
import com.triptune.schedule.entity.ChatMessage;
import com.triptune.schedule.entity.TravelAttendee;
import com.triptune.schedule.entity.TravelRoute;
import com.triptune.schedule.entity.TravelSchedule;
import com.triptune.schedule.enums.AttendeePermission;
import com.triptune.schedule.enums.AttendeeRole;
import com.triptune.schedule.exception.ForbiddenScheduleException;
import com.triptune.schedule.repository.ChatMessageRepository;
import com.triptune.schedule.repository.TravelAttendeeRepository;
import com.triptune.schedule.repository.TravelRouteRepository;
import com.triptune.schedule.repository.TravelScheduleRepository;
import com.triptune.travel.dto.response.PlaceResponse;
import com.triptune.travel.entity.TravelImage;
import com.triptune.travel.entity.TravelPlace;
import com.triptune.travel.repository.TravelPlaceRepository;
import com.triptune.global.response.enums.ErrorCode;
import com.triptune.global.exception.DataNotFoundException;
import com.triptune.global.response.pagination.SchedulePageResponse;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ScheduleServiceTest extends ScheduleTest {

    @InjectMocks private ScheduleService scheduleService;
    @Mock private TravelScheduleRepository travelScheduleRepository;
    @Mock private MemberRepository memberRepository;
    @Mock private TravelAttendeeRepository travelAttendeeRepository;
    @Mock private TravelPlaceRepository travelPlaceRepository;
    @Mock private TravelRouteRepository travelRouteRepository;
    @Mock private ChatMessageRepository chatMessageRepository;

    private Country country;
    private City city;
    private District district;
    private ApiCategory apiCategory;
    private TravelSchedule schedule1;
    private TravelSchedule schedule2;
    private TravelSchedule schedule3;
    private TravelPlace travelPlace1;
    private TravelPlace travelPlace2;
    private Member member1;
    private Member member2;
    private TravelAttendee attendee1;
    private TravelAttendee attendee2;

    @BeforeEach
    void setUp(){
        country = createCountry();
        city = createCity(country);
        district = createDistrict(city, "중구");
        apiCategory = createApiCategory();


        ProfileImage profileImage1 = createProfileImage(1L, "member1Image", member1);
        ProfileImage profileImage2 = createProfileImage(2L, "member2Image", member2);
        member1 = createMember(1L, "member1@email.com", profileImage1);
        member2 = createMember(2L, "member2@email.com", profileImage2);

        schedule1 = createTravelSchedule(1L, "테스트1");
        schedule2 = createTravelSchedule(2L, "테스트2");
        schedule3 = createTravelSchedule(3L, "테스트3");

        attendee1 = createTravelAttendee(1L, member1, schedule1, AttendeeRole.AUTHOR, AttendeePermission.ALL);
        attendee2 = createTravelAttendee(2L, member2, schedule1, AttendeeRole.GUEST, AttendeePermission.READ);
        TravelAttendee attendee3 = createTravelAttendee(3L, member1, schedule2, AttendeeRole.AUTHOR, AttendeePermission.ALL);
        TravelAttendee attendee4 = createTravelAttendee(4L, member2, schedule2, AttendeeRole.GUEST, AttendeePermission.CHAT);
        TravelAttendee attendee5 = createTravelAttendee(5L, member1, schedule3, AttendeeRole.AUTHOR, AttendeePermission.ALL);
        schedule1.setTravelAttendeeList(List.of(attendee1, attendee2));
        schedule2.setTravelAttendeeList(List.of(attendee3, attendee4));
        schedule3.setTravelAttendeeList(List.of(attendee5));

    }


    @Test
    @DisplayName("내 일정 목록 조회")
    void getAllSchedules(){
        // given
        Pageable pageable = PageUtils.schedulePageable(1);

        TravelImage travelImage1 = createTravelImage(travelPlace1, "test1", true);
        TravelImage travelImage2 = createTravelImage(travelPlace1, "test2", false);
        travelPlace1 = createTravelPlace(1L, country, city, district, apiCategory, List.of(travelImage1, travelImage2));

        TravelImage travelImage3 = createTravelImage(travelPlace2, "test1", true);
        TravelImage travelImage4 = createTravelImage(travelPlace2, "test2", false);
        travelPlace2 = createTravelPlace(2L, country, city, district, apiCategory, List.of(travelImage3, travelImage4));

        TravelRoute route1 = createTravelRoute(schedule1, travelPlace1, 1);
        TravelRoute route2 = createTravelRoute(schedule1, travelPlace1, 2);
        TravelRoute route3 = createTravelRoute(schedule1, travelPlace2, 3);
        schedule1.setTravelRouteList(List.of(route1, route2, route3));
        schedule2.setTravelRouteList(new ArrayList<>());

        List<TravelSchedule> schedules = List.of(schedule1, schedule2, schedule3);
        Page<TravelSchedule> schedulePage = PageUtils.createPage(schedules, pageable, schedules.size());

        when(travelScheduleRepository.findTravelSchedulesByMemberId(pageable, member1.getMemberId())).thenReturn(schedulePage);
        when(travelScheduleRepository.countSharedTravelSchedulesByMemberId(anyLong())).thenReturn(2);

        // when
        SchedulePageResponse<ScheduleInfoResponse> response = scheduleService.getAllSchedules(1, member1.getMemberId());

        // then
        List<ScheduleInfoResponse> content = response.getContent();
        assertThat(response.getTotalElements()).isEqualTo(3);
        assertThat(response.getTotalSharedElements()).isEqualTo(2);
        assertThat(content.get(0).getScheduleName()).isEqualTo(schedule1.getScheduleName());
        assertThat(content.get(0).getSinceUpdate()).isNotNull();
        assertThat(content.get(0).getThumbnailUrl()).isNotNull();
        assertThat(content.get(0).getAuthor().getNickname()).isEqualTo(member1.getNickname());
        assertThat(content.get(0).getRole()).isEqualTo(AttendeeRole.AUTHOR);
    }

    @Test
    @DisplayName("내 일정 목록 조회 시 공유된 일정이 없는 경우")
    void getAllSchedulesNotShared(){
        // given
        Pageable pageable = PageUtils.schedulePageable(1);

        List<TravelSchedule> schedules = List.of(schedule3);
        Page<TravelSchedule> schedulePage = PageUtils.createPage(schedules, pageable, schedules.size());

        when(travelScheduleRepository.findTravelSchedulesByMemberId(pageable, member1.getMemberId())).thenReturn(schedulePage);
        when(travelScheduleRepository.countSharedTravelSchedulesByMemberId(anyLong())).thenReturn(0);

        // when
        SchedulePageResponse<ScheduleInfoResponse> response = scheduleService.getAllSchedules(1, member1.getMemberId());

        // then
        List<ScheduleInfoResponse> content = response.getContent();
        assertThat(response.getTotalElements()).isEqualTo(1);
        assertThat(response.getTotalSharedElements()).isEqualTo(0);
        assertThat(content.get(0).getScheduleName()).isEqualTo(schedule3.getScheduleName());
        assertThat(content.get(0).getSinceUpdate()).isNotNull();
        assertThat(content.get(0).getAuthor().getNickname()).isEqualTo(member1.getNickname());
    }

    @Test
    @DisplayName("내 일정 목록 조회 시 일정 데이터 없는 경우")
    void getAllSchedulesNoScheduleData(){
        // given
        Pageable pageable = PageUtils.schedulePageable(1);
        Page<TravelSchedule> emptySchedulePage = PageUtils.createPage(new ArrayList<>(), pageable, 0);

        when(travelScheduleRepository.findTravelSchedulesByMemberId(pageable, member1.getMemberId())).thenReturn(emptySchedulePage);
        when(travelScheduleRepository.countSharedTravelSchedulesByMemberId(anyLong())).thenReturn(2);

        // when
        SchedulePageResponse<ScheduleInfoResponse> response = scheduleService.getAllSchedules(1, member1.getMemberId());

        // then
        assertThat(response.getTotalElements()).isEqualTo(0);
        assertThat(response.getTotalSharedElements()).isEqualTo(2);
        assertThat(response.getContent()).isEmpty();
        verify(travelRouteRepository, times(0)).findAllByTravelSchedule_ScheduleId(any(), any());
    }

    @Test
    @DisplayName("내 일정 목록 조회 시 이미지 썸네일 데이터 없는 경우")
    void getAllSchedulesNoImageThumbnail(){
        // given
        Pageable pageable = PageUtils.schedulePageable(1);

        travelPlace1 = createTravelPlace(1L, country, city, district, apiCategory, new ArrayList<>());
        travelPlace2 = createTravelPlace(2L, country, city, district, apiCategory, new ArrayList<>());

        List<TravelSchedule> schedules = List.of(schedule1, schedule2);
        Page<TravelSchedule> schedulePage = PageUtils.createPage(schedules, pageable, schedules.size());

        when(travelScheduleRepository.findTravelSchedulesByMemberId(pageable, member1.getMemberId())).thenReturn(schedulePage);
        when(travelScheduleRepository.countSharedTravelSchedulesByMemberId(anyLong())).thenReturn(1);

        // when
        SchedulePageResponse<ScheduleInfoResponse> response = scheduleService.getAllSchedules(1, member1.getMemberId());

        // then
        List<ScheduleInfoResponse> content = response.getContent();
        assertThat(response.getTotalElements()).isEqualTo(2);
        assertThat(response.getTotalSharedElements()).isEqualTo(1);
        assertThat(content.get(0).getScheduleName()).isEqualTo("테스트1");
        assertThat(content.get(0).getSinceUpdate()).isNotNull();
        assertThat(content.get(0).getThumbnailUrl()).isNull();

    }


    @Test
    @DisplayName("내 일정 목록 조회 시 이미지 데이터 없는 경우")
    void getAllSchedulesNoImageData(){
        // given
        Pageable pageable = PageUtils.schedulePageable(1);
        travelPlace1 = createTravelPlace(1L, country, city, district, apiCategory, new ArrayList<>());

        List<TravelSchedule> schedules = List.of(schedule1);
        Page<TravelSchedule> schedulePage = PageUtils.createPage(schedules, pageable, schedules.size());

        when(travelScheduleRepository.findTravelSchedulesByMemberId(pageable, member1.getMemberId())).thenReturn(schedulePage);
        when(travelScheduleRepository.countSharedTravelSchedulesByMemberId(anyLong())).thenReturn(1);

        // when
        SchedulePageResponse<ScheduleInfoResponse> response = scheduleService.getAllSchedules(1, member1.getMemberId());

        // then
        List<ScheduleInfoResponse> content = response.getContent();
        assertThat(response.getTotalElements()).isEqualTo(1);
        assertThat(response.getTotalSharedElements()).isEqualTo(1);
        assertThat(content.get(0).getScheduleName()).isEqualTo("테스트1");
        assertThat(content.get(0).getSinceUpdate()).isNotNull();
        assertThat(content.get(0).getThumbnailUrl()).isNull();

    }

    @Test
    @DisplayName("공유된 일정 목록 조회")
    void getSharedSchedules(){
        // given
        Pageable pageable = PageUtils.schedulePageable(1);

        TravelImage travelImage1 = createTravelImage(travelPlace1, "test1", true);
        TravelImage travelImage2 = createTravelImage(travelPlace1, "test2", false);
        travelPlace1 = createTravelPlace(1L, country, city, district, apiCategory, List.of(travelImage1, travelImage2));

        TravelImage travelImage3 = createTravelImage(travelPlace2, "test1", true);
        TravelImage travelImage4 = createTravelImage(travelPlace2, "test2", false);
        travelPlace2 = createTravelPlace(2L, country, city, district, apiCategory, List.of(travelImage3, travelImage4));

        TravelRoute route1 = createTravelRoute(schedule1, travelPlace1, 1);
        TravelRoute route2 = createTravelRoute(schedule1, travelPlace1, 2);
        TravelRoute route3 = createTravelRoute(schedule1, travelPlace2, 3);
        schedule1.setTravelRouteList(List.of(route1, route2, route3));
        schedule2.setTravelRouteList(new ArrayList<>());

        List<TravelSchedule> schedules = List.of(schedule1, schedule2);
        Page<TravelSchedule> schedulePage = PageUtils.createPage(schedules, pageable, schedules.size());

        when(travelScheduleRepository.findSharedTravelSchedulesByMemberId(pageable, member1.getMemberId())).thenReturn(schedulePage);
        when(travelScheduleRepository.countTravelSchedulesByMemberId(anyLong())).thenReturn(3);

        // when
        SchedulePageResponse<ScheduleInfoResponse> response = scheduleService.getSharedSchedules(1, member1.getMemberId());

        // then
        List<ScheduleInfoResponse> content = response.getContent();
        assertThat(response.getTotalElements()).isEqualTo(3);
        assertThat(response.getTotalSharedElements()).isEqualTo(2);
        assertThat(content.get(0).getScheduleName()).isEqualTo(schedule1.getScheduleName());
        assertThat(content.get(0).getSinceUpdate()).isNotNull();
        assertThat(content.get(0).getThumbnailUrl()).isNotNull();
        assertThat(content.get(0).getAuthor().getNickname()).isEqualTo(member1.getNickname());
        assertThat(content.get(0).getRole()).isEqualTo(AttendeeRole.AUTHOR);
    }


    @Test
    @DisplayName("공유된 일정 목록 조회 시 일정 데이터 없는 경우")
    void getSharedSchedulesNoScheduleData(){
        // given
        Pageable pageable = PageUtils.schedulePageable(1);
        Page<TravelSchedule> emptySchedulePage = PageUtils.createPage(new ArrayList<>(), pageable, 0);

        when(travelScheduleRepository.findSharedTravelSchedulesByMemberId(pageable, member1.getMemberId())).thenReturn(emptySchedulePage);
        when(travelScheduleRepository.countTravelSchedulesByMemberId(anyLong())).thenReturn(2);

        // when
        SchedulePageResponse<ScheduleInfoResponse> response = scheduleService.getSharedSchedules(1, member1.getMemberId());

        // then
        assertThat(response.getTotalElements()).isEqualTo(2);
        assertThat(response.getTotalSharedElements()).isEqualTo(0);
        assertThat(response.getContent()).isEmpty();
    }

    @Test
    @DisplayName("공유된 일정 목록 조회 시 이미지 썸네일 데이터 없는 경우")
    void getSharedSchedulesNoImageThumbnail(){
        // given
        Pageable pageable = PageUtils.schedulePageable(1);

        travelPlace1 = createTravelPlace(1L, country, city, district, apiCategory, new ArrayList<>());
        travelPlace2 = createTravelPlace(2L, country, city, district, apiCategory, new ArrayList<>());

        TravelRoute route1 = createTravelRoute(schedule1, travelPlace1, 1);
        TravelRoute route2 = createTravelRoute(schedule1, travelPlace1, 2);
        TravelRoute route3 = createTravelRoute(schedule1, travelPlace2, 3);
        schedule1.setTravelRouteList(List.of(route1, route2, route3));
        schedule2.setTravelRouteList(new ArrayList<>());

        List<TravelSchedule> schedules = List.of(schedule1, schedule2);
        Page<TravelSchedule> schedulePage = PageUtils.createPage(schedules, pageable, schedules.size());

        when(travelScheduleRepository.findSharedTravelSchedulesByMemberId(pageable, member1.getMemberId())).thenReturn(schedulePage);
        when(travelScheduleRepository.countTravelSchedulesByMemberId(anyLong())).thenReturn(3);

        // when
        SchedulePageResponse<ScheduleInfoResponse> response = scheduleService.getSharedSchedules(1, member1.getMemberId());

        // then
        List<ScheduleInfoResponse> content = response.getContent();
        assertThat(response.getTotalElements()).isEqualTo(3);
        assertThat(response.getTotalSharedElements()).isEqualTo(2);
        assertThat(content.get(0).getScheduleName()).isEqualTo("테스트1");
        assertThat(content.get(0).getSinceUpdate()).isNotNull();
        assertThat(content.get(0).getThumbnailUrl()).isNull();

    }


    @Test
    @DisplayName("내 일정 목록 조회 시 이미지 데이터 없는 경우")
    void getSharedSchedulesNoImageData(){
        // given
        Pageable pageable = PageUtils.schedulePageable(1);
        travelPlace1 = createTravelPlace(1L, country, city, district, apiCategory, new ArrayList<>());

        List<TravelSchedule> schedules = List.of(schedule1);
        Page<TravelSchedule> schedulePage = PageUtils.createPage(schedules, pageable, schedules.size());

        when(travelScheduleRepository.findSharedTravelSchedulesByMemberId(pageable, member1.getMemberId())).thenReturn(schedulePage);
        when(travelScheduleRepository.countTravelSchedulesByMemberId(anyLong())).thenReturn(1);

        // when
        SchedulePageResponse<ScheduleInfoResponse> response = scheduleService.getSharedSchedules(1, member1.getMemberId());

        // then
        List<ScheduleInfoResponse> content = response.getContent();
        assertThat(response.getTotalElements()).isEqualTo(1);
        assertThat(response.getTotalSharedElements()).isEqualTo(1);
        assertThat(content.get(0).getScheduleName()).isEqualTo("테스트1");
        assertThat(content.get(0).getSinceUpdate()).isNotNull();
        assertThat(content.get(0).getThumbnailUrl()).isNull();

    }

    @Test
    @DisplayName("수정 권한 있는 내 일정 목록 조회")
    void getEnableEditSchedule(){
        // given
        Pageable pageable = PageUtils.scheduleModalPageable(1);

        List<TravelSchedule> schedules = List.of(schedule1);
        Page<TravelSchedule> schedulePage = PageUtils.createPage(schedules, pageable, schedules.size());

        when(travelScheduleRepository.findEnableEditTravelSchedulesByMemberId(any(), anyLong())).thenReturn(schedulePage);
        when(travelAttendeeRepository.findAuthorNicknameByScheduleId(anyLong())).thenReturn(member1.getNickname());

        // when
        Page<OverviewScheduleResponse> response = scheduleService.getEnableEditSchedule(1, member1.getMemberId());

        // then
        assertThat(response.getTotalElements()).isNotZero();
        assertThat(response.getContent().get(0).getScheduleName()).isEqualTo(schedule1.getScheduleName());
        assertThat(response.getContent().get(0).getStartDate()).isEqualTo(schedule1.getStartDate());
        assertThat(response.getContent().get(0).getAuthor()).isEqualTo(member1.getNickname());
    }

    @Test
    @DisplayName("수정 권한 있는 내 일정 목록 조회 시 일정 데이터 존재하지 않는 경우")
    void getEnableEditSchedule_emptySchedules(){
        // given
        Pageable pageable = PageUtils.scheduleModalPageable(1);

        Page<TravelSchedule> schedulePage = PageUtils.createPage(new ArrayList<>(), pageable, 0);

        when(travelScheduleRepository.findEnableEditTravelSchedulesByMemberId(any(), anyLong())).thenReturn(schedulePage);

        // when
        Page<OverviewScheduleResponse> response = scheduleService.getEnableEditSchedule(1, member1.getMemberId());

        // then
        assertThat(response.getTotalElements()).isZero();
        assertThat(response.getContent()).isEmpty();
    }

    @Test
    @DisplayName("내 일정 검색")
    void searchAllSchedules(){
        // given
        String keyword = "테스트";
        Pageable pageable = PageUtils.schedulePageable(1);

        List<TravelSchedule> schedules = List.of(schedule1, schedule2, schedule3);
        Page<TravelSchedule> schedulePage = PageUtils.createPage(schedules, pageable, schedules.size());

        when(travelScheduleRepository.searchTravelSchedulesByMemberIdAndKeyword(pageable, keyword, member1.getMemberId())).thenReturn(schedulePage);
        when(travelScheduleRepository.countSharedTravelSchedulesByMemberIdAndKeyword(keyword, member1.getMemberId())).thenReturn(1);

        // when
        SchedulePageResponse<ScheduleInfoResponse> response = scheduleService.searchAllSchedules(1, keyword, member1.getMemberId());

        // then
        List<ScheduleInfoResponse> content = response.getContent();
        assertThat(response.getTotalElements()).isEqualTo(3);
        assertThat(response.getTotalSharedElements()).isEqualTo(1);
        assertThat(content.get(0).getScheduleName()).isNotNull();
        assertThat(content.get(0).getSinceUpdate()).isNotNull();
        assertThat(content.get(0).getAuthor().getNickname()).isEqualTo(member1.getNickname());
        assertThat(content.get(0).getRole()).isEqualTo(AttendeeRole.AUTHOR);
    }

    @Test
    @DisplayName("내 일정 검색 시 공유된 일정이 없는 경우")
    void searchAllSchedulesNotShared(){
        // given
        String keyword = "3";
        Pageable pageable = PageUtils.schedulePageable(1);

        List<TravelSchedule> schedules = List.of(schedule3);
        Page<TravelSchedule> schedulePage = PageUtils.createPage(schedules, pageable, schedules.size());

        when(travelScheduleRepository.searchTravelSchedulesByMemberIdAndKeyword(pageable, keyword, member1.getMemberId())).thenReturn(schedulePage);
        when(travelScheduleRepository.countSharedTravelSchedulesByMemberIdAndKeyword(keyword, member1.getMemberId())).thenReturn(0);

        // when
        SchedulePageResponse<ScheduleInfoResponse> response = scheduleService.searchAllSchedules(1, "3", member1.getMemberId());

        // then
        List<ScheduleInfoResponse> content = response.getContent();
        assertThat(response.getTotalElements()).isEqualTo(1);
        assertThat(response.getTotalSharedElements()).isEqualTo(0);
        assertThat(content.get(0).getScheduleName()).isEqualTo(schedule3.getScheduleName());
        assertThat(content.get(0).getSinceUpdate()).isNotNull();
        assertThat(content.get(0).getAuthor().getNickname()).isEqualTo(member1.getNickname());
    }

    @Test
    @DisplayName("내 일정 검색 시 검색 결과가 없는 경우")
    void searchAllSchedulesNoData(){
        // given
        String keyword = "ㅁㄴㅇㄹ";
        Pageable pageable = PageUtils.schedulePageable(1);
        Page<TravelSchedule> emptySchedulePage = PageUtils.createPage(new ArrayList<>(), pageable, 0);

        when(travelScheduleRepository.searchTravelSchedulesByMemberIdAndKeyword(pageable, keyword, member1.getMemberId())).thenReturn(emptySchedulePage);
        when(travelScheduleRepository.countSharedTravelSchedulesByMemberIdAndKeyword(keyword, member1.getMemberId())).thenReturn(2);

        // when
        SchedulePageResponse<ScheduleInfoResponse> response = scheduleService.searchAllSchedules(1, keyword, member1.getMemberId());

        // then
        assertThat(response.getTotalElements()).isEqualTo(0);
        assertThat(response.getTotalSharedElements()).isEqualTo(2);
        assertThat(response.getContent()).isEmpty();
    }

    @Test
    @DisplayName("내 일정 검색 시 이미지 썸네일 데이터 없는 경우")
    void searchAllSchedulesNoImageThumbnail(){
        // given
        String keyword = "테스트";
        Pageable pageable = PageUtils.schedulePageable(1);

        travelPlace1 = createTravelPlace(1L, country, city, district, apiCategory, new ArrayList<>());
        travelPlace2 = createTravelPlace(2L, country, city, district, apiCategory, new ArrayList<>());


        List<TravelSchedule> schedules = List.of(schedule1, schedule2);
        Page<TravelSchedule> schedulePage = PageUtils.createPage(schedules, pageable, schedules.size());

        when(travelScheduleRepository.searchTravelSchedulesByMemberIdAndKeyword(pageable, keyword, member1.getMemberId())).thenReturn(schedulePage);
        when(travelScheduleRepository.countSharedTravelSchedulesByMemberIdAndKeyword(keyword, member1.getMemberId())).thenReturn(1);

        // when
        SchedulePageResponse<ScheduleInfoResponse> response = scheduleService.searchAllSchedules(1, keyword, member1.getMemberId());

        // then
        List<ScheduleInfoResponse> content = response.getContent();
        assertThat(response.getTotalElements()).isEqualTo(2);
        assertThat(response.getTotalSharedElements()).isEqualTo(1);
        assertThat(content.get(0).getScheduleName()).isEqualTo("테스트1");
        assertThat(content.get(0).getSinceUpdate()).isNotNull();
        assertThat(content.get(0).getThumbnailUrl()).isNull();

    }


    @Test
    @DisplayName("내 일정 목록 조회 시 이미지 데이터 없는 경우")
    void searchAllSchedulesNoImageData(){
        // given
        String keyword = "1";
        Pageable pageable = PageUtils.schedulePageable(1);
        travelPlace1 = createTravelPlace(1L, country, city, district, apiCategory, new ArrayList<>());

        List<TravelSchedule> schedules = List.of(schedule1);
        Page<TravelSchedule> schedulePage = PageUtils.createPage(schedules, pageable, schedules.size());

        when(travelScheduleRepository.searchTravelSchedulesByMemberIdAndKeyword(pageable, keyword, member1.getMemberId())).thenReturn(schedulePage);
        when(travelScheduleRepository.countSharedTravelSchedulesByMemberIdAndKeyword(keyword, member1.getMemberId())).thenReturn(1);

        // when
        SchedulePageResponse<ScheduleInfoResponse> response = scheduleService.searchAllSchedules(1, keyword, member1.getMemberId());

        // then
        List<ScheduleInfoResponse> content = response.getContent();
        assertThat(response.getTotalElements()).isEqualTo(1);
        assertThat(response.getTotalSharedElements()).isEqualTo(1);
        assertThat(content.get(0).getScheduleName()).isEqualTo("테스트1");
        assertThat(content.get(0).getSinceUpdate()).isNotNull();
        assertThat(content.get(0).getThumbnailUrl()).isNull();

    }

    @Test
    @DisplayName("공유된 일정 검색")
    void searchSharedSchedules(){
        // given
        String keyword = "테스트";
        Pageable pageable = PageUtils.schedulePageable(1);


        TravelImage travelImage1 = createTravelImage(travelPlace1, "test1", true);
        TravelImage travelImage2 = createTravelImage(travelPlace1, "test2", false);
        travelPlace1 = createTravelPlace(1L, country, city, district, apiCategory, List.of(travelImage1, travelImage2));

        TravelImage travelImage3 = createTravelImage(travelPlace2, "test1", true);
        TravelImage travelImage4 = createTravelImage(travelPlace2, "test2", false);
        travelPlace2 = createTravelPlace(2L, country, city, district, apiCategory, List.of(travelImage3, travelImage4));

        TravelRoute route1 = createTravelRoute(schedule1, travelPlace1, 1);
        TravelRoute route2 = createTravelRoute(schedule1, travelPlace1, 2);
        TravelRoute route3 = createTravelRoute(schedule1, travelPlace2, 3);
        schedule1.setTravelRouteList(List.of(route1, route2, route3));
        schedule2.setTravelRouteList(new ArrayList<>());

        List<TravelSchedule> schedules = List.of(schedule1, schedule2, schedule3);
        Page<TravelSchedule> schedulePage = PageUtils.createPage(schedules, pageable, schedules.size());

        when(travelScheduleRepository.searchSharedTravelSchedulesByMemberIdAndKeyword(pageable, keyword, member1.getMemberId())).thenReturn(schedulePage);
        when(travelScheduleRepository.countTravelSchedulesByMemberIdAndKeyword(keyword, member1.getMemberId())).thenReturn(5);

        // when
        SchedulePageResponse<ScheduleInfoResponse> response = scheduleService.searchSharedSchedules(1, keyword, member1.getMemberId());

        // then
        List<ScheduleInfoResponse> content = response.getContent();
        assertThat(response.getTotalElements()).isEqualTo(5);
        assertThat(response.getTotalSharedElements()).isEqualTo(3);
        assertThat(content.get(0).getScheduleName()).isEqualTo(schedule1.getScheduleName());
        assertThat(content.get(0).getSinceUpdate()).isNotNull();
        assertThat(content.get(0).getThumbnailUrl()).isNotNull();
        assertThat(content.get(0).getAuthor().getNickname()).isEqualTo(member1.getNickname());
        assertThat(content.get(0).getRole()).isEqualTo(AttendeeRole.AUTHOR);
    }


    @Test
    @DisplayName("공유된 일정 목록 조회 시 일정 데이터 없는 경우")
    void searchSharedSchedulesNoData(){
        // given
        String keyword = "테스트";
        Pageable pageable = PageUtils.schedulePageable(1);
        Page<TravelSchedule> emptySchedulePage = PageUtils.createPage(new ArrayList<>(), pageable, 0);

        when(travelScheduleRepository.searchSharedTravelSchedulesByMemberIdAndKeyword(pageable, keyword, member1.getMemberId())).thenReturn(emptySchedulePage);
        when(travelScheduleRepository.countTravelSchedulesByMemberIdAndKeyword(keyword, member1.getMemberId())).thenReturn(2);

        // when
        SchedulePageResponse<ScheduleInfoResponse> response = scheduleService.searchSharedSchedules(1, keyword, member1.getMemberId());

        // then
        assertThat(response.getTotalElements()).isEqualTo(2);
        assertThat(response.getTotalSharedElements()).isEqualTo(0);
        assertThat(response.getContent()).isEmpty();
    }

    @Test
    @DisplayName("공유된 일정 목록 조회 시 이미지 썸네일 데이터 없는 경우")
    void searchSharedSchedulesNoImageThumbnail(){
        // given
        String keyword = "테스트";
        Pageable pageable = PageUtils.schedulePageable(1);

        travelPlace1 = createTravelPlace(1L, country, city, district, apiCategory, new ArrayList<>());
        travelPlace2 = createTravelPlace(2L, country, city, district, apiCategory, new ArrayList<>());

        TravelRoute route1 = createTravelRoute(schedule1, travelPlace1, 1);
        TravelRoute route2 = createTravelRoute(schedule1, travelPlace1, 2);
        TravelRoute route3 = createTravelRoute(schedule1, travelPlace2, 3);
        schedule1.setTravelRouteList(List.of(route1, route2, route3));
        schedule2.setTravelRouteList(new ArrayList<>());

        List<TravelSchedule> schedules = List.of(schedule1, schedule2);
        Page<TravelSchedule> schedulePage = PageUtils.createPage(schedules, pageable, schedules.size());

        when(travelScheduleRepository.searchSharedTravelSchedulesByMemberIdAndKeyword(pageable, keyword, member1.getMemberId())).thenReturn(schedulePage);
        when(travelScheduleRepository.countTravelSchedulesByMemberIdAndKeyword(keyword, member1.getMemberId())).thenReturn(3);

        // when
        SchedulePageResponse<ScheduleInfoResponse> response = scheduleService.searchSharedSchedules(1, keyword, member1.getMemberId());

        // then
        List<ScheduleInfoResponse> content = response.getContent();
        assertThat(response.getTotalElements()).isEqualTo(3);
        assertThat(response.getTotalSharedElements()).isEqualTo(2);
        assertThat(content.get(0).getScheduleName()).isEqualTo("테스트1");
        assertThat(content.get(0).getSinceUpdate()).isNotNull();
        assertThat(content.get(0).getThumbnailUrl()).isNull();

    }


    @Test
    @DisplayName("공유된 일정 검색 시 이미지 데이터 없는 경우")
    void searchSharedSchedulesNoImageData(){
        // given
        String keyword = "테스트";
        Pageable pageable = PageUtils.schedulePageable(1);
        travelPlace1 = createTravelPlace(1L, country, city, district, apiCategory, new ArrayList<>());

        List<TravelSchedule> schedules = List.of(schedule1, schedule2);
        Page<TravelSchedule> schedulePage = PageUtils.createPage(schedules, pageable, schedules.size());

        when(travelScheduleRepository.searchSharedTravelSchedulesByMemberIdAndKeyword(pageable, keyword, member1.getMemberId())).thenReturn(schedulePage);
        when(travelScheduleRepository.countTravelSchedulesByMemberIdAndKeyword(keyword, member1.getMemberId())).thenReturn(1);

        // when
        SchedulePageResponse<ScheduleInfoResponse> response = scheduleService.searchSharedSchedules(1, keyword, member1.getMemberId());

        // then
        List<ScheduleInfoResponse> content = response.getContent();
        assertThat(response.getTotalElements()).isEqualTo(1);
        assertThat(response.getTotalSharedElements()).isEqualTo(2);
        assertThat(content.get(0).getScheduleName()).isEqualTo("테스트1");
        assertThat(content.get(0).getSinceUpdate()).isNotNull();
        assertThat(content.get(0).getThumbnailUrl()).isNull();

    }

    @Test
    @DisplayName("TravelSchedule 를 ScheduleInfoResponse 로 변경")
    void createScheduleInfoResponse(){
        // given
        TravelImage travelImage1 = createTravelImage(travelPlace1, "test1", true);
        TravelImage travelImage2 = createTravelImage(travelPlace1, "test2", false);
        travelPlace1 = createTravelPlace(1L, country, city, district, apiCategory, List.of(travelImage1, travelImage2));

        TravelImage travelImage3 = createTravelImage(travelPlace2, "test1", true);
        TravelImage travelImage4 = createTravelImage(travelPlace2, "test2", false);
        travelPlace2 = createTravelPlace(2L, country, city, district, apiCategory, List.of(travelImage3, travelImage4));

        TravelRoute route1 = createTravelRoute(schedule1, travelPlace1, 1);
        TravelRoute route2 = createTravelRoute(schedule1, travelPlace1, 2);
        TravelRoute route3 = createTravelRoute(schedule1, travelPlace2, 3);
        schedule1.setTravelRouteList(List.of(route1, route2, route3));
        schedule2.setTravelRouteList(new ArrayList<>());

        List<TravelSchedule> travelScheduleList = List.of(schedule1);
        Page<TravelSchedule> schedulePage = PageUtils.createPage(travelScheduleList, PageUtils.schedulePageable(1), travelScheduleList.size());
        // when
        List<ScheduleInfoResponse> response = scheduleService.createScheduleInfoResponse(schedulePage, member1.getMemberId());

        // then
        assertThat(response.size()).isEqualTo(1);
        assertThat(response.get(0).getScheduleName()).isEqualTo(schedule1.getScheduleName());
        assertThat(response.get(0).getSinceUpdate()).isNotNull();
        assertThat(response.get(0).getThumbnailUrl()).isNotNull();
        assertThat(response.get(0).getAuthor().getNickname()).isEqualTo(member1.getNickname());
    }

    @Test
    @DisplayName("TravelSchedule 를 ScheduleInfoResponse 로 변경 시 썸네일 없는 경우")
    void createScheduleInfoResponseWithoutThumbnail(){
        // given
        travelPlace1 = createTravelPlace(1L, country, city, district, apiCategory, new ArrayList<>());
        travelPlace2 = createTravelPlace(2L, country, city, district, apiCategory, new ArrayList<>());

        List<TravelSchedule> travelScheduleList = List.of(schedule1);
        Page<TravelSchedule> schedulePage = PageUtils.createPage(travelScheduleList, PageUtils.schedulePageable(1), travelScheduleList.size());


        // when
        List<ScheduleInfoResponse> response = scheduleService.createScheduleInfoResponse(schedulePage, member1.getMemberId());

        // then
        assertThat(response.get(0).getScheduleName()).isEqualTo(schedule1.getScheduleName());
        assertThat(response.get(0).getSinceUpdate()).isNotNull();
        assertThat(response.get(0).getThumbnailUrl()).isNull();
        assertThat(response.get(0).getAuthor().getNickname()).isEqualTo(member1.getNickname());
    }

    @Test
    @DisplayName("TravelSchedule 를 ScheduleInfoResponse 로 변경 시 작성자가 없어 예외 발생")
    void createScheduleInfoResponse_notFoundException(){
        // given
        List<TravelSchedule> travelScheduleList = List.of(schedule1);
        Page<TravelSchedule> schedulePage = PageUtils.createPage(travelScheduleList, PageUtils.schedulePageable(1), travelScheduleList.size());
        for(TravelAttendee attendee : schedule1.getTravelAttendeeList()){
            attendee.updateRole(AttendeeRole.GUEST);
        }

        // when
        DataNotFoundException fail = assertThrows(DataNotFoundException.class,
                () -> scheduleService.createScheduleInfoResponse(schedulePage, member1.getMemberId()));

        // then
        assertThat(fail.getHttpStatus()).isEqualTo(ErrorCode.AUTHOR_NOT_FOUND.getStatus());
        assertThat(fail.getMessage()).isEqualTo(ErrorCode.AUTHOR_NOT_FOUND.getMessage());

    }

    @Test
    @DisplayName("TravelSchedule 를 ScheduleInfoResponse 로 변경 시 접근 권한이 없어 예외 발생")
    void createScheduleInfoResponse_forbiddenScheduleException(){
        // given
        List<TravelSchedule> travelScheduleList = List.of(schedule3);
        Page<TravelSchedule> schedulePage = PageUtils.createPage(travelScheduleList, PageUtils.schedulePageable(1), travelScheduleList.size());

        // when
        ForbiddenScheduleException fail = assertThrows(ForbiddenScheduleException.class,
                () -> scheduleService.createScheduleInfoResponse(schedulePage, member2.getMemberId()));

        // then
        assertThat(fail.getHttpStatus()).isEqualTo(ErrorCode.FORBIDDEN_ACCESS_SCHEDULE.getStatus());
        assertThat(fail.getMessage()).isEqualTo(ErrorCode.FORBIDDEN_ACCESS_SCHEDULE.getMessage());

    }

    @Test
    @DisplayName("작성자 조회해서 MemberProfileDTO 생성")
    void createAuthorDTO(){
        // given
        // when
        AuthorDTO response = scheduleService.createAuthorDTO(schedule1);

        // then
        assertThat(response.getNickname()).isEqualTo(member1.getNickname());
        assertThat(response.getProfileUrl()).isEqualTo(member1.getProfileImage().getS3ObjectUrl());

    }

    @Test
    @DisplayName("작성자 조회해서 MemberProfileDTO 생성 시 작성자가 없어 예외 발생")
    void createAuthorDTO_notFoundException(){
        // given
        for(TravelAttendee attendee : schedule1.getTravelAttendeeList()){
            attendee.updateRole(AttendeeRole.GUEST);
        }

        // when
        DataNotFoundException fail = assertThrows(DataNotFoundException.class, () -> scheduleService.createAuthorDTO(schedule1));

        // then
        assertThat(fail.getHttpStatus()).isEqualTo(ErrorCode.AUTHOR_NOT_FOUND.getStatus());
        assertThat(fail.getMessage()).isEqualTo(ErrorCode.AUTHOR_NOT_FOUND.getMessage());

    }



    @Test
    @DisplayName("일정 생성")
    void createSchedule(){
        // given
        ScheduleCreateRequest request = createScheduleRequest(LocalDate.now());

        when(travelScheduleRepository.save(any())).thenReturn(schedule1);
        when(memberRepository.findById(anyLong())).thenReturn(Optional.of(member1));

        // when
        ScheduleCreateResponse response = scheduleService.createSchedule(request, member1.getMemberId());

        // then
        verify(travelAttendeeRepository, times(1)).save(any(TravelAttendee.class));
        assertThat(response.getScheduleId()).isEqualTo(schedule1.getScheduleId());

    }

    @Test
    @DisplayName("일정 생성 시 저장된 사용자 정보 없어 DataNotFoundException 발생")
    void createSchedule_CustomUsernameDataNotFoundException(){
        // given
        ScheduleCreateRequest request = createScheduleRequest(LocalDate.now());

        when(travelScheduleRepository.save(any())).thenReturn(schedule1);
        when(memberRepository.findById(anyLong())).thenReturn(Optional.empty());

        // when
        DataNotFoundException fail = assertThrows(DataNotFoundException.class, () -> scheduleService.createSchedule(request, 0L));

        // then
        assertThat(fail.getHttpStatus()).isEqualTo(ErrorCode.MEMBER_NOT_FOUND.getStatus());
        assertThat(fail.getMessage()).isEqualTo(ErrorCode.MEMBER_NOT_FOUND.getMessage());

    }

    @Test
    @DisplayName("일정 상세 조회")
    void getScheduleDetail(){
        // given
        TravelImage travelImage1 = createTravelImage(travelPlace1, "test1", true);
        TravelImage travelImage2 = createTravelImage(travelPlace1, "test2", false);
        travelPlace1 = createTravelPlace(1L, country, city, district, apiCategory, List.of(travelImage1, travelImage2));

        TravelImage travelImage3 = createTravelImage(travelPlace2, "test1", true);
        TravelImage travelImage4 = createTravelImage(travelPlace2, "test2", false);
        travelPlace2 = createTravelPlace(2L, country, city, district, apiCategory, List.of(travelImage3, travelImage4));

        List<PlaceResponse> placeList = List.of(PlaceResponse.from(travelPlace1), PlaceResponse.from(travelPlace2));
        Pageable pageable = PageUtils.defaultPageable(1);

        when(travelScheduleRepository.findById(any())).thenReturn(Optional.of(schedule1));
        when(travelPlaceRepository.findAllByAreaData(any(), anyString(), anyString(), anyString()))
                .thenReturn(PageUtils.createPage(placeList, pageable, 1));

        // when
        ScheduleDetailResponse response = scheduleService.getScheduleDetail(schedule1.getScheduleId(), 1);

        // then
        assertThat(response.getScheduleName()).isEqualTo(schedule1.getScheduleName());
        assertThat(response.getCreatedAt()).isEqualTo(schedule1.getCreatedAt());
        assertThat(response.getPlaceList().getTotalElements()).isEqualTo(placeList.size());
        assertThat(response.getPlaceList().getContent().get(0).getPlaceName()).isEqualTo(placeList.get(0).getPlaceName());
        assertThat(response.getPlaceList().getContent().get(0).getThumbnailUrl()).isNotNull();
    }

    @Test
    @DisplayName("일정 상세 조회 시 여행지 데이터 없는 경우")
    void getScheduleDetailWithoutData(){
        // given
        Pageable pageable = PageUtils.defaultPageable(1);

        when(travelScheduleRepository.findById(any())).thenReturn(Optional.of(schedule1));
        when(travelPlaceRepository.findAllByAreaData(any(), anyString(), anyString(), anyString()))
                .thenReturn(PageUtils.createPage(new ArrayList<>(), pageable, 0));

        // when
        ScheduleDetailResponse response = scheduleService.getScheduleDetail(schedule1.getScheduleId(), 1);

        // then
        assertThat(response.getScheduleName()).isEqualTo(schedule1.getScheduleName());
        assertThat(response.getCreatedAt()).isEqualTo(schedule1.getCreatedAt());
        assertThat(response.getPlaceList().getTotalElements()).isEqualTo(0);
        assertThat(response.getPlaceList().getContent()).isEmpty();
    }

    @Test
    @DisplayName("일정 상세 조회 시 일정을 찾을 수 없어 DataNotFoundException 발생")
    void getScheduleDetail_dataNotFoundException(){
        // given
        when(travelScheduleRepository.findById(any())).thenReturn(Optional.empty());

        // when
        DataNotFoundException fail = assertThrows(DataNotFoundException.class, () -> scheduleService.getScheduleDetail(0L, 1));

        // then
        assertThat(fail.getHttpStatus()).isEqualTo(ErrorCode.SCHEDULE_NOT_FOUND.getStatus());
        assertThat(fail.getMessage()).isEqualTo(ErrorCode.SCHEDULE_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("일정 수정")
    void updateSchedule(){
        Long scheduleId = schedule1.getScheduleId();

        TravelImage travelImage1 = createTravelImage(travelPlace1, "test1", true);
        TravelImage travelImage2 = createTravelImage(travelPlace1, "test2", false);
        travelPlace1 = createTravelPlace(1L, country, city, district, apiCategory, List.of(travelImage1, travelImage2));

        TravelImage travelImage3 = createTravelImage(travelPlace2, "test1", true);
        TravelImage travelImage4 = createTravelImage(travelPlace2, "test2", false);
        travelPlace2 = createTravelPlace(2L, country, city, district, apiCategory, List.of(travelImage3, travelImage4));


        RouteRequest routeRequest1 = createRouteRequest(1, travelPlace1.getPlaceId());
        RouteRequest routeRequest2 = createRouteRequest(2, travelPlace2.getPlaceId());
        ScheduleUpdateRequest scheduleUpdateRequest = createUpdateScheduleRequest(List.of(routeRequest1, routeRequest2));

        when(travelScheduleRepository.findById(scheduleId)).thenReturn(Optional.of(schedule1));
        when(travelPlaceRepository.findById(travelPlace1.getPlaceId())).thenReturn(Optional.of(travelPlace1));
        when(travelPlaceRepository.findById(travelPlace2.getPlaceId())).thenReturn(Optional.of(travelPlace2));

        // when
        assertDoesNotThrow(() -> scheduleService.updateSchedule(member1.getMemberId(), scheduleId, scheduleUpdateRequest));

        // then
        assertThat(schedule1.getTravelRouteList().size()).isEqualTo(2);
        assertThat(schedule1.getScheduleName()).isEqualTo(scheduleUpdateRequest.getScheduleName());
        assertThat(schedule1.getStartDate()).isEqualTo(scheduleUpdateRequest.getStartDate());
        assertThat(schedule1.getTravelRouteList().get(0).getTravelPlace().getPlaceName()).isEqualTo(travelPlace1.getPlaceName());
    }

    @Test
    @DisplayName("일정 수정 중 여행 루트 삭제에서 기존에 저장된 여행 루트가 없을 경우")
    void updateScheduleNoSavedTravelRouteList(){
        // given
        Long scheduleId = schedule2.getScheduleId();


        TravelImage travelImage1 = createTravelImage(travelPlace1, "test1", true);
        TravelImage travelImage2 = createTravelImage(travelPlace1, "test2", false);
        travelPlace1 = createTravelPlace(1L, country, city, district, apiCategory, List.of(travelImage1, travelImage2));

        TravelImage travelImage3 = createTravelImage(travelPlace2, "test1", true);
        TravelImage travelImage4 = createTravelImage(travelPlace2, "test2", false);
        travelPlace2 = createTravelPlace(2L, country, city, district, apiCategory, List.of(travelImage3, travelImage4));

        RouteRequest routeRequest1 = createRouteRequest(1, travelPlace1.getPlaceId());
        RouteRequest routeRequest2 = createRouteRequest(2, travelPlace2.getPlaceId());
        ScheduleUpdateRequest scheduleUpdateRequest = createUpdateScheduleRequest(List.of(routeRequest1, routeRequest2));

        when(travelScheduleRepository.findById(scheduleId)).thenReturn(Optional.of(schedule2));
        when(travelPlaceRepository.findById(travelPlace1.getPlaceId())).thenReturn(Optional.of(travelPlace1));
        when(travelPlaceRepository.findById(travelPlace2.getPlaceId())).thenReturn(Optional.of(travelPlace2));

        // when
        assertDoesNotThrow(() -> scheduleService.updateSchedule(member1.getMemberId(), scheduleId, scheduleUpdateRequest));

        // then
        assertThat(schedule2.getTravelRouteList().size()).isEqualTo(2);
        assertThat(schedule2.getScheduleName()).isEqualTo(scheduleUpdateRequest.getScheduleName());
        assertThat(schedule2.getStartDate()).isEqualTo(scheduleUpdateRequest.getStartDate());
        assertThat(schedule2.getTravelRouteList().get(0).getTravelPlace().getPlaceName()).isEqualTo(travelPlace1.getPlaceName());
    }

    @Test
    @DisplayName("일정의 여행 루트 수정 시 기존에 저장된 여행 루트가 존재하는 경우")
    void updateTravelRouteInSchedule(){
        TravelImage travelImage1 = createTravelImage(travelPlace1, "test1", true);
        TravelImage travelImage2 = createTravelImage(travelPlace1, "test2", false);
        travelPlace1 = createTravelPlace(1L, country, city, district, apiCategory, List.of(travelImage1, travelImage2));

        TravelImage travelImage3 = createTravelImage(travelPlace2, "test1", true);
        TravelImage travelImage4 = createTravelImage(travelPlace2, "test2", false);
        travelPlace2 = createTravelPlace(2L, country, city, district, apiCategory, List.of(travelImage3, travelImage4));

        TravelRoute route1 = createTravelRoute(schedule1, travelPlace1, 1);
        TravelRoute route2 = createTravelRoute(schedule1, travelPlace1, 2);
        TravelRoute route3 = createTravelRoute(schedule1, travelPlace2, 3);
        schedule1.setTravelRouteList(new ArrayList<>(List.of(route1, route2, route3)));
        schedule2.setTravelRouteList(new ArrayList<>());

        RouteRequest routeRequest1 = createRouteRequest(1, travelPlace1.getPlaceId());
        RouteRequest routeRequest2 = createRouteRequest(2, travelPlace2.getPlaceId());
        ScheduleUpdateRequest scheduleUpdateRequest = createUpdateScheduleRequest(List.of(routeRequest1, routeRequest2));

        when(travelPlaceRepository.findById(travelPlace1.getPlaceId())).thenReturn(Optional.of(travelPlace1));
        when(travelPlaceRepository.findById(travelPlace2.getPlaceId())).thenReturn(Optional.of(travelPlace2));

        // when
        assertDoesNotThrow(() -> scheduleService.updateTravelRouteInSchedule(schedule1, scheduleUpdateRequest.getTravelRoute()));

        // then
        assertThat(schedule1.getTravelRouteList().size()).isEqualTo(2);
        assertThat(schedule1.getTravelRouteList().get(0).getTravelPlace().getPlaceName()).isEqualTo(travelPlace1.getPlaceName());
    }


    @Test
    @DisplayName("일정 수정 시 일정 데이터 없어 예외 발생")
    void updateScheduleNoSchedule_dataNotFoundException(){
        // given
        Long scheduleId = schedule1.getScheduleId();

        TravelImage travelImage1 = createTravelImage(travelPlace1, "test1", true);
        TravelImage travelImage2 = createTravelImage(travelPlace1, "test2", false);
        travelPlace1 = createTravelPlace(1L, country, city, district, apiCategory, List.of(travelImage1, travelImage2));

        TravelImage travelImage3 = createTravelImage(travelPlace2, "test1", true);
        TravelImage travelImage4 = createTravelImage(travelPlace2, "test2", false);
        travelPlace2 = createTravelPlace(2L, country, city, district, apiCategory, List.of(travelImage3, travelImage4));

        RouteRequest routeRequest1 = createRouteRequest(1, travelPlace1.getPlaceId());
        RouteRequest routeRequest2 = createRouteRequest(2, travelPlace2.getPlaceId());
        ScheduleUpdateRequest scheduleUpdateRequest = createUpdateScheduleRequest(List.of(routeRequest1, routeRequest2));

        when(travelScheduleRepository.findById(scheduleId)).thenReturn(Optional.empty());

        // when
        DataNotFoundException fail = assertThrows(DataNotFoundException.class,
                () -> scheduleService.updateSchedule(member1.getMemberId(), scheduleId, scheduleUpdateRequest));

        // then
        assertThat(fail.getHttpStatus()).isEqualTo(ErrorCode.SCHEDULE_NOT_FOUND.getStatus());
        assertThat(fail.getMessage()).isEqualTo(ErrorCode.SCHEDULE_NOT_FOUND.getMessage());

    }

    @Test
    @DisplayName("일정 수정 시 요청 사용자에게 접근 권한이 없어 예외 발생")
    void updateScheduleForbiddenAccess_forbiddenScheduleException(){
        // given
        Long scheduleId = schedule3.getScheduleId();

        TravelImage travelImage1 = createTravelImage(travelPlace1, "test1", true);
        TravelImage travelImage2 = createTravelImage(travelPlace1, "test2", false);
        travelPlace1 = createTravelPlace(1L, country, city, district, apiCategory, List.of(travelImage1, travelImage2));

        TravelImage travelImage3 = createTravelImage(travelPlace2, "test1", true);
        TravelImage travelImage4 = createTravelImage(travelPlace2, "test2", false);
        travelPlace2 = createTravelPlace(2L, country, city, district, apiCategory, List.of(travelImage3, travelImage4));

        TravelRoute route1 = createTravelRoute(schedule1, travelPlace1, 1);
        TravelRoute route2 = createTravelRoute(schedule1, travelPlace1, 2);
        TravelRoute route3 = createTravelRoute(schedule1, travelPlace2, 3);
        schedule1.setTravelRouteList(List.of(route1, route2, route3));
        schedule2.setTravelRouteList(new ArrayList<>());

        RouteRequest routeRequest1 = createRouteRequest(1, travelPlace1.getPlaceId());
        RouteRequest routeRequest2 = createRouteRequest(2, travelPlace2.getPlaceId());
        ScheduleUpdateRequest scheduleUpdateRequest = createUpdateScheduleRequest(List.of(routeRequest1, routeRequest2));

        when(travelScheduleRepository.findById(scheduleId)).thenReturn(Optional.of(schedule3));

        // when
        ForbiddenScheduleException fail = assertThrows(ForbiddenScheduleException.class,
                () -> scheduleService.updateSchedule(member2.getMemberId(), scheduleId, scheduleUpdateRequest));

        // then
        assertThat(fail.getHttpStatus()).isEqualTo(ErrorCode.FORBIDDEN_ACCESS_SCHEDULE.getStatus());
        assertThat(fail.getMessage()).isEqualTo(ErrorCode.FORBIDDEN_ACCESS_SCHEDULE.getMessage());

    }

    @Test
    @DisplayName("일정 수정 시 요청 사용자에게 수정 권한이 없어 예외 발생")
    void updateScheduleForbiddenEdit_forbiddenScheduleException(){
        // given
        Long scheduleId = schedule1.getScheduleId();

        TravelImage travelImage1 = createTravelImage(travelPlace1, "test1", true);
        TravelImage travelImage2 = createTravelImage(travelPlace1, "test2", false);
        travelPlace1 = createTravelPlace(1L, country, city, district, apiCategory, List.of(travelImage1, travelImage2));

        TravelImage travelImage3 = createTravelImage(travelPlace2, "test1", true);
        TravelImage travelImage4 = createTravelImage(travelPlace2, "test2", false);
        travelPlace2 = createTravelPlace(2L, country, city, district, apiCategory, List.of(travelImage3, travelImage4));


        RouteRequest routeRequest1 = createRouteRequest(1, travelPlace1.getPlaceId());
        RouteRequest routeRequest2 = createRouteRequest(2, travelPlace2.getPlaceId());
        ScheduleUpdateRequest scheduleUpdateRequest = createUpdateScheduleRequest(List.of(routeRequest1, routeRequest2));

        when(travelScheduleRepository.findById(scheduleId)).thenReturn(Optional.of(schedule1));

        // when
        ForbiddenScheduleException fail = assertThrows(ForbiddenScheduleException.class,
                () -> scheduleService.updateSchedule(member2.getMemberId(), scheduleId, scheduleUpdateRequest));

        // then
        assertThat(fail.getHttpStatus()).isEqualTo(ErrorCode.FORBIDDEN_EDIT_SCHEDULE.getStatus());
        assertThat(fail.getMessage()).isEqualTo(ErrorCode.FORBIDDEN_EDIT_SCHEDULE.getMessage());
    }


    @Test
    @DisplayName("일정 수정 시 여행 루트에 저장된 여행지가 없어 예외 발생")
    void updateScheduleNoTravelPlace_dataNotFoundException(){
        // given
        Long scheduleId = schedule1.getScheduleId();


        TravelImage travelImage1 = createTravelImage(travelPlace1, "test1", true);
        TravelImage travelImage2 = createTravelImage(travelPlace1, "test2", false);
        travelPlace1 = createTravelPlace(1L, country, city, district, apiCategory, List.of(travelImage1, travelImage2));

        TravelImage travelImage3 = createTravelImage(travelPlace2, "test1", true);
        TravelImage travelImage4 = createTravelImage(travelPlace2, "test2", false);
        travelPlace2 = createTravelPlace(2L, country, city, district, apiCategory, List.of(travelImage3, travelImage4));


        RouteRequest routeRequest1 = createRouteRequest(1, travelPlace1.getPlaceId());
        RouteRequest routeRequest2 = createRouteRequest(2, travelPlace2.getPlaceId());
        ScheduleUpdateRequest scheduleUpdateRequest = createUpdateScheduleRequest(List.of(routeRequest1, routeRequest2));

        when(travelScheduleRepository.findById(scheduleId)).thenReturn(Optional.of(schedule1));
        when(travelPlaceRepository.findById(anyLong())).thenReturn(Optional.empty());

        // when
        DataNotFoundException fail = assertThrows(DataNotFoundException.class,
                () -> scheduleService.updateSchedule(member1.getMemberId(), scheduleId, scheduleUpdateRequest));

        // then
        assertThat(fail.getHttpStatus()).isEqualTo(ErrorCode.PLACE_NOT_FOUND.getStatus());
        assertThat(fail.getMessage()).isEqualTo(ErrorCode.PLACE_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("참가자 정보 조회")
    void getAttendeeInfo_containsAttendees(){
        // given
        // when
        TravelAttendee response = scheduleService.getAttendeeInfo(schedule1, member1.getMemberId());

        // then
        assertThat(response.getMember().getEmail()).isEqualTo(member1.getEmail());
        assertThat(response.getTravelSchedule().getScheduleName()).isEqualTo(schedule1.getScheduleName());
    }

    @Test
    @DisplayName("참가자 정보 조회 시 데이터 존재하지 않는 경우")
    void getAttendeeInfo_notContainsAttendees(){
        // given
        // when
        ForbiddenScheduleException fail = assertThrows(ForbiddenScheduleException.class,
                () -> scheduleService.getAttendeeInfo(schedule3, member2.getMemberId()));

        // then
        assertThat(fail.getHttpStatus()).isEqualTo(ErrorCode.FORBIDDEN_ACCESS_SCHEDULE.getStatus());
        assertThat(fail.getMessage()).isEqualTo(ErrorCode.FORBIDDEN_ACCESS_SCHEDULE.getMessage());

    }

    @Test
    @DisplayName("일정 수정 사용자 권한 체크 ALL")
    void checkScheduleEditPermissionALL(){
        // given
        attendee1.updatePermission(AttendeePermission.ALL);

        // when, then
        assertDoesNotThrow(() -> scheduleService.checkScheduleEditPermission(attendee1));
    }

    @Test
    @DisplayName("일정 수정 사용자 권한 체크 EDIT")
    void checkScheduleEditPermissionEdit(){
        // given
        attendee1.updatePermission(AttendeePermission.EDIT);

        // when
        // then
        assertDoesNotThrow(() -> scheduleService.checkScheduleEditPermission(attendee1));
    }

    @Test
    @DisplayName("일정 수정 사용자 권한 체크 중 CHAT 권한으로 예외 발생")
    void checkScheduleEditPermissionCHAT_forbiddenScheduleException(){
        // given
        attendee1.updatePermission(AttendeePermission.CHAT);

        // when
        ForbiddenScheduleException fail = assertThrows(ForbiddenScheduleException.class,
                () -> scheduleService.checkScheduleEditPermission(attendee1));

        // then
        assertThat(fail.getHttpStatus()).isEqualTo(ErrorCode.FORBIDDEN_EDIT_SCHEDULE.getStatus());
        assertThat(fail.getMessage()).isEqualTo(ErrorCode.FORBIDDEN_EDIT_SCHEDULE.getMessage());
    }

    @Test
    @DisplayName("일정 수정 사용자 권한 체크 시 READ 권한으로 예외 발생")
    void checkScheduleEditPermissionREAD_forbiddenScheduleException(){
        // given
        attendee1.updatePermission(AttendeePermission.READ);

        // when
        ForbiddenScheduleException fail = assertThrows(ForbiddenScheduleException.class,
                () -> scheduleService.checkScheduleEditPermission(attendee1));

        // then
        assertThat(fail.getHttpStatus()).isEqualTo(ErrorCode.FORBIDDEN_EDIT_SCHEDULE.getStatus());
        assertThat(fail.getMessage()).isEqualTo(ErrorCode.FORBIDDEN_EDIT_SCHEDULE.getMessage());
    }

    @Test
    @DisplayName("일정 삭제")
    void deleteSchedule(){
        // given
        ChatMessage message1 = createChatMessage("chat1", schedule1.getScheduleId(), member1, "hello1");
        ChatMessage message2 = createChatMessage("chat2", schedule1.getScheduleId(), member1, "hello2");
        ChatMessage message3 = createChatMessage("chat3", schedule1.getScheduleId(), member2, "hello3");
        List<ChatMessage> chatMessages = List.of(message1, message2, message3);

        when(travelAttendeeRepository.findByTravelSchedule_ScheduleIdAndMember_MemberId(anyLong(), anyLong()))
                .thenReturn(Optional.of(attendee1));
        when(chatMessageRepository.findAllByScheduleId(anyLong())).thenReturn(chatMessages);

        // when
        assertDoesNotThrow(() -> scheduleService.deleteSchedule(schedule1.getScheduleId(), member1.getMemberId()));

        // then
        verify(chatMessageRepository, times(1)).deleteAllByScheduleId(schedule1.getScheduleId());
    }

    @Test
    @DisplayName("일정 삭제 시 채팅 메시지 없는 경우")
    void deleteScheduleNoChatMessageData(){
        // given
        when(travelAttendeeRepository.findByTravelSchedule_ScheduleIdAndMember_MemberId(anyLong(), anyLong()))
                .thenReturn(Optional.of(attendee1));
        when(chatMessageRepository.findAllByScheduleId(anyLong())).thenReturn(new ArrayList<>());

        // when
        assertDoesNotThrow(() -> scheduleService.deleteSchedule(schedule1.getScheduleId(), member1.getMemberId()));

        // then
        verify(chatMessageRepository, times(0)).deleteAllByScheduleId(schedule1.getScheduleId());
    }

    @Test
    @DisplayName("일정 삭제 시 작성자가 아닌 사용자가 삭제 요청으로 인해 예외 발생")
    void deleteScheduleNotAuthor_forbiddenScheduleException(){
        // given
        when(travelAttendeeRepository.findByTravelSchedule_ScheduleIdAndMember_MemberId(anyLong(), anyLong()))
                .thenReturn(Optional.of(attendee2));

        // when
        ForbiddenScheduleException fail = assertThrows(ForbiddenScheduleException.class,
                () -> scheduleService.deleteSchedule(schedule1.getScheduleId(), member2.getMemberId()));

        assertThat(fail.getHttpStatus()).isEqualTo(ErrorCode.FORBIDDEN_DELETE_SCHEDULE.getStatus());
        assertThat(fail.getMessage()).isEqualTo(ErrorCode.FORBIDDEN_DELETE_SCHEDULE.getMessage());
    }

    @Test
    @DisplayName("일정 id를 통해 채팅 메시지 삭제")
    void deleteChatMessageByScheduleId(){
        // given
        ChatMessage message1 = createChatMessage("chat1", schedule1.getScheduleId(), member1, "hello1");
        ChatMessage message2 = createChatMessage("chat2", schedule1.getScheduleId(), member1, "hello2");
        ChatMessage message3 = createChatMessage("chat3", schedule1.getScheduleId(), member2, "hello3");
        List<ChatMessage> chatMessages = List.of(message1, message2, message3);

       when(chatMessageRepository.findAllByScheduleId(anyLong())).thenReturn(chatMessages);

        // when
        assertDoesNotThrow(() -> scheduleService.deleteChatMessageByScheduleId(schedule1.getScheduleId()));

        // then
        verify(chatMessageRepository, times(1)).deleteAllByScheduleId(schedule1.getScheduleId());

    }


    @Test
    @DisplayName("일정 id를 통해 채팅 메시지 삭제 시 채팅 메시지 데이터 없는 경우")
    void deleteChatMessageByScheduleId_noData(){
        // given
        when(chatMessageRepository.findAllByScheduleId(anyLong())).thenReturn(new ArrayList<>());

        // when
        assertDoesNotThrow(() -> scheduleService.deleteChatMessageByScheduleId(schedule1.getScheduleId()));

        // then
        verify(chatMessageRepository, times(0)).deleteAllByScheduleId(schedule1.getScheduleId());

    }

}
