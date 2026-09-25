package com.triptune.schedule.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.triptune.common.entity.ApiContentType;
import com.triptune.common.entity.City;
import com.triptune.common.entity.Country;
import com.triptune.common.entity.District;
import com.triptune.common.fixture.ApiContentTypeFixture;
import com.triptune.common.fixture.CityFixture;
import com.triptune.common.fixture.CountryFixture;
import com.triptune.common.fixture.DistrictFixture;
import com.triptune.global.exception.DataNotFoundException;
import com.triptune.global.message.ErrorCode;
import com.triptune.global.message.SuccessCode;
import com.triptune.global.security.SecurityTestUtils;
import com.triptune.global.security.jwt.JwtAuthFilter;
import com.triptune.global.util.PageUtils;
import com.triptune.member.entity.Member;
import com.triptune.member.fixture.MemberFixture;
import com.triptune.profile.entity.ProfileImage;
import com.triptune.profile.fixture.ProfileImageFixture;
import com.triptune.schedule.dto.request.RouteCreateRequest;
import com.triptune.schedule.dto.response.RouteResponse;
import com.triptune.schedule.exception.ForbiddenAttendeeException;
import com.triptune.schedule.fixture.TravelRouteFixture;
import com.triptune.schedule.service.TravelRouteService;
import com.triptune.travel.entity.TravelPlace;
import com.triptune.travel.fixture.TravelPlaceFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static com.triptune.travel.enums.ThemeType.ATTRACTIONS;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TravelRouteController.class)
@AutoConfigureMockMvc(addFilters = false)
public class TravelRouteControllerTest {

    private static final Long SCHEDULE_ID = 1L;

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private JwtAuthFilter jwtAuthFilter;
    @MockBean private TravelRouteService travelRouteService;

    private TravelPlace place1;
    private TravelPlace place2;
    private TravelPlace place3;

    private Member member1;
    private Member member2;


    @BeforeEach
    void setUp(){
        Country country = CountryFixture.createCountry();
        City city = CityFixture.createSeoul(country);
        District district = DistrictFixture.createDistrict(city, "강남구");
        ApiContentType apiContentType = ApiContentTypeFixture.createApiContentType(ATTRACTIONS);

        member1 = MemberFixture.createNativeTypeMemberWithId(
                1L,
                "member1@email.com",
                ProfileImageFixture.createProfileImage("test1")
        );
        member2 = MemberFixture.createNativeTypeMemberWithId(
                2L,
                "member2@email.com",
                ProfileImageFixture.createProfileImage("test2")
        );

        place1 = TravelPlaceFixture.createTravelPlaceWithId(
                1L,
                country,
                city,
                district,
                apiContentType,
                "여행지1"
        );
        place2 = TravelPlaceFixture.createTravelPlaceWithId(
                2L,
                country,
                city,
                district,
                apiContentType,
                "여행지2"
        );
        place3 = TravelPlaceFixture.createTravelPlaceWithId(
                3L,
                country,
                city,
                district,
                apiContentType,
                "여행지3"
        );
    }


    @Test
    @DisplayName("여행 루트 조회 성공")
    void getRoutes() throws Exception {
        // given
        SecurityTestUtils.mockAuthentication(member1);

        List<RouteResponse> routeResponses = List.of(
                TravelRouteFixture.createRouteResponse(1, place1, "place1ThumbnailUrl"),
                TravelRouteFixture.createRouteResponse(2, place1, "place1ThumbnailUrl"),
                TravelRouteFixture.createRouteResponse(3, place2, "place2ThumbnailUrl")
        );
        Page<RouteResponse> response = PageUtils.createPage(
                routeResponses,
                PageUtils.defaultPageable(1),
                routeResponses.size()
        );

        given(travelRouteService.getRoutes(anyLong(), anyInt()))
                .willReturn(response);

        // when, then
        mockMvc.perform(get("/api/schedules/{scheduleId}/routes", SCHEDULE_ID)
                        .param("page", "1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalElements").value(3))
                .andExpect(jsonPath("$.data.content[0].routeOrder").value(1))
                .andExpect(jsonPath("$.data.content[0].placeName").value(place1.getPlaceName()))
                .andExpect(jsonPath("$.data.content[1].routeOrder").value(2))
                .andExpect(jsonPath("$.data.content[1].placeName").value(place1.getPlaceName()))
                .andExpect(jsonPath("$.data.content[2].routeOrder").value(3))
                .andExpect(jsonPath("$.data.content[2].placeName").value(place2.getPlaceName()));
    }


    @Test
    @DisplayName("여행 루트의 마지막 여행지 추가")
    void createLastRoute() throws Exception{
        // given
        SecurityTestUtils.mockAuthentication(member1);

        RouteCreateRequest request = TravelRouteFixture.createRouteCreateRequest(place3.getPlaceId());

        // when, then
        mockMvc.perform(post("/api/schedules/{scheduleId}/routes", SCHEDULE_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(SuccessCode.GENERAL_SUCCESS.getMessage()));
    }


    @Test
    @DisplayName("여행 루트의 마지막 여행지 추가 시 여행지 ID null 값이 들어와 400 반환")
    void createLastRoute_invalidNullPlaceId() throws Exception {
        // given
        SecurityTestUtils.mockAuthentication(member1);
        RouteCreateRequest request = TravelRouteFixture.createRouteCreateRequest(null);

        // when, then
        mockMvc.perform(post("/api/schedules/{scheduleId}/routes", SCHEDULE_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("여행지 ID는 필수 입력 값입니다.")));
    }


    @ParameterizedTest
    @DisplayName("여행 루트의 마지막 여행지 추가 시 여행지 ID에 1 미만 값이 들어와 400 반환")
    @ValueSource(longs = {0L, -1L})
    void createLastRoute_invalidMinPlaceId(Long input) throws Exception {
        // given
        SecurityTestUtils.mockAuthentication(member1);
        RouteCreateRequest request = TravelRouteFixture.createRouteCreateRequest(input);

        // when, then
        mockMvc.perform(post("/api/schedules/{scheduleId}/routes", SCHEDULE_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("여행지 ID는 1 이상의 값이어야 합니다.")));
    }


    @Test
    @DisplayName("여행 루트의 마지막 여행지 추가 시 일정 데이터 존재하지 않아 404 반환")
    void createLastRoute_scheduleNotFound() throws Exception {
        // given
        SecurityTestUtils.mockAuthentication(member1);
        RouteCreateRequest request = TravelRouteFixture.createRouteCreateRequest(place3.getPlaceId());

        willThrow(new DataNotFoundException(ErrorCode.SCHEDULE_NOT_FOUND))
                .given(travelRouteService).createLastRoute(anyLong(), anyLong(), any(RouteCreateRequest.class));

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
    @DisplayName("여행 루트의 마지막 여행지 추가 시 참석자 데이터 존재하지 않아 404 반환")
    void createLastRoute_attendeeNotFound() throws Exception {
        // given
        ProfileImage profileImage = ProfileImageFixture.createProfileImage("memberImage");
        Member member = MemberFixture.createNativeTypeMemberWithId(1000L, "notMember@email.com", profileImage);
        SecurityTestUtils.mockAuthentication(member);

        RouteCreateRequest request = TravelRouteFixture.createRouteCreateRequest(place3.getPlaceId());

        willThrow(new DataNotFoundException(ErrorCode.ATTENDEE_NOT_FOUND))
                .given(travelRouteService).createLastRoute(anyLong(), anyLong(), any(RouteCreateRequest.class));

        // when, then
        mockMvc.perform(post("/api/schedules/{scheduleId}/routes", SCHEDULE_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.ATTENDEE_NOT_FOUND.getMessage()));
    }


    @Test
    @DisplayName("여행 루트의 마지막 여행지 추가 시 편집 권한이 없어서 403 반환")
    void createLastRoute_forbiddenEdit() throws Exception{
        // given
        SecurityTestUtils.mockAuthentication(member2);
        RouteCreateRequest request = TravelRouteFixture.createRouteCreateRequest(place3.getPlaceId());

        willThrow(new ForbiddenAttendeeException(ErrorCode.FORBIDDEN_EDIT_SCHEDULE))
                .given(travelRouteService).createLastRoute(anyLong(), anyLong(), any(RouteCreateRequest.class));

        // when, then
        mockMvc.perform(post("/api/schedules/{scheduleId}/routes", SCHEDULE_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.FORBIDDEN_EDIT_SCHEDULE.getMessage()));

    }

    @Test
    @DisplayName("여행 루트의 마지막 여행지 추가 시 여행지 데이터 존재하지 않아 404 반환")
    void createLastRoute_placeNotFound() throws Exception {
        // given
        SecurityTestUtils.mockAuthentication(member1);
        RouteCreateRequest request = TravelRouteFixture.createRouteCreateRequest(1000L);

        willThrow(new DataNotFoundException(ErrorCode.PLACE_NOT_FOUND))
                .given(travelRouteService).createLastRoute(anyLong(), anyLong(), any(RouteCreateRequest.class));

        // when, then
        mockMvc.perform(post("/api/schedules/{scheduleId}/routes", SCHEDULE_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.PLACE_NOT_FOUND.getMessage()));
    }


}
