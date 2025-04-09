package com.triptune.schedule.controller;

import com.triptune.common.entity.*;
import com.triptune.common.repository.*;
import com.triptune.member.entity.Member;
import com.triptune.member.repository.MemberRepository;
import com.triptune.schedule.ScheduleTest;
import com.triptune.schedule.entity.TravelAttendee;
import com.triptune.schedule.entity.TravelRoute;
import com.triptune.schedule.entity.TravelSchedule;
import com.triptune.schedule.enumclass.AttendeePermission;
import com.triptune.schedule.enumclass.AttendeeRole;
import com.triptune.schedule.repository.TravelAttendeeRepository;
import com.triptune.schedule.repository.TravelRouteRepository;
import com.triptune.schedule.repository.TravelScheduleRepository;
import com.triptune.travel.entity.TravelImage;
import com.triptune.travel.entity.TravelPlace;
import com.triptune.travel.repository.TravelImageRepository;
import com.triptune.travel.repository.TravelPlaceRepository;
import com.triptune.global.enumclass.ErrorCode;
import com.triptune.global.enumclass.SuccessCode;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("h2")
public class RouteControllerTest extends ScheduleTest {
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

    private MockMvc mockMvc;

    private Country country;
    private City city;
    private District district;
    private ApiCategory apiCategory;

    private TravelSchedule schedule1;
    private TravelSchedule schedule2;

    private TravelPlace travelPlace1;
    private TravelPlace travelPlace2;
    private TravelPlace travelPlace3;

    private TravelImage travelImage1;
    private TravelImage travelImage2;
    private TravelImage travelImage3;
    private TravelImage travelImage4;


    @BeforeEach
    void setUp(){
        mockMvc = MockMvcBuilders.webAppContextSetup(wac)
                .addFilter(new CharacterEncodingFilter("UTF-8", true))
                .apply(springSecurity())
                .alwaysDo(print())
                .build();

        country = countryRepository.save(createCountry());
        city = cityRepository.save(createCity(country));
        district = districtRepository.save(createDistrict(city, "강남구"));
        apiCategory = apiCategoryRepository.save(createApiCategory());

        travelImage1 = travelImageRepository.save(createTravelImage(travelPlace1, "test1", true));
        travelImage2 = travelImageRepository.save(createTravelImage(travelPlace1, "test2", false));
        travelImage3 = travelImageRepository.save(createTravelImage(travelPlace2, "test1", true));
        travelImage4 = travelImageRepository.save(createTravelImage(travelPlace2, "test2", false));

        Member member1 = memberRepository.save(createMember(null, "member1"));
        Member member2 = memberRepository.save(createMember(null, "member2"));

        schedule1 = travelScheduleRepository.save(createTravelSchedule(null,"테스트1"));
        schedule2 = travelScheduleRepository.save(createTravelSchedule(null,"테스트2"));

        TravelAttendee attendee1 = travelAttendeeRepository.save(createTravelAttendee(0L, member1, schedule1, AttendeeRole.AUTHOR, AttendeePermission.ALL));
        TravelAttendee attendee2 = travelAttendeeRepository.save(createTravelAttendee(0L, member2, schedule1, AttendeeRole.GUEST, AttendeePermission.READ));
        TravelAttendee attendee3 = travelAttendeeRepository.save(createTravelAttendee(0L, member2, schedule2, AttendeeRole.AUTHOR, AttendeePermission.ALL));

        schedule1.setTravelAttendeeList(List.of(attendee1, attendee2));
        schedule2.setTravelAttendeeList(List.of(attendee3));

        travelPlace3 = travelPlaceRepository.save(createTravelPlace(null, country, city, district, apiCategory, List.of(travelImage3)));
    }


    @Test
    @DisplayName("여행 루트 조회 성공")
    @WithMockUser(username = "member1@email.com")
    void getTravelRoutes() throws Exception {
        // given
        travelPlace1 = travelPlaceRepository.save(createTravelPlace(null, country, city, district, apiCategory, List.of(travelImage1, travelImage2)));
        travelPlace2 = travelPlaceRepository.save(createTravelPlace(null, country, city, district, apiCategory, List.of(travelImage3, travelImage4)));

        TravelRoute route1 = travelRouteRepository.save(createTravelRoute(schedule1, travelPlace1, 1));
        TravelRoute route2 = travelRouteRepository.save(createTravelRoute(schedule1, travelPlace1, 2));
        TravelRoute route3 = travelRouteRepository.save(createTravelRoute(schedule1, travelPlace2, 3));

        schedule1.setTravelRouteList(List.of(route1, route2, route3));


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
    @DisplayName("여행 루트 조회 시 저장된 여행 루트 데이터 없는 경우")
    @WithMockUser(username = "member1@email.com")
    void getTravelRoutesWithoutData() throws Exception {
        mockMvc.perform(get("/api/schedules/{scheduleId}/routes", schedule1.getScheduleId())
                        .param("page", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(0))
                .andExpect(jsonPath("$.data.content").isEmpty());
    }

    @Test
    @DisplayName("여행 루트 조회 시 일정 데이터 존재하지 않아 예외 발생")
    @WithMockUser(username = "member1@email.com")
    void getTravelRoutes_dataNotFoundException() throws Exception {
        mockMvc.perform(get("/api/schedules/{scheduleId}/routes", 0L)
                        .param("page", "1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.SCHEDULE_NOT_FOUND.getMessage()));
    }

    @Test
    @DisplayName("여행 루트 조회 시 해당 일정에 접근 권한이 없어 예외 발생")
    @WithMockUser(username = "member1@email.com")
    void getTravelRoutes_forbiddenScheduleException() throws Exception {
        mockMvc.perform(get("/api/schedules/{scheduleId}/routes", schedule2.getScheduleId())
                        .param("page", "1"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value(ErrorCode.FORBIDDEN_ACCESS_SCHEDULE.getMessage()));
    }

    @Test
    @DisplayName("여행 루트의 마지막 여행지 추가")
    @WithMockUser(username = "member1@email.com")
    void createLastRoute() throws Exception{
        // given
        TravelRoute route1 = travelRouteRepository.save(createTravelRoute(schedule1, travelPlace1, 1));
        TravelRoute route2 = travelRouteRepository.save(createTravelRoute(schedule1, travelPlace1, 2));
        TravelRoute route3 = travelRouteRepository.save(createTravelRoute(schedule1, travelPlace2, 3));

        schedule1.setTravelRouteList(List.of(route1, route2, route3));
        travelPlace1 = travelPlaceRepository.save(createTravelPlace(null, country, city, district, apiCategory, List.of(travelImage1, travelImage2)));
        travelPlace2 = travelPlaceRepository.save(createTravelPlace(null, country, city, district, apiCategory, List.of(travelImage3, travelImage4)));


        // when, then
        mockMvc.perform(post("/api/schedules/{scheduleId}/routes", schedule1.getScheduleId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonString(createRouteCreateRequest(travelPlace3.getPlaceId()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(SuccessCode.GENERAL_SUCCESS.getMessage()));

    }

    @Test
    @DisplayName("여행 루트의 마지막 여행지 추가 시 저장된 여행 루트가 없는 경우")
    @WithMockUser(username = "member1@email.com")
    void createLastRoute_emptyRouteList() throws Exception{
        schedule1.setTravelRouteList(new ArrayList<>());

        mockMvc.perform(post("/api/schedules/{scheduleId}/routes", schedule1.getScheduleId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonString(createRouteCreateRequest(travelPlace3.getPlaceId()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(SuccessCode.GENERAL_SUCCESS.getMessage()));

    }

    @Test
    @DisplayName("여행 루트의 마지막 여행지 추가 시 일정 데이터 존재하지 않아 예외 발생")
    @WithMockUser(username = "member1@email.com")
    void createLastRoute_scheduleNotFoundException() throws Exception {
        mockMvc.perform(post("/api/schedules/{scheduleId}/routes", 0L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonString(createRouteCreateRequest(travelPlace3.getPlaceId()))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.SCHEDULE_NOT_FOUND.getMessage()));
    }


    @Test
    @DisplayName("여행 루트의 마지막 여행지 추가 시 참석자 데이터 존재하지 않아 예외 발생")
    @WithMockUser(username = "member3@email.com")
    void createLastRoute_attendeeNotFoundException() throws Exception {
        mockMvc.perform(post("/api/schedules/{scheduleId}/routes", schedule1.getScheduleId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonString(createRouteCreateRequest(travelPlace3.getPlaceId()))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.ATTENDEE_NOT_FOUND.getMessage()));
    }


    @Test
    @DisplayName("여행 루트의 마지막 여행지 추가 시 편집 권한이 없어서 예외 발생")
    @WithMockUser(username = "member2@email.com")
    void createLastRoute_forbiddenScheduleException() throws Exception{
        // given
        TravelRoute route1 = travelRouteRepository.save(createTravelRoute(schedule1, travelPlace1, 1));
        TravelRoute route2 = travelRouteRepository.save(createTravelRoute(schedule1, travelPlace1, 2));
        TravelRoute route3 = travelRouteRepository.save(createTravelRoute(schedule1, travelPlace2, 3));

        schedule1.setTravelRouteList(List.of(route1, route2, route3));
        travelPlace1 = travelPlaceRepository.save(createTravelPlace(null, country, city, district, apiCategory, List.of(travelImage1, travelImage2)));
        travelPlace2 = travelPlaceRepository.save(createTravelPlace(null, country, city, district, apiCategory, List.of(travelImage3, travelImage4)));


        // when, then
        mockMvc.perform(post("/api/schedules/{scheduleId}/routes", schedule1.getScheduleId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonString(createRouteCreateRequest(travelPlace3.getPlaceId()))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value(ErrorCode.FORBIDDEN_EDIT_SCHEDULE.getMessage()));

    }

    @Test
    @DisplayName("여행 루트의 마지막 여행지 추가 시 여행지 데이터 존재하지 않아 예외 발생")
    @WithMockUser(username = "member1@email.com")
    void createLastRoute_placeNotFoundException() throws Exception {
        mockMvc.perform(post("/api/schedules/{scheduleId}/routes", schedule1.getScheduleId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonString(createRouteCreateRequest(0L))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.PLACE_NOT_FOUND.getMessage()));
    }




}
