package com.triptune.domain.schedule.service;

import com.triptune.domain.common.entity.*;
import com.triptune.domain.member.entity.Member;
import com.triptune.domain.member.entity.ProfileImage;
import com.triptune.domain.member.repository.MemberRepository;
import com.triptune.domain.schedule.ScheduleTest;
import com.triptune.domain.schedule.dto.*;
import com.triptune.domain.schedule.dto.request.CreateScheduleRequest;
import com.triptune.domain.schedule.dto.request.RouteRequest;
import com.triptune.domain.schedule.dto.request.UpdateScheduleRequest;
import com.triptune.domain.schedule.dto.response.CreateScheduleResponse;
import com.triptune.domain.schedule.dto.response.ScheduleDetailResponse;
import com.triptune.domain.schedule.dto.response.ScheduleInfoResponse;
import com.triptune.domain.schedule.entity.ChatMessage;
import com.triptune.domain.schedule.entity.TravelAttendee;
import com.triptune.domain.schedule.entity.TravelRoute;
import com.triptune.domain.schedule.entity.TravelSchedule;
import com.triptune.domain.schedule.enumclass.AttendeePermission;
import com.triptune.domain.schedule.enumclass.AttendeeRole;
import com.triptune.domain.schedule.exception.ForbiddenScheduleException;
import com.triptune.domain.schedule.repository.ChatMessageRepository;
import com.triptune.domain.schedule.repository.TravelAttendeeRepository;
import com.triptune.domain.schedule.repository.TravelRouteRepository;
import com.triptune.domain.schedule.repository.TravelScheduleRepository;
import com.triptune.domain.travel.entity.TravelImage;
import com.triptune.domain.travel.entity.TravelPlace;
import com.triptune.domain.travel.repository.TravelPlaceRepository;
import com.triptune.global.enumclass.ErrorCode;
import com.triptune.global.exception.DataNotFoundException;
import com.triptune.global.response.pagination.SchedulePageResponse;
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

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ScheduleServiceTest extends ScheduleTest {

    @InjectMocks
    private ScheduleService scheduleService;

    @Mock
    private TravelScheduleRepository travelScheduleRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private TravelAttendeeRepository travelAttendeeRepository;

    @Mock
    private TravelPlaceRepository travelPlaceRepository;

    @Mock
    private TravelRouteRepository travelRouteRepository;

    @Mock
    private ChatMessageRepository chatMessageRepository;

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
        Country country = createCountry();
        City city = createCity(country);
        District district = createDistrict(city, "중구");
        ApiCategory apiCategory = createApiCategory();
        travelPlace1 = createTravelPlace(1L, country, city, district, apiCategory);
        TravelImage travelImage1 = createTravelImage(travelPlace1, "test1", true);
        TravelImage travelImage2 = createTravelImage(travelPlace1, "test2", false);
        List<TravelImage> travelImageList1 = new ArrayList<>(List.of(travelImage1, travelImage2));
        travelPlace1.setTravelImageList(travelImageList1);

        travelPlace2 = createTravelPlace(2L, country, city, district, apiCategory);
        TravelImage travelImage3 = createTravelImage(travelPlace2, "test1", true);
        TravelImage travelImage4 = createTravelImage(travelPlace2, "test2", false);
        List<TravelImage> travelImageList2 = new ArrayList<>(List.of(travelImage3, travelImage4));
        travelPlace2.setTravelImageList(travelImageList2);

        member1 = createMember(1L, "member1");
        member2 = createMember(2L, "member2");
        ProfileImage member1Image = createProfileImage(1L, "member1Image");
        ProfileImage member2Image = createProfileImage(2L, "member2Image");
        member1.setProfileImage(member1Image);
        member2.setProfileImage(member2Image);

        schedule1 = createTravelSchedule(1L, "테스트1");
        schedule2 = createTravelSchedule(2L, "테스트2");
        schedule3 = createTravelSchedule(3L, "테스트3");

        attendee1 = createTravelAttendee(member1, schedule1, AttendeeRole.AUTHOR, AttendeePermission.ALL);
        attendee2 = createTravelAttendee(member2, schedule1, AttendeeRole.GUEST, AttendeePermission.READ);
        TravelAttendee attendee3 = createTravelAttendee(member1, schedule2, AttendeeRole.AUTHOR, AttendeePermission.ALL);
        TravelAttendee attendee4 = createTravelAttendee(member2, schedule2, AttendeeRole.GUEST, AttendeePermission.CHAT);
        TravelAttendee attendee5 = createTravelAttendee(member1, schedule3, AttendeeRole.AUTHOR, AttendeePermission.ALL);
        schedule1.setTravelAttendeeList(new ArrayList<>(List.of(attendee1, attendee2)));
        schedule2.setTravelAttendeeList(new ArrayList<>(List.of(attendee3, attendee4)));
        schedule3.setTravelAttendeeList(new ArrayList<>(List.of(attendee5)));

        TravelRoute route1 = createTravelRoute(schedule1, travelPlace1, 1);
        TravelRoute route2 = createTravelRoute(schedule1, travelPlace1, 2);
        TravelRoute route3 = createTravelRoute(schedule1, travelPlace2, 3);
        schedule1.setTravelRouteList(new ArrayList<>(List.of(route1, route2, route3)));
        schedule2.setTravelRouteList(new ArrayList<>());
    }


    @Test
    @DisplayName("내 일정 목록 조회")
    void getAllSchedulesByUserId(){
        // given
        Pageable pageable = PageUtil.schedulePageable(1);

        List<TravelSchedule> schedules = new ArrayList<>(List.of(schedule1, schedule2, schedule3));
        Page<TravelSchedule> schedulePage = PageUtil.createPage(schedules, pageable, schedules.size());

        when(travelScheduleRepository.findTravelSchedulesByUserId(pageable, member1.getUserId())).thenReturn(schedulePage);
        when(travelScheduleRepository.countSharedTravelSchedulesByUserId(anyString())).thenReturn(2);

        // when
        SchedulePageResponse<ScheduleInfoResponse> response = scheduleService.getAllSchedulesByUserId(1, member1.getUserId());

        // then
        List<ScheduleInfoResponse> content = response.getContent();
        assertEquals(response.getTotalElements(), 3);
        assertEquals(response.getTotalSharedElements(), 2);
        assertEquals(content.get(0).getScheduleName(), schedule1.getScheduleName());
        assertNotNull(content.get(0).getSinceUpdate());
        assertNotNull(content.get(0).getThumbnailUrl());
        assertEquals(content.get(0).getAuthor().getNickname(), member1.getNickname());
        assertEquals(content.get(0).getRole(), AttendeeRole.AUTHOR);
    }

    @Test
    @DisplayName("내 일정 목록 조회 시 공유된 일정이 없는 경우")
    void getAllSchedulesByUserIdNotShared(){
        // given
        Pageable pageable = PageUtil.schedulePageable(1);

        List<TravelSchedule> schedules = new ArrayList<>(List.of(schedule3));
        Page<TravelSchedule> schedulePage = PageUtil.createPage(schedules, pageable, schedules.size());

        when(travelScheduleRepository.findTravelSchedulesByUserId(pageable, member1.getUserId())).thenReturn(schedulePage);
        when(travelScheduleRepository.countSharedTravelSchedulesByUserId(anyString())).thenReturn(0);

        // when
        SchedulePageResponse<ScheduleInfoResponse> response = scheduleService.getAllSchedulesByUserId(1, member1.getUserId());

        // then
        List<ScheduleInfoResponse> content = response.getContent();
        assertEquals(response.getTotalElements(), 1);
        assertEquals(response.getTotalSharedElements(), 0);
        assertEquals(content.get(0).getScheduleName(), schedule3.getScheduleName());
        assertNotNull(content.get(0).getSinceUpdate());
        assertEquals(content.get(0).getAuthor().getNickname(), member1.getNickname());
    }

    @Test
    @DisplayName("내 일정 목록 조회 시 일정 데이터 없는 경우")
    void getAllSchedulesByUserIdNoScheduleData(){
        // given
        Pageable pageable = PageUtil.schedulePageable(1);
        Page<TravelSchedule> emptySchedulePage = PageUtil.createPage(new ArrayList<>(), pageable, 0);

        when(travelScheduleRepository.findTravelSchedulesByUserId(pageable, member1.getUserId())).thenReturn(emptySchedulePage);
        when(travelScheduleRepository.countSharedTravelSchedulesByUserId(anyString())).thenReturn(2);

        // when
        SchedulePageResponse<ScheduleInfoResponse> response = scheduleService.getAllSchedulesByUserId(1, member1.getUserId());

        // then
        assertEquals(response.getTotalElements(), 0);
        assertEquals(response.getTotalSharedElements(), 2);
        assertTrue(response.getContent().isEmpty());
        verify(travelRouteRepository, times(0)).findAllByTravelSchedule_ScheduleId(any(), any());
    }

    @Test
    @DisplayName("내 일정 목록 조회 시 이미지 썸네일 데이터 없는 경우")
    void getAllSchedulesByUserIdNoImageThumbnail(){
        // given
        Pageable pageable = PageUtil.schedulePageable(1);
        travelPlace1.getTravelImageList().get(0).setThumbnail(false);

        List<TravelSchedule> schedules = new ArrayList<>(List.of(schedule1, schedule2));
        Page<TravelSchedule> schedulePage = PageUtil.createPage(schedules, pageable, schedules.size());

        when(travelScheduleRepository.findTravelSchedulesByUserId(pageable, member1.getUserId())).thenReturn(schedulePage);
        when(travelScheduleRepository.countSharedTravelSchedulesByUserId(anyString())).thenReturn(1);

        // when
        SchedulePageResponse<ScheduleInfoResponse> response = scheduleService.getAllSchedulesByUserId(1, member1.getUserId());

        // then
        List<ScheduleInfoResponse> content = response.getContent();
        assertEquals(response.getTotalElements(), 2);
        assertEquals(response.getTotalSharedElements(), 1);
        assertEquals(content.get(0).getScheduleName(), "테스트1");
        assertNotNull(content.get(0).getSinceUpdate());
        assertNull(content.get(0).getThumbnailUrl());

    }


    @Test
    @DisplayName("내 일정 목록 조회 시 이미지 데이터 없는 경우")
    void getAllSchedulesByUserIdNoImageData(){
        // given
        Pageable pageable = PageUtil.schedulePageable(1);
        travelPlace1.setTravelImageList(new ArrayList<>());

        List<TravelSchedule> schedules = new ArrayList<>(List.of(schedule1));
        Page<TravelSchedule> schedulePage = PageUtil.createPage(schedules, pageable, schedules.size());

        when(travelScheduleRepository.findTravelSchedulesByUserId(pageable, member1.getUserId())).thenReturn(schedulePage);
        when(travelScheduleRepository.countSharedTravelSchedulesByUserId(anyString())).thenReturn(1);

        // when
        SchedulePageResponse<ScheduleInfoResponse> response = scheduleService.getAllSchedulesByUserId(1, member1.getUserId());

        // then
        List<ScheduleInfoResponse> content = response.getContent();
        assertEquals(response.getTotalElements(), 1);
        assertEquals(response.getTotalSharedElements(), 1);
        assertEquals(content.get(0).getScheduleName(), "테스트1");
        assertNotNull(content.get(0).getSinceUpdate());
        assertNull(content.get(0).getThumbnailUrl());

    }

    @Test
    @DisplayName("공유된 일정 목록 조회")
    void getSharedSchedulesByUserId(){
        // given
        Pageable pageable = PageUtil.schedulePageable(1);

        List<TravelSchedule> schedules = new ArrayList<>(List.of(schedule1, schedule2));
        Page<TravelSchedule> schedulePage = PageUtil.createPage(schedules, pageable, schedules.size());

        when(travelScheduleRepository.findSharedTravelSchedulesByUserId(pageable, member1.getUserId())).thenReturn(schedulePage);
        when(travelScheduleRepository.countTravelSchedulesByUserId(anyString())).thenReturn(3);

        // when
        SchedulePageResponse<ScheduleInfoResponse> response = scheduleService.getSharedSchedulesByUserId(1, member1.getUserId());

        // then
        List<ScheduleInfoResponse> content = response.getContent();
        assertEquals(response.getTotalElements(), 3);
        assertEquals(response.getTotalSharedElements(), 2);
        assertEquals(content.get(0).getScheduleName(), schedule1.getScheduleName());
        assertNotNull(content.get(0).getSinceUpdate());
        assertNotNull(content.get(0).getThumbnailUrl());
        assertEquals(content.get(0).getAuthor().getNickname(), member1.getNickname());
        assertEquals(content.get(0).getRole(), AttendeeRole.AUTHOR);
    }


    @Test
    @DisplayName("공유된 일정 목록 조회 시 일정 데이터 없는 경우")
    void getSharedSchedulesByUserIdNoScheduleData(){
        // given
        Pageable pageable = PageUtil.schedulePageable(1);
        Page<TravelSchedule> emptySchedulePage = PageUtil.createPage(new ArrayList<>(), pageable, 0);

        when(travelScheduleRepository.findSharedTravelSchedulesByUserId(pageable, member1.getUserId())).thenReturn(emptySchedulePage);
        when(travelScheduleRepository.countTravelSchedulesByUserId(anyString())).thenReturn(2);

        // when
        SchedulePageResponse<ScheduleInfoResponse> response = scheduleService.getSharedSchedulesByUserId(1, member1.getUserId());

        // then
        assertEquals(response.getTotalElements(), 2);
        assertEquals(response.getTotalSharedElements(), 0);
        assertTrue(response.getContent().isEmpty());
    }

    @Test
    @DisplayName("공유된 일정 목록 조회 시 이미지 썸네일 데이터 없는 경우")
    void getSharedSchedulesByUserIdNoImageThumbnail(){
        // given
        Pageable pageable = PageUtil.schedulePageable(1);
        travelPlace1.getTravelImageList().get(0).setThumbnail(false);

        List<TravelSchedule> schedules = new ArrayList<>(List.of(schedule1, schedule2));
        Page<TravelSchedule> schedulePage = PageUtil.createPage(schedules, pageable, schedules.size());

        when(travelScheduleRepository.findSharedTravelSchedulesByUserId(pageable, member1.getUserId())).thenReturn(schedulePage);
        when(travelScheduleRepository.countTravelSchedulesByUserId(anyString())).thenReturn(3);

        // when
        SchedulePageResponse<ScheduleInfoResponse> response = scheduleService.getSharedSchedulesByUserId(1, member1.getUserId());

        // then
        List<ScheduleInfoResponse> content = response.getContent();
        assertEquals(response.getTotalElements(), 3);
        assertEquals(response.getTotalSharedElements(), 2);
        assertEquals(content.get(0).getScheduleName(), "테스트1");
        assertNotNull(content.get(0).getSinceUpdate());
        assertNull(content.get(0).getThumbnailUrl());

    }


    @Test
    @DisplayName("내 일정 목록 조회 시 이미지 데이터 없는 경우")
    void getSharedSchedulesByUserIdNoImageData(){
        // given
        Pageable pageable = PageUtil.schedulePageable(1);
        travelPlace1.setTravelImageList(new ArrayList<>());

        List<TravelSchedule> schedules = new ArrayList<>(List.of(schedule1));
        Page<TravelSchedule> schedulePage = PageUtil.createPage(schedules, pageable, schedules.size());

        when(travelScheduleRepository.findSharedTravelSchedulesByUserId(pageable, member1.getUserId())).thenReturn(schedulePage);
        when(travelScheduleRepository.countTravelSchedulesByUserId(anyString())).thenReturn(1);

        // when
        SchedulePageResponse<ScheduleInfoResponse> response = scheduleService.getSharedSchedulesByUserId(1, member1.getUserId());

        // then
        List<ScheduleInfoResponse> content = response.getContent();
        assertEquals(response.getTotalElements(), 1);
        assertEquals(response.getTotalSharedElements(), 1);
        assertEquals(content.get(0).getScheduleName(), "테스트1");
        assertNotNull(content.get(0).getSinceUpdate());
        assertNull(content.get(0).getThumbnailUrl());

    }

    @Test
    @DisplayName("내 일정 검색")
    void searchAllSchedules(){
        // given
        String keyword = "테스트";
        Pageable pageable = PageUtil.schedulePageable(1);

        List<TravelSchedule> schedules = new ArrayList<>(List.of(schedule1, schedule2, schedule3));
        Page<TravelSchedule> schedulePage = PageUtil.createPage(schedules, pageable, schedules.size());

        when(travelScheduleRepository.searchTravelSchedulesByUserIdAndKeyword(pageable, keyword, member1.getUserId())).thenReturn(schedulePage);
        when(travelScheduleRepository.countSharedTravelSchedulesByUserIdAndKeyword(keyword, member1.getUserId())).thenReturn(1);

        // when
        SchedulePageResponse<ScheduleInfoResponse> response = scheduleService.searchAllSchedules(1, keyword, member1.getUserId());

        // then
        List<ScheduleInfoResponse> content = response.getContent();
        assertEquals(response.getTotalElements(), 3);
        assertEquals(response.getTotalSharedElements(), 1);
        assertNotNull(content.get(0).getScheduleName());
        assertNotNull(content.get(0).getSinceUpdate());
        assertEquals(content.get(0).getAuthor().getNickname(), member1.getNickname());
        assertEquals(content.get(0).getRole(), AttendeeRole.AUTHOR);
    }

    @Test
    @DisplayName("내 일정 검색 시 공유된 일정이 없는 경우")
    void searchAllSchedulesNotShared(){
        // given
        String keyword = "3";
        Pageable pageable = PageUtil.schedulePageable(1);

        List<TravelSchedule> schedules = new ArrayList<>(List.of(schedule3));
        Page<TravelSchedule> schedulePage = PageUtil.createPage(schedules, pageable, schedules.size());

        when(travelScheduleRepository.searchTravelSchedulesByUserIdAndKeyword(pageable, keyword, member1.getUserId())).thenReturn(schedulePage);
        when(travelScheduleRepository.countSharedTravelSchedulesByUserIdAndKeyword(keyword, member1.getUserId())).thenReturn(0);

        // when
        SchedulePageResponse<ScheduleInfoResponse> response = scheduleService.searchAllSchedules(1, "3", member1.getUserId());

        // then
        List<ScheduleInfoResponse> content = response.getContent();
        assertEquals(response.getTotalElements(), 1);
        assertEquals(response.getTotalSharedElements(), 0);
        assertEquals(content.get(0).getScheduleName(), schedule3.getScheduleName());
        assertNotNull(content.get(0).getSinceUpdate());
        assertEquals(content.get(0).getAuthor().getNickname(), member1.getNickname());
    }

    @Test
    @DisplayName("내 일정 검색 시 검색 결과가 없는 경우")
    void searchAllSchedulesNoData(){
        // given
        String keyword = "ㅁㄴㅇㄹ";
        Pageable pageable = PageUtil.schedulePageable(1);
        Page<TravelSchedule> emptySchedulePage = PageUtil.createPage(new ArrayList<>(), pageable, 0);

        when(travelScheduleRepository.searchTravelSchedulesByUserIdAndKeyword(pageable, keyword, member1.getUserId())).thenReturn(emptySchedulePage);
        when(travelScheduleRepository.countSharedTravelSchedulesByUserIdAndKeyword(keyword, member1.getUserId())).thenReturn(2);

        // when
        SchedulePageResponse<ScheduleInfoResponse> response = scheduleService.searchAllSchedules(1, keyword, member1.getUserId());

        // then
        assertEquals(response.getTotalElements(), 0);
        assertEquals(response.getTotalSharedElements(), 2);
        assertTrue(response.getContent().isEmpty());
    }

    @Test
    @DisplayName("내 일정 검색 시 이미지 썸네일 데이터 없는 경우")
    void searchAllSchedulesNoImageThumbnail(){
        // given
        String keyword = "테스트";
        Pageable pageable = PageUtil.schedulePageable(1);
        travelPlace1.getTravelImageList().get(0).setThumbnail(false);

        List<TravelSchedule> schedules = new ArrayList<>(List.of(schedule1, schedule2));
        Page<TravelSchedule> schedulePage = PageUtil.createPage(schedules, pageable, schedules.size());

        when(travelScheduleRepository.searchTravelSchedulesByUserIdAndKeyword(pageable, keyword, member1.getUserId())).thenReturn(schedulePage);
        when(travelScheduleRepository.countSharedTravelSchedulesByUserIdAndKeyword(keyword, member1.getUserId())).thenReturn(1);

        // when
        SchedulePageResponse<ScheduleInfoResponse> response = scheduleService.searchAllSchedules(1, keyword, member1.getUserId());

        // then
        List<ScheduleInfoResponse> content = response.getContent();
        assertEquals(response.getTotalElements(), 2);
        assertEquals(response.getTotalSharedElements(), 1);
        assertEquals(content.get(0).getScheduleName(), "테스트1");
        assertNotNull(content.get(0).getSinceUpdate());
        assertNull(content.get(0).getThumbnailUrl());

    }


    @Test
    @DisplayName("내 일정 목록 조회 시 이미지 데이터 없는 경우")
    void searchAllSchedulesNoImageData(){
        // given
        String keyword = "1";
        Pageable pageable = PageUtil.schedulePageable(1);
        travelPlace1.setTravelImageList(new ArrayList<>());

        List<TravelSchedule> schedules = new ArrayList<>(List.of(schedule1));
        Page<TravelSchedule> schedulePage = PageUtil.createPage(schedules, pageable, schedules.size());

        when(travelScheduleRepository.searchTravelSchedulesByUserIdAndKeyword(pageable, keyword, member1.getUserId())).thenReturn(schedulePage);
        when(travelScheduleRepository.countSharedTravelSchedulesByUserIdAndKeyword(keyword, member1.getUserId())).thenReturn(1);

        // when
        SchedulePageResponse<ScheduleInfoResponse> response = scheduleService.searchAllSchedules(1, keyword, member1.getUserId());

        // then
        List<ScheduleInfoResponse> content = response.getContent();
        assertEquals(response.getTotalElements(), 1);
        assertEquals(response.getTotalSharedElements(), 1);
        assertEquals(content.get(0).getScheduleName(), "테스트1");
        assertNotNull(content.get(0).getSinceUpdate());
        assertNull(content.get(0).getThumbnailUrl());

    }

    @Test
    @DisplayName("공유된 일정 검색")
    void searchSharedSchedules(){
        // given
        String keyword = "테스트";
        Pageable pageable = PageUtil.schedulePageable(1);

        List<TravelSchedule> schedules = new ArrayList<>(List.of(schedule1, schedule2, schedule3));
        Page<TravelSchedule> schedulePage = PageUtil.createPage(schedules, pageable, schedules.size());

        when(travelScheduleRepository.searchSharedTravelSchedulesByUserIdAndKeyword(pageable, keyword, member1.getUserId())).thenReturn(schedulePage);
        when(travelScheduleRepository.countTravelSchedulesByUserIdAndKeyword(keyword, member1.getUserId())).thenReturn(5);

        // when
        SchedulePageResponse<ScheduleInfoResponse> response = scheduleService.searchSharedSchedules(1, keyword, member1.getUserId());

        // then
        List<ScheduleInfoResponse> content = response.getContent();
        assertEquals(response.getTotalElements(), 5);
        assertEquals(response.getTotalSharedElements(), 3);
        assertEquals(content.get(0).getScheduleName(), schedule1.getScheduleName());
        assertNotNull(content.get(0).getSinceUpdate());
        assertNotNull(content.get(0).getThumbnailUrl());
        assertEquals(content.get(0).getAuthor().getNickname(), member1.getNickname());
        assertEquals(content.get(0).getRole(), AttendeeRole.AUTHOR);
    }


    @Test
    @DisplayName("공유된 일정 목록 조회 시 일정 데이터 없는 경우")
    void searchSharedSchedulesNoData(){
        // given
        String keyword = "테스트";
        Pageable pageable = PageUtil.schedulePageable(1);
        Page<TravelSchedule> emptySchedulePage = PageUtil.createPage(new ArrayList<>(), pageable, 0);

        when(travelScheduleRepository.searchSharedTravelSchedulesByUserIdAndKeyword(pageable, keyword, member1.getUserId())).thenReturn(emptySchedulePage);
        when(travelScheduleRepository.countTravelSchedulesByUserIdAndKeyword(keyword, member1.getUserId())).thenReturn(2);

        // when
        SchedulePageResponse<ScheduleInfoResponse> response = scheduleService.searchSharedSchedules(1, keyword, member1.getUserId());

        // then
        assertEquals(response.getTotalElements(), 2);
        assertEquals(response.getTotalSharedElements(), 0);
        assertTrue(response.getContent().isEmpty());
    }

    @Test
    @DisplayName("공유된 일정 목록 조회 시 이미지 썸네일 데이터 없는 경우")
    void searchSharedSchedulesNoImageThumbnail(){
        // given
        String keyword = "테스트";
        Pageable pageable = PageUtil.schedulePageable(1);
        travelPlace1.getTravelImageList().get(0).setThumbnail(false);

        List<TravelSchedule> schedules = new ArrayList<>(List.of(schedule1, schedule2));
        Page<TravelSchedule> schedulePage = PageUtil.createPage(schedules, pageable, schedules.size());

        when(travelScheduleRepository.searchSharedTravelSchedulesByUserIdAndKeyword(pageable, keyword, member1.getUserId())).thenReturn(schedulePage);
        when(travelScheduleRepository.countTravelSchedulesByUserIdAndKeyword(keyword, member1.getUserId())).thenReturn(3);

        // when
        SchedulePageResponse<ScheduleInfoResponse> response = scheduleService.searchSharedSchedules(1, keyword, member1.getUserId());

        // then
        List<ScheduleInfoResponse> content = response.getContent();
        assertEquals(response.getTotalElements(), 3);
        assertEquals(response.getTotalSharedElements(), 2);
        assertEquals(content.get(0).getScheduleName(), "테스트1");
        assertNotNull(content.get(0).getSinceUpdate());
        assertNull(content.get(0).getThumbnailUrl());

    }


    @Test
    @DisplayName("공유된 일정 검색 시 이미지 데이터 없는 경우")
    void searchSharedSchedulesNoImageData(){
        // given
        String keyword = "테스트";
        Pageable pageable = PageUtil.schedulePageable(1);
        travelPlace1.setTravelImageList(new ArrayList<>());

        List<TravelSchedule> schedules = new ArrayList<>(List.of(schedule1, schedule2));
        Page<TravelSchedule> schedulePage = PageUtil.createPage(schedules, pageable, schedules.size());

        when(travelScheduleRepository.searchSharedTravelSchedulesByUserIdAndKeyword(pageable, keyword, member1.getUserId())).thenReturn(schedulePage);
        when(travelScheduleRepository.countTravelSchedulesByUserIdAndKeyword(keyword, member1.getUserId())).thenReturn(1);

        // when
        SchedulePageResponse<ScheduleInfoResponse> response = scheduleService.searchSharedSchedules(1, keyword, member1.getUserId());

        // then
        List<ScheduleInfoResponse> content = response.getContent();
        assertEquals(response.getTotalElements(), 1);
        assertEquals(response.getTotalSharedElements(), 2);
        assertEquals(content.get(0).getScheduleName(), "테스트1");
        assertNotNull(content.get(0).getSinceUpdate());
        assertNull(content.get(0).getThumbnailUrl());

    }

    @Test
    @DisplayName("TravelSchedule 를 ScheduleInfoResponse 로 변경")
    void createScheduleInfoResponse(){
        // given
        List<TravelSchedule> travelScheduleList = new ArrayList<>(List.of(schedule1));
        Page<TravelSchedule> schedulePage = PageUtil.createPage(travelScheduleList, PageUtil.schedulePageable(1), travelScheduleList.size());
        // when
        List<ScheduleInfoResponse> response = scheduleService.createScheduleInfoResponse(schedulePage, member1.getUserId());

        // then
        assertEquals(response.size(), 1);
        assertEquals(response.get(0).getScheduleName(), schedule1.getScheduleName());
        assertNotNull(response.get(0).getSinceUpdate());
        assertNotNull(response.get(0).getThumbnailUrl());
        assertEquals(response.get(0).getAuthor().getNickname(), member1.getNickname());
    }

    @Test
    @DisplayName("TravelSchedule 를 ScheduleInfoResponse 로 변경 시 썸네일 없는 경우")
    void createScheduleInfoResponseWithoutThumbnail(){
        // given
        List<TravelSchedule> travelScheduleList = new ArrayList<>(List.of(schedule1));
        Page<TravelSchedule> schedulePage = PageUtil.createPage(travelScheduleList, PageUtil.schedulePageable(1), travelScheduleList.size());
        travelPlace1.getTravelImageList().get(0).setThumbnail(false);

        // when
        List<ScheduleInfoResponse> response = scheduleService.createScheduleInfoResponse(schedulePage, member1.getUserId());

        // then
        assertEquals(response.get(0).getScheduleName(), schedule1.getScheduleName());
        assertNotNull(response.get(0).getSinceUpdate());
        assertNull(response.get(0).getThumbnailUrl());
        assertEquals(response.get(0).getAuthor().getNickname(), member1.getNickname());
    }

    @Test
    @DisplayName("TravelSchedule 를 ScheduleInfoResponse 로 변경 시 작성자가 없어 예외 발생")
    void createScheduleInfoResponse_notFoundException(){
        // given
        List<TravelSchedule> travelScheduleList = new ArrayList<>(List.of(schedule1));
        Page<TravelSchedule> schedulePage = PageUtil.createPage(travelScheduleList, PageUtil.schedulePageable(1), travelScheduleList.size());
        for(TravelAttendee attendee : schedule1.getTravelAttendeeList()){
            attendee.setRole(AttendeeRole.GUEST);
        }

        // when
        DataNotFoundException fail = assertThrows(DataNotFoundException.class, () -> scheduleService.createScheduleInfoResponse(schedulePage, member1.getUserId()));

        // then
        assertEquals(fail.getHttpStatus(), ErrorCode.AUTHOR_NOT_FOUND.getStatus());
        assertEquals(fail.getMessage(), ErrorCode.AUTHOR_NOT_FOUND.getMessage());

    }

    @Test
    @DisplayName("TravelSchedule 를 ScheduleInfoResponse 로 변경 시 접근 권한이 없어 예외 발생")
    void createScheduleInfoResponse_forbiddenScheduleException(){
        // given
        List<TravelSchedule> travelScheduleList = new ArrayList<>(List.of(schedule3));
        Page<TravelSchedule> schedulePage = PageUtil.createPage(travelScheduleList, PageUtil.schedulePageable(1), travelScheduleList.size());

        // when
        ForbiddenScheduleException fail = assertThrows(ForbiddenScheduleException.class, () -> scheduleService.createScheduleInfoResponse(schedulePage, member2.getUserId()));

        // then
        assertEquals(fail.getHttpStatus(), ErrorCode.FORBIDDEN_ACCESS_SCHEDULE.getStatus());
        assertEquals(fail.getMessage(), ErrorCode.FORBIDDEN_ACCESS_SCHEDULE.getMessage());

    }

    @Test
    @DisplayName("작성자 조회해서 AuthorDTO 생성")
    void createAuthorDTO(){
        // given
        // when
        AuthorDTO response = scheduleService.createAuthorDTO(schedule1);

        // then
        assertEquals(response.getNickname(), member1.getNickname());
        assertEquals(response.getProfileUrl(), member1.getProfileImage().getS3ObjectUrl());

    }

    @Test
    @DisplayName("작성자 조회해서 AuthorDTO 생성 시 작성자가 없어 예외 발생")
    void createAuthorDTO_notFoundException(){
        // given
        for(TravelAttendee attendee : schedule1.getTravelAttendeeList()){
            attendee.setRole(AttendeeRole.GUEST);
        }

        // when
        DataNotFoundException fail = assertThrows(DataNotFoundException.class, () -> scheduleService.createAuthorDTO(schedule1));

        // then
        assertEquals(fail.getHttpStatus(), ErrorCode.AUTHOR_NOT_FOUND.getStatus());
        assertEquals(fail.getMessage(), ErrorCode.AUTHOR_NOT_FOUND.getMessage());

    }


    @Test
    @DisplayName("썸네일 조회")
    void getThumbnailUrl(){
        // given
        // when
        String response = scheduleService.getThumbnailUrl(schedule1);

        // then
        System.out.println(response);
        assertNotNull(response);
    }

    @Test
    @DisplayName("썸네일 조회 시 썸네일 이미지 없는 경우")
    void getThumbnailUrlNoThumbnailImage(){
        // given
        travelPlace1.getTravelImageList().get(0).setThumbnail(false);

        // when
        String response = scheduleService.getThumbnailUrl(schedule1);

        // then
        assertNull(response);
    }


    @Test
    @DisplayName("썸네일 조회 시 저장된 이미지 없는 경우")
    void getThumbnailUrlNoImageData(){
        // given
        travelPlace1.setTravelImageList(new ArrayList<>());

        // when
        String response = scheduleService.getThumbnailUrl(schedule1);

        // then
        assertNull(response);
    }


    @Test
    @DisplayName("일정 생성")
    void createSchedule(){
        // given
        String userId = "test";
        CreateScheduleRequest request = createScheduleRequest();

        when(travelScheduleRepository.save(any())).thenReturn(schedule1);
        when(memberRepository.findByUserId(any())).thenReturn(Optional.of(member1));

        // when
        CreateScheduleResponse response = scheduleService.createSchedule(request, userId);

        // then
        verify(travelAttendeeRepository, times(1)).save(any(TravelAttendee.class));
        assertEquals(response.getScheduleId(), schedule1.getScheduleId());

    }

    @Test
    @DisplayName("일정 생성 시 저장된 사용자 정보 없어 DataNotFoundException 발생")
    void createSchedule_CustomUsernameDataNotFoundException(){
        // given
        CreateScheduleRequest request = createScheduleRequest();

        when(travelScheduleRepository.save(any())).thenReturn(schedule1);
        when(memberRepository.findByUserId(any())).thenReturn(Optional.empty());

        // when
        DataNotFoundException fail = assertThrows(DataNotFoundException.class, () -> scheduleService.createSchedule(request, "test"));

        // then
        assertEquals(fail.getHttpStatus(), ErrorCode.USER_NOT_FOUND.getStatus());
        assertEquals(fail.getMessage(), ErrorCode.USER_NOT_FOUND.getMessage());

    }

    @Test
    @DisplayName("일정 상세 조회")
    void getScheduleDetail(){
        // given
        List<TravelPlace> placeList = new ArrayList<>(List.of(travelPlace1, travelPlace2));

        Pageable pageable = PageUtil.defaultPageable(1);

        when(travelScheduleRepository.findByScheduleId(any())).thenReturn(Optional.of(schedule1));
        when(travelPlaceRepository.findAllByAreaData(any(), anyString(), anyString(), anyString()))
                .thenReturn(PageUtil.createPage(placeList, pageable, 1));

        // when
        ScheduleDetailResponse response = scheduleService.getScheduleDetail(schedule1.getScheduleId(), 1);

        // then
        assertEquals(response.getScheduleName(), schedule1.getScheduleName());
        assertEquals(response.getCreatedAt(), schedule1.getCreatedAt());
        assertEquals(response.getPlaceList().getTotalElements(), placeList.size());
        assertEquals(response.getPlaceList().getContent().get(0).getPlaceName(), placeList.get(0).getPlaceName());
        assertNotNull(response.getPlaceList().getContent().get(0).getThumbnailUrl());
    }

    @Test
    @DisplayName("일정 상세 조회 시 여행지 데이터 없는 경우")
    void getScheduleDetailWithoutData(){
        // given
        Pageable pageable = PageUtil.defaultPageable(1);

        when(travelScheduleRepository.findByScheduleId(any())).thenReturn(Optional.of(schedule1));
        when(travelPlaceRepository.findAllByAreaData(any(), anyString(), anyString(), anyString()))
                .thenReturn(PageUtil.createPage(new ArrayList<>(), pageable, 0));

        // when
        ScheduleDetailResponse response = scheduleService.getScheduleDetail(schedule1.getScheduleId(), 1);

        // then
        assertEquals(response.getScheduleName(), schedule1.getScheduleName());
        assertEquals(response.getCreatedAt(), schedule1.getCreatedAt());
        assertEquals(response.getPlaceList().getTotalElements(), 0);
        assertTrue(response.getPlaceList().getContent().isEmpty());
    }

    @Test
    @DisplayName("일정 상세 조회 시 일정을 찾을 수 없어 DataNotFoundException 발생")
    void getScheduleDetail_dataNotFoundException(){
        // given
        when(travelScheduleRepository.findByScheduleId(any())).thenReturn(Optional.empty());

        // when
        DataNotFoundException fail = assertThrows(DataNotFoundException.class, () -> scheduleService.getScheduleDetail(0L, 1));

        // then
        assertEquals(fail.getHttpStatus(), ErrorCode.SCHEDULE_NOT_FOUND.getStatus());
        assertEquals(fail.getMessage(), ErrorCode.SCHEDULE_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("일정 수정")
    void updateSchedule(){
        String userId = member1.getUserId();
        Long scheduleId = schedule1.getScheduleId();

        RouteRequest routeRequest1 = createRouteRequest(1, travelPlace1.getPlaceId());
        RouteRequest routeRequest2 = createRouteRequest(2, travelPlace2.getPlaceId());
        UpdateScheduleRequest updateScheduleRequest = createUpdateScheduleRequest(new ArrayList<>(List.of(routeRequest1, routeRequest2)));

        when(travelScheduleRepository.findByScheduleId(scheduleId)).thenReturn(Optional.of(schedule1));
        when(travelPlaceRepository.findByPlaceId(travelPlace1.getPlaceId())).thenReturn(Optional.of(travelPlace1));
        when(travelPlaceRepository.findByPlaceId(travelPlace2.getPlaceId())).thenReturn(Optional.of(travelPlace2));

        // when
        assertDoesNotThrow(() -> scheduleService.updateSchedule(userId, scheduleId, updateScheduleRequest));

        // then
        assertEquals(schedule1.getTravelRouteList().size(), 2);
        assertEquals(schedule1.getScheduleName(), updateScheduleRequest.getScheduleName());
        assertEquals(schedule1.getStartDate(), updateScheduleRequest.getStartDate());
        assertEquals(schedule1.getTravelRouteList().get(0).getTravelPlace().getPlaceName(), travelPlace1.getPlaceName());
    }

    @Test
    @DisplayName("일정 수정 중 여행 루트 삭제에서 기존에 저장된 여행 루트가 없을 경우")
    void updateScheduleNoSavedTravelRouteList(){
        // given
        String userId = member1.getUserId();
        Long scheduleId = schedule2.getScheduleId();

        RouteRequest routeRequest1 = createRouteRequest(1, travelPlace1.getPlaceId());
        RouteRequest routeRequest2 = createRouteRequest(2, travelPlace2.getPlaceId());
        UpdateScheduleRequest updateScheduleRequest = createUpdateScheduleRequest(new ArrayList<>(List.of(routeRequest1, routeRequest2)));

        when(travelScheduleRepository.findByScheduleId(scheduleId)).thenReturn(Optional.of(schedule2));
        when(travelPlaceRepository.findByPlaceId(travelPlace1.getPlaceId())).thenReturn(Optional.of(travelPlace1));
        when(travelPlaceRepository.findByPlaceId(travelPlace2.getPlaceId())).thenReturn(Optional.of(travelPlace2));

        // when
        assertDoesNotThrow(() -> scheduleService.updateSchedule(userId, scheduleId, updateScheduleRequest));

        // then
        assertEquals(schedule2.getTravelRouteList().size(), 2);
        assertEquals(schedule2.getScheduleName(), updateScheduleRequest.getScheduleName());
        assertEquals(schedule2.getStartDate(), updateScheduleRequest.getStartDate());
        assertEquals(schedule2.getTravelRouteList().get(0).getTravelPlace().getPlaceName(), travelPlace1.getPlaceName());
    }

    @Test
    @DisplayName("일정의 여행 루트 수정 시 기존에 저장된 여행 루트가 존재하는 경우")
    void updateTravelRouteInSchedule(){
        RouteRequest routeRequest1 = createRouteRequest(1, travelPlace1.getPlaceId());
        RouteRequest routeRequest2 = createRouteRequest(2, travelPlace2.getPlaceId());
        UpdateScheduleRequest updateScheduleRequest = createUpdateScheduleRequest(new ArrayList<>(List.of(routeRequest1, routeRequest2)));

        when(travelPlaceRepository.findByPlaceId(travelPlace1.getPlaceId())).thenReturn(Optional.of(travelPlace1));
        when(travelPlaceRepository.findByPlaceId(travelPlace2.getPlaceId())).thenReturn(Optional.of(travelPlace2));

        // when
        assertDoesNotThrow(() -> scheduleService.updateTravelRouteInSchedule(schedule1, updateScheduleRequest.getTravelRoute()));

        // then
        assertEquals(schedule1.getTravelRouteList().size(), 2);
        assertEquals(schedule1.getTravelRouteList().get(0).getTravelPlace().getPlaceName(), travelPlace1.getPlaceName());
    }


    @Test
    @DisplayName("일정 수정 시 일정 데이터 없어 예외 발생")
    void updateScheduleNoSchedule_dataNotFoundException(){
        // given
        String userId = member1.getUserId();
        Long scheduleId = schedule1.getScheduleId();

        RouteRequest routeRequest1 = createRouteRequest(1, travelPlace1.getPlaceId());
        RouteRequest routeRequest2 = createRouteRequest(2, travelPlace2.getPlaceId());
        UpdateScheduleRequest updateScheduleRequest = createUpdateScheduleRequest(new ArrayList<>(List.of(routeRequest1, routeRequest2)));

        when(travelScheduleRepository.findByScheduleId(scheduleId)).thenReturn(Optional.empty());

        // when
        DataNotFoundException fail = assertThrows(DataNotFoundException.class, () -> scheduleService.updateSchedule(userId, scheduleId, updateScheduleRequest));

        // then
        assertEquals(fail.getHttpStatus(), ErrorCode.SCHEDULE_NOT_FOUND.getStatus());
        assertEquals(fail.getMessage(), ErrorCode.SCHEDULE_NOT_FOUND.getMessage());

    }

    @Test
    @DisplayName("일정 수정 시 요청 사용자에게 접근 권한이 없어 예외 발생")
    void updateScheduleForbiddenAccess_forbiddenScheduleException(){
        // given
        String userId = member2.getUserId();
        Long scheduleId = schedule3.getScheduleId();

        RouteRequest routeRequest1 = createRouteRequest(1, travelPlace1.getPlaceId());
        RouteRequest routeRequest2 = createRouteRequest(2, travelPlace2.getPlaceId());
        UpdateScheduleRequest updateScheduleRequest = createUpdateScheduleRequest(new ArrayList<>(List.of(routeRequest1, routeRequest2)));

        when(travelScheduleRepository.findByScheduleId(scheduleId)).thenReturn(Optional.of(schedule3));

        // when
        ForbiddenScheduleException fail = assertThrows(ForbiddenScheduleException.class, () -> scheduleService.updateSchedule(userId, scheduleId, updateScheduleRequest));

        // then
        assertEquals(fail.getHttpStatus(), ErrorCode.FORBIDDEN_ACCESS_SCHEDULE.getStatus());
        assertEquals(fail.getMessage(), ErrorCode.FORBIDDEN_ACCESS_SCHEDULE.getMessage());

    }

    @Test
    @DisplayName("일정 수정 시 요청 사용자에게 수정 권한이 없어 예외 발생")
    void updateScheduleForbiddenEdit_forbiddenScheduleException(){
        // given
        String userId = member2.getUserId();
        Long scheduleId = schedule1.getScheduleId();

        RouteRequest routeRequest1 = createRouteRequest(1, travelPlace1.getPlaceId());
        RouteRequest routeRequest2 = createRouteRequest(2, travelPlace2.getPlaceId());
        UpdateScheduleRequest updateScheduleRequest = createUpdateScheduleRequest(new ArrayList<>(List.of(routeRequest1, routeRequest2)));

        when(travelScheduleRepository.findByScheduleId(scheduleId)).thenReturn(Optional.of(schedule1));

        // when
        ForbiddenScheduleException fail = assertThrows(ForbiddenScheduleException.class, () -> scheduleService.updateSchedule(userId, scheduleId, updateScheduleRequest));

        // then
        assertEquals(fail.getHttpStatus(), ErrorCode.FORBIDDEN_EDIT_SCHEDULE.getStatus());
        assertEquals(fail.getMessage(), ErrorCode.FORBIDDEN_EDIT_SCHEDULE.getMessage());
    }


    @Test
    @DisplayName("일정 수정 시 여행 루트에 저장된 여행지가 없어 예외 발생")
    void updateScheduleNoTravelPlace_dataNotFoundException(){
        // given
        String userId = member1.getUserId();
        Long scheduleId = schedule1.getScheduleId();

        RouteRequest routeRequest1 = createRouteRequest(1, travelPlace1.getPlaceId());
        RouteRequest routeRequest2 = createRouteRequest(2, travelPlace2.getPlaceId());
        UpdateScheduleRequest updateScheduleRequest = createUpdateScheduleRequest(new ArrayList<>(List.of(routeRequest1, routeRequest2)));

        when(travelScheduleRepository.findByScheduleId(scheduleId)).thenReturn(Optional.of(schedule1));
        when(travelPlaceRepository.findByPlaceId(anyLong())).thenReturn(Optional.empty());

        // when
        DataNotFoundException fail = assertThrows(DataNotFoundException.class, () -> scheduleService.updateSchedule(userId, scheduleId, updateScheduleRequest));

        // then
        assertEquals(fail.getHttpStatus(), ErrorCode.PLACE_NOT_FOUND.getStatus());
        assertEquals(fail.getMessage(), ErrorCode.PLACE_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("참가자 정보 조회")
    void getAttendeeInfo_containsAttendees(){
        // given
        // when
        TravelAttendee response = scheduleService.getAttendeeInfo(schedule1, member1.getUserId());

        // then
        assertEquals(response.getMember().getUserId(), member1.getUserId());
        assertEquals(response.getTravelSchedule().getScheduleName(), schedule1.getScheduleName());
    }

    @Test
    @DisplayName("참가자 정보 조회 시 데이터 존재하지 않는 경우")
    void getAttendeeInfo_notContainsAttendees(){
        // given
        // when
        ForbiddenScheduleException fail = assertThrows(ForbiddenScheduleException.class, () -> scheduleService.getAttendeeInfo(schedule3, member2.getUserId()));

        // then
        assertEquals(fail.getHttpStatus(), ErrorCode.FORBIDDEN_ACCESS_SCHEDULE.getStatus());
        assertEquals(fail.getMessage(), ErrorCode.FORBIDDEN_ACCESS_SCHEDULE.getMessage());

    }

    @Test
    @DisplayName("일정 수정 사용자 권한 체크 ALL")
    void checkScheduleEditPermissionALL(){
        // given
        attendee1.setPermission(AttendeePermission.ALL);

        // when, then
        assertDoesNotThrow(() -> scheduleService.checkScheduleEditPermission(attendee1));
    }

    @Test
    @DisplayName("일정 수정 사용자 권한 체크 EDIT")
    void checkScheduleEditPermissionEdit(){
        // given
        attendee1.setPermission(AttendeePermission.EDIT);

        // when
        // then
        assertDoesNotThrow(() -> scheduleService.checkScheduleEditPermission(attendee1));
    }

    @Test
    @DisplayName("일정 수정 사용자 권한 체크 중 CHAT 권한으로 예외 발생")
    void checkScheduleEditPermissionCHAT_forbiddenScheduleException(){
        // given
        attendee1.setPermission(AttendeePermission.CHAT);

        // when
        ForbiddenScheduleException fail = assertThrows(ForbiddenScheduleException.class, () -> scheduleService.checkScheduleEditPermission(attendee1));

        // then
        assertEquals(fail.getHttpStatus(), ErrorCode.FORBIDDEN_EDIT_SCHEDULE.getStatus());
        assertEquals(fail.getMessage(), ErrorCode.FORBIDDEN_EDIT_SCHEDULE.getMessage());
    }

    @Test
    @DisplayName("일정 수정 사용자 권한 체크 시 READ 권한으로 예외 발생")
    void checkScheduleEditPermissionREAD_forbiddenScheduleException(){
        // given
        attendee1.setPermission(AttendeePermission.READ);

        // when
        ForbiddenScheduleException fail = assertThrows(ForbiddenScheduleException.class, () -> scheduleService.checkScheduleEditPermission(attendee1));

        // then
        assertEquals(fail.getHttpStatus(), ErrorCode.FORBIDDEN_EDIT_SCHEDULE.getStatus());
        assertEquals(fail.getMessage(), ErrorCode.FORBIDDEN_EDIT_SCHEDULE.getMessage());
    }

    @Test
    @DisplayName("일정 삭제")
    void deleteSchedule(){
        // given
        ChatMessage message1 = createChatMessage("chat1", schedule1.getScheduleId(), member1, "hello1");
        ChatMessage message2 = createChatMessage("chat2", schedule1.getScheduleId(), member1, "hello2");
        ChatMessage message3 = createChatMessage("chat3", schedule1.getScheduleId(), member2, "hello3");
        List<ChatMessage> chatMessages = new ArrayList<>(List.of(message1, message2, message3));

        when(travelAttendeeRepository.findByTravelSchedule_ScheduleIdAndMember_UserId(anyLong(), anyString())).thenReturn(Optional.of(attendee1));
        when(chatMessageRepository.findAllByScheduleId(anyLong())).thenReturn(chatMessages);

        // when
        assertDoesNotThrow(() -> scheduleService.deleteSchedule(schedule1.getScheduleId(), member1.getUserId()));

        // then
        verify(chatMessageRepository, times(1)).deleteAllByScheduleId(schedule1.getScheduleId());
    }

    @Test
    @DisplayName("일정 삭제 시 채팅 메시지 없는 경우")
    void deleteScheduleNoChatMessageData(){
        // given
        when(travelAttendeeRepository.findByTravelSchedule_ScheduleIdAndMember_UserId(anyLong(), anyString())).thenReturn(Optional.of(attendee1));
        when(chatMessageRepository.findAllByScheduleId(anyLong())).thenReturn(new ArrayList<>());

        // when
        assertDoesNotThrow(() -> scheduleService.deleteSchedule(schedule1.getScheduleId(), member1.getUserId()));

        // then
        verify(chatMessageRepository, times(0)).deleteAllByScheduleId(schedule1.getScheduleId());
    }

    @Test
    @DisplayName("일정 삭제 시 작성자가 아닌 사용자가 삭제 요청으로 인해 예외 발생")
    void deleteScheduleNotAuthor_forbiddenScheduleException(){
        // given
        when(travelAttendeeRepository.findByTravelSchedule_ScheduleIdAndMember_UserId(anyLong(), anyString())).thenReturn(Optional.of(attendee2));

        // when
        ForbiddenScheduleException fail = assertThrows(ForbiddenScheduleException.class, () -> scheduleService.deleteSchedule(schedule1.getScheduleId(), member2.getUserId()));

        assertEquals(fail.getHttpStatus(), ErrorCode.FORBIDDEN_DELETE_SCHEDULE.getStatus());
        assertEquals(fail.getMessage(), ErrorCode.FORBIDDEN_DELETE_SCHEDULE.getMessage());
    }

    @Test
    @DisplayName("일정 id를 통해 채팅 메시지 삭제")
    void deleteChatMessageByScheduleId(){
        // given
        ChatMessage message1 = createChatMessage("chat1", schedule1.getScheduleId(), member1, "hello1");
        ChatMessage message2 = createChatMessage("chat2", schedule1.getScheduleId(), member1, "hello2");
        ChatMessage message3 = createChatMessage("chat3", schedule1.getScheduleId(), member2, "hello3");
        List<ChatMessage> chatMessages = new ArrayList<>(List.of(message1, message2, message3));

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


    @Test
    @DisplayName("저장된 사용자 정보 조회")
    void getMemberByUserId(){
        // given
        String userId = "member1";

        when(memberRepository.findByUserId(any())).thenReturn(Optional.of(member1));

        // when
        Member response = scheduleService.getMemberByUserId(userId);

        // then
        assertEquals(response.getUserId(), userId);
        assertEquals(response.getEmail(), member1.getEmail());
        assertEquals(response.getNickname(), member1.getNickname());

    }

    @Test
    @DisplayName("저장된 사용자 정보 조회 시 데이터 찾을 수 없어 예외 발생")
    void getMember_ByUserId_dataNotFoundException(){
        // given
        when(memberRepository.findByUserId(any())).thenReturn(Optional.empty());

        // when
        DataNotFoundException fail = assertThrows(DataNotFoundException.class, () -> scheduleService.getMemberByUserId("notUser"));

        // then
        assertEquals(fail.getHttpStatus(), ErrorCode.USER_NOT_FOUND.getStatus());
        assertEquals(fail.getMessage(), ErrorCode.USER_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("저장된 일정 조회")
    void getScheduleByScheduleId(){
        // given
        when(travelScheduleRepository.findByScheduleId(any())).thenReturn(Optional.of(schedule1));

        // when
        TravelSchedule response = scheduleService.getScheduleByScheduleId(schedule1.getScheduleId());

        // then
        assertEquals(response.getScheduleName(), schedule1.getScheduleName());
        assertEquals(response.getStartDate(), schedule1.getStartDate());
        assertEquals(response.getEndDate(), schedule1.getEndDate());
    }

    @Test
    @DisplayName("저장된 일정 조회 시 데이터 찾을 수 없어 예외 발생")
    void getScheduleBySchedule_Id_dataNotFoundException(){
        // given
        when(travelScheduleRepository.findByScheduleId(any())).thenReturn(Optional.empty());

        // when
        DataNotFoundException fail = assertThrows(DataNotFoundException.class, () -> scheduleService.getScheduleByScheduleId(0L));

        // then
        assertEquals(fail.getHttpStatus(), ErrorCode.SCHEDULE_NOT_FOUND.getStatus());
        assertEquals(fail.getMessage(), ErrorCode.SCHEDULE_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("저장된 여행지 조회")
    void getPlaceByPlaceId(){
        // given
        Long placeId = travelPlace1.getPlaceId();

        when(travelPlaceRepository.findByPlaceId(anyLong())).thenReturn(Optional.of(travelPlace1));

        // when
        TravelPlace response = scheduleService.getPlaceByPlaceId(placeId);

        // then
        assertEquals(response.getPlaceId(), placeId);
        assertEquals(response.getPlaceName(), travelPlace1.getPlaceName());

    }

    @Test
    @DisplayName("저장된 여행지 데이터 조회 시 데이터 존재하지 않아 예외 발생")
    void getPlaceByPlaceId_dataNotFoundException(){
        // given
        Long placeId = travelPlace1.getPlaceId();

        when(travelPlaceRepository.findByPlaceId(anyLong())).thenReturn(Optional.empty());

        // when
        DataNotFoundException fail = assertThrows(DataNotFoundException.class, () -> scheduleService.getPlaceByPlaceId(placeId));

        // then
        assertEquals(fail.getHttpStatus(), ErrorCode.PLACE_NOT_FOUND.getStatus());
        assertEquals(fail.getMessage(), ErrorCode.PLACE_NOT_FOUND.getMessage());

    }

}
