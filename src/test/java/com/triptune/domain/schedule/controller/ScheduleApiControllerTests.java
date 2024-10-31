package com.triptune.domain.schedule.controller;

import com.triptune.domain.common.entity.*;
import com.triptune.domain.common.repository.*;
import com.triptune.domain.member.entity.Member;
import com.triptune.domain.member.entity.ProfileImage;
import com.triptune.domain.member.repository.MemberRepository;
import com.triptune.domain.member.repository.ProfileImageRepository;
import com.triptune.domain.schedule.ScheduleTest;
import com.triptune.domain.schedule.dto.request.CreateScheduleRequest;
import com.triptune.domain.schedule.dto.request.RouteRequest;
import com.triptune.domain.schedule.dto.request.UpdateScheduleRequest;
import com.triptune.domain.schedule.entity.TravelAttendee;
import com.triptune.domain.schedule.entity.TravelRoute;
import com.triptune.domain.schedule.entity.TravelSchedule;
import com.triptune.domain.schedule.enumclass.AttendeePermission;
import com.triptune.domain.schedule.enumclass.AttendeeRole;
import com.triptune.domain.schedule.repository.TravelAttendeeRepository;
import com.triptune.domain.schedule.repository.TravelRouteRepository;
import com.triptune.domain.schedule.repository.TravelScheduleRepository;
import com.triptune.domain.travel.entity.TravelImage;
import com.triptune.domain.travel.entity.TravelPlace;
import com.triptune.domain.travel.repository.TravelImageRepository;
import com.triptune.domain.travel.repository.TravelPlacePlaceRepository;
import com.triptune.global.enumclass.ErrorCode;
import org.hibernate.sql.Update;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application-test.yml")
public class ScheduleApiControllerTests extends ScheduleTest {
    private final WebApplicationContext wac;
    private final TravelScheduleRepository travelScheduleRepository;
    private final TravelAttendeeRepository travelAttendeeRepository;
    private final MemberRepository memberRepository;
    private final TravelPlacePlaceRepository travelPlaceRepository;
    private final CountryRepository countryRepository;
    private final CityRepository cityRepository;
    private final DistrictRepository districtRepository;
    private final ApiCategoryRepository apiCategoryRepository;
    private final FileRepository fileRepository;
    private final TravelImageRepository travelImageRepository;
    private final TravelRouteRepository travelRouteRepository;
    private final ApiContentTypeRepository apiContentTypeRepository;
    private final ProfileImageRepository profileImageRepository;

    private MockMvc mockMvc;

    private Member member1;
    private Member member2;
    private Member member3;

    private TravelSchedule schedule1;
    private TravelSchedule schedule2;
    private TravelSchedule schedule3;

    private TravelPlace travelPlace1;
    private TravelPlace travelPlace2;


    @Autowired
    public ScheduleApiControllerTests(WebApplicationContext wac, TravelScheduleRepository travelScheduleRepository, TravelAttendeeRepository travelAttendeeRepository, MemberRepository memberRepository, TravelPlacePlaceRepository travelPlaceRepository, CountryRepository countryRepository, CityRepository cityRepository, DistrictRepository districtRepository, ApiCategoryRepository apiCategoryRepository, FileRepository fileRepository, TravelImageRepository travelImageRepository, TravelRouteRepository travelRouteRepository, ApiContentTypeRepository apiContentTypeRepository, ProfileImageRepository profileImageRepository) {
        this.wac = wac;
        this.travelScheduleRepository = travelScheduleRepository;
        this.travelAttendeeRepository = travelAttendeeRepository;
        this.memberRepository = memberRepository;
        this.travelPlaceRepository = travelPlaceRepository;
        this.countryRepository = countryRepository;
        this.cityRepository = cityRepository;
        this.districtRepository = districtRepository;
        this.apiCategoryRepository = apiCategoryRepository;
        this.fileRepository = fileRepository;
        this.travelImageRepository = travelImageRepository;
        this.travelRouteRepository = travelRouteRepository;
        this.apiContentTypeRepository = apiContentTypeRepository;
        this.profileImageRepository = profileImageRepository;
    }

    @BeforeEach
    void setUp(){
        mockMvc = MockMvcBuilders.webAppContextSetup(wac)
                .addFilter(new CharacterEncodingFilter("UTF-8", true))
                .apply(springSecurity())
                .alwaysDo(print())
                .build();

        member1 = memberRepository.save(createMember(null, "member1"));
        member2 = memberRepository.save(createMember(null, "member2"));
        member3 = memberRepository.save(createMember(null, "member3"));
        ProfileImage profileImage1 = profileImageRepository.save(createProfileImage(null, "member1Image"));
        ProfileImage profileImage2 = profileImageRepository.save(createProfileImage(null, "member2Image"));
        ProfileImage profileImage3 = profileImageRepository.save(createProfileImage(null, "member3Image"));
        member1.setProfileImage(profileImage1);
        member2.setProfileImage(profileImage2);
        member3.setProfileImage(profileImage3);

        Country country = countryRepository.save(createCountry());
        City city = cityRepository.save(createCity(country));
        District district1 = districtRepository.save(createDistrict(city, "강남구"));
        District district2 = districtRepository.save(createDistrict(city, "중구"));
        ApiCategory apiCategory = apiCategoryRepository.save(createApiCategory());
        ApiContentType apiContentType = apiContentTypeRepository.save(createApiContentType("관광지"));
        travelPlace1 = travelPlaceRepository.save(createTravelPlace(null, country, city, district1, apiCategory));
        travelPlace2 = travelPlaceRepository.save(createTravelPlace(null, country, city, district2, apiCategory));
        File file1 = fileRepository.save(createFile("test1", true));
        File file2 = fileRepository.save(createFile("test2", false));
        TravelImage travelImage1 = travelImageRepository.save(createTravelImage(travelPlace1, file1));
        TravelImage travelImage2 = travelImageRepository.save(createTravelImage(travelPlace1, file2));
        TravelImage travelImage3 = travelImageRepository.save(createTravelImage(travelPlace2, file1));
        TravelImage travelImage4 = travelImageRepository.save(createTravelImage(travelPlace2, file2));
        travelPlace1.setApiContentType(apiContentType);
        travelPlace1.setTravelImageList(Arrays.asList(travelImage1, travelImage2));
        travelPlace2.setApiContentType(apiContentType);
        travelPlace2.setTravelImageList(Arrays.asList(travelImage3, travelImage4));


        schedule1 = travelScheduleRepository.save(createTravelSchedule(null,"테스트1"));
        schedule2 = travelScheduleRepository.save(createTravelSchedule(null,"테스트2"));
        schedule3 = travelScheduleRepository.save(createTravelSchedule(null,"테스트3"));


    }

    @Test
    @DisplayName("getSchedules(): 내가 참석한 여행지 목록 조회")
    @WithMockUser(username = "member1")
    void getSchedules() throws Exception {
        // given
        TravelAttendee attendee1 = travelAttendeeRepository.save(createTravelAttendee(member1, schedule1, AttendeeRole.AUTHOR, AttendeePermission.ALL));
        TravelAttendee attendee2 = travelAttendeeRepository.save(createTravelAttendee(member1, schedule2, AttendeeRole.AUTHOR, AttendeePermission.ALL));
        TravelAttendee attendee3 = travelAttendeeRepository.save(createTravelAttendee(member2, schedule1, AttendeeRole.GUEST, AttendeePermission.READ));
        TravelAttendee attendee4 = travelAttendeeRepository.save(createTravelAttendee(member3, schedule3, AttendeeRole.AUTHOR, AttendeePermission.ALL));

        TravelRoute route1 = travelRouteRepository.save(createTravelRoute(schedule1, travelPlace1, 1));
        TravelRoute route2 = travelRouteRepository.save(createTravelRoute(schedule1, travelPlace2, 2));
        TravelRoute route3 = travelRouteRepository.save(createTravelRoute(schedule2, travelPlace1, 1));
        TravelRoute route4 = travelRouteRepository.save(createTravelRoute(schedule2, travelPlace2, 2));

        member1.setTravelAttendeeList(List.of(attendee1, attendee2));
        member2.setTravelAttendeeList(List.of(attendee3));
        member3.setTravelAttendeeList(List.of(attendee4));

        schedule1.setTravelRouteList(Arrays.asList(route1, route2));
        schedule2.setTravelRouteList(Arrays.asList(route3, route4));
        schedule1.setTravelAttendeeList(Arrays.asList(attendee1, attendee3));
        schedule2.setTravelAttendeeList(List.of(attendee2));
        schedule3.setTravelAttendeeList(List.of(attendee4));

        // when, then
        mockMvc.perform(get("/api/schedules")
                .param("page", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(2))
                .andExpect(jsonPath("$.data.totalSharedElements").value(1))
                .andExpect(jsonPath("$.data.content[0].sinceUpdate").exists())
                .andExpect(jsonPath("$.data.content[0].scheduleName").exists())
                .andExpect(jsonPath("$.data.content[0].thumbnailUrl").exists());
    }

    @Test
    @DisplayName("getSchedules(): 내가 참석한 여행지 목록 조회 시 여행 루트 데이터가 없는 경우")
    @WithMockUser(username = "member1")
    void getSchedulesNoRouteData() throws Exception {
        // given
        schedule1.setUpdatedAt(LocalDateTime.now().minusDays(3));

        TravelAttendee attendee1 = travelAttendeeRepository.save(createTravelAttendee(member1, schedule1, AttendeeRole.AUTHOR, AttendeePermission.ALL));
        TravelAttendee attendee2 = travelAttendeeRepository.save(createTravelAttendee(member1, schedule2, AttendeeRole.AUTHOR, AttendeePermission.ALL));
        TravelAttendee attendee3 = travelAttendeeRepository.save(createTravelAttendee(member2, schedule1, AttendeeRole.GUEST, AttendeePermission.READ));
        TravelAttendee attendee4 = travelAttendeeRepository.save(createTravelAttendee(member3, schedule3, AttendeeRole.AUTHOR, AttendeePermission.ALL));

        member1.setTravelAttendeeList(List.of(attendee1, attendee2));
        member2.setTravelAttendeeList(List.of(attendee3));
        member3.setTravelAttendeeList(List.of(attendee4));

        schedule1.setTravelAttendeeList(Arrays.asList(attendee1, attendee3));
        schedule2.setTravelAttendeeList(List.of(attendee2));
        schedule3.setTravelAttendeeList(List.of(attendee4));

        // when, then
        mockMvc.perform(get("/api/schedules")
                        .param("page", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(2))
                .andExpect(jsonPath("$.data.content[0].sinceUpdate").exists())
                .andExpect(jsonPath("$.data.content[0].scheduleName").exists())
                .andExpect(jsonPath("$.data.content[0].thumbnailUrl").isEmpty());

    }

    @Test
    @DisplayName("getSchedules(): 내가 참석한 여행지 목록 조회 시 데이터 없는 경우")
    @WithMockUser(username = "member1")
    void getSchedulesWithoutData() throws Exception {
        // given
        // when, then
        mockMvc.perform(get("/api/schedules")
                        .param("page", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(0))
                .andExpect(jsonPath("$.data.content").isEmpty());
    }


    @Test
    @DisplayName("createSchedule(): 일정 만들기 성공")
    @WithMockUser(username = "member1")
    void createSchedule() throws Exception{
        // given
        CreateScheduleRequest request = createScheduleRequest();

        // when, then
        mockMvc.perform(post("/api/schedules")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJsonString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }


    @Test
    @DisplayName("createSchedule(): 일정 만들기 시 필요 입력값이 다 안들어와 예외 발생")
    @WithMockUser(username = "test")
    void createSchedule_methodArgumentNotValidException() throws Exception{
        // given
        CreateScheduleRequest request = createScheduleRequest();
        request.setStartDate(null);

        // when, then
        mockMvc.perform(post("/api/schedules")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("createSchedule(): 일정 만들기 시 오늘 이전 날짜 입력으로 예외 발생")
    @WithMockUser(username = "test")
    void createSchedulePastDate_methodArgumentNotValidException() throws Exception{
        // given
        CreateScheduleRequest request = createScheduleRequest();
        request.setStartDate(LocalDate.now().minusDays(3));

        // when, then
        mockMvc.perform(post("/api/schedules")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("getScheduleDetail(): 일정 상세 조회 성공")
    @WithMockUser(username = "member1")
    void getScheduleDetail() throws Exception {
        // given
        TravelAttendee attendee1 = travelAttendeeRepository.save(createTravelAttendee(member1, schedule1, AttendeeRole.AUTHOR, AttendeePermission.ALL));
        TravelAttendee attendee2 = travelAttendeeRepository.save(createTravelAttendee(member2, schedule1, AttendeeRole.GUEST, AttendeePermission.READ));

        member1.setTravelAttendeeList(List.of(attendee1, attendee2));
        schedule1.setTravelAttendeeList(Arrays.asList(attendee1, attendee2));


        // when, then
        mockMvc.perform(get("/api/schedules/{scheduleId}", schedule1.getScheduleId())
                .param("page", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.scheduleName").value(schedule1.getScheduleName()))
                .andExpect(jsonPath("$.data.attendeeList[0].userId").exists())
                .andExpect(jsonPath("$.data.placeList.totalElements").value(1))
                .andExpect(jsonPath("$.data.placeList.content[0].district").value(travelPlace2.getDistrict().getDistrictName()))
                .andExpect(jsonPath("$.data.placeList.content[0].placeName").value(travelPlace2.getPlaceName()))
                .andExpect(jsonPath("$.data.placeList.content[0].thumbnailUrl").exists())
                .andExpect(jsonPath("$.data.attendeeList[0].userId").value(member1.getUserId()))
                .andExpect(jsonPath("$.data.attendeeList[0].role").value("AUTHOR"));
    }

    @Test
    @DisplayName("getScheduleDetail(): 일정 조회 시 여행지 데이터 존재하지 않는 경우")
    @WithMockUser(username = "member1")
    void getScheduleDetailWithoutData() throws Exception {
        // given
        travelPlace2.getDistrict().setDistrictName("성북구");

        TravelAttendee attendee1 = travelAttendeeRepository.save(createTravelAttendee(member1, schedule1, AttendeeRole.AUTHOR, AttendeePermission.ALL));
        TravelAttendee attendee2 = travelAttendeeRepository.save(createTravelAttendee(member2, schedule1, AttendeeRole.GUEST, AttendeePermission.READ));
        
        member1.setTravelAttendeeList(List.of(attendee1, attendee2));
        schedule1.setTravelAttendeeList(Arrays.asList(attendee1, attendee2));


        // when, then
        mockMvc.perform(get("/api/schedules/{scheduleId}", schedule1.getScheduleId())
                        .param("page", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.scheduleName").value(schedule1.getScheduleName()))
                .andExpect(jsonPath("$.data.placeList.totalElements").value(0))
                .andExpect(jsonPath("$.data.placeList.content").isEmpty())
                .andExpect(jsonPath("$.data.attendeeList[0].userId").value(member1.getUserId()))
                .andExpect(jsonPath("$.data.attendeeList[0].role").value("AUTHOR"));
    }

    @Test
    @DisplayName("getScheduleDetail(): 일정 조회 시 일정 데이터 존재하지 않아 예외 발생")
    @WithMockUser(username = "member1")
    void getScheduleDetail_dataNotFoundException() throws Exception {
        // given
        // when, then
        mockMvc.perform(get("/api/schedules/{scheduleId}", 0L)
                        .param("page", "1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.SCHEDULE_NOT_FOUND.getMessage()));
    }

    @Test
    @DisplayName("updateSchedule(): 일정 수정")
    @WithMockUser(username = "member1")
    void updateSchedule() throws Exception {
        // given
        TravelAttendee attendee1 = travelAttendeeRepository.save(createTravelAttendee(member1, schedule1, AttendeeRole.AUTHOR, AttendeePermission.ALL));
        TravelAttendee attendee3 = travelAttendeeRepository.save(createTravelAttendee(member2, schedule1, AttendeeRole.GUEST, AttendeePermission.READ));

        TravelRoute route1 = travelRouteRepository.save(createTravelRoute(schedule1, travelPlace1, 1));
        TravelRoute route2 = travelRouteRepository.save(createTravelRoute(schedule1, travelPlace2, 2));

        member1.setTravelAttendeeList(new ArrayList<>(List.of(attendee1)));
        member2.setTravelAttendeeList(new ArrayList<>(List.of(attendee3)));
        schedule1.setTravelRouteList(new ArrayList<>(List.of(route1, route2)));
        schedule1.setTravelAttendeeList(new ArrayList<>(List.of(attendee1, attendee3)));

        RouteRequest routeRequest1 = createRouteRequest(1, travelPlace1.getPlaceId());
        RouteRequest routeRequest2 = createRouteRequest(2, travelPlace2.getPlaceId());

        UpdateScheduleRequest request = createUpdateScheduleRequest(Arrays.asList(routeRequest1, routeRequest2));

        // when
        mockMvc.perform(patch("/api/schedules/{scheduleId}", schedule1.getScheduleId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // then
        assertEquals(schedule1.getScheduleName(), request.getScheduleName());
        assertEquals(schedule1.getTravelRouteList().size(), 2);
    }

    @Test
    @DisplayName("updateSchedule(): 일정 수정 시 일정 종료일을 오늘 날짜 이전으로 설정해 예외 발생")
    @WithMockUser(username = "member1")
    void updateSchedule_methodArgumentNotValidException() throws Exception {
        // given
        TravelAttendee attendee1 = travelAttendeeRepository.save(createTravelAttendee(member1, schedule1, AttendeeRole.AUTHOR, AttendeePermission.ALL));
        TravelAttendee attendee3 = travelAttendeeRepository.save(createTravelAttendee(member2, schedule1, AttendeeRole.GUEST, AttendeePermission.READ));

        TravelRoute route1 = travelRouteRepository.save(createTravelRoute(schedule1, travelPlace1, 1));
        TravelRoute route2 = travelRouteRepository.save(createTravelRoute(schedule1, travelPlace2, 2));

        member1.setTravelAttendeeList(new ArrayList<>(List.of(attendee1)));
        member2.setTravelAttendeeList(new ArrayList<>(List.of(attendee3)));
        schedule1.setTravelRouteList(new ArrayList<>(List.of(route1, route2)));
        schedule1.setTravelAttendeeList(new ArrayList<>(List.of(attendee1, attendee3)));

        RouteRequest routeRequest1 = createRouteRequest(1, travelPlace1.getPlaceId());
        RouteRequest routeRequest2 = createRouteRequest(2, travelPlace2.getPlaceId());

        UpdateScheduleRequest request = createUpdateScheduleRequest(new ArrayList<>(List.of(routeRequest1, routeRequest2)));
        request.setEndDate(LocalDate.now().minusDays(10));


        // when
        mockMvc.perform(patch("/api/schedules/{scheduleId}", schedule1.getScheduleId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));

    }

    @Test
    @DisplayName("updateSchedule(): 일정 수정 시 저장된 여행 루트 없는 경우")
    @WithMockUser(username = "member1")
    void updateScheduleNoSavedTravelRoute() throws Exception {
        // given
        TravelAttendee attendee1 = travelAttendeeRepository.save(createTravelAttendee(member1, schedule1, AttendeeRole.AUTHOR, AttendeePermission.ALL));
        TravelAttendee attendee3 = travelAttendeeRepository.save(createTravelAttendee(member2, schedule1, AttendeeRole.GUEST, AttendeePermission.READ));

        member1.setTravelAttendeeList(List.of(attendee1));
        member2.setTravelAttendeeList(List.of(attendee3));
        schedule1.setTravelAttendeeList(Arrays.asList(attendee1, attendee3));

        RouteRequest routeRequest1 = createRouteRequest(1, travelPlace1.getPlaceId());
        UpdateScheduleRequest request = createUpdateScheduleRequest(List.of(routeRequest1));

        // when
        mockMvc.perform(patch("/api/schedules/{scheduleId}", schedule1.getScheduleId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // then
        assertEquals(schedule1.getScheduleName(), request.getScheduleName());
        assertEquals(schedule1.getTravelRouteList().size(), 1);
    }

    @Test
    @DisplayName("updateSchedule(): 일정 수정 시 접근 권한이 없어 예외 발생")
    @WithMockUser(username = "member3")
    void updateScheduleForbiddenAccess_forbiddenScheduleException() throws Exception {
        // given
        TravelAttendee attendee1 = travelAttendeeRepository.save(createTravelAttendee(member1, schedule1, AttendeeRole.AUTHOR, AttendeePermission.ALL));
        TravelAttendee attendee3 = travelAttendeeRepository.save(createTravelAttendee(member2, schedule1, AttendeeRole.GUEST, AttendeePermission.READ));

        TravelRoute route1 = travelRouteRepository.save(createTravelRoute(schedule1, travelPlace1, 1));
        TravelRoute route2 = travelRouteRepository.save(createTravelRoute(schedule1, travelPlace2, 2));

        member1.setTravelAttendeeList(List.of(attendee1));
        member2.setTravelAttendeeList(List.of(attendee3));
        schedule1.setTravelRouteList(Arrays.asList(route1, route2));
        schedule1.setTravelAttendeeList(Arrays.asList(attendee1, attendee3));

        RouteRequest routeRequest1 = createRouteRequest(1, travelPlace1.getPlaceId());
        UpdateScheduleRequest request = createUpdateScheduleRequest(List.of(routeRequest1));

        // when
        mockMvc.perform(patch("/api/schedules/{scheduleId}", schedule1.getScheduleId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value(ErrorCode.FORBIDDEN_ACCESS_SCHEDULE.getMessage()));

        // then
        assertEquals(schedule1.getScheduleName(), "테스트1");
        assertEquals(schedule1.getTravelRouteList().size(), 2);
    }


    @Test
    @DisplayName("updateSchedule(): 일정 수정 시 수정 권한이 없어 예외 발생")
    @WithMockUser(username = "member2")
    void updateScheduleForbiddenEdit_forbiddenScheduleException() throws Exception {
        // given
        TravelAttendee attendee1 = travelAttendeeRepository.save(createTravelAttendee(member1, schedule1, AttendeeRole.AUTHOR, AttendeePermission.ALL));
        TravelAttendee attendee3 = travelAttendeeRepository.save(createTravelAttendee(member2, schedule1, AttendeeRole.GUEST, AttendeePermission.READ));

        TravelRoute route1 = travelRouteRepository.save(createTravelRoute(schedule1, travelPlace1, 1));
        TravelRoute route2 = travelRouteRepository.save(createTravelRoute(schedule1, travelPlace2, 2));

        member1.setTravelAttendeeList(List.of(attendee1));
        member2.setTravelAttendeeList(List.of(attendee3));
        schedule1.setTravelRouteList(Arrays.asList(route1, route2));
        schedule1.setTravelAttendeeList(Arrays.asList(attendee1, attendee3));

        RouteRequest routeRequest1 = createRouteRequest(1, travelPlace1.getPlaceId());
        UpdateScheduleRequest request = createUpdateScheduleRequest(List.of(routeRequest1));

        // when
        mockMvc.perform(patch("/api/schedules/{scheduleId}", schedule1.getScheduleId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value(ErrorCode.FORBIDDEN_EDIT_SCHEDULE.getMessage()));

        // then
        assertEquals(schedule1.getScheduleName(), "테스트1");
        assertEquals(schedule1.getTravelRouteList().size(), 2);
    }


    @Test
    @DisplayName("getTravelPlaces(): 여행지 조회 성공")
    @WithMockUser(username = "member1")
    void getTravelPlaces() throws Exception {
        // given
        // when, then
        mockMvc.perform(get("/api/schedules/{scheduleId}/travels", schedule1.getScheduleId())
                        .param("page", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(1))
                .andExpect(jsonPath("$.data.content[0].district").value(travelPlace2.getDistrict().getDistrictName()))
                .andExpect(jsonPath("$.data.content[0].placeName").value(travelPlace2.getPlaceName()))
                .andExpect(jsonPath("$.data.content[0].longitude").isNotEmpty())
                .andExpect(jsonPath("$.data.content[0].latitude").isNotEmpty())
                .andExpect(jsonPath("$.data.content[0].thumbnailUrl").exists());
    }


    @Test
    @DisplayName("getTravelPlaces(): 여행지 조회 시 여행지 데이터 존재하지 않는 경우")
    @WithMockUser(username = "member1")
    void getTravelPlacesWithoutData() throws Exception {
        // given
        travelPlaceRepository.deleteAll();
        
        // when, then
        mockMvc.perform(get("/api/schedules/{scheduleId}/travels", schedule1.getScheduleId())
                        .param("page", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(0))
                .andExpect(jsonPath("$.data.content").isEmpty());
    }


    @Test
    @DisplayName("getTravelPlaces(): 여행지 조회 시 일정 데이터 존재하지 않아 예외 발생")
    @WithMockUser(username = "member1")
    void getTravelPlaces_dataNotFoundException() throws Exception {
        // given
        // when, then
        mockMvc.perform(get("/api/schedules/{scheduleId}/travels", 0L)
                        .param("page", "1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.SCHEDULE_NOT_FOUND.getMessage()));
    }


    @Test
    @DisplayName("searchTravelPlaces(): 여행지 검색 성공")
    @WithMockUser(username = "member1")
    void searchTravelPlaces() throws Exception {
        // given
        // when, then
        mockMvc.perform(get("/api/schedules/{scheduleId}/travels/search", schedule1.getScheduleId())
                        .param("page", "1")
                        .param("keyword", "강남"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(1))
                .andExpect(jsonPath("$.data.content[0].district").value(travelPlace1.getDistrict().getDistrictName()))
                .andExpect(jsonPath("$.data.content[0].placeName").value(travelPlace1.getPlaceName()))
                .andExpect(jsonPath("$.data.content[0].longitude").isNotEmpty())
                .andExpect(jsonPath("$.data.content[0].latitude").isNotEmpty())
                .andExpect(jsonPath("$.data.content[0].thumbnailUrl").exists());
    }

    @Test
    @DisplayName("searchTravelPlaces(): 여행지 검색 시 검색 결과가 존재하지 않는 경우")
    @WithMockUser(username = "member1")
    void searchTravelPlacesWithoutData() throws Exception {
        // given
        // when, then
        mockMvc.perform(get("/api/schedules/{scheduleId}/travels/search", schedule1.getScheduleId())
                        .param("page", "1")
                        .param("keyword", "ㅁㄴㅇㄹ"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(0))
                .andExpect(jsonPath("$.data.content").isEmpty());
    }


    @Test
    @DisplayName("searchTravelPlaces(): 여행지 검색 시 일정 데이터 존재하지 않아 예외 발생")
    @WithMockUser(username = "member1")
    void searchTravelPlaces_dataNotFoundException() throws Exception {
        // given
        // when, then
        mockMvc.perform(get("/api/schedules/{scheduleId}/travels", 0L)
                        .param("page", "1")
                        .param("keyword", "ㅁㄴㅇㄹ"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.SCHEDULE_NOT_FOUND.getMessage()));
    }

    @Test
    @DisplayName("getTravelRoutes(): 여행 루트 조회 성공")
    @WithMockUser(username = "member1")
    void getTravelRoutes() throws Exception {
        // given
        TravelRoute route1 = travelRouteRepository.save(createTravelRoute(schedule1, travelPlace1, 1));
        TravelRoute route2 = travelRouteRepository.save(createTravelRoute(schedule1, travelPlace1, 2));
        TravelRoute route3 = travelRouteRepository.save(createTravelRoute(schedule1, travelPlace2, 3));

        List<TravelRoute> travelRouteListAll = Arrays.asList(route1, route2, route3);
        List<TravelRoute> travelRouteList1 = Arrays.asList(route1, route2);
        List<TravelRoute> travelRouteList2 = List.of(route3);

        schedule1.setTravelRouteList(travelRouteListAll);
        travelPlace1.setTravelRouteList(travelRouteList1);
        travelPlace2.setTravelRouteList(travelRouteList2);

        // when, then
        mockMvc.perform(get("/api/schedules/{scheduleId}/routes", schedule1.getScheduleId())
                        .param("page", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(3))
                .andExpect(jsonPath("$.data.content[0].routeOrder").value(route1.getRouteOrder()))
                .andExpect(jsonPath("$.data.content[0].placeId").value(travelPlace1.getPlaceId()))
                .andExpect(jsonPath("$.data.content[1].routeOrder").value(route2.getRouteOrder()))
                .andExpect(jsonPath("$.data.content[1].placeId").value(travelPlace1.getPlaceId()))
                .andExpect(jsonPath("$.data.content[2].routeOrder").value(route3.getRouteOrder()))
                .andExpect(jsonPath("$.data.content[2].placeId").value(travelPlace2.getPlaceId()));
    }

    @Test
    @DisplayName("getTravelRoutes(): 여행 루트 조회 시 저장된 여행 루트 데이터 없는 경우")
    @WithMockUser(username = "member1")
    void getTravelRoutesWithoutData() throws Exception {
        // given
        // when, then
         mockMvc.perform(get("/api/schedules/{scheduleId}/routes", schedule1.getScheduleId())
                                .param("page", "1"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.data.totalElements").value(0))
                        .andExpect(jsonPath("$.data.content").isEmpty());
    }

    @Test
    @DisplayName("getTravelRoutes(): 여행 루트 조회 시 일정 데이터 존재하지 않아 예외 발생")
    @WithMockUser(username = "member1")
    void getTravelRoutes_dataNotFoundException() throws Exception {
        // given
        // when, then
        mockMvc.perform(get("/api/schedules/{scheduleId}/travels", 0L)
                        .param("page", "1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.SCHEDULE_NOT_FOUND.getMessage()));
    }



}
