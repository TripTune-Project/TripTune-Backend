package com.triptune.domain.schedule.controller;

import com.triptune.domain.common.entity.*;
import com.triptune.domain.common.repository.*;
import com.triptune.domain.member.entity.Member;
import com.triptune.domain.member.repository.MemberRepository;
import com.triptune.domain.schedule.ScheduleTest;
import com.triptune.domain.schedule.dto.CreateScheduleRequest;
import com.triptune.domain.schedule.entity.TravelAttendee;
import com.triptune.domain.schedule.entity.TravelSchedule;
import com.triptune.domain.schedule.repository.AttendeeRepository;
import com.triptune.domain.schedule.repository.ScheduleRepository;
import com.triptune.domain.travel.entity.TravelImage;
import com.triptune.domain.travel.entity.TravelPlace;
import com.triptune.domain.travel.repository.TravelImageRepository;
import com.triptune.domain.travel.repository.TravelRepository;
import com.triptune.global.enumclass.ErrorCode;
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
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application-test.yml")
public class ScheduleApiControllerTests extends ScheduleTest {
    private final WebApplicationContext wac;
    private final ScheduleRepository scheduleRepository;
    private final AttendeeRepository attendeeRepository;
    private final MemberRepository memberRepository;
    private final TravelRepository travelRepository;
    private final CountryRepository countryRepository;
    private final CityRepository cityRepository;
    private final DistrictRepository districtRepository;
    private final ApiCategoryRepository apiCategoryRepository;
    private final FileRepository fileRepository;
    private final TravelImageRepository travelImageRepository;
    private MockMvc mockMvc;

    @Autowired
    public ScheduleApiControllerTests(WebApplicationContext wac, ScheduleRepository scheduleRepository, AttendeeRepository attendeeRepository, MemberRepository memberRepository, TravelRepository travelRepository, CountryRepository countryRepository, CityRepository cityRepository, DistrictRepository districtRepository, ApiCategoryRepository apiCategoryRepository, FileRepository fileRepository, TravelImageRepository travelImageRepository) {
        this.wac = wac;
        this.scheduleRepository = scheduleRepository;
        this.attendeeRepository = attendeeRepository;
        this.memberRepository = memberRepository;
        this.travelRepository = travelRepository;
        this.countryRepository = countryRepository;
        this.cityRepository = cityRepository;
        this.districtRepository = districtRepository;
        this.apiCategoryRepository = apiCategoryRepository;
        this.fileRepository = fileRepository;
        this.travelImageRepository = travelImageRepository;
    }

    @BeforeEach
    void setUp(){
        mockMvc = MockMvcBuilders.webAppContextSetup(wac)
                .addFilter(new CharacterEncodingFilter("UTF-8", true))
                .apply(springSecurity())
                .alwaysDo(print())
                .build();

    }


    @Test
    @DisplayName("일정 만들기 성공")
    @WithMockUser(username = "member1")
    void createSchedule_success() throws Exception{
        // given
        CreateScheduleRequest request = createScheduleRequest();

        Member member1 = createMember(1L, "member1");
        memberRepository.save(member1);

        // when, then
        mockMvc.perform(post("/api/schedules")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJsonString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }


    @Test
    @DisplayName("일정 만들기 실패: 필요 입력값이 다 안들어온 경우")
    @WithMockUser(username = "test")
    void createSchedule_MethodArgumentNotValidException() throws Exception{
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
    @DisplayName("일정 조회 성공 : 여행지 데이터 존재하는 경우")
    @WithMockUser(username = "member1")
    void getScheduleWithPlaceList_success() throws Exception {
        // given
        TravelSchedule schedule = scheduleRepository.save(createTravelSchedule());

        Member member1 = memberRepository.save(createMember(null, "member1"));
        Member member2 = memberRepository.save(createMember(null, "member2"));

        List<TravelAttendee> attendeeList = new ArrayList<>();
        attendeeList.add(createTravelAttendee(member1, schedule));
        attendeeList.add(createTravelAttendee(member2, schedule));
        attendeeRepository.saveAll(attendeeList);

        Country country = countryRepository.save(createCountry());
        City city = cityRepository.save(createCity(country));
        District district = districtRepository.save(createDistrict(city, "중구"));
        ApiCategory apiCategory = apiCategoryRepository.save(createApiCategory());

        TravelPlace travelPlace = travelRepository.save(createTravelPlace(country, city, district, apiCategory));
        File file1 = fileRepository.save(createFile(null, "test1", true));
        File file2 = fileRepository.save(createFile(null, "test2", false));;

        TravelImage travelImage1 = travelImageRepository.save(createTravelImage(travelPlace, file1));
        TravelImage travelImage2 = travelImageRepository.save(createTravelImage(travelPlace, file2));
        List<TravelImage> travelImageList = Arrays.asList(travelImage1, travelImage2);

        travelPlace.setTravelImageList(travelImageList);

        // when, then
        mockMvc.perform(get("/api/schedules/{scheduleId}", schedule.getScheduleId())
                .param("page", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.scheduleName").value(schedule.getScheduleName()))
                .andExpect(jsonPath("$.data.attendeeList[0].userId").exists())
                .andExpect(jsonPath("$.data.placeList.totalElements").value(1))
                .andExpect(jsonPath("$.data.placeList.content[0].district").value(district.getDistrictName()))
                .andExpect(jsonPath("$.data.placeList.content[0].placeName").value(travelPlace.getPlaceName()))
                .andExpect(jsonPath("$.data.placeList.content[0].thumbnailUrl").value(travelImageList.get(0).getFile().getS3ObjectUrl()));
    }

    @Test
    @DisplayName("일정 조회 성공 : 여행지 데이터 존재하지 않는 경우")
    @WithMockUser(username = "member1")
    void getScheduleWithoutPlaceList_success() throws Exception {
        // given
        TravelSchedule schedule = scheduleRepository.save(createTravelSchedule());

        Member member1 = memberRepository.save(createMember(null, "member1"));
        Member member2 = memberRepository.save(createMember(null, "member2"));

        List<TravelAttendee> attendeeList = new ArrayList<>();
        attendeeList.add(createTravelAttendee(member1, schedule));
        attendeeList.add(createTravelAttendee(member2, schedule));
        attendeeRepository.saveAll(attendeeList);


        // when, then
        mockMvc.perform(get("/api/schedules/{scheduleId}", schedule.getScheduleId())
                        .param("page", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.scheduleName").value(schedule.getScheduleName()))
                .andExpect(jsonPath("$.data.attendeeList[0].userId").exists())
                .andExpect(jsonPath("$.data.placeList.totalElements").value(0))
                .andExpect(jsonPath("$.data.placeList.content").isEmpty());
    }

    @Test
    @DisplayName("일정 조회 실패 : 일정 데이터 존재하지 않는 경우")
    @WithMockUser(username = "member1")
    void getSchedule_notFoundException() throws Exception {
        // given
        // when, then
        mockMvc.perform(get("/api/schedules/{scheduleId}", 0L)
                        .param("page", "1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.DATA_NOT_FOUND.getMessage()));
    }

    @Test
    @DisplayName("여행지 조회 성공 : 여행지 데이터 존재하는 경우")
    @WithMockUser(username = "member1")
    void getTravelPlaces_success() throws Exception {
        // given
        TravelSchedule schedule = scheduleRepository.save(createTravelSchedule());

        Country country = countryRepository.save(createCountry());
        City city = cityRepository.save(createCity(country));
        District district = districtRepository.save(createDistrict(city, "중구"));
        ApiCategory apiCategory = apiCategoryRepository.save(createApiCategory());

        TravelPlace travelPlace = createTravelPlace(country, city, district, apiCategory);
        travelRepository.save(travelPlace);

        File file1 = fileRepository.save(createFile(null, "test1", true));
        File file2 = fileRepository.save(createFile(null, "test2", false));;

        TravelImage travelImage1 = travelImageRepository.save(createTravelImage(travelPlace, file1));
        TravelImage travelImage2 = travelImageRepository.save(createTravelImage(travelPlace, file2));
        List<TravelImage> travelImageList = Arrays.asList(travelImage1, travelImage2);

        travelPlace.setTravelImageList(travelImageList);
        travelRepository.save(travelPlace);

        // when, then
        mockMvc.perform(get("/api/schedules/{scheduleId}/travels", schedule.getScheduleId())
                        .param("page", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(1))
                .andExpect(jsonPath("$.data.content[0].district").value(district.getDistrictName()))
                .andExpect(jsonPath("$.data.content[0].placeName").value(travelPlace.getPlaceName()))
                .andExpect(jsonPath("$.data.content[0].thumbnailUrl").value(travelImageList.get(0).getFile().getS3ObjectUrl()));
    }


    @Test
    @DisplayName("여행지 조회 성공 : 여행지 데이터 존재하지 않는 경우")
    @WithMockUser(username = "member1")
    void getTravelPlacesWithoutData_success() throws Exception {
        // given
        TravelSchedule schedule = scheduleRepository.save(createTravelSchedule());

        // when, then
        mockMvc.perform(get("/api/schedules/{scheduleId}/travels", schedule.getScheduleId())
                        .param("page", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(0))
                .andExpect(jsonPath("$.data.content").isEmpty());
    }

    @Test
    @DisplayName("일정 조회 실패 : 일정 데이터 존재하지 않는 경우")
    @WithMockUser(username = "member1")
    void getTravelPlaces_notFoundException() throws Exception {
        // given
        // when, then
        mockMvc.perform(get("/api/schedules/{scheduleId}/travels", 0L)
                        .param("page", "1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.DATA_NOT_FOUND.getMessage()));
    }

}
