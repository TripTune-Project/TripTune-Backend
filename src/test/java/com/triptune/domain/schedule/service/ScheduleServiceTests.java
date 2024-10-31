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
import com.triptune.domain.schedule.dto.response.RouteResponse;
import com.triptune.domain.schedule.dto.response.ScheduleDetailResponse;
import com.triptune.domain.schedule.dto.response.ScheduleInfoResponse;
import com.triptune.domain.schedule.entity.TravelAttendee;
import com.triptune.domain.schedule.entity.TravelRoute;
import com.triptune.domain.schedule.entity.TravelSchedule;
import com.triptune.domain.schedule.enumclass.AttendeePermission;
import com.triptune.domain.schedule.enumclass.AttendeeRole;
import com.triptune.domain.schedule.exception.ForbiddenScheduleException;
import com.triptune.domain.schedule.repository.TravelAttendeeRepository;
import com.triptune.domain.schedule.repository.TravelRouteRepository;
import com.triptune.domain.schedule.repository.TravelScheduleRepository;
import com.triptune.domain.travel.dto.response.PlaceResponse;
import com.triptune.domain.travel.entity.TravelImage;
import com.triptune.domain.travel.entity.TravelPlace;
import com.triptune.domain.travel.repository.TravelPlacePlaceRepository;
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
public class ScheduleServiceTests extends ScheduleTest {

    @InjectMocks
    private ScheduleService scheduleService;

    @Mock
    private TravelScheduleRepository travelScheduleRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private TravelAttendeeRepository travelAttendeeRepository;

    @Mock
    private TravelPlacePlaceRepository travelPlaceRepository;

    @Mock
    private TravelRouteRepository travelRouteRepository;

    private TravelSchedule schedule1;
    private TravelSchedule schedule2;
    private TravelPlace travelPlace1;
    private TravelPlace travelPlace2;
    private Member member1;
    private Member member2;
    private Member member3;
    private TravelAttendee attentee1;

    @BeforeEach
    void setUp(){
        Country country = createCountry();
        City city = createCity(country);
        District district = createDistrict(city, "중구");
        ApiCategory apiCategory = createApiCategory();
        travelPlace1 = createTravelPlace(1L, country, city, district, apiCategory);
        File file1 = createFile("test1", true);
        File file2 = createFile("test2", false);
        TravelImage travelImage1 = createTravelImage(travelPlace1, file1);
        TravelImage travelImage2 = createTravelImage(travelPlace1, file2);
        List<TravelImage> travelImageList1 = new ArrayList<>(List.of(travelImage1, travelImage2));
        travelPlace1.setTravelImageList(travelImageList1);

        travelPlace2 = createTravelPlace(2L, country, city, district, apiCategory);
        TravelImage travelImage3 = createTravelImage(travelPlace2, file1);
        TravelImage travelImage4 = createTravelImage(travelPlace2, file2);
        List<TravelImage> travelImageList2 = new ArrayList<>(List.of(travelImage3, travelImage4));
        travelPlace2.setTravelImageList(travelImageList2);

        member1 = createMember(1L, "member1");
        member2 = createMember(2L, "member2");
        member3 = createMember(3L, "member3");
        ProfileImage member1Image = createProfileImage(1L, "member1Image");
        ProfileImage member2Image = createProfileImage(2L, "member2Image");
        ProfileImage member3Image = createProfileImage(3L, "member3Image");
        member1.setProfileImage(member1Image);
        member2.setProfileImage(member2Image);
        member3.setProfileImage(member3Image);

        schedule1 = createTravelSchedule(1L, "테스트1");
        schedule2 = createTravelSchedule(2L, "테스트2");

        attentee1 = createTravelAttendee(member1, schedule1, AttendeeRole.AUTHOR, AttendeePermission.ALL);
        TravelAttendee attendee2 = createTravelAttendee(member2, schedule1, AttendeeRole.GUEST, AttendeePermission.READ);
        TravelAttendee attendee3 = createTravelAttendee(member3, schedule2, AttendeeRole.AUTHOR, AttendeePermission.ALL);
        schedule1.setTravelAttendeeList(new ArrayList<>(List.of(attentee1, attendee2)));
        schedule2.setTravelAttendeeList(new ArrayList<>(List.of(attendee3)));

        TravelRoute route1 = createTravelRoute(schedule1, travelPlace1, 1);
        TravelRoute route2 = createTravelRoute(schedule1, travelPlace1, 2);
        TravelRoute route3 = createTravelRoute(schedule1, travelPlace2, 3);
        schedule1.setTravelRouteList(new ArrayList<>(List.of(route1, route2, route3)));
        schedule2.setTravelRouteList(new ArrayList<>());
    }


    @Test
    @DisplayName("getSchedules(): 내 일정 목록 조회")
    void getSchedules(){
        // given
        int page = 1;
        String userId = "member1";

        Pageable pageable = PageUtil.schedulePageable(1);

        List<TravelSchedule> schedules = new ArrayList<>(List.of(schedule1, schedule2));
        Page<TravelSchedule> schedulePage = PageUtil.createPage(schedules, pageable, schedules.size());

        when(memberRepository.findByUserId(any())).thenReturn(Optional.of(createMember(1L, userId)));
        when(travelScheduleRepository.findTravelSchedulesByAttendee(pageable, 1L)).thenReturn(schedulePage);

        // when
        SchedulePageResponse<ScheduleInfoResponse> response = scheduleService.getSchedules(page, userId);

        // then
        List<ScheduleInfoResponse> content = response.getContent();
        assertEquals(response.getTotalElements(), 2);
        assertEquals(response.getTotalSharedElements(), 1);
        assertEquals(content.get(0).getScheduleName(), schedule1.getScheduleName());
        assertNotNull(content.get(0).getSinceUpdate());
        assertNotNull(content.get(0).getThumbnailUrl());
        assertEquals(content.get(0).getAuthor().getUserId(), member1.getUserId());
    }

    @Test
    @DisplayName("getSchedules(): 내 일정 목록 조회 시 공유된 일정이 없는 경우")
    void getSchedulesNotShared(){
        // given
        int page = 1;
        String userId = "member1";

        Pageable pageable = PageUtil.schedulePageable(1);

        schedule1.setTravelAttendeeList(new ArrayList<>(List.of(createTravelAttendee(member1, schedule1, AttendeeRole.AUTHOR, AttendeePermission.ALL))));

        List<TravelSchedule> schedules = new ArrayList<>(List.of(schedule1, schedule2));
        Page<TravelSchedule> schedulePage = PageUtil.createPage(schedules, pageable, schedules.size());

        when(memberRepository.findByUserId(any())).thenReturn(Optional.of(createMember(1L, userId)));
        when(travelScheduleRepository.findTravelSchedulesByAttendee(pageable, 1L)).thenReturn(schedulePage);

        // when
        SchedulePageResponse<ScheduleInfoResponse> response = scheduleService.getSchedules(page, userId);

        // then
        List<ScheduleInfoResponse> content = response.getContent();
        assertEquals(response.getTotalElements(), 2);
        assertEquals(response.getTotalSharedElements(), 0);
        assertEquals(content.get(0).getScheduleName(), schedule1.getScheduleName());
        assertNotNull(content.get(0).getSinceUpdate());
        assertNotNull(content.get(0).getThumbnailUrl());
        assertEquals(content.get(0).getAuthor().getUserId(), member1.getUserId());
    }


    @Test
    @DisplayName("getSchedules(): 내 일정 목록 조회 시 사용자 데이터가 없는 경우")
    void getSchedulesNoUserData(){
        // given
        int page = 1;
        String userId = "member1";

        when(memberRepository.findByUserId(any())).thenReturn(Optional.empty());

        // when
        DataNotFoundException fail = assertThrows(DataNotFoundException.class, () -> scheduleService.getSchedules(page, userId));

        // then
        assertEquals(fail.getHttpStatus(), ErrorCode.USER_NOT_FOUND.getStatus());
        assertEquals(fail.getMessage(), ErrorCode.USER_NOT_FOUND.getMessage());

    }

    @Test
    @DisplayName("getSchedules(): 내 일정 목록 조회 시 일정 데이터 없는 경우")
    void getSchedulesNoScheduleData(){
        // given
        int page = 1;
        String userId = "member1";

        Pageable pageable = PageUtil.schedulePageable(1);

        Page<TravelSchedule> emptySchedulePage = PageUtil.createPage(new ArrayList<>(), pageable, 0);

        when(memberRepository.findByUserId(any())).thenReturn(Optional.of(createMember(1L, userId)));
        when(travelScheduleRepository.findTravelSchedulesByAttendee(pageable, 1L)).thenReturn(emptySchedulePage);

        // when
        SchedulePageResponse<ScheduleInfoResponse> response = scheduleService.getSchedules(page, userId);

        // then
        assertEquals(response.getTotalElements(), 0);
        assertTrue(response.getContent().isEmpty());
        verify(travelRouteRepository, times(0)).findAllByTravelSchedule_ScheduleId(any(), any());
    }

    @Test
    @DisplayName("getSchedules(): 내 일정 목록 조회 시 이미지 썸네일 데이터 없는 경우")
    void getSchedulesNoImageThumbnail(){
        // given
        int page = 1;
        String userId = "member1";

        Pageable pageable = PageUtil.schedulePageable(1);

        travelPlace1.getTravelImageList().get(0).getFile().setThumbnail(false);

        List<TravelSchedule> schedules = new ArrayList<>(List.of(schedule1, schedule2));
        Page<TravelSchedule> schedulePage = PageUtil.createPage(schedules, pageable, schedules.size());

        when(memberRepository.findByUserId(any())).thenReturn(Optional.of(createMember(1L, userId)));
        when(travelScheduleRepository.findTravelSchedulesByAttendee(pageable, 1L)).thenReturn(schedulePage);

        // when
        SchedulePageResponse<ScheduleInfoResponse> response = scheduleService.getSchedules(page, userId);

        // then
        List<ScheduleInfoResponse> content = response.getContent();
        assertEquals(response.getTotalElements(), 2);
        assertEquals(content.get(0).getScheduleName(), "테스트1");
        assertNotNull(content.get(0).getSinceUpdate());
        assertNull(content.get(0).getThumbnailUrl());

    }


    @Test
    @DisplayName("getSchedules(): 내 일정 목록 조회 시 이미지 데이터 없는 경우")
    void getSchedulesNoImageData(){
        // given
        int page = 1;
        String userId = "member1";

        Pageable pageable = PageUtil.schedulePageable(1);

        travelPlace1.setTravelImageList(new ArrayList<>());

        List<TravelSchedule> schedules = new ArrayList<>(List.of(schedule1, schedule2));
        Page<TravelSchedule> schedulePage = PageUtil.createPage(schedules, pageable, schedules.size());

        when(memberRepository.findByUserId(any())).thenReturn(Optional.of(createMember(1L, userId)));
        when(travelScheduleRepository.findTravelSchedulesByAttendee(pageable, 1L)).thenReturn(schedulePage);

        // when
        SchedulePageResponse<ScheduleInfoResponse> response = scheduleService.getSchedules(page, userId);

        // then
        List<ScheduleInfoResponse> content = response.getContent();
        assertEquals(response.getTotalElements(), 2);
        assertEquals(content.get(0).getScheduleName(), "테스트1");
        assertNotNull(content.get(0).getSinceUpdate());
        assertNull(content.get(0).getThumbnailUrl());

    }

    @Test
    @DisplayName("convertToScheduleOverviewResponse(): TravelSchedule 를 TravelOverviewResponse 로 변경")
    void convertToScheduleInfoResponse(){
        // given
        // when
        ScheduleInfoResponse response = scheduleService.convertToScheduleInfoResponse(schedule1);

        // then
        assertEquals(response.getScheduleName(), schedule1.getScheduleName());
        assertNotNull(response.getSinceUpdate());
        assertNotNull(response.getThumbnailUrl());
        assertEquals(response.getAuthor().getUserId(), member1.getUserId());
    }

    @Test
    @DisplayName("convertToScheduleOverviewResponse(): TravelSchedule 를 TravelOverviewResponse 로 변경 시 썸네일 없는 경우")
    void convertToScheduleInfoResponseWithoutThumbnail(){
        // given
        travelPlace1.getTravelImageList().get(0).getFile().setThumbnail(false);

        // when
        ScheduleInfoResponse response = scheduleService.convertToScheduleInfoResponse(schedule1);

        // then
        assertEquals(response.getScheduleName(), schedule1.getScheduleName());
        assertNotNull(response.getSinceUpdate());
        assertNull(response.getThumbnailUrl());
        assertEquals(response.getAuthor().getUserId(), member1.getUserId());
    }

    @Test
    @DisplayName("convertToScheduleOverviewResponse(): TravelSchedule 를 TravelOverviewResponse 로 변경 시 작성자가 없어 예외 발생")
    void convertToScheduleInfoResponse_notFoundException(){
        // given
        for(TravelAttendee attendee : schedule1.getTravelAttendeeList()){
            attendee.setRole(AttendeeRole.GUEST);
        }

        // when
        DataNotFoundException fail = assertThrows(DataNotFoundException.class, () -> scheduleService.convertToScheduleInfoResponse(schedule1));

        // then
        assertEquals(fail.getHttpStatus(), ErrorCode.AUTHOR_NOT_FOUND.getStatus());
        assertEquals(fail.getMessage(), ErrorCode.AUTHOR_NOT_FOUND.getMessage());

    }

    @Test
    @DisplayName("getAuthorDTO() : 작성자 조회해서 dto 생성")
    void getAuthorDTO(){
        // given
        // when
        AuthorDTO response = scheduleService.getAuthorDTO(schedule1);

        // then
        assertEquals(response.getUserId(), member1.getUserId());
        assertEquals(response.getProfileUrl(), member1.getProfileImage().getS3ObjectUrl());

    }

    @Test
    @DisplayName("getAuthorDTO() : 작성자 조회해서 dto 생성 시 작성자가 없어 예외 발생")
    void getAuthorDTO_notFoundException(){
        // given
        for(TravelAttendee attendee : schedule1.getTravelAttendeeList()){
            attendee.setRole(AttendeeRole.GUEST);
        }

        // when
        DataNotFoundException fail = assertThrows(DataNotFoundException.class, () -> scheduleService.getAuthorDTO(schedule1));

        // then
        assertEquals(fail.getHttpStatus(), ErrorCode.AUTHOR_NOT_FOUND.getStatus());
        assertEquals(fail.getMessage(), ErrorCode.AUTHOR_NOT_FOUND.getMessage());

    }


    @Test
    @DisplayName("getAuthorMember() : 일정 작성자 조회")
    void getAuthorMember(){
        // given
        // when
        Member response = scheduleService.getAuthorMember(schedule1.getTravelAttendeeList());

        // then
        assertEquals(response.getMemberId(), member1.getMemberId());
        assertEquals(response.getUserId(), member1.getUserId());
    }

    @Test
    @DisplayName("getAuthorMember() : 일정 작성자 조회 시 작성자가 없어 예외 발생")
    void getAuthorMember_notFoundException(){
        // given
        for(TravelAttendee attendee : schedule1.getTravelAttendeeList()){
            attendee.setRole(AttendeeRole.GUEST);
        }

        // when
        DataNotFoundException fail = assertThrows(DataNotFoundException.class, () -> scheduleService.getAuthorMember(schedule1.getTravelAttendeeList()));

        // then
        assertEquals(fail.getHttpStatus(), ErrorCode.AUTHOR_NOT_FOUND.getStatus());
        assertEquals(fail.getMessage(), ErrorCode.AUTHOR_NOT_FOUND.getMessage());
    }


    @Test
    @DisplayName("getThumbnailUrl(): 썸네일 조회")
    void getThumbnailUrl(){
        // given
        // when
        String response = scheduleService.getThumbnailUrl(schedule1);

        // then
        System.out.println(response);
        assertNotNull(response);
    }

    @Test
    @DisplayName("getThumbnailUrl(): 썸네일 조회 시 썸네일 이미지 없는 경우")
    void getThumbnailUrlNoThumbnailImage(){
        // given
        travelPlace1.getTravelImageList().get(0).getFile().setThumbnail(false);

        // when
        String response = scheduleService.getThumbnailUrl(schedule1);

        // then
        System.out.println(response);
        assertNull(response);
    }


    @Test
    @DisplayName("getThumbnailUrl(): 썸네일 조회 시 저장된 이미지 없는 경우")
    void getThumbnailUrlNoImageData(){
        // given
        travelPlace1.setTravelImageList(new ArrayList<>());

        // when
        String response = scheduleService.getThumbnailUrl(schedule1);

        // then
        assertNull(response);
    }


    @Test
    @DisplayName("createSchedule(): 일정 만들기 성공")
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
    @DisplayName("createSchedule(): 저장된 사용자 정보 없어 DataNotFoundException 발생")
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
    @DisplayName("getScheduleDetail(): 일정 조회 성공")
    void getScheduleDetail(){
        // given
        List<TravelPlace> placeList = new ArrayList<>(List.of(travelPlace1, travelPlace2));

        Pageable pageable = PageUtil.defaultPageable(1);

        when(travelScheduleRepository.findByScheduleId(any())).thenReturn(Optional.of(schedule1));
        when(travelPlaceRepository.findAllByAreaData(any(), anyString(), anyString(), anyString()))
                .thenReturn(PageUtil.createPage(placeList, pageable, 1));
        when(travelAttendeeRepository.findAllByTravelSchedule_ScheduleId(any())).thenReturn(schedule1.getTravelAttendeeList());

        // when
        ScheduleDetailResponse response = scheduleService.getScheduleDetail(schedule1.getScheduleId(), 1);

        // then
        assertEquals(response.getScheduleName(), schedule1.getScheduleName());
        assertEquals(response.getCreatedAt(), schedule1.getCreatedAt());
        assertEquals(response.getAttendeeList().get(0).getUserId(), schedule1.getTravelAttendeeList().get(0).getMember().getUserId());
        assertEquals(response.getAttendeeList().get(0).getRole(), schedule1.getTravelAttendeeList().get(0).getRole().name());
        assertEquals(response.getPlaceList().getTotalElements(), placeList.size());
        assertEquals(response.getPlaceList().getContent().get(0).getPlaceName(), placeList.get(0).getPlaceName());
        assertNotNull(response.getPlaceList().getContent().get(0).getThumbnailUrl());
    }

    @Test
    @DisplayName("getScheduleDetail(): 일정 조회 시 여행지 데이터 없는 경우")
    void getScheduleDetailWithoutData(){
        // given
        Pageable pageable = PageUtil.defaultPageable(1);

        when(travelScheduleRepository.findByScheduleId(any())).thenReturn(Optional.of(schedule1));
        when(travelPlaceRepository.findAllByAreaData(any(), anyString(), anyString(), anyString()))
                .thenReturn(PageUtil.createPage(new ArrayList<>(), pageable, 0));
        when(travelAttendeeRepository.findAllByTravelSchedule_ScheduleId(any())).thenReturn(schedule1.getTravelAttendeeList());

        // when
        ScheduleDetailResponse response = scheduleService.getScheduleDetail(schedule1.getScheduleId(), 1);

        // then
        assertEquals(response.getScheduleName(), schedule1.getScheduleName());
        assertEquals(response.getCreatedAt(), schedule1.getCreatedAt());
        assertEquals(response.getAttendeeList().get(0).getUserId(), schedule1.getTravelAttendeeList().get(0).getMember().getUserId());
        assertEquals(response.getAttendeeList().get(0).getRole(), schedule1.getTravelAttendeeList().get(0).getRole().name());
        assertEquals(response.getPlaceList().getTotalElements(), 0);
        assertTrue(response.getPlaceList().getContent().isEmpty());
    }

    @Test
    @DisplayName("getScheduleDetail(): 일정 조회 시 일정을 찾을 수 없어 DataNotFoundException 발생")
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
    @DisplayName("updateSchedule(): 일정 수정 성공")
    void updateSchedule(){
        String userId = member1.getUserId();
        Long scheduleId = schedule1.getScheduleId();

        RouteRequest routeRequest1 = createRouteRequest(1, travelPlace1.getPlaceId());
        RouteRequest routeRequest2 = createRouteRequest(2, travelPlace2.getPlaceId());
        UpdateScheduleRequest updateScheduleRequest = createUpdateScheduleRequest(new ArrayList<>(List.of(routeRequest1, routeRequest2)));

        when(travelScheduleRepository.findByScheduleId(scheduleId)).thenReturn(Optional.of(schedule1));
        when(memberRepository.findByUserId(userId)).thenReturn(Optional.of(member1));
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
    @DisplayName("updateSchedule(): 일정 수정 중 여행 루트 삭제에서 기존에 저장된 여행 루트가 없을 경우")
    void updateScheduleNoSavedTravelRouteList(){
        // given
        String userId = member3.getUserId();
        Long scheduleId = schedule2.getScheduleId();

        RouteRequest routeRequest1 = createRouteRequest(1, travelPlace1.getPlaceId());
        RouteRequest routeRequest2 = createRouteRequest(2, travelPlace2.getPlaceId());
        UpdateScheduleRequest updateScheduleRequest = createUpdateScheduleRequest(new ArrayList<>(List.of(routeRequest1, routeRequest2)));

        when(travelScheduleRepository.findByScheduleId(scheduleId)).thenReturn(Optional.of(schedule2));
        when(memberRepository.findByUserId(userId)).thenReturn(Optional.of(member3));
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
    @DisplayName("updateTravelRouteInSchedule(): 일정의 여행 루트 수정")
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
    @DisplayName("updateTravelRouteInSchedule(): 일정의 여행 루트 수정 중 기존에 저장된 여행 루트가 없을 경우")
    void updateTravelRouteInScheduleNoSavedTravelRouteList(){
        RouteRequest routeRequest1 = createRouteRequest(1, travelPlace1.getPlaceId());
        RouteRequest routeRequest2 = createRouteRequest(2, travelPlace2.getPlaceId());
        UpdateScheduleRequest updateScheduleRequest = createUpdateScheduleRequest(new ArrayList<>(List.of(routeRequest1, routeRequest2)));

        when(travelPlaceRepository.findByPlaceId(travelPlace1.getPlaceId())).thenReturn(Optional.of(travelPlace1));
        when(travelPlaceRepository.findByPlaceId(travelPlace2.getPlaceId())).thenReturn(Optional.of(travelPlace2));

        // when
        assertDoesNotThrow(() -> scheduleService.updateTravelRouteInSchedule(schedule2, updateScheduleRequest.getTravelRoute()));

        // then
        assertEquals(schedule2.getTravelRouteList().size(), 2);
        assertEquals(schedule2.getTravelRouteList().get(0).getTravelPlace().getPlaceName(), travelPlace1.getPlaceName());
    }

    @Test
    @DisplayName("updateSchedule(): 일정 수정 시 일정 데이터 없어 예외 발생")
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
    @DisplayName("updateSchedule(): 일정 수정 시 요청 사용자에게 접근 권한이 없어 예외 발생")
    void updateScheduleForbiddenAccess_forbiddenScheduleException(){
        // given
        String userId = member1.getUserId();
        Long scheduleId = schedule2.getScheduleId();

        RouteRequest routeRequest1 = createRouteRequest(1, travelPlace1.getPlaceId());
        RouteRequest routeRequest2 = createRouteRequest(2, travelPlace2.getPlaceId());
        UpdateScheduleRequest updateScheduleRequest = createUpdateScheduleRequest(new ArrayList<>(List.of(routeRequest1, routeRequest2)));

        when(travelScheduleRepository.findByScheduleId(scheduleId)).thenReturn(Optional.of(schedule2));
        when(memberRepository.findByUserId(userId)).thenReturn(Optional.of(member1));

        // when
        ForbiddenScheduleException fail = assertThrows(ForbiddenScheduleException.class, () -> scheduleService.updateSchedule(userId, scheduleId, updateScheduleRequest));

        // then
        assertEquals(fail.getHttpStatus(), ErrorCode.FORBIDDEN_ACCESS_SCHEDULE.getStatus());
        assertEquals(fail.getMessage(), ErrorCode.FORBIDDEN_ACCESS_SCHEDULE.getMessage());

    }

    @Test
    @DisplayName("updateSchedule(): 일정 수정 시 요청 사용자에게 수정 권한이 없어 예외 발생")
    void updateScheduleForbiddenEdit_forbiddenScheduleException(){
        // given
        String userId = member2.getUserId();
        Long scheduleId = schedule1.getScheduleId();

        RouteRequest routeRequest1 = createRouteRequest(1, travelPlace1.getPlaceId());
        RouteRequest routeRequest2 = createRouteRequest(2, travelPlace2.getPlaceId());
        UpdateScheduleRequest updateScheduleRequest = createUpdateScheduleRequest(new ArrayList<>(List.of(routeRequest1, routeRequest2)));

        when(travelScheduleRepository.findByScheduleId(scheduleId)).thenReturn(Optional.of(schedule1));
        when(memberRepository.findByUserId(userId)).thenReturn(Optional.of(member2));

        // when
        ForbiddenScheduleException fail = assertThrows(ForbiddenScheduleException.class, () -> scheduleService.updateSchedule(userId, scheduleId, updateScheduleRequest));

        // then
        assertEquals(fail.getHttpStatus(), ErrorCode.FORBIDDEN_EDIT_SCHEDULE.getStatus());
        assertEquals(fail.getMessage(), ErrorCode.FORBIDDEN_EDIT_SCHEDULE.getMessage());
    }


    @Test
    @DisplayName("updateSchedule(): 일정 수정 시 여행 루트에 저장된 여행지가 없어 예외 발생")
    void updateScheduleNoTravelPlace_dataNotFoundException(){
        // given
        String userId = member1.getUserId();
        Long scheduleId = schedule1.getScheduleId();

        RouteRequest routeRequest1 = createRouteRequest(1, travelPlace1.getPlaceId());
        RouteRequest routeRequest2 = createRouteRequest(2, travelPlace2.getPlaceId());
        UpdateScheduleRequest updateScheduleRequest = createUpdateScheduleRequest(new ArrayList<>(List.of(routeRequest1, routeRequest2)));

        when(travelScheduleRepository.findByScheduleId(scheduleId)).thenReturn(Optional.of(schedule1));
        when(memberRepository.findByUserId(userId)).thenReturn(Optional.of(member1));
        when(travelPlaceRepository.findByPlaceId(anyLong())).thenReturn(Optional.empty());

        // when
        DataNotFoundException fail = assertThrows(DataNotFoundException.class, () -> scheduleService.updateSchedule(userId, scheduleId, updateScheduleRequest));

        // then
        assertEquals(fail.getHttpStatus(), ErrorCode.PLACE_NOT_FOUND.getStatus());
        assertEquals(fail.getMessage(), ErrorCode.PLACE_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("findAttendeeInSchedule(): 요청 사용자가 일정에 참가자에 포함되는 경우")
    void getAttendeeInfo_containsAttendees(){
        // given
        String userId = member1.getUserId();

        when(memberRepository.findByUserId(userId)).thenReturn(Optional.of(member1));

        // when
        TravelAttendee response = scheduleService.getAttendeeInfo(userId, schedule1);

        // then
        assertEquals(response.getMember().getUserId(), userId);
        assertEquals(response.getTravelSchedule().getScheduleName(), schedule1.getScheduleName());
    }

    @Test
    @DisplayName("findAttendeeInSchedule(): 요청 사용자가 일정에 참가자에 포함되지 않는 경우")
    void getAttendeeInfo_notContainsAttendees(){
        // given
        String userId = member1.getUserId();
        when(memberRepository.findByUserId(userId)).thenReturn(Optional.of(member1));

        // when
        ForbiddenScheduleException fail = assertThrows(ForbiddenScheduleException.class, () -> scheduleService.getAttendeeInfo(userId, schedule2));

        // then
        assertEquals(fail.getHttpStatus(), ErrorCode.FORBIDDEN_ACCESS_SCHEDULE.getStatus());
        assertEquals(fail.getMessage(), ErrorCode.FORBIDDEN_ACCESS_SCHEDULE.getMessage());

    }

    @Test
    @DisplayName("checkUserPermission(): 사용자 편집 권한 중 ALL")
    void checkUserPermissionALL(){
        // given
        AttendeePermission permission = AttendeePermission.ALL;

        // when
        // then
        assertDoesNotThrow(() -> scheduleService.checkUserPermission(permission));
    }

    @Test
    @DisplayName("checkUserPermission(): 사용자 편집 권한 중 EDIT")
    void checkUserPermissionEdit(){
        // given
        AttendeePermission permission = AttendeePermission.EDIT;

        // when
        // then
        assertDoesNotThrow(() -> scheduleService.checkUserPermission(permission));
    }

    @Test
    @DisplayName("checkUserPermission(): 사용자 편집 권한 체크 시 CHAT 권한으로 예외 발생")
    void checkUserPermissionCHAT_forbiddenScheduleException(){
        // given
        AttendeePermission permission = AttendeePermission.CHAT;

        // when
        ForbiddenScheduleException fail = assertThrows(ForbiddenScheduleException.class, () -> scheduleService.checkUserPermission(permission));

        // then
        assertEquals(fail.getHttpStatus(), ErrorCode.FORBIDDEN_EDIT_SCHEDULE.getStatus());
        assertEquals(fail.getMessage(), ErrorCode.FORBIDDEN_EDIT_SCHEDULE.getMessage());
    }

    @Test
    @DisplayName("checkUserPermission(): 사용자 편집 권한 체크 시 READ 권한으로 예외 발생")
    void checkUserPermissionREAD_forbiddenScheduleException(){
        // given
        AttendeePermission permission = AttendeePermission.READ;

        // when
        ForbiddenScheduleException fail = assertThrows(ForbiddenScheduleException.class, () -> scheduleService.checkUserPermission(permission));

        // then
        assertEquals(fail.getHttpStatus(), ErrorCode.FORBIDDEN_EDIT_SCHEDULE.getStatus());
        assertEquals(fail.getMessage(), ErrorCode.FORBIDDEN_EDIT_SCHEDULE.getMessage());
    }

    @Test
    @DisplayName("deleteSchedule(): 일정 삭제")
    void deleteSchedule(){
        // given
        when(travelScheduleRepository.findByScheduleId(any())).thenReturn(Optional.of(schedule1));
        when(memberRepository.findByUserId(any())).thenReturn(Optional.of(member1));

        // when
        assertDoesNotThrow(() -> scheduleService.deleteSchedule(schedule1.getScheduleId(), member1.getUserId()));
    }

    @Test
    @DisplayName("deleteSchedule(): 일정 삭제 시 작성자가 아닌 사용자가 삭제 요청으로 인해 예외 발생")
    void deleteScheduleNotAuthor_forbiddenScheduleException(){
        // given
        when(travelScheduleRepository.findByScheduleId(any())).thenReturn(Optional.of(schedule1));
        when(memberRepository.findByUserId(any())).thenReturn(Optional.of(member2));

        // when
        ForbiddenScheduleException fail = assertThrows(ForbiddenScheduleException.class, () -> scheduleService.deleteSchedule(schedule1.getScheduleId(), member2.getUserId()));

        assertEquals(fail.getHttpStatus(), ErrorCode.FORBIDDEN_DELETE_SCHEDULE.getStatus());
        assertEquals(fail.getMessage(), ErrorCode.FORBIDDEN_DELETE_SCHEDULE.getMessage());
    }



    @Test
    @DisplayName("getTravelPlaces(): 여행지 조회 성공")
    void getTravelPlaces(){
        // given
        List<TravelPlace> placeList = new ArrayList<>(List.of(travelPlace1, travelPlace2));

        TravelSchedule schedule = createTravelSchedule(1L, "테스트");

        Pageable pageable = PageUtil.defaultPageable(1);

        when(travelScheduleRepository.findByScheduleId(any())).thenReturn(Optional.of(schedule));
        when(travelPlaceRepository.findAllByAreaData(any(), anyString(), anyString(), anyString()))
                .thenReturn(PageUtil.createPage(placeList, pageable, 1));

        // when
        Page<PlaceResponse> response = scheduleService.getTravelPlaces(schedule.getScheduleId(), 1);

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

        when(travelScheduleRepository.findByScheduleId(any())).thenReturn(Optional.of(schedule1));
        when(travelPlaceRepository.findAllByAreaData(any(), anyString(), anyString(), anyString()))
                .thenReturn(PageUtil.createPage(new ArrayList<>(), pageable, 0));

        // when
        Page<PlaceResponse> response = scheduleService.getTravelPlaces(schedule1.getScheduleId(), 1);

        // then
        assertEquals(response.getTotalElements(), 0);
        assertTrue(response.getContent().isEmpty());
    }

    @Test
    @DisplayName("getTravelPlaces(): 여행지 조회 시 일정을 찾을 수 없어 예외 발생")
    void getTravelPlaces_dataNotFoundException(){
        // given
        when(travelScheduleRepository.findByScheduleId(any())).thenReturn(Optional.empty());

        // when
        DataNotFoundException fail = assertThrows(DataNotFoundException.class, () -> scheduleService.getTravelPlaces(0L, 1));

        // then
        assertEquals(fail.getHttpStatus(), ErrorCode.SCHEDULE_NOT_FOUND.getStatus());
        assertEquals(fail.getMessage(), ErrorCode.SCHEDULE_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("searchTravelPlaces(): 여행지 검색 성공")
    void searchTravelPlaces(){
        // given
        String keyword = "중구";
        Pageable pageable = PageUtil.defaultPageable(1);

        List<TravelPlace> travelPlaceList = new ArrayList<>(List.of(travelPlace1, travelPlace2));
        Page<TravelPlace> travelPlacePage = PageUtil.createPage(travelPlaceList, pageable, travelPlaceList.size());

        when(travelScheduleRepository.findByScheduleId(any())).thenReturn(Optional.of(createTravelSchedule(1L, "테스트")));
        when(travelPlaceRepository.searchTravelPlaces(pageable, keyword)).thenReturn(travelPlacePage);

        // when
        Page<PlaceResponse> response = scheduleService.searchTravelPlaces(1L, 1, keyword);


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

        when(travelScheduleRepository.findByScheduleId(any())).thenReturn(Optional.of(createTravelSchedule(1L, "테스트")));
        when(travelPlaceRepository.searchTravelPlaces(pageable, keyword)).thenReturn(travelPlacePage);

        // when
        Page<PlaceResponse> response = scheduleService.searchTravelPlaces(1L, 1, keyword);


        // then
        assertEquals(response.getTotalElements(), 0);
    }

    @Test
    @DisplayName("searchTravelPlaces(): 여행지 검색 시 일정을 찾을 수 없어 예외 발생")
    void searchTravelPlaces_dataNotFoundException(){
        // given
        when(travelScheduleRepository.findByScheduleId(any())).thenReturn(Optional.empty());

        // when
        DataNotFoundException fail = assertThrows(DataNotFoundException.class, () -> scheduleService.searchTravelPlaces(0L, 1, "강남"));

        // then
        assertEquals(fail.getHttpStatus(), ErrorCode.SCHEDULE_NOT_FOUND.getStatus());
        assertEquals(fail.getMessage(), ErrorCode.SCHEDULE_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("getTravelRoutes(): 여행 루트 조회 성공")
    void getTravelRoutes(){
        // given
        Pageable pageable = PageUtil.defaultPageable(1);

        when(travelScheduleRepository.findByScheduleId(any())).thenReturn(Optional.of(schedule1));
        when(travelRouteRepository.findAllByTravelSchedule_ScheduleId(pageable, schedule1.getScheduleId()))
                .thenReturn(PageUtil.createPage(schedule1.getTravelRouteList(), pageable, schedule1.getTravelRouteList().size()));


        // when
        Page<RouteResponse> response = scheduleService.getTravelRoutes(schedule1.getScheduleId(), 1);

        // then
        List<RouteResponse> content = response.getContent();
        assertEquals(response.getTotalElements(), schedule1.getTravelRouteList().size());
        assertEquals(content.get(0).getAddress(), travelPlace1.getAddress());
        assertEquals(content.get(0).getRouteOrder(), schedule1.getTravelRouteList().get(0).getRouteOrder());
        assertEquals(content.get(1).getRouteOrder(), schedule1.getTravelRouteList().get(1).getRouteOrder());
        assertEquals(content.get(2).getRouteOrder(), schedule1.getTravelRouteList().get(2).getRouteOrder());
    }

    @Test
    @DisplayName("getTravelRoutes(): 여행 루트 조회 시 저장된 여행 루트 데이터 없는 경우")
    void getTravelRoutesWithoutData(){
        // given
        Pageable pageable = PageUtil.defaultPageable(1);

        when(travelScheduleRepository.findByScheduleId(any())).thenReturn(Optional.of(schedule1));
        when(travelRouteRepository.findAllByTravelSchedule_ScheduleId(pageable, schedule1.getScheduleId()))
                .thenReturn(PageUtil.createPage(new ArrayList<>(), pageable, 0));


        // when
        Page<RouteResponse> response = scheduleService.getTravelRoutes(schedule1.getScheduleId(), 1);

        // then
        assertEquals(response.getTotalElements(), 0);
        assertTrue(response.getContent().isEmpty());
    }

    @Test
    @DisplayName("getTravelRoutes(): 여행 루트 조회 시 일정을 찾을 수 없어 예외 발생")
    void getTravelRoutes_dataNotFoundException(){
        // given
        when(travelScheduleRepository.findByScheduleId(any())).thenReturn(Optional.empty());

        // when
        DataNotFoundException fail = assertThrows(DataNotFoundException.class, () -> scheduleService.getTravelRoutes(0L, 1));

        // then
        assertEquals(fail.getHttpStatus(), ErrorCode.SCHEDULE_NOT_FOUND.getStatus());
        assertEquals(fail.getMessage(), ErrorCode.SCHEDULE_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("getSimpleTravelPlacesByJunggu(): 중구 기준 여행지 데이터 조회")
    void getSimpleTravelPlacesByJunggu(){
        // given
        List<TravelPlace> placeList = new ArrayList<>(List.of(travelPlace1, travelPlace2));
        Pageable pageable = PageUtil.defaultPageable(1);

        when(travelPlaceRepository.findAllByAreaData(pageable, "대한민국", "서울", "중구"))
                .thenReturn(PageUtil.createPage(placeList, pageable, placeList.size()));


        // when
        Page<PlaceResponse> response = scheduleService.getSimpleTravelPlacesByJunggu(1);

        // then
        List<PlaceResponse> content = response.getContent();
        assertEquals(response.getTotalElements(), placeList.size());
        assertEquals(content.get(0).getAddress(), travelPlace1.getAddress());
    }

    @Test
    @DisplayName("getSimpleTravelPlacesByJunggu(): 중구 기준 여행지 데이터 조회 시 데이터 없는 경우")
    void getSimpleTravelPlacesByJungguWithoutData(){
        // given
        Pageable pageable = PageUtil.defaultPageable(1);

        when(travelPlaceRepository.findAllByAreaData(pageable, "대한민국", "서울", "중구"))
                .thenReturn(PageUtil.createPage(new ArrayList<>(), pageable, 0));


        // when
        Page<PlaceResponse> response = scheduleService.getSimpleTravelPlacesByJunggu(1);

        // then
        assertEquals(response.getTotalElements(), 0);
        assertTrue(response.getContent().isEmpty());
    }

    @Test
    @DisplayName("getSavedMember(): 저장된 사용자 정보 조회")
    void getSavedMember(){
        // given
        String userId = "member1";

        when(memberRepository.findByUserId(any())).thenReturn(Optional.of(member1));

        // when
        Member response = scheduleService.getSavedMember(userId);

        // then
        assertEquals(response.getUserId(), userId);
        assertEquals(response.getEmail(), member1.getEmail());
        assertEquals(response.getNickname(), member1.getNickname());

    }

    @Test
    @DisplayName("getSavedMember(): 저장된 사용자 정보 조회 시 데이터 찾을 수 없어 예외 발생")
    void getSavedMember_dataNotFoundException(){
        // given
        when(memberRepository.findByUserId(any())).thenReturn(Optional.empty());

        // when
        DataNotFoundException fail = assertThrows(DataNotFoundException.class, () -> scheduleService.getSavedMember("notUser"));

        // then
        assertEquals(fail.getHttpStatus(), ErrorCode.USER_NOT_FOUND.getStatus());
        assertEquals(fail.getMessage(), ErrorCode.USER_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("getSavedSchedule(): 저장된 일정 조회")
    void getSavedSchedule(){
        // given
        when(travelScheduleRepository.findByScheduleId(any())).thenReturn(Optional.of(schedule1));

        // when
        TravelSchedule response = scheduleService.getSavedSchedule(schedule1.getScheduleId());

        // then
        assertEquals(response.getScheduleName(), schedule1.getScheduleName());
        assertEquals(response.getStartDate(), schedule1.getStartDate());
        assertEquals(response.getEndDate(), schedule1.getEndDate());
    }

    @Test
    @DisplayName("getSavedSchedule(): 저장된 일정 조회 시 데이터 찾을 수 없어 예외 발생")
    void getSavedSchedule_dataNotFoundException(){
        // given
        when(travelScheduleRepository.findByScheduleId(any())).thenReturn(Optional.empty());

        // when
        DataNotFoundException fail = assertThrows(DataNotFoundException.class, () -> scheduleService.getSavedSchedule(0L));

        // then
        assertEquals(fail.getHttpStatus(), ErrorCode.SCHEDULE_NOT_FOUND.getStatus());
        assertEquals(fail.getMessage(), ErrorCode.SCHEDULE_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("getSavedPlace(): 저장된 여행지 조회 성공")
    void getSavedPlace(){
        // given
        Long placeId = travelPlace1.getPlaceId();

        when(travelPlaceRepository.findByPlaceId(anyLong())).thenReturn(Optional.of(travelPlace1));

        // when
        TravelPlace response = scheduleService.getSavedPlace(placeId);

        // then
        assertEquals(response.getPlaceId(), placeId);
        assertEquals(response.getPlaceName(), travelPlace1.getPlaceName());

    }

    @Test
    @DisplayName("getSavedPlace(): 여행지가 존재하지 않아 예외 발생")
    void getSavedPlaceNoTravelPlace_dataNotFoundException(){
        // given
        Long placeId = travelPlace1.getPlaceId();

        when(travelPlaceRepository.findByPlaceId(anyLong())).thenReturn(Optional.empty());

        // when
        DataNotFoundException fail = assertThrows(DataNotFoundException.class, () -> scheduleService.getSavedPlace(placeId));

        // then
        assertEquals(fail.getHttpStatus(), ErrorCode.PLACE_NOT_FOUND.getStatus());
        assertEquals(fail.getMessage(), ErrorCode.PLACE_NOT_FOUND.getMessage());

    }

}
