package com.triptune.schedule.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.triptune.common.entity.*;
import com.triptune.common.repository.*;
import com.triptune.global.message.SuccessCode;
import com.triptune.member.entity.Member;
import com.triptune.profile.entity.ProfileImage;
import com.triptune.member.repository.MemberRepository;
import com.triptune.profile.repository.ProfileImageRepository;
import com.triptune.schedule.ScheduleTest;
import com.triptune.schedule.dto.request.ScheduleCreateRequest;
import com.triptune.schedule.dto.request.RouteRequest;
import com.triptune.schedule.dto.request.ScheduleUpdateRequest;
import com.triptune.schedule.entity.TravelRoute;
import com.triptune.schedule.entity.TravelSchedule;
import com.triptune.schedule.enums.AttendeePermission;
import com.triptune.schedule.enums.AttendeeRole;
import com.triptune.schedule.enums.ScheduleSearchType;
import com.triptune.schedule.repository.ChatMessageRepository;
import com.triptune.schedule.repository.TravelAttendeeRepository;
import com.triptune.schedule.repository.TravelRouteRepository;
import com.triptune.schedule.repository.TravelScheduleRepository;
import com.triptune.travel.entity.TravelPlace;
import com.triptune.travel.enums.ThemeType;
import com.triptune.travel.repository.TravelImageRepository;
import com.triptune.travel.repository.TravelPlaceRepository;
import com.triptune.global.message.ErrorCode;
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

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional
@AutoConfigureMockMvc
@ActiveProfiles("mongo")
public class TravelScheduleControllerTest extends ScheduleTest {
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
    @Autowired private ProfileImageRepository profileImageRepository;
    @Autowired private ChatMessageRepository chatMessageRepository;
    @Autowired private ApiContentTypeRepository apiContentTypeRepository;

    private Member member1;
    private Member member2;
    private Member member3;

    private TravelPlace placeWithThumbnail1;
    private TravelPlace placeWithThumbnail2;
    private TravelPlace placeWithoutThumbnail;

    @BeforeEach
    void setUp(){
        chatMessageRepository.deleteAll();

        ProfileImage profileImage1 = profileImageRepository.save(createProfileImage("member1Image"));
        member1 = memberRepository.save(createNativeTypeMember("member1@email.com", profileImage1));

        ProfileImage profileImage2 = profileImageRepository.save(createProfileImage("member2Image"));
        member2 = memberRepository.save(createNativeTypeMember("member2@email.com", profileImage2));

        ProfileImage profileImage3 = profileImageRepository.save(createProfileImage("member3Image"));
        member3 = memberRepository.save(createNativeTypeMember("member3@email.com", profileImage3));

        Country country = countryRepository.save(createCountry());
        City city = cityRepository.save(createCity(country, "서울"));
        District gangnam = districtRepository.save(createDistrict(city, "강남구"));
        District jungGu = districtRepository.save(createDistrict(city, "중구"));
        ApiCategory apiCategory = apiCategoryRepository.save(createApiCategory());
        ApiContentType apiContentType = apiContentTypeRepository.save(createApiContentType(ThemeType.ATTRACTIONS));

        placeWithThumbnail1 = travelPlaceRepository.save(
                createTravelPlaceWithLocation(
                        country,
                        city,
                        gangnam,
                        apiCategory,
                        apiContentType,
                        "여행지1",
                        37.49850,
                        127.02820
                )
        );
        travelImageRepository.save(createTravelImage(placeWithThumbnail1, "test1", true));
        travelImageRepository.save(createTravelImage(placeWithThumbnail1, "test2", false));

        placeWithThumbnail2 = travelPlaceRepository.save(
                createTravelPlaceWithLocation(
                        country,
                        city,
                        jungGu,
                        apiCategory,
                        apiContentType,
                        "여행지2",
                        37.56420,
                        126.99800
                )
        );
        travelImageRepository.save(createTravelImage(placeWithThumbnail2, "test1", true));
        travelImageRepository.save(createTravelImage(placeWithThumbnail2, "test2", false));

        placeWithoutThumbnail = travelPlaceRepository.save(
                createTravelPlaceWithLocation(
                        country,
                        city,
                        gangnam,
                        apiCategory,
                        apiContentType,
                        "여행지3",
                        37.49790,
                        127.02760
                )
        );

    }

    @Test
    @DisplayName("전체 일정 목록 조회")
    void getAllSchedules() throws Exception {
        // given
        TravelSchedule schedule1 = travelScheduleRepository.save(createTravelSchedule("테스트1"));
        travelAttendeeRepository.save(createAuthorTravelAttendee(schedule1, member1));
        travelAttendeeRepository.save(createGuestTravelAttendee(schedule1, member2, AttendeePermission.READ));
        travelRouteRepository.save(createTravelRoute(schedule1, placeWithThumbnail1, 1));
        travelRouteRepository.save(createTravelRoute(schedule1, placeWithoutThumbnail, 2));
        travelRouteRepository.save(createTravelRoute(schedule1, placeWithThumbnail2, 3));

        TravelSchedule schedule2 = travelScheduleRepository.save(createTravelSchedule("테스트2"));
        travelAttendeeRepository.save(createAuthorTravelAttendee(schedule2, member2));
        travelAttendeeRepository.save(createGuestTravelAttendee(schedule2, member1, AttendeePermission.CHAT));

        TravelSchedule schedule3 = travelScheduleRepository.save(createTravelSchedule("테스트3"));
        travelAttendeeRepository.save(createAuthorTravelAttendee(schedule3, member1));


        mockAuthentication(member1);

        // when, then
        mockMvc.perform(get("/api/schedules")
                        .param("page", "1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalElements").value(3))
                .andExpect(jsonPath("$.data.totalSharedElements").value(2))
                .andExpect(jsonPath("$.data.content[0].scheduleName").value(schedule3.getScheduleName()))
                .andExpect(jsonPath("$.data.content[0].sinceUpdate").exists())
                .andExpect(jsonPath("$.data.content[0].author.nickname").value(member1.getNickname()))
                .andExpect(jsonPath("$.data.content[0].thumbnailUrl", nullValue()))
                .andExpect(jsonPath("$.data.content[1].scheduleName").value(schedule2.getScheduleName()))
                .andExpect(jsonPath("$.data.content[1].sinceUpdate").exists())
                .andExpect(jsonPath("$.data.content[1].author.nickname").value(member2.getNickname()))
                .andExpect(jsonPath("$.data.content[1].thumbnailUrl", nullValue()))
                .andExpect(jsonPath("$.data.content[2].scheduleName").value(schedule1.getScheduleName()))
                .andExpect(jsonPath("$.data.content[2].sinceUpdate").exists())
                .andExpect(jsonPath("$.data.content[2].author.nickname").value(member1.getNickname()))
                .andExpect(jsonPath("$.data.content[2].thumbnailUrl").value(placeWithThumbnail1.getThumbnailUrl()));
    }

    @Test
    @DisplayName("전체 일정 목록 조회 시 여행 루트 데이터가 없는 경우")
    void getAllSchedules_emptyRoutes() throws Exception {
        // given
        TravelSchedule schedule1 = travelScheduleRepository.save(createTravelSchedule("테스트1"));
        travelAttendeeRepository.save(createAuthorTravelAttendee(schedule1, member1));
        travelAttendeeRepository.save(createGuestTravelAttendee(schedule1, member2, AttendeePermission.READ));

        TravelSchedule schedule2 = travelScheduleRepository.save(createTravelSchedule("테스트2"));
        travelAttendeeRepository.save(createAuthorTravelAttendee(schedule2, member2));
        travelAttendeeRepository.save(createGuestTravelAttendee(schedule2, member1, AttendeePermission.CHAT));

        TravelSchedule schedule3 = travelScheduleRepository.save(createTravelSchedule("테스트3"));
        travelAttendeeRepository.save(createAuthorTravelAttendee(schedule3, member1));

        mockAuthentication(member1);

        // when, then
        mockMvc.perform(get("/api/schedules")
                        .param("page", "1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalElements").value(3))
                .andExpect(jsonPath("$.data.totalSharedElements").value(2))
                .andExpect(jsonPath("$.data.content[0].scheduleName").value(schedule3.getScheduleName()))
                .andExpect(jsonPath("$.data.content[0].sinceUpdate").exists())
                .andExpect(jsonPath("$.data.content[0].author.nickname").value(member1.getNickname()))
                .andExpect(jsonPath("$.data.content[0].thumbnailUrl", nullValue()))
                .andExpect(jsonPath("$.data.content[1].scheduleName").value(schedule2.getScheduleName()))
                .andExpect(jsonPath("$.data.content[1].sinceUpdate").exists())
                .andExpect(jsonPath("$.data.content[1].author.nickname").value(member2.getNickname()))
                .andExpect(jsonPath("$.data.content[1].thumbnailUrl", nullValue()))
                .andExpect(jsonPath("$.data.content[2].scheduleName").value(schedule1.getScheduleName()))
                .andExpect(jsonPath("$.data.content[2].sinceUpdate").exists())
                .andExpect(jsonPath("$.data.content[2].author.nickname").value(member1.getNickname()))
                .andExpect(jsonPath("$.data.content[2].thumbnailUrl", nullValue()));

    }

    @Test
    @DisplayName("전체 일정 목록 조회 시 데이터 없는 경우")
    void getAllSchedules_emptyResult() throws Exception {
        // given
        mockAuthentication(member1);

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
        TravelSchedule schedule1 = travelScheduleRepository.save(createTravelSchedule("테스트1"));
        travelAttendeeRepository.save(createAuthorTravelAttendee(schedule1, member1));
        travelAttendeeRepository.save(createGuestTravelAttendee(schedule1, member2, AttendeePermission.READ));
        travelRouteRepository.save(createTravelRoute(schedule1, placeWithThumbnail1, 1));
        travelRouteRepository.save(createTravelRoute(schedule1, placeWithoutThumbnail, 2));

        TravelSchedule schedule2 = travelScheduleRepository.save(createTravelSchedule("테스트2"));
        travelAttendeeRepository.save(createAuthorTravelAttendee(schedule2, member2));
        travelAttendeeRepository.save(createGuestTravelAttendee(schedule2, member1, AttendeePermission.CHAT));
        travelRouteRepository.save(createTravelRoute(schedule2, placeWithThumbnail2, 1));

        TravelSchedule schedule3 = travelScheduleRepository.save(createTravelSchedule("테스트3"));
        travelAttendeeRepository.save(createAuthorTravelAttendee(schedule3, member1));


        mockAuthentication(member1);

        // when, then
        mockMvc.perform(get("/api/schedules/shared")
                        .param("page", "1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalElements").value(3))
                .andExpect(jsonPath("$.data.totalSharedElements").value(2))
                .andExpect(jsonPath("$.data.content[0].scheduleName").value(schedule2.getScheduleName()))
                .andExpect(jsonPath("$.data.content[0].sinceUpdate").exists())
                .andExpect(jsonPath("$.data.content[0].author.nickname").value(member2.getNickname()))
                .andExpect(jsonPath("$.data.content[0].thumbnailUrl").value(placeWithThumbnail2.getThumbnailUrl()))
                .andExpect(jsonPath("$.data.content[1].scheduleName").value(schedule1.getScheduleName()))
                .andExpect(jsonPath("$.data.content[1].sinceUpdate").exists())
                .andExpect(jsonPath("$.data.content[1].author.nickname").value(member1.getNickname()))
                .andExpect(jsonPath("$.data.content[1].thumbnailUrl").value(placeWithThumbnail2.getThumbnailUrl()));
    }

    @Test
    @DisplayName("공유된 일정 목록 조회 시 여행 루트 데이터가 없는 경우")
    void getSharedSchedules_emptyRoute() throws Exception {
        // given
        TravelSchedule schedule1 = travelScheduleRepository.save(createTravelSchedule("테스트1"));
        travelAttendeeRepository.save(createAuthorTravelAttendee(schedule1, member1));
        travelAttendeeRepository.save(createGuestTravelAttendee(schedule1, member2, AttendeePermission.READ));

        TravelSchedule schedule2 = travelScheduleRepository.save(createTravelSchedule("테스트2"));
        travelAttendeeRepository.save(createAuthorTravelAttendee(schedule2, member2));
        travelAttendeeRepository.save(createGuestTravelAttendee(schedule2, member1, AttendeePermission.CHAT));

        TravelSchedule schedule3 = travelScheduleRepository.save(createTravelSchedule("테스트3"));
        travelAttendeeRepository.save(createAuthorTravelAttendee(schedule3, member1));


        mockAuthentication(member1);

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
                .andExpect(jsonPath("$.data.content[1].sinceUpdate").exists())
                .andExpect(jsonPath("$.data.content[1].author.nickname").value(member1.getNickname()))
                .andExpect(jsonPath("$.data.content[1].thumbnailUrl", nullValue()));

    }

    @Test
    @DisplayName("공유된 일정 목록 조회 시 공유된 일정 없는 경우")
    void getSharedSchedules_withoutShared() throws Exception {
        // given
        TravelSchedule schedule1 = travelScheduleRepository.save(createTravelSchedule("테스트1"));
        travelAttendeeRepository.save(createAuthorTravelAttendee(schedule1, member1));
        travelRouteRepository.save(createTravelRoute(schedule1, placeWithThumbnail1, 1));
        travelRouteRepository.save(createTravelRoute(schedule1, placeWithoutThumbnail, 2));

        TravelSchedule schedule2 = travelScheduleRepository.save(createTravelSchedule("테스트2"));
        travelAttendeeRepository.save(createAuthorTravelAttendee(schedule2, member2));
        travelRouteRepository.save(createTravelRoute(schedule2, placeWithThumbnail2, 1));

        TravelSchedule schedule3 = travelScheduleRepository.save(createTravelSchedule("테스트3"));
        travelAttendeeRepository.save(createAuthorTravelAttendee(schedule3, member1));

        mockAuthentication(member1);

        // when, then
        mockMvc.perform(get("/api/schedules/shared")
                        .param("page", "1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalElements").value(2))
                .andExpect(jsonPath("$.data.totalSharedElements").value(0))
                .andExpect(jsonPath("$.data.content").isEmpty());
    }

    @Test
    @DisplayName("공유된 일정 목록 조회 시 데이터 없는 경우")
    void getSharedSchedules_emptyResult() throws Exception {
        // given
        mockAuthentication(member1);

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
    void getEnableEditSchedule() throws Exception {
        // given
        TravelSchedule schedule1 = travelScheduleRepository.save(createTravelSchedule("테스트1"));
        travelAttendeeRepository.save(createAuthorTravelAttendee(schedule1, member1));
        travelAttendeeRepository.save(createGuestTravelAttendee(schedule1, member2, AttendeePermission.READ));
        travelRouteRepository.save(createTravelRoute(schedule1, placeWithThumbnail1, 1));
        travelRouteRepository.save(createTravelRoute(schedule1, placeWithoutThumbnail, 2));

        TravelSchedule schedule2 = travelScheduleRepository.save(createTravelSchedule("테스트2"));
        travelAttendeeRepository.save(createAuthorTravelAttendee(schedule2, member2));
        travelAttendeeRepository.save(createGuestTravelAttendee(schedule2, member1, AttendeePermission.CHAT));
        travelRouteRepository.save(createTravelRoute(schedule2, placeWithThumbnail2, 1));

        TravelSchedule schedule3 = travelScheduleRepository.save(createTravelSchedule("테스트3"));
        travelAttendeeRepository.save(createAuthorTravelAttendee(schedule3, member1));


        mockAuthentication(member1);

        // when, then
        mockMvc.perform(get("/api/schedules/edit")
                        .param("page", "1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalElements").value(2))
                .andExpect(jsonPath("$.data.content[0].scheduleName").value(schedule3.getScheduleName()))
                .andExpect(jsonPath("$.data.content[0].author").value(member1.getNickname()))
                .andExpect(jsonPath("$.data.content[1].scheduleName").value(schedule1.getScheduleName()))
                .andExpect(jsonPath("$.data.content[1].author").value(member1.getNickname()));
    }

    @Test
    @DisplayName("수정 권한 있는 일정 조회 시 일정 데이터가 없는 경우")
    void getEnableEditSchedule_emptyResult() throws Exception {
        // given
        mockAuthentication(member1);

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
        TravelSchedule schedule1 = travelScheduleRepository.save(createTravelSchedule("테스트1"));
        travelAttendeeRepository.save(createAuthorTravelAttendee(schedule1, member1));
        travelAttendeeRepository.save(createGuestTravelAttendee(schedule1, member2, AttendeePermission.READ));
        travelRouteRepository.save(createTravelRoute(schedule1, placeWithThumbnail1, 1));
        travelRouteRepository.save(createTravelRoute(schedule1, placeWithoutThumbnail, 2));
        travelRouteRepository.save(createTravelRoute(schedule1, placeWithThumbnail2, 3));

        TravelSchedule schedule2 = travelScheduleRepository.save(createTravelSchedule("테스트2"));
        travelAttendeeRepository.save(createAuthorTravelAttendee(schedule2, member2));
        travelAttendeeRepository.save(createGuestTravelAttendee(schedule2, member1, AttendeePermission.CHAT));

        TravelSchedule schedule3 = travelScheduleRepository.save(createTravelSchedule("테스트3"));
        travelAttendeeRepository.save(createAuthorTravelAttendee(schedule3, member1));


        mockAuthentication(member1);

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
                .andExpect(jsonPath("$.data.content[0].scheduleName").value(schedule1.getScheduleName()))
                .andExpect(jsonPath("$.data.content[0].sinceUpdate").exists())
                .andExpect(jsonPath("$.data.content[0].thumbnailUrl").value(placeWithThumbnail2.getThumbnailUrl()))
                .andExpect(jsonPath("$.data.content[0].author.nickname").value(member1.getNickname()))
                .andExpect(jsonPath("$.data.content[0].role").value(AttendeeRole.AUTHOR.name()));
    }

    @Test
    @DisplayName("전체 일정 검색 시 여행 루트 데이터가 없는 경우")
    void searchSchedules_emptyRoutes() throws Exception {
        // given
        TravelSchedule schedule1 = travelScheduleRepository.save(createTravelSchedule("테스트1"));
        travelAttendeeRepository.save(createAuthorTravelAttendee(schedule1, member1));
        travelAttendeeRepository.save(createGuestTravelAttendee(schedule1, member2, AttendeePermission.READ));

        TravelSchedule schedule2 = travelScheduleRepository.save(createTravelSchedule("테스트2"));
        travelAttendeeRepository.save(createAuthorTravelAttendee(schedule2, member2));
        travelAttendeeRepository.save(createGuestTravelAttendee(schedule2, member1, AttendeePermission.CHAT));

        TravelSchedule schedule3 = travelScheduleRepository.save(createTravelSchedule("테스트3"));
        travelAttendeeRepository.save(createAuthorTravelAttendee(schedule3, member1));

        mockAuthentication(member1);

        // when, then
        mockMvc.perform(get("/api/schedules/search")
                        .param("page", "1")
                        .param("type", ScheduleSearchType.ALL.getValue())
                        .param("keyword", "테스트"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalElements").value(3))
                .andExpect(jsonPath("$.data.totalSharedElements").value(2))
                .andExpect(jsonPath("$.data.content.length()").value(3))
                .andExpect(jsonPath("$.data.content[0].scheduleName").value(schedule3.getScheduleName()))
                .andExpect(jsonPath("$.data.content[0].thumbnailUrl", nullValue()))
                .andExpect(jsonPath("$.data.content[0].author.nickname").value(member1.getNickname()))
                .andExpect(jsonPath("$.data.content[0].role").value(AttendeeRole.AUTHOR.name()))
                .andExpect(jsonPath("$.data.content[1].scheduleName").value(schedule2.getScheduleName()))
                .andExpect(jsonPath("$.data.content[1].thumbnailUrl", nullValue()))
                .andExpect(jsonPath("$.data.content[1].author.nickname").value(member2.getNickname()))
                .andExpect(jsonPath("$.data.content[1].role").value(AttendeeRole.GUEST.name()))
                .andExpect(jsonPath("$.data.content[2].scheduleName").value(schedule1.getScheduleName()))
                .andExpect(jsonPath("$.data.content[2].sinceUpdate").exists())
                .andExpect(jsonPath("$.data.content[2].thumbnailUrl", nullValue()))
                .andExpect(jsonPath("$.data.content[2].author.nickname").value(member1.getNickname()))
                .andExpect(jsonPath("$.data.content[2].role").value(AttendeeRole.AUTHOR.name()));
    }

    @Test
    @DisplayName("전체 일정 검색 시 공유된 일정 없는 경우")
    void searchSchedules_emptyShared() throws Exception {
        // given
        TravelSchedule schedule1 = travelScheduleRepository.save(createTravelSchedule("테스트1"));
        travelAttendeeRepository.save(createAuthorTravelAttendee(schedule1, member1));
        travelRouteRepository.save(createTravelRoute(schedule1, placeWithThumbnail1, 1));
        travelRouteRepository.save(createTravelRoute(schedule1, placeWithoutThumbnail, 2));
        travelRouteRepository.save(createTravelRoute(schedule1, placeWithThumbnail2, 3));

        TravelSchedule schedule2 = travelScheduleRepository.save(createTravelSchedule("테스트2"));
        travelAttendeeRepository.save(createAuthorTravelAttendee(schedule2, member2));

        TravelSchedule schedule3 = travelScheduleRepository.save(createTravelSchedule("테스트3"));
        travelAttendeeRepository.save(createAuthorTravelAttendee(schedule3, member1));

        mockAuthentication(member1);

        // when, then
        mockMvc.perform(get("/api/schedules/search")
                        .param("page", "1")
                        .param("type", ScheduleSearchType.ALL.getValue())
                        .param("keyword", "테스트"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalElements").value(2))
                .andExpect(jsonPath("$.data.totalSharedElements").value(0))
                .andExpect(jsonPath("$.data.content.length()").value(2))
                .andExpect(jsonPath("$.data.content[0].scheduleName").value(schedule3.getScheduleName()))
                .andExpect(jsonPath("$.data.content[0].thumbnailUrl", nullValue()))
                .andExpect(jsonPath("$.data.content[0].author.nickname").value(member1.getNickname()))
                .andExpect(jsonPath("$.data.content[0].role").value(AttendeeRole.AUTHOR.name()))
                .andExpect(jsonPath("$.data.content[1].scheduleName").value(schedule1.getScheduleName()))
                .andExpect(jsonPath("$.data.content[1].sinceUpdate").exists())
                .andExpect(jsonPath("$.data.content[1].thumbnailUrl").value(placeWithThumbnail2.getThumbnailUrl()))
                .andExpect(jsonPath("$.data.content[1].author.nickname").value(member1.getNickname()))
                .andExpect(jsonPath("$.data.content[1].role").value(AttendeeRole.AUTHOR.name()));
    }

    @Test
    @DisplayName("전체 일정 검색 시 검색 결과 없는 경우")
    void searchSchedules_emptyResult() throws Exception {
        // given
        TravelSchedule schedule1 = travelScheduleRepository.save(createTravelSchedule("테스트1"));
        travelAttendeeRepository.save(createAuthorTravelAttendee(schedule1, member1));
        travelAttendeeRepository.save(createGuestTravelAttendee(schedule1, member2, AttendeePermission.READ));
        travelRouteRepository.save(createTravelRoute(schedule1, placeWithThumbnail1, 1));
        travelRouteRepository.save(createTravelRoute(schedule1, placeWithoutThumbnail, 2));
        travelRouteRepository.save(createTravelRoute(schedule1, placeWithThumbnail2, 3));

        TravelSchedule schedule2 = travelScheduleRepository.save(createTravelSchedule("테스트2"));
        travelAttendeeRepository.save(createAuthorTravelAttendee(schedule2, member2));
        travelAttendeeRepository.save(createGuestTravelAttendee(schedule2, member1, AttendeePermission.CHAT));

        TravelSchedule schedule3 = travelScheduleRepository.save(createTravelSchedule("테스트3"));
        travelAttendeeRepository.save(createAuthorTravelAttendee(schedule3, member1));


        mockAuthentication(member1);

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
        TravelSchedule schedule1 = travelScheduleRepository.save(createTravelSchedule("테스트1"));
        travelAttendeeRepository.save(createAuthorTravelAttendee(schedule1, member1));
        travelAttendeeRepository.save(createGuestTravelAttendee(schedule1, member2, AttendeePermission.READ));
        travelRouteRepository.save(createTravelRoute(schedule1, placeWithThumbnail1, 1));
        travelRouteRepository.save(createTravelRoute(schedule1, placeWithoutThumbnail, 2));
        travelRouteRepository.save(createTravelRoute(schedule1, placeWithThumbnail2, 3));

        TravelSchedule schedule2 = travelScheduleRepository.save(createTravelSchedule("테스트2"));
        travelAttendeeRepository.save(createAuthorTravelAttendee(schedule2, member2));
        travelAttendeeRepository.save(createGuestTravelAttendee(schedule2, member1, AttendeePermission.CHAT));

        TravelSchedule schedule3 = travelScheduleRepository.save(createTravelSchedule("테스트3"));
        travelAttendeeRepository.save(createAuthorTravelAttendee(schedule3, member1));


        mockAuthentication(member1);

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
                .andExpect(jsonPath("$.data.content.length()").value(2))
                .andExpect(jsonPath("$.data.content[0].scheduleName").value(schedule2.getScheduleName()))
                .andExpect(jsonPath("$.data.content[0].thumbnailUrl", nullValue()))
                .andExpect(jsonPath("$.data.content[0].author.nickname").value(member2.getNickname()))
                .andExpect(jsonPath("$.data.content[0].role").value(AttendeeRole.GUEST.name()))
                .andExpect(jsonPath("$.data.content[1].scheduleName").value(schedule1.getScheduleName()))
                .andExpect(jsonPath("$.data.content[1].sinceUpdate").exists())
                .andExpect(jsonPath("$.data.content[1].thumbnailUrl").value(placeWithThumbnail2.getThumbnailUrl()))
                .andExpect(jsonPath("$.data.content[1].author.nickname").value(member1.getNickname()))
                .andExpect(jsonPath("$.data.content[1].role").value(AttendeeRole.AUTHOR.name()));
    }

    @Test
    @DisplayName("공유된 일정 검색 시 여행 루트 데이터가 없는 경우")
    void searchSharedSchedules_emptyRoutes() throws Exception {
        // given
        TravelSchedule schedule1 = travelScheduleRepository.save(createTravelSchedule("테스트1"));
        travelAttendeeRepository.save(createAuthorTravelAttendee(schedule1, member1));
        travelAttendeeRepository.save(createGuestTravelAttendee(schedule1, member2, AttendeePermission.READ));

        TravelSchedule schedule2 = travelScheduleRepository.save(createTravelSchedule("테스트2"));
        travelAttendeeRepository.save(createAuthorTravelAttendee(schedule2, member2));
        travelAttendeeRepository.save(createGuestTravelAttendee(schedule2, member1, AttendeePermission.CHAT));

        TravelSchedule schedule3 = travelScheduleRepository.save(createTravelSchedule("테스트3"));
        travelAttendeeRepository.save(createAuthorTravelAttendee(schedule3, member1));


        mockAuthentication(member1);

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
                .andExpect(jsonPath("$.data.content.length()").value(2))
                .andExpect(jsonPath("$.data.content[0].scheduleName").value(schedule2.getScheduleName()))
                .andExpect(jsonPath("$.data.content[0].thumbnailUrl", nullValue()))
                .andExpect(jsonPath("$.data.content[0].author.nickname").value(member2.getNickname()))
                .andExpect(jsonPath("$.data.content[0].role").value(AttendeeRole.GUEST.name()))
                .andExpect(jsonPath("$.data.content[1].scheduleName").value(schedule1.getScheduleName()))
                .andExpect(jsonPath("$.data.content[1].sinceUpdate").exists())
                .andExpect(jsonPath("$.data.content[1].thumbnailUrl", nullValue()))
                .andExpect(jsonPath("$.data.content[1].author.nickname").value(member1.getNickname()))
                .andExpect(jsonPath("$.data.content[1].role").value(AttendeeRole.AUTHOR.name()));

    }

    @Test
    @DisplayName("공유된 일정 검색 시 검색 결과 없는 경우")
    void searchSharedSchedules_emptyResult() throws Exception {
        // given
        TravelSchedule schedule1 = travelScheduleRepository.save(createTravelSchedule("테스트1"));
        travelAttendeeRepository.save(createAuthorTravelAttendee(schedule1, member1));
        travelAttendeeRepository.save(createGuestTravelAttendee(schedule1, member2, AttendeePermission.READ));
        travelRouteRepository.save(createTravelRoute(schedule1, placeWithThumbnail1, 1));
        travelRouteRepository.save(createTravelRoute(schedule1, placeWithoutThumbnail, 2));
        travelRouteRepository.save(createTravelRoute(schedule1, placeWithThumbnail2, 3));

        TravelSchedule schedule2 = travelScheduleRepository.save(createTravelSchedule("테스트2"));
        travelAttendeeRepository.save(createAuthorTravelAttendee(schedule2, member2));
        travelAttendeeRepository.save(createGuestTravelAttendee(schedule2, member1, AttendeePermission.CHAT));

        TravelSchedule schedule3 = travelScheduleRepository.save(createTravelSchedule("테스트3"));
        travelAttendeeRepository.save(createAuthorTravelAttendee(schedule3, member1));


        mockAuthentication(member1);

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
        mockAuthentication(member1);

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
        ScheduleCreateRequest request = createScheduleRequest(
                "테스트",
                LocalDate.now(),
                LocalDate.now()
        );
        mockAuthentication(member1);

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
        ScheduleCreateRequest request = createScheduleRequest(
                input,
                LocalDate.now(),
                LocalDate.now().plusDays(10)
        );
        mockAuthentication(member1);

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
        ScheduleCreateRequest request = createScheduleRequest(
                null,
                LocalDate.now(),
                LocalDate.now().plusDays(10)
        );
        mockAuthentication(member1);

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
        ScheduleCreateRequest request = createScheduleRequest(
                "테스트",
                null,
                LocalDate.now().plusDays(10)
        );
        mockAuthentication(member1);

        // when, then
        mockMvc.perform(post("/api/schedules")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("일정 시작 날짜는 필수 입력 값입니다.")));
    }
//
    @Test
    @DisplayName("일정 생성 시 일정 시작일 오늘 이전 날짜 값으로 예외 발생")
    void createSchedule_invalidPreviousStartDate() throws Exception {
        // given
        ScheduleCreateRequest request = createScheduleRequest(
                "테스트",
                LocalDate.now().minusDays(1),
                LocalDate.now().plusDays(10)
        );
        mockAuthentication(member1);

        // when, then
        mockMvc.perform(post("/api/schedules")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("오늘 이후 날짜만 입력 가능합니다.")));
    }


    @Test
    @DisplayName("일정 생성 시 일정 종료일 null 값이 들어와 예외 발생")
    void createSchedule_invalidNullEndDate() throws Exception {
        // given
        ScheduleCreateRequest request = createScheduleRequest(
                "테스트",
                LocalDate.now(),
                null
        );
        mockAuthentication(member1);

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
    @DisplayName("일정 생성 시 일정 종료일 오늘 이전 날짜 값으로 예외 발생")
    void createSchedule_invalidPreviousEndDate() throws Exception {
        // given
        ScheduleCreateRequest request = createScheduleRequest(
                "테스트",
                LocalDate.now(),
                LocalDate.now().minusDays(1)
        );
        mockAuthentication(member1);

        // when, then
        mockMvc.perform(post("/api/schedules")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("오늘 이후 날짜만 입력 가능합니다.")));
    }

    @Test
    @DisplayName("일정 생성 시 일정 생성일이 종료일 이후 날짜로 예외 발생")
    void createSchedule_invalidStartDateAfterEndDate() throws Exception {
        // given
        ScheduleCreateRequest request = createScheduleRequest(
                "테스트",
                LocalDate.now().plusDays(10),
                LocalDate.now()
        );
        mockAuthentication(member1);

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
        TravelSchedule schedule = travelScheduleRepository.save(createTravelSchedule("테스트1"));

        travelAttendeeRepository.save(createAuthorTravelAttendee(schedule, member1));
        travelAttendeeRepository.save(createGuestTravelAttendee(schedule, member2, AttendeePermission.READ));

        travelRouteRepository.save(createTravelRoute(schedule, placeWithThumbnail1, 1));
        travelRouteRepository.save(createTravelRoute(schedule, placeWithoutThumbnail, 2));
        travelRouteRepository.save(createTravelRoute(schedule, placeWithThumbnail2, 3));

        mockAuthentication(member1);

        // when, then
        mockMvc.perform(get("/api/schedules/{scheduleId}", schedule.getScheduleId())
                        .param("page", "1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.scheduleName").value(schedule.getScheduleName()))
                .andExpect(jsonPath("$.data.placeList.totalElements").value(3))
                .andExpect(jsonPath("$.data.placeList.content[0].placeName").value(placeWithThumbnail2.getPlaceName()))
                .andExpect(jsonPath("$.data.placeList.content[0].thumbnailUrl").value(placeWithThumbnail2.getThumbnailUrl()))
                .andExpect(jsonPath("$.data.placeList.content[1].placeName").value(placeWithThumbnail1.getPlaceName()))
                .andExpect(jsonPath("$.data.placeList.content[1].thumbnailUrl").value(placeWithThumbnail1.getThumbnailUrl()))
                .andExpect(jsonPath("$.data.placeList.content[2].placeName").value(placeWithoutThumbnail.getPlaceName()))
                .andExpect(jsonPath("$.data.placeList.content[2].thumbnailUrl", nullValue()));
    }

    @Test
    @DisplayName("일정 상세 조회 시 여행지 데이터 존재하지 않는 경우")
    void getScheduleDetail_emptyPlaces() throws Exception {
        // given
        travelImageRepository.deleteAll();
        travelPlaceRepository.deleteAll();

        TravelSchedule schedule = travelScheduleRepository.save(createTravelSchedule("테스트1"));

        travelAttendeeRepository.save(createAuthorTravelAttendee(schedule, member1));
        travelAttendeeRepository.save(createGuestTravelAttendee(schedule, member2, AttendeePermission.READ));

        mockAuthentication(member1);

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
    @DisplayName("일정 상세 조회 시 일정 데이터 존재하지 않아 예외 발생")
    void getScheduleDetail_scheduleNotFound() throws Exception {
        // given
        mockAuthentication(member1);

        // when, then
        mockMvc.perform(get("/api/schedules/{scheduleId}", 1000L)
                        .param("page", "1"))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.SCHEDULE_NOT_FOUND.getMessage()));
    }


    @Test
    @DisplayName("일정 상세 조회 시 일정에 접근 권한이 없어 예외 발생")
    void getScheduleDetail_forbiddenSchedule() throws Exception {
        // given
        TravelSchedule schedule = travelScheduleRepository.save(createTravelSchedule("테스트2"));
        travelAttendeeRepository.save(createAuthorTravelAttendee(schedule, member2));

        mockAuthentication(member1);

        // when, then
        mockMvc.perform(get("/api/schedules/{scheduleId}", schedule.getScheduleId())
                        .param("page", "1"))
                .andDo(print())
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.FORBIDDEN_ACCESS_SCHEDULE.getMessage()));
    }


    @Test
    @DisplayName("일정 수정")
    void updateSchedule() throws Exception {
        // given
        TravelSchedule schedule = travelScheduleRepository.save(createTravelSchedule("테스트1"));

        travelAttendeeRepository.save(createAuthorTravelAttendee(schedule, member1));
        travelAttendeeRepository.save(createGuestTravelAttendee(schedule, member2, AttendeePermission.READ));

        travelRouteRepository.save(createTravelRoute(schedule, placeWithThumbnail1, 1));
        travelRouteRepository.save(createTravelRoute(schedule, placeWithoutThumbnail, 2));
        travelRouteRepository.save(createTravelRoute(schedule, placeWithThumbnail2, 3));

        RouteRequest routeRequest1 = createRouteRequest(1, placeWithThumbnail2.getPlaceId());
        RouteRequest routeRequest2 = createRouteRequest(2, placeWithoutThumbnail.getPlaceId());

        ScheduleUpdateRequest request = createUpdateScheduleRequest(
                "수정 테스트",
                LocalDate.now().minusDays(2),
                LocalDate.now().minusDays(1),
                routeRequest1,
                routeRequest2
        );

        mockAuthentication(member1);

        // when
        mockMvc.perform(patch("/api/schedules/{scheduleId}", schedule.getScheduleId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(SuccessCode.GENERAL_SUCCESS.getMessage()));

        // then
        assertThat(schedule.getScheduleName()).isEqualTo(request.getScheduleName());
        assertThat(schedule.getStartDate()).isEqualTo(request.getStartDate());
        assertThat(schedule.getEndDate()).isEqualTo(request.getEndDate());
        assertThat(schedule.getTravelRoutes()).hasSize(2);
        assertThat(schedule.getTravelRoutes())
                .extracting(TravelRoute::getRouteOrder)
                .containsExactly(1, 2);

    }


    @Test
    @DisplayName("일정 수정 시 여행 루트 없는 경우")
    void updateSchedule_emptyRoutes() throws Exception {
        // given
        TravelSchedule schedule = travelScheduleRepository.save(createTravelSchedule("테스트1"));

        travelAttendeeRepository.save(createAuthorTravelAttendee(schedule, member1));
        travelAttendeeRepository.save(createGuestTravelAttendee(schedule, member2, AttendeePermission.READ));

        ScheduleUpdateRequest request = createUpdateScheduleRequest(
                "수정 테스트",
                LocalDate.now(),
                LocalDate.now().plusDays(1)
        );

        mockAuthentication(member1);

        // when
        mockMvc.perform(patch("/api/schedules/{scheduleId}", schedule.getScheduleId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(SuccessCode.GENERAL_SUCCESS.getMessage()));

        // then
        assertThat(schedule.getScheduleName()).isEqualTo(request.getScheduleName());
        assertThat(schedule.getStartDate()).isEqualTo(request.getStartDate());
        assertThat(schedule.getEndDate()).isEqualTo(request.getEndDate());
        assertThat(schedule.getTravelRoutes()).hasSize(0);
    }


    @ParameterizedTest
    @DisplayName("일정 수정 시 일정명 빈 값으로 예외 발생")
    @ValueSource(strings = {"", " "})
    void updateSchedule_invalidNotBlankScheduleName(String input) throws Exception {
        // given
        TravelSchedule schedule = travelScheduleRepository.save(createTravelSchedule("테스트1"));

        travelAttendeeRepository.save(createAuthorTravelAttendee(schedule, member1));
        travelAttendeeRepository.save(createGuestTravelAttendee(schedule, member2, AttendeePermission.READ));

        travelRouteRepository.save(createTravelRoute(schedule, placeWithThumbnail1, 1));
        travelRouteRepository.save(createTravelRoute(schedule, placeWithoutThumbnail, 2));

        ScheduleUpdateRequest request = createUpdateScheduleRequest(
                input,
                LocalDate.now(),
                LocalDate.now().plusDays(1),
                createRouteRequest(1, placeWithThumbnail1.getPlaceId()),
                createRouteRequest(2, placeWithoutThumbnail.getPlaceId())
        );

        mockAuthentication(member1);

        // when, then
        mockMvc.perform(patch("/api/schedules/{scheduleId}", schedule.getScheduleId())
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
        TravelSchedule schedule = travelScheduleRepository.save(createTravelSchedule("테스트1"));

        travelAttendeeRepository.save(createAuthorTravelAttendee(schedule, member1));
        travelAttendeeRepository.save(createGuestTravelAttendee(schedule, member2, AttendeePermission.READ));

        travelRouteRepository.save(createTravelRoute(schedule, placeWithThumbnail1, 1));
        travelRouteRepository.save(createTravelRoute(schedule, placeWithoutThumbnail, 2));

        ScheduleUpdateRequest request = createUpdateScheduleRequest(
                null,
                LocalDate.now(),
                LocalDate.now().plusDays(1),
                createRouteRequest(1, placeWithThumbnail1.getPlaceId()),
                createRouteRequest(2, placeWithoutThumbnail.getPlaceId())
        );

        mockAuthentication(member1);

        // when, then
        mockMvc.perform(patch("/api/schedules/{scheduleId}", schedule.getScheduleId())
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
        TravelSchedule schedule = travelScheduleRepository.save(createTravelSchedule("테스트1"));

        travelAttendeeRepository.save(createAuthorTravelAttendee(schedule, member1));
        travelAttendeeRepository.save(createGuestTravelAttendee(schedule, member2, AttendeePermission.READ));
        
        travelRouteRepository.save(createTravelRoute(schedule, placeWithThumbnail1, 1));
        travelRouteRepository.save(createTravelRoute(schedule, placeWithoutThumbnail, 2));

        ScheduleUpdateRequest request = createUpdateScheduleRequest(
                "수정 테스트",
                null,
                LocalDate.now().plusDays(1),
                createRouteRequest(1, placeWithThumbnail1.getPlaceId()),
                createRouteRequest(2, placeWithoutThumbnail.getPlaceId())
        );

        mockAuthentication(member1);

        // when, then
        mockMvc.perform(patch("/api/schedules/{scheduleId}", schedule.getScheduleId())
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
        TravelSchedule schedule = travelScheduleRepository.save(createTravelSchedule("테스트1"));

        travelAttendeeRepository.save(createAuthorTravelAttendee(schedule, member1));
        travelAttendeeRepository.save(createGuestTravelAttendee(schedule, member2, AttendeePermission.READ));

        travelRouteRepository.save(createTravelRoute(schedule, placeWithThumbnail1, 1));
        travelRouteRepository.save(createTravelRoute(schedule, placeWithoutThumbnail, 2));


        ScheduleUpdateRequest request = createUpdateScheduleRequest(
                "수정 테스트",
                LocalDate.now(),
                null,
                createRouteRequest(1, placeWithThumbnail1.getPlaceId()),
                createRouteRequest(2, placeWithoutThumbnail.getPlaceId())
        );

        mockAuthentication(member1);

        // when, then
        mockMvc.perform(patch("/api/schedules/{scheduleId}", schedule.getScheduleId())
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
        TravelSchedule schedule = travelScheduleRepository.save(createTravelSchedule("테스트1"));

        travelAttendeeRepository.save(createAuthorTravelAttendee(schedule, member1));
        travelAttendeeRepository.save(createGuestTravelAttendee(schedule, member2, AttendeePermission.READ));

        travelRouteRepository.save(createTravelRoute(schedule, placeWithThumbnail1, 1));
        travelRouteRepository.save(createTravelRoute(schedule, placeWithoutThumbnail, 2));

        ScheduleUpdateRequest request = createUpdateScheduleRequest(
                "수정 테스트",
                LocalDate.now().plusDays(1),
                LocalDate.now(),
                createRouteRequest(1, placeWithThumbnail1.getPlaceId()),
                createRouteRequest(2, placeWithoutThumbnail.getPlaceId())
        );

        mockAuthentication(member1);

        // when, then
        mockMvc.perform(patch("/api/schedules/{scheduleId}", schedule.getScheduleId())
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
        TravelSchedule schedule = travelScheduleRepository.save(createTravelSchedule("테스트1"));

        travelAttendeeRepository.save(createAuthorTravelAttendee(schedule, member1));
        travelAttendeeRepository.save(createGuestTravelAttendee(schedule, member2, AttendeePermission.READ));

        travelRouteRepository.save(createTravelRoute(schedule, placeWithThumbnail1, 1));
        travelRouteRepository.save(createTravelRoute(schedule, placeWithoutThumbnail, 2));

        RouteRequest routeRequest = createRouteRequest(null, placeWithThumbnail2.getPlaceId());

        ScheduleUpdateRequest request = createUpdateScheduleRequest(
                "수정 테스트",
                LocalDate.now(),
                LocalDate.now().plusDays(1),
                routeRequest
        );

        mockAuthentication(member1);

        // when, then
        mockMvc.perform(patch("/api/schedules/{scheduleId}", schedule.getScheduleId())
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
        TravelSchedule schedule = travelScheduleRepository.save(createTravelSchedule("테스트1"));

        travelAttendeeRepository.save(createAuthorTravelAttendee(schedule, member1));
        travelAttendeeRepository.save(createGuestTravelAttendee(schedule, member2, AttendeePermission.READ));

        travelRouteRepository.save(createTravelRoute(schedule, placeWithThumbnail1, 1));
        travelRouteRepository.save(createTravelRoute(schedule, placeWithoutThumbnail, 2));

        RouteRequest routeRequest = createRouteRequest(input, placeWithThumbnail2.getPlaceId());

        ScheduleUpdateRequest request = createUpdateScheduleRequest(
                "수정 테스트",
                LocalDate.now(),
                LocalDate.now().plusDays(1),
                routeRequest
        );

        mockAuthentication(member1);

        // when, then
        mockMvc.perform(patch("/api/schedules/{scheduleId}", schedule.getScheduleId())
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
        TravelSchedule schedule = travelScheduleRepository.save(createTravelSchedule("테스트1"));

        travelAttendeeRepository.save(createAuthorTravelAttendee(schedule, member1));
        travelAttendeeRepository.save(createGuestTravelAttendee(schedule, member2, AttendeePermission.READ));

        travelRouteRepository.save(createTravelRoute(schedule, placeWithThumbnail1, 1));
        travelRouteRepository.save(createTravelRoute(schedule, placeWithThumbnail2, 2));

        RouteRequest routeRequest = createRouteRequest(1, null);

        ScheduleUpdateRequest request = createUpdateScheduleRequest(
                "수정 테스트",
                LocalDate.now(),
                LocalDate.now().plusDays(1),
                routeRequest
        );

        mockAuthentication(member1);

        // when, then
        mockMvc.perform(patch("/api/schedules/{scheduleId}", schedule.getScheduleId())
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
        TravelSchedule schedule = travelScheduleRepository.save(createTravelSchedule("테스트1"));

        travelAttendeeRepository.save(createAuthorTravelAttendee(schedule, member1));
        travelAttendeeRepository.save(createGuestTravelAttendee(schedule, member2, AttendeePermission.READ));

        travelRouteRepository.save(createTravelRoute(schedule, placeWithThumbnail1, 1));
        travelRouteRepository.save(createTravelRoute(schedule, placeWithoutThumbnail, 2));

        RouteRequest routeRequest = createRouteRequest(1, input);

        ScheduleUpdateRequest request = createUpdateScheduleRequest(
                "수정 테스트",
                LocalDate.now(),
                LocalDate.now().plusDays(1),
                routeRequest
        );

        mockAuthentication(member1);

        // when, then
        mockMvc.perform(patch("/api/schedules/{scheduleId}", schedule.getScheduleId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("여행지 ID는 1 이상의 값이어야 합니다.")));
    }

    @Test
    @DisplayName("일정 수정 시 접근 권한이 없어 예외 발생")
    void updateSchedule_forbiddenAccess() throws Exception {
        // given
        TravelSchedule schedule = travelScheduleRepository.save(createTravelSchedule("테스트1"));

        travelAttendeeRepository.save(createAuthorTravelAttendee(schedule, member1));
        travelAttendeeRepository.save(createGuestTravelAttendee(schedule, member2, AttendeePermission.READ));

        ScheduleUpdateRequest request = createUpdateScheduleRequest(
                "수정 테스트",
                LocalDate.now(),
                LocalDate.now().plusDays(1),
                createRouteRequest(1, placeWithThumbnail1.getPlaceId())
        );

        mockAuthentication(member3);

        // when, then
        mockMvc.perform(patch("/api/schedules/{scheduleId}", schedule.getScheduleId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.FORBIDDEN_ACCESS_SCHEDULE.getMessage()));
    }


    @Test
    @DisplayName("일정 수정 시 수정 권한이 없어 예외 발생")
    void updateSchedule_forbiddenEdit() throws Exception {
        // given
        TravelSchedule schedule = travelScheduleRepository.save(createTravelSchedule("테스트1"));

        travelAttendeeRepository.save(createAuthorTravelAttendee(schedule, member1));
        travelAttendeeRepository.save(createGuestTravelAttendee(schedule, member2, AttendeePermission.READ));

        ScheduleUpdateRequest request = createUpdateScheduleRequest(
                "수정 테스트",
                LocalDate.now(),
                LocalDate.now().plusDays(1),
                createRouteRequest(1, placeWithThumbnail1.getPlaceId())
        );

        mockAuthentication(member2);

        // when, then
        mockMvc.perform(patch("/api/schedules/{scheduleId}", schedule.getScheduleId())
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
        TravelSchedule schedule = travelScheduleRepository.save(createTravelSchedule("테스트1"));

        travelAttendeeRepository.save(createAuthorTravelAttendee(schedule, member1));
        travelAttendeeRepository.save(createGuestTravelAttendee(schedule, member2, AttendeePermission.READ));

        travelRouteRepository.save(createTravelRoute(schedule, placeWithThumbnail1, 1));
        travelRouteRepository.save(createTravelRoute(schedule, placeWithThumbnail2, 2));

        chatMessageRepository.save(createChatMessage(schedule.getScheduleId(), member1.getMemberId(), "hello1"));
        chatMessageRepository.save(createChatMessage(schedule.getScheduleId(), member1.getMemberId(), "hello2"));
        chatMessageRepository.save(createChatMessage(schedule.getScheduleId(), member2.getMemberId(), "hello3"));

        mockAuthentication(member1);

        // when, then
        mockMvc.perform(delete("/api/schedules/{scheduleId}", schedule.getScheduleId()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(SuccessCode.GENERAL_SUCCESS.getMessage()));

    }

    @Test
    @DisplayName("일정 삭제 시 채팅 메시지 데이터 없는 경우")
    void deleteSchedule_noChatMessageData() throws Exception {
        // given
        TravelSchedule schedule = travelScheduleRepository.save(createTravelSchedule("테스트1"));

        travelAttendeeRepository.save(createAuthorTravelAttendee(schedule, member1));
        travelAttendeeRepository.save(createGuestTravelAttendee(schedule, member2, AttendeePermission.READ));

        travelRouteRepository.save(createTravelRoute(schedule, placeWithThumbnail1, 1));
        travelRouteRepository.save(createTravelRoute(schedule, placeWithThumbnail2, 2));

        mockAuthentication(member1);

        // when, then
        mockMvc.perform(delete("/api/schedules/{scheduleId}", schedule.getScheduleId()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(SuccessCode.GENERAL_SUCCESS.getMessage()));

    }

    @Test
    @DisplayName("일정 삭제 시 삭제 권한이 없는 회원 요청으로 예외 발생")
    void deleteSchedule_forbiddenSchedule() throws Exception {
        // given
        TravelSchedule schedule = travelScheduleRepository.save(createTravelSchedule("테스트1"));

        travelAttendeeRepository.save(createAuthorTravelAttendee(schedule, member1));
        travelAttendeeRepository.save(createGuestTravelAttendee(schedule, member2, AttendeePermission.READ));

        travelRouteRepository.save(createTravelRoute(schedule, placeWithThumbnail1, 1));
        travelRouteRepository.save(createTravelRoute(schedule, placeWithThumbnail2, 2));

        mockAuthentication(member2);

        // when, then
        mockMvc.perform(delete("/api/schedules/{scheduleId}", schedule.getScheduleId()))
                .andDo(print())
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.FORBIDDEN_DELETE_SCHEDULE.getMessage()));

    }

    @Test
    @DisplayName("일정 상세 조회 내에 여행지 조회")
    void getTravelPlaces() throws Exception {
        // given
        TravelSchedule schedule = travelScheduleRepository.save(createTravelSchedule("테스트1"));
        travelAttendeeRepository.save(createAuthorTravelAttendee(schedule, member1));

        mockAuthentication(member1);

        // when, then
        mockMvc.perform(get("/api/schedules/{scheduleId}/travels", schedule.getScheduleId())
                        .param("page", "1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalElements").value(3))
                .andExpect(jsonPath("$.data.content[0].district").value(placeWithThumbnail2.getDistrict().getDistrictName()))
                .andExpect(jsonPath("$.data.content[0].placeName").value(placeWithThumbnail2.getPlaceName()))
                .andExpect(jsonPath("$.data.content[0].longitude").exists())
                .andExpect(jsonPath("$.data.content[0].latitude").exists())
                .andExpect(jsonPath("$.data.content[0].thumbnailUrl").value(placeWithThumbnail2.getThumbnailUrl()));
    }

    @Test
    @DisplayName("일정 상세 조회 내 여행지 조회 시 여행지 데이터 존재하지 않는 경우")
    void getTravelPlacesWithoutData() throws Exception {
        // given
        travelImageRepository.deleteAll();
        travelPlaceRepository.deleteAll();

        TravelSchedule schedule = travelScheduleRepository.save(createTravelSchedule("테스트1"));
        travelAttendeeRepository.save(createAuthorTravelAttendee(schedule, member1));

        mockAuthentication(member1);

        // when, then
        mockMvc.perform(get("/api/schedules/{scheduleId}/travels", schedule.getScheduleId())
                        .param("page", "1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalElements").value(0))
                .andExpect(jsonPath("$.data.content").isEmpty());
    }

    @Test
    @DisplayName("일정 상세 조회 내 여행지 조회 시 해당 일정에 접근 권한이 없어 예외 발생")
    void getTravelPlaces_forbiddenScheduleAccess() throws Exception {
        // given
        TravelSchedule schedule = travelScheduleRepository.save(createTravelSchedule("테스트1"));
        travelAttendeeRepository.save(createAuthorTravelAttendee(schedule, member2));

        mockAuthentication(member1);

        // when, then
        mockMvc.perform(get("/api/schedules/{scheduleId}/travels", schedule.getScheduleId())
                        .param("page", "1"))
                .andDo(print())
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.FORBIDDEN_ACCESS_SCHEDULE.getMessage()));
    }


    @Test
    @DisplayName("일정 상세 조회 내 여행지 조회 시 일정 데이터 존재하지 않아 예외 발생")
    void getTravelPlaces_scheduleNotFound() throws Exception {
        // given
        TravelSchedule schedule = travelScheduleRepository.save(createTravelSchedule("테스트1"));
        travelAttendeeRepository.save(createAuthorTravelAttendee(schedule, member1));

        mockAuthentication(member1);

        // when, then
        mockMvc.perform(get("/api/schedules/{scheduleId}/travels", 0L)
                        .param("page", "1"))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.SCHEDULE_NOT_FOUND.getMessage()));
    }


    @Test
    @DisplayName("일정 상세 조회 내 여행지 검색")
    void searchTravelPlaces() throws Exception {
        // given
        TravelSchedule schedule = travelScheduleRepository.save(createTravelSchedule("테스트1"));
        travelAttendeeRepository.save(createAuthorTravelAttendee(schedule, member1));

        mockAuthentication(member1);

        // when, then
        mockMvc.perform(get("/api/schedules/{scheduleId}/travels/search", schedule.getScheduleId())
                        .param("page", "1")
                        .param("keyword", "강남"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalElements").value(2))
                .andExpect(jsonPath("$.data.content[0].district").value(placeWithoutThumbnail.getDistrict().getDistrictName()))
                .andExpect(jsonPath("$.data.content[0].placeName").value(placeWithoutThumbnail.getPlaceName()))
                .andExpect(jsonPath("$.data.content[0].longitude").exists())
                .andExpect(jsonPath("$.data.content[0].latitude").exists())
                .andExpect(jsonPath("$.data.content[0].thumbnailUrl", nullValue()))
                .andExpect(jsonPath("$.data.content[1].district").value(placeWithThumbnail1.getDistrict().getDistrictName()))
                .andExpect(jsonPath("$.data.content[1].placeName").value(placeWithThumbnail1.getPlaceName()))
                .andExpect(jsonPath("$.data.content[1].longitude").exists())
                .andExpect(jsonPath("$.data.content[1].latitude").exists())
                .andExpect(jsonPath("$.data.content[1].thumbnailUrl").value(placeWithThumbnail1.getThumbnailUrl()))
                ;
    }

    @Test
    @DisplayName("일정 상세 조회 내 여행지 검색 시 검색 결과가 존재하지 않는 경우")
    void searchTravelPlaces_emptyResult() throws Exception {
        // given
        TravelSchedule schedule = travelScheduleRepository.save(createTravelSchedule("테스트1"));
        travelAttendeeRepository.save(createAuthorTravelAttendee(schedule, member1));

        mockAuthentication(member1);

        // when, then
        mockMvc.perform(get("/api/schedules/{scheduleId}/travels/search", schedule.getScheduleId())
                        .param("page", "1")
                        .param("keyword", "ㅁㄴㅇㄹ"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalElements").value(0))
                .andExpect(jsonPath("$.data.content").isEmpty());
    }


    @Test
    @DisplayName("일정 상세 조회 내 여행지 검색 시 일정 데이터 존재하지 않아 예외 발생")
    void searchTravelPlaces_scheduleNotFound() throws Exception {
        // given
        mockAuthentication(member1);

        // when, then
        mockMvc.perform(get("/api/schedules/{scheduleId}/travels/search", 0L)
                        .param("page", "1")
                        .param("keyword", "ㅁㄴㅇㄹ"))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.SCHEDULE_NOT_FOUND.getMessage()));
    }


    @Test
    @DisplayName("일정 상세 조회 내 여행지 검색 시 해당 일정에 접근 권한이 없어 예외 발생")
    void searchTravelPlaces_forbiddenScheduleAccess() throws Exception {
        // given
        TravelSchedule schedule = travelScheduleRepository.save(createTravelSchedule("테스트1"));
        travelAttendeeRepository.save(createAuthorTravelAttendee(schedule, member2));

        mockAuthentication(member1);

        // when, then
        mockMvc.perform(get("/api/schedules/{scheduleId}/travels/search", schedule.getScheduleId())
                        .param("page", "1")
                        .param("keyword", "중구"))
                .andDo(print())
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.FORBIDDEN_ACCESS_SCHEDULE.getMessage()));
    }

}
