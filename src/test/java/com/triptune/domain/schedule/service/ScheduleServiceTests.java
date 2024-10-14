package com.triptune.domain.schedule.service;

import com.triptune.domain.common.entity.*;
import com.triptune.domain.member.entity.Member;
import com.triptune.domain.member.repository.MemberRepository;
import com.triptune.domain.schedule.ScheduleTest;
import com.triptune.domain.schedule.dto.CreateScheduleRequest;
import com.triptune.domain.schedule.dto.CreateScheduleResponse;
import com.triptune.domain.schedule.dto.RouteResponse;
import com.triptune.domain.schedule.dto.ScheduleResponse;
import com.triptune.domain.schedule.entity.TravelAttendee;
import com.triptune.domain.schedule.entity.TravelRoute;
import com.triptune.domain.schedule.entity.TravelSchedule;
import com.triptune.domain.schedule.repository.TravelAttendeeRepository;
import com.triptune.domain.schedule.repository.TravelRouteRepository;
import com.triptune.domain.schedule.repository.TravelScheduleRepository;
import com.triptune.domain.travel.dto.PlaceSimpleResponse;
import com.triptune.domain.travel.entity.TravelImage;
import com.triptune.domain.travel.entity.TravelPlace;
import com.triptune.domain.travel.repository.TravelPlacePlaceRepository;
import com.triptune.global.enumclass.ErrorCode;
import com.triptune.global.exception.DataNotFoundException;
import com.triptune.global.util.PageableUtil;
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
    @DisplayName("createSchedule(): 일정 만들기 성공")
    void createSchedule_success(){
        // given
        String userId = "test";
        CreateScheduleRequest request = createScheduleRequest();
        TravelSchedule savedtravelSchedule = createTravelSchedule();
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
        TravelSchedule schedule = createTravelSchedule();

        when(travelScheduleRepository.save(any())).thenReturn(schedule);
        when(memberRepository.findByUserId(any())).thenReturn(Optional.empty());

        // when
        DataNotFoundException fail = assertThrows(DataNotFoundException.class, () -> scheduleService.createSchedule(request, "test"));

        // then
        assertEquals(fail.getHttpStatus(), ErrorCode.USER_NOT_FOUND.getStatus());
        assertEquals(fail.getMessage(), ErrorCode.USER_NOT_FOUND.getMessage());

    }

    @Test
    @DisplayName("getSchedule(): 일정 조회 성공")
    void getSchedule(){
        // given
        Country country = createCountry();
        City city = createCity(country);
        District district = createDistrict(city, "중구");
        ApiCategory apiCategory = createApiCategory();
        TravelPlace travelPlace1 = createTravelPlace(country, city, district, apiCategory);
        TravelPlace travelPlace2 = createTravelPlace(country, city, district, apiCategory);

        File file1 = createFile(1L, "test1", true);
        File file2 = createFile(2L, "test2", false);

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

        TravelSchedule schedule = createTravelSchedule();
        Member member1 = createMember(1L, "member1");
        Member member2 = createMember(2L, "member2");

        List<TravelAttendee> attendeeList = new ArrayList<>();
        attendeeList.add(createTravelAttendee(member1, schedule));
        attendeeList.add(createTravelAttendee(member2, schedule));

        Pageable pageable = PageableUtil.createPageRequest(1, 5);

        when(travelScheduleRepository.findByScheduleId(any())).thenReturn(Optional.of(schedule));
        when(travelPlaceRepository.findAllByAreaData(any(), anyString(), anyString(), anyString()))
                .thenReturn(new PageImpl<>(placeList, pageable, 1));
        when(travelAttendeeRepository.findAllByTravelSchedule_ScheduleId(any())).thenReturn(attendeeList);

        // when
        ScheduleResponse response = scheduleService.getSchedule(schedule.getScheduleId(), 1);

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
    @DisplayName("getSchedule(): 일정 조회 시 여행지 데이터 없는 경우")
    void getScheduleWithoutData(){
        // given
        TravelSchedule schedule = createTravelSchedule();
        Member member1 = createMember(1L, "member1");
        Member member2 = createMember(2L, "member2");

        List<TravelAttendee> attendeeList = new ArrayList<>();
        attendeeList.add(createTravelAttendee(member1, schedule));
        attendeeList.add(createTravelAttendee(member2, schedule));

        Pageable pageable = PageableUtil.createPageRequest(1, 5);

        when(travelScheduleRepository.findByScheduleId(any())).thenReturn(Optional.of(schedule));
        when(travelPlaceRepository.findAllByAreaData(any(), anyString(), anyString(), anyString()))
                .thenReturn(new PageImpl<>(new ArrayList<>(), pageable, 0));
        when(travelAttendeeRepository.findAllByTravelSchedule_ScheduleId(any())).thenReturn(attendeeList);

        // when
        ScheduleResponse response = scheduleService.getSchedule(schedule.getScheduleId(), 1);

        // then
        assertEquals(response.getScheduleName(), schedule.getScheduleName());
        assertEquals(response.getCreatedAt(), schedule.getCreatedAt());
        assertEquals(response.getAttendeeList().get(0).getUserId(), attendeeList.get(0).getMember().getUserId());
        assertEquals(response.getAttendeeList().get(0).getRole(), attendeeList.get(0).getRole().name());
        assertEquals(response.getPlaceList().getTotalElements(), 0);
        assertTrue(response.getPlaceList().getContent().isEmpty());
    }

    @Test
    @DisplayName("getSchedule(): 일정 조회 시 일정을 찾을 수 없어 DataNotFoundException 발생")
    void getSchedule_dataNotFoundException(){
        // given
        when(travelScheduleRepository.findByScheduleId(any())).thenReturn(Optional.empty());

        // when
        DataNotFoundException fail = assertThrows(DataNotFoundException.class, () -> scheduleService.getSchedule(0L, 1));

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
        TravelPlace travelPlace1 = createTravelPlace(country, city, district, apiCategory);
        TravelPlace travelPlace2 = createTravelPlace(country, city, district, apiCategory);

        File file1 = createFile(1L, "test1", true);
        File file2 = createFile(2L, "test2", false);

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

        TravelSchedule schedule = createTravelSchedule();

        Pageable pageable = PageableUtil.createPageRequest(1, 5);

        when(travelScheduleRepository.findByScheduleId(any())).thenReturn(Optional.of(schedule));
        when(travelPlaceRepository.findAllByAreaData(any(), anyString(), anyString(), anyString()))
                .thenReturn(new PageImpl<>(placeList, pageable, 1));

        // when
        Page<PlaceSimpleResponse> response = scheduleService.getTravelPlaces(schedule.getScheduleId(), 1);

        // then
        assertEquals(response.getTotalElements(), placeList.size());
        assertEquals(response.getContent().get(0).getPlaceName(), placeList.get(0).getPlaceName());
        assertNotNull(response.getContent().get(0).getThumbnailUrl());
    }

    @Test
    @DisplayName("getTravelPlaces(): 여행지 조회 시 여행지 데이터 없는 경우")
    void getTravelPlacesWithoutData(){
        // given
        TravelSchedule schedule = createTravelSchedule();
        Pageable pageable = PageableUtil.createPageRequest(1, 5);

        when(travelScheduleRepository.findByScheduleId(any())).thenReturn(Optional.of(schedule));
        when(travelPlaceRepository.findAllByAreaData(any(), anyString(), anyString(), anyString()))
                .thenReturn(new PageImpl<>(new ArrayList<>(), pageable, 0));

        // when
        Page<PlaceSimpleResponse> response = scheduleService.getTravelPlaces(schedule.getScheduleId(), 1);

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
        Pageable pageable = PageableUtil.createPageRequest(1, 5);

        Country country = createCountry();
        City city = createCity(country);
        District district = createDistrict(city, "강남구");
        ApiCategory apiCategory = createApiCategory();
        TravelPlace travelPlace = createTravelPlace(country, city, district, apiCategory);

        List<TravelPlace> travelPlaceList = new ArrayList<>();
        travelPlaceList.add(travelPlace);

        File file1 = createFile(1L, "test1", true);
        File file2 = createFile(2L, "test2", false);

        TravelImage travelImage1 = createTravelImage(travelPlace, file1);
        TravelImage travelImage2 = createTravelImage(travelPlace, file2);
        List<TravelImage> travelImageList = Arrays.asList(travelImage1, travelImage2);
        travelPlace.setTravelImageList(travelImageList);

        Page<TravelPlace> travelPlacePage = new PageImpl<>(travelPlaceList, pageable, travelPlaceList.size());

        when(travelScheduleRepository.findByScheduleId(any())).thenReturn(Optional.of(createTravelSchedule()));
        when(travelPlaceRepository.searchTravelPlaces(pageable, keyword)).thenReturn(travelPlacePage);

        // when
        Page<PlaceSimpleResponse> response = scheduleService.searchTravelPlaces(1L, 1, keyword);


        // then
        List<PlaceSimpleResponse> content = response.getContent();
        assertEquals(content.get(0).getPlaceName(), travelPlace.getPlaceName());
        assertEquals(content.get(0).getAddress(), travelPlace.getAddress());
        assertEquals(content.get(0).getThumbnailUrl(), travelImage1.getFile().getS3ObjectUrl());
    }

    @Test
    @DisplayName("searchTravelPlaces(): 여행지 검색 시 검색 결과 존재하지 않는 경우")
    void searchTravelPlacesWithoutData(){
        // given
        String keyword = "ㅁㄴㅇㄹ";
        Pageable pageable = PageableUtil.createPageRequest(1, 5);

        Page<TravelPlace> travelPlacePage = new PageImpl<>(new ArrayList<>(), pageable, 0);

        when(travelScheduleRepository.findByScheduleId(any())).thenReturn(Optional.of(createTravelSchedule()));
        when(travelPlaceRepository.searchTravelPlaces(pageable, keyword)).thenReturn(travelPlacePage);

        // when
        Page<PlaceSimpleResponse> response = scheduleService.searchTravelPlaces(1L, 1, keyword);


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
        TravelSchedule schedule = createTravelSchedule();

        Country country = createCountry();
        City city = createCity(country);
        District district = createDistrict(city, "강남구");
        ApiCategory apiCategory = createApiCategory();
        TravelPlace travelPlace = createTravelPlace(country, city, district, apiCategory);

        File file1 = createFile(1L, "test1", true);
        File file2 = createFile(2L, "test2", false);

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

        Pageable pageable = PageableUtil.createPageRequest(1, 5);

        when(travelScheduleRepository.findByScheduleId(any())).thenReturn(Optional.of(schedule));
        when(travelRouteRepository.findAllByTravelSchedule_ScheduleId(pageable, schedule.getScheduleId()))
                .thenReturn(new PageImpl<>(travelRouteList, pageable, travelRouteList.size()));


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
        TravelSchedule schedule = createTravelSchedule();

        Pageable pageable = PageableUtil.createPageRequest(1, 5);

        when(travelScheduleRepository.findByScheduleId(any())).thenReturn(Optional.of(schedule));
        when(travelRouteRepository.findAllByTravelSchedule_ScheduleId(pageable, schedule.getScheduleId()))
                .thenReturn(new PageImpl<>(new ArrayList<>(), pageable, 0));


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
        TravelPlace travelPlace1 = createTravelPlace(country, city, district, apiCategory);
        TravelPlace travelPlace2 = createTravelPlace(country, city, district, apiCategory);

        File file1 = createFile(1L, "test1", true);
        File file2 = createFile(2L, "test2", false);

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

        Pageable pageable = PageableUtil.createPageRequest(1, 5);

        when(travelPlaceRepository.findAllByAreaData(pageable, "대한민국", "서울", "중구"))
                .thenReturn(new PageImpl<>(placeList, pageable, placeList.size()));


        // when
        Page<PlaceSimpleResponse> response = scheduleService.getSimpleTravelPlacesByJunggu(1);

        // then
        List<PlaceSimpleResponse> content = response.getContent();
        assertEquals(response.getTotalElements(), placeList.size());
        assertEquals(content.get(0).getAddress(), travelPlace1.getAddress());
        assertEquals(content.get(0).getThumbnailUrl(), travelImage1.getFile().getS3ObjectUrl());
    }

    @Test
    @DisplayName("getSimpleTravelPlacesByJunggu(): 중구 기준 여행지 데이터 조회 시 데이터 없는 경우")
    void getSimpleTravelPlacesByJungguWithoutData(){
        // given
        Pageable pageable = PageableUtil.createPageRequest(1, 5);

        when(travelPlaceRepository.findAllByAreaData(pageable, "대한민국", "서울", "중구"))
                .thenReturn(new PageImpl<>(new ArrayList<>(), pageable, 0));


        // when
        Page<PlaceSimpleResponse> response = scheduleService.getSimpleTravelPlacesByJunggu(1);

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
        TravelSchedule schedule = createTravelSchedule();

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
