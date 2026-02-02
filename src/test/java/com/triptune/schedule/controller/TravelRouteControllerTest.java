package com.triptune.schedule.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.triptune.common.entity.*;
import com.triptune.common.fixture.*;
import com.triptune.common.repository.*;
import com.triptune.global.security.SecurityTestUtils;
import com.triptune.member.entity.Member;
import com.triptune.member.fixture.MemberFixture;
import com.triptune.member.repository.MemberRepository;
import com.triptune.profile.entity.ProfileImage;
import com.triptune.profile.fixture.ProfileImageFixture;
import com.triptune.profile.repository.ProfileImageRepository;
import com.triptune.schedule.fixture.TravelAttendeeFixture;
import com.triptune.schedule.fixture.TravelRouteFixture;
import com.triptune.schedule.fixture.TravelScheduleFixture;
import com.triptune.schedule.dto.request.RouteCreateRequest;
import com.triptune.schedule.entity.TravelRoute;
import com.triptune.schedule.entity.TravelSchedule;
import com.triptune.schedule.enums.AttendeePermission;
import com.triptune.schedule.repository.TravelAttendeeRepository;
import com.triptune.schedule.repository.TravelRouteRepository;
import com.triptune.schedule.repository.TravelScheduleRepository;
import com.triptune.travel.entity.TravelPlace;
import com.triptune.travel.enums.ThemeType;
import com.triptune.travel.fixture.TravelImageFixture;
import com.triptune.travel.fixture.TravelPlaceFixture;
import com.triptune.travel.repository.TravelImageRepository;
import com.triptune.travel.repository.TravelPlaceRepository;
import com.triptune.global.message.ErrorCode;
import com.triptune.global.message.SuccessCode;
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
import org.springframework.transaction.annotation.Transactional;

import static com.triptune.schedule.enums.AttendeePermission.*;
import static com.triptune.travel.enums.ThemeType.*;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional
@AutoConfigureMockMvc
@ActiveProfiles("h2")
public class TravelRouteControllerTest {
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
    @Autowired private ApiContentTypeRepository apiContentTypeRepository;
    @Autowired private ProfileImageRepository profileImageRepository;

    private TravelSchedule schedule1;
    private TravelSchedule schedule2;

    private TravelPlace place1;
    private TravelPlace place2;
    private TravelPlace place3;

    private Member member1;
    private Member member2;


    @BeforeEach
    void setUp(){
        Country country = countryRepository.save(CountryFixture.createCountry());
        City city = cityRepository.save(CityFixture.createCity(country, "서울"));
        District district = districtRepository.save(DistrictFixture.createDistrict(city, "강남구"));
        ApiCategory apiCategory = apiCategoryRepository.save(ApiCategoryFixture.createApiCategory());
        ApiContentType apiContentType = apiContentTypeRepository.save(ApiContentTypeFixture.createApiContentType(ATTRACTIONS));

        ProfileImage profileImage1 = profileImageRepository.save(ProfileImageFixture.createProfileImage("test1"));
        member1 = memberRepository.save(MemberFixture.createNativeTypeMember("member1@email.com", profileImage1));

        ProfileImage profileImage2 = profileImageRepository.save(ProfileImageFixture.createProfileImage("test2"));
        member2 = memberRepository.save(MemberFixture.createNativeTypeMember("member2@email.com", profileImage2));

        schedule1 = travelScheduleRepository.save(TravelScheduleFixture.createTravelSchedule("테스트1"));
        schedule2 = travelScheduleRepository.save(TravelScheduleFixture.createTravelSchedule("테스트2"));

        travelAttendeeRepository.save(TravelAttendeeFixture.createAuthorTravelAttendee(schedule1, member1));
        travelAttendeeRepository.save(TravelAttendeeFixture.createGuestTravelAttendee(schedule1, member2, READ));
        travelAttendeeRepository.save(TravelAttendeeFixture.createAuthorTravelAttendee(schedule2, member2));

        place1 = travelPlaceRepository.save(
                TravelPlaceFixture.createTravelPlace(
                        country,
                        city,
                        district,
                        apiCategory,
                        apiContentType,
                        "여행지1"
                )
        );
        travelImageRepository.save(TravelImageFixture.createTravelImage(place1, "test1", true));
        travelImageRepository.save(TravelImageFixture.createTravelImage(place1, "test2", false));

        place2 = travelPlaceRepository.save(
                TravelPlaceFixture.createTravelPlace(
                        country,
                        city,
                        district,
                        apiCategory,
                        apiContentType,
                        "여행지2"
                )
        );
        travelImageRepository.save(TravelImageFixture.createTravelImage(place2, "test1", true));
        travelImageRepository.save(TravelImageFixture.createTravelImage(place2, "test2", false));

        place3 = travelPlaceRepository.save(
                TravelPlaceFixture.createTravelPlace(
                        country,
                        city,
                        district,
                        apiCategory,
                        apiContentType,
                        "여행지3"
                )
        );
    }


    @Test
    @DisplayName("여행 루트 조회 성공")
    void getTravelRoutes() throws Exception {
        // given
        TravelRoute route1 = travelRouteRepository.save(TravelRouteFixture.createTravelRoute(schedule1, place1, 1));
        TravelRoute route2 = travelRouteRepository.save(TravelRouteFixture.createTravelRoute(schedule1, place1, 2));
        TravelRoute route3 = travelRouteRepository.save(TravelRouteFixture.createTravelRoute(schedule1, place2, 3));

        SecurityTestUtils.mockAuthentication(member1);

        // when, then
        mockMvc.perform(get("/api/schedules/{scheduleId}/routes", schedule1.getScheduleId())
                        .param("page", "1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalElements").value(3))
                .andExpect(jsonPath("$.data.content[0].routeOrder").value(route1.getRouteOrder()))
                .andExpect(jsonPath("$.data.content[0].placeName").value(place1.getPlaceName()))
                .andExpect(jsonPath("$.data.content[1].routeOrder").value(route2.getRouteOrder()))
                .andExpect(jsonPath("$.data.content[1].placeName").value(place1.getPlaceName()))
                .andExpect(jsonPath("$.data.content[2].routeOrder").value(route3.getRouteOrder()))
                .andExpect(jsonPath("$.data.content[2].placeName").value(place2.getPlaceName()));
    }

    @Test
    @DisplayName("여행 루트 조회 시 저장된 여행 루트 데이터 없는 경우")
    void getTravelRoutesWithoutData() throws Exception {
        // given
        SecurityTestUtils.mockAuthentication(member1);

        // when, then
        mockMvc.perform(get("/api/schedules/{scheduleId}/routes", schedule1.getScheduleId())
                        .param("page", "1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalElements").value(0))
                .andExpect(jsonPath("$.data.content").isEmpty());
    }

    @Test
    @DisplayName("여행 루트 조회 시 일정 데이터 존재하지 않아 예외 발생")
    void getTravelRoutes_scheduleNotFound() throws Exception {
        // given
        SecurityTestUtils.mockAuthentication(member1);

        // when, then
        mockMvc.perform(get("/api/schedules/{scheduleId}/routes", 0L)
                        .param("page", "1"))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.SCHEDULE_NOT_FOUND.getMessage()));
    }

    @Test
    @DisplayName("여행 루트 조회 시 해당 일정에 접근 권한이 없어 예외 발생")
    void getTravelRoutes_forbiddenScheduleAccess() throws Exception {
        // given
        SecurityTestUtils.mockAuthentication(member1);

        // when, then
        mockMvc.perform(get("/api/schedules/{scheduleId}/routes", schedule2.getScheduleId())
                        .param("page", "1"))
                .andDo(print())
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.FORBIDDEN_ACCESS_SCHEDULE.getMessage()));
    }

    @Test
    @DisplayName("여행 루트의 마지막 여행지 추가")
    void createLastRoute() throws Exception{
        // given
        travelRouteRepository.save(TravelRouteFixture.createTravelRoute(schedule1, place1, 1));
        travelRouteRepository.save(TravelRouteFixture.createTravelRoute(schedule1, place1, 2));
        travelRouteRepository.save(TravelRouteFixture.createTravelRoute(schedule1, place2, 3));

        SecurityTestUtils.mockAuthentication(member1);

        RouteCreateRequest request = TravelRouteFixture.createRouteCreateRequest(place3.getPlaceId());

        // when, then
        mockMvc.perform(post("/api/schedules/{scheduleId}/routes", schedule1.getScheduleId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(SuccessCode.GENERAL_SUCCESS.getMessage()));
    }


    @Test
    @DisplayName("여행 루트의 마지막 여행지 추가 시 여행지 ID null 값이 들어와 예외 발생")
    void createLastRoute_invalidNullPlaceId() throws Exception {
        // given
        SecurityTestUtils.mockAuthentication(member1);
        RouteCreateRequest request = TravelRouteFixture.createRouteCreateRequest(null);

        // when, then
        mockMvc.perform(post("/api/schedules/{scheduleId}/routes", schedule1.getScheduleId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("여행지 ID는 필수 입력 값입니다.")));
    }


    @ParameterizedTest
    @DisplayName("여행 루트의 마지막 여행지 추가 시 여행지 ID에 1 미만 값이 들어와 예외 발생")
    @ValueSource(longs = {0L, -1L})
    void createLastRoute_invalidMinPlaceId(Long input) throws Exception {
        // given
        SecurityTestUtils.mockAuthentication(member1);
        RouteCreateRequest request = TravelRouteFixture.createRouteCreateRequest(input);

        // when, then
        mockMvc.perform(post("/api/schedules/{scheduleId}/routes", schedule1.getScheduleId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("여행지 ID는 1 이상의 값이어야 합니다.")));
    }

    @Test
    @DisplayName("여행 루트의 마지막 여행지 추가 시 저장된 여행 루트가 없는 경우")
    void createLastRoute_emptyRouteList() throws Exception{
        // given
        SecurityTestUtils.mockAuthentication(member1);
        RouteCreateRequest request = TravelRouteFixture.createRouteCreateRequest(place3.getPlaceId());

        // when, then
        mockMvc.perform(post("/api/schedules/{scheduleId}/routes", schedule1.getScheduleId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(SuccessCode.GENERAL_SUCCESS.getMessage()));

    }

    @Test
    @DisplayName("여행 루트의 마지막 여행지 추가 시 일정 데이터 존재하지 않아 예외 발생")
    void createLastRoute_scheduleNotFound() throws Exception {
        // given
        SecurityTestUtils.mockAuthentication(member1);
        RouteCreateRequest request = TravelRouteFixture.createRouteCreateRequest(place3.getPlaceId());

        // when, then
        mockMvc.perform(post("/api/schedules/{scheduleId}/routes", 0L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.SCHEDULE_NOT_FOUND.getMessage()));
    }


    @Test
    @DisplayName("여행 루트의 마지막 여행지 추가 시 참석자 데이터 존재하지 않아 예외 발생")
    void createLastRoute_attendeeNotFound() throws Exception {
        // given
        ProfileImage profileImage = ProfileImageFixture.createProfileImage("memberImage");
        Member member = MemberFixture.createNativeTypeMemberWithId(1000L, "notMember@email.com", profileImage);
        SecurityTestUtils.mockAuthentication(member);

        RouteCreateRequest request = TravelRouteFixture.createRouteCreateRequest(place3.getPlaceId());

        // when, then
        mockMvc.perform(post("/api/schedules/{scheduleId}/routes", schedule1.getScheduleId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.ATTENDEE_NOT_FOUND.getMessage()));
    }


    @Test
    @DisplayName("여행 루트의 마지막 여행지 추가 시 편집 권한이 없어서 예외 발생")
    void createLastRoute_forbiddenEdit() throws Exception{
        // given
        travelRouteRepository.save(TravelRouteFixture.createTravelRoute(schedule1, place1, 1));
        travelRouteRepository.save(TravelRouteFixture.createTravelRoute(schedule1, place1, 2));
        travelRouteRepository.save(TravelRouteFixture.createTravelRoute(schedule1, place2, 3));

        SecurityTestUtils.mockAuthentication(member2);

        RouteCreateRequest request = TravelRouteFixture.createRouteCreateRequest(place3.getPlaceId());

        // when, then
        mockMvc.perform(post("/api/schedules/{scheduleId}/routes", schedule1.getScheduleId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.FORBIDDEN_EDIT_SCHEDULE.getMessage()));

    }

    @Test
    @DisplayName("여행 루트의 마지막 여행지 추가 시 여행지 데이터 존재하지 않아 예외 발생")
    void createLastRoute_placeNotFound() throws Exception {
        // given
        SecurityTestUtils.mockAuthentication(member1);
        RouteCreateRequest request = TravelRouteFixture.createRouteCreateRequest(1000L);

        // when, then
        mockMvc.perform(post("/api/schedules/{scheduleId}/routes", schedule1.getScheduleId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.PLACE_NOT_FOUND.getMessage()));
    }


}
