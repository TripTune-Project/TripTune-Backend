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
import com.triptune.member.dto.request.*;
import com.triptune.member.enums.JoinType;
import com.triptune.member.enums.SocialType;
import com.triptune.member.repository.SocialMemberRepository;
import com.triptune.schedule.entity.TravelSchedule;
import com.triptune.schedule.enums.AttendeePermission;
import com.triptune.schedule.enums.AttendeeRole;
import com.triptune.schedule.repository.ChatMessageRepository;
import com.triptune.schedule.repository.TravelAttendeeRepository;
import com.triptune.schedule.repository.TravelScheduleRepository;
import com.triptune.travel.entity.TravelImage;
import com.triptune.travel.entity.TravelPlace;
import com.triptune.travel.repository.TravelImageRepository;
import com.triptune.travel.repository.TravelPlaceRepository;
import com.triptune.global.s3.S3Service;
import com.triptune.email.service.EmailService;
import com.triptune.member.MemberTest;
import com.triptune.member.entity.Member;
import com.triptune.profile.entity.ProfileImage;
import com.triptune.member.repository.MemberRepository;
import com.triptune.profile.repository.ProfileImageRepository;
import com.triptune.global.response.enums.ErrorCode;
import com.triptune.global.response.enums.SuccessCode;
import com.triptune.global.security.jwt.JwtUtils;
import com.triptune.global.redis.RedisService;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
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
    @Autowired
    private WebApplicationContext wac;
    @Autowired
    private JwtUtils jwtUtils;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private ProfileImageRepository profileImageRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private TravelPlaceRepository travelPlaceRepository;
    @Autowired
    private CountryRepository countryRepository;
    @Autowired
    private CityRepository cityRepository;
    @Autowired
    private DistrictRepository districtRepository;
    @Autowired
    private TravelImageRepository travelImageRepository;
    @Autowired
    private ApiCategoryRepository apiCategoryRepository;
    @Autowired
    private BookmarkRepository bookmarkRepository;
    @Autowired
    private TravelAttendeeRepository travelAttendeeRepository;
    @Autowired
    private TravelScheduleRepository travelScheduleRepository;
    @Autowired
    private ChatMessageRepository chatMessageRepository;
    @Autowired
    private SocialMemberRepository socialMemberRepository;

    @MockBean
    private RedisService redisService;
    @MockBean
    private EmailService emailService;
    @MockBean
    private S3Service s3Service;

    private MockMvc mockMvc;
    private TravelPlace travelPlace1;
    private TravelPlace travelPlace2;
    private TravelPlace travelPlace3;

    private TravelImage travelImage1;
    private TravelImage travelImage2;
    private TravelImage travelImage3;


    @BeforeEach
    void setUp() {
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
        // given
        when(redisService.getEmailData(any(), anyString())).thenReturn("true");

        JoinRequest request = createMemberRequest(
                "member@email.com",
                "password12!@",
                "password12!@",
                "nickname"
        );

        // when, then
        mockMvc.perform(post("/api/members/join")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(SuccessCode.GENERAL_SUCCESS.getMessage()));
    }


    @ParameterizedTest
    @DisplayName("회원가입 시 이메일 빈 값으로 예외 발생")
    @ValueSource(strings = {"", " "})
    void join_invalidNotBlankEmail(String input) throws Exception {
        // given
        JoinRequest request = createMemberRequest(
                input,
                "password12!@",
                "password12!@",
                "nickname"
        );

        // when, then
        mockMvc.perform(post("/api/members/join")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("이메일은 필수 입력 값입니다.")));
    }

    @Test
    @DisplayName("회원가입 시 이메일 null 값이 들어와 예외 발생")
    void join_invalidNullEmail() throws Exception {
        // given
        JoinRequest request = createMemberRequest(
                null,
                "password12!@",
                "password12!@",
                "nickname"
        );


        // when, then
        mockMvc.perform(post("/api/members/join")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("이메일은 필수 입력 값입니다.")));

    }

    @ParameterizedTest
    @DisplayName("회원가입 시 이메일 형식에 맞지 않아 예외 발생")
    @ValueSource(strings = {"test", "test@", "test$email.com"})
    void join_invalidEmail(String input) throws Exception {
        // given
        JoinRequest request = createMemberRequest(
                input,
                "password12!@",
                "password12!@",
                "nickname"
        );

        // when, then
        mockMvc.perform(post("/api/members/join")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("이메일 형식에 맞지 않습니다.")));
    }


    @ParameterizedTest
    @DisplayName("회원가입 시 비밀번호 빈 값으로 예외 발생")
    @ValueSource(strings = {"", " "})
    void join_invalidNotBlankPassword(String input) throws Exception {
        // given
        JoinRequest request = createMemberRequest(
                "member@email.com",
                input,
                "password12!@",
                "nickname"
        );

        // when, then
        mockMvc.perform(post("/api/members/join")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("비밀번호는 필수 입력 값입니다.")));
    }

    @Test
    @DisplayName("회원가입 시 비밀번호 null 값이 들어와 예외 발생")
    void join_invalidNullPassword() throws Exception {
        // given
        JoinRequest request = createMemberRequest(
                "member@email.com",
                null,
                "password12!@",
                "nickname"
        );

        // when, then
        mockMvc.perform(post("/api/members/join")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("비밀번호는 필수 입력 값입니다.")));
    }

    @ParameterizedTest
    @DisplayName("회원가입 시 비밀번호 유효성 검사로 예외 발생")
    @ValueSource(strings = {"p", "p1@", "password", "1@", "passworddddddddd", "passworddddddddd!@", "password!@"})
    void join_invalidPassword(String input) throws Exception {
        // given
        JoinRequest request = createMemberRequest(
                "member@email.com",
                input,
                "password12!@",
                "nickname"
        );

        // when, then
        mockMvc.perform(post("/api/members/join")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("비밀번호는 8자 이상 15자 이하의 영문, 숫자, 특수문자 조합이어야 합니다."));
    }

    @ParameterizedTest
    @DisplayName("회원가입 시 비밀번호 재입력 빈 값으로 예외 발생")
    @ValueSource(strings = {"", " "})
    void join_invalidNotBlankRePassword(String input) throws Exception {
        // given
        JoinRequest request = createMemberRequest(
                "member@email.com",
                "password12!@",
                input,
                "nickname"
        );

        // when, then
        mockMvc.perform(post("/api/members/join")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("비밀번호 재입력은 필수 입력 값입니다.")));
    }

    @Test
    @DisplayName("회원가입 시 비밀번호 재입력 null 값이 들어와 예외 발생")
    void join_invalidNullRePassword() throws Exception {
        // given
        JoinRequest request = createMemberRequest(
                "member@email.com",
                "password12!@",
                null,
                "nickname"
        );

        // when, then
        mockMvc.perform(post("/api/members/join")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("비밀번호 재입력은 필수 입력 값입니다.")));
    }

    @ParameterizedTest
    @DisplayName("회원가입 시 비밀번호 재입력 유효성 검사로 예외 발생")
    @ValueSource(strings = {"p", "p1@", "password", "1@", "passworddddddddd", "passworddddddddd!@", "password!@"})
    void join_invalidRePassword(String input) throws Exception {
        // given
        JoinRequest request = createMemberRequest(
                "member@email.com",
                "password12!@",
                input,
                "nickname"
        );

        // when, then
        mockMvc.perform(post("/api/members/join")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("비밀번호는 8자 이상 15자 이하의 영문, 숫자, 특수문자 조합이어야 합니다."));
    }


    @ParameterizedTest
    @DisplayName("회원가입 시 닉네임 빈 값으로 예외 발생")
    @ValueSource(strings = {"", " "})
    void join_invalidNotBlankNickname(String input) throws Exception {
        // given
        JoinRequest request = createMemberRequest(
                "member@email.com",
                "password12!@",
                "password12!@",
                input
        );

        // when, then
        mockMvc.perform(post("/api/members/join")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("닉네임은 필수 입력 값입니다.")));
    }

    @Test
    @DisplayName("회원가입 시 닉네임 null 값이 들어와 예외 발생")
    void join_invalidNullNickname() throws Exception {
        // given
        JoinRequest request = createMemberRequest(
                "member@email.com",
                "password12!@",
                "password12!@",
                null
        );

        // when, then
        mockMvc.perform(post("/api/members/join")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("닉네임은 필수 입력 값입니다.")));
    }

    @ParameterizedTest
    @DisplayName("회원가입 시 닉네임 입력값 검사로 예외 발생")
    @ValueSource(strings = {"n", "닉", "1", "1@", "닉네임임임임임임임임임임임임임임", "@@@@@@@@@@@@@@@"})
    void join_invalidNickname(String input) throws Exception {
        // given
        JoinRequest request = createMemberRequest(
                "member@email.com",
                "password12!@",
                "password12!@",
                input
        );

        // when, then
        mockMvc.perform(post("/api/members/join")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("닉네임은 4자 이상 15자 이하이며, 한글 또는 영문자가 반드시 포함되어야 합니다."));
    }

    @Test
    @DisplayName("회원가입 시 비밀번호, 비밀번호 재입력 불일치로 인한 예외 발생")
    void join_incorrectPasswordAndRePassword() throws Exception {
        // given
        JoinRequest request = createMemberRequest(
                "member@email.com",
                "password12!@",
                "repassword12!@",
                "nickname"
        );

        // when, then
        mockMvc.perform(post("/api/members/join")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.INCORRECT_PASSWORD_REPASSWORD.getMessage()));
    }

    @Test
    @DisplayName("회원가입 시 이미 존재하는 이메일로 인해 예외 발생")
    void join_existedEmail() throws Exception {
        // given
        JoinRequest request = createMemberRequest(
                "member@email.com",
                "password12!@",
                "password12!@",
                "nickname"
        );

        memberRepository.save(createMember(null, "member@email.com"));

        // when, then
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
        // given
        JoinRequest request = createMemberRequest(
                "member@email.com",
                "password12!@",
                "password12!@",
                "nickname"
        );

        // then, then
        mockMvc.perform(post("/api/members/join")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.NOT_VERIFIED_EMAIL.getMessage()));
        ;
    }


    @Test
    @DisplayName("자체 로그인")
    void login() throws Exception {
        String encodePassword = passwordEncoder.encode("password12!@");
        ProfileImage profileImage = profileImageRepository.save(createProfileImage(null, "member"));
        Member member = memberRepository.save(createNativeTypeMember(null, "member@email.com", encodePassword, profileImage));

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
    @DisplayName("통합 회원 로그인")
    void login_bothMember() throws Exception {
        String encodePassword = passwordEncoder.encode("password12!@");
        ProfileImage profileImage = profileImageRepository.save(createProfileImage(null, "member"));
        Member member = memberRepository.save(createBothTypeMember(null, "member@email.com", encodePassword, profileImage));
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


    @ParameterizedTest
    @DisplayName("로그인 시 이메일 빈 값으로 예외 발생")
    @ValueSource(strings = {"", " "})
    void login_invalidNotBlankEmail(String input) throws Exception {
        // given
        LoginRequest request = createLoginRequest(
                input,
                "password12!@"
        );

        // when, then
        mockMvc.perform(post("/api/members/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("이메일은 필수 입력 값입니다.")));
    }

    @Test
    @DisplayName("로그인 시 이메일 null 값이 들어와 예외 발생")
    void login_invalidNullEmail() throws Exception {
        // given
        LoginRequest request = createLoginRequest(
                null,
                "password12!@"
        );

        // when, then
        mockMvc.perform(post("/api/members/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("이메일은 필수 입력 값입니다.")));
    }


    @ParameterizedTest
    @DisplayName("로그인 시 이메일 형식에 맞지 않아 예외 발생")
    @ValueSource(strings = {"test", "test@", "test$email.com"})
    void login_invalidEmail(String input) throws Exception {
        // given
        LoginRequest request = createLoginRequest(
                input,
                "password12!@"
        );

        // when, then
        mockMvc.perform(post("/api/members/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("이메일 형식에 맞지 않습니다.")));
    }


    @ParameterizedTest
    @DisplayName("로그인 시 비밀번호 빈 값으로 예외 발생")
    @ValueSource(strings = {"", " "})
    void login_invalidNotBlankPassword(String input) throws Exception {
        // given
        LoginRequest request = createLoginRequest(
                "member@email.com",
                input
        );

        // when, then
        mockMvc.perform(post("/api/members/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("비밀번호는 필수 입력 값입니다.")));
    }

    @Test
    @DisplayName("로그인 시 비밀번호 null 값이 들어와 예외 발생")
    void login_invalidNullPassword() throws Exception {
        // given
        LoginRequest request = createLoginRequest(
                "member@email.com",
                null
        );

        // when, then
        mockMvc.perform(post("/api/members/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("비밀번호는 필수 입력 값입니다.")));
    }


    @Test
    @DisplayName("로그인 시 이메일 맞지 않아 예외 발생")
    void login_incorrectEmail() throws Exception {
        mockMvc.perform(post("/api/members/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonString(createLoginRequest("fail@email.com", "password12!@"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.FAILED_LOGIN.getMessage()));
    }

    @Test
    @DisplayName("로그인 시 비밀번호 맞지 않아 예외 발생")
    void login_incorrectPassword() throws Exception {
        memberRepository.save(createMember(null, "member@email.com"));

        mockMvc.perform(post("/api/members/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonString(createLoginRequest("member@email.com", "fail!@"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.FAILED_LOGIN.getMessage()));
    }

    @Test
    @DisplayName("소셜 회원이 자체 로그인 시도해 예외 발생")
    void login_socialMember() throws Exception {
        ProfileImage profileImage = profileImageRepository.save(createProfileImage(null, "member"));
        Member member = memberRepository.save(createSocialTypeMember(null, "member@email.com", profileImage));
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
    void logout() throws Exception {
        Member member = memberRepository.save(createMember(null, "member@email.com"));
        String accessToken = jwtUtils.createAccessToken(member.getMemberId());

        MvcResult result = mockMvc.perform(patch("/api/members/logout")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonString(createLogoutRequest(member.getNickname()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(SuccessCode.GENERAL_SUCCESS.getMessage()))
                .andReturn();

        Cookie[] cookies = result.getResponse().getCookies();

        assertThat(cookies).isNotNull();
        assertThat(cookies.length).isEqualTo(3);
    }


    @ParameterizedTest
    @DisplayName("로그아웃 시 닉네임 빈 값으로 예외 발생")
    @ValueSource(strings = {"", " "})
    void logout_invalidNotBlankNickname(String input) throws Exception {
        // given
        Member member = memberRepository.save(createMember(null, "member@email.com"));
        String accessToken = jwtUtils.createAccessToken(member.getMemberId());

        LogoutRequest request = createLogoutRequest(input);

        // when, then
        mockMvc.perform(patch("/api/members/logout")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("닉네임은 필수 입력 값입니다.")));

    }

    @Test
    @DisplayName("로그아웃 시 닉네임 null 값이 들어와 예외 발생")
    void logout_invalidNullNickname() throws Exception {
        // given
        Member member = memberRepository.save(createMember(null, "member@email.com"));
        String accessToken = jwtUtils.createAccessToken(member.getMemberId());

        LogoutRequest request = createLogoutRequest(null);

        // when, then
        mockMvc.perform(patch("/api/members/logout")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("닉네임은 필수 입력 값입니다.")));
    }

    @ParameterizedTest
    @DisplayName("로그아웃 시 닉네임 입력값 검사로 예외 발생")
    @ValueSource(strings = {"n", "닉", "1", "1@", "닉네임임임임임임임임임임임임임임", "@@@@@@@@@@@@@@@", "12345", "행복1"})
    void logout_invalidNickname(String input) throws Exception {
        // given
        Member member = memberRepository.save(createMember(null, "member@email.com"));
        String accessToken = jwtUtils.createAccessToken(member.getMemberId());

        LogoutRequest request = createLogoutRequest(input);

        // when, then
        mockMvc.perform(patch("/api/members/logout")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("닉네임은 4자 이상 15자 이하이며, 한글 또는 영문자가 반드시 포함되어야 합니다."));
    }


    @Test
    @DisplayName("로그아웃 시 회원 데이터 없어 예외 발생")
    void logout_memberNotFound() throws Exception {
        // given
        Member member = memberRepository.save(createMember(null, "member@email.com"));
        String accessToken = jwtUtils.createAccessToken(member.getMemberId());

        // when, then
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
    void refreshToken() throws Exception {
        // given
        Member member = memberRepository.save(createMember(null, "member@email.com"));
        String refreshToken = jwtUtils.createRefreshToken(member.getMemberId());
        member.updateRefreshToken(refreshToken);

        Cookie cookie = createRefreshTokenCookie(refreshToken);

        // when, then
        mockMvc.perform(post("/api/members/refresh")
                        .cookie(cookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(SuccessCode.GENERAL_SUCCESS.getMessage()))
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty());
    }

    @Test
    @DisplayName("토큰 갱신 시 쿠키 존재하지 않아 예외 발생")
    void refreshToken_noCookie() throws Exception {
        mockMvc.perform(post("/api/members/refresh"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("/api/members/refresh : " + ErrorCode.MISMATCH_REFRESH_TOKEN.getMessage()));
    }

    @Test
    @DisplayName("토큰 갱신 시 쿠키에 refreshToken 정보 없어서 예외 발생")
    void refreshToken_notRefreshTokenCookie() throws Exception {
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
        String refreshToken = jwtUtils.createToken("ExpiredRefreshToken", -604800000);
        Cookie cookie = createRefreshTokenCookie(refreshToken);

        mockMvc.perform(post("/api/members/refresh")
                        .cookie(cookie))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("/api/members/refresh : " + ErrorCode.EXPIRED_JWT_TOKEN.getMessage()));
    }

    @Test
    @DisplayName("토큰 갱신 시 회원 데이터 존재하지 않아 예외 발생")
    void refreshToken_memberNotFound() throws Exception {
        String refreshToken = jwtUtils.createRefreshToken(0L);
        Cookie cookie = createRefreshTokenCookie(refreshToken);

        mockMvc.perform(post("/api/members/refresh")
                        .cookie(cookie))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.MEMBER_NOT_FOUND.getMessage()));

    }

    @Test
    @DisplayName("토큰 갱신 시 요청 refreshToken 과 저장된 refresh token 값이 달라 예외 발생")
    void refreshToken_NotEqualsRefreshToken() throws Exception {
        Member member = memberRepository.save(createMember(null, "member@email.com"));
        String refreshToken = jwtUtils.createRefreshToken(member.getMemberId());
        Cookie cookie = createRefreshTokenCookie(refreshToken);

        mockMvc.perform(post("/api/members/refresh")
                        .cookie(cookie))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("/api/members/refresh : " + ErrorCode.MISMATCH_REFRESH_TOKEN.getMessage()));

    }

    @Test
    @DisplayName("일반 회원 비밀번호 찾기")
    void findPassword_nativeMember() throws Exception {
        ProfileImage profileImage = profileImageRepository.save(createProfileImage(null, "member"));
        String encodedPassword = passwordEncoder.encode("password12!@");
        Member member = memberRepository.save(createNativeTypeMember(null, "member@email.com", encodedPassword, profileImage));

        mockMvc.perform(post("/api/members/find-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonString(createFindPasswordRequest(member.getEmail()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(SuccessCode.GENERAL_SUCCESS.getMessage()));
    }

    @Test
    @DisplayName("소셜 회원 비밀번호 찾기")
    void findPassword_socialMember() throws Exception {
        ProfileImage profileImage = profileImageRepository.save(createProfileImage(null, "member"));
        Member member = memberRepository.save(createSocialTypeMember(null, "member@email.com", profileImage));
        socialMemberRepository.save(createSocialMember(null, member, "member", SocialType.NAVER));

        mockMvc.perform(post("/api/members/find-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonString(createFindPasswordRequest(member.getEmail()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(SuccessCode.GENERAL_SUCCESS.getMessage()));
    }

    @Test
    @DisplayName("통합 회원 비밀번호 찾기")
    void findPassword_bothMember() throws Exception {
        ProfileImage profileImage = profileImageRepository.save(createProfileImage(null, "member"));
        String encodedPassword = passwordEncoder.encode("password12!@");
        Member member = memberRepository.save(createBothTypeMember(null, "member@email.com", encodedPassword, profileImage));
        socialMemberRepository.save(createSocialMember(null, member, "member", SocialType.NAVER));

        mockMvc.perform(post("/api/members/find-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonString(createFindPasswordRequest(member.getEmail()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(SuccessCode.GENERAL_SUCCESS.getMessage()));
    }


    @ParameterizedTest
    @DisplayName("비밀번호 찾기 시 이메일 빈 값으로 예외 발생")
    @ValueSource(strings = {"", " "})
    void findPassword_invalidNotBlankEmail(String input) throws Exception {
        // given
        FindPasswordRequest request = createFindPasswordRequest(input);

        // when, then
        mockMvc.perform(post("/api/members/find-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("이메일은 필수 입력 값입니다.")));
    }

    @Test
    @DisplayName("비밀번호 찾기 시 이메일 null 값이 들어와 예외 발생")
    void findPassword_invalidNullEmail() throws Exception {
        // given
        FindPasswordRequest request = createFindPasswordRequest(null);

        // when, then
        mockMvc.perform(post("/api/members/find-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("이메일은 필수 입력 값입니다.")));
    }


    @ParameterizedTest
    @DisplayName("비밀번호 찾기 시 이메일 형식에 맞지 않아 예외 발생")
    @ValueSource(strings = {"test", "test@", "test$email.com"})
    void findPassword_invalidEmail(String input) throws Exception {
        // given
        FindPasswordRequest request = createFindPasswordRequest(input);

        // when, then
        mockMvc.perform(post("/api/members/find-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("이메일 형식에 맞지 않습니다.")));
    }

    @Test
    @DisplayName("비밀번호 찾기 시 회원 데이터 존재하지 않아 예외 발생")
    void findPassword_memberNotFound() throws Exception {
        mockMvc.perform(post("/api/members/find-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonString(createFindPasswordRequest("notMember@email.com"))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.MEMBER_NOT_FOUND.getMessage()));
    }


    @Test
    @DisplayName("일반 회원 비밀번호 초기화")
    void resetPassword_nativeMember() throws Exception {
        // given
        ResetPasswordRequest request = createResetPasswordRequest("changePassword", "password12!@", "password12!@");

        ProfileImage profileImage = profileImageRepository.save(createProfileImage(null, "profileImage"));
        String savedPassword = passwordEncoder.encode("savedPassword12!@");
        Member member = memberRepository.save(createNativeTypeMember(null, "member@email.com", savedPassword, profileImage));

        when(redisService.getData(anyString())).thenReturn(member.getEmail());

        // when, then
        mockMvc.perform(patch("/api/members/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(SuccessCode.GENERAL_SUCCESS.getMessage()));

        assertThat(passwordEncoder.matches(request.getPassword(), member.getPassword())).isTrue();
        assertThat(member.getJoinType()).isEqualTo(JoinType.NATIVE);
        assertThat(member.getUpdatedAt()).isNotNull();
    }


    @Test
    @DisplayName("소셜 회원 비밀번호 초기화")
    void resetPassword_socialMember() throws Exception {
        // given
        ResetPasswordRequest request = createResetPasswordRequest("changePassword", "password12!@", "password12!@");

        ProfileImage profileImage = profileImageRepository.save(createProfileImage(null, "member"));
        Member member = memberRepository.save(createSocialTypeMember(null, "member@email.com", profileImage));
        socialMemberRepository.save(createSocialMember(null, member, "member", SocialType.NAVER));

        when(redisService.getData(anyString())).thenReturn(member.getEmail());

        // when, then
        mockMvc.perform(patch("/api/members/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(SuccessCode.GENERAL_SUCCESS.getMessage()));

        assertThat(passwordEncoder.matches(request.getPassword(), member.getPassword())).isTrue();
        assertThat(member.getJoinType()).isEqualTo(JoinType.BOTH);
        assertThat(member.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("통합 회원 비밀번호 초기화")
    void resetPassword_bothMember() throws Exception {
        // given
        ResetPasswordRequest request = createResetPasswordRequest("changePassword", "password12!@", "password12!@");

        ProfileImage profileImage = profileImageRepository.save(createProfileImage(null, "member"));
        String savedPassword = passwordEncoder.encode("savedPassword12!@");
        Member member = memberRepository.save(createBothTypeMember(null, "member@email.com", savedPassword, profileImage));
        socialMemberRepository.save(createSocialMember(null, member, "member", SocialType.NAVER));

        when(redisService.getData(anyString())).thenReturn(member.getEmail());

        // when, then
        mockMvc.perform(patch("/api/members/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(SuccessCode.GENERAL_SUCCESS.getMessage()));

        assertThat(passwordEncoder.matches(request.getPassword(), member.getPassword())).isTrue();
        assertThat(member.getJoinType()).isEqualTo(JoinType.BOTH);
        assertThat(member.getUpdatedAt()).isNotNull();
    }


    @ParameterizedTest
    @DisplayName("비밀번호 초기화 시 비밀번호 변경 토큰 빈 값으로 예외 발생")
    @ValueSource(strings = {"", " "})
    void resetPassword_invalidNotBlankPasswordToken(String input) throws Exception {
        // given
        ResetPasswordRequest request = createResetPasswordRequest(input, "password12!@", "password12!@");

        // when, then
        mockMvc.perform(patch("/api/members/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("비밀번호 변경 토큰은 필수 입력 값입니다.")));
    }

    @Test
    @DisplayName("비밀번호 초기화 시 비밀번호 변경 토큰 null 값이 들어와 예외 발생")
    void resetPassword_invalidNullPasswordToken() throws Exception {
        // given
        ResetPasswordRequest request = createResetPasswordRequest(null, "password12!@", "password12!@");

        // when, then
        mockMvc.perform(patch("/api/members/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("비밀번호 변경 토큰은 필수 입력 값입니다.")));

    }

    @ParameterizedTest
    @DisplayName("비밀번호 초기화 시 비밀번호 빈 값으로 예외 발생")
    @ValueSource(strings = {"", " "})
    void resetPassword_invalidNotBlankPassword(String input) throws Exception {
        // given
        ResetPasswordRequest request = createResetPasswordRequest("changePassword", input, "password12!@");

        // when, then
        mockMvc.perform(patch("/api/members/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("비밀번호는 필수 입력 값입니다.")));
    }

    @Test
    @DisplayName("비밀번호 초기화 시 비밀번호 null 값이 들어와 예외 발생")
    void resetPassword_invalidNullPassword() throws Exception {
        // given
        ResetPasswordRequest request = createResetPasswordRequest("changePassword", null, "password12!@");

        // when, then
        mockMvc.perform(patch("/api/members/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("비밀번호는 필수 입력 값입니다.")));
    }

    @ParameterizedTest
    @DisplayName("비밀번호 초기화 시 비밀번호 유효성 검사로 예외 발생")
    @ValueSource(strings = {"p", "p1@", "password", "1@", "passworddddddddd", "passworddddddddd!@", "password!@"})
    void resetPassword_invalidPassword(String input) throws Exception {
        // given
        ResetPasswordRequest request = createResetPasswordRequest("changePassword", input, "password12!@");

        // when, then
        mockMvc.perform(patch("/api/members/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("비밀번호는 8자 이상 15자 이하의 영문, 숫자, 특수문자 조합이어야 합니다."));
    }

    @ParameterizedTest
    @DisplayName("비밀번호 초기화 시 비밀번호 재입력 빈 값으로 예외 발생")
    @ValueSource(strings = {"", " "})
    void resetPassword_invalidNotBlankRePassword(String input) throws Exception {
        // given
        ResetPasswordRequest request = createResetPasswordRequest("changePassword", "password12!@", input);

        // when, then
        mockMvc.perform(patch("/api/members/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("비밀번호 재입력은 필수 입력 값입니다.")));
    }

    @Test
    @DisplayName("비밀번호 초기화 시 비밀번호 재입력 null 값이 들어와 예외 발생")
    void resetPassword_invalidNullRePassword() throws Exception {
        // given
        ResetPasswordRequest request = createResetPasswordRequest("changePassword", "password12!@", null);

        // when, then
        mockMvc.perform(patch("/api/members/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("비밀번호 재입력은 필수 입력 값입니다.")));
    }

    @ParameterizedTest
    @DisplayName("비밀번호 초기화 시 비밀번호 재입력 유효성 검사로 예외 발생")
    @ValueSource(strings = {"p", "p1@", "password", "1@", "passworddddddddd", "passworddddddddd!@", "password!@"})
    void resetPassword_invalidRePassword(String input) throws Exception {
        // given
        ResetPasswordRequest request = createResetPasswordRequest("changePassword", "password12!@", input);

        // when, then
        mockMvc.perform(patch("/api/members/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("비밀번호는 8자 이상 15자 이하의 영문, 숫자, 특수문자 조합이어야 합니다."));
    }

    @Test
    @DisplayName("비밀번호 초기화 시 비밀번호와 재입력 비밀번호가 달라 예외 발생")
    void resetPassword_notMathPassword() throws Exception {
        // given
        ResetPasswordRequest request = createResetPasswordRequest("changePassword", "password12!@", "password34!@");
        memberRepository.save(createMember(null, "member@email.com"));

        // when, then
        mockMvc.perform(patch("/api/members/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.INCORRECT_PASSWORD_REPASSWORD.getMessage()));

    }

    @Test
    @DisplayName("비밀번호 초기화 시 저장된 비밀번호 변경 토큰이 존재하지 않아 예외 발생")
    void resetPassword_passwordTokenNotFound() throws Exception {
        // given
        ResetPasswordRequest request = createResetPasswordRequest("changePassword", "password12!@", "password12!@");
        memberRepository.save(createMember(null, "member@email.com"));

        // when, then
        mockMvc.perform(patch("/api/members/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.INVALID_CHANGE_PASSWORD.getMessage()));

    }

    @Test
    @DisplayName("비밀번호 초기화 시 회원 데이터 존재하지 않아 예외 발생")
    void resetPassword_memberNotFound() throws Exception {
        // given
        ResetPasswordRequest request = createResetPasswordRequest("changePassword", "password12!@", "password12!@");

        when(redisService.getData(anyString())).thenReturn("noMember@email.com");

        // when, then
        mockMvc.perform(patch("/api/members/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.MEMBER_NOT_FOUND.getMessage()));

    }

    @Test
    @DisplayName("일반 회원 비밀번호 변경")
    void changePassword_nativeMember() throws Exception {
        String encodePassword = passwordEncoder.encode("test123@");
        ProfileImage profileImage = profileImageRepository.save(createProfileImage(null, "member"));
        Member member = memberRepository.save(createNativeTypeMember(null, "member@email.com", encodePassword, profileImage));

        ChangePasswordRequest request = createChangePasswordRequest("test123@", "test123!", "test123!");

        mockAuthentication(member);

        mockMvc.perform(patch("/api/members/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(SuccessCode.GENERAL_SUCCESS.getMessage()));
    }

    @Test
    @DisplayName("통합 회원 비밀번호 변경")
    void changePassword_bothMember() throws Exception {
        String encodePassword = passwordEncoder.encode("test123@");
        ProfileImage profileImage = profileImageRepository.save(createProfileImage(null, "member"));
        Member member = memberRepository.save(createBothTypeMember(null, "member@email.com", encodePassword, profileImage));

        ChangePasswordRequest request = createChangePasswordRequest("test123@", "test123!", "test123!");

        mockAuthentication(member);

        mockMvc.perform(patch("/api/members/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(SuccessCode.GENERAL_SUCCESS.getMessage()));
    }


    @ParameterizedTest
    @DisplayName("비밀번호 변경 시 현재 비밀번호 빈 값으로 예외 발생")
    @ValueSource(strings = {"", " "})
    void changePassword_invalidNotBlankNowPassword(String input) throws Exception {
        // given
        Member member = memberRepository.save(createMember(null, "member@email.com"));
        ChangePasswordRequest request = createChangePasswordRequest(input, "password12!@", "password12!@");

        mockAuthentication(member);

        // when, then
        mockMvc.perform(patch("/api/members/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("현재 비밀번호는 필수 입력 값입니다.")));
    }

    @Test
    @DisplayName("비밀번호 변경 시 현재 비밀번호 null 값이 들어와 예외 발생")
    void changePassword_invalidNullNowPassword() throws Exception {
        // given
        Member member = memberRepository.save(createMember(null, "member@email.com"));
        ChangePasswordRequest request = createChangePasswordRequest(null, "password12!@", "password12!@");

        mockAuthentication(member);

        // when, then
        mockMvc.perform(patch("/api/members/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("현재 비밀번호는 필수 입력 값입니다.")));

    }

    @ParameterizedTest
    @DisplayName("비밀번호 변경 시 비밀번호 빈 값으로 예외 발생")
    @ValueSource(strings = {"", " "})
    void changePassword_invalidNotBlankPassword(String input) throws Exception {
        // given
        Member member = memberRepository.save(createMember(null, "member@email.com"));
        ChangePasswordRequest request = createChangePasswordRequest("password12!@", input, "password12!@");

        mockAuthentication(member);

        // when, then
        mockMvc.perform(patch("/api/members/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("비밀번호는 필수 입력 값입니다.")));
    }

    @Test
    @DisplayName("비밀번호 변경 시 비밀번호 null 값이 들어와 예외 발생")
    void changePassword_invalidNullPassword() throws Exception {
        // given
        Member member = memberRepository.save(createMember(null, "member@email.com"));
        ChangePasswordRequest request = createChangePasswordRequest("password12!@", null, "password12!@");

        mockAuthentication(member);

        // when, then
        mockMvc.perform(patch("/api/members/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("비밀번호는 필수 입력 값입니다.")));
    }

    @ParameterizedTest
    @DisplayName("비밀번호 변경 시 비밀번호 유효성 검사로 예외 발생")
    @ValueSource(strings = {"p", "p1@", "password", "1@", "passworddddddddd", "passworddddddddd!@", "password!@"})
    void changePassword_invalidPassword(String input) throws Exception {
        // given
        Member member = memberRepository.save(createMember(null, "member@email.com"));
        ChangePasswordRequest request = createChangePasswordRequest("password12!@", input, "password12!@");

        mockAuthentication(member);

        // when, then
        mockMvc.perform(patch("/api/members/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("비밀번호는 8자 이상 15자 이하의 영문, 숫자, 특수문자 조합이어야 합니다."));
    }

    @ParameterizedTest
    @DisplayName("비밀번호 변경 시 비밀번호 재입력 빈 값으로 예외 발생")
    @ValueSource(strings = {"", " "})
    void changePassword_invalidNotBlankRePassword(String input) throws Exception {
        // given
        Member member = memberRepository.save(createMember(null, "member@email.com"));
        ChangePasswordRequest request = createChangePasswordRequest("password12!@", "password12!@", input);

        mockAuthentication(member);

        // when, then
        mockMvc.perform(patch("/api/members/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("비밀번호 재입력은 필수 입력 값입니다.")));
    }

    @Test
    @DisplayName("비밀번호 변경 시 비밀번호 재입력 null 값이 들어와 예외 발생")
    void changePassword_invalidNullRePassword() throws Exception {
        // given
        Member member = memberRepository.save(createMember(null, "member@email.com"));
        ChangePasswordRequest request = createChangePasswordRequest("password12!@", "password12!@", null);

        mockAuthentication(member);

        // when, then
        mockMvc.perform(patch("/api/members/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("비밀번호 재입력은 필수 입력 값입니다.")));
    }

    @ParameterizedTest
    @DisplayName("비밀번호 변경 시 비밀번호 재입력 유효성 검사로 예외 발생")
    @ValueSource(strings = {"p", "p1@", "password", "1@", "passworddddddddd", "passworddddddddd!@", "password!@"})
    void changePassword_invalidRePassword(String input) throws Exception {
        // given
        Member member = memberRepository.save(createMember(null, "member@email.com"));
        ChangePasswordRequest request = createChangePasswordRequest("password12!@", "password12!@", input);

        mockAuthentication(member);

        // when, then
        mockMvc.perform(patch("/api/members/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("비밀번호는 8자 이상 15자 이하의 영문, 숫자, 특수문자 조합이어야 합니다."));
    }


    @Test
    @DisplayName("비밀번호 변경 시 변경 비밀번호와 재입력 비밀번호가 일치하지 않아 예외 발생")
    void changePassword_inCorrectNewPassword() throws Exception {
        // given
        Member member = memberRepository.save(createMember(null, "member@email.com"));
        ChangePasswordRequest request = createChangePasswordRequest("password12!", "password12!@", "test456!");

        mockAuthentication(member);

        // when, then
        mockMvc.perform(patch("/api/members/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.INCORRECT_PASSWORD_REPASSWORD.getMessage()));

    }

    @Test
    @DisplayName("비밀번호 변경 시 현재 비밀번호와 변경 비밀번호가 같아 예외 발생")
    void changePassword_correctNowPassword() throws Exception {
        // given
        Member member = memberRepository.save(createMember(null, "member@email.com"));
        ChangePasswordRequest request = createChangePasswordRequest("password12!@", "password12!@", "password12!@");

        mockAuthentication(member);

        // when, then
        mockMvc.perform(patch("/api/members/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.CORRECT_NOWPASSWORD_NEWPASSWORD.getMessage()));
    }

    @Test
    @DisplayName("비밀번호 변경 시 회원 정보를 찾을 수 없어 예외 발생")
    void changePassword_memberNotFound() throws Exception {
        // given
        ChangePasswordRequest request = createChangePasswordRequest("password12!", "password12!@", "password12!@");

        mockAuthentication(createMember(1000L, "notMember@email.com"));

        // when, then
        mockMvc.perform(patch("/api/members/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.MEMBER_NOT_FOUND.getMessage()));
    }

    @Test
    @DisplayName("비밀번호 변경 시 소셜 회원으로 예외 발생")
    void changePassword_socialMember() throws Exception {
        // given
        ProfileImage profileImage = profileImageRepository.save(
                createProfileImage(null, "member")
        );

        Member member = memberRepository.save(
                createSocialTypeMember(null, "member@email.com", profileImage)
        );

        ChangePasswordRequest request = createChangePasswordRequest("password12!", "password12!@", "password12!@");

        mockAuthentication(member);

        // when, then
        mockMvc.perform(patch("/api/members/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.SOCIAL_MEMBER_PASSWORD_CHANGE_NOT_ALLOWED.getMessage()));
    }

    @Test
    @DisplayName("비밀번호 변경 시 저장된 비밀번호와 현재 비밀번호가 일치하지 않아 예외 발생")
    void changePassword_incorrectSavedPassword() throws Exception {
        // given
        ProfileImage profileImage = profileImageRepository.save(
                createProfileImage(null, "member")
        );

        String encodePassword = passwordEncoder.encode("test123@");

        Member member = memberRepository.save(
                createNativeTypeMember(
                        null,
                        "member@email.com",
                        encodePassword,
                        profileImage
                )
        );

        ChangePasswordRequest request = createChangePasswordRequest("password12!", "password12!@", "password12!@");

        mockAuthentication(member);

        // when, then
        mockMvc.perform(patch("/api/members/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.INCORRECT_PASSWORD.getMessage()));
    }


    // TODO: validation 테스트 추가
    @Test
    @DisplayName("일반 회원 정보 조회")
    void getMemberInfo_nativeMember() throws Exception {
        // given
        ProfileImage profileImage = profileImageRepository.save(
                createProfileImage(null, "profileImage")
        );

        String encodedPassword = passwordEncoder.encode("password12!@");

        Member member = memberRepository.save(
                createNativeTypeMember(
                        null,
                        "nativeMember@email.com",
                        encodedPassword,
                        profileImage
                )
        );

        mockAuthentication(member);

        // when, then
        mockMvc.perform(get("/api/members/info"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.email").value(member.getEmail()))
                .andExpect(jsonPath("$.data.nickname").value(member.getNickname()))
                .andExpect(jsonPath("$.data.profileImage").value(profileImage.getS3ObjectUrl()));
    }

    @Test
    @DisplayName("소셜 회원 정보 조회")
    void getMemberInfo_socialMember() throws Exception {
        // given
        ProfileImage profileImage = profileImageRepository.save(createProfileImage(null, "profileImage"));
        String encodePassword = passwordEncoder.encode("password12!@");
        Member member = memberRepository.save(createNativeTypeMember(null, "socailMember@email.com", encodePassword, profileImage));

        mockAuthentication(member);

        // when, then
        mockMvc.perform(get("/api/members/info"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.email").value(member.getEmail()))
                .andExpect(jsonPath("$.data.nickname").value(member.getNickname()))
                .andExpect(jsonPath("$.data.profileImage").value(profileImage.getS3ObjectUrl()));
    }


    @Test
    @DisplayName("통합 회원 정보 조회")
    void getMemberInfo_bothMember() throws Exception {
        // given
        ProfileImage profileImage = profileImageRepository.save(createProfileImage(null, "profileImage"));
        String encodePassword = passwordEncoder.encode("password12!@");
        Member member = memberRepository.save(createBothTypeMember(null, "member@email.com", encodePassword, profileImage));

        mockAuthentication(member);

        // when, then
        mockMvc.perform(get("/api/members/info"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.email").value(member.getEmail()))
                .andExpect(jsonPath("$.data.nickname").value(member.getNickname()))
                .andExpect(jsonPath("$.data.profileImage").value(profileImage.getS3ObjectUrl()));
    }

    @Test
    @DisplayName("회원 정보 조회 시 회원 데이터 없어 예외 발생")
    void getMemberInfo_memberNotFound() throws Exception {
        Member member = createMember(0L, "notMember@email.com");
        mockAuthentication(member);

        mockMvc.perform(get("/api/members/info"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.MEMBER_NOT_FOUND.getMessage()));
    }


    @Test
    @DisplayName("회원 닉네임 변경")
    void changeNickname() throws Exception {
        Member member = memberRepository.save(createMember(null, "member@email.com"));
        ChangeNicknameRequest request = createChangeNicknameRequest("newNickname");

        mockAuthentication(member);

        mockMvc.perform(patch("/api/members/change-nickname")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(SuccessCode.GENERAL_SUCCESS.getMessage()));

    }

    @Test
    @DisplayName("회원 닉네임 변경 시 입력 조건이 맞지 않아 예외 발생")
    void changeNickname_invalidNickname() throws Exception {
        Member member = memberRepository.save(createMember(null, "member@email.com"));
        ChangeNicknameRequest request = createChangeNicknameRequest("no");

        mockAuthentication(member);

        mockMvc.perform(patch("/api/members/change-nickname")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("닉네임은 4자 이상 15자 이하이며, 한글 또는 영문자가 반드시 포함되어야 합니다."));

    }

    @Test
    @DisplayName("회원 닉네임 변경 시 회원 데이터 없어 예외 발생")
    void changeNickname_memberNotFound() throws Exception {
        ChangeNicknameRequest request = createChangeNicknameRequest("newNickname");

        Member member = createMember(0L, "notMember@email.com");
        mockAuthentication(member);

        mockMvc.perform(patch("/api/members/change-nickname")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.MEMBER_NOT_FOUND.getMessage()));
    }

    @Test
    @DisplayName("회원 닉네임 변경 시 이미 존재하는 닉네임으로 예외 발생")
    void changeNickname_dataExist() throws Exception {
        Member member = memberRepository.save(createMember(null, "member@email.com"));
        ChangeNicknameRequest request = createChangeNicknameRequest(member.getNickname());

        mockAuthentication(member);

        mockMvc.perform(patch("/api/members/change-nickname")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.ALREADY_EXISTED_NICKNAME.getMessage()));
    }

    @Test
    @DisplayName("이메일 변경")
    void changeEmail() throws Exception {
        Member member = memberRepository.save(createMember(null, "member@email.com"));
        when(redisService.getEmailData(any(), anyString())).thenReturn("true");

        mockAuthentication(member);

        mockMvc.perform(patch("/api/members/change-email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonString(createEmailRequest("changeEmail@email.com"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(SuccessCode.GENERAL_SUCCESS.getMessage()));
    }

    @Test
    @DisplayName("이메일 변경 시 이미 존재하는 이메일로 예외 발생")
    void changeEmail_duplicateEmail() throws Exception {
        Member member = memberRepository.save(createMember(null, "member@email.com"));
        mockAuthentication(member);

        mockMvc.perform(patch("/api/members/change-email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonString(createEmailRequest("member@email.com"))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.ALREADY_EXISTED_EMAIL.getMessage()));
        ;
    }

    @Test
    @DisplayName("이메일 변경 시 인증되지 않은 이메일로 예외 발생")
    void changeEmail_notVerifiedEmail() throws Exception {
        Member member = memberRepository.save(createMember(null, "member"));
        mockAuthentication(member);

        when(redisService.getEmailData(any(), anyString())).thenReturn(null);

        mockMvc.perform(patch("/api/members/change-email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonString(createEmailRequest("changeEmail@email.com"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.NOT_VERIFIED_EMAIL.getMessage()));
        ;
    }

    @Test
    @DisplayName("이메일 변경 시 회원 데이터 없어 예외 발생")
    void changeEmail_memberNotFound() throws Exception {
        when(redisService.getEmailData(any(), anyString())).thenReturn("true");

        Member member = createMember(0L, "notMember@email.com");
        mockAuthentication(member);

        mockMvc.perform(patch("/api/members/change-email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonString(createEmailRequest("changeEmail@email.com"))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.MEMBER_NOT_FOUND.getMessage()));
        ;
    }

    @Test
    @DisplayName("회원 북마크 조회 - 최신순")
    void getMemberBookmarks_sortNewest() throws Exception {
        Member member = memberRepository.save(createMember(null, "member@email.com"));
        bookmarkRepository.save(createBookmark(null, member, travelPlace1, LocalDateTime.now()));
        bookmarkRepository.save(createBookmark(null, member, travelPlace2, LocalDateTime.now().minusDays(1)));
        bookmarkRepository.save(createBookmark(null, member, travelPlace3, LocalDateTime.now().minusDays(2)));

        mockAuthentication(member);

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
    @DisplayName("회원 북마크 조회 - 오래된 순")
    void getMemberBookmarks_sortOldest() throws Exception {
        Member member = memberRepository.save(createMember(null, "member@email.com"));
        bookmarkRepository.save(createBookmark(null, member, travelPlace1, LocalDateTime.now()));
        bookmarkRepository.save(createBookmark(null, member, travelPlace2, LocalDateTime.now().minusDays(1)));
        bookmarkRepository.save(createBookmark(null, member, travelPlace3, LocalDateTime.now().minusDays(2)));

        mockAuthentication(member);

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
    @DisplayName("회원 북마크 조회 - 이름순")
    void getMemberBookmarks_sortName() throws Exception {
        Member member = memberRepository.save(createMember(null, "member@email.com"));
        bookmarkRepository.save(createBookmark(null, member, travelPlace1, LocalDateTime.now()));
        bookmarkRepository.save(createBookmark(null, member, travelPlace2, LocalDateTime.now().minusDays(1)));
        bookmarkRepository.save(createBookmark(null, member, travelPlace3, LocalDateTime.now().minusDays(2)));

        mockAuthentication(member);

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
    @DisplayName("회원 북마크 조회 시 데이터 없는 경우")
    void getMemberBookmarks_emptyData() throws Exception {
        Member member = memberRepository.save(createMember(null, "member"));

        mockAuthentication(member);

        mockMvc.perform(get("/api/members/bookmark")
                        .param("page", "1")
                        .param("sort", "newest"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(0))
                .andExpect(jsonPath("$.data.content").isEmpty());
    }

    @Test
    @DisplayName("회원 북마크 조회 시 정렬 잘못된 값이 들어와 예외 발생")
    void getMemberBookmarks_IllegalSortType() throws Exception {
        Member member = memberRepository.save(createMember(null, "member"));
        mockAuthentication(member);

        mockMvc.perform(get("/api/members/bookmark")
                        .param("page", "1")
                        .param("sort", "ne"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.ILLEGAL_BOOKMARK_SORT_TYPE.getMessage()));
    }

    @Test
    @DisplayName("일반 회원 탈퇴 - 작성자, 참석자 존재하는 경우")
    void deactivateMember1() throws Exception {
        String encodePassword = passwordEncoder.encode("test123@");

        ProfileImage profileImage = profileImageRepository.save(createProfileImage(null, "test"));
        Member member = memberRepository.save(createNativeTypeMember(null, "member@email.com", encodePassword, profileImage));

        TravelSchedule schedule1 = travelScheduleRepository.save(createTravelSchedule(null, "테스트1"));
        TravelSchedule schedule2 = travelScheduleRepository.save(createTravelSchedule(null, "테스트2"));

        travelAttendeeRepository.save(createTravelAttendee(null, member, schedule1, AttendeeRole.AUTHOR, AttendeePermission.ALL));
        travelAttendeeRepository.save(createTravelAttendee(null, member, schedule2, AttendeeRole.GUEST, AttendeePermission.READ));

        chatMessageRepository.save(createChatMessage(null, schedule1.getScheduleId(), member, "테스트1"));
        chatMessageRepository.save(createChatMessage(null, schedule1.getScheduleId(), member, "테스트2"));

        bookmarkRepository.save(createBookmark(null, member, travelPlace1, LocalDateTime.now()));
        bookmarkRepository.save(createBookmark(null, member, travelPlace1, LocalDateTime.now()));

        mockAuthentication(member);

        MvcResult result = mockMvc.perform(patch("/api/members/deactivate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonString(createDeactivateRequest("test123@"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(SuccessCode.GENERAL_SUCCESS.getMessage()))
                .andReturn();

        Cookie[] cookies = result.getResponse().getCookies();

        assertThat(cookies).isNotNull();
        assertThat(cookies.length).isEqualTo(3);
    }

    @Test
    @DisplayName("회원 탈퇴 - 작성자만 존재하는 경우")
    void deactivateMember2() throws Exception {
        String encodePassword = passwordEncoder.encode("test123@");

        ProfileImage profileImage = profileImageRepository.save(createProfileImage(null, "test"));
        Member member = memberRepository.save(createNativeTypeMember(null, "member@email.com", encodePassword, profileImage));
        Member otherMember = memberRepository.save(createMember(null, "otherMember"));

        TravelSchedule schedule1 = travelScheduleRepository.save(createTravelSchedule(null, "테스트1"));

        travelAttendeeRepository.save(createTravelAttendee(null, member, schedule1, AttendeeRole.AUTHOR, AttendeePermission.ALL));
        travelAttendeeRepository.save(createTravelAttendee(null, otherMember, schedule1, AttendeeRole.GUEST, AttendeePermission.READ));

        chatMessageRepository.save(createChatMessage(null, schedule1.getScheduleId(), member, "테스트1"));
        chatMessageRepository.save(createChatMessage(null, schedule1.getScheduleId(), member, "테스트2"));

        bookmarkRepository.save(createBookmark(null, member, travelPlace1, LocalDateTime.now()));
        bookmarkRepository.save(createBookmark(null, member, travelPlace1, LocalDateTime.now()));

        mockAuthentication(member);

        MvcResult result = mockMvc.perform(patch("/api/members/deactivate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonString(createDeactivateRequest("test123@"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(SuccessCode.GENERAL_SUCCESS.getMessage()))
                .andReturn();

        Cookie[] cookies = result.getResponse().getCookies();

        assertThat(cookies).isNotNull();
        assertThat(cookies.length).isEqualTo(3);
    }

    @Test
    @DisplayName("회원 탈퇴 - 참석자만 존재하는 경우")
    void deactivateMember3() throws Exception {
        String encodePassword = passwordEncoder.encode("test123@");

        ProfileImage profileImage = profileImageRepository.save(createProfileImage(null, "test"));
        Member member = memberRepository.save(createNativeTypeMember(null, "member@email.com", encodePassword, profileImage));

        TravelSchedule schedule1 = travelScheduleRepository.save(createTravelSchedule(null, "테스트1"));

        travelAttendeeRepository.save(createTravelAttendee(null, member, schedule1, AttendeeRole.GUEST, AttendeePermission.READ));

        chatMessageRepository.save(createChatMessage(null, schedule1.getScheduleId(), member, "테스트1"));
        chatMessageRepository.save(createChatMessage(null, schedule1.getScheduleId(), member, "테스트2"));

        bookmarkRepository.save(createBookmark(null, member, travelPlace1, LocalDateTime.now()));
        bookmarkRepository.save(createBookmark(null, member, travelPlace1, LocalDateTime.now()));

        mockAuthentication(member);

        MvcResult result = mockMvc.perform(patch("/api/members/deactivate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonString(createDeactivateRequest("test123@"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(SuccessCode.GENERAL_SUCCESS.getMessage()))
                .andReturn();

        Cookie[] cookies = result.getResponse().getCookies();

        assertThat(cookies).isNotNull();
        assertThat(cookies.length).isEqualTo(3);
    }

    @Test
    @DisplayName("회원 탈퇴 - 일정 데이터 없는 경우")
    void deactivateMember4() throws Exception {
        String encodePassword = passwordEncoder.encode("test123@");

        ProfileImage profileImage = profileImageRepository.save(createProfileImage(null, "test"));
        Member member = memberRepository.save(createNativeTypeMember(null, "member@email.com", encodePassword, profileImage));

        bookmarkRepository.save(createBookmark(null, member, travelPlace1, LocalDateTime.now()));
        bookmarkRepository.save(createBookmark(null, member, travelPlace1, LocalDateTime.now()));

        mockAuthentication(member);

        mockMvc.perform(patch("/api/members/deactivate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonString(createDeactivateRequest("test123@"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(SuccessCode.GENERAL_SUCCESS.getMessage()));
    }

    @Test
    @DisplayName("회원 탈퇴 시 회원 데이터를 찾을 수 없는 경우")
    void deactivateMember_memberNotFound() throws Exception {
        Member member = createMember(0L, "notMember@email.com");
        mockAuthentication(member);

        mockMvc.perform(patch("/api/members/deactivate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonString(createDeactivateRequest("test123@"))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.MEMBER_NOT_FOUND.getMessage()));
    }

    @Test
    @DisplayName("회원 탈퇴 시 비밀번호가 맞지 않아 예외 발생")
    void deactivateMember_incorrectPassword() throws Exception {
        String encodePassword = passwordEncoder.encode("incorrect12@");

        ProfileImage profileImage = profileImageRepository.save(createProfileImage(null, "test"));
        Member member = memberRepository.save(createNativeTypeMember(null, "member@email.com", encodePassword, profileImage));

        mockAuthentication(member);

        mockMvc.perform(patch("/api/members/deactivate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJsonString(createDeactivateRequest("test123@"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.INCORRECT_PASSWORD.getMessage()));
    }

}
