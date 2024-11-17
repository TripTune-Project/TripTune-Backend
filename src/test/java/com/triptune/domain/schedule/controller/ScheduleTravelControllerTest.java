package com.triptune.domain.schedule.controller;

import com.triptune.domain.BaseTest;
import com.triptune.domain.common.entity.*;
import com.triptune.domain.common.repository.*;
import com.triptune.domain.member.entity.Member;
import com.triptune.domain.member.entity.ProfileImage;
import com.triptune.domain.member.repository.MemberRepository;
import com.triptune.domain.member.repository.ProfileImageRepository;
import com.triptune.domain.schedule.entity.TravelAttendee;
import com.triptune.domain.schedule.entity.TravelSchedule;
import com.triptune.domain.schedule.enumclass.AttendeePermission;
import com.triptune.domain.schedule.enumclass.AttendeeRole;
import com.triptune.domain.schedule.repository.TravelAttendeeRepository;
import com.triptune.domain.schedule.repository.TravelRouteRepository;
import com.triptune.domain.schedule.repository.TravelScheduleRepository;
import com.triptune.domain.travel.entity.TravelImage;
import com.triptune.domain.travel.entity.TravelPlace;
import com.triptune.domain.travel.repository.TravelImageRepository;
import com.triptune.domain.travel.repository.TravelPlaceRepository;
import com.triptune.global.enumclass.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.AutoConfigureDataMongo;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
public class ScheduleTravelControllerTest extends BaseTest {

    private final WebApplicationContext wac;
    private final TravelScheduleRepository travelScheduleRepository;
    private final TravelAttendeeRepository travelAttendeeRepository;
    private final MemberRepository memberRepository;
    private final TravelPlaceRepository travelPlaceRepository;
    private final CountryRepository countryRepository;
    private final CityRepository cityRepository;
    private final DistrictRepository districtRepository;
    private final ApiCategoryRepository apiCategoryRepository;
    private final TravelImageRepository travelImageRepository;
    private final ApiContentTypeRepository apiContentTypeRepository;

    private MockMvc mockMvc;

    private TravelSchedule schedule1;
    private TravelSchedule schedule2;

    private TravelPlace travelPlace1;
    private TravelPlace travelPlace2;


    @Autowired
    public ScheduleTravelControllerTest(WebApplicationContext wac, TravelScheduleRepository travelScheduleRepository, TravelAttendeeRepository travelAttendeeRepository, MemberRepository memberRepository, TravelPlaceRepository travelPlaceRepository, CountryRepository countryRepository, CityRepository cityRepository, DistrictRepository districtRepository, ApiCategoryRepository apiCategoryRepository, TravelImageRepository travelImageRepository, ApiContentTypeRepository apiContentTypeRepository) {
        this.wac = wac;
        this.travelScheduleRepository = travelScheduleRepository;
        this.travelAttendeeRepository = travelAttendeeRepository;
        this.memberRepository = memberRepository;
        this.travelPlaceRepository = travelPlaceRepository;
        this.countryRepository = countryRepository;
        this.cityRepository = cityRepository;
        this.districtRepository = districtRepository;
        this.apiCategoryRepository = apiCategoryRepository;
        this.travelImageRepository = travelImageRepository;
        this.apiContentTypeRepository = apiContentTypeRepository;
    }

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
        ApiContentType apiContentType = apiContentTypeRepository.save(createApiContentType("관광지"));
        travelPlace1 = travelPlaceRepository.save(createTravelPlace(null, country, city, district1, apiCategory));
        travelPlace2 = travelPlaceRepository.save(createTravelPlace(null, country, city, district2, apiCategory));
        TravelImage travelImage1 = travelImageRepository.save(createTravelImage(travelPlace1, "test1", true));
        TravelImage travelImage2 = travelImageRepository.save(createTravelImage(travelPlace1, "test2", false));
        TravelImage travelImage3 = travelImageRepository.save(createTravelImage(travelPlace2, "test1", true));
        TravelImage travelImage4 = travelImageRepository.save(createTravelImage(travelPlace2, "test2", false));
        travelPlace1.setApiContentType(apiContentType);
        travelPlace1.setTravelImageList(Arrays.asList(travelImage1, travelImage2));
        travelPlace2.setApiContentType(apiContentType);
        travelPlace2.setTravelImageList(Arrays.asList(travelImage3, travelImage4));

        Member member1 = memberRepository.save(createMember(null, "member1"));
        Member member2 = memberRepository.save(createMember(null, "member2"));

        schedule1 = travelScheduleRepository.save(createTravelSchedule(null,"테스트1"));
        schedule2 = travelScheduleRepository.save(createTravelSchedule(null,"테스트2"));

        TravelAttendee attendee1 = travelAttendeeRepository.save(createTravelAttendee(member1, schedule1, AttendeeRole.AUTHOR, AttendeePermission.ALL));
        TravelAttendee attendee2 = travelAttendeeRepository.save(createTravelAttendee(member2, schedule1, AttendeeRole.GUEST, AttendeePermission.READ));
        TravelAttendee attendee3 = travelAttendeeRepository.save(createTravelAttendee(member2, schedule2, AttendeeRole.AUTHOR, AttendeePermission.ALL));

        member1.setTravelAttendeeList(new ArrayList<>(List.of(attendee1)));
        member2.setTravelAttendeeList(new ArrayList<>(List.of(attendee2, attendee3)));
        schedule1.setTravelAttendeeList(new ArrayList<>(List.of(attendee1, attendee2)));
        schedule2.setTravelAttendeeList(new ArrayList<>(List.of(attendee3)));

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
    @DisplayName("getTravelPlaces(): 여행지 조회 시 해당 일정에 접근 권한이 없어 예외 발생")
    @WithMockUser(username = "member1")
    void getTravelPlaces_forbiddenScheduleException() throws Exception {
        // given
        // when, then
        mockMvc.perform(get("/api/schedules/{scheduleId}/travels", schedule2.getScheduleId())
                        .param("page", "1"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value(ErrorCode.FORBIDDEN_ACCESS_SCHEDULE.getMessage()));
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
        mockMvc.perform(get("/api/schedules/{scheduleId}/travels/search", 0L)
                        .param("page", "1")
                        .param("keyword", "ㅁㄴㅇㄹ"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.SCHEDULE_NOT_FOUND.getMessage()));
    }


    @Test
    @DisplayName("searchTravelPlaces(): 여행지 검색 시 해당 일정에 접근 권한이 없어 예외 발생")
    @WithMockUser(username = "member1")
    void searchTravelPlaces_forbiddenScheduleException() throws Exception {
        // given
        // when, then
        mockMvc.perform(get("/api/schedules/{scheduleId}/travels/search", schedule2.getScheduleId())
                        .param("page", "1")
                        .param("keyword", "중구"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value(ErrorCode.FORBIDDEN_ACCESS_SCHEDULE.getMessage()));
    }


}
