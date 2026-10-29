package com.triptune.route;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.triptune.common.entity.ApiContentType;
import com.triptune.common.entity.City;
import com.triptune.common.entity.Country;
import com.triptune.common.entity.District;
import com.triptune.common.fixture.*;
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
import com.triptune.route.dto.request.RouteCreateRequest;
import com.triptune.route.dto.request.RouteUpdateRequest;
import com.triptune.route.entity.TravelRoute;
import com.triptune.schedule.entity.TravelSchedule;
import com.triptune.attendee.fixture.TravelAttendeeFixture;
import com.triptune.route.fixture.TravelRouteFixture;
import com.triptune.schedule.fixture.TravelScheduleFixture;
import com.triptune.attendee.repository.TravelAttendeeRepository;
import com.triptune.route.repository.TravelRouteRepository;
import com.triptune.schedule.repository.TravelScheduleRepository;
import com.triptune.travel.entity.TravelImage;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.triptune.attendee.enums.AttendeePermission.READ;
import static com.triptune.travel.enums.ThemeType.ATTRACTIONS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
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

    private TravelPlace place1WithThumb;
    private TravelPlace place2WithThumb;
    private TravelPlace place3WithoutThumb;

    private String place1ThumbUrl;
    private String place2ThumbUrl;

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

        place1WithThumb = travelPlaceRepository.save(
                TravelPlaceFixture.createTravelPlace(
                        country,
                        city,
                        district,
                        apiContentType,
                        "여행지1"
                )
        );
        TravelImage place1Thumb = travelImageRepository.save(TravelImageFixture.createTravelImage(place1WithThumb, "test1", true));
        travelImageRepository.save(TravelImageFixture.createTravelImage(place1WithThumb, "test2", false));
        place1ThumbUrl = S3Fixture.createS3ObjectUrl(place1Thumb.getS3ObjectKey());

        place2WithThumb = travelPlaceRepository.save(
                TravelPlaceFixture.createTravelPlace(
                        country,
                        city,
                        district,
                        apiContentType,
                        "여행지2"
                )
        );
        TravelImage place2Thumb = travelImageRepository.save(TravelImageFixture.createTravelImage(place2WithThumb, "test1", true));
        travelImageRepository.save(TravelImageFixture.createTravelImage(place2WithThumb, "test2", false));
        place2ThumbUrl = S3Fixture.createS3ObjectUrl(place2Thumb.getS3ObjectKey());


        place3WithoutThumb = travelPlaceRepository.save(
                TravelPlaceFixture.createTravelPlace(
                        country,
                        city,
                        district,
                        apiContentType,
                        "여행지3"
                )
        );

        SecurityTestUtils.mockAuthentication(member1);
    }


    @Test
    @DisplayName("여행 루트 조회")
    void getTravelRoutes() throws Exception {
        // given
        int routeVersion = schedule.getRouteVersion();

        TravelRoute route1 = travelRouteRepository.save(TravelRouteFixture.createRoute(schedule, place1WithThumb, 1));
        TravelRoute route2 = travelRouteRepository.save(TravelRouteFixture.createRoute(schedule, place2WithThumb, 2));
        TravelRoute route3 = travelRouteRepository.save(TravelRouteFixture.createRoute(schedule, place3WithoutThumb, 3));

        // when, then
        mockMvc.perform(get("/api/schedules/{scheduleId}/routes", schedule.getScheduleId())
                        .param("page", "1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.routeVersion").value(routeVersion))
                .andExpect(jsonPath("$.data.totalElements").value(3))
                .andExpect(jsonPath("$.data.content[0].routeId").value(route1.getRouteId()))
                .andExpect(jsonPath("$.data.content[0].routeOrder").value(route1.getRouteOrder()))
                .andExpect(jsonPath("$.data.content[0].placeName").value(place1WithThumb.getPlaceName()))
                .andExpect(jsonPath("$.data.content[0].thumbnailUrl").value(place1ThumbUrl))
                .andExpect(jsonPath("$.data.content[1].routeOrder").value(route2.getRouteOrder()))
                .andExpect(jsonPath("$.data.content[1].placeName").value(place2WithThumb.getPlaceName()))
                .andExpect(jsonPath("$.data.content[1].thumbnailUrl").value(place2ThumbUrl))
                .andExpect(jsonPath("$.data.content[2].routeOrder").value(route3.getRouteOrder()))
                .andExpect(jsonPath("$.data.content[2].placeName").value(place3WithoutThumb.getPlaceName()))
                .andExpect(jsonPath("$.data.content[2].thumbnailUrl", nullValue()));
    }

    @Test
    @DisplayName("여행 루트 조회 시 저장된 여행 루트 데이터 없는 경우")
    void getTravelRoutes_emptyResult() throws Exception {
        // given
        int routeVersion = schedule.getRouteVersion();

        // when, then
        mockMvc.perform(get("/api/schedules/{scheduleId}/routes", schedule.getScheduleId())
                        .param("page", "1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.routeVersion").value(routeVersion))
                .andExpect(jsonPath("$.data.totalElements").value(0))
                .andExpect(jsonPath("$.data.content").isEmpty());
    }

    @Test
    @DisplayName("여행 루트 조회 시 일정 데이터 존재하지 않아 예외 발생")
    void getTravelRoutes_scheduleNotFound() throws Exception {
        // given
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
    void getTravelRoutes_forbiddenScheduleAccess() throws Exception {
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
    @DisplayName("여행 루트 생성")
    void createTravelRoute() throws Exception{
        // given
        int routeVersion = schedule.getRouteVersion();

        travelRouteRepository.save(TravelRouteFixture.createRoute(schedule, place1WithThumb, 1));
        travelRouteRepository.save(TravelRouteFixture.createRoute(schedule, place1WithThumb, 2));
        travelRouteRepository.save(TravelRouteFixture.createRoute(schedule, place2WithThumb, 3));

        RouteCreateRequest request = TravelRouteFixture.createRouteCreateRequest(
                place3WithoutThumb.getPlaceId(),
                routeVersion
        );

        // when
        mockMvc.perform(post("/api/schedules/{scheduleId}/routes", schedule.getScheduleId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(SuccessCode.GENERAL_SUCCESS.getMessage()))
                .andExpect(jsonPath("$.data.routeVersion").value(routeVersion+1));

        // then
        em.flush();
        em.clear();

        List<TravelRoute> travelRoutes = travelRouteRepository.findAllByTravelSchedule_ScheduleId(schedule.getScheduleId());
        assertThat(travelRoutes).hasSize(4);
        assertThat(travelRoutes)
                .extracting(
                        TravelRoute::getRouteOrder,
                        r -> r.getTravelPlace().getPlaceId()
                )
                .containsExactly(
                    tuple(1, place1WithThumb.getPlaceId()),
                    tuple(2, place1WithThumb.getPlaceId()),
                    tuple(3, place2WithThumb.getPlaceId()),
                    tuple(4, place3WithoutThumb.getPlaceId())
                );
    }

    @Test
    @DisplayName("여행 루트 생성 시 처음 저장인 경우")
    void createTravelRoute_first() throws Exception{
        // given
        int routeVersion = schedule.getRouteVersion();

        RouteCreateRequest request = TravelRouteFixture.createRouteCreateRequest(
                place3WithoutThumb.getPlaceId(),
                routeVersion
        );

        // when, then
        mockMvc.perform(post("/api/schedules/{scheduleId}/routes", schedule.getScheduleId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(SuccessCode.GENERAL_SUCCESS.getMessage()))
                .andExpect(jsonPath("$.data.routeVersion").value(routeVersion+1));

        List<TravelRoute> travelRoutes = travelRouteRepository.findAllByTravelSchedule_ScheduleId(schedule.getScheduleId());
        assertThat(travelRoutes).hasSize(1);
        assertThat(travelRoutes.get(0).getRouteOrder()).isEqualTo(1);
        assertThat(travelRoutes.get(0).getTravelPlace().getPlaceId()).isEqualTo(place3WithoutThumb.getPlaceId());

    }

    @Test
    @DisplayName("여행 루트 생성 시 일정 데이터 존재하지 않아 예외 발생")
    void createTravelRoute_scheduleNotFound() throws Exception {
        // given
        RouteCreateRequest request = TravelRouteFixture.createRouteCreateRequest(
                place3WithoutThumb.getPlaceId(),
                schedule.getRouteVersion()
        );

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
    @DisplayName("여행 루트 생성 시 참석자 데이터 존재하지 않아 예외 발생")
    void createTravelRoute_attendeeNotFound() throws Exception {
        // given
        ProfileImage profileImage = ProfileImageFixture.createProfileImage("memberImage");
        Member member = MemberFixture.createNativeTypeMemberWithId(1000L, "notMember@email.com", profileImage);
        SecurityTestUtils.mockAuthentication(member);

        RouteCreateRequest request = TravelRouteFixture.createRouteCreateRequest(
                place3WithoutThumb.getPlaceId(),
                schedule.getRouteVersion()
        );

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
    @DisplayName("여행 루트 생성 시 여행지 데이터 존재하지 않아 예외 발생")
    void createTravelRoute_placeNotFound() throws Exception {
        // given
        RouteCreateRequest request = TravelRouteFixture.createRouteCreateRequest(
                1000L,
                schedule.getRouteVersion()
        );

        // when, then
        mockMvc.perform(post("/api/schedules/{scheduleId}/routes", schedule.getScheduleId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.PLACE_NOT_FOUND.getMessage()));
    }


    @Test
    @DisplayName("여행 루트 생성 시 여행 루트 버전 충돌로 409 반환")
    void createTravelRoute_conflictRouteVersion() throws Exception {
        // given
        RouteCreateRequest request = TravelRouteFixture.createRouteCreateRequest(
                place3WithoutThumb.getPlaceId(),
                schedule.getRouteVersion()+1
        );

        // when, then
        mockMvc.perform(post("/api/schedules/{scheduleId}/routes", schedule.getScheduleId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.CONFLICT_ROUTE_VERSION.getMessage()));
    }

    @Test
    @DisplayName("여행 루트 수정")
    void updateTravelRoute() throws Exception{
        // given
        int routeVersion = schedule.getRouteVersion();

        TravelRoute route1 = travelRouteRepository.save(TravelRouteFixture.createRoute(schedule, place1WithThumb, 1));
        TravelRoute route2 = travelRouteRepository.save(TravelRouteFixture.createRoute(schedule, place1WithThumb, 2));
        TravelRoute route3 = travelRouteRepository.save(TravelRouteFixture.createRoute(schedule, place2WithThumb, 3));

        List<Long> routeIds = new ArrayList<>(List.of(route3.getRouteId(), route2.getRouteId(), route1.getRouteId()));
        RouteUpdateRequest request = TravelRouteFixture.createRouteUpdateRequest(routeVersion, routeIds);

        // when, then
        mockMvc.perform(patch("/api/schedules/{scheduleId}/routes", schedule.getScheduleId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(SuccessCode.GENERAL_SUCCESS.getMessage()))
                .andExpect(jsonPath("$.data.routeVersion").value(routeVersion+1));

        List<TravelRoute> travelRoutes = travelRouteRepository.findAllByTravelSchedule_ScheduleId(schedule.getScheduleId());
        assertThat(travelRoutes).hasSize(3);
        assertThat(travelRoutes)
                .extracting(
                        TravelRoute::getRouteOrder,
                        r -> r.getTravelPlace().getPlaceId()
                )
                .containsExactly(
                        tuple(3, place1WithThumb.getPlaceId()),
                        tuple(2, place1WithThumb.getPlaceId()),
                        tuple(1, place2WithThumb.getPlaceId())
                );
    }


    @Test
    @DisplayName("여행 루트 수정 - 저장된 데이터 1개인 경우")
    void updateTravelRoute_one() throws Exception{
        // given
        int routeVersion = schedule.getRouteVersion();

        TravelRoute route1 = travelRouteRepository.save(TravelRouteFixture.createRoute(schedule, place1WithThumb, 1));

        List<Long> routeIds = new ArrayList<>(List.of(route1.getRouteId()));
        RouteUpdateRequest request = TravelRouteFixture.createRouteUpdateRequest(routeVersion, routeIds);

        // when, then
        mockMvc.perform(patch("/api/schedules/{scheduleId}/routes", schedule.getScheduleId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(SuccessCode.GENERAL_SUCCESS.getMessage()))
                .andExpect(jsonPath("$.data.routeVersion").value(routeVersion+1));

        List<TravelRoute> travelRoutes = travelRouteRepository.findAllByTravelSchedule_ScheduleId(schedule.getScheduleId());
        assertThat(travelRoutes).hasSize(1);
        assertThat(travelRoutes.get(0).getRouteOrder()).isEqualTo(1);
        assertThat(travelRoutes.get(0).getRouteId()).isEqualTo(route1.getRouteId());
    }


    @Test
    @DisplayName("여행 루트 수정 시 저장된 데이터 갯수와 일치하지 않아 예외 발생")
    void updateTravelRoute_sizeMismatch() throws Exception {
        // given
        TravelRoute route1 = travelRouteRepository.save(TravelRouteFixture.createRoute(schedule, place1WithThumb, 1));
        TravelRoute route2 = travelRouteRepository.save(TravelRouteFixture.createRoute(schedule, place1WithThumb, 2));

        List<Long> routeIds = new ArrayList<>(List.of(3L, route2.getRouteId(), route1.getRouteId()));
        RouteUpdateRequest request = TravelRouteFixture.createRouteUpdateRequest(0, routeIds);

        // when, then
        mockMvc.perform(patch("/api/schedules/{scheduleId}/routes", schedule.getScheduleId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString(ErrorCode.ROUTE_MISMATCH.getMessage())));
    }


    @Test
    @DisplayName("여행 루트 수정 시 저장된 데이터와 일치하지 않아 예외 발생")
    void updateTravelRoute_savedMismatch()  throws Exception {
        // given
        TravelRoute route1 = travelRouteRepository.save(TravelRouteFixture.createRoute(schedule, place1WithThumb, 1));
        travelRouteRepository.save(TravelRouteFixture.createRoute(schedule, place1WithThumb, 2));

        List<Long> routeIds = new ArrayList<>(List.of(1004L, route1.getRouteId()));
        RouteUpdateRequest request = TravelRouteFixture.createRouteUpdateRequest(0, routeIds);


        // when, then
        mockMvc.perform(patch("/api/schedules/{scheduleId}/routes", schedule.getScheduleId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.ROUTE_MISMATCH.getMessage()));
    }

    @Test
    @DisplayName("여행 루트 수정 시 요청값에 중복이 들어와 예외 발생")
    void updateTravelRoute_duplicatedRouteId() throws Exception {
        // given
        TravelRoute route1 = travelRouteRepository.save(TravelRouteFixture.createRoute(schedule, place1WithThumb, 1));

        List<Long> routeIds = new ArrayList<>(List.of(route1.getRouteId(), route1.getRouteId()));
        RouteUpdateRequest request = TravelRouteFixture.createRouteUpdateRequest(0, routeIds);


        // when, then
        mockMvc.perform(patch("/api/schedules/{scheduleId}/routes", schedule.getScheduleId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.ROUTE_MISMATCH.getMessage()));
    }


    @Test
    @DisplayName("여행 루트 수정 시 저장된 데이터가 없어 예외 발생")
    void updateTravelRoute_emptySavedRoute() throws Exception{
        // given
        List<Long> routeIds = new ArrayList<>(List.of(1L, 2L, 3L));
        RouteUpdateRequest request = TravelRouteFixture.createRouteUpdateRequest(0, routeIds);

        // when, then
        mockMvc.perform(patch("/api/schedules/{scheduleId}/routes", schedule.getScheduleId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.ROUTE_MISMATCH.getMessage()));

    }


    @Test
    @DisplayName("여행 루트 수정 시 빈 리스트 요청으로 예외 발생")
    void updateTravelRoute_emptyRouteIds() throws Exception{
        // given
        travelRouteRepository.save(TravelRouteFixture.createRoute(schedule, place1WithThumb, 1));
        travelRouteRepository.save(TravelRouteFixture.createRoute(schedule, place1WithThumb, 2));
        travelRouteRepository.save(TravelRouteFixture.createRoute(schedule, place2WithThumb, 3));

        List<Long> routeIds = Collections.emptyList();
        RouteUpdateRequest request = TravelRouteFixture.createRouteUpdateRequest(0, routeIds);

        // when, then
        mockMvc.perform(patch("/api/schedules/{scheduleId}/routes", schedule.getScheduleId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.ROUTE_MISMATCH.getMessage()));

    }

    @Test
    @DisplayName("여행 루트 수정 시 저장된 데이터와 요청에 빈 리스트로 예외 발생")
    void updateTravelRoute_emptySavedRouteAndRouteIds() throws Exception{
        // given
        List<Long> routeIds = Collections.emptyList();
        RouteUpdateRequest request = TravelRouteFixture.createRouteUpdateRequest(0, routeIds);

        // when, then
        mockMvc.perform(patch("/api/schedules/{scheduleId}/routes", schedule.getScheduleId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.ROUTE_MISMATCH.getMessage()));

    }


    @Test
    @DisplayName("여행 루트 수정 시 여행 루트 버전 충돌로 409 반환")
    void updateTravelRoute_conflictRouteVersion() throws Exception{
        // given
        TravelRoute route1 = travelRouteRepository.save(TravelRouteFixture.createRoute(schedule, place1WithThumb, 1));
        TravelRoute route2 = travelRouteRepository.save(TravelRouteFixture.createRoute(schedule, place1WithThumb, 2));
        TravelRoute route3 = travelRouteRepository.save(TravelRouteFixture.createRoute(schedule, place2WithThumb, 3));

        List<Long> routeIds = new ArrayList<>(List.of(route3.getRouteId(), route2.getRouteId(), route1.getRouteId()));
        RouteUpdateRequest request = TravelRouteFixture.createRouteUpdateRequest(
                schedule.getRouteVersion()+1,
                routeIds
        );

        // when, then
        mockMvc.perform(patch("/api/schedules/{scheduleId}/routes", schedule.getScheduleId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.CONFLICT_ROUTE_VERSION.getMessage()));

    }


    @Test
    @DisplayName("여행 루트 삭제 - 첫번째 위치한 여행루트인 경우")
    void deleteTravelRoute_first() throws Exception{
        // given
        Integer routeVersion = schedule.getRouteVersion();

        TravelRoute route1 = travelRouteRepository.save(TravelRouteFixture.createRoute(schedule, place1WithThumb, 1));
        TravelRoute route2 = travelRouteRepository.save(TravelRouteFixture.createRoute(schedule, place2WithThumb, 2));
        TravelRoute route3 = travelRouteRepository.save(TravelRouteFixture.createRoute(schedule, place3WithoutThumb, 3));

        // when, then
        mockMvc.perform(delete("/api/schedules/{scheduleId}/routes/{routeId}"
                        , schedule.getScheduleId(), route1.getRouteId())
                        .param("routeVersion", String.valueOf(routeVersion)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(SuccessCode.GENERAL_SUCCESS.getMessage()))
                .andExpect(jsonPath("$.data.routeVersion").value(routeVersion+1));

        List<TravelRoute> routes = travelRouteRepository.findAllByTravelSchedule_ScheduleId(schedule.getScheduleId());
        assertThat(routes).hasSize(2);
        assertThat(routes).doesNotContain(route1);
        assertThat(routes)
                .extracting(
                        TravelRoute::getRouteId,
                        TravelRoute::getRouteOrder
                )
                .containsExactly(
                        tuple(route2.getRouteId(), 1),
                        tuple(route3.getRouteId(), 2)
                );

    }


    @Test
    @DisplayName("여행 루트 삭제 - 중간에 위치한 여행루트인 경우")
    void deleteTravelRoute_middle() throws Exception{
        // given
        Integer routeVersion = schedule.getRouteVersion();

        TravelRoute route1 = travelRouteRepository.save(TravelRouteFixture.createRoute(schedule, place1WithThumb, 1));
        TravelRoute route2 = travelRouteRepository.save(TravelRouteFixture.createRoute(schedule, place2WithThumb, 2));
        TravelRoute route3 = travelRouteRepository.save(TravelRouteFixture.createRoute(schedule, place3WithoutThumb, 3));

        // when, then
        mockMvc.perform(delete("/api/schedules/{scheduleId}/routes/{routeId}"
                        , schedule.getScheduleId(), route2.getRouteId())
                        .param("routeVersion", String.valueOf(routeVersion)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(SuccessCode.GENERAL_SUCCESS.getMessage()))
                .andExpect(jsonPath("$.data.routeVersion").value(routeVersion+1));

        List<TravelRoute> routes = travelRouteRepository.findAllByTravelSchedule_ScheduleId(schedule.getScheduleId());
        assertThat(routes).hasSize(2);
        assertThat(routes).doesNotContain(route2);
        assertThat(routes)
                .extracting(
                        TravelRoute::getRouteId,
                        TravelRoute::getRouteOrder
                )
                .containsExactly(
                        tuple(route1.getRouteId(), 1),
                        tuple(route3.getRouteId(), 2)
                );

    }


    @Test
    @DisplayName("여행 루트 삭제 - 마지막에 위치한 여행루트인 경우")
    void deleteTravelRoute_last() throws Exception{
        // given
        Integer routeVersion = schedule.getRouteVersion();

        TravelRoute route1 = travelRouteRepository.save(TravelRouteFixture.createRoute(schedule, place1WithThumb, 1));
        TravelRoute route2 = travelRouteRepository.save(TravelRouteFixture.createRoute(schedule, place2WithThumb, 2));
        TravelRoute route3 = travelRouteRepository.save(TravelRouteFixture.createRoute(schedule, place3WithoutThumb, 3));

        // when, then
        mockMvc.perform(delete("/api/schedules/{scheduleId}/routes/{routeId}"
                        , schedule.getScheduleId(), route3.getRouteId())
                        .param("routeVersion", String.valueOf(routeVersion)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(SuccessCode.GENERAL_SUCCESS.getMessage()))
                .andExpect(jsonPath("$.data.routeVersion").value(routeVersion+1));

        List<TravelRoute> routes = travelRouteRepository.findAllByTravelSchedule_ScheduleId(schedule.getScheduleId());
        assertThat(routes).hasSize(2);
        assertThat(routes).doesNotContain(route3);
        assertThat(routes)
                .extracting(
                        TravelRoute::getRouteId,
                        TravelRoute::getRouteOrder
                )
                .containsExactly(
                        tuple(route1.getRouteId(), 1),
                        tuple(route2.getRouteId(), 2)
                );

    }



    @Test
    @DisplayName("여행 루트 삭제 - 저장된 여행루트 데이터가 1개인 경우")
    void deleteTravelRoute_one() throws Exception{
        // given
        Integer routeVersion = schedule.getRouteVersion();

        TravelRoute route = travelRouteRepository.save(TravelRouteFixture.createRoute(schedule, place1WithThumb, 1));

        // when, then
        mockMvc.perform(delete("/api/schedules/{scheduleId}/routes/{routeId}"
                        , schedule.getScheduleId(), route.getRouteId())
                        .param("routeVersion", String.valueOf(routeVersion)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(SuccessCode.GENERAL_SUCCESS.getMessage()))
                .andExpect(jsonPath("$.data.routeVersion").value(routeVersion+1));

        List<TravelRoute> routes = travelRouteRepository.findAllByTravelSchedule_ScheduleId(schedule.getScheduleId());
        assertThat(routes).hasSize(0);
    }



    @Test
    @DisplayName("여행 루트 삭제 시 여행 루트 버전 충돌로 예외 발생")
    void deleteTravelRoute_conflictRouteVersion() throws Exception{
        // given
        Integer routeVersion = schedule.getRouteVersion()+1;

        // when, then
        mockMvc.perform(delete("/api/schedules/{scheduleId}/routes/{routeId}"
                        , schedule.getScheduleId(), 1L)
                        .param("routeVersion", String.valueOf(routeVersion)))
                .andDo(print())
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.CONFLICT_ROUTE_VERSION.getMessage()));
    }

}
