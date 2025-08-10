package com.triptune.schedule.controller;

import com.triptune.common.entity.*;
import com.triptune.common.repository.*;
import com.triptune.BaseTest;
import com.triptune.member.entity.Member;
import com.triptune.member.repository.MemberRepository;
import com.triptune.schedule.entity.TravelAttendee;
import com.triptune.schedule.entity.TravelSchedule;
import com.triptune.schedule.enums.AttendeePermission;
import com.triptune.schedule.enums.AttendeeRole;
import com.triptune.schedule.repository.TravelAttendeeRepository;
import com.triptune.schedule.repository.TravelScheduleRepository;
import com.triptune.travel.entity.TravelImage;
import com.triptune.travel.entity.TravelPlace;
import com.triptune.travel.repository.TravelImageRepository;
import com.triptune.travel.repository.TravelPlaceRepository;
import com.triptune.global.response.enums.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;

import java.util.List;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("h2")
public class TravelScheduleControllerTest extends BaseTest {

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

    private MockMvc mockMvc;

    private TravelSchedule schedule1;
    private TravelSchedule schedule2;

    private TravelPlace travelPlace1;
    private TravelPlace travelPlace2;

    private TravelImage travelImage1;

    private Member member1;


    @BeforeEach
    void setUp(){
        mockMvc = MockMvcBuilders.webAppContextSetup(wac)
                .addFilter(new CharacterEncodingFilter("UTF-8", true))
                .apply(springSecurity())
                .alwaysDo(print())
                .build();

        Country country = countryRepository.save(createCountry());
        City city = cityRepository.save(createCity(country));
        District district1 = districtRepository.save(createDistrict(city, "강남구"));
        District district2 = districtRepository.save(createDistrict(city, "중구"));
        ApiCategory apiCategory = apiCategoryRepository.save(createApiCategory());

        travelPlace1 = travelPlaceRepository.save(createTravelPlace(null, country, city, district1, apiCategory));
        travelPlace2 = travelPlaceRepository.save(createTravelPlace(null, country, city, district2, apiCategory));

        travelImage1 = travelImageRepository.save(createTravelImage(travelPlace1, "test1", true));
        travelImageRepository.save(createTravelImage(travelPlace1, "test2", false));
        travelImageRepository.save(createTravelImage(travelPlace2, "test1", true));
        travelImageRepository.save(createTravelImage(travelPlace2, "test2", false));

        member1 = memberRepository.save(createMember(null, "member1@email.com"));
        Member member2 = memberRepository.save(createMember(null, "member2@email.com"));

        schedule1 = travelScheduleRepository.save(createTravelSchedule(null,"테스트1"));
        schedule2 = travelScheduleRepository.save(createTravelSchedule(null,"테스트2"));

        TravelAttendee attendee1 = travelAttendeeRepository.save(createTravelAttendee(null, member1, schedule1, AttendeeRole.AUTHOR, AttendeePermission.ALL));
        TravelAttendee attendee2 = travelAttendeeRepository.save(createTravelAttendee(null, member2, schedule1, AttendeeRole.GUEST, AttendeePermission.READ));
        TravelAttendee attendee3 = travelAttendeeRepository.save(createTravelAttendee(null, member2, schedule2, AttendeeRole.AUTHOR, AttendeePermission.ALL));

        schedule1.setTravelAttendees(List.of(attendee1, attendee2));
        schedule2.setTravelAttendees(List.of(attendee3));

    }


    @Test
    @DisplayName("여행지 조회")
    void getTravelPlaces() throws Exception {
        // given
        mockAuthentication(member1);

        // when, then
        mockMvc.perform(get("/api/schedules/{scheduleId}/travels", schedule1.getScheduleId())
                        .param("page", "1"))
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

        mockAuthentication(member1);

        // when, then
        mockMvc.perform(get("/api/schedules/{scheduleId}/travels", schedule1.getScheduleId())
                        .param("page", "1"))
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
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.FORBIDDEN_ACCESS_SCHEDULE.getMessage()));
    }



    @Test
    @DisplayName("여행지 조회 시 일정 데이터 존재하지 않아 예외 발생")
    void getTravelPlaces_scheduleNotFound() throws Exception {
        // given
        mockAuthentication(member1);

        // when, then
        mockMvc.perform(get("/api/schedules/{scheduleId}/travels", 0L)
                        .param("page", "1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.SCHEDULE_NOT_FOUND.getMessage()));
    }


    @Test
    @DisplayName("여행지 검색")
    void searchTravelPlaces() throws Exception {
        // given
        mockAuthentication(member1);

        // when, then
        mockMvc.perform(get("/api/schedules/{scheduleId}/travels/search", schedule1.getScheduleId())
                        .param("page", "1")
                        .param("keyword", "강남"))
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
        mockAuthentication(member1);

        // when, then
        mockMvc.perform(get("/api/schedules/{scheduleId}/travels/search", schedule1.getScheduleId())
                        .param("page", "1")
                        .param("keyword", "ㅁㄴㅇㄹ"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalElements").value(0))
                .andExpect(jsonPath("$.data.content").isEmpty());
    }


    @Test
    @DisplayName("여행지 검색 시 일정 데이터 존재하지 않아 예외 발생")
    void searchTravelPlaces_scheduleNotFound() throws Exception {
        // given
        mockAuthentication(member1);

        // when, then
        mockMvc.perform(get("/api/schedules/{scheduleId}/travels/search", 0L)
                        .param("page", "1")
                        .param("keyword", "ㅁㄴㅇㄹ"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.SCHEDULE_NOT_FOUND.getMessage()));
    }


    @Test
    @DisplayName("여행지 검색 시 해당 일정에 접근 권한이 없어 예외 발생")
    void searchTravelPlaces_forbiddenScheduleAccess() throws Exception {
        // given
        mockAuthentication(member1);

        // when, then
        mockMvc.perform(get("/api/schedules/{scheduleId}/travels/search", schedule2.getScheduleId())
                        .param("page", "1")
                        .param("keyword", "중구"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.FORBIDDEN_ACCESS_SCHEDULE.getMessage()));
    }


}
