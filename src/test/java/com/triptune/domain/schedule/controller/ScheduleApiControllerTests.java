package com.triptune.domain.schedule.controller;

import com.triptune.domain.common.entity.*;
import com.triptune.domain.common.repository.*;
import com.triptune.domain.member.entity.Member;
import com.triptune.domain.member.repository.MemberRepository;
import com.triptune.domain.schedule.ScheduleTest;
import com.triptune.domain.schedule.dto.CreateScheduleRequest;
import com.triptune.domain.schedule.dto.RouteResponse;
import com.triptune.domain.schedule.entity.TravelAttendee;
import com.triptune.domain.schedule.entity.TravelRoute;
import com.triptune.domain.schedule.entity.TravelSchedule;
import com.triptune.domain.schedule.repository.TravelAttendeeRepository;
import com.triptune.domain.schedule.repository.TravelRouteRepository;
import com.triptune.domain.schedule.repository.TravelScheduleRepository;
import com.triptune.domain.travel.entity.TravelImage;
import com.triptune.domain.travel.entity.TravelPlace;
import com.triptune.domain.travel.repository.TravelImageRepository;
import com.triptune.domain.travel.repository.TravelPlacePlaceRepository;
import com.triptune.global.enumclass.ErrorCode;
import com.triptune.global.exception.DataNotFoundException;
import com.triptune.global.util.PageableUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
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

    private MockMvc mockMvc;

    @Autowired
    public ScheduleApiControllerTests(WebApplicationContext wac, TravelScheduleRepository travelScheduleRepository, TravelAttendeeRepository travelAttendeeRepository, MemberRepository memberRepository, TravelPlacePlaceRepository travelPlaceRepository, CountryRepository countryRepository, CityRepository cityRepository, DistrictRepository districtRepository, ApiCategoryRepository apiCategoryRepository, FileRepository fileRepository, TravelImageRepository travelImageRepository, TravelRouteRepository travelRouteRepository) {
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
    @DisplayName("createSchedule(): 일정 만들기 성공")
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
    @DisplayName("createSchedule(): 일정 만들기 시 필요 입력값이 다 안들어와 MethodArgumentNotValidException 발생")
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
    @DisplayName("getSchedule(): 일정 조회 성공")
    @WithMockUser(username = "member1")
    void getSchedule() throws Exception {
        // given
        TravelSchedule schedule = travelScheduleRepository.save(createTravelSchedule());

        Member member1 = memberRepository.save(createMember(null, "member1"));
        Member member2 = memberRepository.save(createMember(null, "member2"));

        List<TravelAttendee> attendeeList = new ArrayList<>();
        attendeeList.add(createTravelAttendee(member1, schedule));
        attendeeList.add(createTravelAttendee(member2, schedule));
        travelAttendeeRepository.saveAll(attendeeList);

        Country country = countryRepository.save(createCountry());
        City city = cityRepository.save(createCity(country));
        District district = districtRepository.save(createDistrict(city, "중구"));
        ApiCategory apiCategory = apiCategoryRepository.save(createApiCategory());

        TravelPlace travelPlace = travelPlaceRepository.save(createTravelPlace(country, city, district, apiCategory));
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
    @DisplayName("getSchedule(): 일정 조회 시 여행지 데이터 존재하지 않는 경우")
    @WithMockUser(username = "member1")
    void getScheduleWithoutData() throws Exception {
        // given
        TravelSchedule schedule = travelScheduleRepository.save(createTravelSchedule());

        Member member1 = memberRepository.save(createMember(null, "member1"));
        Member member2 = memberRepository.save(createMember(null, "member2"));

        List<TravelAttendee> attendeeList = new ArrayList<>();
        attendeeList.add(createTravelAttendee(member1, schedule));
        attendeeList.add(createTravelAttendee(member2, schedule));
        travelAttendeeRepository.saveAll(attendeeList);


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
    @DisplayName("getSchedule(): 일정 조회 시 일정 데이터 존재하지 않아 DataNotFoundException 발생")
    @WithMockUser(username = "member1")
    void getSchedule_dataNotFoundException() throws Exception {
        // given
        // when, then
        mockMvc.perform(get("/api/schedules/{scheduleId}", 0L)
                        .param("page", "1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.DATA_NOT_FOUND.getMessage()));
    }

    @Test
    @DisplayName("getTravelPlaces(): 여행지 조회 성공")
    @WithMockUser(username = "member1")
    void getTravelPlaces() throws Exception {
        // given
        TravelSchedule schedule = travelScheduleRepository.save(createTravelSchedule());

        Country country = countryRepository.save(createCountry());
        City city = cityRepository.save(createCity(country));
        District district = districtRepository.save(createDistrict(city, "중구"));
        ApiCategory apiCategory = apiCategoryRepository.save(createApiCategory());

        TravelPlace travelPlace = travelPlaceRepository.save(createTravelPlace(country, city, district, apiCategory));

        File file1 = fileRepository.save(createFile(null, "test1", true));
        File file2 = fileRepository.save(createFile(null, "test2", false));;

        TravelImage travelImage1 = travelImageRepository.save(createTravelImage(travelPlace, file1));
        TravelImage travelImage2 = travelImageRepository.save(createTravelImage(travelPlace, file2));
        List<TravelImage> travelImageList = Arrays.asList(travelImage1, travelImage2);

        travelPlace.setTravelImageList(travelImageList);

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
    @DisplayName("getTravelPlaces(): 여행지 조회 시 여행지 데이터 존재하지 않는 경우")
    @WithMockUser(username = "member1")
    void getTravelPlacesWithoutData() throws Exception {
        // given
        TravelSchedule schedule = travelScheduleRepository.save(createTravelSchedule());

        // when, then
        mockMvc.perform(get("/api/schedules/{scheduleId}/travels", schedule.getScheduleId())
                        .param("page", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(0))
                .andExpect(jsonPath("$.data.content").isEmpty());
    }


    @Test
    @DisplayName("getTravelPlaces(): 여행지 조회 시 여행지 데이터 존재하지 않아 DataNotFoundException 발생")
    @WithMockUser(username = "member1")
    void getTravelPlaces_dataNotFoundException() throws Exception {
        // given
        // when, then
        mockMvc.perform(get("/api/schedules/{scheduleId}/travels", 0L)
                        .param("page", "1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.DATA_NOT_FOUND.getMessage()));
    }


    @Test
    @DisplayName("searchTravelPlaces(): 여행지 검색 성공")
    @WithMockUser(username = "member1")
    void searchTravelPlaces() throws Exception {
        // given
        TravelSchedule schedule = travelScheduleRepository.save(createTravelSchedule());

        Country country = countryRepository.save(createCountry());
        City city = cityRepository.save(createCity(country));
        District district1 = districtRepository.save(createDistrict(city, "강남구"));
        District district2 = districtRepository.save(createDistrict(city, "중구"));
        ApiCategory apiCategory = apiCategoryRepository.save(createApiCategory());

        TravelPlace travelPlace1 = travelPlaceRepository.save(createTravelPlace(country, city, district1, apiCategory));
        TravelPlace travelPlace2 = travelPlaceRepository.save(createTravelPlace(country, city, district2, apiCategory));

        File file1 = fileRepository.save(createFile(null, "test1", true));
        File file2 = fileRepository.save(createFile(null, "test2", false));;

        TravelImage travelImage1 = travelImageRepository.save(createTravelImage(travelPlace1, file1));
        TravelImage travelImage2 = travelImageRepository.save(createTravelImage(travelPlace1, file2));
        List<TravelImage> travelImageList1 = Arrays.asList(travelImage1, travelImage2);

        TravelImage travelImage3 = travelImageRepository.save(createTravelImage(travelPlace2, file1));
        TravelImage travelImage4 = travelImageRepository.save(createTravelImage(travelPlace2, file2));
        List<TravelImage> travelImageList2 = Arrays.asList(travelImage3, travelImage4);

        travelPlace1.setTravelImageList(travelImageList1);
        travelPlace2.setTravelImageList(travelImageList2);

        // when, then
        mockMvc.perform(get("/api/schedules/{scheduleId}/travels/search", schedule.getScheduleId())
                        .param("page", "1")
                        .param("keyword", "강남"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(1))
                .andExpect(jsonPath("$.data.content[0].district").value(district1.getDistrictName()))
                .andExpect(jsonPath("$.data.content[0].placeName").value(travelPlace1.getPlaceName()))
                .andExpect(jsonPath("$.data.content[0].thumbnailUrl").value(travelImageList1.get(0).getFile().getS3ObjectUrl()));
    }

    @Test
    @DisplayName("searchTravelPlaces(): 여행지 검색 시 검색 결과가 존재하지 않는 경우")
    @WithMockUser(username = "member1")
    void searchTravelPlacesWithoutData() throws Exception {
        // given
        TravelSchedule schedule = travelScheduleRepository.save(createTravelSchedule());

        // when, then
        mockMvc.perform(get("/api/schedules/{scheduleId}/travels/search", schedule.getScheduleId())
                        .param("page", "1")
                        .param("keyword", "ㅁㄴㅇㄹ"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(0))
                .andExpect(jsonPath("$.data.content").isEmpty());
    }


    @Test
    @DisplayName("searchTravelPlaces(): 여행지 검색 시 일정 데이터 존재하지 않아 DataNotFoundException 발생")
    @WithMockUser(username = "member1")
    void searchTravelPlaces_dataNotFoundException() throws Exception {
        // given
        // when, then
        mockMvc.perform(get("/api/schedules/{scheduleId}/travels", 0L)
                        .param("page", "1")
                        .param("keyword", "ㅁㄴㅇㄹ"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.DATA_NOT_FOUND.getMessage()));
    }

    @Test
    @DisplayName("getTravelRoutes(): 여행 루트 조회 성공")
    @WithMockUser(username = "member1")
    void getTravelRoutes() throws Exception {
        // given
        TravelSchedule schedule = travelScheduleRepository.save(createTravelSchedule());

        Country country = countryRepository.save(createCountry());
        City city = cityRepository.save(createCity(country));
        District district1 = districtRepository.save(createDistrict(city, "강남구"));
        District district2 = districtRepository.save(createDistrict(city, "중구"));
        ApiCategory apiCategory = apiCategoryRepository.save(createApiCategory());

        TravelPlace travelPlace1 = travelPlaceRepository.save(createTravelPlace(country, city, district1, apiCategory));
        TravelPlace travelPlace2 = travelPlaceRepository.save(createTravelPlace(country, city, district2, apiCategory));

        File file1 = fileRepository.save(createFile(null, "test1", true));
        File file2 = fileRepository.save(createFile(null, "test2", false));;

        TravelImage travelImage1 = travelImageRepository.save(createTravelImage(travelPlace1, file1));
        TravelImage travelImage2 = travelImageRepository.save(createTravelImage(travelPlace1, file2));
        List<TravelImage> travelImageList1 = Arrays.asList(travelImage1, travelImage2);

        TravelImage travelImage3 = travelImageRepository.save(createTravelImage(travelPlace2, file1));
        TravelImage travelImage4 = travelImageRepository.save(createTravelImage(travelPlace2, file2));
        List<TravelImage> travelImageList2 = Arrays.asList(travelImage3, travelImage4);

        travelPlace1.setTravelImageList(travelImageList1);
        travelPlace2.setTravelImageList(travelImageList2);

        TravelRoute route1 = travelRouteRepository.save(createTravelRoute(schedule, travelPlace1, 1));
        TravelRoute route2 = travelRouteRepository.save(createTravelRoute(schedule, travelPlace1, 2));
        TravelRoute route3 = travelRouteRepository.save(createTravelRoute(schedule, travelPlace2, 3));

        List<TravelRoute> travelRouteListAll = Arrays.asList(route1, route2, route3);
        List<TravelRoute> travelRouteList1 = Arrays.asList(route1, route2);
        List<TravelRoute> travelRouteList2 = List.of(route3);

        schedule.setTravelRouteList(travelRouteListAll);
        travelPlace1.setTravelRouteList(travelRouteList1);
        travelPlace2.setTravelRouteList(travelRouteList2);

        // when, then
        mockMvc.perform(get("/api/schedules/{scheduleId}/routes", schedule.getScheduleId())
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
        TravelSchedule schedule = travelScheduleRepository.save(createTravelSchedule());

        // when, then
         mockMvc.perform(get("/api/schedules/{scheduleId}/routes", schedule.getScheduleId())
                                .param("page", "1"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.data.totalElements").value(0))
                        .andExpect(jsonPath("$.data.content").isEmpty());
    }

    @Test
    @DisplayName("getTravelRoutes(): 여행 루트 조회 시 일정을 찾을 수 없어 DataNotFoundException 발생")
    @WithMockUser(username = "member1")
    void getTravelRoutes_dataNotFoundException() throws Exception {
        // given
        // when, then
        mockMvc.perform(get("/api/schedules/{scheduleId}/travels", 0L)
                        .param("page", "1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.DATA_NOT_FOUND.getMessage()));
    }

}
