package com.triptune.schedule.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.triptune.common.entity.*;
import com.triptune.common.repository.*;
import com.triptune.global.response.enums.SuccessCode;
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
import com.triptune.schedule.enums.AttendeePermission;
import com.triptune.schedule.enums.AttendeeRole;
import com.triptune.schedule.enums.ScheduleSearchType;
import com.triptune.schedule.repository.ChatMessageRepository;
import com.triptune.schedule.repository.TravelAttendeeRepository;
import com.triptune.schedule.repository.TravelRouteRepository;
import com.triptune.schedule.repository.TravelScheduleRepository;
import com.triptune.travel.entity.TravelImage;
import com.triptune.travel.entity.TravelPlace;
import com.triptune.travel.repository.TravelImageRepository;
import com.triptune.travel.repository.TravelPlaceRepository;
import com.triptune.global.response.enums.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional
@AutoConfigureMockMvc
@ActiveProfiles("mongo")
public class TravelScheduleControllerTest extends ScheduleTest {
    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
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

    private Member member1;
    private Member member2;
    private Member member3;

    private TravelSchedule schedule1;
    private TravelSchedule schedule2;
    private TravelSchedule schedule3;

    private TravelPlace travelPlace1;
    private TravelPlace travelPlace2;

    private TravelImage travelImage1;

    @BeforeEach
    void setUp(){
        chatMessageRepository.deleteAll();

        ProfileImage profileImage1 = profileImageRepository.save(createProfileImage(null, "member1Image"));
        ProfileImage profileImage2 = profileImageRepository.save(createProfileImage(null, "member2Image"));
        ProfileImage profileImage3 = profileImageRepository.save(createProfileImage(null, "member3Image"));

        member1 = memberRepository.save(createMember(null, "member1@email.com", profileImage1));
        member2 = memberRepository.save(createMember(null, "member2@email.com", profileImage2));
        member3 = memberRepository.save(createMember(null, "member3@email.com", profileImage3));

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

        travelImage1 = travelImageRepository.save(createTravelImage(travelPlace1, "test1", true));
        TravelImage travelImage2 = travelImageRepository.save(createTravelImage(travelPlace1, "test2", false));
        TravelImage travelImage3 = travelImageRepository.save(createTravelImage(travelPlace2, "test1", true));
        TravelImage travelImage4 = travelImageRepository.save(createTravelImage(travelPlace2, "test2", false));

        travelPlace1 = travelPlaceRepository.save(createTravelPlace(travelPlace1.getPlaceId(), country, city, district1, apiCategory, List.of(travelImage1, travelImage2)));
        travelPlace2 = travelPlaceRepository.save(createTravelPlace(travelPlace2.getPlaceId(), country, city, district2, apiCategory, List.of(travelImage3, travelImage4)));

    }

    @Test
    @DisplayName("전체 일정 목록 조회")
    void getAllSchedules() throws Exception {
        // given
        TravelAttendee attendee1 = travelAttendeeRepository.save(createTravelAttendee(null, member1, schedule1, AttendeeRole.AUTHOR, AttendeePermission.ALL));
        TravelAttendee attendee2 = travelAttendeeRepository.save(createTravelAttendee(null, member2, schedule1, AttendeeRole.GUEST, AttendeePermission.READ));
        TravelAttendee attendee3 = travelAttendeeRepository.save(createTravelAttendee(null, member1, schedule2, AttendeeRole.AUTHOR, AttendeePermission.ALL));
        TravelAttendee attendee4 = travelAttendeeRepository.save(createTravelAttendee(null, member3, schedule3, AttendeeRole.AUTHOR, AttendeePermission.ALL));
        schedule1.addTravelAttendee(attendee1);
        schedule1.addTravelAttendee(attendee2);
        schedule2.addTravelAttendee(attendee3);
        schedule3.addTravelAttendee(attendee4);

        TravelRoute route1 = travelRouteRepository.save(createTravelRoute(schedule1, travelPlace1, 1));
        TravelRoute route2 = travelRouteRepository.save(createTravelRoute(schedule1, travelPlace2, 2));
        TravelRoute route3 = travelRouteRepository.save(createTravelRoute(schedule2, travelPlace1, 1));
        TravelRoute route4 = travelRouteRepository.save(createTravelRoute(schedule2, travelPlace2, 2));
        schedule1.addTravelRoutes(route1);
        schedule1.addTravelRoutes(route2);
        schedule2.addTravelRoutes(route3);
        schedule2.addTravelRoutes(route4);

        mockAuthentication(member1);

        // when, then
        mockMvc.perform(get("/api/schedules")
                        .param("page", "1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalElements").value(2))
                .andExpect(jsonPath("$.data.totalSharedElements").value(1))
                .andExpect(jsonPath("$.data.content[0].sinceUpdate").exists())
                .andExpect(jsonPath("$.data.content[0].scheduleName").exists())
                .andExpect(jsonPath("$.data.content[0].thumbnailUrl").exists())
                .andExpect(jsonPath("$.data.content[0].role").exists());
    }

    @Test
    @DisplayName("전체 일정 목록 조회 시 여행 루트 데이터가 없는 경우")
    void getAllSchedulesNoRouteData() throws Exception {
        // given
        TravelAttendee attendee1 = travelAttendeeRepository.save(createTravelAttendee(null, member3, schedule3, AttendeeRole.AUTHOR, AttendeePermission.ALL));
        schedule3.addTravelAttendee(attendee1);

        mockAuthentication(member3);

        // when, then
        mockMvc.perform(get("/api/schedules")
                        .param("page", "1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalElements").value(1))
                .andExpect(jsonPath("$.data.content[0].sinceUpdate").exists())
                .andExpect(jsonPath("$.data.content[0].scheduleName").exists())
                .andExpect(jsonPath("$.data.content[0].thumbnailUrl").isEmpty());

    }

    @Test
    @DisplayName("전체 일정 목록 조회 시 데이터 없는 경우")
    void getAllSchedulesWithoutData() throws Exception {
        // given
        mockAuthentication(member1);

        // when, then
        mockMvc.perform(get("/api/schedules")
                        .param("page", "1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalElements").value(0))
                .andExpect(jsonPath("$.data.content").isEmpty());
    }

    @Test
    @DisplayName("공유된 일정 목록 조회")
    void getSharedSchedules() throws Exception {
        // given
        TravelAttendee attendee1 = travelAttendeeRepository.save(createTravelAttendee(null, member1, schedule1, AttendeeRole.AUTHOR, AttendeePermission.ALL));
        TravelAttendee attendee2 = travelAttendeeRepository.save(createTravelAttendee(null, member2, schedule1, AttendeeRole.GUEST, AttendeePermission.READ));
        TravelAttendee attendee3 = travelAttendeeRepository.save(createTravelAttendee(null, member1, schedule2, AttendeeRole.AUTHOR, AttendeePermission.ALL));
        TravelAttendee attendee4 = travelAttendeeRepository.save(createTravelAttendee(null, member1, schedule3, AttendeeRole.AUTHOR, AttendeePermission.ALL));
        schedule1.addTravelAttendee(attendee1);
        schedule1.addTravelAttendee(attendee2);
        schedule2.addTravelAttendee(attendee3);
        schedule3.addTravelAttendee(attendee4);

        TravelRoute route1 = travelRouteRepository.save(createTravelRoute(schedule1, travelPlace1, 1));
        TravelRoute route2 = travelRouteRepository.save(createTravelRoute(schedule1, travelPlace2, 2));
        TravelRoute route3 = travelRouteRepository.save(createTravelRoute(schedule2, travelPlace1, 1));
        TravelRoute route4 = travelRouteRepository.save(createTravelRoute(schedule2, travelPlace2, 2));
        schedule1.addTravelRoutes(route1);
        schedule1.addTravelRoutes(route2);
        schedule2.addTravelRoutes(route3);
        schedule2.addTravelRoutes(route4);

        mockAuthentication(member1);

        // when, then
        mockMvc.perform(get("/api/schedules/shared")
                        .param("page", "1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalElements").value(3))
                .andExpect(jsonPath("$.data.totalSharedElements").value(1))
                .andExpect(jsonPath("$.data.content[0].sinceUpdate").exists())
                .andExpect(jsonPath("$.data.content[0].scheduleName").exists())
                .andExpect(jsonPath("$.data.content[0].thumbnailUrl").exists())
                .andExpect(jsonPath("$.data.content[0].role").exists());
    }

    @Test
    @DisplayName("공유된 일정 목록 조회 시 여행 루트 데이터가 없는 경우")
    void getSharedSchedulesNoRouteData() throws Exception {
        // given
        TravelAttendee attendee1 = travelAttendeeRepository.save(createTravelAttendee(null, member1, schedule1, AttendeeRole.AUTHOR, AttendeePermission.ALL));
        TravelAttendee attendee2 = travelAttendeeRepository.save(createTravelAttendee(null, member2, schedule1, AttendeeRole.GUEST, AttendeePermission.READ));
        TravelAttendee attendee3 = travelAttendeeRepository.save(createTravelAttendee(null, member1, schedule2, AttendeeRole.AUTHOR, AttendeePermission.ALL));
        TravelAttendee attendee4 = travelAttendeeRepository.save(createTravelAttendee(null, member1, schedule3, AttendeeRole.AUTHOR, AttendeePermission.ALL));
        schedule1.addTravelAttendee(attendee1);
        schedule1.addTravelAttendee(attendee2);
        schedule2.addTravelAttendee(attendee3);
        schedule3.addTravelAttendee(attendee4);

        mockAuthentication(member1);

        // when, then
        mockMvc.perform(get("/api/schedules/shared")
                        .param("page", "1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalElements").value(3))
                .andExpect(jsonPath("$.data.totalSharedElements").value(1))
                .andExpect(jsonPath("$.data.content[0].sinceUpdate").exists())
                .andExpect(jsonPath("$.data.content[0].scheduleName").exists())
                .andExpect(jsonPath("$.data.content[0].thumbnailUrl").isEmpty());

    }

    @Test
    @DisplayName("공유된 일정 목록 조회 시 공유된 일정 없는 경우")
    void getSharedSchedulesWithoutSharedDataByEmail() throws Exception {
        // given
        TravelAttendee attendee1 = travelAttendeeRepository.save(createTravelAttendee(null, member1, schedule1, AttendeeRole.AUTHOR, AttendeePermission.ALL));
        TravelAttendee attendee2 = travelAttendeeRepository.save(createTravelAttendee(null, member1, schedule2, AttendeeRole.AUTHOR, AttendeePermission.ALL));
        TravelAttendee attendee3 = travelAttendeeRepository.save(createTravelAttendee(null, member1, schedule3, AttendeeRole.AUTHOR, AttendeePermission.ALL));
        schedule1.addTravelAttendee(attendee1);
        schedule2.addTravelAttendee(attendee2);
        schedule3.addTravelAttendee(attendee3);

        mockAuthentication(member1);

        // when, then
        mockMvc.perform(get("/api/schedules/shared")
                        .param("page", "1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalElements").value(3))
                .andExpect(jsonPath("$.data.totalSharedElements").value(0))
                .andExpect(jsonPath("$.data.content").isEmpty());
    }

    @Test
    @DisplayName("공유된 일정 목록 조회 시 데이터 없는 경우")
    void getSharedSchedulesWithoutData() throws Exception {
        // given
        mockAuthentication(member1);

        // when, then
        mockMvc.perform(get("/api/schedules/shared")
                        .param("page", "1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalElements").value(0))
                .andExpect(jsonPath("$.data.totalSharedElements").value(0))
                .andExpect(jsonPath("$.data.content").isEmpty());
    }


    @Test
    @DisplayName("수정 권한 있는 내 일정 조회")
    void getEnableEditSchedule() throws Exception {
        // given
        TravelAttendee attendee1 = travelAttendeeRepository.save(createTravelAttendee(null, member1, schedule1, AttendeeRole.AUTHOR, AttendeePermission.ALL));
        TravelAttendee attendee2 = travelAttendeeRepository.save(createTravelAttendee(null, member2, schedule1, AttendeeRole.GUEST, AttendeePermission.READ));
        TravelAttendee attendee3 = travelAttendeeRepository.save(createTravelAttendee(null, member3, schedule2, AttendeeRole.AUTHOR, AttendeePermission.ALL));
        TravelAttendee attendee4 = travelAttendeeRepository.save(createTravelAttendee(null, member1, schedule2, AttendeeRole.GUEST, AttendeePermission.CHAT));
        schedule1.addTravelAttendee(attendee1);
        schedule1.addTravelAttendee(attendee2);
        schedule2.addTravelAttendee(attendee3);
        schedule3.addTravelAttendee(attendee4);


        mockAuthentication(member1);

        // when, then
        mockMvc.perform(get("/api/schedules/edit")
                        .param("page", "1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalElements").value(1))
                .andExpect(jsonPath("$.data.content[0].scheduleName").value(schedule1.getScheduleName()))
                .andExpect(jsonPath("$.data.content[0].author").value(member1.getNickname()));
    }

    @Test
    @DisplayName("수정 권한 있는 일정 조회 시 일정 데이터가 없는 경우")
    void getEnableEditSchedule_emptySchedules() throws Exception {
        // given
        mockAuthentication(member1);

        // when, then
        mockMvc.perform(get("/api/schedules/edit")
                        .param("page", "1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalElements").value(0))
                .andExpect(jsonPath("$.data.content").isEmpty());
    }


    @Test
    @DisplayName("전체 일정 검색")
    void searchSchedules() throws Exception {
        // given
        TravelAttendee attendee1 = travelAttendeeRepository.save(createTravelAttendee(null, member1, schedule1, AttendeeRole.AUTHOR, AttendeePermission.ALL));
        TravelAttendee attendee2 = travelAttendeeRepository.save(createTravelAttendee(null, member2, schedule1, AttendeeRole.GUEST, AttendeePermission.READ));
        TravelAttendee attendee3 = travelAttendeeRepository.save(createTravelAttendee(null, member1, schedule2, AttendeeRole.AUTHOR, AttendeePermission.ALL));
        TravelAttendee attendee4 = travelAttendeeRepository.save(createTravelAttendee(null, member1, schedule3, AttendeeRole.AUTHOR, AttendeePermission.ALL));
        schedule1.addTravelAttendee(attendee1);
        schedule1.addTravelAttendee(attendee2);
        schedule2.addTravelAttendee(attendee3);
        schedule3.addTravelAttendee(attendee4);

        TravelRoute route1 = travelRouteRepository.save(createTravelRoute(schedule1, travelPlace1, 1));
        TravelRoute route2 = travelRouteRepository.save(createTravelRoute(schedule1, travelPlace2, 2));
        TravelRoute route3 = travelRouteRepository.save(createTravelRoute(schedule2, travelPlace1, 1));
        TravelRoute route4 = travelRouteRepository.save(createTravelRoute(schedule2, travelPlace2, 2));
        schedule1.addTravelRoutes(route1);
        schedule1.addTravelRoutes(route2);
        schedule2.addTravelRoutes(route3);
        schedule2.addTravelRoutes(route4);

        mockAuthentication(member1);

        // when, then
        mockMvc.perform(get("/api/schedules/search")
                        .param("page", "1")
                        .param("type", ScheduleSearchType.ALL.getValue())
                        .param("keyword", "1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalElements").value(1))
                .andExpect(jsonPath("$.data.totalSharedElements").value(1))
                .andExpect(jsonPath("$.data.content[0].sinceUpdate").exists())
                .andExpect(jsonPath("$.data.content[0].scheduleName").value(schedule1.getScheduleName()))
                .andExpect(jsonPath("$.data.content[0].thumbnailUrl").exists())
                .andExpect(jsonPath("$.data.content[0].role").exists());
    }

    @Test
    @DisplayName("전체 일정 검색 시 여행 루트 데이터가 없는 경우")
    void searchSchedulesNoRouteData() throws Exception {
        // given
        TravelAttendee attendee1 = travelAttendeeRepository.save(createTravelAttendee(null, member1, schedule1, AttendeeRole.AUTHOR, AttendeePermission.ALL));
        TravelAttendee attendee2 = travelAttendeeRepository.save(createTravelAttendee(null, member2, schedule1, AttendeeRole.GUEST, AttendeePermission.READ));
        TravelAttendee attendee3 = travelAttendeeRepository.save(createTravelAttendee(null, member1, schedule2, AttendeeRole.AUTHOR, AttendeePermission.ALL));
        TravelAttendee attendee4 = travelAttendeeRepository.save(createTravelAttendee(null, member1, schedule3, AttendeeRole.AUTHOR, AttendeePermission.ALL));
        schedule1.addTravelAttendee(attendee1);
        schedule1.addTravelAttendee(attendee2);
        schedule2.addTravelAttendee(attendee3);
        schedule3.addTravelAttendee(attendee4);

        mockAuthentication(member1);

        // when, then
        mockMvc.perform(get("/api/schedules/search")
                        .param("page", "1")
                        .param("type", ScheduleSearchType.ALL.getValue())
                        .param("keyword", "테스트"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalElements").value(3))
                .andExpect(jsonPath("$.data.totalSharedElements").value(1))
                .andExpect(jsonPath("$.data.content[0].sinceUpdate").exists())
                .andExpect(jsonPath("$.data.content[0].scheduleName").exists())
                .andExpect(jsonPath("$.data.content[0].thumbnailUrl").isEmpty());

    }

    @Test
    @DisplayName("전체 일정 검색 시 공유된 일정 없는 경우")
    void searchSchedulesWithoutSharedData() throws Exception {
        // given
        TravelAttendee attendee1 = travelAttendeeRepository.save(createTravelAttendee(null, member1, schedule1, AttendeeRole.AUTHOR, AttendeePermission.ALL));
        TravelAttendee attendee2 = travelAttendeeRepository.save(createTravelAttendee(null, member1, schedule2, AttendeeRole.AUTHOR, AttendeePermission.ALL));
        TravelAttendee attendee3 = travelAttendeeRepository.save(createTravelAttendee(null, member1, schedule3, AttendeeRole.AUTHOR, AttendeePermission.ALL));
        schedule1.addTravelAttendee(attendee1);
        schedule2.addTravelAttendee(attendee2);
        schedule3.addTravelAttendee(attendee3);

        mockAuthentication(member1);

        // when, then
        mockMvc.perform(get("/api/schedules/search")
                        .param("page", "1")
                        .param("type", ScheduleSearchType.ALL.getValue())
                        .param("keyword", "테스트"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalElements").value(3))
                .andExpect(jsonPath("$.data.totalSharedElements").value(0))
                .andExpect(jsonPath("$.data.content").exists());
    }

    @Test
    @DisplayName("전체 일정 검색 시 검색 결과 없는 경우")
    void searchSchedulesWithoutData() throws Exception {
        // given
        mockAuthentication(member1);

        // when, then
        mockMvc.perform(get("/api/schedules/search")
                        .param("page", "1")
                        .param("type", ScheduleSearchType.ALL.getValue())
                        .param("keyword", "ㅁㄴㅇㄹ"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalElements").value(0))
                .andExpect(jsonPath("$.data.totalSharedElements").value(0))
                .andExpect(jsonPath("$.data.content").isEmpty());
    }

    @Test
    @DisplayName("공유된 일정 검색")
    void searchSharedSchedules() throws Exception {
        // given
        TravelAttendee attendee1 = travelAttendeeRepository.save(createTravelAttendee(null, member1, schedule1, AttendeeRole.AUTHOR, AttendeePermission.ALL));
        TravelAttendee attendee2 = travelAttendeeRepository.save(createTravelAttendee(null, member2, schedule1, AttendeeRole.GUEST, AttendeePermission.READ));
        TravelAttendee attendee3 = travelAttendeeRepository.save(createTravelAttendee(null, member1, schedule2, AttendeeRole.AUTHOR, AttendeePermission.ALL));
        TravelAttendee attendee4 = travelAttendeeRepository.save(createTravelAttendee(null, member1, schedule3, AttendeeRole.AUTHOR, AttendeePermission.ALL));
        schedule1.addTravelAttendee(attendee1);
        schedule1.addTravelAttendee(attendee2);
        schedule2.addTravelAttendee(attendee3);
        schedule3.addTravelAttendee(attendee4);

        TravelRoute route1 = travelRouteRepository.save(createTravelRoute(schedule1, travelPlace1, 1));
        TravelRoute route2 = travelRouteRepository.save(createTravelRoute(schedule1, travelPlace2, 2));
        TravelRoute route3 = travelRouteRepository.save(createTravelRoute(schedule2, travelPlace1, 1));
        TravelRoute route4 = travelRouteRepository.save(createTravelRoute(schedule2, travelPlace2, 2));
        schedule1.addTravelRoutes(route1);
        schedule1.addTravelRoutes(route2);
        schedule2.addTravelRoutes(route3);
        schedule2.addTravelRoutes(route4);

        mockAuthentication(member1);

        // when, then
        mockMvc.perform(get("/api/schedules/search")
                        .param("page", "1")
                        .param("type", ScheduleSearchType.SHARE.getValue())
                        .param("keyword", "테스트"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalElements").value(3))
                .andExpect(jsonPath("$.data.totalSharedElements").value(1))
                .andExpect(jsonPath("$.data.content[0].sinceUpdate").exists())
                .andExpect(jsonPath("$.data.content[0].scheduleName").value(schedule1.getScheduleName()))
                .andExpect(jsonPath("$.data.content[0].thumbnailUrl").exists())
                .andExpect(jsonPath("$.data.content[0].role").exists());
    }

    @Test
    @DisplayName("공유된 일정 검색 시 여행 루트 데이터가 없는 경우")
    void searchSharedSchedulesNoRouteData() throws Exception {
        // given
        TravelAttendee attendee1 = travelAttendeeRepository.save(createTravelAttendee(null, member1, schedule1, AttendeeRole.AUTHOR, AttendeePermission.ALL));
        TravelAttendee attendee2 = travelAttendeeRepository.save(createTravelAttendee(null, member2, schedule1, AttendeeRole.GUEST, AttendeePermission.READ));
        TravelAttendee attendee3 = travelAttendeeRepository.save(createTravelAttendee(null, member1, schedule2, AttendeeRole.AUTHOR, AttendeePermission.ALL));
        TravelAttendee attendee4 = travelAttendeeRepository.save(createTravelAttendee(null, member1, schedule3, AttendeeRole.AUTHOR, AttendeePermission.ALL));
        schedule1.addTravelAttendee(attendee1);
        schedule1.addTravelAttendee(attendee2);
        schedule2.addTravelAttendee(attendee3);
        schedule3.addTravelAttendee(attendee4);

        mockAuthentication(member1);

        // when, then
        mockMvc.perform(get("/api/schedules/search")
                        .param("page", "1")
                        .param("type", ScheduleSearchType.SHARE.getValue())
                        .param("keyword", "테스트"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalElements").value(3))
                .andExpect(jsonPath("$.data.totalSharedElements").value(1))
                .andExpect(jsonPath("$.data.content[0].sinceUpdate").exists())
                .andExpect(jsonPath("$.data.content[0].scheduleName").exists())
                .andExpect(jsonPath("$.data.content[0].thumbnailUrl").isEmpty());

    }

    @Test
    @DisplayName("공유된 일정 검색 시 검색 결과 없는 경우")
    void searchSharedSchedulesWithoutData() throws Exception {
        // given
        mockAuthentication(member1);

        // when, then
        mockMvc.perform(get("/api/schedules/search")
                        .param("page", "1")
                        .param("type", ScheduleSearchType.SHARE.getValue())
                        .param("keyword", "ㅁㄴㅇㄹ"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalElements").value(0))
                .andExpect(jsonPath("$.data.totalSharedElements").value(0))
                .andExpect(jsonPath("$.data.content").isEmpty());
    }

    @Test
    @DisplayName("전체, 공유된 일정 검색 시 검색 타입이 없어 예외 발생")
    void searchSchedules_illegalSearchType() throws Exception {
        // given
        mockAuthentication(member1);

        // when, then
        mockMvc.perform(get("/api/schedules/search")
                        .param("page", "1")
                        .param("type", "not")
                        .param("keyword", "ㅁㄴㅇㄹ"))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.ILLEGAL_SCHEDULE_SEARCH_TYPE.getMessage()));
    }

    @Test
    @DisplayName("일정 생성")
    void createSchedule() throws Exception{
        // given
        ScheduleCreateRequest request = createScheduleRequest(
                "테스트",
                LocalDate.now(),
                LocalDate.now()
        );
        mockAuthentication(member1);

        // when, then
        mockMvc.perform(post("/api/schedules")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(SuccessCode.GENERAL_SUCCESS.getMessage()));
    }

    @ParameterizedTest
    @DisplayName("일정 생성 시 일정명 빈 값으로 예외 발생")
    @ValueSource(strings = {"", " "})
    void createSchedule_invalidNotBlankScheduleName(String input) throws Exception {
        // given
        ScheduleCreateRequest request = createScheduleRequest(
                input,
                LocalDate.now(),
                LocalDate.now().plusDays(10)
        );
        mockAuthentication(member1);

        // when, then
        mockMvc.perform(post("/api/schedules")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("일정 이름은 필수 입력 값입니다.")));
    }

    @Test
    @DisplayName("일정 생성 시 일정명 null 값이 들어와 예외 발생")
    void createSchedule_invalidNullScheduleName() throws Exception {
        // given
        ScheduleCreateRequest request = createScheduleRequest(
                null,
                LocalDate.now(),
                LocalDate.now().plusDays(10)
        );
        mockAuthentication(member1);

        // when, then
        mockMvc.perform(post("/api/schedules")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("일정 이름은 필수 입력 값입니다.")));
    }

    @Test
    @DisplayName("일정 생성 시 일정 시작일 null 값이 들어와 예외 발생")
    void createSchedule_invalidNullStartDate() throws Exception {
        // given
        ScheduleCreateRequest request = createScheduleRequest(
                "테스트",
                null,
                LocalDate.now().plusDays(10)
        );
        mockAuthentication(member1);

        // when, then
        mockMvc.perform(post("/api/schedules")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("일정 시작 날짜는 필수 입력 값입니다.")));
    }

    @Test
    @DisplayName("일정 생성 시 일정 시작일 오늘 이전 날짜 값으로 예외 발생")
    void createSchedule_invalidPreviousStartDate() throws Exception {
        // given
        ScheduleCreateRequest request = createScheduleRequest(
                "테스트",
                LocalDate.now().minusDays(1),
                LocalDate.now().plusDays(10)
        );
        mockAuthentication(member1);

        // when, then
        mockMvc.perform(post("/api/schedules")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("오늘 이후 날짜만 입력 가능합니다.")));
    }


    @Test
    @DisplayName("일정 생성 시 일정 종료일 null 값이 들어와 예외 발생")
    void createSchedule_invalidNullEndDate() throws Exception {
        // given
        ScheduleCreateRequest request = createScheduleRequest(
                "테스트",
                LocalDate.now(),
                null
        );
        mockAuthentication(member1);

        // when, then
        mockMvc.perform(post("/api/schedules")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("일정 종료 날짜는 필수 입력 값입니다.")));
    }

    @Test
    @DisplayName("일정 생성 시 일정 종료일 오늘 이전 날짜 값으로 예외 발생")
    void createSchedule_invalidPreviousEndDate() throws Exception {
        // given
        ScheduleCreateRequest request = createScheduleRequest(
                "테스트",
                LocalDate.now(),
                LocalDate.now().minusDays(1)
        );
        mockAuthentication(member1);

        // when, then
        mockMvc.perform(post("/api/schedules")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("오늘 이후 날짜만 입력 가능합니다.")));
    }

    @Test
    @DisplayName("일정 생성 시 일정 생성일이 종료일 이후 날짜로 예외 발생")
    void createSchedule_invalidStartDateAfterEndDate() throws Exception {
        // given
        ScheduleCreateRequest request = createScheduleRequest(
                "테스트",
                LocalDate.now().plusDays(10),
                LocalDate.now()
        );
        mockAuthentication(member1);

        // when, then
        mockMvc.perform(post("/api/schedules")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("시작일은 종료일보다 이전이어야 합니다.")));
    }

    @Test
    @DisplayName("일정 상세 조회")
    void getScheduleDetail() throws Exception {
        // given
        TravelAttendee attendee1 = travelAttendeeRepository.save(createTravelAttendee(null, member1, schedule1, AttendeeRole.AUTHOR, AttendeePermission.ALL));
        TravelAttendee attendee2 = travelAttendeeRepository.save(createTravelAttendee(null, member2, schedule1, AttendeeRole.GUEST, AttendeePermission.READ));
        schedule1.addTravelAttendee(attendee1);
        schedule1.addTravelAttendee(attendee2);

        mockAuthentication(member1);

        // when, then
        mockMvc.perform(get("/api/schedules/{scheduleId}", schedule1.getScheduleId())
                        .param("page", "1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.scheduleName").value(schedule1.getScheduleName()))
                .andExpect(jsonPath("$.data.placeList.totalElements").value(2))
                .andExpect(jsonPath("$.data.placeList.content[0].district").value(travelPlace2.getDistrict().getDistrictName()))
                .andExpect(jsonPath("$.data.placeList.content[0].placeName").value(travelPlace2.getPlaceName()))
                .andExpect(jsonPath("$.data.placeList.content[0].thumbnailUrl").exists());
    }

    @Test
    @DisplayName("일정 상세 조회 시 여행지 데이터 존재하지 않는 경우")
    void getScheduleDetail_noTravelData() throws Exception {
        // given
        travelImageRepository.deleteAll();
        travelPlaceRepository.deleteAll();

        TravelAttendee attendee1 = travelAttendeeRepository.save(createTravelAttendee(null, member1, schedule1, AttendeeRole.AUTHOR, AttendeePermission.ALL));
        TravelAttendee attendee2 = travelAttendeeRepository.save(createTravelAttendee(null, member2, schedule1, AttendeeRole.GUEST, AttendeePermission.READ));
        schedule1.addTravelAttendee(attendee1);
        schedule1.addTravelAttendee(attendee2);

        mockAuthentication(member1);

        // when, then
        mockMvc.perform(get("/api/schedules/{scheduleId}", schedule1.getScheduleId())
                        .param("page", "1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.scheduleName").value(schedule1.getScheduleName()))
                .andExpect(jsonPath("$.data.placeList.totalElements").value(0))
                .andExpect(jsonPath("$.data.placeList.content").isEmpty());
    }

    @Test
    @DisplayName("일정 상세 조회 시 일정 데이터 존재하지 않아 예외 발생")
    void getScheduleDetail_scheduleNotFound() throws Exception {
        // given
        mockAuthentication(member1);

        // when, then
        mockMvc.perform(get("/api/schedules/{scheduleId}", 1000L)
                        .param("page", "1"))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.SCHEDULE_NOT_FOUND.getMessage()));
    }


    @Test
    @DisplayName("일정 상세 조회 시 일정에 접근 권한이 없어 예외 발생")
    void getScheduleDetail_forbiddenSchedule() throws Exception {
        // given
        mockAuthentication(member1);

        // when, then
        mockMvc.perform(delete("/api/schedules/{scheduleId}", schedule2.getScheduleId()))
                .andDo(print())
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.FORBIDDEN_ACCESS_SCHEDULE.getMessage()));
    }


    @Test
    @DisplayName("일정 수정")
    void updateSchedule() throws Exception {
        // given
        TravelAttendee attendee1 = travelAttendeeRepository.save(createTravelAttendee(null, member1, schedule1, AttendeeRole.AUTHOR, AttendeePermission.ALL));
        TravelAttendee attendee2 = travelAttendeeRepository.save(createTravelAttendee(null, member2, schedule1, AttendeeRole.GUEST, AttendeePermission.READ));
        schedule1.addTravelAttendee(attendee1);
        schedule1.addTravelAttendee(attendee2);

        TravelRoute route1 = travelRouteRepository.save(createTravelRoute(schedule1, travelPlace1, 1));
        TravelRoute route2 = travelRouteRepository.save(createTravelRoute(schedule1, travelPlace2, 2));
        schedule1.addTravelRoutes(route1);
        schedule1.addTravelRoutes(route2);

        List<RouteRequest> routeRequests = List.of(
                createRouteRequest(1, travelPlace1.getPlaceId()),
                createRouteRequest(2, travelPlace2.getPlaceId())
        );

        ScheduleUpdateRequest request = createUpdateScheduleRequest(
                "수정 테스트",
                LocalDate.now().minusDays(2),
                LocalDate.now().minusDays(1),
                routeRequests
        );

        mockAuthentication(member1);

        // when
        mockMvc.perform(patch("/api/schedules/{scheduleId}", schedule1.getScheduleId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(SuccessCode.GENERAL_SUCCESS.getMessage()));

        // then
        assertThat(schedule1.getScheduleName()).isEqualTo(request.getScheduleName());
        assertThat(schedule1.getStartDate()).isEqualTo(request.getStartDate());
        assertThat(schedule1.getEndDate()).isEqualTo(request.getEndDate());
        assertThat(schedule1.getTravelRoutes().size()).isEqualTo(2);
    }


    @Test
    @DisplayName("일정 수정 시 여행 루트 안 만든 경우")
    void updateSchedule_emptyTravelRoutes() throws Exception {
        // given
        TravelAttendee attendee1 = travelAttendeeRepository.save(createTravelAttendee(null, member1, schedule1, AttendeeRole.AUTHOR, AttendeePermission.ALL));
        TravelAttendee attendee2 = travelAttendeeRepository.save(createTravelAttendee(null, member2, schedule1, AttendeeRole.GUEST, AttendeePermission.READ));
        schedule1.addTravelAttendee(attendee1);
        schedule1.addTravelAttendee(attendee2);

        ScheduleUpdateRequest request = createUpdateScheduleRequest(
                "수정 테스트",
                LocalDate.now(),
                LocalDate.now().plusDays(1),
                null
        );

        mockAuthentication(member1);

        // when
        mockMvc.perform(patch("/api/schedules/{scheduleId}", schedule1.getScheduleId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(SuccessCode.GENERAL_SUCCESS.getMessage()));

        // then
        assertThat(schedule1.getScheduleName()).isEqualTo(request.getScheduleName());
        assertThat(schedule1.getStartDate()).isEqualTo(request.getStartDate());
        assertThat(schedule1.getEndDate()).isEqualTo(request.getEndDate());
        assertThat(schedule1.getTravelRoutes().size()).isEqualTo(0);
    }


    @ParameterizedTest
    @DisplayName("일정 수정 시 일정명 빈 값으로 예외 발생")
    @ValueSource(strings = {"", " "})
    void updateSchedule_invalidNotBlankScheduleName(String input) throws Exception {
        // given
        TravelAttendee attendee1 = travelAttendeeRepository.save(createTravelAttendee(null, member1, schedule1, AttendeeRole.AUTHOR, AttendeePermission.ALL));
        TravelAttendee attendee2 = travelAttendeeRepository.save(createTravelAttendee(null, member2, schedule1, AttendeeRole.GUEST, AttendeePermission.READ));
        schedule1.addTravelAttendee(attendee1);
        schedule1.addTravelAttendee(attendee2);

        TravelRoute route1 = travelRouteRepository.save(createTravelRoute(schedule1, travelPlace1, 1));
        TravelRoute route2 = travelRouteRepository.save(createTravelRoute(schedule1, travelPlace2, 2));
        schedule1.addTravelRoutes(route1);
        schedule1.addTravelRoutes(route2);

        List<RouteRequest> routeRequests = List.of(
                createRouteRequest(1, travelPlace1.getPlaceId()),
                createRouteRequest(2, travelPlace2.getPlaceId())
        );

        ScheduleUpdateRequest request = createUpdateScheduleRequest(
                input,
                LocalDate.now(),
                LocalDate.now().plusDays(1),
                routeRequests
        );

        mockAuthentication(member1);

        // when, then
        mockMvc.perform(patch("/api/schedules/{scheduleId}", schedule1.getScheduleId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("일정 이름은 필수 입력 값입니다.")));
    }

    @Test
    @DisplayName("일정 수정 시 일정명 null 값이 들어와 예외 발생")
    void updateSchedule_invalidNullScheduleName() throws Exception {
        // given
        TravelAttendee attendee1 = travelAttendeeRepository.save(createTravelAttendee(null, member1, schedule1, AttendeeRole.AUTHOR, AttendeePermission.ALL));
        TravelAttendee attendee2 = travelAttendeeRepository.save(createTravelAttendee(null, member2, schedule1, AttendeeRole.GUEST, AttendeePermission.READ));
        schedule1.addTravelAttendee(attendee1);
        schedule1.addTravelAttendee(attendee2);

        TravelRoute route1 = travelRouteRepository.save(createTravelRoute(schedule1, travelPlace1, 1));
        TravelRoute route2 = travelRouteRepository.save(createTravelRoute(schedule1, travelPlace2, 2));
        schedule1.addTravelRoutes(route1);
        schedule1.addTravelRoutes(route2);

        List<RouteRequest> routeRequests = List.of(
                createRouteRequest(1, travelPlace1.getPlaceId()),
                createRouteRequest(2, travelPlace2.getPlaceId())
        );

        ScheduleUpdateRequest request = createUpdateScheduleRequest(
                null,
                LocalDate.now(),
                LocalDate.now().plusDays(1),
                routeRequests
        );

        mockAuthentication(member1);

        // when, then
        mockMvc.perform(patch("/api/schedules/{scheduleId}", schedule1.getScheduleId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("일정 이름은 필수 입력 값입니다.")));
    }

    @Test
    @DisplayName("일정 수정 시 일정 시작일 null 값이 들어와 예외 발생")
    void updateSchedule_invalidNullStartDate() throws Exception {
        // given
        TravelAttendee attendee1 = travelAttendeeRepository.save(createTravelAttendee(null, member1, schedule1, AttendeeRole.AUTHOR, AttendeePermission.ALL));
        TravelAttendee attendee2 = travelAttendeeRepository.save(createTravelAttendee(null, member2, schedule1, AttendeeRole.GUEST, AttendeePermission.READ));
        schedule1.addTravelAttendee(attendee1);
        schedule1.addTravelAttendee(attendee2);

        TravelRoute route1 = travelRouteRepository.save(createTravelRoute(schedule1, travelPlace1, 1));
        TravelRoute route2 = travelRouteRepository.save(createTravelRoute(schedule1, travelPlace2, 2));
        schedule1.addTravelRoutes(route1);
        schedule1.addTravelRoutes(route2);

        List<RouteRequest> routeRequests = List.of(
                createRouteRequest(1, travelPlace1.getPlaceId()),
                createRouteRequest(2, travelPlace2.getPlaceId())
        );

        ScheduleUpdateRequest request = createUpdateScheduleRequest(
                "수정 테스트",
                null,
                LocalDate.now().plusDays(1),
                routeRequests
        );

        mockAuthentication(member1);

        // when, then
        mockMvc.perform(patch("/api/schedules/{scheduleId}", schedule1.getScheduleId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("일정 시작 날짜는 필수 입력 값입니다.")));
    }


    @Test
    @DisplayName("일정 수정 시 일정 종료일 null 값이 들어와 예외 발생")
    void updateSchedule_invalidNullEndDate() throws Exception {
        // given
        TravelAttendee attendee1 = travelAttendeeRepository.save(createTravelAttendee(null, member1, schedule1, AttendeeRole.AUTHOR, AttendeePermission.ALL));
        TravelAttendee attendee2 = travelAttendeeRepository.save(createTravelAttendee(null, member2, schedule1, AttendeeRole.GUEST, AttendeePermission.READ));
        schedule1.addTravelAttendee(attendee1);
        schedule1.addTravelAttendee(attendee2);

        TravelRoute route1 = travelRouteRepository.save(createTravelRoute(schedule1, travelPlace1, 1));
        TravelRoute route2 = travelRouteRepository.save(createTravelRoute(schedule1, travelPlace2, 2));
        schedule1.addTravelRoutes(route1);
        schedule1.addTravelRoutes(route2);

        List<RouteRequest> routeRequests = List.of(
                createRouteRequest(1, travelPlace1.getPlaceId()),
                createRouteRequest(2, travelPlace2.getPlaceId())
        );

        ScheduleUpdateRequest request = createUpdateScheduleRequest(
                "수정 테스트",
                LocalDate.now(),
                null,
                routeRequests
        );

        mockAuthentication(member1);

        // when, then
        mockMvc.perform(patch("/api/schedules/{scheduleId}", schedule1.getScheduleId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("일정 종료 날짜는 필수 입력 값입니다.")));
    }

    @Test
    @DisplayName("일정 수정 시 일정 생성일이 종료일 이후 날짜로 예외 발생")
    void updateSchedule_invalidStartDateAfterEndDate() throws Exception {
        // given
        TravelAttendee attendee1 = travelAttendeeRepository.save(createTravelAttendee(null, member1, schedule1, AttendeeRole.AUTHOR, AttendeePermission.ALL));
        TravelAttendee attendee2 = travelAttendeeRepository.save(createTravelAttendee(null, member2, schedule1, AttendeeRole.GUEST, AttendeePermission.READ));
        schedule1.addTravelAttendee(attendee1);
        schedule1.addTravelAttendee(attendee2);

        TravelRoute route1 = travelRouteRepository.save(createTravelRoute(schedule1, travelPlace1, 1));
        TravelRoute route2 = travelRouteRepository.save(createTravelRoute(schedule1, travelPlace2, 2));
        schedule1.addTravelRoutes(route1);
        schedule1.addTravelRoutes(route2);

        List<RouteRequest> routeRequests = List.of(
                createRouteRequest(1, travelPlace1.getPlaceId()),
                createRouteRequest(2, travelPlace2.getPlaceId())
        );

        ScheduleUpdateRequest request = createUpdateScheduleRequest(
                "수정 테스트",
                LocalDate.now().plusDays(1),
                LocalDate.now(),
                routeRequests
        );

        mockAuthentication(member1);

        // when, then
        mockMvc.perform(patch("/api/schedules/{scheduleId}", schedule1.getScheduleId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("시작일은 종료일보다 이전이어야 합니다.")));
    }


    @Test
    @DisplayName("일정 수정 시 여행 루트 순서 null 값이 들어와 예외 발생")
    void updateSchedule_invalidNullRouteOrder() throws Exception {
        // given
        TravelAttendee attendee1 = travelAttendeeRepository.save(createTravelAttendee(null, member1, schedule1, AttendeeRole.AUTHOR, AttendeePermission.ALL));
        TravelAttendee attendee2 = travelAttendeeRepository.save(createTravelAttendee(null, member2, schedule1, AttendeeRole.GUEST, AttendeePermission.READ));
        schedule1.addTravelAttendee(attendee1);
        schedule1.addTravelAttendee(attendee2);

        TravelRoute route1 = travelRouteRepository.save(createTravelRoute(schedule1, travelPlace1, 1));
        TravelRoute route2 = travelRouteRepository.save(createTravelRoute(schedule1, travelPlace2, 2));
        schedule1.addTravelRoutes(route1);
        schedule1.addTravelRoutes(route2);

        List<RouteRequest> routeRequests = List.of(createRouteRequest(null, travelPlace1.getPlaceId()));

        ScheduleUpdateRequest request = createUpdateScheduleRequest(
                "수정 테스트",
                LocalDate.now(),
                LocalDate.now().plusDays(1),
                routeRequests
        );

        mockAuthentication(member1);

        // when, then
        mockMvc.perform(patch("/api/schedules/{scheduleId}", schedule1.getScheduleId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("여행 루트 순서는 필수 입력 값입니다.")));
    }


    @ParameterizedTest
    @DisplayName("일정 수정 시 여행 루트 순서에 1 미만 값이 들어와 예외 발생")
    @ValueSource(ints = {-1000, -1, 0})
    void updateSchedule_invalidMinRouteOrder(Integer input) throws Exception {
        // given
        TravelAttendee attendee1 = travelAttendeeRepository.save(createTravelAttendee(null, member1, schedule1, AttendeeRole.AUTHOR, AttendeePermission.ALL));
        TravelAttendee attendee2 = travelAttendeeRepository.save(createTravelAttendee(null, member2, schedule1, AttendeeRole.GUEST, AttendeePermission.READ));
        schedule1.addTravelAttendee(attendee1);
        schedule1.addTravelAttendee(attendee2);

        TravelRoute route1 = travelRouteRepository.save(createTravelRoute(schedule1, travelPlace1, 1));
        TravelRoute route2 = travelRouteRepository.save(createTravelRoute(schedule1, travelPlace2, 2));
        schedule1.addTravelRoutes(route1);
        schedule1.addTravelRoutes(route2);


        List<RouteRequest> routeRequests = List.of(createRouteRequest(input, travelPlace1.getPlaceId()));

        ScheduleUpdateRequest request = createUpdateScheduleRequest(
                "수정 테스트",
                LocalDate.now(),
                LocalDate.now().plusDays(1),
                routeRequests
        );

        mockAuthentication(member1);

        // when, then
        mockMvc.perform(patch("/api/schedules/{scheduleId}", schedule1.getScheduleId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("여행 루트 순서는 1 이상의 값이어야 합니다.")));
    }

    @Test
    @DisplayName("일정 수정 시 여행 루트의 여행지 id에 null 값이 들어와 예외 발생")
    void updateSchedule_invalidNullPlaceId() throws Exception {
        // given
        TravelAttendee attendee1 = travelAttendeeRepository.save(createTravelAttendee(null, member1, schedule1, AttendeeRole.AUTHOR, AttendeePermission.ALL));
        TravelAttendee attendee2 = travelAttendeeRepository.save(createTravelAttendee(null, member2, schedule1, AttendeeRole.GUEST, AttendeePermission.READ));
        schedule1.addTravelAttendee(attendee1);
        schedule1.addTravelAttendee(attendee2);

        TravelRoute route1 = travelRouteRepository.save(createTravelRoute(schedule1, travelPlace1, 1));
        TravelRoute route2 = travelRouteRepository.save(createTravelRoute(schedule1, travelPlace2, 2));
        schedule1.addTravelRoutes(route1);
        schedule1.addTravelRoutes(route2);


        List<RouteRequest> routeRequests = List.of(createRouteRequest(1, null));

        ScheduleUpdateRequest request = createUpdateScheduleRequest(
                "수정 테스트",
                LocalDate.now(),
                LocalDate.now().plusDays(1),
                routeRequests
        );

        mockAuthentication(member1);

        // when, then
        mockMvc.perform(patch("/api/schedules/{scheduleId}", schedule1.getScheduleId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("여행지 ID는 필수 입력 값입니다.")));
    }




    @ParameterizedTest
    @DisplayName("일정 수정 시 여행 루트의 여행지 id에 1미만 값이 들어와 예외 발생")
    @ValueSource(longs = {-1L, 0L})
    void updateSchedule_invalidMinPlaceId(Long input) throws Exception {
        // given
        TravelAttendee attendee1 = travelAttendeeRepository.save(createTravelAttendee(null, member1, schedule1, AttendeeRole.AUTHOR, AttendeePermission.ALL));
        TravelAttendee attendee2 = travelAttendeeRepository.save(createTravelAttendee(null, member2, schedule1, AttendeeRole.GUEST, AttendeePermission.READ));
        schedule1.addTravelAttendee(attendee1);
        schedule1.addTravelAttendee(attendee2);

        TravelRoute route1 = travelRouteRepository.save(createTravelRoute(schedule1, travelPlace1, 1));
        TravelRoute route2 = travelRouteRepository.save(createTravelRoute(schedule1, travelPlace2, 2));
        schedule1.addTravelRoutes(route1);
        schedule1.addTravelRoutes(route2);


        List<RouteRequest> routeRequests = List.of(createRouteRequest(1, input));

        ScheduleUpdateRequest request = createUpdateScheduleRequest(
                "수정 테스트",
                LocalDate.now(),
                LocalDate.now().plusDays(1),
                routeRequests
        );

        mockAuthentication(member1);

        // when, then
        mockMvc.perform(patch("/api/schedules/{scheduleId}", schedule1.getScheduleId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("여행지 ID는 1 이상의 값이어야 합니다.")));
    }

    @Test
    @DisplayName("일정 수정 시 접근 권한이 없어 예외 발생")
    void updateSchedule_forbiddenAccess() throws Exception {
        // given
        TravelAttendee attendee1 = travelAttendeeRepository.save(createTravelAttendee(null, member1, schedule1, AttendeeRole.AUTHOR, AttendeePermission.ALL));
        TravelAttendee attendee2 = travelAttendeeRepository.save(createTravelAttendee(null, member2, schedule1, AttendeeRole.GUEST, AttendeePermission.READ));
        schedule1.addTravelAttendee(attendee1);
        schedule1.addTravelAttendee(attendee2);

        RouteRequest routeRequest1 = createRouteRequest(1, travelPlace1.getPlaceId());
        ScheduleUpdateRequest request = createUpdateScheduleRequest(List.of(routeRequest1));

        mockAuthentication(member3);

        // when, then
        mockMvc.perform(patch("/api/schedules/{scheduleId}", schedule1.getScheduleId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.FORBIDDEN_ACCESS_SCHEDULE.getMessage()));
    }


    @Test
    @DisplayName("일정 수정 시 수정 권한이 없어 예외 발생")
    void updateSchedule_forbiddenEdit() throws Exception {
        // given
        TravelAttendee attendee1 = travelAttendeeRepository.save(createTravelAttendee(null, member1, schedule1, AttendeeRole.AUTHOR, AttendeePermission.ALL));
        TravelAttendee attendee2 = travelAttendeeRepository.save(createTravelAttendee(null, member2, schedule1, AttendeeRole.GUEST, AttendeePermission.READ));
        schedule1.addTravelAttendee(attendee1);
        schedule1.addTravelAttendee(attendee2);

        RouteRequest routeRequest1 = createRouteRequest(1, travelPlace1.getPlaceId());
        ScheduleUpdateRequest request = createUpdateScheduleRequest(List.of(routeRequest1));

        mockAuthentication(member2);

        // when, then
        mockMvc.perform(patch("/api/schedules/{scheduleId}", schedule1.getScheduleId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.FORBIDDEN_EDIT_SCHEDULE.getMessage()));
    }


    @Test
    @DisplayName("일정 삭제")
    void deleteSchedule() throws Exception {
        // given
        TravelAttendee attendee1 = travelAttendeeRepository.save(createTravelAttendee(null, member1, schedule1, AttendeeRole.AUTHOR, AttendeePermission.ALL));
        TravelAttendee attendee2 = travelAttendeeRepository.save(createTravelAttendee(null, member2, schedule1, AttendeeRole.GUEST, AttendeePermission.READ));
        schedule1.addTravelAttendee(attendee1);
        schedule1.addTravelAttendee(attendee2);

        TravelRoute route1 = travelRouteRepository.save(createTravelRoute(schedule1, travelPlace1, 1));
        TravelRoute route2 = travelRouteRepository.save(createTravelRoute(schedule1, travelPlace2, 2));
        schedule1.addTravelRoutes(route1);
        schedule1.addTravelRoutes(route2);

        chatMessageRepository.save(createChatMessage("chat1", schedule1.getScheduleId(), member1, "hello1"));
        chatMessageRepository.save(createChatMessage("chat2", schedule1.getScheduleId(), member1, "hello2"));
        chatMessageRepository.save(createChatMessage("chat3", schedule1.getScheduleId(), member2, "hello3"));

        mockAuthentication(member1);

        // when, then
        mockMvc.perform(delete("/api/schedules/{scheduleId}", schedule1.getScheduleId()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(SuccessCode.GENERAL_SUCCESS.getMessage()));

    }

    @Test
    @DisplayName("일정 삭제 시 채팅 메시지 데이터 없는 경우")
    void deleteSchedule_noChatMessageData() throws Exception {
        // given
        TravelAttendee attendee1 = travelAttendeeRepository.save(createTravelAttendee(null, member1, schedule1, AttendeeRole.AUTHOR, AttendeePermission.ALL));
        TravelAttendee attendee2 = travelAttendeeRepository.save(createTravelAttendee(null, member2, schedule1, AttendeeRole.GUEST, AttendeePermission.READ));
        schedule1.addTravelAttendee(attendee1);
        schedule1.addTravelAttendee(attendee2);

        TravelRoute route1 = travelRouteRepository.save(createTravelRoute(schedule1, travelPlace1, 1));
        TravelRoute route2 = travelRouteRepository.save(createTravelRoute(schedule1, travelPlace2, 2));
        schedule1.addTravelRoutes(route1);
        schedule1.addTravelRoutes(route2);

        mockAuthentication(member1);

        // when, then
        mockMvc.perform(delete("/api/schedules/{scheduleId}", schedule1.getScheduleId()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(SuccessCode.GENERAL_SUCCESS.getMessage()));

    }

    @Test
    @DisplayName("일정 삭제 시 삭제 권한이 없는 회원 요청으로 예외 발생")
    void deleteSchedule_forbiddenSchedule() throws Exception {
        // given
        TravelAttendee attendee1 = travelAttendeeRepository.save(createTravelAttendee(null, member1, schedule1, AttendeeRole.AUTHOR, AttendeePermission.ALL));
        TravelAttendee attendee2 = travelAttendeeRepository.save(createTravelAttendee(null, member2, schedule1, AttendeeRole.GUEST, AttendeePermission.READ));
        schedule1.addTravelAttendee(attendee1);
        schedule1.addTravelAttendee(attendee2);

        TravelRoute route1 = travelRouteRepository.save(createTravelRoute(schedule1, travelPlace1, 1));
        TravelRoute route2 = travelRouteRepository.save(createTravelRoute(schedule1, travelPlace2, 2));
        schedule1.addTravelRoutes(route1);
        schedule1.addTravelRoutes(route2);

        mockAuthentication(member2);

        // when, then
        mockMvc.perform(delete("/api/schedules/{scheduleId}", schedule1.getScheduleId()))
                .andDo(print())
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.FORBIDDEN_DELETE_SCHEDULE.getMessage()));

    }


    @Test
    @DisplayName("여행지 조회")
    void getTravelPlaces() throws Exception {
        // given
        TravelAttendee attendee1 = travelAttendeeRepository.save(createTravelAttendee(null, member1, schedule1, AttendeeRole.AUTHOR, AttendeePermission.ALL));
        schedule1.addTravelAttendee(attendee1);

        mockAuthentication(member1);

        // when, then
        mockMvc.perform(get("/api/schedules/{scheduleId}/travels", schedule1.getScheduleId())
                        .param("page", "1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalElements").value(2))
                .andExpect(jsonPath("$.data.content[0].district").value(travelPlace2.getDistrict().getDistrictName()))
                .andExpect(jsonPath("$.data.content[0].placeName").value(travelPlace2.getPlaceName()))
                .andExpect(jsonPath("$.data.content[0].longitude").isNotEmpty())
                .andExpect(jsonPath("$.data.content[0].latitude").isNotEmpty())
                .andExpect(jsonPath("$.data.content[0].thumbnailUrl").value(travelImage1.getS3ObjectUrl()));
    }

    @Test
    @DisplayName("여행지 조회 시 여행지 데이터 존재하지 않는 경우")
    void getTravelPlacesWithoutData() throws Exception {
        // given
        travelImageRepository.deleteAll();
        travelPlaceRepository.deleteAll();

        TravelAttendee attendee1 = travelAttendeeRepository.save(createTravelAttendee(null, member1, schedule1, AttendeeRole.AUTHOR, AttendeePermission.ALL));
        schedule1.addTravelAttendee(attendee1);

        mockAuthentication(member1);

        // when, then
        mockMvc.perform(get("/api/schedules/{scheduleId}/travels", schedule1.getScheduleId())
                        .param("page", "1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalElements").value(0))
                .andExpect(jsonPath("$.data.content").isEmpty());
    }

    @Test
    @DisplayName("여행지 조회 시 해당 일정에 접근 권한이 없어 예외 발생")
    void getTravelPlaces_forbiddenScheduleAccess() throws Exception {
        // given
        mockAuthentication(member1);

        // when, then
        mockMvc.perform(get("/api/schedules/{scheduleId}/travels", schedule2.getScheduleId())
                        .param("page", "1"))
                .andDo(print())
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.FORBIDDEN_ACCESS_SCHEDULE.getMessage()));
    }



    @Test
    @DisplayName("여행지 조회 시 일정 데이터 존재하지 않아 예외 발생")
    void getTravelPlaces_scheduleNotFound() throws Exception {
        // given
        TravelAttendee attendee1 = travelAttendeeRepository.save(createTravelAttendee(null, member1, schedule1, AttendeeRole.AUTHOR, AttendeePermission.ALL));
        schedule1.addTravelAttendee(attendee1);

        mockAuthentication(member1);

        // when, then
        mockMvc.perform(get("/api/schedules/{scheduleId}/travels", 0L)
                        .param("page", "1"))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.SCHEDULE_NOT_FOUND.getMessage()));
    }


    @Test
    @DisplayName("여행지 검색")
    void searchTravelPlaces() throws Exception {
        // given
        TravelAttendee attendee1 = travelAttendeeRepository.save(createTravelAttendee(null, member1, schedule1, AttendeeRole.AUTHOR, AttendeePermission.ALL));
        schedule1.addTravelAttendee(attendee1);

        mockAuthentication(member1);

        // when, then
        mockMvc.perform(get("/api/schedules/{scheduleId}/travels/search", schedule1.getScheduleId())
                        .param("page", "1")
                        .param("keyword", "강남"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalElements").value(1))
                .andExpect(jsonPath("$.data.content[0].district").value(travelPlace1.getDistrict().getDistrictName()))
                .andExpect(jsonPath("$.data.content[0].placeName").value(travelPlace1.getPlaceName()))
                .andExpect(jsonPath("$.data.content[0].longitude").isNotEmpty())
                .andExpect(jsonPath("$.data.content[0].latitude").isNotEmpty())
                .andExpect(jsonPath("$.data.content[0].thumbnailUrl").value(travelImage1.getS3ObjectUrl()));
    }

    @Test
    @DisplayName("여행지 검색 시 검색 결과가 존재하지 않는 경우")
    void searchTravelPlacesWithoutData() throws Exception {
        // given
        TravelAttendee attendee1 = travelAttendeeRepository.save(createTravelAttendee(null, member1, schedule1, AttendeeRole.AUTHOR, AttendeePermission.ALL));
        schedule1.addTravelAttendee(attendee1);

        mockAuthentication(member1);

        // when, then
        mockMvc.perform(get("/api/schedules/{scheduleId}/travels/search", schedule1.getScheduleId())
                        .param("page", "1")
                        .param("keyword", "ㅁㄴㅇㄹ"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalElements").value(0))
                .andExpect(jsonPath("$.data.content").isEmpty());
    }


    @Test
    @DisplayName("여행지 검색 시 일정 데이터 존재하지 않아 예외 발생")
    void searchTravelPlaces_scheduleNotFound() throws Exception {
        // given
        TravelAttendee attendee1 = travelAttendeeRepository.save(createTravelAttendee(null, member1, schedule1, AttendeeRole.AUTHOR, AttendeePermission.ALL));
        schedule1.addTravelAttendee(attendee1);

        mockAuthentication(member1);

        // when, then
        mockMvc.perform(get("/api/schedules/{scheduleId}/travels/search", 0L)
                        .param("page", "1")
                        .param("keyword", "ㅁㄴㅇㄹ"))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.SCHEDULE_NOT_FOUND.getMessage()));
    }


    @Test
    @DisplayName("여행지 검색 시 해당 일정에 접근 권한이 없어 예외 발생")
    void searchTravelPlaces_forbiddenScheduleAccess() throws Exception {
        // given
        TravelAttendee attendee1 = travelAttendeeRepository.save(createTravelAttendee(null, member1, schedule1, AttendeeRole.AUTHOR, AttendeePermission.ALL));
        schedule1.addTravelAttendee(attendee1);

        mockAuthentication(member1);

        // when, then
        mockMvc.perform(get("/api/schedules/{scheduleId}/travels/search", schedule2.getScheduleId())
                        .param("page", "1")
                        .param("keyword", "중구"))
                .andDo(print())
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.FORBIDDEN_ACCESS_SCHEDULE.getMessage()));
    }

}
