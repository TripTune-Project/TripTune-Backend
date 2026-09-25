package com.triptune.schedule;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.triptune.common.entity.ApiContentType;
import com.triptune.common.entity.City;
import com.triptune.common.entity.Country;
import com.triptune.common.entity.District;
import com.triptune.common.fixture.ApiContentTypeFixture;
import com.triptune.common.fixture.CityFixture;
import com.triptune.common.fixture.CountryFixture;
import com.triptune.common.fixture.DistrictFixture;
import com.triptune.common.repository.ApiContentTypeRepository;
import com.triptune.common.repository.CityRepository;
import com.triptune.common.repository.CountryRepository;
import com.triptune.common.repository.DistrictRepository;
import com.triptune.global.message.ErrorCode;
import com.triptune.global.message.SuccessCode;
import com.triptune.global.security.SecurityTestUtils;
import com.triptune.member.entity.Member;
import com.triptune.member.fixture.MemberFixture;
import com.triptune.member.repository.MemberRepository;
import com.triptune.profile.entity.ProfileImage;
import com.triptune.profile.fixture.ProfileImageFixture;
import com.triptune.profile.repository.ProfileImageRepository;
import com.triptune.schedule.dto.request.RouteCreateRequest;
import com.triptune.schedule.entity.TravelRoute;
import com.triptune.schedule.entity.TravelSchedule;
import com.triptune.schedule.fixture.TravelAttendeeFixture;
import com.triptune.schedule.fixture.TravelRouteFixture;
import com.triptune.schedule.fixture.TravelScheduleFixture;
import com.triptune.schedule.repository.TravelAttendeeRepository;
import com.triptune.schedule.repository.TravelRouteRepository;
import com.triptune.schedule.repository.TravelScheduleRepository;
import com.triptune.travel.entity.TravelPlace;
import com.triptune.travel.fixture.TravelImageFixture;
import com.triptune.travel.fixture.TravelPlaceFixture;
import com.triptune.travel.repository.TravelImageRepository;
import com.triptune.travel.repository.TravelPlaceRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.triptune.schedule.enums.AttendeePermission.READ;
import static com.triptune.travel.enums.ThemeType.ATTRACTIONS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional
@AutoConfigureMockMvc
@ActiveProfiles("h2")
public class TravelRouteIntegrationTest {
    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private TravelScheduleRepository travelScheduleRepository;
    @Autowired private TravelAttendeeRepository travelAttendeeRepository;
    @Autowired private MemberRepository memberRepository;
    @Autowired private TravelPlaceRepository travelPlaceRepository;
    @Autowired private CountryRepository countryRepository;
    @Autowired private CityRepository cityRepository;
    @Autowired private DistrictRepository districtRepository;
    @Autowired private TravelImageRepository travelImageRepository;
    @Autowired private TravelRouteRepository travelRouteRepository;
    @Autowired private ApiContentTypeRepository apiContentTypeRepository;
    @Autowired private ProfileImageRepository profileImageRepository;
    @Autowired private EntityManager em;

    private TravelSchedule schedule;

    private TravelPlace place1;
    private TravelPlace place2;
    private TravelPlace place3;

    private Member member1;

    @BeforeEach
    void setUp(){
        Country country = countryRepository.save(CountryFixture.createCountry());
        City city = cityRepository.save(CityFixture.createSeoul(country));
        District district = districtRepository.save(DistrictFixture.createDistrict(city, "강남구"));
        ApiContentType apiContentType = apiContentTypeRepository.save(ApiContentTypeFixture.createApiContentType(ATTRACTIONS));

        ProfileImage profileImage1 = profileImageRepository.save(ProfileImageFixture.createProfileImage("test1"));
        member1 = memberRepository.save(MemberFixture.createNativeTypeMember("member1@email.com", profileImage1));

        ProfileImage profileImage2 = profileImageRepository.save(ProfileImageFixture.createProfileImage("test2"));
        Member member2 = memberRepository.save(MemberFixture.createNativeTypeMember("member2@email.com", profileImage2));

        schedule = travelScheduleRepository.save(TravelScheduleFixture.createSchedule("테스트1"));

        travelAttendeeRepository.save(TravelAttendeeFixture.createAuthorAttendee(schedule, member1));
        travelAttendeeRepository.save(TravelAttendeeFixture.createGuestAttendee(schedule, member2, READ));

        place1 = travelPlaceRepository.save(
                TravelPlaceFixture.createTravelPlace(
                        country,
                        city,
                        district,
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
                        apiContentType,
                        "여행지3"
                )
        );
    }


    @Test
    @DisplayName("여행 루트 조회 성공")
    void getRoutes() throws Exception {
        // given
        TravelRoute route1 = travelRouteRepository.save(TravelRouteFixture.createRoute(schedule, place1, 1));
        TravelRoute route2 = travelRouteRepository.save(TravelRouteFixture.createRoute(schedule, place1, 2));
        TravelRoute route3 = travelRouteRepository.save(TravelRouteFixture.createRoute(schedule, place2, 3));

        SecurityTestUtils.mockAuthentication(member1);

        // when, then
        mockMvc.perform(get("/api/schedules/{scheduleId}/routes", schedule.getScheduleId())
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
    void getRoutes_withoutData() throws Exception {
        // given
        SecurityTestUtils.mockAuthentication(member1);

        // when, then
        mockMvc.perform(get("/api/schedules/{scheduleId}/routes", schedule.getScheduleId())
                        .param("page", "1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalElements").value(0))
                .andExpect(jsonPath("$.data.content").isEmpty());
    }

    @Test
    @DisplayName("여행 루트 조회 시 일정 데이터 존재하지 않아 예외 발생")
    void getRoutes_scheduleNotFound() throws Exception {
        // given
        SecurityTestUtils.mockAuthentication(member1);

        // when, then
        mockMvc.perform(get("/api/schedules/{scheduleId}/routes", 1000L)
                        .param("page", "1"))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.SCHEDULE_NOT_FOUND.getMessage()));
    }

    @Test
    @DisplayName("여행 루트 조회 시 해당 일정에 접근 권한이 없어 예외 발생")
    void getRoutes_forbiddenScheduleAccess() throws Exception {
        // given
        ProfileImage profileImage3 = profileImageRepository.save(ProfileImageFixture.createProfileImage("test3"));
        Member member3 = memberRepository.save(MemberFixture.createNativeTypeMember("member3@email.com", profileImage3));

        SecurityTestUtils.mockAuthentication(member3);

        // when, then
        mockMvc.perform(get("/api/schedules/{scheduleId}/routes", schedule.getScheduleId())
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
        travelRouteRepository.save(TravelRouteFixture.createRoute(schedule, place1, 1));
        travelRouteRepository.save(TravelRouteFixture.createRoute(schedule, place1, 2));
        travelRouteRepository.save(TravelRouteFixture.createRoute(schedule, place2, 3));

        SecurityTestUtils.mockAuthentication(member1);

        RouteCreateRequest request = TravelRouteFixture.createRouteCreateRequest(place3.getPlaceId());

        // when
        mockMvc.perform(post("/api/schedules/{scheduleId}/routes", schedule.getScheduleId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(SuccessCode.GENERAL_SUCCESS.getMessage()));

        // then
        em.flush();
        em.clear();

        List<TravelRoute> travelRoutes = travelRouteRepository.findAll();
        assertThat(travelRoutes).hasSize(4);
        assertThat(travelRoutes.get(0).getRouteOrder()).isEqualTo(1);
        assertThat(travelRoutes.get(0).getTravelPlace().getPlaceId()).isEqualTo(place1.getPlaceId());
        assertThat(travelRoutes.get(1).getRouteOrder()).isEqualTo(2);
        assertThat(travelRoutes.get(1).getTravelPlace().getPlaceId()).isEqualTo(place1.getPlaceId());
        assertThat(travelRoutes.get(2).getRouteOrder()).isEqualTo(3);
        assertThat(travelRoutes.get(2).getTravelPlace().getPlaceId()).isEqualTo(place2.getPlaceId());
        assertThat(travelRoutes.get(3).getRouteOrder()).isEqualTo(4);
        assertThat(travelRoutes.get(3).getTravelPlace().getPlaceId()).isEqualTo(place3.getPlaceId());

    }

    @Test
    @DisplayName("여행 루트의 마지막 여행지 추가 시 저장된 여행 루트가 없는 경우")
    void createLastRoute_emptyRouteList() throws Exception{
        // given
        SecurityTestUtils.mockAuthentication(member1);
        RouteCreateRequest request = TravelRouteFixture.createRouteCreateRequest(place3.getPlaceId());

        // when, then
        mockMvc.perform(post("/api/schedules/{scheduleId}/routes", schedule.getScheduleId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(SuccessCode.GENERAL_SUCCESS.getMessage()));

        List<TravelRoute> travelRoutes = travelRouteRepository.findAll();
        assertThat(travelRoutes).hasSize(1);
        assertThat(travelRoutes.get(0).getRouteOrder()).isEqualTo(1);
        assertThat(travelRoutes.get(0).getTravelPlace().getPlaceId()).isEqualTo(place3.getPlaceId());

    }

    @Test
    @DisplayName("여행 루트의 마지막 여행지 추가 시 일정 데이터 존재하지 않아 예외 발생")
    void createLastRoute_scheduleNotFound() throws Exception {
        // given
        SecurityTestUtils.mockAuthentication(member1);
        RouteCreateRequest request = TravelRouteFixture.createRouteCreateRequest(place3.getPlaceId());

        // when, then
        mockMvc.perform(post("/api/schedules/{scheduleId}/routes", 1000L)
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
        mockMvc.perform(post("/api/schedules/{scheduleId}/routes", schedule.getScheduleId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.ATTENDEE_NOT_FOUND.getMessage()));
    }


    @Test
    @DisplayName("여행 루트의 마지막 여행지 추가 시 여행지 데이터 존재하지 않아 예외 발생")
    void createLastRoute_placeNotFound() throws Exception {
        // given
        SecurityTestUtils.mockAuthentication(member1);
        RouteCreateRequest request = TravelRouteFixture.createRouteCreateRequest(1000L);

        // when, then
        mockMvc.perform(post("/api/schedules/{scheduleId}/routes", schedule.getScheduleId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.PLACE_NOT_FOUND.getMessage()));
    }


}
