package com.triptune.domain.schedule.service;

import com.triptune.domain.common.entity.*;
import com.triptune.domain.member.entity.Member;
import com.triptune.domain.member.repository.MemberRepository;
import com.triptune.domain.schedule.ScheduleTest;
import com.triptune.domain.schedule.dto.*;
import com.triptune.domain.schedule.entity.TravelAttendee;
import com.triptune.domain.schedule.entity.TravelRoute;
import com.triptune.domain.schedule.entity.TravelSchedule;
import com.triptune.domain.schedule.repository.TravelAttendeeRepository;
import com.triptune.domain.schedule.repository.TravelRouteRepository;
import com.triptune.domain.schedule.repository.TravelScheduleRepository;
import com.triptune.domain.travel.dto.PlaceResponse;
import com.triptune.domain.travel.entity.TravelImage;
import com.triptune.domain.travel.entity.TravelPlace;
import com.triptune.domain.travel.repository.TravelPlacePlaceRepository;
import com.triptune.global.enumclass.ErrorCode;
import com.triptune.global.exception.DataNotFoundException;
import com.triptune.global.util.PageUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

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


    @Test
    @DisplayName("getSchedules(): 내 일정 목록 조회")
    void getSchedules(){
        // given
        int page = 1;
        String userId = "member1";

        Pageable pageable = PageUtil.createPageRequest(1, 5);

        Country country = createCountry();
        City city = createCity(country);
        District district = createDistrict(city, "중구");
        ApiCategory apiCategory = createApiCategory();
        TravelPlace travelPlace = createTravelPlace(1L, country, city, district, apiCategory);

        File file1 = createFile("test1", true);
        File file2 = createFile("test2", false);

        TravelImage travelImage1 = createTravelImage(travelPlace, file1);
        TravelImage travelImage2 = createTravelImage(travelPlace, file2);
        List<TravelImage> travelImageList = Arrays.asList(travelImage1, travelImage2);
        travelPlace.setTravelImageList(travelImageList);


        List<TravelSchedule> schedules = Arrays.asList(createTravelSchedule(1L, "테스트1"), createTravelSchedule(2L, "테스트2"));
        Page<TravelSchedule> schedulePage = PageUtil.createPage(schedules, pageable, schedules.size());

        List<TravelImage> travelImages = Arrays.asList(createTravelImage(travelPlace, file1), createTravelImage(travelPlace, file2));

        when(memberRepository.findByUserId(any())).thenReturn(Optional.of(createMember(1L, userId)));
        when(travelScheduleRepository.findTravelSchedulesByAttendee(pageable, 1L)).thenReturn(schedulePage);
        when(travelRouteRepository.findPlaceImagesOfFirstRoute(1L)).thenReturn(travelImages);

        // when
        Page<ScheduleOverviewResponse> response = scheduleService.getSchedules(page, userId);

        // then
        List<ScheduleOverviewResponse> content = response.getContent();
        assertEquals(response.getTotalElements(), 2);
        assertEquals(content.get(0).getScheduleName(), "테스트1");
        assertNotNull(content.get(0).getSinceUpdate());
        assertNotNull(content.get(0).getThumbnailUrl());

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

        Pageable pageable = PageUtil.createPageRequest(1, 5);

        Page<TravelSchedule> emptySchedulePage = PageUtil.createPage(new ArrayList<>(), pageable, 0);

        when(memberRepository.findByUserId(any())).thenReturn(Optional.of(createMember(1L, userId)));
        when(travelScheduleRepository.findTravelSchedulesByAttendee(pageable, 1L)).thenReturn(emptySchedulePage);

        // when
        Page<ScheduleOverviewResponse> response = scheduleService.getSchedules(page, userId);

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

        Pageable pageable = PageUtil.createPageRequest(1, 5);

        Country country = createCountry();
        City city = createCity(country);
        District district = createDistrict(city, "중구");
        ApiCategory apiCategory = createApiCategory();
        TravelPlace travelPlace = createTravelPlace(1L, country, city, district, apiCategory);

        File file1 = createFile("test1", false);
        File file2 = createFile("test2", false);

        TravelImage travelImage1 = createTravelImage(travelPlace, file1);
        TravelImage travelImage2 = createTravelImage(travelPlace, file2);
        List<TravelImage> travelImageList = Arrays.asList(travelImage1, travelImage2);
        travelPlace.setTravelImageList(travelImageList);


        List<TravelSchedule> schedules = Arrays.asList(createTravelSchedule(1L, "테스트1"), createTravelSchedule(2L, "테스트2"));
        Page<TravelSchedule> schedulePage = PageUtil.createPage(schedules, pageable, schedules.size());

        List<TravelImage> travelImages = Arrays.asList(createTravelImage(travelPlace, file1), createTravelImage(travelPlace, file2));

        when(memberRepository.findByUserId(any())).thenReturn(Optional.of(createMember(1L, userId)));
        when(travelScheduleRepository.findTravelSchedulesByAttendee(pageable, 1L)).thenReturn(schedulePage);
        when(travelRouteRepository.findPlaceImagesOfFirstRoute(1L)).thenReturn(travelImages);

        // when
        Page<ScheduleOverviewResponse> response = scheduleService.getSchedules(page, userId);

        // then
        List<ScheduleOverviewResponse> content = response.getContent();
        assertEquals(response.getTotalElements(), 2);
        assertEquals(content.get(0).getScheduleName(), "테스트1");
        assertNotNull(content.get(0).getSinceUpdate());
        assertNull(content.get(0).getThumbnailUrl());

    }


    @Test
    @DisplayName("getSchedules(): 내 일정 목록 조회 시 이미지 썸네일 데이터 없는 경우")
    void getSchedulesNoImageData(){
        // given
        int page = 1;
        String userId = "member1";

        Pageable pageable = PageUtil.createPageRequest(1, 5);

        List<TravelSchedule> schedules = Arrays.asList(createTravelSchedule(1L, "테스트1"), createTravelSchedule(2L, "테스트2"));
        Page<TravelSchedule> schedulePage = PageUtil.createPage(schedules, pageable, schedules.size());

        when(memberRepository.findByUserId(any())).thenReturn(Optional.of(createMember(1L, userId)));
        when(travelScheduleRepository.findTravelSchedulesByAttendee(pageable, 1L)).thenReturn(schedulePage);
        when(travelRouteRepository.findPlaceImagesOfFirstRoute(1L)).thenReturn(new ArrayList<>());

        // when
        Page<ScheduleOverviewResponse> response = scheduleService.getSchedules(page, userId);

        // then
        List<ScheduleOverviewResponse> content = response.getContent();
        assertEquals(response.getTotalElements(), 2);
        assertEquals(content.get(0).getScheduleName(), "테스트1");
        assertNotNull(content.get(0).getSinceUpdate());
        assertNull(content.get(0).getThumbnailUrl());

    }

    @Test
    @DisplayName("convertToScheduleOverviewResponse(): TravelSchedule 를 TravelOverviewResponse 로 변경")
    void convertToScheduleOverviewResponse(){
        // given
        TravelSchedule schedule = createTravelSchedule(1L, "테스트1");

        Country country = createCountry();
        City city = createCity(country);
        District district = createDistrict(city, "중구");
        ApiCategory apiCategory = createApiCategory();
        TravelPlace travelPlace = createTravelPlace(1L, country, city, district, apiCategory);

        File file1 = createFile("test1", true);
        File file2 = createFile("test2", false);

        TravelImage travelImage1 = createTravelImage(travelPlace, file1);
        TravelImage travelImage2 = createTravelImage(travelPlace, file2);
        List<TravelImage> travelImageList = Arrays.asList(travelImage1, travelImage2);
        travelPlace.setTravelImageList(travelImageList);


        when(travelRouteRepository.findPlaceImagesOfFirstRoute(1L)).thenReturn(travelImageList);

        // when
        ScheduleOverviewResponse response = scheduleService.convertToScheduleOverviewResponse(schedule);

        // then
        assertEquals(response.getScheduleName(), "테스트1");
        assertNotNull(response.getSinceUpdate());
        assertNotNull(response.getThumbnailUrl());
    }

    @Test
    @DisplayName("convertToScheduleOverviewResponse(): TravelSchedule 를 TravelOverviewResponse 로 변경 시 썸네일 없는 경우")
    void convertToScheduleOverviewResponseWithoutThumbnail(){
        // given
        TravelSchedule schedule = createTravelSchedule(1L, "테스트1");

        when(travelRouteRepository.findPlaceImagesOfFirstRoute(1L)).thenReturn(new ArrayList<>());

        // when
        ScheduleOverviewResponse response = scheduleService.convertToScheduleOverviewResponse(schedule);

        // then
        assertEquals(response.getScheduleName(), "테스트1");
        assertNotNull(response.getSinceUpdate());
        assertNull(response.getThumbnailUrl());
    }


    @Test
    @DisplayName("getThumbnailUrl(): 썸네일 조회")
    void getThumbnailUrl(){
        // given
        TravelSchedule schedule = createTravelSchedule(1L, "테스트1");

        Country country = createCountry();
        City city = createCity(country);
        District district = createDistrict(city, "중구");
        ApiCategory apiCategory = createApiCategory();
        TravelPlace travelPlace = createTravelPlace(1L, country, city, district, apiCategory);

        File file1 = createFile("test1", true);
        File file2 = createFile("test2", false);

        TravelImage travelImage1 = createTravelImage(travelPlace, file1);
        TravelImage travelImage2 = createTravelImage(travelPlace, file2);
        List<TravelImage> travelImageList = Arrays.asList(travelImage1, travelImage2);
        travelPlace.setTravelImageList(travelImageList);


        when(travelRouteRepository.findPlaceImagesOfFirstRoute(1L)).thenReturn(travelImageList);

        // when
        String response = scheduleService.getThumbnailUrl(schedule);

        // then
        System.out.println(response);
        assertNotNull(response);
    }

    @Test
    @DisplayName("getThumbnailUrl(): 썸네일 조회 시 썸네일 이미지 없는 경우")
    void getThumbnailUrlNoThumbnailImage(){
        // given
        TravelSchedule schedule = createTravelSchedule(1L, "테스트1");

        Country country = createCountry();
        City city = createCity(country);
        District district = createDistrict(city, "중구");
        ApiCategory apiCategory = createApiCategory();
        TravelPlace travelPlace = createTravelPlace(1L, country, city, district, apiCategory);

        File file1 = createFile("test1", false);
        File file2 = createFile("test2", false);

        TravelImage travelImage1 = createTravelImage(travelPlace, file1);
        TravelImage travelImage2 = createTravelImage(travelPlace, file2);
        List<TravelImage> travelImageList = Arrays.asList(travelImage1, travelImage2);
        travelPlace.setTravelImageList(travelImageList);


        when(travelRouteRepository.findPlaceImagesOfFirstRoute(1L)).thenReturn(travelImageList);

        // when
        String response = scheduleService.getThumbnailUrl(schedule);

        // then
        System.out.println(response);
        assertNull(response);
    }


    @Test
    @DisplayName("getThumbnailUrl(): 썸네일 조회 시 저장된 이미지 없는 경우")
    void getThumbnailUrlNoImageData(){
        // given
        TravelSchedule schedule = createTravelSchedule(1L, "테스트1");

        when(travelRouteRepository.findPlaceImagesOfFirstRoute(1L)).thenReturn(new ArrayList<>());

        // when
        String response = scheduleService.getThumbnailUrl(schedule);

        // then
        assertNull(response);
    }


    @Test
    @DisplayName("createSchedule(): 일정 만들기 성공")
    void createSchedule(){
        // given
        String userId = "test";
        CreateScheduleRequest request = createScheduleRequest();
        TravelSchedule savedtravelSchedule = createTravelSchedule(1L, "테스트");
        Member savedMember = createMember(1L, userId);

        when(travelScheduleRepository.save(any())).thenReturn(savedtravelSchedule);
        when(memberRepository.findByUserId(any())).thenReturn(Optional.of(savedMember));

        // when
        CreateScheduleResponse response = scheduleService.createSchedule(request, userId);

        // then
        verify(travelAttendeeRepository, times(1)).save(any(TravelAttendee.class));
        assertEquals(response.getScheduleId(), savedtravelSchedule.getScheduleId());

    }

    @Test
    @DisplayName("createSchedule(): 저장된 사용자 정보 없어 DataNotFoundException 발생")
    void createSchedule_CustomUsernameDataNotFoundException(){
        // given
        CreateScheduleRequest request = createScheduleRequest();
        TravelSchedule schedule = createTravelSchedule(1L, "테스트");

        when(travelScheduleRepository.save(any())).thenReturn(schedule);
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
        Country country = createCountry();
        City city = createCity(country);
        District district = createDistrict(city, "중구");
        ApiCategory apiCategory = createApiCategory();
        TravelPlace travelPlace1 = createTravelPlace(1L, country, city, district, apiCategory);
        TravelPlace travelPlace2 = createTravelPlace(2L, country, city, district, apiCategory);

        File file1 = createFile("test1", true);
        File file2 = createFile("test2", false);

        TravelImage travelImage1 = createTravelImage(travelPlace1, file1);
        TravelImage travelImage2 = createTravelImage(travelPlace1, file2);
        List<TravelImage> travelImageList1 = Arrays.asList(travelImage1, travelImage2);
        travelPlace1.setTravelImageList(travelImageList1);

        TravelImage travelImage3 = createTravelImage(travelPlace2, file1);
        TravelImage travelImage4 = createTravelImage(travelPlace2, file2);
        List<TravelImage> travelImageList2 = Arrays.asList(travelImage3, travelImage4);
        travelPlace2.setTravelImageList(travelImageList2);

        List<TravelPlace> placeList = new ArrayList<>();
        placeList.add(travelPlace1);
        placeList.add(travelPlace2);

        TravelSchedule schedule = createTravelSchedule(1L, "테스트");
        Member member1 = createMember(1L, "member1");
        Member member2 = createMember(2L, "member2");

        List<TravelAttendee> attendeeList = new ArrayList<>();
        attendeeList.add(createTravelAttendee(member1, schedule));
        attendeeList.add(createTravelAttendee(member2, schedule));

        Pageable pageable = PageUtil.createPageRequest(1, 5);

        when(travelScheduleRepository.findByScheduleId(any())).thenReturn(Optional.of(schedule));
        when(travelPlaceRepository.findAllByAreaData(any(), anyString(), anyString(), anyString()))
                .thenReturn(PageUtil.createPage(placeList, pageable, 1));
        when(travelAttendeeRepository.findAllByTravelSchedule_ScheduleId(any())).thenReturn(attendeeList);

        // when
        ScheduleResponse response = scheduleService.getScheduleDetail(schedule.getScheduleId(), 1);

        // then
        assertEquals(response.getScheduleName(), schedule.getScheduleName());
        assertEquals(response.getCreatedAt(), schedule.getCreatedAt());
        assertEquals(response.getAttendeeList().get(0).getUserId(), attendeeList.get(0).getMember().getUserId());
        assertEquals(response.getAttendeeList().get(0).getRole(), attendeeList.get(0).getRole().name());
        assertEquals(response.getPlaceList().getTotalElements(), placeList.size());
        assertEquals(response.getPlaceList().getContent().get(0).getPlaceName(), placeList.get(0).getPlaceName());
        assertNotNull(response.getPlaceList().getContent().get(0).getThumbnailUrl());
    }

    @Test
    @DisplayName("getScheduleDetail(): 일정 조회 시 여행지 데이터 없는 경우")
    void getScheduleDetailWithoutData(){
        // given
        TravelSchedule schedule = createTravelSchedule(1L, "테스트");
        Member member1 = createMember(1L, "member1");
        Member member2 = createMember(2L, "member2");

        List<TravelAttendee> attendeeList = new ArrayList<>();
        attendeeList.add(createTravelAttendee(member1, schedule));
        attendeeList.add(createTravelAttendee(member2, schedule));

        Pageable pageable = PageUtil.createPageRequest(1, 5);

        when(travelScheduleRepository.findByScheduleId(any())).thenReturn(Optional.of(schedule));
        when(travelPlaceRepository.findAllByAreaData(any(), anyString(), anyString(), anyString()))
                .thenReturn(PageUtil.createPage(new ArrayList<>(), pageable, 0));
        when(travelAttendeeRepository.findAllByTravelSchedule_ScheduleId(any())).thenReturn(attendeeList);

        // when
        ScheduleResponse response = scheduleService.getScheduleDetail(schedule.getScheduleId(), 1);

        // then
        assertEquals(response.getScheduleName(), schedule.getScheduleName());
        assertEquals(response.getCreatedAt(), schedule.getCreatedAt());
        assertEquals(response.getAttendeeList().get(0).getUserId(), attendeeList.get(0).getMember().getUserId());
        assertEquals(response.getAttendeeList().get(0).getRole(), attendeeList.get(0).getRole().name());
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
        assertEquals(fail.getHttpStatus(), ErrorCode.DATA_NOT_FOUND.getStatus());
        assertEquals(fail.getMessage(), ErrorCode.DATA_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("getTravelPlaces(): 여행지 조회 성공")
    void getTravelPlaces(){
        // given
        Country country = createCountry();
        City city = createCity(country);
        District district = createDistrict(city, "중구");
        ApiCategory apiCategory = createApiCategory();
        TravelPlace travelPlace1 = createTravelPlace(1L, country, city, district, apiCategory);
        TravelPlace travelPlace2 = createTravelPlace(2L, country, city, district, apiCategory);

        File file1 = createFile("test1", true);
        File file2 = createFile("test2", false);

        TravelImage travelImage1 = createTravelImage(travelPlace1, file1);
        TravelImage travelImage2 = createTravelImage(travelPlace1, file2);
        List<TravelImage> travelImageList1 = Arrays.asList(travelImage1, travelImage2);
        travelPlace1.setTravelImageList(travelImageList1);

        TravelImage travelImage3 = createTravelImage(travelPlace2, file1);
        TravelImage travelImage4 = createTravelImage(travelPlace2, file2);
        List<TravelImage> travelImageList2 = Arrays.asList(travelImage3, travelImage4);
        travelPlace2.setTravelImageList(travelImageList2);

        List<TravelPlace> placeList = new ArrayList<>();
        placeList.add(travelPlace1);
        placeList.add(travelPlace2);

        TravelSchedule schedule = createTravelSchedule(1L, "테스트");

        Pageable pageable = PageUtil.createPageRequest(1, 5);

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
        TravelSchedule schedule = createTravelSchedule(1L, "테스트");
        Pageable pageable = PageUtil.createPageRequest(1, 5);

        when(travelScheduleRepository.findByScheduleId(any())).thenReturn(Optional.of(schedule));
        when(travelPlaceRepository.findAllByAreaData(any(), anyString(), anyString(), anyString()))
                .thenReturn(PageUtil.createPage(new ArrayList<>(), pageable, 0));

        // when
        Page<PlaceResponse> response = scheduleService.getTravelPlaces(schedule.getScheduleId(), 1);

        // then
        assertEquals(response.getTotalElements(), 0);
        assertTrue(response.getContent().isEmpty());
    }

    @Test
    @DisplayName("getTravelPlaces(): 여행지 조회 시 일정을 찾을 수 없어 DataNotFoundException 발생")
    void getTravelPlaces_dataNotFoundException(){
        // given
        when(travelScheduleRepository.findByScheduleId(any())).thenReturn(Optional.empty());

        // when
        DataNotFoundException fail = assertThrows(DataNotFoundException.class, () -> scheduleService.getTravelPlaces(0L, 1));

        // then
        assertEquals(fail.getHttpStatus(), ErrorCode.DATA_NOT_FOUND.getStatus());
        assertEquals(fail.getMessage(), ErrorCode.DATA_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("searchTravelPlaces(): 여행지 검색 성공")
    void searchTravelPlaces(){
        // given
        String keyword = "강남";
        Pageable pageable = PageUtil.createPageRequest(1, 5);

        Country country = createCountry();
        City city = createCity(country);
        District district = createDistrict(city, "강남구");
        ApiCategory apiCategory = createApiCategory();
        TravelPlace travelPlace = createTravelPlace(1L, country, city, district, apiCategory);

        List<TravelPlace> travelPlaceList = new ArrayList<>();
        travelPlaceList.add(travelPlace);

        File file1 = createFile("test1", true);
        File file2 = createFile("test2", false);

        TravelImage travelImage1 = createTravelImage(travelPlace, file1);
        TravelImage travelImage2 = createTravelImage(travelPlace, file2);
        List<TravelImage> travelImageList = Arrays.asList(travelImage1, travelImage2);
        travelPlace.setTravelImageList(travelImageList);

        Page<TravelPlace> travelPlacePage = PageUtil.createPage(travelPlaceList, pageable, travelPlaceList.size());

        when(travelScheduleRepository.findByScheduleId(any())).thenReturn(Optional.of(createTravelSchedule(1L, "테스트")));
        when(travelPlaceRepository.searchTravelPlaces(pageable, keyword)).thenReturn(travelPlacePage);

        // when
        Page<PlaceResponse> response = scheduleService.searchTravelPlaces(1L, 1, keyword);


        // then
        List<PlaceResponse> content = response.getContent();
        assertEquals(content.get(0).getPlaceName(), travelPlace.getPlaceName());
        assertEquals(content.get(0).getAddress(), travelPlace.getAddress());
        assertEquals(content.get(0).getThumbnailUrl(), travelImage1.getFile().getS3ObjectUrl());
    }

    @Test
    @DisplayName("searchTravelPlaces(): 여행지 검색 시 검색 결과 존재하지 않는 경우")
    void searchTravelPlacesWithoutData(){
        // given
        String keyword = "ㅁㄴㅇㄹ";
        Pageable pageable = PageUtil.createPageRequest(1, 5);

        Page<TravelPlace> travelPlacePage = PageUtil.createPage(new ArrayList<>(), pageable, 0);

        when(travelScheduleRepository.findByScheduleId(any())).thenReturn(Optional.of(createTravelSchedule(1L, "테스트")));
        when(travelPlaceRepository.searchTravelPlaces(pageable, keyword)).thenReturn(travelPlacePage);

        // when
        Page<PlaceResponse> response = scheduleService.searchTravelPlaces(1L, 1, keyword);


        // then
        assertEquals(response.getTotalElements(), 0);
    }

    @Test
    @DisplayName("searchTravelPlaces(): 여행지 검색 시 일정을 찾을 수 없어 DataNotFoundException 발생")
    void searchTravelPlaces_dataNotFoundException(){
        // given
        when(travelScheduleRepository.findByScheduleId(any())).thenReturn(Optional.empty());

        // when
        DataNotFoundException fail = assertThrows(DataNotFoundException.class, () -> scheduleService.searchTravelPlaces(0L, 1, "강남"));

        // then
        assertEquals(fail.getHttpStatus(), ErrorCode.DATA_NOT_FOUND.getStatus());
        assertEquals(fail.getMessage(), ErrorCode.DATA_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("getTravelRoutes(): 여행 루트 조회 성공")
    void getTravelRoutes(){
        // given
        TravelSchedule schedule = createTravelSchedule(1L, "테스트");

        Country country = createCountry();
        City city = createCity(country);
        District district = createDistrict(city, "강남구");
        ApiCategory apiCategory = createApiCategory();
        TravelPlace travelPlace = createTravelPlace(1L, country, city, district, apiCategory);

        File file1 = createFile("test1", true);
        File file2 = createFile("test2", false);

        TravelImage travelImage1 = createTravelImage(travelPlace, file1);
        TravelImage travelImage2 = createTravelImage(travelPlace, file2);
        List<TravelImage> travelImageList = Arrays.asList(travelImage1, travelImage2);
        travelPlace.setTravelImageList(travelImageList);

        TravelRoute route1 = createTravelRoute(schedule, travelPlace, 1);
        TravelRoute route2 = createTravelRoute(schedule, travelPlace, 2);
        TravelRoute route3 = createTravelRoute(schedule, travelPlace, 3);

        List<TravelRoute> travelRouteList = new ArrayList<>();
        travelRouteList.add(route1);
        travelRouteList.add(route2);
        travelRouteList.add(route3);

        Pageable pageable = PageUtil.createPageRequest(1, 5);

        when(travelScheduleRepository.findByScheduleId(any())).thenReturn(Optional.of(schedule));
        when(travelRouteRepository.findAllByTravelSchedule_ScheduleId(pageable, schedule.getScheduleId()))
                .thenReturn(PageUtil.createPage(travelRouteList, pageable, travelRouteList.size()));


        // when
        Page<RouteResponse> response = scheduleService.getTravelRoutes(schedule.getScheduleId(), 1);

        // then
        List<RouteResponse> content = response.getContent();
        assertEquals(response.getTotalElements(), travelRouteList.size());
        assertEquals(content.get(0).getAddress(), travelPlace.getAddress());
        assertEquals(content.get(0).getThumbnailUrl(), travelImage1.getFile().getS3ObjectUrl());
        assertEquals(content.get(0).getRouteOrder(), route1.getRouteOrder());
        assertEquals(content.get(1).getRouteOrder(), route2.getRouteOrder());
        assertEquals(content.get(2).getRouteOrder(), route3.getRouteOrder());
    }

    @Test
    @DisplayName("getTravelRoutes(): 여행 루트 조회 시 저장된 여행 루트 데이터 없는 경우")
    void getTravelRoutesWithoutData(){
        // given
        TravelSchedule schedule = createTravelSchedule(1L, "테스트");

        Pageable pageable = PageUtil.createPageRequest(1, 5);

        when(travelScheduleRepository.findByScheduleId(any())).thenReturn(Optional.of(schedule));
        when(travelRouteRepository.findAllByTravelSchedule_ScheduleId(pageable, schedule.getScheduleId()))
                .thenReturn(PageUtil.createPage(new ArrayList<>(), pageable, 0));


        // when
        Page<RouteResponse> response = scheduleService.getTravelRoutes(schedule.getScheduleId(), 1);

        // then
        assertEquals(response.getTotalElements(), 0);
        assertTrue(response.getContent().isEmpty());
    }

    @Test
    @DisplayName("getTravelRoutes(): 여행 루트 조회 시 일정을 찾을 수 없어 DataNotFoundException 발생")
    void getTravelRoutes_dataNotFoundException(){
        // given
        when(travelScheduleRepository.findByScheduleId(any())).thenReturn(Optional.empty());

        // when
        DataNotFoundException fail = assertThrows(DataNotFoundException.class, () -> scheduleService.getTravelRoutes(0L, 1));

        // then
        assertEquals(fail.getHttpStatus(), ErrorCode.DATA_NOT_FOUND.getStatus());
        assertEquals(fail.getMessage(), ErrorCode.DATA_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("getSimpleTravelPlacesByJunggu(): 중구 기준 여행지 데이터 조회")
    void getSimpleTravelPlacesByJunggu(){
        // given
        Country country = createCountry();
        City city = createCity(country);
        District district = createDistrict(city, "중구");
        ApiCategory apiCategory = createApiCategory();
        TravelPlace travelPlace1 = createTravelPlace(1L, country, city, district, apiCategory);
        TravelPlace travelPlace2 = createTravelPlace(2L, country, city, district, apiCategory);

        File file1 = createFile("test1", true);
        File file2 = createFile("test2", false);

        TravelImage travelImage1 = createTravelImage(travelPlace1, file1);
        TravelImage travelImage2 = createTravelImage(travelPlace1, file2);
        List<TravelImage> travelImageList1 = Arrays.asList(travelImage1, travelImage2);
        travelPlace1.setTravelImageList(travelImageList1);

        TravelImage travelImage3 = createTravelImage(travelPlace2, file1);
        TravelImage travelImage4 = createTravelImage(travelPlace2, file2);
        List<TravelImage> travelImageList2 = Arrays.asList(travelImage3, travelImage4);
        travelPlace2.setTravelImageList(travelImageList2);

        List<TravelPlace> placeList = new ArrayList<>();
        placeList.add(travelPlace1);
        placeList.add(travelPlace2);

        Pageable pageable = PageUtil.createPageRequest(1, 5);

        when(travelPlaceRepository.findAllByAreaData(pageable, "대한민국", "서울", "중구"))
                .thenReturn(PageUtil.createPage(placeList, pageable, placeList.size()));


        // when
        Page<PlaceResponse> response = scheduleService.getSimpleTravelPlacesByJunggu(1);

        // then
        List<PlaceResponse> content = response.getContent();
        assertEquals(response.getTotalElements(), placeList.size());
        assertEquals(content.get(0).getAddress(), travelPlace1.getAddress());
        assertEquals(content.get(0).getThumbnailUrl(), travelImage1.getFile().getS3ObjectUrl());
    }

    @Test
    @DisplayName("getSimpleTravelPlacesByJunggu(): 중구 기준 여행지 데이터 조회 시 데이터 없는 경우")
    void getSimpleTravelPlacesByJungguWithoutData(){
        // given
        Pageable pageable = PageUtil.createPageRequest(1, 5);

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
        String userId = "test1";
        Member member = createMember(1L, userId);

        when(memberRepository.findByUserId(any())).thenReturn(Optional.of(member));

        // when
        Member response = scheduleService.getSavedMember(userId);

        // then
        assertEquals(response.getUserId(), userId);
        assertEquals(response.getEmail(), member.getEmail());
        assertEquals(response.getNickname(), member.getNickname());

    }

    @Test
    @DisplayName("getSavedMember(): 저장된 사용자 정보 조회 시 데이터 찾을 수 없어 DataNotFoundException 발생")
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
        TravelSchedule schedule = createTravelSchedule(1L, "테스트");

        when(travelScheduleRepository.findByScheduleId(any())).thenReturn(Optional.of(schedule));

        // when
        TravelSchedule response = scheduleService.getSavedSchedule(schedule.getScheduleId());

        // then
        assertEquals(response.getScheduleName(), schedule.getScheduleName());
        assertEquals(response.getStartDate(), schedule.getStartDate());
        assertEquals(response.getEndDate(), schedule.getEndDate());
    }

    @Test
    @DisplayName("getSavedSchedule(): 저장된 일정 조회 시 데이터 찾을 수 없어 DataNotFoundException 발생")
    void getSavedSchedule_dataNotFoundException(){
        // given
        when(travelScheduleRepository.findByScheduleId(any())).thenReturn(Optional.empty());

        // when
        DataNotFoundException fail = assertThrows(DataNotFoundException.class, () -> scheduleService.getSavedSchedule(0L));

        // then
        assertEquals(fail.getHttpStatus(), ErrorCode.DATA_NOT_FOUND.getStatus());
        assertEquals(fail.getMessage(), ErrorCode.DATA_NOT_FOUND.getMessage());
    }

}
