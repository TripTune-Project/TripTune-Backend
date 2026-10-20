package com.triptune.route.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.triptune.common.entity.ApiContentType;
import com.triptune.common.entity.City;
import com.triptune.common.entity.Country;
import com.triptune.common.entity.District;
import com.triptune.common.fixture.*;
import com.triptune.global.exception.DataNotFoundException;
import com.triptune.global.message.ErrorCode;
import com.triptune.global.message.SuccessCode;
import com.triptune.global.response.page.RoutePageResponse;
import com.triptune.global.security.SecurityTestUtils;
import com.triptune.global.security.jwt.JwtAuthFilter;
import com.triptune.global.util.PageUtils;
import com.triptune.member.entity.Member;
import com.triptune.member.fixture.MemberFixture;
import com.triptune.profile.entity.ProfileImage;
import com.triptune.profile.fixture.ProfileImageFixture;
import com.triptune.route.dto.request.RouteCreateRequest;
import com.triptune.route.dto.request.RouteUpdateRequest;
import com.triptune.route.dto.response.RouteVersionResponse;
import com.triptune.route.dto.response.RouteResponse;
import com.triptune.attendee.exception.ForbiddenAttendeeException;
import com.triptune.route.entity.TravelRoute;
import com.triptune.route.fixture.TravelRouteFixture;
import com.triptune.route.service.TravelRouteService;
import com.triptune.schedule.entity.TravelSchedule;
import com.triptune.schedule.fixture.TravelScheduleFixture;
import com.triptune.travel.entity.TravelImage;
import com.triptune.travel.entity.TravelPlace;
import com.triptune.travel.fixture.TravelImageFixture;
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

import java.util.ArrayList;
import java.util.List;

import static com.triptune.travel.enums.ThemeType.ATTRACTIONS;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TravelRouteController.class)
@AutoConfigureMockMvc(addFilters = false)
public class TravelRouteControllerTest {


    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private JwtAuthFilter jwtAuthFilter;
    @MockBean private TravelRouteService travelRouteService;

    private TravelPlace place1WithThumb;
    private TravelPlace place2WithThumb;
    private TravelPlace place3WithoutThumb;

    private String place1ThumbUrl;
    private String place2ThumbUrl;

    private Member member1;
    private Member member2;

    private TravelSchedule schedule;

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

        place1WithThumb = TravelPlaceFixture.createTravelPlaceWithId(
                1L,
                country,
                city,
                district,
                apiContentType,
                "여행지1"
        );
        TravelImage place1Thumb = TravelImageFixture.createTravelImage(place1WithThumb, "test1", true);
        place1ThumbUrl = S3Fixture.createS3ObjectUrl(place1Thumb.getS3ObjectKey());

        place2WithThumb = TravelPlaceFixture.createTravelPlaceWithId(
                2L,
                country,
                city,
                district,
                apiContentType,
                "여행지2"
        );
        TravelImage place2Thumb = TravelImageFixture.createTravelImage(place2WithThumb, "test2", true);
        place2ThumbUrl = S3Fixture.createS3ObjectUrl(place2Thumb.getS3ObjectKey());

        place3WithoutThumb = TravelPlaceFixture.createTravelPlaceWithId(
                3L,
                country,
                city,
                district,
                apiContentType,
                "여행지3"
        );

        schedule = TravelScheduleFixture.createScheduleWithIdAndVersion(
                1L,
                "테스트1",
                1,
                2
        );
    }


    @Test
    @DisplayName("여행 루트 조회")
    void getRoutes() throws Exception {
        // given
        SecurityTestUtils.mockAuthentication(member1);
        int routeVersion = 3;

        List<RouteResponse> routeResponses = List.of(
                TravelRouteFixture.createRouteResponse(1, place1WithThumb, place1ThumbUrl),
                TravelRouteFixture.createRouteResponse(2, place2WithThumb, place2ThumbUrl),
                TravelRouteFixture.createRouteResponse(3, place3WithoutThumb, null)
        );

        Page<RouteResponse> pageResponse = PageUtils.createPage(
                routeResponses,
                PageUtils.defaultPageable(1),
                routeResponses.size()
        );

        RoutePageResponse<RouteResponse> response = RoutePageResponse.of(
                routeVersion,
                pageResponse
        );

        given(travelRouteService.getTravelRoutes(anyLong(), anyInt()))
                .willReturn(response);

        // when, then
        mockMvc.perform(get("/api/schedules/{scheduleId}/routes", schedule.getScheduleId())
                        .param("page", "1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.routeVersion").value(routeVersion))
                .andExpect(jsonPath("$.data.totalElements").value(3))
                .andExpect(jsonPath("$.data.content[0].routeOrder").value(1))
                .andExpect(jsonPath("$.data.content[0].placeName").value(place1WithThumb.getPlaceName()))
                .andExpect(jsonPath("$.data.content[0].thumbnailUrl").value(place1ThumbUrl))
                .andExpect(jsonPath("$.data.content[1].routeOrder").value(2))
                .andExpect(jsonPath("$.data.content[1].placeName").value(place2WithThumb.getPlaceName()))
                .andExpect(jsonPath("$.data.content[1].thumbnailUrl").value(place2ThumbUrl))
                .andExpect(jsonPath("$.data.content[2].routeOrder").value(3))
                .andExpect(jsonPath("$.data.content[2].placeName").value(place3WithoutThumb.getPlaceName()))
                .andExpect(jsonPath("$.data.content[2].thumbnailUrl", nullValue()));
    }


    @Test
    @DisplayName("여행 루트의 마지막 여행지 추가")
    void createLastTravelRoute() throws Exception{
        // given
        SecurityTestUtils.mockAuthentication(member1);

        int routeVersion = 3;

        RouteCreateRequest request = TravelRouteFixture.createRouteCreateRequest(
                place1WithThumb.getPlaceId(),
                routeVersion
        );

        RouteVersionResponse response = TravelRouteFixture.createRouteCreateResponse(routeVersion+1);

        given(travelRouteService.createLastTravelRoute(eq(schedule.getScheduleId()), eq(member1.getMemberId()), any(RouteCreateRequest.class)))
                .willReturn(response);


        // when, then
        mockMvc.perform(post("/api/schedules/{scheduleId}/routes", schedule.getScheduleId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(SuccessCode.GENERAL_SUCCESS.getMessage()))
                .andExpect(jsonPath("$.data.routeVersion").value(routeVersion+1));
    }


    @Test
    @DisplayName("여행 루트의 마지막 여행지 추가 시 여행 루트 버전에 null 값이 들어와 400 반환")
    void createLastTravelRoute_invalidNullRouteVersion() throws Exception {
        // given
        SecurityTestUtils.mockAuthentication(member1);
        RouteCreateRequest request = TravelRouteFixture.createRouteCreateRequest(null, 3);

        // when, then
        mockMvc.perform(post("/api/schedules/{scheduleId}/routes", schedule.getScheduleId())
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
    void createLastTravelRoute_invalidMinPlaceId(Long input) throws Exception {
        // given
        SecurityTestUtils.mockAuthentication(member1);
        RouteCreateRequest request = TravelRouteFixture.createRouteCreateRequest(input, 3);

        // when, then
        mockMvc.perform(post("/api/schedules/{scheduleId}/routes", schedule.getScheduleId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("여행지 ID는 1 이상의 값이어야 합니다.")));
    }



    @Test
    @DisplayName("여행 루트의 마지막 여행지 추가 시 여행지 ID null 값이 들어와 400 반환")
    void createLastTravelRoute_invalidNullPlaceId() throws Exception {
        // given
        SecurityTestUtils.mockAuthentication(member1);
        RouteCreateRequest request = TravelRouteFixture.createRouteCreateRequest(place3WithoutThumb.getPlaceId(), null);

        // when, then
        mockMvc.perform(post("/api/schedules/{scheduleId}/routes", schedule.getScheduleId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("여행 루트 버전은 필수 입력 값입니다.")));
    }


    @Test
    @DisplayName("여행 루트의 마지막 여행지 추가 시 여행지 ID에 0 미만 값이 들어와 400 반환")
    void createLastTravelRoute_invalidMinRouteVersion() throws Exception {
        // given
        SecurityTestUtils.mockAuthentication(member1);
        RouteCreateRequest request = TravelRouteFixture.createRouteCreateRequest(place3WithoutThumb.getPlaceId(), -1);

        // when, then
        mockMvc.perform(post("/api/schedules/{scheduleId}/routes", schedule.getScheduleId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("여행 루트 버전은 0 이상의 값이어야 합니다.")));
    }



    @Test
    @DisplayName("여행 루트의 마지막 여행지 추가 시 일정 데이터 존재하지 않아 404 반환")
    void createLastTravelRoute_scheduleNotFound() throws Exception {
        // given
        SecurityTestUtils.mockAuthentication(member1);
        RouteCreateRequest request = TravelRouteFixture.createRouteCreateRequest(place3WithoutThumb.getPlaceId(), 3);

        willThrow(new DataNotFoundException(ErrorCode.SCHEDULE_NOT_FOUND))
                .given(travelRouteService).createLastTravelRoute(anyLong(), anyLong(), any(RouteCreateRequest.class));

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
    void createLastTravelRoute_attendeeNotFound() throws Exception {
        // given
        ProfileImage profileImage = ProfileImageFixture.createProfileImage("memberImage");
        Member member = MemberFixture.createNativeTypeMemberWithId(1000L, "notMember@email.com", profileImage);
        SecurityTestUtils.mockAuthentication(member);

        RouteCreateRequest request = TravelRouteFixture.createRouteCreateRequest(place3WithoutThumb.getPlaceId(), 3);

        willThrow(new DataNotFoundException(ErrorCode.ATTENDEE_NOT_FOUND))
                .given(travelRouteService).createLastTravelRoute(anyLong(), anyLong(), any(RouteCreateRequest.class));

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
    @DisplayName("여행 루트의 마지막 여행지 추가 시 편집 권한이 없어서 403 반환")
    void createLastTravelRoute_forbiddenEdit() throws Exception{
        // given
        SecurityTestUtils.mockAuthentication(member2);
        RouteCreateRequest request = TravelRouteFixture.createRouteCreateRequest(place3WithoutThumb.getPlaceId(), 3);

        willThrow(new ForbiddenAttendeeException(ErrorCode.FORBIDDEN_EDIT_SCHEDULE))
                .given(travelRouteService).createLastTravelRoute(anyLong(), anyLong(), any(RouteCreateRequest.class));

        // when, then
        mockMvc.perform(post("/api/schedules/{scheduleId}/routes", schedule.getScheduleId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.FORBIDDEN_EDIT_SCHEDULE.getMessage()));

    }

    @Test
    @DisplayName("여행 루트의 마지막 여행지 추가 시 여행지 데이터 존재하지 않아 404 반환")
    void createLastTravelRoute_placeNotFound() throws Exception {
        // given
        SecurityTestUtils.mockAuthentication(member1);
        RouteCreateRequest request = TravelRouteFixture.createRouteCreateRequest(1000L, 3);

        willThrow(new DataNotFoundException(ErrorCode.PLACE_NOT_FOUND))
                .given(travelRouteService).createLastTravelRoute(anyLong(), anyLong(), any(RouteCreateRequest.class));

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
    @DisplayName("여행 루트 수정")
    void updateTravelRoute() throws Exception{
        // given
        SecurityTestUtils.mockAuthentication(member1);
        int routeVersion = 3;

        List<Long> routeIds = new ArrayList<>(List.of(3L, 2L, 1L));
        RouteUpdateRequest request = TravelRouteFixture.createRouteUpdateRequest(routeVersion, routeIds);

        RouteVersionResponse response = TravelRouteFixture.createRouteCreateResponse(routeVersion+1);

        given(travelRouteService.updateTravelRoute(eq(schedule.getScheduleId()), any(RouteUpdateRequest.class)))
                .willReturn(response);


        // when, then
        mockMvc.perform(patch("/api/schedules/{scheduleId}/routes", schedule.getScheduleId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(SuccessCode.GENERAL_SUCCESS.getMessage()))
                .andExpect(jsonPath("$.data.routeVersion").value(routeVersion+1));
    }


    @Test
    @DisplayName("여행 루트 수정 시 여행 루트 버전에 null 값이 들어와 400 반환")
    void updateTravelRoute_invalidNullRouteVersion() throws Exception {
        // given
        SecurityTestUtils.mockAuthentication(member1);

        List<Long> routeIds = new ArrayList<>(List.of(3L, 2L, 1L));
        RouteUpdateRequest request = TravelRouteFixture.createRouteUpdateRequest(null, routeIds);

        // when, then
        mockMvc.perform(patch("/api/schedules/{scheduleId}/routes", schedule.getScheduleId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("여행 루트 버전은 필수 입력 값입니다."));
    }


    @Test
    @DisplayName("여행 루트 수정 시 여행 루트 ID 리스트에 null 값이 들어와 400 반환")
    void updateTravelRoute_invalidNullOrderedRouteIds() throws Exception {
        // given
        SecurityTestUtils.mockAuthentication(member1);

        RouteUpdateRequest request = TravelRouteFixture.createRouteUpdateRequest(3, null);


        // when, then
        mockMvc.perform(patch("/api/schedules/{scheduleId}/routes", schedule.getScheduleId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("여행 루트 ID는 필수 입력 값입니다.")));
    }


    @Test
    @DisplayName("여행 루트 삭제")
    void deleteTravelRoute() throws Exception{
        // given
        SecurityTestUtils.mockAuthentication(member1);
        Integer routeVersion = schedule.getRouteVersion();

        TravelRoute route = TravelRouteFixture.createRouteWithId(
                1L,
                schedule,
                place1WithThumb,
                1
        );

        RouteVersionResponse response = TravelRouteFixture.createRouteCreateResponse(routeVersion+1);

        given(travelRouteService.deleteTravelRoute(eq(schedule.getScheduleId()), eq(route.getRouteId()), eq(routeVersion)))
                .willReturn(response);


        // when, then
        mockMvc.perform(delete("/api/schedules/{scheduleId}/routes/{routeId}"
                        , schedule.getScheduleId(), route.getRouteId())
                        .param("routeVersion", String.valueOf(routeVersion)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(SuccessCode.GENERAL_SUCCESS.getMessage()))
                .andExpect(jsonPath("$.data.routeVersion").value(routeVersion+1));
    }

    @Test
    @DisplayName("여행 루트 삭제 시 일정 데이터 존재하지 않아 예외 발생")
    void deleteTravelRoute_scheduleNotFound() throws Exception{
        // given
        SecurityTestUtils.mockAuthentication(member1);

        willThrow(new DataNotFoundException(ErrorCode.SCHEDULE_NOT_FOUND))
                .given(travelRouteService).deleteTravelRoute(eq(1004L), eq(1L), eq(1));

        // when, then
        mockMvc.perform(delete("/api/schedules/{scheduleId}/routes/{routeId}"
                        , 1004L, 1L)
                        .param("routeVersion", String.valueOf(1)))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.SCHEDULE_NOT_FOUND.getMessage()));
    }


    @Test
    @DisplayName("여행 루트 삭제 시 여행루트 데이터 존재하지 않아 예외 발생")
    void deleteTravelRoute_routeNotFound() throws Exception{
        // given
        SecurityTestUtils.mockAuthentication(member1);
        Integer routeVersion = schedule.getRouteVersion();

        willThrow(new DataNotFoundException(ErrorCode.SCHEDULE_NOT_FOUND))
                .given(travelRouteService).deleteTravelRoute(eq(schedule.getScheduleId()), eq(1004L), eq(routeVersion));

        // when, then
        mockMvc.perform(delete("/api/schedules/{scheduleId}/routes/{routeId}"
                        , schedule.getScheduleId(), 1004L)
                        .param("routeVersion", String.valueOf(routeVersion)))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.SCHEDULE_NOT_FOUND.getMessage()));
    }


}
