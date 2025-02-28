package com.triptune.schedule.controller;

import com.triptune.common.entity.*;
import com.triptune.common.repository.*;
import com.triptune.member.entity.Member;
import com.triptune.profile.entity.ProfileImage;
import com.triptune.member.repository.MemberRepository;
import com.triptune.profile.repository.ProfileImageRepository;
import com.triptune.schedule.ScheduleTest;
import com.triptune.schedule.dto.request.ScheduleCreateRequest;
import com.triptune.schedule.dto.request.RouteRequest;
import com.triptune.schedule.dto.request.ScheduleUpdateRequest;
import com.triptune.schedule.entity.TravelAttendee;
import com.triptune.schedule.entity.TravelRoute;
import com.triptune.schedule.entity.TravelSchedule;
import com.triptune.schedule.enumclass.AttendeePermission;
import com.triptune.schedule.enumclass.AttendeeRole;
import com.triptune.schedule.enumclass.ScheduleSearchType;
import com.triptune.schedule.repository.ChatMessageRepository;
import com.triptune.schedule.repository.TravelAttendeeRepository;
import com.triptune.schedule.repository.TravelRouteRepository;
import com.triptune.schedule.repository.TravelScheduleRepository;
import com.triptune.travel.entity.TravelImage;
import com.triptune.travel.entity.TravelPlace;
import com.triptune.travel.repository.TravelImageRepository;
import com.triptune.travel.repository.TravelPlaceRepository;
import com.triptune.global.enumclass.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;

import java.time.LocalDate;
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
@ActiveProfiles("mongo")
public class ScheduleControllerTest extends ScheduleTest {
    @Autowired private WebApplicationContext wac;
    @Autowired private TravelScheduleRepository travelScheduleRepository;
    @Autowired private TravelAttendeeRepository travelAttendeeRepository;
    @Autowired private MemberRepository memberRepository;
    @Autowired private TravelPlaceRepository travelPlaceRepository;
    @Autowired private CountryRepository countryRepository;
    @Autowired private CityRepository cityRepository;
    @Autowired private DistrictRepository districtRepository;
    @Autowired private ApiCategoryRepository apiCategoryRepository;
    @Autowired private TravelImageRepository travelImageRepository;
    @Autowired private TravelRouteRepository travelRouteRepository;
    @Autowired private ProfileImageRepository profileImageRepository;
    @Autowired private ChatMessageRepository chatMessageRepository;

    private MockMvc mockMvc;

    private Member member1;
    private Member member2;
    private Member member3;

    private TravelSchedule schedule1;
    private TravelSchedule schedule2;
    private TravelSchedule schedule3;

    private TravelPlace travelPlace1;
    private TravelPlace travelPlace2;


    @BeforeEach
    void setUp(){
        mockMvc = MockMvcBuilders.webAppContextSetup(wac)
                .addFilter(new CharacterEncodingFilter("UTF-8", true))
                .apply(springSecurity())
                .alwaysDo(print())
                .build();

        chatMessageRepository.deleteAll();

        ProfileImage profileImage1 = profileImageRepository.save(createProfileImage(null, "member1Image"));
        ProfileImage profileImage2 = profileImageRepository.save(createProfileImage(null, "member2Image"));
        ProfileImage profileImage3 = profileImageRepository.save(createProfileImage(null, "member3Image"));

        member1 = memberRepository.save(createMember(null, "member1", profileImage1));
        member2 = memberRepository.save(createMember(null, "member2", profileImage2));
        member3 = memberRepository.save(createMember(null, "member3", profileImage3));

        Country country = countryRepository.save(createCountry());
        City city = cityRepository.save(createCity(country));
        District district1 = districtRepository.save(createDistrict(city, "강남구"));
        District district2 = districtRepository.save(createDistrict(city, "중구"));
        ApiCategory apiCategory = apiCategoryRepository.save(createApiCategory());

        schedule1 = travelScheduleRepository.save(createTravelSchedule(null,"테스트1"));
        schedule2 = travelScheduleRepository.save(createTravelSchedule(null,"테스트2"));
        schedule3 = travelScheduleRepository.save(createTravelSchedule(null,"테스트3"));

        travelPlace1 = travelPlaceRepository.save(createTravelPlace(null, country, city, district1, apiCategory));
        travelPlace2 = travelPlaceRepository.save(createTravelPlace(null, country, city, district2, apiCategory));

        TravelImage travelImage1 = travelImageRepository.save(createTravelImage(travelPlace1, "test1", true));
        TravelImage travelImage2 = travelImageRepository.save(createTravelImage(travelPlace1, "test2", false));
        TravelImage travelImage3 = travelImageRepository.save(createTravelImage(travelPlace2, "test1", true));
        TravelImage travelImage4 = travelImageRepository.save(createTravelImage(travelPlace2, "test2", false));

        travelPlace1 = travelPlaceRepository.save(createTravelPlace(travelPlace1.getPlaceId(), country, city, district1, apiCategory, List.of(travelImage1, travelImage2)));
        travelPlace2 = travelPlaceRepository.save(createTravelPlace(travelPlace2.getPlaceId(), country, city, district2, apiCategory, List.of(travelImage3, travelImage4)));

    }

    @Test
    @DisplayName("전체 일정 목록 조회")
    @WithMockUser(username = "member1")
    void getAllSchedulesByUserId() throws Exception {
        TravelAttendee attendee1 = travelAttendeeRepository.save(createTravelAttendee(0L, member1, schedule1, AttendeeRole.AUTHOR, AttendeePermission.ALL));
        TravelAttendee attendee2 = travelAttendeeRepository.save(createTravelAttendee(0L, member2, schedule1, AttendeeRole.GUEST, AttendeePermission.READ));
        TravelAttendee attendee3 = travelAttendeeRepository.save(createTravelAttendee(0L, member1, schedule2, AttendeeRole.AUTHOR, AttendeePermission.ALL));
        TravelAttendee attendee4 = travelAttendeeRepository.save(createTravelAttendee(0L, member3, schedule3, AttendeeRole.AUTHOR, AttendeePermission.ALL));

        schedule1.setTravelAttendeeList(new ArrayList<>(List.of(attendee1, attendee2)));
        schedule2.setTravelAttendeeList(new ArrayList<>(List.of(attendee3)));
        schedule3.setTravelAttendeeList(new ArrayList<>(List.of(attendee4)));

        TravelRoute route1 = travelRouteRepository.save(createTravelRoute(schedule1, travelPlace1, 1));
        TravelRoute route2 = travelRouteRepository.save(createTravelRoute(schedule1, travelPlace2, 2));
        TravelRoute route3 = travelRouteRepository.save(createTravelRoute(schedule2, travelPlace1, 1));
        TravelRoute route4 = travelRouteRepository.save(createTravelRoute(schedule2, travelPlace2, 2));
        schedule1.setTravelRouteList(new ArrayList<>(List.of(route1, route2)));
        schedule2.setTravelRouteList(new ArrayList<>(List.of(route3, route4)));


        mockMvc.perform(get("/api/schedules")
                        .param("page", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(2))
                .andExpect(jsonPath("$.data.totalSharedElements").value(1))
                .andExpect(jsonPath("$.data.content[0].sinceUpdate").exists())
                .andExpect(jsonPath("$.data.content[0].scheduleName").exists())
                .andExpect(jsonPath("$.data.content[0].thumbnailUrl").exists())
                .andExpect(jsonPath("$.data.content[0].role").exists());
    }

    @Test
    @DisplayName("전체 일정 목록 조회 시 여행 루트 데이터가 없는 경우")
    @WithMockUser(username = "member3")
    void getAllSchedulesByUserIdNoRouteData() throws Exception {
        TravelAttendee attendee1 = travelAttendeeRepository.save(createTravelAttendee(0L, member3, schedule3, AttendeeRole.AUTHOR, AttendeePermission.ALL));
        schedule3.setTravelAttendeeList(new ArrayList<>(List.of(attendee1)));

        mockMvc.perform(get("/api/schedules")
                        .param("page", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(1))
                .andExpect(jsonPath("$.data.content[0].sinceUpdate").exists())
                .andExpect(jsonPath("$.data.content[0].scheduleName").exists())
                .andExpect(jsonPath("$.data.content[0].thumbnailUrl").isEmpty());

    }

    @Test
    @DisplayName("전체 일정 목록 조회 시 데이터 없는 경우")
    @WithMockUser(username = "member1")
    void getAllSchedulesByUserIdWithoutData() throws Exception {
        mockMvc.perform(get("/api/schedules")
                        .param("page", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(0))
                .andExpect(jsonPath("$.data.content").isEmpty());
    }

    @Test
    @DisplayName("공유된 일정 목록 조회")
    @WithMockUser(username = "member1")
    void getSharedSchedulesByUserId() throws Exception {
        TravelAttendee attendee1 = travelAttendeeRepository.save(createTravelAttendee(0L, member1, schedule1, AttendeeRole.AUTHOR, AttendeePermission.ALL));
        TravelAttendee attendee2 = travelAttendeeRepository.save(createTravelAttendee(0L, member2, schedule1, AttendeeRole.GUEST, AttendeePermission.READ));
        TravelAttendee attendee3 = travelAttendeeRepository.save(createTravelAttendee(0L, member1, schedule2, AttendeeRole.AUTHOR, AttendeePermission.ALL));
        TravelAttendee attendee4 = travelAttendeeRepository.save(createTravelAttendee(0L, member1, schedule3, AttendeeRole.AUTHOR, AttendeePermission.ALL));

        schedule1.setTravelAttendeeList(new ArrayList<>(List.of(attendee1, attendee2)));
        schedule2.setTravelAttendeeList(new ArrayList<>(List.of(attendee3)));
        schedule3.setTravelAttendeeList(new ArrayList<>(List.of(attendee4)));

        TravelRoute route1 = travelRouteRepository.save(createTravelRoute(schedule1, travelPlace1, 1));
        TravelRoute route2 = travelRouteRepository.save(createTravelRoute(schedule1, travelPlace2, 2));
        TravelRoute route3 = travelRouteRepository.save(createTravelRoute(schedule2, travelPlace1, 1));
        TravelRoute route4 = travelRouteRepository.save(createTravelRoute(schedule2, travelPlace2, 2));
        schedule1.setTravelRouteList(Arrays.asList(route1, route2));
        schedule2.setTravelRouteList(Arrays.asList(route3, route4));


        mockMvc.perform(get("/api/schedules/shared")
                        .param("page", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(3))
                .andExpect(jsonPath("$.data.totalSharedElements").value(1))
                .andExpect(jsonPath("$.data.content[0].sinceUpdate").exists())
                .andExpect(jsonPath("$.data.content[0].scheduleName").exists())
                .andExpect(jsonPath("$.data.content[0].thumbnailUrl").exists())
                .andExpect(jsonPath("$.data.content[0].role").exists());
    }

    @Test
    @DisplayName("공유된 일정 목록 조회 시 여행 루트 데이터가 없는 경우")
    @WithMockUser(username = "member1")
    void getSharedSchedulesByUserIdNoRouteData() throws Exception {
        TravelAttendee attendee1 = travelAttendeeRepository.save(createTravelAttendee(0L, member1, schedule1, AttendeeRole.AUTHOR, AttendeePermission.ALL));
        TravelAttendee attendee2 = travelAttendeeRepository.save(createTravelAttendee(0L, member2, schedule1, AttendeeRole.GUEST, AttendeePermission.READ));
        TravelAttendee attendee3 = travelAttendeeRepository.save(createTravelAttendee(0L, member1, schedule2, AttendeeRole.AUTHOR, AttendeePermission.ALL));
        TravelAttendee attendee4 = travelAttendeeRepository.save(createTravelAttendee(0L, member1, schedule3, AttendeeRole.AUTHOR, AttendeePermission.ALL));

        schedule1.setTravelAttendeeList(new ArrayList<>(List.of(attendee1, attendee2)));
        schedule2.setTravelAttendeeList(new ArrayList<>(List.of(attendee3)));
        schedule3.setTravelAttendeeList(new ArrayList<>(List.of(attendee4)));

        mockMvc.perform(get("/api/schedules/shared")
                        .param("page", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(3))
                .andExpect(jsonPath("$.data.totalSharedElements").value(1))
                .andExpect(jsonPath("$.data.content[0].sinceUpdate").exists())
                .andExpect(jsonPath("$.data.content[0].scheduleName").exists())
                .andExpect(jsonPath("$.data.content[0].thumbnailUrl").isEmpty());

    }

    @Test
    @DisplayName("공유된 일정 목록 조회 시 공유된 일정 없는 경우")
    @WithMockUser(username = "member1")
    void getSharedSchedulesWithoutSharedDataByUserId() throws Exception {
        TravelAttendee attendee1 = travelAttendeeRepository.save(createTravelAttendee(0L, member1, schedule1, AttendeeRole.AUTHOR, AttendeePermission.ALL));
        TravelAttendee attendee3 = travelAttendeeRepository.save(createTravelAttendee(0L, member1, schedule2, AttendeeRole.AUTHOR, AttendeePermission.ALL));
        TravelAttendee attendee4 = travelAttendeeRepository.save(createTravelAttendee(0L, member1, schedule3, AttendeeRole.AUTHOR, AttendeePermission.ALL));

        schedule1.setTravelAttendeeList(new ArrayList<>(List.of(attendee1)));
        schedule2.setTravelAttendeeList(new ArrayList<>(List.of(attendee3)));
        schedule3.setTravelAttendeeList(new ArrayList<>(List.of(attendee4)));

        mockMvc.perform(get("/api/schedules/shared")
                        .param("page", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(3))
                .andExpect(jsonPath("$.data.totalSharedElements").value(0))
                .andExpect(jsonPath("$.data.content").isEmpty());
    }

    @Test
    @DisplayName("공유된 일정 목록 조회 시 데이터 없는 경우")
    @WithMockUser(username = "member1")
    void getSharedSchedulesByUserIdWithoutData() throws Exception {
        mockMvc.perform(get("/api/schedules/shared")
                        .param("page", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(0))
                .andExpect(jsonPath("$.data.totalSharedElements").value(0))
                .andExpect(jsonPath("$.data.content").isEmpty());
    }


    @Test
    @DisplayName("수정 권한 있는 내 일정 조회")
    @WithMockUser(username = "member1")
    void getEnableEditScheduleByUserId() throws Exception {
        TravelAttendee attendee1 = travelAttendeeRepository.save(createTravelAttendee(null, member1, schedule1, AttendeeRole.AUTHOR, AttendeePermission.ALL));
        TravelAttendee attendee2 = travelAttendeeRepository.save(createTravelAttendee(null, member2, schedule1, AttendeeRole.GUEST, AttendeePermission.READ));
        TravelAttendee attendee3 = travelAttendeeRepository.save(createTravelAttendee(null, member3, schedule2, AttendeeRole.AUTHOR, AttendeePermission.ALL));
        TravelAttendee attendee4 = travelAttendeeRepository.save(createTravelAttendee(null, member1, schedule2, AttendeeRole.GUEST, AttendeePermission.CHAT));

        schedule1.setTravelAttendeeList(new ArrayList<>(List.of(attendee1, attendee2)));
        schedule2.setTravelAttendeeList(new ArrayList<>(List.of(attendee3, attendee4)));

        mockMvc.perform(get("/api/schedules/edit")
                        .param("page", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(1))
                .andExpect(jsonPath("$.data.content[0].scheduleName").value(schedule1.getScheduleName()))
                .andExpect(jsonPath("$.data.content[0].author").value(member1.getNickname()));
    }

    @Test
    @DisplayName("수정 가능한 일정 조회 시 일정 데이터가 없는 경우")
    @WithMockUser(username = "member1")
    void getEnableEditScheduleByUserId_emptySchedules() throws Exception {
        mockMvc.perform(get("/api/schedules/edit")
                        .param("page", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(0))
                .andExpect(jsonPath("$.data.content").isEmpty());
    }


    @Test
    @DisplayName("전체 일정 검색")
    @WithMockUser(username = "member1")
    void searchSchedules() throws Exception {
        TravelAttendee attendee1 = travelAttendeeRepository.save(createTravelAttendee(null, member1, schedule1, AttendeeRole.AUTHOR, AttendeePermission.ALL));
        TravelAttendee attendee2 = travelAttendeeRepository.save(createTravelAttendee(null, member2, schedule1, AttendeeRole.GUEST, AttendeePermission.READ));
        TravelAttendee attendee3 = travelAttendeeRepository.save(createTravelAttendee(null, member1, schedule2, AttendeeRole.AUTHOR, AttendeePermission.ALL));
        TravelAttendee attendee4 = travelAttendeeRepository.save(createTravelAttendee(null, member1, schedule3, AttendeeRole.AUTHOR, AttendeePermission.ALL));

        schedule1.setTravelAttendeeList(new ArrayList<>(List.of(attendee1, attendee2)));
        schedule2.setTravelAttendeeList(new ArrayList<>(List.of(attendee3)));
        schedule3.setTravelAttendeeList(new ArrayList<>(List.of(attendee4)));

        TravelRoute route1 = travelRouteRepository.save(createTravelRoute(schedule1, travelPlace1, 1));
        TravelRoute route2 = travelRouteRepository.save(createTravelRoute(schedule1, travelPlace2, 2));
        TravelRoute route3 = travelRouteRepository.save(createTravelRoute(schedule2, travelPlace1, 1));
        TravelRoute route4 = travelRouteRepository.save(createTravelRoute(schedule2, travelPlace2, 2));
        schedule1.setTravelRouteList(Arrays.asList(route1, route2));
        schedule2.setTravelRouteList(Arrays.asList(route3, route4));

        mockMvc.perform(get("/api/schedules/search")
                        .param("page", "1")
                        .param("type", ScheduleSearchType.ALL.getValue())
                        .param("keyword", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(1))
                .andExpect(jsonPath("$.data.totalSharedElements").value(1))
                .andExpect(jsonPath("$.data.content[0].sinceUpdate").exists())
                .andExpect(jsonPath("$.data.content[0].scheduleName").value(schedule1.getScheduleName()))
                .andExpect(jsonPath("$.data.content[0].thumbnailUrl").exists())
                .andExpect(jsonPath("$.data.content[0].role").exists());
    }

    @Test
    @DisplayName("전체 일정 검색 시 여행 루트 데이터가 없는 경우")
    @WithMockUser(username = "member1")
    void searchSchedulesNoRouteData() throws Exception {
        TravelAttendee attendee1 = travelAttendeeRepository.save(createTravelAttendee(null, member1, schedule1, AttendeeRole.AUTHOR, AttendeePermission.ALL));
        TravelAttendee attendee2 = travelAttendeeRepository.save(createTravelAttendee(null, member2, schedule1, AttendeeRole.GUEST, AttendeePermission.READ));
        TravelAttendee attendee3 = travelAttendeeRepository.save(createTravelAttendee(null, member1, schedule2, AttendeeRole.AUTHOR, AttendeePermission.ALL));
        TravelAttendee attendee4 = travelAttendeeRepository.save(createTravelAttendee(null, member1, schedule3, AttendeeRole.AUTHOR, AttendeePermission.ALL));

        schedule1.setTravelAttendeeList(new ArrayList<>(List.of(attendee1, attendee2)));
        schedule2.setTravelAttendeeList(new ArrayList<>(List.of(attendee3)));
        schedule3.setTravelAttendeeList(new ArrayList<>(List.of(attendee4)));

        mockMvc.perform(get("/api/schedules/search")
                        .param("page", "1")
                        .param("type", ScheduleSearchType.ALL.getValue())
                        .param("keyword", "테스트"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(3))
                .andExpect(jsonPath("$.data.totalSharedElements").value(1))
                .andExpect(jsonPath("$.data.content[0].sinceUpdate").exists())
                .andExpect(jsonPath("$.data.content[0].scheduleName").exists())
                .andExpect(jsonPath("$.data.content[0].thumbnailUrl").isEmpty());

    }

    @Test
    @DisplayName("전체 일정 검색 시 공유된 일정 없는 경우")
    @WithMockUser(username = "member1")
    void searchSchedulesWithoutSharedData() throws Exception {
        TravelAttendee attendee1 = travelAttendeeRepository.save(createTravelAttendee(null, member1, schedule1, AttendeeRole.AUTHOR, AttendeePermission.ALL));
        TravelAttendee attendee3 = travelAttendeeRepository.save(createTravelAttendee(null, member1, schedule2, AttendeeRole.AUTHOR, AttendeePermission.ALL));
        TravelAttendee attendee4 = travelAttendeeRepository.save(createTravelAttendee(null, member1, schedule3, AttendeeRole.AUTHOR, AttendeePermission.ALL));

        schedule1.setTravelAttendeeList(new ArrayList<>(List.of(attendee1)));
        schedule2.setTravelAttendeeList(new ArrayList<>(List.of(attendee3)));
        schedule3.setTravelAttendeeList(new ArrayList<>(List.of(attendee4)));

        mockMvc.perform(get("/api/schedules/search")
                        .param("page", "1")
                        .param("type", ScheduleSearchType.ALL.getValue())
                        .param("keyword", "테스트"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(3))
                .andExpect(jsonPath("$.data.totalSharedElements").value(0))
                .andExpect(jsonPath("$.data.content").exists());
    }

    @Test
    @DisplayName("전체 일정 검색 시 검색 결과 없는 경우")
    @WithMockUser(username = "member1")
    void searchSchedulesWithoutData() throws Exception {
        mockMvc.perform(get("/api/schedules/search")
                        .param("page", "1")
                        .param("type", ScheduleSearchType.ALL.getValue())
                        .param("keyword", "ㅁㄴㅇㄹ"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(0))
                .andExpect(jsonPath("$.data.totalSharedElements").value(0))
                .andExpect(jsonPath("$.data.content").isEmpty());
    }

    @Test
    @DisplayName("공유된 일정 검색")
    @WithMockUser(username = "member1")
    void searchSharedSchedules() throws Exception {
        TravelAttendee attendee1 = travelAttendeeRepository.save(createTravelAttendee(null, member1, schedule1, AttendeeRole.AUTHOR, AttendeePermission.ALL));
        TravelAttendee attendee2 = travelAttendeeRepository.save(createTravelAttendee(null, member2, schedule1, AttendeeRole.GUEST, AttendeePermission.READ));
        TravelAttendee attendee3 = travelAttendeeRepository.save(createTravelAttendee(null, member1, schedule2, AttendeeRole.AUTHOR, AttendeePermission.ALL));
        TravelAttendee attendee4 = travelAttendeeRepository.save(createTravelAttendee(null, member1, schedule3, AttendeeRole.AUTHOR, AttendeePermission.ALL));

        schedule1.setTravelAttendeeList(new ArrayList<>(List.of(attendee1, attendee2)));
        schedule2.setTravelAttendeeList(new ArrayList<>(List.of(attendee3)));
        schedule3.setTravelAttendeeList(new ArrayList<>(List.of(attendee4)));

        TravelRoute route1 = travelRouteRepository.save(createTravelRoute(schedule1, travelPlace1, 1));
        TravelRoute route2 = travelRouteRepository.save(createTravelRoute(schedule1, travelPlace2, 2));
        TravelRoute route3 = travelRouteRepository.save(createTravelRoute(schedule2, travelPlace1, 1));
        TravelRoute route4 = travelRouteRepository.save(createTravelRoute(schedule2, travelPlace2, 2));
        schedule1.setTravelRouteList(Arrays.asList(route1, route2));
        schedule2.setTravelRouteList(Arrays.asList(route3, route4));

        mockMvc.perform(get("/api/schedules/search")
                        .param("page", "1")
                        .param("type", ScheduleSearchType.SHARE.getValue())
                        .param("keyword", "테스트"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(3))
                .andExpect(jsonPath("$.data.totalSharedElements").value(1))
                .andExpect(jsonPath("$.data.content[0].sinceUpdate").exists())
                .andExpect(jsonPath("$.data.content[0].scheduleName").value(schedule1.getScheduleName()))
                .andExpect(jsonPath("$.data.content[0].thumbnailUrl").exists())
                .andExpect(jsonPath("$.data.content[0].role").exists());
    }

    @Test
    @DisplayName("공유된 일정 검색 시 여행 루트 데이터가 없는 경우")
    @WithMockUser(username = "member1")
    void searchSharedSchedulesNoRouteData() throws Exception {
        TravelAttendee attendee1 = travelAttendeeRepository.save(createTravelAttendee(null, member1, schedule1, AttendeeRole.AUTHOR, AttendeePermission.ALL));
        TravelAttendee attendee2 = travelAttendeeRepository.save(createTravelAttendee(null, member2, schedule1, AttendeeRole.GUEST, AttendeePermission.READ));
        TravelAttendee attendee3 = travelAttendeeRepository.save(createTravelAttendee(null, member1, schedule2, AttendeeRole.AUTHOR, AttendeePermission.ALL));
        TravelAttendee attendee4 = travelAttendeeRepository.save(createTravelAttendee(null, member1, schedule3, AttendeeRole.AUTHOR, AttendeePermission.ALL));

        schedule1.setTravelAttendeeList(new ArrayList<>(List.of(attendee1, attendee2)));
        schedule2.setTravelAttendeeList(new ArrayList<>(List.of(attendee3)));
        schedule3.setTravelAttendeeList(new ArrayList<>(List.of(attendee4)));

        mockMvc.perform(get("/api/schedules/search")
                        .param("page", "1")
                        .param("type", ScheduleSearchType.SHARE.getValue())
                        .param("keyword", "테스트"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(3))
                .andExpect(jsonPath("$.data.totalSharedElements").value(1))
                .andExpect(jsonPath("$.data.content[0].sinceUpdate").exists())
                .andExpect(jsonPath("$.data.content[0].scheduleName").exists())
                .andExpect(jsonPath("$.data.content[0].thumbnailUrl").isEmpty());

    }

    @Test
    @DisplayName("공유된 일정 검색 시 검색 결과 없는 경우")
    @WithMockUser(username = "member1")
    void searchSharedSchedulesWithoutData() throws Exception {
        mockMvc.perform(get("/api/schedules/search")
                        .param("page", "1")
                        .param("type", ScheduleSearchType.SHARE.getValue())
                        .param("keyword", "ㅁㄴㅇㄹ"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(0))
                .andExpect(jsonPath("$.data.totalSharedElements").value(0))
                .andExpect(jsonPath("$.data.content").isEmpty());
    }

    @Test
    @WithMockUser(username = "member1")
    @DisplayName("전체, 공유된 일정 검색 시 검색 타입이 없어 예외 발생")
    void searchSchedules_illegalArgumentException() throws Exception {
        mockMvc.perform(get("/api/schedules/search")
                        .param("page", "1")
                        .param("type", "not")
                        .param("keyword", "ㅁㄴㅇㄹ"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.ILLEGAL_SCHEDULE_SEARCH_TYPE.getMessage()));
    }

    @Test
    @DisplayName("일정 생성")
    @WithMockUser(username = "member1")
    void createSchedule() throws Exception{
        // given
        ScheduleCreateRequest request = createScheduleRequest();

        // when, then
        mockMvc.perform(post("/api/schedules")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }


    @Test
    @DisplayName("일정 생성 시 필요 입력값이 다 안들어와 예외 발생")
    @WithMockUser(username = "test")
    void createSchedule_methodArgumentNotValidException() throws Exception{
        // given
        ScheduleCreateRequest request = createScheduleRequest();
        request.setStartDate(null);

        // when, then
        mockMvc.perform(post("/api/schedules")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("일정 생성 시 오늘 이전 날짜 입력으로 예외 발생")
    @WithMockUser(username = "test")
    void createSchedulePastDate_methodArgumentNotValidException() throws Exception{
        // given
        ScheduleCreateRequest request = createScheduleRequest();
        request.setStartDate(LocalDate.now().minusDays(3));

        // when, then
        mockMvc.perform(post("/api/schedules")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("일정 상세 조회")
    @WithMockUser(username = "member1")
    void getScheduleDetail() throws Exception {
        // given
        TravelAttendee attendee1 = travelAttendeeRepository.save(createTravelAttendee(null, member1, schedule1, AttendeeRole.AUTHOR, AttendeePermission.ALL));
        TravelAttendee attendee2 = travelAttendeeRepository.save(createTravelAttendee(null, member2, schedule1, AttendeeRole.GUEST, AttendeePermission.READ));



        schedule1.setTravelAttendeeList(new ArrayList<>(List.of(attendee1, attendee2)));

        // when, then
        mockMvc.perform(get("/api/schedules/{scheduleId}", schedule1.getScheduleId())
                        .param("page", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.scheduleName").value(schedule1.getScheduleName()))
                .andExpect(jsonPath("$.data.placeList.totalElements").value(1))
                .andExpect(jsonPath("$.data.placeList.content[0].district").value(travelPlace2.getDistrict().getDistrictName()))
                .andExpect(jsonPath("$.data.placeList.content[0].placeName").value(travelPlace2.getPlaceName()))
                .andExpect(jsonPath("$.data.placeList.content[0].thumbnailUrl").exists());
    }

    @Test
    @DisplayName("일정 상세 조회 시 여행지 데이터 존재하지 않는 경우")
    @WithMockUser(username = "member1")
    void getScheduleDetailWithoutData() throws Exception {
        // given
        travelPlace2.getDistrict().updateDistrictName("성북구");

        TravelAttendee attendee1 = travelAttendeeRepository.save(createTravelAttendee(null, member1, schedule1, AttendeeRole.AUTHOR, AttendeePermission.ALL));
        TravelAttendee attendee2 = travelAttendeeRepository.save(createTravelAttendee(null, member2, schedule1, AttendeeRole.GUEST, AttendeePermission.READ));

        schedule1.setTravelAttendeeList(new ArrayList<>(List.of(attendee1, attendee2)));


        // when, then
        mockMvc.perform(get("/api/schedules/{scheduleId}", schedule1.getScheduleId())
                        .param("page", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.scheduleName").value(schedule1.getScheduleName()))
                .andExpect(jsonPath("$.data.placeList.totalElements").value(0))
                .andExpect(jsonPath("$.data.placeList.content").isEmpty());
    }

    @Test
    @DisplayName("일정 상세 조회 시 일정 데이터 존재하지 않아 예외 발생")
    @WithMockUser(username = "member1")
    void getScheduleDetail_dataNotFoundException() throws Exception {
        mockMvc.perform(get("/api/schedules/{scheduleId}", 0L)
                        .param("page", "1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.SCHEDULE_NOT_FOUND.getMessage()));
    }


    @Test
    @DisplayName("일정 상세 조회 시 일정에 접근 권한이 없어 예외 발생")
    @WithMockUser(username = "member1")
    void getScheduleDetailNotAttendee_forbiddenScheduleException() throws Exception {
        mockMvc.perform(delete("/api/schedules/{scheduleId}", schedule2.getScheduleId()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.FORBIDDEN_ACCESS_SCHEDULE.getMessage()));
    }

    @Test
    @DisplayName("일정 수정")
    @WithMockUser(username = "member1")
    void updateSchedule() throws Exception {
        // given
        TravelAttendee attendee1 = travelAttendeeRepository.save(createTravelAttendee(null, member1, schedule1, AttendeeRole.AUTHOR, AttendeePermission.ALL));
        TravelAttendee attendee3 = travelAttendeeRepository.save(createTravelAttendee(null, member2, schedule1, AttendeeRole.GUEST, AttendeePermission.READ));

        TravelRoute route1 = travelRouteRepository.save(createTravelRoute(schedule1, travelPlace1, 1));
        TravelRoute route2 = travelRouteRepository.save(createTravelRoute(schedule1, travelPlace2, 2));

        schedule1.setTravelRouteList(new ArrayList<>(List.of(route1, route2)));
        schedule1.setTravelAttendeeList(new ArrayList<>(List.of(attendee1, attendee3)));

        RouteRequest routeRequest1 = createRouteRequest(1, travelPlace1.getPlaceId());
        RouteRequest routeRequest2 = createRouteRequest(2, travelPlace2.getPlaceId());

        ScheduleUpdateRequest request = createUpdateScheduleRequest(new ArrayList<>(List.of(routeRequest1, routeRequest2)));

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
    @DisplayName("일정 수정 시 필요 데이터 입력 안해 예외 발생")
    @WithMockUser(username = "member1")
    void updateSchedule_methodArgumentNotValidException() throws Exception {
        // given
        TravelAttendee attendee1 = travelAttendeeRepository.save(createTravelAttendee(null, member1, schedule1, AttendeeRole.AUTHOR, AttendeePermission.ALL));
        TravelAttendee attendee3 = travelAttendeeRepository.save(createTravelAttendee(null, member2, schedule1, AttendeeRole.GUEST, AttendeePermission.READ));

        schedule1.setTravelAttendeeList(new ArrayList<>(List.of(attendee1, attendee3)));

        RouteRequest routeRequest1 = createRouteRequest(1, travelPlace1.getPlaceId());
        RouteRequest routeRequest2 = createRouteRequest(2, travelPlace2.getPlaceId());

        ScheduleUpdateRequest request = createUpdateScheduleRequest(null, new ArrayList<>(List.of(routeRequest1, routeRequest2)));

        // when
        mockMvc.perform(patch("/api/schedules/{scheduleId}", schedule1.getScheduleId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));

    }

    @Test
    @DisplayName("일정 수정 시 저장된 여행 루트 없는 경우")
    @WithMockUser(username = "member1")
    void updateScheduleNoSavedTravelRoute() throws Exception {
        // given
        TravelAttendee attendee1 = travelAttendeeRepository.save(createTravelAttendee(null, member1, schedule1, AttendeeRole.AUTHOR, AttendeePermission.ALL));
        TravelAttendee attendee3 = travelAttendeeRepository.save(createTravelAttendee(null, member2, schedule1, AttendeeRole.GUEST, AttendeePermission.READ));

        schedule1.setTravelAttendeeList(new ArrayList<>(List.of(attendee1, attendee3)));

        RouteRequest routeRequest1 = createRouteRequest(1, travelPlace1.getPlaceId());
        ScheduleUpdateRequest request = createUpdateScheduleRequest(new ArrayList<>(List.of(routeRequest1)));

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
    @DisplayName("일정 수정 시 접근 권한이 없어 예외 발생")
    @WithMockUser(username = "member3")
    void updateScheduleForbiddenAccess_forbiddenScheduleException() throws Exception {
        // given
        TravelAttendee attendee1 = travelAttendeeRepository.save(createTravelAttendee(null, member1, schedule1, AttendeeRole.AUTHOR, AttendeePermission.ALL));
        TravelAttendee attendee3 = travelAttendeeRepository.save(createTravelAttendee(null, member2, schedule1, AttendeeRole.GUEST, AttendeePermission.READ));

        schedule1.setTravelAttendeeList(new ArrayList<>(List.of(attendee1, attendee3)));

        RouteRequest routeRequest1 = createRouteRequest(1, travelPlace1.getPlaceId());
        ScheduleUpdateRequest request = createUpdateScheduleRequest(new ArrayList<>(List.of(routeRequest1)));

        // when, then
        mockMvc.perform(patch("/api/schedules/{scheduleId}", schedule1.getScheduleId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value(ErrorCode.FORBIDDEN_ACCESS_SCHEDULE.getMessage()));

    }


    @Test
    @DisplayName("일정 수정 시 수정 권한이 없어 예외 발생")
    @WithMockUser(username = "member2")
    void updateScheduleForbiddenEdit_forbiddenScheduleException() throws Exception {
        // given
        TravelAttendee attendee1 = travelAttendeeRepository.save(createTravelAttendee(null, member1, schedule1, AttendeeRole.AUTHOR, AttendeePermission.ALL));
        TravelAttendee attendee3 = travelAttendeeRepository.save(createTravelAttendee(null, member2, schedule1, AttendeeRole.GUEST, AttendeePermission.READ));

        schedule1.setTravelAttendeeList(new ArrayList<>(List.of(attendee1, attendee3)));

        RouteRequest routeRequest1 = createRouteRequest(1, travelPlace1.getPlaceId());
        ScheduleUpdateRequest request = createUpdateScheduleRequest(new ArrayList<>(List.of(routeRequest1)));

        // when, then
        mockMvc.perform(patch("/api/schedules/{scheduleId}", schedule1.getScheduleId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value(ErrorCode.FORBIDDEN_EDIT_SCHEDULE.getMessage()));
    }


    @Test
    @DisplayName("일정 삭제")
    @WithMockUser(username = "member1")
    void deleteSchedule() throws Exception {
        // given
        TravelAttendee attendee1 = travelAttendeeRepository.save(createTravelAttendee(null, member1, schedule1, AttendeeRole.AUTHOR, AttendeePermission.ALL));
        TravelAttendee attendee2 = travelAttendeeRepository.save(createTravelAttendee(null, member2, schedule1, AttendeeRole.GUEST, AttendeePermission.READ));

        schedule1.setTravelAttendeeList(new ArrayList<>(List.of(attendee1, attendee2)));

        chatMessageRepository.save(createChatMessage("chat1", schedule1.getScheduleId(), member1, "hello1"));
        chatMessageRepository.save(createChatMessage("chat2", schedule1.getScheduleId(), member1, "hello2"));
        chatMessageRepository.save(createChatMessage("chat3", schedule1.getScheduleId(), member2, "hello3"));


        // when, then
        mockMvc.perform(delete("/api/schedules/{scheduleId}", schedule1.getScheduleId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

    }

    @Test
    @DisplayName("일정 삭제 시 채팅 메시지 데이터 없는 경우")
    @WithMockUser(username = "member1")
    void deleteScheduleNoDataChatMessage() throws Exception {
        // given
        TravelAttendee attendee1 = travelAttendeeRepository.save(createTravelAttendee(null, member1, schedule1, AttendeeRole.AUTHOR, AttendeePermission.ALL));
        TravelAttendee attendee2 = travelAttendeeRepository.save(createTravelAttendee(null, member2, schedule1, AttendeeRole.GUEST, AttendeePermission.READ));

        schedule1.setTravelAttendeeList(new ArrayList<>(List.of(attendee1, attendee2)));

        // when, then
        mockMvc.perform(delete("/api/schedules/{scheduleId}", schedule1.getScheduleId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

    }

    @Test
    @DisplayName("일정 삭제 시 삭제 권한이 없는 사용자 요청으로 예외 발생")
    @WithMockUser(username = "member2")
    void deleteSchedule_forbiddenScheduleException() throws Exception {
        // given
        TravelAttendee attendee1 = travelAttendeeRepository.save(createTravelAttendee(null, member1, schedule1, AttendeeRole.AUTHOR, AttendeePermission.ALL));
        TravelAttendee attendee2 = travelAttendeeRepository.save(createTravelAttendee(null, member2, schedule1, AttendeeRole.GUEST, AttendeePermission.READ));

        schedule1.setTravelAttendeeList(new ArrayList<>(List.of(attendee1, attendee2)));

        // when, then
        mockMvc.perform(delete("/api/schedules/{scheduleId}", schedule1.getScheduleId()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.FORBIDDEN_DELETE_SCHEDULE.getMessage()));

    }


}
