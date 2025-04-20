package com.triptune.member.controller;

import com.triptune.bookmark.repository.BookmarkRepository;
import com.triptune.common.entity.ApiCategory;
import com.triptune.common.entity.City;
import com.triptune.common.entity.Country;
import com.triptune.common.entity.District;
import com.triptune.common.repository.ApiCategoryRepository;
import com.triptune.common.repository.CityRepository;
import com.triptune.common.repository.CountryRepository;
import com.triptune.common.repository.DistrictRepository;
import com.triptune.member.entity.SocialMember;
import com.triptune.member.enumclass.SocialType;
import com.triptune.member.repository.SocialMemberRepository;
import com.triptune.schedule.entity.TravelSchedule;
import com.triptune.schedule.enumclass.AttendeePermission;
import com.triptune.schedule.enumclass.AttendeeRole;
import com.triptune.schedule.repository.ChatMessageRepository;
import com.triptune.schedule.repository.TravelAttendeeRepository;
import com.triptune.schedule.repository.TravelScheduleRepository;
import com.triptune.travel.entity.TravelImage;
import com.triptune.travel.entity.TravelPlace;
import com.triptune.travel.repository.TravelImageRepository;
import com.triptune.travel.repository.TravelPlaceRepository;
import com.triptune.global.service.S3Service;
import com.triptune.email.service.EmailService;
import com.triptune.member.MemberTest;
import com.triptune.member.dto.request.ChangeNicknameRequest;
import com.triptune.member.dto.request.ChangePasswordRequest;
import com.triptune.member.dto.request.JoinRequest;
import com.triptune.member.entity.Member;
import com.triptune.profile.entity.ProfileImage;
import com.triptune.member.repository.MemberRepository;
import com.triptune.profile.repository.ProfileImageRepository;
import com.triptune.global.enumclass.ErrorCode;
import com.triptune.global.enumclass.SuccessCode;
import com.triptune.global.util.JwtUtils;
import com.triptune.global.util.RedisUtils;
import jakarta.persistence.GeneratedValue;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("mongo")
public class MemberControllerTest extends MemberTest {
    @Autowired private WebApplicationContext wac;
    @Autowired private JwtUtils jwtUtils;
    @Autowired private MemberRepository memberRepository;
    @Autowired private ProfileImageRepository profileImageRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private TravelPlaceRepository travelPlaceRepository;
    @Autowired private CountryRepository countryRepository;
    @Autowired private CityRepository cityRepository;
    @Autowired private DistrictRepository districtRepository;
    @Autowired private TravelImageRepository travelImageRepository;
    @Autowired private ApiCategoryRepository apiCategoryRepository;
    @Autowired private BookmarkRepository bookmarkRepository;
    @Autowired private TravelAttendeeRepository travelAttendeeRepository;
    @Autowired private TravelScheduleRepository travelScheduleRepository;
    @Autowired private ChatMessageRepository chatMessageRepository;
    @Autowired private SocialMemberRepository socialMemberRepository;

    @MockBean private RedisUtils redisUtils;
    @MockBean private EmailService emailService;
    @MockBean private S3Service s3Service;

    private MockMvc mockMvc;
    private TravelPlace travelPlace1;
    private TravelPlace travelPlace2;
    private TravelPlace travelPlace3;

    private TravelImage travelImage1;
    private TravelImage travelImage2;
    private TravelImage travelImage3;


    @BeforeEach
    void setUp(){
        mockMvc = MockMvcBuilders.webAppContextSetup(wac)
                .addFilter(new CharacterEncodingFilter("UTF-8", true))
                .apply(springSecurity())
                .alwaysDo(print())
                .build();

        chatMessageRepository.deleteAll();

        Country country = countryRepository.save(createCountry());
        City city = cityRepository.save(createCity(country));
        District district = districtRepository.save(createDistrict(city, "강남"));
        ApiCategory apiCategory = apiCategoryRepository.save(createApiCategory());

        travelImage1 = travelImageRepository.save(createTravelImage(travelPlace1, "test1", true));
        travelImage2 = travelImageRepository.save(createTravelImage(travelPlace2, "test1", true));
        travelImage3 = travelImageRepository.save(createTravelImage(travelPlace3, "test1", true));
        travelPlace1 = travelPlaceRepository.save(createTravelPlace(null, country, city, district, apiCategory, "가장소", List.of(travelImage1)));
        travelPlace2 = travelPlaceRepository.save(createTravelPlace(null, country, city, district, apiCategory, "나장소", List.of(travelImage2)));
        travelPlace3 = travelPlaceRepository.save(createTravelPlace(null, country, city, district, apiCategory, "다장소", List.of(travelImage3)));

    }

    @Test
    @DisplayName("회원가입")
    void join() throws Exception {
        when(redisUtils.getEmailData(any(), anyString())).thenReturn("true");

        mockMvc.perform(post("/api/members/join")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJsonString(createMemberRequest("member@email.com", "password12!@", "password12!@"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(SuccessCode.GENERAL_SUCCESS.getMessage()));
    }

    @Test
    @DisplayName("회원 가입 시 비밀번호 유효성 검사로 예외 발생")
    void joinInvalidPassword_methodArgumentNotValidException() throws Exception {
        JoinRequest request = createMemberRequest("member@email.com", "password", "password");

        mockMvc.perform(post("/api/members/join")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").isNotEmpty());
    }

    @Test
    @DisplayName("회원 가입 시 비밀번호, 비밀번호 재입력 불일치로 인한 예외 발생")
    void join_CustomNotValidException() throws Exception {
        JoinRequest request = createMemberRequest("member@email.com", "password123@", "repassword123@");

        mockMvc.perform(post("/api/members/join")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.INCORRECT_PASSWORD_REPASSWORD.getMessage()));
    }

    @Test
    @DisplayName("회원 가입 시 이미 존재하는 이메일로 인해 예외 발생")
    void joinExistedEmail_dataExistException() throws Exception {
        JoinRequest request = createMemberRequest("member@email.com", "password12!@", "password12!@");
        memberRepository.save(createMember(null, "member@email.com"));

        mockMvc.perform(post("/api/members/join")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.ALREADY_EXISTED_EMAIL.getMessage()));
    }

    @Test
    @DisplayName("회원가입 시 인증되지 않은 이메일로 예외 발생")
    void join_notVerifiedEmail() throws Exception {
        mockMvc.perform(post("/api/members/join")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonString(createMemberRequest("member@email.com", "password12!@", "password12!@"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.NOT_VERIFIED_EMAIL.getMessage()));;
    }


    @Test
    @DisplayName("로그인")
    void login() throws Exception {
        String encodePassword = passwordEncoder.encode("password12!@");
        ProfileImage profileImage = profileImageRepository.save(createProfileImage(null, "member"));
        Member member = memberRepository.save(createMember(null, "member@email.com", encodePassword, profileImage));

        MvcResult result = mockMvc.perform(post("/api/members/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonString(createLoginRequest("member@email.com", "password12!@"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(SuccessCode.GENERAL_SUCCESS.getMessage()))
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.data.nickname").value(member.getNickname()))
                .andReturn();

        Cookie[] cookies = result.getResponse().getCookies();

        assertThat(cookies).isNotNull();
        assertThat(cookies.length).isEqualTo(1);
        assertThat(cookies[0].getName()).isEqualTo("refreshToken");
        assertThat(cookies[0].getValue()).isNotNull();
    }

    @Test
    @DisplayName("소셜, 자체 로그인 사용자 로그인")
    void login_socialMemberAndMember() throws Exception {
        String encodePassword = passwordEncoder.encode("password12!@");
        ProfileImage profileImage = profileImageRepository.save(createProfileImage(null, "member"));
        Member member = memberRepository.save(createMember(null, "member@email.com", encodePassword, profileImage));
        socialMemberRepository.save(createSocialMember(null, member, "member", SocialType.NAVER));

        MvcResult result = mockMvc.perform(post("/api/members/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonString(createLoginRequest("member@email.com", "password12!@"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(SuccessCode.GENERAL_SUCCESS.getMessage()))
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.data.nickname").value(member.getNickname()))
                .andReturn();

        Cookie[] cookies = result.getResponse().getCookies();

        assertThat(cookies).isNotNull();
        assertThat(cookies.length).isEqualTo(1);
        assertThat(cookies[0].getName()).isEqualTo("refreshToken");
        assertThat(cookies[0].getValue()).isNotNull();
    }


    @Test
    @DisplayName("로그인 시 이메일 맞지 않아 예외 발생")
    void login_incorrectEmail() throws Exception{
        mockMvc.perform(post("/api/members/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonString(createLoginRequest("fail@email.com", "password12!@"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.FAILED_LOGIN.getMessage()));
    }

    @Test
    @DisplayName("로그인 시 비밀번호 맞지 않아 예외 발생")
    void login_incorrectPassword() throws Exception{
        memberRepository.save(createMember(null, "member@email.com"));

        mockMvc.perform(post("/api/members/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonString(createLoginRequest("member@email.com", "fail!@"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.FAILED_LOGIN.getMessage()));
    }

    @Test
    @DisplayName("소셜 로그인 정보만 있는 사용자가 자체 로그인 시도해 예외 발생")
    void login_socialMember() throws Exception{
        ProfileImage profileImage = profileImageRepository.save(createProfileImage(null, "member"));
        Member member = memberRepository.save(createMember(null, "member@email.com", true, profileImage));
        socialMemberRepository.save(createSocialMember(null, member, "member", SocialType.NAVER));

        mockMvc.perform(post("/api/members/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonString(createLoginRequest("member@email.com", "fail!@"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.FAILED_LOGIN.getMessage()));
    }


    @Test
    @DisplayName("로그아웃")
    void logout() throws Exception{
        Member member = memberRepository.save(createMember(null, "member@email.com"));
        String accessToken = jwtUtils.createToken(member.getEmail(), 3600);

        mockMvc.perform(patch("/api/members/logout")
                        .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJsonString(createLogoutRequest(member.getNickname()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(SuccessCode.GENERAL_SUCCESS.getMessage()));
    }

    @Test
    @DisplayName("로그아웃 시 존재하지 않는 사용자 요청으로 인해 예외 발생")
    void logout_dataNotFoundException() throws Exception{
        Member member = memberRepository.save(createMember(null, "member@email.com"));
        String accessToken = jwtUtils.createToken(member.getEmail(), 3600);

        mockMvc.perform(patch("/api/members/logout")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonString(createLogoutRequest("notMember"))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.MEMBER_NOT_FOUND.getMessage()));
    }

    @Test
    @DisplayName("토큰 갱신")
    void refreshToken() throws Exception{
        Member member = memberRepository.save(createMember(null, "member@email.com"));
        String refreshToken = jwtUtils.createToken(member.getEmail(), 10000000);
        member.updateRefreshToken(refreshToken);

        Cookie cookie = createRefreshTokenCookie(refreshToken);

        mockMvc.perform(post("/api/members/refresh")
                                .cookie(cookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(SuccessCode.GENERAL_SUCCESS.getMessage()));
    }

    @Test
    @DisplayName("토큰 갱신 시 쿠키 존재하지 않아 예외 발생")
    void refreshToken_noCookie() throws Exception{
        mockMvc.perform(post("/api/members/refresh"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("/api/members/refresh : " + ErrorCode.MISMATCH_REFRESH_TOKEN.getMessage()));
    }

    @Test
    @DisplayName("토큰 갱신 시 쿠키에 refreshToken 정보 없어서 예외 발생")
    void refreshToken_notRefreshTokenCookie() throws Exception{
        Cookie cookie = new Cookie("error", "error");

        mockMvc.perform(post("/api/members/refresh")
                        .cookie(cookie))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("/api/members/refresh : " + ErrorCode.MISMATCH_REFRESH_TOKEN.getMessage()));
    }


    @Test
    @DisplayName("토큰 갱신 시 refresh token 만료로 예외 발생")
    void refreshToken_unauthorizedExpiredException() throws Exception {
        String refreshToken = jwtUtils.createToken("member", -604800000);
        Cookie cookie = createRefreshTokenCookie(refreshToken);

        mockMvc.perform(post("/api/members/refresh")
                        .cookie(cookie))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("/api/members/refresh : " + ErrorCode.EXPIRED_JWT_TOKEN.getMessage()));
    }

    @Test
    @DisplayName("토큰 갱신 시 사용자 데이터 존재하지 않아 예외 발생")
    void refreshToken_memberNotFoundException() throws Exception {
        String refreshToken = jwtUtils.createToken("member", 100000000);
        Cookie cookie = createRefreshTokenCookie(refreshToken);

        mockMvc.perform(post("/api/members/refresh")
                        .cookie(cookie))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.MEMBER_NOT_FOUND.getMessage()));

    }

    @Test
    @DisplayName("토큰 갱신 시 사용자가 요청과 저장된 refresh token 값이 달라 예외 발생")
    void refreshToken_NotEqualsRefreshToken() throws Exception {
        Member member = memberRepository.save(createMember(null, "member@email.com"));
        String refreshToken = jwtUtils.createToken(member.getEmail(), 10000000);
        Cookie cookie = createRefreshTokenCookie(refreshToken);

        mockMvc.perform(post("/api/members/refresh")
                        .cookie(cookie))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("/api/members/refresh : " + ErrorCode.MISMATCH_REFRESH_TOKEN.getMessage()));

    }

    @Test
    @DisplayName("비밀번호 찾기")
    void findPassword() throws Exception{
        memberRepository.save(createMember(null, "member@email.com"));

        mockMvc.perform(post("/api/members/find-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonString(createFindPasswordRequest("member@email.com"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(SuccessCode.GENERAL_SUCCESS.getMessage()));
    }

    @Test
    @DisplayName("소셜 사용자 비밀번호 찾기")
    void findPassword_socialMember() throws Exception{
        ProfileImage profileImage = profileImageRepository.save(createProfileImage(null, "member"));
        Member member = memberRepository.save(createMember(null, "member@email.com", true, profileImage));
        socialMemberRepository.save(createSocialMember(null, member, "member", SocialType.NAVER));

        mockMvc.perform(post("/api/members/find-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonString(createFindPasswordRequest("member@email.com"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(SuccessCode.GENERAL_SUCCESS.getMessage()));
    }

    @Test
    @DisplayName("비밀번호 찾기 시 사용자 데이터 존재하지 않아 예외 발생")
    void findPassword_memberNotFoundException() throws Exception{
        memberRepository.save(createMember(null, "member@email.com"));

        mockMvc.perform(post("/api/members/find-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonString(createFindPasswordRequest("notMember"))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.MEMBER_NOT_FOUND.getMessage()));
    }

    @Test
    @DisplayName("비밀번호 초기화")
    void resetPassword() throws Exception{
        Member member = memberRepository.save(createMember(null, "member@email.com"));
        when(redisUtils.getData(anyString())).thenReturn(member.getEmail());

        mockMvc.perform(patch("/api/members/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonString(createResetPasswordRequest("changePassword", "password12!@", "password12!@"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(SuccessCode.GENERAL_SUCCESS.getMessage()));

    }


    @Test
    @DisplayName("소셜 사용자 비밀번호 초기화")
    void resetPassword_socialMember() throws Exception{
        ProfileImage profileImage = profileImageRepository.save(createProfileImage(null, "member"));
        Member member = memberRepository.save(createMember(null, "member@email.com", true, profileImage));
        socialMemberRepository.save(createSocialMember(null, member, "member", SocialType.NAVER));

        when(redisUtils.getData(anyString())).thenReturn(member.getEmail());

        mockMvc.perform(patch("/api/members/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonString(createResetPasswordRequest("changePassword", "password12!@", "password12!@"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(SuccessCode.GENERAL_SUCCESS.getMessage()));

    }

    @Test
    @DisplayName("비밀번호 초기화 시 비밀번호와 재입력 비밀번호가 달라 예외 발생")
    void resetPassword_notMathPassword() throws Exception{
        memberRepository.save(createMember(null, "member@email.com"));

        mockMvc.perform(patch("/api/members/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonString(createResetPasswordRequest("changePassword", "password12!@", "password34!@"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.INCORRECT_PASSWORD_REPASSWORD.getMessage()));

    }

    @Test
    @DisplayName("비밀번호 초기화 시 저장된 비밀번호 변경 토큰이 존재하지 않아 예외 발생")
    void changePassword_PasswordTokenNotFoundException() throws Exception{
        memberRepository.save(createMember(null, "member@email.com"));

        mockMvc.perform(patch("/api/members/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonString(createResetPasswordRequest("changePassword", "password12!@", "password12!@"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.INVALID_CHANGE_PASSWORD.getMessage()));

    }

    @Test
    @DisplayName("비밀번호 초기화 시 사용자 데이터 존재하지 않아 예외 발생")
    void resetPassword_memberNotFoundException() throws Exception{
        when(redisUtils.getData(anyString())).thenReturn("noMember@email.com");

        mockMvc.perform(patch("/api/members/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonString(createResetPasswordRequest("changePassword", "password12!@", "password12!@"))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.MEMBER_NOT_FOUND.getMessage()));

    }

    @Test
    @WithMockUser("member@email.com")
    @DisplayName("비밀번호 변경")
    void changePassword() throws Exception {
        String encodePassword = passwordEncoder.encode("test123@");
        ProfileImage profileImage = profileImageRepository.save(createProfileImage(null, "member"));
        memberRepository.save(createMember(null, "member@email.com", encodePassword, profileImage));

        ChangePasswordRequest request = createChangePasswordRequest("test123@", "test123!", "test123!");

        mockMvc.perform(patch("/api/members/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(SuccessCode.GENERAL_SUCCESS.getMessage()));
    }

    @Test
    @WithMockUser("member@email.com")
    @DisplayName("비밀번호 변경 시 입력값 조건 틀려 예외 발생")
    void changePassword_MethodArgumentNotValidException() throws Exception{
        memberRepository.save(createMember(null, "member@email.com"));
        ChangePasswordRequest request = createChangePasswordRequest("틀린값1", "test123!", "test123!");

        mockMvc.perform(patch("/api/members/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("비밀번호는 8자 이상 15자 이하의 영문, 숫자, 특수문자 조합이어야 합니다."));
    }


    @Test
    @WithMockUser("member@email.com")
    @DisplayName("비밀번호 변경 시 변경 비밀번호와 재입력 비밀번호가 일치하지 않아 예외 발생")
    void changePassword_inCorrectNewPassword() throws Exception{
        memberRepository.save(createMember(null, "member@email.com"));
        ChangePasswordRequest request = createChangePasswordRequest("test123@", "test123!", "test456!");

        mockMvc.perform(patch("/api/members/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(ErrorCode.INCORRECT_PASSWORD_REPASSWORD.getMessage()));

    }

    @Test
    @WithMockUser("member@email.com")
    @DisplayName("비밀번호 변경 시 현재 비밀번호와 변경 비밀번호가 같아 예외 발생")
    void changePassword_correctNowPassword() throws Exception{
        memberRepository.save(createMember(null, "member@email.com"));
        ChangePasswordRequest request = createChangePasswordRequest("test123@", "test123@", "test123@");

        mockMvc.perform(patch("/api/members/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(ErrorCode.CORRECT_NOWPASSWORD_NEWPASSWORD.getMessage()));
    }

    @Test
    @WithMockUser("notMember@email.com")
    @DisplayName("비밀번호 변경 시 사용자 정보를 찾을 수 없어 예외 발생")
    void changePassword_memberNotFoundException() throws Exception{
        ChangePasswordRequest request = createChangePasswordRequest("test123@", "test123!", "test123!");

        mockMvc.perform(patch("/api/members/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(ErrorCode.MEMBER_NOT_FOUND.getMessage()));
    }

    @Test
    @WithMockUser("member@email.com")
    @DisplayName("비밀번호 변경 시 저장된 비밀번호와 현재 비밀번호가 일치하지 않아 예외 발생")
    void changePassword_incorrectSavedPassword() throws Exception{
        String encodePassword = passwordEncoder.encode("test123@");
        ProfileImage profileImage = profileImageRepository.save(createProfileImage(null, "member"));
        memberRepository.save(createMember(null, "member@email.com", encodePassword, profileImage));

        ChangePasswordRequest request = createChangePasswordRequest("test123!", "test123!!", "test123!!");

        mockMvc.perform(patch("/api/members/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(ErrorCode.INCORRECT_PASSWORD.getMessage()));
    }

    @Test
    @WithMockUser("member@email.com")
    @DisplayName("사용자 정보 조회")
    void getMemberInfo() throws Exception{
        ProfileImage profileImage = profileImageRepository.save(createProfileImage(null, "profileImage"));
        Member member = memberRepository.save(createMember(null, "member@email.com", profileImage));

        mockMvc.perform(get("/api/members/info"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.email").value(member.getEmail()))
                .andExpect(jsonPath("$.data.nickname").value(member.getNickname()))
                .andExpect(jsonPath("$.data.profileImage").value(profileImage.getS3ObjectUrl()));
    }

    @Test
    @WithMockUser("notMember@email.com")
    @DisplayName("사용자 정보 조회 시 사용자 데이터 없어 예외 발생")
    void getMemberInfo_memberNotFoundException() throws Exception{
        mockMvc.perform(get("/api/members/info"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.MEMBER_NOT_FOUND.getMessage()));
    }


    @Test
    @WithMockUser("member@email.com")
    @DisplayName("사용자 닉네임 변경")
    void changeNickname() throws Exception{
        memberRepository.save(createMember(null, "member@email.com"));
        ChangeNicknameRequest request = createChangeNicknameRequest("newNickname");

        mockMvc.perform(patch("/api/members/change-nickname")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(SuccessCode.GENERAL_SUCCESS.getMessage()));

    }

    @Test
    @WithMockUser("member@email.com")
    @DisplayName("사용자 닉네임 변경 시 입력 조건이 맞지 않아 예외 발생")
    void changeNickname_methodInvalidArgumentException() throws Exception{
        memberRepository.save(createMember(null, "member@email.com"));
        ChangeNicknameRequest request = createChangeNicknameRequest("no");

        mockMvc.perform(patch("/api/members/change-nickname")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("닉네임은 4자 이상 15자 이하의 영문 대/소문자, 한글, 숫자만 사용 가능합니다."));

    }

    @Test
    @WithMockUser("notMember@email.com")
    @DisplayName("사용자 닉네임 변경 시 사용자 데이터 없어 예외 발생")
    void changeNickname_memberNotFoundException() throws Exception{
        ChangeNicknameRequest request = createChangeNicknameRequest("newNickname");

        mockMvc.perform(patch("/api/members/change-nickname")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.MEMBER_NOT_FOUND.getMessage()));
    }

    @Test
    @WithMockUser("member@email.com")
    @DisplayName("사용자 닉네임 변경 시 이미 존재하는 닉네임으로 예외 발생")
    void changeNickname_dataExistException() throws Exception{
        Member member = memberRepository.save(createMember(null, "member@email.com"));
        ChangeNicknameRequest request = createChangeNicknameRequest(member.getNickname());

        mockMvc.perform(patch("/api/members/change-nickname")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.ALREADY_EXISTED_NICKNAME.getMessage()));
    }

    @Test
    @WithMockUser("member@email.com")
    @DisplayName("이메일 변경")
    void changeEmail() throws Exception {
        memberRepository.save(createMember(null, "member@email.com"));
        when(redisUtils.getEmailData(any(), anyString())).thenReturn("true");

        mockMvc.perform(patch("/api/members/change-email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonString(createEmailRequest("changeEmail@email.com"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(SuccessCode.GENERAL_SUCCESS.getMessage()));
    }

    @Test
    @WithMockUser("member@email.com")
    @DisplayName("이메일 변경 시 이미 존재하는 이메일로 예외 발생")
    void changeEmail_duplicateEmail() throws Exception {
        memberRepository.save(createMember(null, "member@email.com"));

        mockMvc.perform(patch("/api/members/change-email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonString(createEmailRequest("member@email.com"))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.ALREADY_EXISTED_EMAIL.getMessage()));;
    }

    @Test
    @WithMockUser("member@email.com")
    @DisplayName("이메일 변경 시 인증되지 않은 이메일로 예외 발생")
    void changeEmail_notVerifiedEmail() throws Exception {
        memberRepository.save(createMember(null, "member"));
        when(redisUtils.getEmailData(any(), anyString())).thenReturn(null);

        mockMvc.perform(patch("/api/members/change-email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonString(createEmailRequest("changeEmail@email.com"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.NOT_VERIFIED_EMAIL.getMessage()));;
    }

    @Test
    @WithMockUser("member@email.com")
    @DisplayName("이메일 변경 시 존재하지 않는 사용자로 예외 발생")
    void changeEmail_memberNotFoundException() throws Exception {
        when(redisUtils.getEmailData(any(), anyString())).thenReturn("true");

        mockMvc.perform(patch("/api/members/change-email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonString(createEmailRequest("changeEmail@email.com"))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.MEMBER_NOT_FOUND.getMessage()));;
    }

    @Test
    @WithMockUser("member@email.com")
    @DisplayName("사용자 북마크 조회 - 최신순")
    void getMemberBookmarks_sortNewest() throws Exception{
        Member member = memberRepository.save(createMember(null, "member@email.com"));
        bookmarkRepository.save(createBookmark(null, member, travelPlace1, LocalDateTime.now()));
        bookmarkRepository.save(createBookmark(null, member, travelPlace2, LocalDateTime.now().minusDays(1)));
        bookmarkRepository.save(createBookmark(null, member, travelPlace3, LocalDateTime.now().minusDays(2)));

        mockMvc.perform(get("/api/members/bookmark")
                .param("page", "1")
                .param("sort", "newest"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(3))
                .andExpect(jsonPath("$.data.content[0].placeName").value(travelPlace1.getPlaceName()))
                .andExpect(jsonPath("$.data.content[0].thumbnailUrl").value(travelImage1.getS3ObjectUrl()))
                .andExpect(jsonPath("$.data.content[1].placeName").value(travelPlace2.getPlaceName()))
                .andExpect(jsonPath("$.data.content[1].thumbnailUrl").value(travelImage2.getS3ObjectUrl()))
                .andExpect(jsonPath("$.data.content[2].placeName").value(travelPlace3.getPlaceName()))
                .andExpect(jsonPath("$.data.content[2].thumbnailUrl").value(travelImage3.getS3ObjectUrl()));
    }

    @Test
    @WithMockUser("member@email.com")
    @DisplayName("사용자 북마크 조회 - 오래된 순")
    void getMemberBookmarks_sortOldest() throws Exception{
        Member member = memberRepository.save(createMember(null, "member@email.com"));
        bookmarkRepository.save(createBookmark(null, member, travelPlace1, LocalDateTime.now()));
        bookmarkRepository.save(createBookmark(null, member, travelPlace2, LocalDateTime.now().minusDays(1)));
        bookmarkRepository.save(createBookmark(null, member, travelPlace3, LocalDateTime.now().minusDays(2)));

        mockMvc.perform(get("/api/members/bookmark")
                        .param("page", "1")
                        .param("sort", "oldest"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(3))
                .andExpect(jsonPath("$.data.content[0].placeName").value(travelPlace3.getPlaceName()))
                .andExpect(jsonPath("$.data.content[0].thumbnailUrl").value(travelImage3.getS3ObjectUrl()))
                .andExpect(jsonPath("$.data.content[1].placeName").value(travelPlace2.getPlaceName()))
                .andExpect(jsonPath("$.data.content[1].thumbnailUrl").value(travelImage2.getS3ObjectUrl()))
                .andExpect(jsonPath("$.data.content[2].placeName").value(travelPlace1.getPlaceName()))
                .andExpect(jsonPath("$.data.content[2].thumbnailUrl").value(travelImage1.getS3ObjectUrl()));
    }

    @Test
    @WithMockUser("member@email.com")
    @DisplayName("사용자 북마크 조회 - 이름순")
    void getMemberBookmarks_sortName() throws Exception{
        Member member = memberRepository.save(createMember(null, "member@email.com"));
        bookmarkRepository.save(createBookmark(null, member, travelPlace1, LocalDateTime.now()));
        bookmarkRepository.save(createBookmark(null, member, travelPlace2, LocalDateTime.now().minusDays(1)));
        bookmarkRepository.save(createBookmark(null, member, travelPlace3, LocalDateTime.now().minusDays(2)));

        mockMvc.perform(get("/api/members/bookmark")
                        .param("page", "1")
                        .param("sort", "name"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(3))
                .andExpect(jsonPath("$.data.content[0].placeName").value(travelPlace1.getPlaceName()))
                .andExpect(jsonPath("$.data.content[0].thumbnailUrl").value(travelImage1.getS3ObjectUrl()))
                .andExpect(jsonPath("$.data.content[1].placeName").value(travelPlace2.getPlaceName()))
                .andExpect(jsonPath("$.data.content[1].thumbnailUrl").value(travelImage2.getS3ObjectUrl()))
                .andExpect(jsonPath("$.data.content[2].placeName").value(travelPlace3.getPlaceName()))
                .andExpect(jsonPath("$.data.content[2].thumbnailUrl").value(travelImage3.getS3ObjectUrl()));
    }

    @Test
    @WithMockUser("member@email.com")
    @DisplayName("사용자 북마크 조회 시 데이터 없는 경우")
    void getMemberBookmarks_emptyData() throws Exception{
        memberRepository.save(createMember(null, "member"));

        mockMvc.perform(get("/api/members/bookmark")
                        .param("page", "1")
                        .param("sort", "newest"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(0))
                .andExpect(jsonPath("$.data.content").isEmpty());
    }

    @Test
    @WithMockUser("member@email.com")
    @DisplayName("사용자 북마크 조회 시 정렬 잘못된 값이 들어와 예외 발생")
    void getMemberBookmarks_IllegalException() throws Exception{
        memberRepository.save(createMember(null, "member"));

        mockMvc.perform(get("/api/members/bookmark")
                        .param("page", "1")
                        .param("sort", "ne"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.ILLEGAL_BOOKMARK_SORT_TYPE.getMessage()));
    }

    @Test
    @WithMockUser("member@email.com")
    @DisplayName("사용자 탈퇴 - 작성자, 참석자 존재하는 경우")
    void deactivateMember1() throws Exception{
        String encodePassword = passwordEncoder.encode("test123@");

        ProfileImage profileImage = profileImageRepository.save(createProfileImage(null, "test"));
        Member member = memberRepository.save(createMember(null, "member@email.com", encodePassword, profileImage));

        TravelSchedule schedule1 = travelScheduleRepository.save(createTravelSchedule(null, "테스트1"));
        TravelSchedule schedule2 = travelScheduleRepository.save(createTravelSchedule(null, "테스트2"));

        travelAttendeeRepository.save(createTravelAttendee(null, member, schedule1, AttendeeRole.AUTHOR, AttendeePermission.ALL));
        travelAttendeeRepository.save(createTravelAttendee(null, member, schedule2, AttendeeRole.GUEST, AttendeePermission.READ));

        chatMessageRepository.save(createChatMessage(null, schedule1.getScheduleId(), member, "테스트1"));
        chatMessageRepository.save(createChatMessage(null, schedule1.getScheduleId(), member, "테스트2"));

        bookmarkRepository.save(createBookmark(null, member, travelPlace1, LocalDateTime.now()));
        bookmarkRepository.save(createBookmark(null, member, travelPlace1, LocalDateTime.now()));

        mockMvc.perform(patch("/api/members/deactivate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonString(createDeactivateRequest("test123@"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(SuccessCode.GENERAL_SUCCESS.getMessage()));
    }

    @Test
    @WithMockUser("member@email.com")
    @DisplayName("사용자 탈퇴 - 작성자만 존재하는 경우")
    void deactivateMember2() throws Exception{
        String encodePassword = passwordEncoder.encode("test123@");

        ProfileImage profileImage = profileImageRepository.save(createProfileImage(null, "test"));
        Member member = memberRepository.save(createMember(null, "member@email.com", encodePassword, profileImage));
        Member otherMember = memberRepository.save(createMember(null, "otherMember"));

        TravelSchedule schedule1 = travelScheduleRepository.save(createTravelSchedule(null, "테스트1"));

        travelAttendeeRepository.save(createTravelAttendee(null, member, schedule1, AttendeeRole.AUTHOR, AttendeePermission.ALL));
        travelAttendeeRepository.save(createTravelAttendee(null, otherMember, schedule1, AttendeeRole.GUEST, AttendeePermission.READ));

        chatMessageRepository.save(createChatMessage(null, schedule1.getScheduleId(), member, "테스트1"));
        chatMessageRepository.save(createChatMessage(null, schedule1.getScheduleId(), member, "테스트2"));

        bookmarkRepository.save(createBookmark(null, member, travelPlace1, LocalDateTime.now()));
        bookmarkRepository.save(createBookmark(null, member, travelPlace1, LocalDateTime.now()));

        mockMvc.perform(patch("/api/members/deactivate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonString(createDeactivateRequest("test123@"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(SuccessCode.GENERAL_SUCCESS.getMessage()));
    }

    @Test
    @WithMockUser("member@email.com")
    @DisplayName("사용자 탈퇴 - 참석자만 존재하는 경우")
    void deactivateMember3() throws Exception{
        String encodePassword = passwordEncoder.encode("test123@");

        ProfileImage profileImage = profileImageRepository.save(createProfileImage(null, "test"));
        Member member = memberRepository.save(createMember(null, "member@email.com", encodePassword, profileImage));

        TravelSchedule schedule1 = travelScheduleRepository.save(createTravelSchedule(null, "테스트1"));

        travelAttendeeRepository.save(createTravelAttendee(null, member, schedule1, AttendeeRole.GUEST, AttendeePermission.READ));

        chatMessageRepository.save(createChatMessage(null, schedule1.getScheduleId(), member, "테스트1"));
        chatMessageRepository.save(createChatMessage(null, schedule1.getScheduleId(), member, "테스트2"));

        bookmarkRepository.save(createBookmark(null, member, travelPlace1, LocalDateTime.now()));
        bookmarkRepository.save(createBookmark(null, member, travelPlace1, LocalDateTime.now()));

        mockMvc.perform(patch("/api/members/deactivate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonString(createDeactivateRequest("test123@"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(SuccessCode.GENERAL_SUCCESS.getMessage()));
    }

    @Test
    @WithMockUser("member@email.com")
    @DisplayName("사용자 탈퇴 - 일정 데이터 없는 경우")
    void deactivateMember4() throws Exception{
        String encodePassword = passwordEncoder.encode("test123@");

        ProfileImage profileImage = profileImageRepository.save(createProfileImage(null, "test"));
        Member member = memberRepository.save(createMember(null, "member@email.com", encodePassword, profileImage));

        bookmarkRepository.save(createBookmark(null, member, travelPlace1, LocalDateTime.now()));
        bookmarkRepository.save(createBookmark(null, member, travelPlace1, LocalDateTime.now()));

        mockMvc.perform(patch("/api/members/deactivate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonString(createDeactivateRequest("test123@"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(SuccessCode.GENERAL_SUCCESS.getMessage()));
    }

    @Test
    @WithMockUser("notMember@email.com")
    @DisplayName("사용자 탈퇴 시 사용자 데이터를 찾을 수 없는 경우")
    void deactivateMember_memberNotFoundException() throws Exception{
        mockMvc.perform(patch("/api/members/deactivate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonString(createDeactivateRequest("test123@"))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.MEMBER_NOT_FOUND.getMessage()));
    }

    @Test
    @WithMockUser("member@email.com")
    @DisplayName("사용자 탈퇴 시 비밀번호가 맞지 않아 예외 발생")
    void deactivateMember_incorrectPassword() throws Exception{
        String encodePassword = passwordEncoder.encode("incorrect12@");

        ProfileImage profileImage = profileImageRepository.save(createProfileImage(null, "test"));
        memberRepository.save(createMember(null, "member@email.com", encodePassword, profileImage));

        mockMvc.perform(patch("/api/members/deactivate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonString(createDeactivateRequest("test123@"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.INCORRECT_PASSWORD.getMessage()));
    }





}
