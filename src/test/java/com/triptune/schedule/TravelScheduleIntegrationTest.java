package com.triptune.schedule;

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
import com.triptune.schedule.dto.request.RouteRequest;
import com.triptune.schedule.dto.request.ScheduleCreateRequest;
import com.triptune.schedule.dto.request.ScheduleUpdateRequest;
import com.triptune.schedule.entity.ChatMessage;
import com.triptune.schedule.entity.TravelRoute;
import com.triptune.schedule.entity.TravelSchedule;
import com.triptune.schedule.enums.AttendeeRole;
import com.triptune.schedule.enums.ScheduleSearchType;
import com.triptune.schedule.fixture.ChatMessageFixture;
import com.triptune.schedule.fixture.TravelAttendeeFixture;
import com.triptune.schedule.fixture.TravelRouteFixture;
import com.triptune.schedule.fixture.TravelScheduleFixture;
import com.triptune.schedule.repository.ChatMessageRepository;
import com.triptune.schedule.repository.TravelAttendeeRepository;
import com.triptune.schedule.repository.TravelRouteRepository;
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
import java.util.List;

import static com.triptune.schedule.enums.AttendeePermission.CHAT;
import static com.triptune.schedule.enums.AttendeePermission.READ;
import static com.triptune.travel.enums.ThemeType.ATTRACTIONS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional
@AutoConfigureMockMvc
@ActiveProfiles("mongo")
public class TravelScheduleIntegrationTest {

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
    @Autowired private ProfileImageRepository profileImageRepository;
    @Autowired private ChatMessageRepository chatMessageRepository;
    @Autowired private ApiContentTypeRepository apiContentTypeRepository;
    @Autowired private EntityManager em;


    private Member member1;
    private Member member2;
    private Member member3;

    private TravelPlace place1WithThumb;
    private TravelPlace place2WithThumb;
    private TravelPlace placeWithoutThumb;

    String place1ThumbUrl;
    String place2ThumbUrl;

    @BeforeEach
    void setUp(){
        chatMessageRepository.deleteAll();

        ProfileImage profileImage1 = profileImageRepository.save(ProfileImageFixture.createProfileImage("member1Image"));
        member1 = memberRepository.save(MemberFixture.createNativeTypeMember("member1@email.com", profileImage1));

        ProfileImage profileImage2 = profileImageRepository.save(ProfileImageFixture.createProfileImage("member2Image"));
        member2 = memberRepository.save(MemberFixture.createNativeTypeMember("member2@email.com", profileImage2));

        ProfileImage profileImage3 = profileImageRepository.save(ProfileImageFixture.createProfileImage("member3Image"));
        member3 = memberRepository.save(MemberFixture.createNativeTypeMember("member3@email.com", profileImage3));

        Country country = countryRepository.save(CountryFixture.createCountry());
        City city = cityRepository.save(CityFixture.createSeoul(country));
        District gangnam = districtRepository.save(DistrictFixture.createDistrict(city, "강남구"));
        District jungGu = districtRepository.save(DistrictFixture.createDistrict(city, "중구"));
        ApiContentType apiContentType = apiContentTypeRepository.save(ApiContentTypeFixture.createApiContentType(ATTRACTIONS));

        place1WithThumb = travelPlaceRepository.save(
                TravelPlaceFixture.createTravelPlaceWithLocation(
                        country,
                        city,
                        gangnam,
                        apiContentType,
                        "여행지1",
                        37.49850,
                        127.02820
                )
        );
        TravelImage place1Thumb = travelImageRepository.save(TravelImageFixture.createTravelImage(place1WithThumb, "test1", true));
        travelImageRepository.save(TravelImageFixture.createTravelImage(place1WithThumb, "test2", false));
        place1ThumbUrl = S3Fixture.createS3ObjectUrl(place1Thumb.getS3ObjectKey());

        place2WithThumb = travelPlaceRepository.save(
                TravelPlaceFixture.createTravelPlaceWithLocation(
                        country,
                        city,
                        jungGu,
                        apiContentType,
                        "여행지2",
                        37.56420,
                        126.99800
                )
        );
        TravelImage place2Thumb = travelImageRepository.save(TravelImageFixture.createTravelImage(place2WithThumb, "test1", true));
        travelImageRepository.save(TravelImageFixture.createTravelImage(place2WithThumb, "test2", false));
        place2ThumbUrl = S3Fixture.createS3ObjectUrl(place2Thumb.getS3ObjectKey());

        placeWithoutThumb = travelPlaceRepository.save(
                TravelPlaceFixture.createTravelPlaceWithLocation(
                        country,
                        city,
                        gangnam,
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
        TravelSchedule schedule1 = travelScheduleRepository.save(TravelScheduleFixture.createSchedule("테스트1"));
        travelAttendeeRepository.save(TravelAttendeeFixture.createAuthorAttendee(schedule1, member1));
        travelAttendeeRepository.save(TravelAttendeeFixture.createGuestAttendee(schedule1, member2, READ));
        travelRouteRepository.save(TravelRouteFixture.createRoute(schedule1, place1WithThumb, 1));
        travelRouteRepository.save(TravelRouteFixture.createRoute(schedule1, placeWithoutThumb, 2));
        travelRouteRepository.save(TravelRouteFixture.createRoute(schedule1, place2WithThumb, 3));

        TravelSchedule schedule2 = travelScheduleRepository.save(TravelScheduleFixture.createSchedule("테스트2"));
        travelAttendeeRepository.save(TravelAttendeeFixture.createAuthorAttendee(schedule2, member2));
        travelAttendeeRepository.save(TravelAttendeeFixture.createGuestAttendee(schedule2, member1, CHAT));

        TravelSchedule schedule3 = travelScheduleRepository.save(TravelScheduleFixture.createSchedule("테스트3"));
        travelAttendeeRepository.save(TravelAttendeeFixture.createAuthorAttendee(schedule3, member1));

        SecurityTestUtils.mockAuthentication(member1);

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
                .andExpect(jsonPath("$.data.content[2].thumbnailUrl").value(place1ThumbUrl));
    }

    @Test
    @DisplayName("전체 일정 목록 조회 시 데이터 없는 경우")
    void getAllSchedules_emptyResult() throws Exception {
        // given
        SecurityTestUtils.mockAuthentication(member1);

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
        TravelSchedule schedule1 = travelScheduleRepository.save(TravelScheduleFixture.createSchedule("테스트1"));
        travelAttendeeRepository.save(TravelAttendeeFixture.createAuthorAttendee(schedule1, member1));
        travelAttendeeRepository.save(TravelAttendeeFixture.createGuestAttendee(schedule1, member2, READ));
        travelRouteRepository.save(TravelRouteFixture.createRoute(schedule1, place1WithThumb, 1));
        travelRouteRepository.save(TravelRouteFixture.createRoute(schedule1, placeWithoutThumb, 2));

        TravelSchedule schedule2 = travelScheduleRepository.save(TravelScheduleFixture.createSchedule("테스트2"));
        travelAttendeeRepository.save(TravelAttendeeFixture.createAuthorAttendee(schedule2, member2));
        travelAttendeeRepository.save(TravelAttendeeFixture.createGuestAttendee(schedule2, member1, CHAT));
        travelRouteRepository.save(TravelRouteFixture.createRoute(schedule2, place2WithThumb, 1));

        TravelSchedule schedule3 = travelScheduleRepository.save(TravelScheduleFixture.createSchedule("테스트3"));
        travelAttendeeRepository.save(TravelAttendeeFixture.createAuthorAttendee(schedule3, member1));

        SecurityTestUtils.mockAuthentication(member1);

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
                .andExpect(jsonPath("$.data.content[0].thumbnailUrl").value(place2ThumbUrl))
                .andExpect(jsonPath("$.data.content[1].scheduleName").value(schedule1.getScheduleName()))
                .andExpect(jsonPath("$.data.content[1].sinceUpdate").exists())
                .andExpect(jsonPath("$.data.content[1].author.nickname").value(member1.getNickname()))
                .andExpect(jsonPath("$.data.content[1].thumbnailUrl").value(place1ThumbUrl));
    }

    @Test
    @DisplayName("공유된 일정 목록 조회 시 공유된 일정 없는 경우")
    void getSharedSchedules_withoutShared() throws Exception {
        // given
        TravelSchedule schedule1 = travelScheduleRepository.save(TravelScheduleFixture.createSchedule("테스트1"));
        travelAttendeeRepository.save(TravelAttendeeFixture.createAuthorAttendee(schedule1, member1));
        travelRouteRepository.save(TravelRouteFixture.createRoute(schedule1, place1WithThumb, 1));
        travelRouteRepository.save(TravelRouteFixture.createRoute(schedule1, placeWithoutThumb, 2));

        TravelSchedule schedule2 = travelScheduleRepository.save(TravelScheduleFixture.createSchedule("테스트2"));
        travelAttendeeRepository.save(TravelAttendeeFixture.createAuthorAttendee(schedule2, member2));
        travelRouteRepository.save(TravelRouteFixture.createRoute(schedule2, place2WithThumb, 1));

        TravelSchedule schedule3 = travelScheduleRepository.save(TravelScheduleFixture.createSchedule("테스트3"));
        travelAttendeeRepository.save(TravelAttendeeFixture.createAuthorAttendee(schedule3, member1));

        SecurityTestUtils.mockAuthentication(member1);

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
        SecurityTestUtils.mockAuthentication(member1);

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
        TravelSchedule schedule1 = travelScheduleRepository.save(TravelScheduleFixture.createSchedule("테스트1"));
        travelAttendeeRepository.save(TravelAttendeeFixture.createAuthorAttendee(schedule1, member1));
        travelAttendeeRepository.save(TravelAttendeeFixture.createGuestAttendee(schedule1, member2, READ));
        travelRouteRepository.save(TravelRouteFixture.createRoute(schedule1, place1WithThumb, 1));
        travelRouteRepository.save(TravelRouteFixture.createRoute(schedule1, placeWithoutThumb, 2));

        TravelSchedule schedule2 = travelScheduleRepository.save(TravelScheduleFixture.createSchedule("테스트2"));
        travelAttendeeRepository.save(TravelAttendeeFixture.createAuthorAttendee(schedule2, member2));
        travelAttendeeRepository.save(TravelAttendeeFixture.createGuestAttendee(schedule2, member1, CHAT));
        travelRouteRepository.save(TravelRouteFixture.createRoute(schedule2, place2WithThumb, 1));

        TravelSchedule schedule3 = travelScheduleRepository.save(TravelScheduleFixture.createSchedule("테스트3"));
        travelAttendeeRepository.save(TravelAttendeeFixture.createAuthorAttendee(schedule3, member1));

        SecurityTestUtils.mockAuthentication(member1);

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
    void getEnableEditSchedules_emptyResult() throws Exception {
        // given
        SecurityTestUtils.mockAuthentication(member1);

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
        TravelSchedule schedule1 = travelScheduleRepository.save(TravelScheduleFixture.createSchedule("테스트1"));
        travelAttendeeRepository.save(TravelAttendeeFixture.createAuthorAttendee(schedule1, member1));
        travelAttendeeRepository.save(TravelAttendeeFixture.createGuestAttendee(schedule1, member2, READ));
        travelRouteRepository.save(TravelRouteFixture.createRoute(schedule1, place1WithThumb, 1));
        travelRouteRepository.save(TravelRouteFixture.createRoute(schedule1, placeWithoutThumb, 2));
        travelRouteRepository.save(TravelRouteFixture.createRoute(schedule1, place2WithThumb, 3));

        TravelSchedule schedule2 = travelScheduleRepository.save(TravelScheduleFixture.createSchedule("테스트2"));
        travelAttendeeRepository.save(TravelAttendeeFixture.createAuthorAttendee(schedule2, member2));
        travelAttendeeRepository.save(TravelAttendeeFixture.createGuestAttendee(schedule2, member1, CHAT));

        TravelSchedule schedule3 = travelScheduleRepository.save(TravelScheduleFixture.createSchedule("검색 안됨"));
        travelAttendeeRepository.save(TravelAttendeeFixture.createAuthorAttendee(schedule3, member1));

        SecurityTestUtils.mockAuthentication(member1);

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
                .andExpect(jsonPath("$.data.content.length()").value(2))
                .andExpect(jsonPath("$.data.content[0].scheduleName").value(schedule2.getScheduleName()))
                .andExpect(jsonPath("$.data.content[0].sinceUpdate").exists())
                .andExpect(jsonPath("$.data.content[0].thumbnailUrl", nullValue()))
                .andExpect(jsonPath("$.data.content[0].author.nickname").value(member2.getNickname()))
                .andExpect(jsonPath("$.data.content[0].role").value(AttendeeRole.GUEST.name()))
                .andExpect(jsonPath("$.data.content[1].scheduleName").value(schedule1.getScheduleName()))
                .andExpect(jsonPath("$.data.content[1].sinceUpdate").exists())
                .andExpect(jsonPath("$.data.content[1].thumbnailUrl").value(place2ThumbUrl))
                .andExpect(jsonPath("$.data.content[1].author.nickname").value(member1.getNickname()))
                .andExpect(jsonPath("$.data.content[1].role").value(AttendeeRole.AUTHOR.name()));
    }

    @Test
    @DisplayName("전체 일정 검색 시 공유된 일정 없는 경우")
    void searchSchedules_emptyShared() throws Exception {
        // given
        TravelSchedule schedule1 = travelScheduleRepository.save(TravelScheduleFixture.createSchedule("테스트1"));
        travelAttendeeRepository.save(TravelAttendeeFixture.createAuthorAttendee(schedule1, member1));
        travelRouteRepository.save(TravelRouteFixture.createRoute(schedule1, place1WithThumb, 1));
        travelRouteRepository.save(TravelRouteFixture.createRoute(schedule1, placeWithoutThumb, 2));
        travelRouteRepository.save(TravelRouteFixture.createRoute(schedule1, place2WithThumb, 3));

        TravelSchedule schedule2 = travelScheduleRepository.save(TravelScheduleFixture.createSchedule("테스트2"));
        travelAttendeeRepository.save(TravelAttendeeFixture.createAuthorAttendee(schedule2, member2));

        TravelSchedule schedule3 = travelScheduleRepository.save(TravelScheduleFixture.createSchedule("테스트3"));
        travelAttendeeRepository.save(TravelAttendeeFixture.createAuthorAttendee(schedule3, member1));

        SecurityTestUtils.mockAuthentication(member1);

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
                .andExpect(jsonPath("$.data.content[1].thumbnailUrl").value(place2ThumbUrl))
                .andExpect(jsonPath("$.data.content[1].author.nickname").value(member1.getNickname()))
                .andExpect(jsonPath("$.data.content[1].role").value(AttendeeRole.AUTHOR.name()));
    }

    @Test
    @DisplayName("전체 일정 검색 시 검색 결과 없는 경우")
    void searchSchedules_emptyResult() throws Exception {
        // given
        TravelSchedule schedule1 = travelScheduleRepository.save(TravelScheduleFixture.createSchedule("테스트1"));
        travelAttendeeRepository.save(TravelAttendeeFixture.createAuthorAttendee(schedule1, member1));
        travelAttendeeRepository.save(TravelAttendeeFixture.createGuestAttendee(schedule1, member2, READ));
        travelRouteRepository.save(TravelRouteFixture.createRoute(schedule1, place1WithThumb, 1));
        travelRouteRepository.save(TravelRouteFixture.createRoute(schedule1, placeWithoutThumb, 2));
        travelRouteRepository.save(TravelRouteFixture.createRoute(schedule1, place2WithThumb, 3));

        TravelSchedule schedule2 = travelScheduleRepository.save(TravelScheduleFixture.createSchedule("테스트2"));
        travelAttendeeRepository.save(TravelAttendeeFixture.createAuthorAttendee(schedule2, member2));
        travelAttendeeRepository.save(TravelAttendeeFixture.createGuestAttendee(schedule2, member1, CHAT));

        TravelSchedule schedule3 = travelScheduleRepository.save(TravelScheduleFixture.createSchedule("테스트3"));
        travelAttendeeRepository.save(TravelAttendeeFixture.createAuthorAttendee(schedule3, member1));

        SecurityTestUtils.mockAuthentication(member1);

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
        TravelSchedule schedule1 = travelScheduleRepository.save(TravelScheduleFixture.createSchedule("테스트1"));
        travelAttendeeRepository.save(TravelAttendeeFixture.createAuthorAttendee(schedule1, member1));
        travelAttendeeRepository.save(TravelAttendeeFixture.createGuestAttendee(schedule1, member2, READ));
        travelRouteRepository.save(TravelRouteFixture.createRoute(schedule1, place1WithThumb, 1));
        travelRouteRepository.save(TravelRouteFixture.createRoute(schedule1, placeWithoutThumb, 2));
        travelRouteRepository.save(TravelRouteFixture.createRoute(schedule1, place2WithThumb, 3));

        TravelSchedule schedule2 = travelScheduleRepository.save(TravelScheduleFixture.createSchedule("테스트2"));
        travelAttendeeRepository.save(TravelAttendeeFixture.createAuthorAttendee(schedule2, member2));
        travelAttendeeRepository.save(TravelAttendeeFixture.createGuestAttendee(schedule2, member1, CHAT));

        TravelSchedule schedule3 = travelScheduleRepository.save(TravelScheduleFixture.createSchedule("테스트3"));
        travelAttendeeRepository.save(TravelAttendeeFixture.createAuthorAttendee(schedule3, member1));

        SecurityTestUtils.mockAuthentication(member1);

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
                .andExpect(jsonPath("$.data.content[1].thumbnailUrl").value(place2ThumbUrl))
                .andExpect(jsonPath("$.data.content[1].author.nickname").value(member1.getNickname()))
                .andExpect(jsonPath("$.data.content[1].role").value(AttendeeRole.AUTHOR.name()));
    }


    @Test
    @DisplayName("공유된 일정 검색 시 검색 결과 없는 경우")
    void searchSharedSchedules_emptyResult() throws Exception {
        // given
        TravelSchedule schedule1 = travelScheduleRepository.save(TravelScheduleFixture.createSchedule("테스트1"));
        travelAttendeeRepository.save(TravelAttendeeFixture.createAuthorAttendee(schedule1, member1));
        travelAttendeeRepository.save(TravelAttendeeFixture.createGuestAttendee(schedule1, member2, READ));
        travelRouteRepository.save(TravelRouteFixture.createRoute(schedule1, place1WithThumb, 1));
        travelRouteRepository.save(TravelRouteFixture.createRoute(schedule1, placeWithoutThumb, 2));
        travelRouteRepository.save(TravelRouteFixture.createRoute(schedule1, place2WithThumb, 3));

        TravelSchedule schedule2 = travelScheduleRepository.save(TravelScheduleFixture.createSchedule("테스트2"));
        travelAttendeeRepository.save(TravelAttendeeFixture.createAuthorAttendee(schedule2, member2));
        travelAttendeeRepository.save(TravelAttendeeFixture.createGuestAttendee(schedule2, member1, CHAT));

        TravelSchedule schedule3 = travelScheduleRepository.save(TravelScheduleFixture.createSchedule("테스트3"));
        travelAttendeeRepository.save(TravelAttendeeFixture.createAuthorAttendee(schedule3, member1));


        SecurityTestUtils.mockAuthentication(member1);

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
        ScheduleCreateRequest request = TravelScheduleFixture.createScheduleRequest(
                "테스트",
                LocalDate.now(),
                LocalDate.now()
        );
        SecurityTestUtils.mockAuthentication(member1);

        // when
        mockMvc.perform(post("/api/schedules")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(SuccessCode.GENERAL_SUCCESS.getMessage()));

        // then
        em.flush();
        em.clear();

        TravelSchedule createdSchedule = travelScheduleRepository.findAll().get(0);
        assertThat(createdSchedule.getScheduleName()).isEqualTo(request.getScheduleName());
        assertThat(createdSchedule.getStartDate()).isEqualTo(request.getStartDate());
    }


    @Test
    @DisplayName("일정 상세 조회")
    void getScheduleDetail() throws Exception {
        // given
        TravelSchedule schedule = travelScheduleRepository.save(TravelScheduleFixture.createSchedule("테스트1"));

        travelAttendeeRepository.save(TravelAttendeeFixture.createAuthorAttendee(schedule, member1));
        travelAttendeeRepository.save(TravelAttendeeFixture.createGuestAttendee(schedule, member2, READ));

        travelRouteRepository.save(TravelRouteFixture.createRoute(schedule, place1WithThumb, 1));
        travelRouteRepository.save(TravelRouteFixture.createRoute(schedule, placeWithoutThumb, 2));
        travelRouteRepository.save(TravelRouteFixture.createRoute(schedule, place2WithThumb, 3));

        SecurityTestUtils.mockAuthentication(member1);

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
                .andExpect(jsonPath("$.data.placeList.content[1].placeName").value(place1WithThumb.getPlaceName()))
                .andExpect(jsonPath("$.data.placeList.content[1].thumbnailUrl").value(place1ThumbUrl))
                .andExpect(jsonPath("$.data.placeList.content[2].placeName").value(placeWithoutThumb.getPlaceName()))
                .andExpect(jsonPath("$.data.placeList.content[2].thumbnailUrl", nullValue()));
    }

    @Test
    @DisplayName("일정 상세 조회 시 여행지 데이터 존재하지 않는 경우")
    void getScheduleDetail_emptyPlaces() throws Exception {
        // given
        travelImageRepository.deleteAll();
        travelPlaceRepository.deleteAll();

        TravelSchedule schedule = travelScheduleRepository.save(TravelScheduleFixture.createSchedule("테스트1"));

        travelAttendeeRepository.save(TravelAttendeeFixture.createAuthorAttendee(schedule, member1));
        travelAttendeeRepository.save(TravelAttendeeFixture.createGuestAttendee(schedule, member2, READ));

        SecurityTestUtils.mockAuthentication(member1);

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
        SecurityTestUtils.mockAuthentication(member1);

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
        TravelSchedule schedule = travelScheduleRepository.save(TravelScheduleFixture.createSchedule("테스트2"));
        travelAttendeeRepository.save(TravelAttendeeFixture.createAuthorAttendee(schedule, member2));

        SecurityTestUtils.mockAuthentication(member1);

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
        TravelSchedule schedule = travelScheduleRepository.save(TravelScheduleFixture.createSchedule("테스트1"));

        travelAttendeeRepository.save(TravelAttendeeFixture.createAuthorAttendee(schedule, member1));
        travelAttendeeRepository.save(TravelAttendeeFixture.createGuestAttendee(schedule, member2, READ));

        travelRouteRepository.save(TravelRouteFixture.createRoute(schedule, place1WithThumb, 1));
        travelRouteRepository.save(TravelRouteFixture.createRoute(schedule, placeWithoutThumb, 2));
        travelRouteRepository.save(TravelRouteFixture.createRoute(schedule, place2WithThumb, 3));

        RouteRequest routeRequest1 = TravelRouteFixture.createRouteRequest(1, place2WithThumb.getPlaceId());
        RouteRequest routeRequest2 = TravelRouteFixture.createRouteRequest(2, placeWithoutThumb.getPlaceId());

        ScheduleUpdateRequest request = TravelScheduleFixture.createUpdateScheduleRequest(
                "수정 테스트",
                LocalDate.now().minusDays(2),
                LocalDate.now().minusDays(1),
                routeRequest1,
                routeRequest2
        );

        SecurityTestUtils.mockAuthentication(member1);

        // when
        mockMvc.perform(patch("/api/schedules/{scheduleId}", schedule.getScheduleId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(SuccessCode.GENERAL_SUCCESS.getMessage()));

        // then
        em.flush();
        em.clear();

        TravelSchedule updatedSchedule = travelScheduleRepository.findAll().get(0);
        assertThat(updatedSchedule.getScheduleName()).isEqualTo(request.getScheduleName());
        assertThat(updatedSchedule.getStartDate()).isEqualTo(request.getStartDate());
        assertThat(updatedSchedule.getEndDate()).isEqualTo(request.getEndDate());
        assertThat(updatedSchedule.getTravelRoutes()).hasSize(2);
        assertThat(updatedSchedule.getTravelRoutes())
                .extracting(TravelRoute::getRouteOrder)
                .containsExactly(1, 2);

    }


    @Test
    @DisplayName("일정 수정 시 여행 루트 없는 경우")
    void updateSchedule_emptyRoutes() throws Exception {
        // given
        TravelSchedule schedule = travelScheduleRepository.save(TravelScheduleFixture.createSchedule("테스트1"));

        travelAttendeeRepository.save(TravelAttendeeFixture.createAuthorAttendee(schedule, member1));
        travelAttendeeRepository.save(TravelAttendeeFixture.createGuestAttendee(schedule, member2, READ));

        ScheduleUpdateRequest request = TravelScheduleFixture.createUpdateScheduleRequest(
                "수정 테스트",
                LocalDate.now(),
                LocalDate.now().plusDays(1)
        );

        SecurityTestUtils.mockAuthentication(member1);

        // when
        mockMvc.perform(patch("/api/schedules/{scheduleId}", schedule.getScheduleId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(SuccessCode.GENERAL_SUCCESS.getMessage()));

        // then
        em.flush();
        em.clear();

        TravelSchedule updatedSchedule = travelScheduleRepository.findAll().get(0);
        assertThat(updatedSchedule.getScheduleName()).isEqualTo(request.getScheduleName());
        assertThat(updatedSchedule.getStartDate()).isEqualTo(request.getStartDate());
        assertThat(updatedSchedule.getEndDate()).isEqualTo(request.getEndDate());
        assertThat(updatedSchedule.getTravelRoutes()).hasSize(0);
    }


    @Test
    @DisplayName("일정 수정 시 접근 권한이 없어 예외 발생")
    void updateSchedule_forbiddenAccess() throws Exception {
        // given
        TravelSchedule schedule = travelScheduleRepository.save(TravelScheduleFixture.createSchedule("테스트1"));

        travelAttendeeRepository.save(TravelAttendeeFixture.createAuthorAttendee(schedule, member1));
        travelAttendeeRepository.save(TravelAttendeeFixture.createGuestAttendee(schedule, member2, READ));

        ScheduleUpdateRequest request = TravelScheduleFixture.createUpdateScheduleRequest(
                "수정 테스트",
                LocalDate.now(),
                LocalDate.now().plusDays(1),
                TravelRouteFixture.createRouteRequest(1, place1WithThumb.getPlaceId())
        );

        SecurityTestUtils.mockAuthentication(member3);

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
        TravelSchedule schedule = travelScheduleRepository.save(TravelScheduleFixture.createSchedule("테스트1"));

        travelAttendeeRepository.save(TravelAttendeeFixture.createAuthorAttendee(schedule, member1));
        travelAttendeeRepository.save(TravelAttendeeFixture.createGuestAttendee(schedule, member2, READ));

        ScheduleUpdateRequest request = TravelScheduleFixture.createUpdateScheduleRequest(
                "수정 테스트",
                LocalDate.now(),
                LocalDate.now().plusDays(1),
                TravelRouteFixture.createRouteRequest(1, place1WithThumb.getPlaceId())
        );

        SecurityTestUtils.mockAuthentication(member2);

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
        TravelSchedule schedule = travelScheduleRepository.save(TravelScheduleFixture.createSchedule("테스트1"));

        travelAttendeeRepository.save(TravelAttendeeFixture.createAuthorAttendee(schedule, member1));
        travelAttendeeRepository.save(TravelAttendeeFixture.createGuestAttendee(schedule, member2, READ));

        travelRouteRepository.save(TravelRouteFixture.createRoute(schedule, place1WithThumb, 1));
        travelRouteRepository.save(TravelRouteFixture.createRoute(schedule, place2WithThumb, 2));

        chatMessageRepository.save(ChatMessageFixture.createChatMessage(schedule.getScheduleId(), member1.getMemberId(), "hello1"));
        chatMessageRepository.save(ChatMessageFixture.createChatMessage(schedule.getScheduleId(), member1.getMemberId(), "hello2"));
        chatMessageRepository.save(ChatMessageFixture.createChatMessage(schedule.getScheduleId(), member2.getMemberId(), "hello3"));

        SecurityTestUtils.mockAuthentication(member1);

        // when
        mockMvc.perform(delete("/api/schedules/{scheduleId}", schedule.getScheduleId()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(SuccessCode.GENERAL_SUCCESS.getMessage()));

        // then
        em.flush();
        em.clear();

        List<TravelSchedule> deletedSchedule = travelScheduleRepository.findAll();
        assertThat(deletedSchedule).isEmpty();
        List<ChatMessage> deletedChatMessage = chatMessageRepository.findAllByScheduleId(schedule.getScheduleId());
        assertThat(deletedChatMessage).isEmpty();
    }

    @Test
    @DisplayName("일정 삭제 시 채팅 메시지 데이터 없는 경우")
    void deleteSchedule_noChatMessageData() throws Exception {
        // given
        TravelSchedule schedule = travelScheduleRepository.save(TravelScheduleFixture.createSchedule("테스트1"));

        travelAttendeeRepository.save(TravelAttendeeFixture.createAuthorAttendee(schedule, member1));
        travelAttendeeRepository.save(TravelAttendeeFixture.createGuestAttendee(schedule, member2, READ));

        travelRouteRepository.save(TravelRouteFixture.createRoute(schedule, place1WithThumb, 1));
        travelRouteRepository.save(TravelRouteFixture.createRoute(schedule, place2WithThumb, 2));

        SecurityTestUtils.mockAuthentication(member1);

        // when
        mockMvc.perform(delete("/api/schedules/{scheduleId}", schedule.getScheduleId()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(SuccessCode.GENERAL_SUCCESS.getMessage()));

        // then
        em.flush();
        em.clear();

        List<TravelSchedule> deletedSchedule = travelScheduleRepository.findAll();
        assertThat(deletedSchedule).isEmpty();
    }

    @Test
    @DisplayName("일정 삭제 시 삭제 권한이 없는 회원 요청으로 예외 발생")
    void deleteSchedule_forbiddenSchedule() throws Exception {
        // given
        TravelSchedule schedule = travelScheduleRepository.save(TravelScheduleFixture.createSchedule("테스트1"));

        travelAttendeeRepository.save(TravelAttendeeFixture.createAuthorAttendee(schedule, member1));
        travelAttendeeRepository.save(TravelAttendeeFixture.createGuestAttendee(schedule, member2, READ));

        travelRouteRepository.save(TravelRouteFixture.createRoute(schedule, place1WithThumb, 1));
        travelRouteRepository.save(TravelRouteFixture.createRoute(schedule, place2WithThumb, 2));

        SecurityTestUtils.mockAuthentication(member2);

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
        TravelSchedule schedule = travelScheduleRepository.save(TravelScheduleFixture.createSchedule("테스트1"));
        travelAttendeeRepository.save(TravelAttendeeFixture.createAuthorAttendee(schedule, member1));

        SecurityTestUtils.mockAuthentication(member1);

        // when, then
        mockMvc.perform(get("/api/schedules/{scheduleId}/travels", schedule.getScheduleId())
                        .param("page", "1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalElements").value(3))
                .andExpect(jsonPath("$.data.content[0].district").value(place2WithThumb.getDistrict().getDistrictName()))
                .andExpect(jsonPath("$.data.content[0].placeName").value(place2WithThumb.getPlaceName()))
                .andExpect(jsonPath("$.data.content[0].longitude").exists())
                .andExpect(jsonPath("$.data.content[0].latitude").exists())
                .andExpect(jsonPath("$.data.content[0].thumbnailUrl").value(place2ThumbUrl));
    }

    @Test
    @DisplayName("일정 상세 조회 내 여행지 조회 시 여행지 데이터 존재하지 않는 경우")
    void getTravelPlacesWithoutData() throws Exception {
        // given
        travelImageRepository.deleteAll();
        travelPlaceRepository.deleteAll();

        TravelSchedule schedule = travelScheduleRepository.save(TravelScheduleFixture.createSchedule("테스트1"));
        travelAttendeeRepository.save(TravelAttendeeFixture.createAuthorAttendee(schedule, member1));

        SecurityTestUtils.mockAuthentication(member1);

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
        TravelSchedule schedule = travelScheduleRepository.save(TravelScheduleFixture.createSchedule("테스트1"));
        travelAttendeeRepository.save(TravelAttendeeFixture.createAuthorAttendee(schedule, member2));

        SecurityTestUtils.mockAuthentication(member1);

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
        TravelSchedule schedule = travelScheduleRepository.save(TravelScheduleFixture.createSchedule("테스트1"));
        travelAttendeeRepository.save(TravelAttendeeFixture.createAuthorAttendee(schedule, member1));

        SecurityTestUtils.mockAuthentication(member1);

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
        TravelSchedule schedule = travelScheduleRepository.save(TravelScheduleFixture.createSchedule("테스트1"));
        travelAttendeeRepository.save(TravelAttendeeFixture.createAuthorAttendee(schedule, member1));

        SecurityTestUtils.mockAuthentication(member1);

        // when, then
        mockMvc.perform(get("/api/schedules/{scheduleId}/travels/search", schedule.getScheduleId())
                        .param("page", "1")
                        .param("keyword", "강남"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalElements").value(2))
                .andExpect(jsonPath("$.data.content[0].district").value(placeWithoutThumb.getDistrict().getDistrictName()))
                .andExpect(jsonPath("$.data.content[0].placeName").value(placeWithoutThumb.getPlaceName()))
                .andExpect(jsonPath("$.data.content[0].longitude").exists())
                .andExpect(jsonPath("$.data.content[0].latitude").exists())
                .andExpect(jsonPath("$.data.content[0].thumbnailUrl", nullValue()))
                .andExpect(jsonPath("$.data.content[1].district").value(place1WithThumb.getDistrict().getDistrictName()))
                .andExpect(jsonPath("$.data.content[1].placeName").value(place1WithThumb.getPlaceName()))
                .andExpect(jsonPath("$.data.content[1].longitude").exists())
                .andExpect(jsonPath("$.data.content[1].latitude").exists())
                .andExpect(jsonPath("$.data.content[1].thumbnailUrl").value(place1ThumbUrl))
                ;
    }

    @Test
    @DisplayName("일정 상세 조회 내 여행지 검색 시 검색 결과가 존재하지 않는 경우")
    void searchTravelPlaces_emptyResult() throws Exception {
        // given
        TravelSchedule schedule = travelScheduleRepository.save(TravelScheduleFixture.createSchedule("테스트1"));
        travelAttendeeRepository.save(TravelAttendeeFixture.createAuthorAttendee(schedule, member1));

        SecurityTestUtils.mockAuthentication(member1);

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
        SecurityTestUtils.mockAuthentication(member1);

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
        TravelSchedule schedule = travelScheduleRepository.save(TravelScheduleFixture.createSchedule("테스트1"));
        travelAttendeeRepository.save(TravelAttendeeFixture.createAuthorAttendee(schedule, member2));

        SecurityTestUtils.mockAuthentication(member1);

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
