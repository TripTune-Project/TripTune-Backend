package com.triptune.member;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.triptune.bookmark.fixture.BookmarkFixture;
import com.triptune.bookmark.repository.BookmarkRepository;
import com.triptune.common.entity.ApiContentType;
import com.triptune.common.entity.City;
import com.triptune.common.entity.Country;
import com.triptune.common.entity.District;
import com.triptune.common.fixture.*;
import com.triptune.common.repository.ApiContentTypeRepository;
import com.triptune.common.repository.CityRepository;
import com.triptune.common.repository.CountryRepository;
import com.triptune.common.repository.DistrictRepository;
import com.triptune.email.dto.request.EmailRequest;
import com.triptune.email.service.EmailService;
import com.triptune.global.message.ErrorCode;
import com.triptune.global.message.SuccessCode;
import com.triptune.global.redis.RedisService;
import com.triptune.global.s3.S3ObjectManager;
import com.triptune.global.s3.S3Service;
import com.triptune.global.security.CookieType;
import com.triptune.global.security.SecurityTestUtils;
import com.triptune.global.security.jwt.JwtUtils;
import com.triptune.member.dto.request.*;
import com.triptune.member.entity.Member;
import com.triptune.member.entity.SocialMember;
import com.triptune.member.enums.DeactivateValue;
import com.triptune.member.enums.JoinType;
import com.triptune.member.enums.SocialType;
import com.triptune.member.fixture.MemberFixture;
import com.triptune.member.fixture.SocialMemberFixture;
import com.triptune.member.repository.MemberRepository;
import com.triptune.member.repository.SocialMemberRepository;
import com.triptune.profile.entity.ProfileImage;
import com.triptune.profile.fixture.ProfileImageFixture;
import com.triptune.profile.repository.ProfileImageRepository;
import com.triptune.schedule.entity.ChatMessage;
import com.triptune.schedule.entity.TravelRoute;
import com.triptune.schedule.entity.TravelSchedule;
import com.triptune.schedule.enums.AttendeePermission;
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
import com.triptune.travel.enums.ThemeType;
import com.triptune.travel.fixture.TravelImageFixture;
import com.triptune.travel.fixture.TravelPlaceFixture;
import com.triptune.travel.repository.TravelImageRepository;
import com.triptune.travel.repository.TravelPlaceRepository;
import jakarta.persistence.EntityManager;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static com.triptune.member.fixture.MemberFixture.createLoginRequest;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional
@AutoConfigureMockMvc
@ActiveProfiles("mongo")
public class MemberIntegrationTest {
    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private JwtUtils jwtUtils;
    @Autowired private MemberRepository memberRepository;
    @Autowired private ProfileImageRepository profileImageRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private TravelPlaceRepository travelPlaceRepository;
    @Autowired private CountryRepository countryRepository;
    @Autowired private CityRepository cityRepository;
    @Autowired private DistrictRepository districtRepository;
    @Autowired private TravelImageRepository travelImageRepository;
    @Autowired private BookmarkRepository bookmarkRepository;
    @Autowired private TravelAttendeeRepository travelAttendeeRepository;
    @Autowired private TravelScheduleRepository travelScheduleRepository;
    @Autowired private TravelRouteRepository travelRouteRepository;
    @Autowired private ChatMessageRepository chatMessageRepository;
    @Autowired private SocialMemberRepository socialMemberRepository;
    @Autowired private ApiContentTypeRepository apiContentTypeRepository;
    @Autowired private S3ObjectManager s3ObjectManager;
    @Autowired private EntityManager em;

    @MockBean private RedisService redisService;
    @MockBean private EmailService emailService;
    @MockBean private S3Service s3Service;

    private ProfileImage profileImage;

    private TravelPlace place1;
    private TravelPlace place2;
    private TravelPlace place3;

    private String place1ThumbUrl;
    private String place2ThumbUrl;
    private String place3ThumbUrl;


    @BeforeEach
    void setUp() {
        chatMessageRepository.deleteAll();

        Country country = countryRepository.save(CountryFixture.createCountry());
        City city = cityRepository.save(CityFixture.createSeoul(country));
        District district = districtRepository.save(DistrictFixture.createDistrict(city, "강남"));
        ApiContentType apiContentType = apiContentTypeRepository.save(ApiContentTypeFixture.createApiContentType(ThemeType.ATTRACTIONS));

        profileImage = profileImageRepository.save(ProfileImageFixture.createProfileImage("memberImage"));

        place1 = travelPlaceRepository.save(
                TravelPlaceFixture.createTravelPlace(
                        country,
                        city,
                        district,
                        apiContentType,
                        "가장소"
                )
        );
        TravelImage place1Thumb = travelImageRepository.save(TravelImageFixture.createTravelImage(place1, "test1", true));
        place1ThumbUrl = S3Fixture.createS3ObjectUrl(place1Thumb.getS3ObjectKey());

        place2 = travelPlaceRepository.save(
                TravelPlaceFixture.createTravelPlace(
                        country,
                        city,
                        district,
                        apiContentType,
                        "나장소"
                )
        );
        TravelImage place2Thumb = travelImageRepository.save(TravelImageFixture.createTravelImage(place2, "test1", true));
        place2ThumbUrl = S3Fixture.createS3ObjectUrl(place2Thumb.getS3ObjectKey());

        place3 = travelPlaceRepository.save(
                TravelPlaceFixture.createTravelPlace(
                        country,
                        city,
                        district,
                        apiContentType,
                        "다장소"
                )
        );
        TravelImage place3Thumb = travelImageRepository.save(TravelImageFixture.createTravelImage(place3, "test1", true));
        place3ThumbUrl = S3Fixture.createS3ObjectUrl(place3Thumb.getS3ObjectKey());

    }

    @Test
    @DisplayName("회원가입")
    void join() throws Exception {
        // given
        when(redisService.getEmailData(any(), anyString())).thenReturn("true");

        JoinRequest request = MemberFixture.createMemberRequest(
                "member@email.com",
                "password12!@",
                "password12!@",
                "홍길동122"
        );

        // when
        mockMvc.perform(post("/api/members/join")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(SuccessCode.GENERAL_SUCCESS.getMessage()));

        // then
        em.flush();
        em.clear();
        Member joinedMember = memberRepository.findByEmail("member@email.com").orElseThrow();
        assertThat(joinedMember.getNickname()).isEqualTo("홍길동122");
        assertThat(joinedMember.getCreatedAt()).isEqualTo(joinedMember.getUpdatedAt());
    }


    @Test
    @DisplayName("회원가입 시 이미 존재하는 이메일로 인해 예외 발생")
    void join_existedEmail() throws Exception {
        // given
        JoinRequest request = MemberFixture.createMemberRequest(
                "member@email.com",
                "password12!@",
                "password12!@",
                "nickname"
        );

        memberRepository.save(MemberFixture.createNativeTypeMember(request.getEmail(), profileImage));

        // when, then
        mockMvc.perform(post("/api/members/join")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.ALREADY_EXISTED_EMAIL.getMessage()));
    }

    @Test
    @DisplayName("일반 회원 로그인")
    void login() throws Exception {
        // given
        String encodePassword = passwordEncoder.encode("password12!@");
        Member member = memberRepository.save(MemberFixture.createNativeTypeMember(
                "member@email.com",
                encodePassword,
                profileImage
        ));

        LoginRequest request = createLoginRequest(member.getEmail(), "password12!@");

        // when
        MvcResult result = mockMvc.perform(post("/api/members/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(SuccessCode.GENERAL_SUCCESS.getMessage()))
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.data.nickname").value(member.getNickname()))
                .andReturn();

        // then
        List<String> cookies = result.getResponse().getHeaders(HttpHeaders.SET_COOKIE);
        assertThat(cookies).hasSize(1);
        assertThat(cookies.get(0))
                .startsWith(CookieType.REFRESH_TOKEN.getKey() + "=")
                .contains("Max-Age=" + CookieType.REFRESH_TOKEN.getMaxAgeSeconds())
                .contains("HttpOnly");
    }

    @Test
    @DisplayName("로그인 시 비밀번호 맞지 않아 예외 발생")
    void login_incorrectPassword() throws Exception {
        // given
        memberRepository.save(MemberFixture.createNativeTypeMember("member@email.com", profileImage));

        LoginRequest request = MemberFixture.createLoginRequest("member@email.com", "fail!@");

        // when, then
        mockMvc.perform(post("/api/members/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.FAILED_LOGIN.getMessage()));
    }

    @Test
    @DisplayName("로그아웃")
    void logout() throws Exception {
        // given
        Member member = memberRepository.save(MemberFixture.createNativeTypeMember("member@email.com", profileImage));
        String accessToken = jwtUtils.createAccessToken(member.getMemberId());

        LogoutRequest request = MemberFixture.createLogoutRequest(member.getNickname());

        // when
        MvcResult result = mockMvc.perform(patch("/api/members/logout")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(SuccessCode.GENERAL_SUCCESS.getMessage()))
                .andReturn();

        // then
        List<String> cookies = result.getResponse().getHeaders(HttpHeaders.SET_COOKIE);
        assertThat(cookies).hasSize(3);

        assertThat(cookies).anyMatch(c ->
                c.startsWith(CookieType.ACCESS_TOKEN.getKey() + "=")
                        && c.contains("Max-Age=0")
                );
        assertThat(cookies).anyMatch(c ->
                c.startsWith(CookieType.REFRESH_TOKEN.getKey() + "=")
                        && c.contains("Max-Age=0")
        );
        assertThat(cookies).anyMatch(c ->
                c.startsWith(CookieType.NICKNAME.getKey() + "=")
                        && c.contains("Max-Age=0")
        );
    }

    @Test
    @DisplayName("토큰 갱신")
    void refreshToken() throws Exception {
        // given
        Member member = memberRepository.save(MemberFixture.createNativeTypeMember("member@email.com", profileImage));
        String refreshToken = jwtUtils.createRefreshToken(member.getMemberId());
        member.updateRefreshToken(refreshToken);

        Cookie cookie = MemberFixture.createRefreshTokenCookie(refreshToken);

        // when, then
        mockMvc.perform(post("/api/members/refresh")
                        .cookie(cookie))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(SuccessCode.GENERAL_SUCCESS.getMessage()))
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty());
    }


    @Test
    @DisplayName("토큰 갱신 시 refresh token 만료로 예외 발생")
    void refreshToken_expired() throws Exception {
        // given
        String refreshToken = jwtUtils.createToken("ExpiredRefreshToken", -604800000);
        Cookie cookie = MemberFixture.createRefreshTokenCookie(refreshToken);

        // when, then
        mockMvc.perform(post("/api/members/refresh")
                        .cookie(cookie))
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.EXPIRED_JWT_TOKEN.getMessage()));
    }

    @Test
    @DisplayName("토큰 갱신 시 요청 refreshToken 과 저장된 refresh token 값이 달라 예외 발생")
    void refreshToken_NotEqualsRefreshToken() throws Exception {
        // given
        Member member = memberRepository.save(MemberFixture.createNativeTypeMember("member@email.com", profileImage));
        String refreshToken = jwtUtils.createRefreshToken(member.getMemberId());

        Cookie cookie = MemberFixture.createRefreshTokenCookie(refreshToken);

        // when, then
        mockMvc.perform(post("/api/members/refresh")
                        .cookie(cookie))
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.MISMATCH_REFRESH_TOKEN.getMessage()));

    }

    @Test
    @DisplayName("비밀번호 찾기")
    void findPassword_nativeMember() throws Exception {
        // given
        String encodedPassword = passwordEncoder.encode("password12!@");
        Member member = memberRepository.save(MemberFixture.createNativeTypeMember(
                "member@email.com",
                encodedPassword,
                profileImage
        ));

        FindPasswordRequest request = MemberFixture.createFindPasswordRequest(member.getEmail());

        // when, then
        mockMvc.perform(post("/api/members/find-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(SuccessCode.GENERAL_SUCCESS.getMessage()));

        // then
        verify(emailService).sendResetPasswordEmail(any(FindPasswordRequest.class));
    }

    @Test
    @DisplayName("일반 회원 비밀번호 초기화")
    void resetPassword_nativeMember() throws Exception {
        // given
        String previousPassword = passwordEncoder.encode("savedPassword12!@");
        Member member = memberRepository.save(MemberFixture.createNativeTypeMember(
                "member@email.com",
                previousPassword,
                profileImage
        ));

        ResetPasswordRequest request = MemberFixture.createResetPasswordRequest("changePassword", "password12!@", "password12!@");

        when(redisService.getData(anyString())).thenReturn(member.getEmail());

        // when
        mockMvc.perform(patch("/api/members/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(SuccessCode.GENERAL_SUCCESS.getMessage()));

        // then
        em.flush();
        em.clear();
        Member resetMember = memberRepository.findByEmail(member.getEmail()).orElseThrow();
        assertThat(passwordEncoder.matches(request.getPassword(), resetMember.getPassword())).isTrue();
        assertThat(passwordEncoder.matches(previousPassword, resetMember.getPassword())).isFalse();
        assertThat(resetMember.getJoinType()).isEqualTo(JoinType.NATIVE);
        assertThat(resetMember.getCreatedAt()).isNotEqualTo(resetMember.getUpdatedAt());
    }


    @Test
    @DisplayName("소셜 회원 비밀번호 초기화")
    void resetPassword_socialMember() throws Exception {
        // given
        Member member = memberRepository.save(MemberFixture.createSocialTypeMember("member@email.com", profileImage));
        socialMemberRepository.save(SocialMemberFixture.createSocialMember(
                member,
                SocialType.NAVER,
                "member"
        ));

        ResetPasswordRequest request = MemberFixture.createResetPasswordRequest("changePassword", "password12!@", "password12!@");

        when(redisService.getData(anyString())).thenReturn(member.getEmail());

        // when, then
        mockMvc.perform(patch("/api/members/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(SuccessCode.GENERAL_SUCCESS.getMessage()));

        em.flush();
        em.clear();
        Member resetMember = memberRepository.findByEmail(member.getEmail()).orElseThrow();
        assertThat(passwordEncoder.matches(request.getPassword(), resetMember.getPassword())).isTrue();
        assertThat(resetMember.getJoinType()).isEqualTo(JoinType.BOTH);
        assertThat(resetMember.getCreatedAt()).isNotEqualTo(resetMember.getUpdatedAt());
    }

    @Test
    @DisplayName("통합 회원 비밀번호 초기화")
    void resetPassword_bothMember() throws Exception {
        // given
        String previousPassword = passwordEncoder.encode("previousPassword12!@");
        Member member = memberRepository.save(MemberFixture.createBothTypeMember(
                "member@email.com",
                previousPassword,
                profileImage
        ));
        socialMemberRepository.save(SocialMemberFixture.createSocialMember(
                member,
                SocialType.NAVER,
                "member"
        ));

        ResetPasswordRequest request = MemberFixture.createResetPasswordRequest("changePassword", "password12!@", "password12!@");

        when(redisService.getData(anyString())).thenReturn(member.getEmail());

        // when, then
        mockMvc.perform(patch("/api/members/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(SuccessCode.GENERAL_SUCCESS.getMessage()));

        em.flush();
        em.clear();
        Member resetMember = memberRepository.findByEmail(member.getEmail()).orElseThrow();
        assertThat(passwordEncoder.matches(request.getPassword(), resetMember.getPassword())).isTrue();
        assertThat(passwordEncoder.matches(previousPassword, resetMember.getPassword())).isFalse();
        assertThat(resetMember.getJoinType()).isEqualTo(JoinType.BOTH);
        assertThat(resetMember.getCreatedAt()).isNotEqualTo(resetMember.getUpdatedAt());
    }


    @Test
    @DisplayName("일반 회원 비밀번호 변경")
    void changePassword_nativeMember() throws Exception {
        // given
        String previousPassword = passwordEncoder.encode("prevPassword12!");
        Member member = memberRepository.save(MemberFixture.createNativeTypeMember(
                "member@email.com",
                previousPassword,
                profileImage
        ));
        SecurityTestUtils.mockAuthentication(member);

        ChangePasswordRequest request = MemberFixture.createChangePasswordRequest(
                "prevPassword12!",
                "password12!@",
                "password12!@"
        );

        // when
        mockMvc.perform(patch("/api/members/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(SuccessCode.GENERAL_SUCCESS.getMessage()));

        // then
        em.flush();
        em.clear();
        Member changeMember = memberRepository.findByEmail(member.getEmail()).orElseThrow();
        assertThat(passwordEncoder.matches(request.getNewPassword(), changeMember.getPassword())).isTrue();
        assertThat(passwordEncoder.matches(previousPassword, changeMember.getPassword())).isFalse();
        assertThat(changeMember.getCreatedAt()).isNotEqualTo(changeMember.getUpdatedAt());
    }

    @Test
    @DisplayName("통합 회원 비밀번호 변경")
    void changePassword_bothMember() throws Exception {
        // given
        String previousPassword = passwordEncoder.encode("prevPassword12!");
        Member member = memberRepository.save(MemberFixture.createBothTypeMember(
                "member@email.com",
                previousPassword,
                profileImage
        ));
        SecurityTestUtils.mockAuthentication(member);

        ChangePasswordRequest request = MemberFixture.createChangePasswordRequest(
                "prevPassword12!",
                "password12!@",
                "password12!@"
        );

        // when
        mockMvc.perform(patch("/api/members/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(SuccessCode.GENERAL_SUCCESS.getMessage()));

        // then
        em.flush();
        em.clear();
        Member changeMember = memberRepository.findByEmail(member.getEmail()).orElseThrow();
        assertThat(passwordEncoder.matches(request.getNewPassword(), changeMember.getPassword())).isTrue();
        assertThat(passwordEncoder.matches(previousPassword, changeMember.getPassword())).isFalse();
        assertThat(changeMember.getCreatedAt()).isNotEqualTo(changeMember.getUpdatedAt());
    }


    @Test
    @DisplayName("비밀번호 변경 시 소셜 회원으로 예외 발생")
    void changePassword_socialMember() throws Exception {
        // given
        Member member = memberRepository.save(MemberFixture.createSocialTypeMember("member@email.com", profileImage));
        SecurityTestUtils.mockAuthentication(member);

        ChangePasswordRequest request = MemberFixture.createChangePasswordRequest(
                "password12!",
                "password12!@",
                "password12!@"
        );

        // when, then
        mockMvc.perform(patch("/api/members/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.SOCIAL_MEMBER_PASSWORD_CHANGE_NOT_ALLOWED.getMessage()));
    }

    @Test
    @DisplayName("회원 정보 조회")
    void getMemberInfo() throws Exception {
        // given
        String encodedPassword = passwordEncoder.encode("password12!@");
        Member member = memberRepository.save(MemberFixture.createNativeTypeMember(
                "member@email.com",
                encodedPassword,
                profileImage
        ));
        String profileImageUrl = s3ObjectManager.generateS3ObjectUrl(profileImage.getS3ObjectKey());

        SecurityTestUtils.mockAuthentication(member);

        // when, then
        mockMvc.perform(get("/api/members/info"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.email").value(member.getEmail()))
                .andExpect(jsonPath("$.data.nickname").value(member.getNickname()))
                .andExpect(jsonPath("$.data.profileImage").value(profileImageUrl));
    }

    @Test
    @DisplayName("회원 닉네임 변경")
    void changeNickname() throws Exception {
        // given
        Member member = memberRepository.save(MemberFixture.createNativeTypeMember("member@email.com", profileImage));
        SecurityTestUtils.mockAuthentication(member);

        ChangeNicknameRequest request = MemberFixture.createChangeNicknameRequest("newNickname");

        // when
        mockMvc.perform(patch("/api/members/change-nickname")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(SuccessCode.GENERAL_SUCCESS.getMessage()));

        // then
        em.flush();
        em.clear();
        Member changeMember = memberRepository.findByEmail(member.getEmail()).orElseThrow();
        assertThat(changeMember.getNickname()).isEqualTo(request.getNickname());
        assertThat(changeMember.getCreatedAt()).isNotEqualTo(changeMember.getUpdatedAt());
    }

    @Test
    @DisplayName("이메일 변경")
    void changeEmail() throws Exception {
        // given
        Member member = memberRepository.save(MemberFixture.createNativeTypeMember("member@email.com", profileImage));
        SecurityTestUtils.mockAuthentication(member);

        EmailRequest request = MemberFixture.createEmailRequest("changeEmail@email.com");

        when(redisService.getEmailData(any(), anyString())).thenReturn("true");

        // when
        mockMvc.perform(patch("/api/members/change-email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(SuccessCode.GENERAL_SUCCESS.getMessage()));

        // then
        em.flush();
        em.clear();
        Member changeMember = memberRepository.findByEmail(member.getEmail()).orElseThrow();
        assertThat(changeMember.getEmail()).isEqualTo(request.getEmail());
        assertThat(changeMember.getCreatedAt()).isNotEqualTo(changeMember.getUpdatedAt());
    }

    @Test
    @DisplayName("회원 북마크 조회 - 최신순")
    void getMemberBookmarks_sortNewest() throws Exception {
        // given
        Member member = memberRepository.save(MemberFixture.createNativeTypeMember("member@email.com", profileImage));

        bookmarkRepository.save(BookmarkFixture.createBookmark(
                member,
                place1,
                LocalDateTime.now().minusDays(3)
        ));
        bookmarkRepository.save(BookmarkFixture.createBookmark(
                member,
                place2,
                LocalDateTime.now().minusDays(2)
        ));
        bookmarkRepository.save(BookmarkFixture.createBookmark(
                member,
                place3,
                LocalDateTime.now()
        ));

        SecurityTestUtils.mockAuthentication(member);

        // when, then
        mockMvc.perform(get("/api/members/bookmark")
                        .param("page", "1")
                        .param("sort", "newest"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalElements").value(3))
                .andExpect(jsonPath("$.data.content[0].placeName").value(place3.getPlaceName()))
                .andExpect(jsonPath("$.data.content[0].thumbnailUrl").value(place3ThumbUrl))
                .andExpect(jsonPath("$.data.content[1].placeName").value(place2.getPlaceName()))
                .andExpect(jsonPath("$.data.content[1].thumbnailUrl").value(place2ThumbUrl))
                .andExpect(jsonPath("$.data.content[2].placeName").value(place1.getPlaceName()))
                .andExpect(jsonPath("$.data.content[2].thumbnailUrl").value(place1ThumbUrl));
    }

    @Test
    @DisplayName("회원 북마크 조회 - 오래된순")
    void getMemberBookmarks_sortOldest() throws Exception {
        // given
        Member member = memberRepository.save(MemberFixture.createNativeTypeMember("member@email.com", profileImage));

        bookmarkRepository.save(BookmarkFixture.createBookmark(
                member,
                place1,
                LocalDateTime.now().minusDays(3)
        ));
        bookmarkRepository.save(BookmarkFixture.createBookmark(
                member,
                place2,
                LocalDateTime.now().minusDays(2)
        ));
        bookmarkRepository.save(BookmarkFixture.createBookmark(
                member,
                place3,
                LocalDateTime.now()
        ));

        SecurityTestUtils.mockAuthentication(member);

        // when, then
        mockMvc.perform(get("/api/members/bookmark")
                        .param("page", "1")
                        .param("sort", "oldest"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalElements").value(3))
                .andExpect(jsonPath("$.data.content[0].placeName").value(place1.getPlaceName()))
                .andExpect(jsonPath("$.data.content[0].thumbnailUrl").value(place1ThumbUrl))
                .andExpect(jsonPath("$.data.content[1].placeName").value(place2.getPlaceName()))
                .andExpect(jsonPath("$.data.content[1].thumbnailUrl").value(place2ThumbUrl))
                .andExpect(jsonPath("$.data.content[2].placeName").value(place3.getPlaceName()))
                .andExpect(jsonPath("$.data.content[2].thumbnailUrl").value(place3ThumbUrl));
    }

    @Test
    @DisplayName("회원 북마크 조회 - 이름순")
    void getMemberBookmarks_sortName() throws Exception {
        // given
        Member member = memberRepository.save(MemberFixture.createNativeTypeMember("member@email.com", profileImage));

        bookmarkRepository.saveAll(List.of(
                BookmarkFixture.createBookmark(member, place1),
                BookmarkFixture.createBookmark(member, place2),
                BookmarkFixture.createBookmark(member, place3)
        ));

        SecurityTestUtils.mockAuthentication(member);

        // when, then
        mockMvc.perform(get("/api/members/bookmark")
                        .param("page", "1")
                        .param("sort", "name"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalElements").value(3))
                .andExpect(jsonPath("$.data.content[0].placeName").value(place1.getPlaceName()))
                .andExpect(jsonPath("$.data.content[0].thumbnailUrl").value(place1ThumbUrl))
                .andExpect(jsonPath("$.data.content[1].placeName").value(place2.getPlaceName()))
                .andExpect(jsonPath("$.data.content[1].thumbnailUrl").value(place2ThumbUrl))
                .andExpect(jsonPath("$.data.content[2].placeName").value(place3.getPlaceName()))
                .andExpect(jsonPath("$.data.content[2].thumbnailUrl").value(place3ThumbUrl));
    }

    @Test
    @DisplayName("회원 북마크 조회 시 데이터 없는 경우")
    void getMemberBookmarks_emptyData() throws Exception {
        // given
        Member member = memberRepository.save(MemberFixture.createNativeTypeMember("member@email.com", profileImage));
        SecurityTestUtils.mockAuthentication(member);

        // when, then
        mockMvc.perform(get("/api/members/bookmark")
                        .param("page", "1")
                        .param("sort", "newest"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalElements").value(0))
                .andExpect(jsonPath("$.data.content").isEmpty());
    }

    @Test
    @DisplayName("일반 회원 탈퇴 - 작성자, 참석자 존재하는 경우")
    void deactivateMember_nativeMember1() throws Exception {
        // given
        String password = "password12!@";
        String encodedPassword = passwordEncoder.encode(password);
        Member member = memberRepository.save(MemberFixture.createNativeTypeMember(
                "member@email.com",
                encodedPassword,
                profileImage
        ));
        Long memberId = member.getMemberId();

        TravelSchedule schedule1 = travelScheduleRepository.save(TravelScheduleFixture.createSchedule("테스트1"));
        TravelSchedule schedule2 = travelScheduleRepository.save(TravelScheduleFixture.createSchedule("테스트2"));

        travelRouteRepository.saveAll(List.of(
                TravelRouteFixture.createRoute(schedule1, place1, 1),
                TravelRouteFixture.createRoute(schedule1, place2, 2)
        ));

        travelAttendeeRepository.saveAll(List.of(
                TravelAttendeeFixture.createAuthorAttendee(schedule1, member),
                TravelAttendeeFixture.createGuestAttendee(schedule2, member, AttendeePermission.READ)
        ));

        chatMessageRepository.saveAll(List.of(
                ChatMessageFixture.createChatMessage(schedule1.getScheduleId(), member.getMemberId(), "테스트1"),
                ChatMessageFixture.createChatMessage(schedule1.getScheduleId(), member.getMemberId(), "테스트2")
        ));

        bookmarkRepository.saveAll(List.of(
                BookmarkFixture.createBookmark(member, place1),
                BookmarkFixture.createBookmark(member, place1)
        ));

        SecurityTestUtils.mockAuthentication(member);

        DeactivateRequest request = MemberFixture.createDeactivateRequest(password);

        // when
        MvcResult result = mockMvc.perform(patch("/api/members/deactivate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(SuccessCode.GENERAL_SUCCESS.getMessage()))
                .andReturn();

        // then
        List<String> cookies = result.getResponse().getHeaders(HttpHeaders.SET_COOKIE);
        assertThat(cookies).hasSize(3);

        assertThat(cookies).anyMatch(c ->
                c.startsWith(CookieType.ACCESS_TOKEN.getKey() + "=")
                        && c.contains("Max-Age=0")
        );
        assertThat(cookies).anyMatch(c ->
                c.startsWith(CookieType.REFRESH_TOKEN.getKey() + "=")
                        && c.contains("Max-Age=0")
        );
        assertThat(cookies).anyMatch(c ->
                c.startsWith(CookieType.NICKNAME.getKey() + "=")
                        && c.contains("Max-Age=0")
        );

        em.flush();
        em.clear();

        List<TravelSchedule> savedTravelSchedules = travelScheduleRepository.findAll();
        assertThat(savedTravelSchedules).hasSize(1);
        assertThat(savedTravelSchedules.get(0).getScheduleId()).isEqualTo(schedule2.getScheduleId());

        Member deactivatedMember = memberRepository.findById(memberId).orElseThrow();
        assertThat(deactivatedMember.getNickname()).isEqualTo(DeactivateValue.DEACTIVATE.name());
        assertThat(deactivatedMember.getEmail()).isEqualTo(DeactivateValue.DEACTIVATE.name());
        assertThat(deactivatedMember.getPassword()).isEqualTo(DeactivateValue.DEACTIVATE.name());
        assertThat(deactivatedMember.getRefreshToken()).isNull();
        assertThat(deactivatedMember.getCreatedAt()).isNotEqualTo(deactivatedMember.getUpdatedAt());
        assertThat(deactivatedMember.isActive()).isFalse();


        assertThat(bookmarkRepository.findAll()).isEmpty();
        assertThat(travelAttendeeRepository.findAll()).isEmpty();
        assertThat(travelRouteRepository.findAll()).isEmpty();
        assertThat(chatMessageRepository.findAll()).isEmpty();
        verify(s3Service).deleteS3File(anyString());
    }

    @Test
    @DisplayName("일반 회원 탈퇴 - 작성자만 존재하는 경우")
    void deactivateMember_nativeMember2() throws Exception {
        // given
        String password = "password12!@";
        String encodedPassword = passwordEncoder.encode(password);
        Member member = memberRepository.save(MemberFixture.createNativeTypeMember(
                "member@email.com",
                encodedPassword,
                profileImage
        ));
        Long memberId = member.getMemberId();

        TravelSchedule schedule = travelScheduleRepository.save(TravelScheduleFixture.createSchedule("테스트1"));

        travelRouteRepository.saveAll(List.of(
                TravelRouteFixture.createRoute(schedule, place1, 1),
                TravelRouteFixture.createRoute(schedule, place2, 2)
        ));

        travelAttendeeRepository.save(
                TravelAttendeeFixture.createAuthorAttendee(schedule, member)
        );

        chatMessageRepository.saveAll(List.of(
                ChatMessageFixture.createChatMessage(schedule.getScheduleId(), member.getMemberId(), "테스트1"),
                ChatMessageFixture.createChatMessage(schedule.getScheduleId(), member.getMemberId(), "테스트2")
        ));

        bookmarkRepository.saveAll(List.of(
                BookmarkFixture.createBookmark(member, place1),
                BookmarkFixture.createBookmark(member, place1)
        ));

        SecurityTestUtils.mockAuthentication(member);

        DeactivateRequest request = MemberFixture.createDeactivateRequest(password);

        // when
        MvcResult result = mockMvc.perform(patch("/api/members/deactivate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(SuccessCode.GENERAL_SUCCESS.getMessage()))
                .andReturn();

        // then
        List<String> cookies = result.getResponse().getHeaders(HttpHeaders.SET_COOKIE);
        assertThat(cookies).hasSize(3);

        assertThat(cookies).anyMatch(c ->
                c.startsWith(CookieType.ACCESS_TOKEN.getKey() + "=")
                        && c.contains("Max-Age=0")
        );
        assertThat(cookies).anyMatch(c ->
                c.startsWith(CookieType.REFRESH_TOKEN.getKey() + "=")
                        && c.contains("Max-Age=0")
        );
        assertThat(cookies).anyMatch(c ->
                c.startsWith(CookieType.NICKNAME.getKey() + "=")
                        && c.contains("Max-Age=0")
        );


        em.flush();
        em.clear();

        Member deactivatedMember = memberRepository.findById(memberId).orElseThrow();
        assertThat(deactivatedMember.getNickname()).isEqualTo(DeactivateValue.DEACTIVATE.name());
        assertThat(deactivatedMember.getEmail()).isEqualTo(DeactivateValue.DEACTIVATE.name());
        assertThat(deactivatedMember.getPassword()).isEqualTo(DeactivateValue.DEACTIVATE.name());
        assertThat(deactivatedMember.getRefreshToken()).isNull();
        assertThat(deactivatedMember.getCreatedAt()).isNotEqualTo(deactivatedMember.getUpdatedAt());
        assertThat(deactivatedMember.isActive()).isFalse();


        assertThat(bookmarkRepository.findAll()).isEmpty();
        assertThat(travelAttendeeRepository.findAll()).isEmpty();
        assertThat(travelRouteRepository.findAll()).isEmpty();
        assertThat(travelScheduleRepository.findAll()).isEmpty();
        assertThat(chatMessageRepository.findAll()).isEmpty();
        verify(s3Service).deleteS3File(anyString());
    }

    @Test
    @DisplayName("일반 회원 탈퇴 - 참석자만 존재하는 경우")
    void deactivateMember_nativeMember3() throws Exception {
        // given
        String password = "password12!@";
        String encodedPassword = passwordEncoder.encode(password);
        Member member = memberRepository.save(MemberFixture.createNativeTypeMember(
                "member@email.com",
                encodedPassword,
                profileImage
        ));
        Long memberId = member.getMemberId();

        TravelSchedule schedule = travelScheduleRepository.save(TravelScheduleFixture.createSchedule("테스트1"));

        travelRouteRepository.saveAll(List.of(
                TravelRouteFixture.createRoute(schedule, place1, 1),
                TravelRouteFixture.createRoute(schedule, place2, 2)
        ));

        travelAttendeeRepository.save(
                TravelAttendeeFixture.createGuestAttendee(schedule, member, AttendeePermission.READ)
        );

        chatMessageRepository.saveAll(List.of(
                ChatMessageFixture.createChatMessage(schedule.getScheduleId(), member.getMemberId(), "테스트1"),
                ChatMessageFixture.createChatMessage(schedule.getScheduleId(), member.getMemberId(), "테스트2")
        ));

        bookmarkRepository.saveAll(List.of(
                BookmarkFixture.createBookmark(member, place1),
                BookmarkFixture.createBookmark(member, place1)
        ));

        SecurityTestUtils.mockAuthentication(member);

        DeactivateRequest request = MemberFixture.createDeactivateRequest(password);

        // when
        MvcResult result = mockMvc.perform(patch("/api/members/deactivate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(SuccessCode.GENERAL_SUCCESS.getMessage()))
                .andReturn();

        // then
        List<String> cookies = result.getResponse().getHeaders(HttpHeaders.SET_COOKIE);
        assertThat(cookies).hasSize(3);

        assertThat(cookies).anyMatch(c ->
                c.startsWith(CookieType.ACCESS_TOKEN.getKey() + "=")
                        && c.contains("Max-Age=0")
        );
        assertThat(cookies).anyMatch(c ->
                c.startsWith(CookieType.REFRESH_TOKEN.getKey() + "=")
                        && c.contains("Max-Age=0")
        );
        assertThat(cookies).anyMatch(c ->
                c.startsWith(CookieType.NICKNAME.getKey() + "=")
                        && c.contains("Max-Age=0")
        );

        em.flush();
        em.clear();

        List<TravelSchedule> savedTravelSchedules = travelScheduleRepository.findAll();
        assertThat(savedTravelSchedules).hasSize(1);
        assertThat(savedTravelSchedules.get(0).getScheduleId()).isEqualTo(schedule.getScheduleId());

        Member deactivatedMember = memberRepository.findById(memberId).orElseThrow();
        assertThat(deactivatedMember.getNickname()).isEqualTo(DeactivateValue.DEACTIVATE.name());
        assertThat(deactivatedMember.getEmail()).isEqualTo(DeactivateValue.DEACTIVATE.name());
        assertThat(deactivatedMember.getPassword()).isEqualTo(DeactivateValue.DEACTIVATE.name());
        assertThat(deactivatedMember.getRefreshToken()).isNull();
        assertThat(deactivatedMember.getCreatedAt()).isNotEqualTo(deactivatedMember.getUpdatedAt());
        assertThat(deactivatedMember.isActive()).isFalse();

        List<TravelRoute> travelRoutes = travelRouteRepository.findAll();
        assertThat(travelRoutes).hasSize(2);

        List<ChatMessage> chatMessages = chatMessageRepository.findAllByScheduleId(schedule.getScheduleId());
        assertThat(chatMessages).hasSize(2);


        assertThat(bookmarkRepository.findAll()).isEmpty();
        assertThat(travelAttendeeRepository.findAll()).isEmpty();
        verify(s3Service).deleteS3File(anyString());
    }


    @Test
    @DisplayName("통합 회원 탈퇴")
    void deactivateMember_bothMember() throws Exception {
        // given
        String password = "password12!@";
        String encodedPassword = passwordEncoder.encode(password);
        Member member = memberRepository.save(MemberFixture.createBothTypeMember(
                "member@email.com",
                encodedPassword,
                profileImage
        ));
        Long memberId = member.getMemberId();

        socialMemberRepository.saveAll(List.of(
                SocialMemberFixture.createSocialMember(member, SocialType.KAKAO, "kakao"),
                SocialMemberFixture.createSocialMember(member, SocialType.NAVER, "naver")
        ));

        TravelSchedule schedule1 = travelScheduleRepository.save(TravelScheduleFixture.createSchedule("테스트1"));
        TravelSchedule schedule2 = travelScheduleRepository.save(TravelScheduleFixture.createSchedule("테스트2"));

        travelRouteRepository.saveAll(List.of(
                TravelRouteFixture.createRoute(schedule1, place1, 1),
                TravelRouteFixture.createRoute(schedule1, place2, 2)
        ));

        travelAttendeeRepository.saveAll(List.of(
                TravelAttendeeFixture.createAuthorAttendee(schedule1, member),
                TravelAttendeeFixture.createGuestAttendee(schedule2, member, AttendeePermission.READ)
        ));

        chatMessageRepository.saveAll(List.of(
                ChatMessageFixture.createChatMessage(schedule1.getScheduleId(), member.getMemberId(), "테스트1"),
                ChatMessageFixture.createChatMessage(schedule1.getScheduleId(), member.getMemberId(), "테스트2")
        ));

        bookmarkRepository.saveAll(List.of(
                BookmarkFixture.createBookmark(member, place1),
                BookmarkFixture.createBookmark(member, place1)
        ));

        SecurityTestUtils.mockAuthentication(member);

        DeactivateRequest request = MemberFixture.createDeactivateRequest(password);

        // when
        MvcResult result = mockMvc.perform(patch("/api/members/deactivate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(SuccessCode.GENERAL_SUCCESS.getMessage()))
                .andReturn();

        // then
        List<String> cookies = result.getResponse().getHeaders(HttpHeaders.SET_COOKIE);
        assertThat(cookies).hasSize(3);

        assertThat(cookies).anyMatch(c ->
                c.startsWith(CookieType.ACCESS_TOKEN.getKey() + "=")
                        && c.contains("Max-Age=0")
        );
        assertThat(cookies).anyMatch(c ->
                c.startsWith(CookieType.REFRESH_TOKEN.getKey() + "=")
                        && c.contains("Max-Age=0")
        );
        assertThat(cookies).anyMatch(c ->
                c.startsWith(CookieType.NICKNAME.getKey() + "=")
                        && c.contains("Max-Age=0")
        );

        em.flush();
        em.clear();

        List<TravelSchedule> savedTravelSchedules = travelScheduleRepository.findAll();
        assertThat(savedTravelSchedules).hasSize(1);
        assertThat(savedTravelSchedules.get(0).getScheduleId()).isEqualTo(schedule2.getScheduleId());

        Member deactivatedMember = memberRepository.findById(memberId).orElseThrow();
        assertThat(deactivatedMember.getNickname()).isEqualTo(DeactivateValue.DEACTIVATE.name());
        assertThat(deactivatedMember.getEmail()).isEqualTo(DeactivateValue.DEACTIVATE.name());
        assertThat(deactivatedMember.getPassword()).isEqualTo(DeactivateValue.DEACTIVATE.name());
        assertThat(deactivatedMember.getRefreshToken()).isNull();
        assertThat(deactivatedMember.getCreatedAt()).isNotEqualTo(deactivatedMember.getUpdatedAt());
        assertThat(deactivatedMember.isActive()).isFalse();

        List<SocialMember> socialMembers = socialMemberRepository.findAll();
        assertThat(socialMembers).hasSize(2);
        assertThat(socialMembers.get(0).getSocialId()).isEqualTo(DeactivateValue.DEACTIVATE.name());
        assertThat(socialMembers.get(0).getCreatedAt()).isNotEqualTo(socialMembers.get(0).getUpdatedAt());
        assertThat(socialMembers.get(1).getSocialId()).isEqualTo(DeactivateValue.DEACTIVATE.name());
        assertThat(socialMembers.get(1).getCreatedAt()).isNotEqualTo(socialMembers.get(1).getUpdatedAt());

        assertThat(bookmarkRepository.findAll()).isEmpty();
        assertThat(travelAttendeeRepository.findAll()).isEmpty();
        assertThat(travelRouteRepository.findAll()).isEmpty();
        assertThat(chatMessageRepository.findAll()).isEmpty();
        verify(s3Service).deleteS3File(anyString());
    }


    @Test
    @DisplayName("회원 탈퇴 - 일정 데이터 없는 경우")
    void deactivateMember_emptySchedule() throws Exception {
        // given
        String password = "password12!@";
        String encodedPassword = passwordEncoder.encode(password);
        Member member = memberRepository.save(MemberFixture.createNativeTypeMember(
                "member@email.com",
                encodedPassword,
                profileImage
        ));
        Long memberId = member.getMemberId();

        bookmarkRepository.saveAll(List.of(
                BookmarkFixture.createBookmark(member, place1),
                BookmarkFixture.createBookmark(member, place1)
        ));

        SecurityTestUtils.mockAuthentication(member);

        DeactivateRequest request = MemberFixture.createDeactivateRequest(password);

        // when
        MvcResult result = mockMvc.perform(patch("/api/members/deactivate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(SuccessCode.GENERAL_SUCCESS.getMessage()))
                .andReturn();


        // then
        List<String> cookies = result.getResponse().getHeaders(HttpHeaders.SET_COOKIE);
        assertThat(cookies).hasSize(3);

        assertThat(cookies).anyMatch(c ->
                c.startsWith(CookieType.ACCESS_TOKEN.getKey() + "=")
                        && c.contains("Max-Age=0")
        );
        assertThat(cookies).anyMatch(c ->
                c.startsWith(CookieType.REFRESH_TOKEN.getKey() + "=")
                        && c.contains("Max-Age=0")
        );
        assertThat(cookies).anyMatch(c ->
                c.startsWith(CookieType.NICKNAME.getKey() + "=")
                        && c.contains("Max-Age=0")
        );


        em.flush();
        em.clear();

        Member deactivatedMember = memberRepository.findById(memberId).orElseThrow();
        assertThat(deactivatedMember.getNickname()).isEqualTo(DeactivateValue.DEACTIVATE.name());
        assertThat(deactivatedMember.getEmail()).isEqualTo(DeactivateValue.DEACTIVATE.name());
        assertThat(deactivatedMember.getPassword()).isEqualTo(DeactivateValue.DEACTIVATE.name());
        assertThat(deactivatedMember.getRefreshToken()).isNull();
        assertThat(deactivatedMember.getCreatedAt()).isNotEqualTo(deactivatedMember.getUpdatedAt());
        assertThat(deactivatedMember.isActive()).isFalse();


        assertThat(bookmarkRepository.findAll()).isEmpty();
        verify(s3Service).deleteS3File(anyString());
    }


    @Test
    @DisplayName("소셜 회원 탈퇴 요청으로 예외 발생")
    void deactivateMember_socialMember() throws Exception {
        // given
        Member member = memberRepository.save(MemberFixture.createSocialTypeMember("member@email.com", profileImage));

        socialMemberRepository.saveAll(List.of(
                SocialMemberFixture.createSocialMember(member, SocialType.KAKAO, "kakao"),
                SocialMemberFixture.createSocialMember(member, SocialType.NAVER, "naver")
        ));

        SecurityTestUtils.mockAuthentication(member);

        DeactivateRequest request = MemberFixture.createDeactivateRequest("test123@");

        // when, then
        mockMvc.perform(patch("/api/members/deactivate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.SOCIAL_MEMBER_DEACTIVATE_NOT_ALLOWED.getMessage()))
                .andReturn();
    }

}
