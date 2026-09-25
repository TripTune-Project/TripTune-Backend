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
import com.triptune.global.message.ErrorCode;
import com.triptune.global.message.SuccessCode;
import com.triptune.global.response.page.PageResponse;
import com.triptune.global.response.page.SchedulePageResponse;
import com.triptune.global.security.SecurityTestUtils;
import com.triptune.global.security.jwt.JwtAuthFilter;
import com.triptune.global.util.PageUtils;
import com.triptune.member.entity.Member;
import com.triptune.member.fixture.MemberFixture;
import com.triptune.profile.entity.ProfileImage;
import com.triptune.profile.fixture.ProfileImageFixture;
import com.triptune.schedule.dto.request.RouteRequest;
import com.triptune.schedule.dto.request.ScheduleCreateRequest;
import com.triptune.schedule.dto.request.ScheduleUpdateRequest;
import com.triptune.schedule.dto.response.OverviewScheduleResponse;
import com.triptune.schedule.dto.response.ScheduleCreateResponse;
import com.triptune.schedule.dto.response.ScheduleDetailResponse;
import com.triptune.schedule.dto.response.ScheduleInfoResponse;
import com.triptune.schedule.entity.TravelSchedule;
import com.triptune.schedule.enums.ScheduleSearchType;
import com.triptune.schedule.exception.ForbiddenAttendeeException;
import com.triptune.schedule.exception.ForbiddenScheduleException;
import com.triptune.schedule.fixture.TravelRouteFixture;
import com.triptune.schedule.fixture.TravelScheduleFixture;
import com.triptune.schedule.service.TravelScheduleService;
import com.triptune.travel.dto.response.PlaceResponse;
import com.triptune.travel.entity.TravelImage;
import com.triptune.travel.entity.TravelPlace;
import com.triptune.travel.fixture.TravelImageFixture;
import com.triptune.travel.fixture.TravelPlaceFixture;
import com.triptune.travel.service.TravelService;
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

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static com.triptune.schedule.enums.AttendeeRole.AUTHOR;
import static com.triptune.schedule.enums.AttendeeRole.GUEST;
import static com.triptune.travel.enums.ThemeType.ATTRACTIONS;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TravelScheduleController.class)
@AutoConfigureMockMvc(addFilters = false)
public class TravelScheduleControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private JwtAuthFilter jwtAuthFilter;
    @MockBean private TravelScheduleService travelScheduleService;
    @MockBean private TravelService travelService;


    private Member member1;
    private Member member2;

    private TravelPlace place1WithThumb;
    private TravelPlace place2WithThumb;
    private TravelPlace placeWithoutThumb;

    String place1ThumbUrl;
    String place2ThumbUrl;

    @BeforeEach
    void setUp(){
        ProfileImage profileImage1 = ProfileImageFixture.createProfileImage("member1Image");
        member1 = MemberFixture.createNativeTypeMemberWithId(1L, "member1@email.com", profileImage1);

        ProfileImage profileImage2 = ProfileImageFixture.createProfileImage("member2Image");
        member2 = MemberFixture.createNativeTypeMemberWithId(2L, "member2@email.com", profileImage2);

        Country country = CountryFixture.createCountry();
        City city = CityFixture.createSeoul(country);
        District gangnam = DistrictFixture.createDistrict(city, "강남구");
        District jungGu = DistrictFixture.createDistrict(city, "중구");
        ApiContentType apiContentType = ApiContentTypeFixture.createApiContentType(ATTRACTIONS);

        place1WithThumb = TravelPlaceFixture.createTravelPlaceWithId(
                1L,
                country,
                city,
                gangnam,
                apiContentType,
                "여행지1"
        );
        TravelImage place1Thumb = TravelImageFixture.createTravelImage(place1WithThumb, "test1", true);
        place1ThumbUrl = "http://test.com/" + place1Thumb.getS3ObjectKey();

        place2WithThumb = TravelPlaceFixture.createTravelPlaceWithId(
                2L,
                country,
                city,
                jungGu,
                apiContentType,
                "여행지2"
        );
        TravelImage place2Thumb = TravelImageFixture.createTravelImage(place2WithThumb, "test1", true);
        place2ThumbUrl = "http://test.com/" + place2Thumb.getS3ObjectKey();

        placeWithoutThumb = TravelPlaceFixture.createTravelPlaceWithId(
                3L,
                country,
                city,
                gangnam,
                apiContentType,
                "여행지3"
        );

    }

    @Test
    @DisplayName("전체 일정 목록 조회")
    void getAllSchedules() throws Exception {
        // given
        SecurityTestUtils.mockAuthentication(member1);

        TravelSchedule schedule1 = TravelScheduleFixture.createSchedule("테스트1");
        TravelSchedule schedule2 = TravelScheduleFixture.createSchedule("테스트2");
        TravelSchedule schedule3 = TravelScheduleFixture.createSchedule("테스트3");

        List<ScheduleInfoResponse> infoResponse = List.of(
                TravelScheduleFixture.createScheduleInfoResponse(schedule3, AUTHOR, "지금", null, member1),
                TravelScheduleFixture.createScheduleInfoResponse(schedule2, GUEST, "5분 전", null, member2),
                TravelScheduleFixture.createScheduleInfoResponse(schedule1, AUTHOR, "3시간 전", place2ThumbUrl, member1)
        );

        Page<ScheduleInfoResponse> page = PageUtils.createPage(
                infoResponse,
                PageUtils.schedulePageable(1),
                infoResponse.size()
        );
        SchedulePageResponse<ScheduleInfoResponse> response = SchedulePageResponse.of(page, 3, 2);

        given(travelScheduleService.getAllSchedules(anyInt(), anyLong()))
                .willReturn(response);


        // when, then
        mockMvc.perform(get("/api/schedules")
                        .param("page", "1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalElements").value(3))
                .andExpect(jsonPath("$.data.totalSharedElements").value(2))
                .andExpect(jsonPath("$.data.content[0].scheduleName").value(schedule3.getScheduleName()))
                .andExpect(jsonPath("$.data.content[0].author.nickname").value(member1.getNickname()))
                .andExpect(jsonPath("$.data.content[0].thumbnailUrl", nullValue()))
                .andExpect(jsonPath("$.data.content[1].scheduleName").value(schedule2.getScheduleName()))
                .andExpect(jsonPath("$.data.content[1].author.nickname").value(member2.getNickname()))
                .andExpect(jsonPath("$.data.content[1].thumbnailUrl", nullValue()))
                .andExpect(jsonPath("$.data.content[2].scheduleName").value(schedule1.getScheduleName()))
                .andExpect(jsonPath("$.data.content[2].author.nickname").value(member1.getNickname()))
                .andExpect(jsonPath("$.data.content[2].thumbnailUrl").value(place2ThumbUrl));
    }


    @Test
    @DisplayName("전체 일정 목록 조회 시 데이터 없는 경우")
    void getAllSchedules_emptyResult() throws Exception {
        // given
        SecurityTestUtils.mockAuthentication(member1);

        Page<ScheduleInfoResponse> page = PageUtils.createPage(
                Collections.emptyList(),
                PageUtils.schedulePageable(1),
                0
        );
        SchedulePageResponse<ScheduleInfoResponse> response = SchedulePageResponse.of(page, 0, 0);

        given(travelScheduleService.getAllSchedules(anyInt(), anyLong()))
                .willReturn(response);

        // when, then
        mockMvc.perform(get("/api/schedules")
                        .param("page", "1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalElements").value(0))
                .andExpect(jsonPath("$.data.totalSharedElements").value(0))
                .andExpect(jsonPath("$.data.content").isEmpty());
    }

    @Test
    @DisplayName("공유된 일정 목록 조회")
    void getSharedSchedules() throws Exception {
        // given
        SecurityTestUtils.mockAuthentication(member1);

        TravelSchedule schedule1 = TravelScheduleFixture.createSchedule("테스트1");
        TravelSchedule schedule2 = TravelScheduleFixture.createSchedule("테스트2");

        List<ScheduleInfoResponse> infoResponse = List.of(
                TravelScheduleFixture.createScheduleInfoResponse(schedule2, GUEST, "5분 전", null, member2),
                TravelScheduleFixture.createScheduleInfoResponse(schedule1, AUTHOR, "3시간 전", place2ThumbUrl, member1)
        );

        Page<ScheduleInfoResponse> page = PageUtils.createPage(
                infoResponse,
                PageUtils.schedulePageable(1),
                infoResponse.size()
        );
        SchedulePageResponse<ScheduleInfoResponse> response = SchedulePageResponse.of(page, 3, 2);

        given(travelScheduleService.getSharedSchedules(anyInt(), anyLong()))
                .willReturn(response);


        // when, then
        mockMvc.perform(get("/api/schedules/shared")
                        .param("page", "1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalElements").value(3))
                .andExpect(jsonPath("$.data.totalSharedElements").value(2))
                .andExpect(jsonPath("$.data.content[0].scheduleName").value(schedule2.getScheduleName()))
                .andExpect(jsonPath("$.data.content[0].author.nickname").value(member2.getNickname()))
                .andExpect(jsonPath("$.data.content[0].thumbnailUrl", nullValue()))
                .andExpect(jsonPath("$.data.content[1].scheduleName").value(schedule1.getScheduleName()))
                .andExpect(jsonPath("$.data.content[1].author.nickname").value(member1.getNickname()))
                .andExpect(jsonPath("$.data.content[1].thumbnailUrl").value(place2ThumbUrl));
    }


    @Test
    @DisplayName("공유된 일정 목록 조회 시 데이터 없는 경우")
    void getSharedSchedules_emptyResult() throws Exception {
        // given
        SecurityTestUtils.mockAuthentication(member1);

        Page<ScheduleInfoResponse> page = PageUtils.createPage(
                Collections.emptyList(),
                PageUtils.schedulePageable(1),
                0
        );
        SchedulePageResponse<ScheduleInfoResponse> response = SchedulePageResponse.of(page, 0, 0);

        given(travelScheduleService.getSharedSchedules(anyInt(), anyLong()))
                .willReturn(response);

        // when, then
        mockMvc.perform(get("/api/schedules/shared")
                        .param("page", "1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalElements").value(0))
                .andExpect(jsonPath("$.data.totalSharedElements").value(0))
                .andExpect(jsonPath("$.data.content").isEmpty());
    }


    @Test
    @DisplayName("수정 권한 있는 내 일정 조회")
    void getEnableEditSchedules() throws Exception {
        // given
        SecurityTestUtils.mockAuthentication(member1);

        TravelSchedule schedule1 = TravelScheduleFixture.createSchedule("테스트1");
        TravelSchedule schedule2 = TravelScheduleFixture.createSchedule("테스트2");

        List<OverviewScheduleResponse> overviewResponse = List.of(
                TravelScheduleFixture.createOverviewScheduleResponse(schedule2, member2.getNickname()),
                TravelScheduleFixture.createOverviewScheduleResponse(schedule1, member1.getNickname())
        );

        Page<OverviewScheduleResponse> response = PageUtils.createPage(
                overviewResponse,
                PageUtils.schedulePageable(1),
                overviewResponse.size()
        );

        given(travelScheduleService.getEnableEditSchedules(anyInt(), anyLong()))
                .willReturn(response);

        // when, then
        mockMvc.perform(get("/api/schedules/edit")
                        .param("page", "1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalElements").value(2))
                .andExpect(jsonPath("$.data.content[0].scheduleName").value(schedule2.getScheduleName()))
                .andExpect(jsonPath("$.data.content[0].author").value(member2.getNickname()))
                .andExpect(jsonPath("$.data.content[1].scheduleName").value(schedule1.getScheduleName()))
                .andExpect(jsonPath("$.data.content[1].author").value(member1.getNickname()));
    }

    @Test
    @DisplayName("수정 권한 있는 일정 조회 시 일정 데이터가 없는 경우")
    void getEnableEditSchedules_emptyResult() throws Exception {
        // given
        SecurityTestUtils.mockAuthentication(member1);

        Page<OverviewScheduleResponse> response = PageUtils.createPage(
                Collections.emptyList(),
                PageUtils.schedulePageable(1),
                0
        );

        given(travelScheduleService.getEnableEditSchedules(anyInt(), anyLong()))
                .willReturn(response);

        // when, then
        mockMvc.perform(get("/api/schedules/edit")
                        .param("page", "1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalElements").value(0))
                .andExpect(jsonPath("$.data.content").isEmpty());
    }


    @Test
    @DisplayName("전체 일정 검색")
    void searchSchedules() throws Exception {
        // given
        SecurityTestUtils.mockAuthentication(member1);

        TravelSchedule schedule = TravelScheduleFixture.createSchedule("테스트1");
        List<ScheduleInfoResponse> infoResponse = List.of(
                TravelScheduleFixture.createScheduleInfoResponse(schedule, AUTHOR, "3시간 전", place2ThumbUrl, member1)
        );

        Page<ScheduleInfoResponse> page = PageUtils.createPage(
                infoResponse,
                PageUtils.schedulePageable(1),
                infoResponse.size()
        );
        SchedulePageResponse<ScheduleInfoResponse> response = SchedulePageResponse.of(page, 3, 2);

        given(travelScheduleService.searchAllSchedules(anyInt(), eq("1"), anyLong()))
                .willReturn(response);

        // when, then
        mockMvc.perform(get("/api/schedules/search")
                        .param("page", "1")
                        .param("type", ScheduleSearchType.ALL.getValue())
                        .param("keyword", "1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalElements").value(3))
                .andExpect(jsonPath("$.data.totalSharedElements").value(2))
                .andExpect(jsonPath("$.data.content.length()").value(1))
                .andExpect(jsonPath("$.data.content[0].scheduleName").value(schedule.getScheduleName()))
                .andExpect(jsonPath("$.data.content[0].thumbnailUrl").value(place2ThumbUrl))
                .andExpect(jsonPath("$.data.content[0].author.nickname").value(member1.getNickname()))
                .andExpect(jsonPath("$.data.content[0].role").value(AUTHOR.name()));
    }

    @Test
    @DisplayName("전체 일정 검색 시 검색 결과 없는 경우")
    void searchSchedules_emptyResult() throws Exception {
        // given
        SecurityTestUtils.mockAuthentication(member1);

        Page<ScheduleInfoResponse> page = PageUtils.createPage(
                Collections.emptyList(),
                PageUtils.schedulePageable(1),
                0
        );
        SchedulePageResponse<ScheduleInfoResponse> response = SchedulePageResponse.of(page, 3, 2);

        given(travelScheduleService.searchAllSchedules(anyInt(), eq("ㅁㄴㅇㄹ"), anyLong()))
                .willReturn(response);


        // when, then
        mockMvc.perform(get("/api/schedules/search")
                        .param("page", "1")
                        .param("type", ScheduleSearchType.ALL.getValue())
                        .param("keyword", "ㅁㄴㅇㄹ"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalElements").value(3))
                .andExpect(jsonPath("$.data.totalSharedElements").value(2))
                .andExpect(jsonPath("$.data.content").isEmpty());
    }

    @Test
    @DisplayName("공유된 일정 검색")
    void searchSharedSchedules() throws Exception {
        // given
        SecurityTestUtils.mockAuthentication(member1);

        TravelSchedule schedule = TravelScheduleFixture.createSchedule("테스트1");
        List<ScheduleInfoResponse> infoResponse = List.of(
                TravelScheduleFixture.createScheduleInfoResponse(schedule, AUTHOR, "3시간 전", place2ThumbUrl, member1)
        );

        Page<ScheduleInfoResponse> page = PageUtils.createPage(
                infoResponse,
                PageUtils.schedulePageable(1),
                infoResponse.size()
        );
        SchedulePageResponse<ScheduleInfoResponse> response = SchedulePageResponse.of(page, 3, 2);

        given(travelScheduleService.searchSharedSchedules(anyInt(), eq("테스트"), anyLong()))
                .willReturn(response);

        // when, then
        mockMvc.perform(get("/api/schedules/search")
                        .param("page", "1")
                        .param("type", ScheduleSearchType.SHARE.getValue())
                        .param("keyword", "테스트"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalElements").value(3))
                .andExpect(jsonPath("$.data.totalSharedElements").value(2))
                .andExpect(jsonPath("$.data.content.length()").value(1))
                .andExpect(jsonPath("$.data.content[0].scheduleName").value(schedule.getScheduleName()))
                .andExpect(jsonPath("$.data.content[0].thumbnailUrl").value(place2ThumbUrl))
                .andExpect(jsonPath("$.data.content[0].author.nickname").value(member1.getNickname()))
                .andExpect(jsonPath("$.data.content[0].role").value(AUTHOR.name()));
    }

    @Test
    @DisplayName("공유된 일정 검색 시 검색 결과 없는 경우")
    void searchSharedSchedules_emptyResult() throws Exception {
        // given
        SecurityTestUtils.mockAuthentication(member1);

        Page<ScheduleInfoResponse> page = PageUtils.createPage(
                Collections.emptyList(),
                PageUtils.schedulePageable(1),
                0
        );
        SchedulePageResponse<ScheduleInfoResponse> response = SchedulePageResponse.of(page, 3, 2);

        given(travelScheduleService.searchSharedSchedules(anyInt(), eq("테스트3"), anyLong()))
                .willReturn(response);

        // when, then
        mockMvc.perform(get("/api/schedules/search")
                        .param("page", "1")
                        .param("type", ScheduleSearchType.SHARE.getValue())
                        .param("keyword", "테스트3"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalElements").value(3))
                .andExpect(jsonPath("$.data.totalSharedElements").value(2))
                .andExpect(jsonPath("$.data.content").isEmpty());
    }

    @Test
    @DisplayName("전체, 공유된 일정 검색 시 검색 타입이 없어 예외 발생")
    void searchSchedules_illegalSearchType() throws Exception {
        // given
        SecurityTestUtils.mockAuthentication(member1);

        // when, then
        mockMvc.perform(get("/api/schedules/search")
                        .param("page", "1")
                        .param("type", "not")
                        .param("keyword", "ㅁㄴㅇㄹ"))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.ILLEGAL_SCHEDULE_SEARCH_TYPE.getMessage()));
    }

    @Test
    @DisplayName("일정 생성")
    void createSchedule() throws Exception{
        // given
        SecurityTestUtils.mockAuthentication(member1);

        ScheduleCreateRequest request = TravelScheduleFixture.createScheduleRequest(
                "테스트",
                LocalDate.now(),
                LocalDate.now()
        );
        ScheduleCreateResponse response = TravelScheduleFixture.createScheduleCreateResponse(1L);

        given(travelScheduleService.createSchedule(any(ScheduleCreateRequest.class), anyLong()))
                .willReturn(response);

        // when, then
        mockMvc.perform(post("/api/schedules")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(SuccessCode.GENERAL_SUCCESS.getMessage()));
    }

    @ParameterizedTest
    @DisplayName("일정 생성 시 일정명 빈 값으로 예외 발생")
    @ValueSource(strings = {"", " "})
    void createSchedule_invalidNotBlankScheduleName(String input) throws Exception {
        // given
        SecurityTestUtils.mockAuthentication(member1);

        ScheduleCreateRequest request = TravelScheduleFixture.createScheduleRequest(
                input,
                LocalDate.now(),
                LocalDate.now().plusDays(10)
        );

        // when, then
        mockMvc.perform(post("/api/schedules")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("일정 이름은 필수 입력 값입니다.")));
    }

    @Test
    @DisplayName("일정 생성 시 일정명 null 값이 들어와 예외 발생")
    void createSchedule_invalidNullScheduleName() throws Exception {
        // given
        SecurityTestUtils.mockAuthentication(member1);

        ScheduleCreateRequest request = TravelScheduleFixture.createScheduleRequest(
                null,
                LocalDate.now(),
                LocalDate.now().plusDays(10)
        );

        // when, then
        mockMvc.perform(post("/api/schedules")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("일정 이름은 필수 입력 값입니다.")));
    }

    @Test
    @DisplayName("일정 생성 시 일정 시작일 null 값이 들어와 예외 발생")
    void createSchedule_invalidNullStartDate() throws Exception {
        // given
        SecurityTestUtils.mockAuthentication(member1);

        ScheduleCreateRequest request = TravelScheduleFixture.createScheduleRequest(
                "테스트",
                null,
                LocalDate.now().plusDays(10)
        );

        // when, then
        mockMvc.perform(post("/api/schedules")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("일정 시작 날짜는 필수 입력 값입니다.")));
    }

    @Test
    @DisplayName("일정 생성 시 일정 종료일 null 값이 들어와 예외 발생")
    void createSchedule_invalidNullEndDate() throws Exception {
        // given
        SecurityTestUtils.mockAuthentication(member1);

        ScheduleCreateRequest request = TravelScheduleFixture.createScheduleRequest(
                "테스트",
                LocalDate.now(),
                null
        );

        // when, then
        mockMvc.perform(post("/api/schedules")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("일정 종료 날짜는 필수 입력 값입니다.")));
    }


    @Test
    @DisplayName("일정 생성 시 일정 생성일이 종료일 이후 날짜로 예외 발생")
    void createSchedule_invalidStartDateAfterEndDate() throws Exception {
        // given
        SecurityTestUtils.mockAuthentication(member1);

        ScheduleCreateRequest request = TravelScheduleFixture.createScheduleRequest(
                "테스트",
                LocalDate.now().plusDays(10),
                LocalDate.now()
        );

        // when, then
        mockMvc.perform(post("/api/schedules")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("시작일은 종료일보다 이전이어야 합니다.")));
    }

    @Test
    @DisplayName("일정 상세 조회")
    void getScheduleDetail() throws Exception {
        // given
        SecurityTestUtils.mockAuthentication(member1);

        TravelSchedule schedule = TravelScheduleFixture.createScheduleWithId(1L, "테스트1");

        List<PlaceResponse> placeResponses = List.of(
                TravelPlaceFixture.createPlaceResponse(place2WithThumb, place2ThumbUrl),
                TravelPlaceFixture.createPlaceResponse(placeWithoutThumb, null),
                TravelPlaceFixture.createPlaceResponse(place1WithThumb, place1ThumbUrl)
        );
        Page<PlaceResponse> page = PageUtils.createPage(
                placeResponses,
                PageUtils.defaultPageable(1),
                placeResponses.size()
        );

        PageResponse<PlaceResponse> pageResponse = PageResponse.of(page);
        ScheduleDetailResponse response = TravelScheduleFixture.createScheduleDetailResponse(schedule, pageResponse);

        given(travelScheduleService.getScheduleDetail(anyLong(), anyInt()))
                .willReturn(response);

        // when, then
        mockMvc.perform(get("/api/schedules/{scheduleId}", schedule.getScheduleId())
                        .param("page", "1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.scheduleName").value(schedule.getScheduleName()))
                .andExpect(jsonPath("$.data.placeList.totalElements").value(3))
                .andExpect(jsonPath("$.data.placeList.content[0].placeName").value(place2WithThumb.getPlaceName()))
                .andExpect(jsonPath("$.data.placeList.content[0].thumbnailUrl").value(place2ThumbUrl))
                .andExpect(jsonPath("$.data.placeList.content[1].placeName").value(placeWithoutThumb.getPlaceName()))
                .andExpect(jsonPath("$.data.placeList.content[1].thumbnailUrl", nullValue()))
                .andExpect(jsonPath("$.data.placeList.content[2].placeName").value(place1WithThumb.getPlaceName()))
                .andExpect(jsonPath("$.data.placeList.content[2].thumbnailUrl").value(place1ThumbUrl));
    }

    @Test
    @DisplayName("일정 상세 조회 시 여행지 데이터 존재하지 않는 경우")
    void getScheduleDetail_emptyPlaces() throws Exception {
        // given
        SecurityTestUtils.mockAuthentication(member1);

        TravelSchedule schedule = TravelScheduleFixture.createScheduleWithId(1L, "테스트1");

        Page<PlaceResponse> page = PageUtils.createPage(
                Collections.emptyList(),
                PageUtils.defaultPageable(1),
                0
        );
        PageResponse<PlaceResponse> pageResponse = PageResponse.of(page);
        ScheduleDetailResponse response = TravelScheduleFixture.createScheduleDetailResponse(schedule, pageResponse);

        given(travelScheduleService.getScheduleDetail(anyLong(), anyInt()))
                .willReturn(response);


        // when, then
        mockMvc.perform(get("/api/schedules/{scheduleId}", schedule.getScheduleId())
                        .param("page", "1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.scheduleName").value(schedule.getScheduleName()))
                .andExpect(jsonPath("$.data.placeList.totalElements").value(0))
                .andExpect(jsonPath("$.data.placeList.content").isEmpty());
    }


    @Test
    @DisplayName("일정 수정")
    void updateSchedule() throws Exception {
        // given
        SecurityTestUtils.mockAuthentication(member1);

        RouteRequest routeRequest1 = TravelRouteFixture.createRouteRequest(1, place2WithThumb.getPlaceId());
        RouteRequest routeRequest2 = TravelRouteFixture.createRouteRequest(2, placeWithoutThumb.getPlaceId());

        ScheduleUpdateRequest request = TravelScheduleFixture.createUpdateScheduleRequest(
                "수정 테스트",
                LocalDate.now().minusDays(2),
                LocalDate.now().minusDays(1),
                routeRequest1,
                routeRequest2
        );

        // when, then
        mockMvc.perform(patch("/api/schedules/{scheduleId}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(SuccessCode.GENERAL_SUCCESS.getMessage()));
    }

    @ParameterizedTest
    @DisplayName("일정 수정 시 일정명 빈 값으로 예외 발생")
    @ValueSource(strings = {"", " "})
    void updateSchedule_invalidNotBlankScheduleName(String input) throws Exception {
        // given
        SecurityTestUtils.mockAuthentication(member1);

        ScheduleUpdateRequest request = TravelScheduleFixture.createUpdateScheduleRequest(
                input,
                LocalDate.now(),
                LocalDate.now().plusDays(1),
                TravelRouteFixture.createRouteRequest(1, place1WithThumb.getPlaceId()),
                TravelRouteFixture.createRouteRequest(2, placeWithoutThumb.getPlaceId())
        );

        // when, then
        mockMvc.perform(patch("/api/schedules/{scheduleId}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("일정 이름은 필수 입력 값입니다.")));
    }

    @Test
    @DisplayName("일정 수정 시 일정명 null 값이 들어와 예외 발생")
    void updateSchedule_invalidNullScheduleName() throws Exception {
        // given
        SecurityTestUtils.mockAuthentication(member1);

        ScheduleUpdateRequest request = TravelScheduleFixture.createUpdateScheduleRequest(
                null,
                LocalDate.now(),
                LocalDate.now().plusDays(1),
                TravelRouteFixture.createRouteRequest(1, place1WithThumb.getPlaceId()),
                TravelRouteFixture.createRouteRequest(2, placeWithoutThumb.getPlaceId())
        );

        // when, then
        mockMvc.perform(patch("/api/schedules/{scheduleId}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("일정 이름은 필수 입력 값입니다.")));
    }

    @Test
    @DisplayName("일정 수정 시 일정 시작일 null 값이 들어와 예외 발생")
    void updateSchedule_invalidNullStartDate() throws Exception {
        // given
        SecurityTestUtils.mockAuthentication(member1);

        ScheduleUpdateRequest request = TravelScheduleFixture.createUpdateScheduleRequest(
                "수정 테스트",
                null,
                LocalDate.now().plusDays(1),
                TravelRouteFixture.createRouteRequest(1, place1WithThumb.getPlaceId()),
                TravelRouteFixture.createRouteRequest(2, placeWithoutThumb.getPlaceId())
        );

        // when, then
        mockMvc.perform(patch("/api/schedules/{scheduleId}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("일정 시작 날짜는 필수 입력 값입니다.")));
    }


    @Test
    @DisplayName("일정 수정 시 일정 종료일 null 값이 들어와 예외 발생")
    void updateSchedule_invalidNullEndDate() throws Exception {
        // given
        SecurityTestUtils.mockAuthentication(member1);

        ScheduleUpdateRequest request = TravelScheduleFixture.createUpdateScheduleRequest(
                "수정 테스트",
                LocalDate.now(),
                null,
                TravelRouteFixture.createRouteRequest(1, place1WithThumb.getPlaceId()),
                TravelRouteFixture.createRouteRequest(2, placeWithoutThumb.getPlaceId())
        );

        // when, then
        mockMvc.perform(patch("/api/schedules/{scheduleId}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("일정 종료 날짜는 필수 입력 값입니다.")));
    }

    @Test
    @DisplayName("일정 수정 시 일정 생성일이 종료일 이후 날짜로 예외 발생")
    void updateSchedule_invalidStartDateAfterEndDate() throws Exception {
        // given
        SecurityTestUtils.mockAuthentication(member1);

        ScheduleUpdateRequest request = TravelScheduleFixture.createUpdateScheduleRequest(
                "수정 테스트",
                LocalDate.now().plusDays(1),
                LocalDate.now(),
                TravelRouteFixture.createRouteRequest(1, place1WithThumb.getPlaceId()),
                TravelRouteFixture.createRouteRequest(2, placeWithoutThumb.getPlaceId())
        );

        // when, then
        mockMvc.perform(patch("/api/schedules/{scheduleId}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("시작일은 종료일보다 이전이어야 합니다.")));
    }


    @Test
    @DisplayName("일정 수정 시 여행 루트 순서 null 값이 들어와 예외 발생")
    void updateSchedule_invalidNullRouteOrder() throws Exception {
        // given
        SecurityTestUtils.mockAuthentication(member1);

        RouteRequest routeRequest = TravelRouteFixture.createRouteRequest(null, place2WithThumb.getPlaceId());

        ScheduleUpdateRequest request = TravelScheduleFixture.createUpdateScheduleRequest(
                "수정 테스트",
                LocalDate.now(),
                LocalDate.now().plusDays(1),
                routeRequest
        );

        // when, then
        mockMvc.perform(patch("/api/schedules/{scheduleId}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("여행 루트 순서는 필수 입력 값입니다.")));
    }


    @ParameterizedTest
    @DisplayName("일정 수정 시 여행 루트 순서에 1 미만 값이 들어와 예외 발생")
    @ValueSource(ints = {-1000, -1, 0})
    void updateSchedule_invalidMinRouteOrder(Integer input) throws Exception {
        // given
        SecurityTestUtils.mockAuthentication(member1);

        RouteRequest routeRequest = TravelRouteFixture.createRouteRequest(input, place2WithThumb.getPlaceId());

        ScheduleUpdateRequest request = TravelScheduleFixture.createUpdateScheduleRequest(
                "수정 테스트",
                LocalDate.now(),
                LocalDate.now().plusDays(1),
                routeRequest
        );

        // when, then
        mockMvc.perform(patch("/api/schedules/{scheduleId}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("여행 루트 순서는 1 이상의 값이어야 합니다.")));
    }

    @Test
    @DisplayName("일정 수정 시 여행 루트의 여행지 id에 null 값이 들어와 예외 발생")
    void updateSchedule_invalidNullPlaceId() throws Exception {
        // given
        SecurityTestUtils.mockAuthentication(member1);

        RouteRequest routeRequest = TravelRouteFixture.createRouteRequest(1, null);

        ScheduleUpdateRequest request = TravelScheduleFixture.createUpdateScheduleRequest(
                "수정 테스트",
                LocalDate.now(),
                LocalDate.now().plusDays(1),
                routeRequest
        );

        // when, then
        mockMvc.perform(patch("/api/schedules/{scheduleId}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("여행지 ID는 필수 입력 값입니다.")));
    }


    @ParameterizedTest
    @DisplayName("일정 수정 시 여행 루트의 여행지 id에 1미만 값이 들어와 예외 발생")
    @ValueSource(longs = {-1L, 0L})
    void updateSchedule_invalidMinPlaceId(Long input) throws Exception {
        // given
        SecurityTestUtils.mockAuthentication(member1);

        RouteRequest routeRequest = TravelRouteFixture.createRouteRequest(1, input);

        ScheduleUpdateRequest request = TravelScheduleFixture.createUpdateScheduleRequest(
                "수정 테스트",
                LocalDate.now(),
                LocalDate.now().plusDays(1),
                routeRequest
        );

        // when, then
        mockMvc.perform(patch("/api/schedules/{scheduleId}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("여행지 ID는 1 이상의 값이어야 합니다.")));
    }


    @Test
    @DisplayName("일정 수정 시 수정 권한이 없어 예외 발생")
    void updateSchedule_forbiddenEdit() throws Exception {
        // given
        SecurityTestUtils.mockAuthentication(member2);

        ScheduleUpdateRequest request = TravelScheduleFixture.createUpdateScheduleRequest(
                "수정 테스트",
                LocalDate.now(),
                LocalDate.now().plusDays(1),
                TravelRouteFixture.createRouteRequest(1, place1WithThumb.getPlaceId())
        );

        willThrow(new ForbiddenAttendeeException(ErrorCode.FORBIDDEN_EDIT_SCHEDULE))
                .given(travelScheduleService).updateSchedule(any(ScheduleUpdateRequest.class), anyLong(), anyLong());

        // when, then
        mockMvc.perform(patch("/api/schedules/{scheduleId}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.FORBIDDEN_EDIT_SCHEDULE.getMessage()));
    }


    @Test
    @DisplayName("일정 삭제")
    void deleteSchedule() throws Exception {
        // given
        SecurityTestUtils.mockAuthentication(member1);

        // when, then
        mockMvc.perform(delete("/api/schedules/{scheduleId}", 1L))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(SuccessCode.GENERAL_SUCCESS.getMessage()));

    }


    @Test
    @DisplayName("일정 삭제 시 삭제 권한이 없는 회원 요청으로 예외 발생")
    void deleteSchedule_forbiddenSchedule() throws Exception {
        // given
        SecurityTestUtils.mockAuthentication(member2);

        willThrow(new ForbiddenScheduleException(ErrorCode.FORBIDDEN_DELETE_SCHEDULE))
                .given(travelScheduleService).deleteSchedule(anyLong(), anyLong());

        // when, then
        mockMvc.perform(delete("/api/schedules/{scheduleId}", 1L))
                .andDo(print())
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.FORBIDDEN_DELETE_SCHEDULE.getMessage()));

    }

    @Test
    @DisplayName("일정 상세 조회 내에 여행지 조회")
    void getTravelPlaces() throws Exception {
        // give
        SecurityTestUtils.mockAuthentication(member1);

        List<PlaceResponse> placeResponses = List.of(
                TravelPlaceFixture.createPlaceResponse(place2WithThumb, place2ThumbUrl),
                TravelPlaceFixture.createPlaceResponse(placeWithoutThumb, null),
                TravelPlaceFixture.createPlaceResponse(place1WithThumb, place1ThumbUrl)
        );
        Page<PlaceResponse> response = PageUtils.createPage(
                placeResponses,
                PageUtils.defaultPageable(1),
                placeResponses.size()
        );

        given(travelService.getTravelPlacesByJungGu(anyInt()))
                .willReturn(response);

        // when, then
        mockMvc.perform(get("/api/schedules/{scheduleId}/travels", 1L)
                        .param("page", "1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalElements").value(3))
                .andExpect(jsonPath("$.data.content[0].placeName").value(place2WithThumb.getPlaceName()))
                .andExpect(jsonPath("$.data.content[0].thumbnailUrl").value(place2ThumbUrl))
                .andExpect(jsonPath("$.data.content[1].placeName").value(placeWithoutThumb.getPlaceName()))
                .andExpect(jsonPath("$.data.content[1].thumbnailUrl", nullValue()))
                .andExpect(jsonPath("$.data.content[2].placeName").value(place1WithThumb.getPlaceName()))
                .andExpect(jsonPath("$.data.content[2].thumbnailUrl").value(place1ThumbUrl));
    }

    @Test
    @DisplayName("일정 상세 조회 내 여행지 조회 시 여행지 데이터 존재하지 않는 경우")
    void getTravelPlacesWithoutData() throws Exception {
        // given
        SecurityTestUtils.mockAuthentication(member1);

        Page<PlaceResponse> response = PageUtils.createPage(
                Collections.emptyList(),
                PageUtils.defaultPageable(1),
                0
        );

        given(travelService.getTravelPlacesByJungGu(anyInt()))
                .willReturn(response);

        // when, then
        mockMvc.perform(get("/api/schedules/{scheduleId}/travels", 1L)
                        .param("page", "1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalElements").value(0))
                .andExpect(jsonPath("$.data.content").isEmpty());
    }

    @Test
    @DisplayName("일정 상세 조회 내 여행지 검색")
    void searchTravelPlaces() throws Exception {
        // given
        SecurityTestUtils.mockAuthentication(member1);

        List<PlaceResponse> placeResponses = List.of(
                TravelPlaceFixture.createPlaceResponse(placeWithoutThumb, null),
                TravelPlaceFixture.createPlaceResponse(place1WithThumb, place1ThumbUrl)
        );
        Page<PlaceResponse> response = PageUtils.createPage(
                placeResponses,
                PageUtils.defaultPageable(1),
                placeResponses.size()
        );

        given(travelService.searchTravelPlaces(anyInt(), eq("강남")))
                .willReturn(response);

        // when, then
        mockMvc.perform(get("/api/schedules/{scheduleId}/travels/search", 1L)
                        .param("page", "1")
                        .param("keyword", "강남"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalElements").value(2))
                .andExpect(jsonPath("$.data.content[0].district").value(placeWithoutThumb.getDistrict().getDistrictName()))
                .andExpect(jsonPath("$.data.content[0].placeName").value(placeWithoutThumb.getPlaceName()))
                .andExpect(jsonPath("$.data.content[0].thumbnailUrl", nullValue()))
                .andExpect(jsonPath("$.data.content[1].district").value(place1WithThumb.getDistrict().getDistrictName()))
                .andExpect(jsonPath("$.data.content[1].placeName").value(place1WithThumb.getPlaceName()))
                .andExpect(jsonPath("$.data.content[1].thumbnailUrl").value(place1ThumbUrl));
    }

    @Test
    @DisplayName("일정 상세 조회 내 여행지 검색 시 검색 결과가 존재하지 않는 경우")
    void searchTravelPlaces_emptyResult() throws Exception {
        // given
        SecurityTestUtils.mockAuthentication(member1);

        Page<PlaceResponse> response = PageUtils.createPage(
                Collections.emptyList(),
                PageUtils.defaultPageable(1),
                0
        );

        given(travelService.searchTravelPlaces(anyInt(), eq("ㅁㄴㅇㄹ")))
                .willReturn(response);

        // when, then
        mockMvc.perform(get("/api/schedules/{scheduleId}/travels/search", 1L)
                        .param("page", "1")
                        .param("keyword", "ㅁㄴㅇㄹ"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalElements").value(0))
                .andExpect(jsonPath("$.data.content").isEmpty());
    }



}
