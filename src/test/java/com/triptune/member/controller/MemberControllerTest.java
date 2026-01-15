package com.triptune.member.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.triptune.bookmark.repository.BookmarkRepository;
import com.triptune.common.entity.*;
import com.triptune.common.repository.*;
import com.triptune.email.dto.request.EmailRequest;
import com.triptune.member.dto.request.*;
import com.triptune.member.enums.JoinType;
import com.triptune.member.enums.SocialType;
import com.triptune.member.repository.SocialMemberRepository;
import com.triptune.schedule.entity.TravelSchedule;
import com.triptune.schedule.enums.AttendeePermission;
import com.triptune.schedule.repository.ChatMessageRepository;
import com.triptune.schedule.repository.TravelAttendeeRepository;
import com.triptune.schedule.repository.TravelScheduleRepository;
import com.triptune.travel.entity.TravelImage;
import com.triptune.travel.entity.TravelPlace;
import com.triptune.travel.enums.ThemeType;
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
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.not;
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
@Transactional
@AutoConfigureMockMvc
@ActiveProfiles("mongo")
public class MemberControllerTest extends MemberTest {
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
    @Autowired private ApiCategoryRepository apiCategoryRepository;
    @Autowired private BookmarkRepository bookmarkRepository;
    @Autowired private TravelAttendeeRepository travelAttendeeRepository;
    @Autowired private TravelScheduleRepository travelScheduleRepository;
    @Autowired private ChatMessageRepository chatMessageRepository;
    @Autowired private SocialMemberRepository socialMemberRepository;
    @Autowired private ApiContentTypeRepository apiContentTypeRepository;

    @MockBean private RedisService redisService;
    @MockBean private EmailService emailService;
    @MockBean private S3Service s3Service;

    private TravelPlace place1;
    private TravelPlace place2;
    private TravelPlace place3;

    private TravelImage place1Thumbnail;
    private TravelImage place2Thumbnail;
    private TravelImage place3Thumbnail;


    @BeforeEach
    void setUp() {
        chatMessageRepository.deleteAll();

        Country country = countryRepository.save(createCountry());
        City city = cityRepository.save(createCity(country, "서울"));
        District district = districtRepository.save(createDistrict(city, "강남"));
        ApiCategory apiCategory = apiCategoryRepository.save(createApiCategory());
        ApiContentType apiContentType = apiContentTypeRepository.save(createApiContentType(ThemeType.ATTRACTIONS));

        place1 = travelPlaceRepository.save(
                createTravelPlace(
                        country,
                        city,
                        district,
                        apiCategory,
                        apiContentType,
                        "가장소"
                )
        );
        place1Thumbnail = travelImageRepository.save(createTravelImage(place1, "test1", true));

        place2 = travelPlaceRepository.save(
                createTravelPlace(
                        country,
                        city,
                        district,
                        apiCategory,
                        apiContentType,
                        "나장소"
                )
        );
        place2Thumbnail = travelImageRepository.save(createTravelImage(place2, "test1", true));

        place3 = travelPlaceRepository.save(
                createTravelPlace(
                        country,
                        city,
                        district,
                        apiCategory,
                        apiContentType,
                        "다장소"
                )
        );
        place3Thumbnail = travelImageRepository.save(createTravelImage(place3, "test1", true));

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
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
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
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
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
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
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
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
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
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
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
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
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
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
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
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
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
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
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
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
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
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
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
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
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
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
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
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
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

        ProfileImage profileImage = profileImageRepository.save(createProfileImage("memberImage"));
        memberRepository.save(createNativeTypeMember(request.getEmail(), profileImage));

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
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.NOT_VERIFIED_EMAIL.getMessage()));
        ;
    }


    @Test
    @DisplayName("일반 회원 로그인")
    void login() throws Exception {
        // given
        ProfileImage profileImage = profileImageRepository.save(createProfileImage("memberImage"));
        String encodePassword = passwordEncoder.encode("password12!@");
        Member member = memberRepository.save(createNativeTypeMember("member@email.com", encodePassword, profileImage));

        LoginRequest request = createLoginRequest("member@email.com", "password12!@");

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
        Cookie[] cookies = result.getResponse().getCookies();

        assertThat(cookies).isNotNull();
        assertThat(cookies.length).isEqualTo(1);
        assertThat(cookies[0].getName()).isEqualTo("refreshToken");
        assertThat(cookies[0].getValue()).isNotNull();
    }

    @Test
    @DisplayName("통합 회원 로그인")
    void login_bothMember() throws Exception {
        // given
        String encodePassword = passwordEncoder.encode("password12!@");
        ProfileImage profileImage = profileImageRepository.save(createProfileImage("memberImage"));
        Member member = memberRepository.save(createBothTypeMember("member@email.com", encodePassword, profileImage));
        socialMemberRepository.save(createSocialMember(member, SocialType.NAVER, "member"));

        LoginRequest request = createLoginRequest("member@email.com", "password12!@");

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
        LoginRequest request = createLoginRequest(input, "password12!@");

        // when, then
        mockMvc.perform(post("/api/members/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("이메일은 필수 입력 값입니다.")));
    }

    @Test
    @DisplayName("로그인 시 이메일 null 값이 들어와 예외 발생")
    void login_invalidNullEmail() throws Exception {
        // given
        LoginRequest request = createLoginRequest(null, "password12!@");

        // when, then
        mockMvc.perform(post("/api/members/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("이메일은 필수 입력 값입니다.")));
    }


    @ParameterizedTest
    @DisplayName("로그인 시 이메일 형식에 맞지 않아 예외 발생")
    @ValueSource(strings = {"test", "test@", "test$email.com"})
    void login_invalidEmail(String input) throws Exception {
        // given
        LoginRequest request = createLoginRequest(input, "password12!@");

        // when, then
        mockMvc.perform(post("/api/members/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("이메일 형식에 맞지 않습니다.")));
    }


    @ParameterizedTest
    @DisplayName("로그인 시 비밀번호 빈 값으로 예외 발생")
    @ValueSource(strings = {"", " "})
    void login_invalidNotBlankPassword(String input) throws Exception {
        // given
        LoginRequest request = createLoginRequest("member@email.com", input);

        // when, then
        mockMvc.perform(post("/api/members/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("비밀번호는 필수 입력 값입니다.")));
    }

    @Test
    @DisplayName("로그인 시 비밀번호 null 값이 들어와 예외 발생")
    void login_invalidNullPassword() throws Exception {
        // given
        LoginRequest request = createLoginRequest("member@email.com", null);

        // when, then
        mockMvc.perform(post("/api/members/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("비밀번호는 필수 입력 값입니다.")));
    }


    @Test
    @DisplayName("로그인 시 이메일 맞지 않아 예외 발생")
    void login_incorrectEmail() throws Exception {
        // given
        LoginRequest request = createLoginRequest("fail@email.com", "password12!@");

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
    @DisplayName("로그인 시 비밀번호 맞지 않아 예외 발생")
    void login_incorrectPassword() throws Exception {
        // given
        ProfileImage profileImage = profileImageRepository.save(createProfileImage("memberImage"));
        memberRepository.save(createNativeTypeMember("member@email.com", profileImage));

        LoginRequest request = createLoginRequest("member@email.com", "fail!@");

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
    @DisplayName("소셜 회원이 자체 로그인 시도해 예외 발생")
    void login_socialMember() throws Exception {
        // given
        ProfileImage profileImage = profileImageRepository.save(createProfileImage("memberImage"));
        Member member = memberRepository.save(createSocialTypeMember("member@email.com", profileImage));
        socialMemberRepository.save(createSocialMember(member, SocialType.NAVER, "member"));

        LoginRequest request = createLoginRequest("member@email.com", "fail!@");

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
        ProfileImage profileImage = profileImageRepository.save(createProfileImage("memberImage"));
        Member member = memberRepository.save(createNativeTypeMember("member@email.com", profileImage));
        String accessToken = jwtUtils.createAccessToken(member.getMemberId());

        LogoutRequest request = createLogoutRequest(member.getNickname());

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
        Cookie[] cookies = result.getResponse().getCookies();

        assertThat(cookies).isNotNull();
        assertThat(cookies.length).isEqualTo(3);
    }


    @ParameterizedTest
    @DisplayName("로그아웃 시 닉네임 빈 값으로 예외 발생")
    @ValueSource(strings = {"", " "})
    void logout_invalidNotBlankNickname(String input) throws Exception {
        // given
        ProfileImage profileImage = profileImageRepository.save(createProfileImage("memberImage"));
        Member member = memberRepository.save(createNativeTypeMember("member@email.com", profileImage));
        String accessToken = jwtUtils.createAccessToken(member.getMemberId());

        LogoutRequest request = createLogoutRequest(input);

        // when, then
        mockMvc.perform(patch("/api/members/logout")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("닉네임은 필수 입력 값입니다.")));

    }

    @Test
    @DisplayName("로그아웃 시 닉네임 null 값이 들어와 예외 발생")
    void logout_invalidNullNickname() throws Exception {
        // given
        ProfileImage profileImage = profileImageRepository.save(createProfileImage("memberImage"));
        Member member = memberRepository.save(createNativeTypeMember("member@email.com", profileImage));
        String accessToken = jwtUtils.createAccessToken(member.getMemberId());

        LogoutRequest request = createLogoutRequest(null);

        // when, then
        mockMvc.perform(patch("/api/members/logout")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("닉네임은 필수 입력 값입니다.")));
    }

    @ParameterizedTest
    @DisplayName("로그아웃 시 닉네임 입력값 검사로 예외 발생")
    @ValueSource(strings = {"n", "닉", "1", "1@", "닉네임임임임임임임임임임임임임임", "@@@@@@@@@@@@@@@", "12345", "행복1"})
    void logout_invalidNickname(String input) throws Exception {
        // given
        ProfileImage profileImage = profileImageRepository.save(createProfileImage("memberImage"));
        Member member = memberRepository.save(createNativeTypeMember("member@email.com", profileImage));
        String accessToken = jwtUtils.createAccessToken(member.getMemberId());

        LogoutRequest request = createLogoutRequest(input);

        // when, then
        mockMvc.perform(patch("/api/members/logout")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("닉네임은 4자 이상 15자 이하이며, 한글 또는 영문자가 반드시 포함되어야 합니다."));
    }


    @Test
    @DisplayName("로그아웃 시 회원 데이터 없어 예외 발생")
    void logout_memberNotFound() throws Exception {
        // given
        ProfileImage profileImage = profileImageRepository.save(createProfileImage("memberImage"));
        Member member = memberRepository.save(createNativeTypeMember("member@email.com", profileImage));
        String accessToken = jwtUtils.createAccessToken(member.getMemberId());

        LogoutRequest request = createLogoutRequest("notMember");

        // when, then
        mockMvc.perform(patch("/api/members/logout")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.MEMBER_NOT_FOUND.getMessage()));
    }

    @Test
    @DisplayName("토큰 갱신")
    void refreshToken() throws Exception {
        // given
        ProfileImage profileImage = profileImageRepository.save(createProfileImage("memberImage"));
        Member member = memberRepository.save(createNativeTypeMember("member@email.com", profileImage));
        String refreshToken = jwtUtils.createRefreshToken(member.getMemberId());
        member.updateRefreshToken(refreshToken);

        Cookie cookie = createRefreshTokenCookie(refreshToken);

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
    @DisplayName("토큰 갱신 시 쿠키 존재하지 않아 예외 발생")
    void refreshToken_noCookie() throws Exception {
        // given, when, then
        mockMvc.perform(post("/api/members/refresh"))
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("/api/members/refresh : " + ErrorCode.MISMATCH_REFRESH_TOKEN.getMessage()));
    }

    @Test
    @DisplayName("토큰 갱신 시 쿠키에 refreshToken 정보 없어서 예외 발생")
    void refreshToken_notRefreshTokenCookie() throws Exception {
        // given
        Cookie cookie = new Cookie("error", "error");

        // when, then
        mockMvc.perform(post("/api/members/refresh")
                        .cookie(cookie))
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("/api/members/refresh : " + ErrorCode.MISMATCH_REFRESH_TOKEN.getMessage()));
    }


    @Test
    @DisplayName("토큰 갱신 시 refresh token 만료로 예외 발생")
    void refreshToken_expired() throws Exception {
        // given
        String refreshToken = jwtUtils.createToken("ExpiredRefreshToken", -604800000);
        Cookie cookie = createRefreshTokenCookie(refreshToken);

        // when, then
        mockMvc.perform(post("/api/members/refresh")
                        .cookie(cookie))
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("/api/members/refresh : " + ErrorCode.EXPIRED_JWT_TOKEN.getMessage()));
    }

    @Test
    @DisplayName("토큰 갱신 시 회원 데이터 존재하지 않아 예외 발생")
    void refreshToken_memberNotFound() throws Exception {
        // given
        String refreshToken = jwtUtils.createRefreshToken(0L);
        Cookie cookie = createRefreshTokenCookie(refreshToken);

        // when, then
        mockMvc.perform(post("/api/members/refresh")
                        .cookie(cookie))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.MEMBER_NOT_FOUND.getMessage()));

    }

    @Test
    @DisplayName("토큰 갱신 시 요청 refreshToken 과 저장된 refresh token 값이 달라 예외 발생")
    void refreshToken_NotEqualsRefreshToken() throws Exception {
        // given
        ProfileImage profileImage = profileImageRepository.save(createProfileImage("memberImage"));
        Member member = memberRepository.save(createNativeTypeMember("member@email.com", profileImage));
        String refreshToken = jwtUtils.createRefreshToken(member.getMemberId());

        Cookie cookie = createRefreshTokenCookie(refreshToken);

        // when, then
        mockMvc.perform(post("/api/members/refresh")
                        .cookie(cookie))
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("/api/members/refresh : " + ErrorCode.MISMATCH_REFRESH_TOKEN.getMessage()));

    }

    @Test
    @DisplayName("일반 회원 비밀번호 찾기")
    void findPassword_nativeMember() throws Exception {
        // given
        ProfileImage profileImage = profileImageRepository.save(createProfileImage("memberImage"));
        String encodedPassword = passwordEncoder.encode("password12!@");
        Member member = memberRepository.save(createNativeTypeMember("member@email.com", encodedPassword, profileImage));

        FindPasswordRequest request = createFindPasswordRequest(member.getEmail());

        // when, then
        mockMvc.perform(post("/api/members/find-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(SuccessCode.GENERAL_SUCCESS.getMessage()));
    }

    @Test
    @DisplayName("소셜 회원 비밀번호 찾기")
    void findPassword_socialMember() throws Exception {
        // given
        ProfileImage profileImage = profileImageRepository.save(createProfileImage("memberImage"));
        Member member = memberRepository.save(createSocialTypeMember("member@email.com", profileImage));
        socialMemberRepository.save(createSocialMember(member, SocialType.NAVER, "member"));

        FindPasswordRequest request = createFindPasswordRequest(member.getEmail());

        // when, then
        mockMvc.perform(post("/api/members/find-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(SuccessCode.GENERAL_SUCCESS.getMessage()));
    }

    @Test
    @DisplayName("통합 회원 비밀번호 찾기")
    void findPassword_bothMember() throws Exception {
        // given
        ProfileImage profileImage = profileImageRepository.save(createProfileImage("memberImage"));
        String encodedPassword = passwordEncoder.encode("password12!@");
        Member member = memberRepository.save(createBothTypeMember("member@email.com", encodedPassword, profileImage));
        socialMemberRepository.save(createSocialMember(member, SocialType.NAVER, "member"));

        FindPasswordRequest request = createFindPasswordRequest(member.getEmail());

        // when, then
        mockMvc.perform(post("/api/members/find-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
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
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
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
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
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
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("이메일 형식에 맞지 않습니다.")));
    }

    @Test
    @DisplayName("비밀번호 찾기 시 회원 데이터 존재하지 않아 예외 발생")
    void findPassword_memberNotFound() throws Exception {
        // given
        FindPasswordRequest request = createFindPasswordRequest("notMember@email.com");

        // when, then
        mockMvc.perform(post("/api/members/find-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.MEMBER_NOT_FOUND.getMessage()));
    }


    @Test
    @DisplayName("일반 회원 비밀번호 초기화")
    void resetPassword_nativeMember() throws Exception {
        // given
        ProfileImage profileImage = profileImageRepository.save(createProfileImage("memberImage"));
        String encodedPassword = passwordEncoder.encode("savedPassword12!@");
        Member member = memberRepository.save(createNativeTypeMember("member@email.com", encodedPassword, profileImage));

        ResetPasswordRequest request = createResetPasswordRequest("changePassword", "password12!@", "password12!@");

        when(redisService.getData(anyString())).thenReturn(member.getEmail());

        // when, then
        mockMvc.perform(patch("/api/members/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(SuccessCode.GENERAL_SUCCESS.getMessage()));

        assertThat(passwordEncoder.matches(request.getPassword(), member.getPassword())).isTrue();
        assertThat(member.getJoinType()).isEqualTo(JoinType.NATIVE);
    }


    @Test
    @DisplayName("소셜 회원 비밀번호 초기화")
    void resetPassword_socialMember() throws Exception {
        // given
        ProfileImage profileImage = profileImageRepository.save(createProfileImage("memberImage"));
        Member member = memberRepository.save(createSocialTypeMember("member@email.com", profileImage));
        socialMemberRepository.save(createSocialMember(member, SocialType.NAVER, "member"));

        ResetPasswordRequest request = createResetPasswordRequest("changePassword", "password12!@", "password12!@");

        when(redisService.getData(anyString())).thenReturn(member.getEmail());

        // when, then
        mockMvc.perform(patch("/api/members/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(SuccessCode.GENERAL_SUCCESS.getMessage()));

        assertThat(passwordEncoder.matches(request.getPassword(), member.getPassword())).isTrue();
        assertThat(member.getJoinType()).isEqualTo(JoinType.BOTH);
    }

    @Test
    @DisplayName("통합 회원 비밀번호 초기화")
    void resetPassword_bothMember() throws Exception {
        // given
        ProfileImage profileImage = profileImageRepository.save(createProfileImage("memberImage"));
        String encodedPassword = passwordEncoder.encode("savedPassword12!@");
        Member member = memberRepository.save(createBothTypeMember("member@email.com", encodedPassword, profileImage));
        socialMemberRepository.save(createSocialMember(member, SocialType.NAVER, "member"));

        ResetPasswordRequest request = createResetPasswordRequest("changePassword", "password12!@", "password12!@");

        when(redisService.getData(anyString())).thenReturn(member.getEmail());

        // when, then
        mockMvc.perform(patch("/api/members/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(SuccessCode.GENERAL_SUCCESS.getMessage()));

        assertThat(passwordEncoder.matches(request.getPassword(), member.getPassword())).isTrue();
        assertThat(member.getJoinType()).isEqualTo(JoinType.BOTH);
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
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
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
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
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
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
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
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
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
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
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
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
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
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
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
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("비밀번호는 8자 이상 15자 이하의 영문, 숫자, 특수문자 조합이어야 합니다."));
    }

    @Test
    @DisplayName("비밀번호 초기화 시 비밀번호와 재입력 비밀번호가 달라 예외 발생")
    void resetPassword_notMathPassword() throws Exception {
        // given
        ProfileImage profileImage = createProfileImage("memberImage");
        memberRepository.save(createNativeTypeMember("member@email.com", profileImage));

        ResetPasswordRequest request = createResetPasswordRequest("changePassword", "password12!@", "password34!@");

        // when, then
        mockMvc.perform(patch("/api/members/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.INCORRECT_PASSWORD_REPASSWORD.getMessage()));

    }

    @Test
    @DisplayName("비밀번호 초기화 시 저장된 비밀번호 변경 토큰이 존재하지 않아 예외 발생")
    void resetPassword_passwordTokenNotFound() throws Exception {
        // given
        ProfileImage profileImage = createProfileImage("memberImage");
        memberRepository.save(createNativeTypeMember("member@email.com", profileImage));

        ResetPasswordRequest request = createResetPasswordRequest("changePassword", "password12!@", "password12!@");

        // when, then
        mockMvc.perform(patch("/api/members/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
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
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.MEMBER_NOT_FOUND.getMessage()));

    }

    @Test
    @DisplayName("일반 회원 비밀번호 변경")
    void changePassword_nativeMember() throws Exception {
        // given
        ProfileImage profileImage = createProfileImage("memberImage");
        String encodePassword = passwordEncoder.encode("test123@");
        Member member = memberRepository.save(createNativeTypeMember("member@email.com", encodePassword, profileImage));

        ChangePasswordRequest request = createChangePasswordRequest("test123@", "test123!", "test123!");

        mockAuthentication(member);

        // when, then
        mockMvc.perform(patch("/api/members/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(SuccessCode.GENERAL_SUCCESS.getMessage()));

    }

    @Test
    @DisplayName("통합 회원 비밀번호 변경")
    void changePassword_bothMember() throws Exception {
        // given
        ProfileImage profileImage = createProfileImage("memberImage");
        String encodePassword = passwordEncoder.encode("test123@");
        Member member = memberRepository.save(createBothTypeMember("member@email.com", encodePassword, profileImage));

        ChangePasswordRequest request = createChangePasswordRequest("test123@", "test123!", "test123!");

        mockAuthentication(member);

        // when, then
        mockMvc.perform(patch("/api/members/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(SuccessCode.GENERAL_SUCCESS.getMessage()));

    }


    @ParameterizedTest
    @DisplayName("비밀번호 변경 시 현재 비밀번호 빈 값으로 예외 발생")
    @ValueSource(strings = {"", " "})
    void changePassword_invalidNotBlankNowPassword(String input) throws Exception {
        // given
        ProfileImage profileImage = createProfileImage("memberImage");
        Member member = memberRepository.save(createNativeTypeMember("member@email.com", profileImage));

        ChangePasswordRequest request = createChangePasswordRequest(input, "password12!@", "password12!@");

        mockAuthentication(member);

        // when, then
        mockMvc.perform(patch("/api/members/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("현재 비밀번호는 필수 입력 값입니다.")));
    }

    @Test
    @DisplayName("비밀번호 변경 시 현재 비밀번호 null 값이 들어와 예외 발생")
    void changePassword_invalidNullNowPassword() throws Exception {
        // given
        ProfileImage profileImage = createProfileImage("memberImage");
        Member member = memberRepository.save(createNativeTypeMember("member@email.com", profileImage));

        ChangePasswordRequest request = createChangePasswordRequest(null, "password12!@", "password12!@");

        mockAuthentication(member);

        // when, then
        mockMvc.perform(patch("/api/members/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("현재 비밀번호는 필수 입력 값입니다.")));

    }

    @ParameterizedTest
    @DisplayName("비밀번호 변경 시 비밀번호 빈 값으로 예외 발생")
    @ValueSource(strings = {"", " "})
    void changePassword_invalidNotBlankPassword(String input) throws Exception {
        // given
        ProfileImage profileImage = createProfileImage("memberImage");
        Member member = memberRepository.save(createNativeTypeMember("member@email.com", profileImage));

        ChangePasswordRequest request = createChangePasswordRequest("password12!@", input, "password12!@");

        mockAuthentication(member);

        // when, then
        mockMvc.perform(patch("/api/members/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("비밀번호는 필수 입력 값입니다.")));
    }

    @Test
    @DisplayName("비밀번호 변경 시 비밀번호 null 값이 들어와 예외 발생")
    void changePassword_invalidNullPassword() throws Exception {
        // given
        ProfileImage profileImage = createProfileImage("memberImage");
        Member member = memberRepository.save(createNativeTypeMember("member@email.com", profileImage));

        ChangePasswordRequest request = createChangePasswordRequest("password12!@", null, "password12!@");

        mockAuthentication(member);

        // when, then
        mockMvc.perform(patch("/api/members/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("비밀번호는 필수 입력 값입니다.")));
    }

    @ParameterizedTest
    @DisplayName("비밀번호 변경 시 비밀번호 유효성 검사로 예외 발생")
    @ValueSource(strings = {"p", "p1@", "password", "1@", "passworddddddddd", "passworddddddddd!@", "password!@"})
    void changePassword_invalidPassword(String input) throws Exception {
        // given
        ProfileImage profileImage = createProfileImage("memberImage");
        Member member = memberRepository.save(createNativeTypeMember("member@email.com", profileImage));

        ChangePasswordRequest request = createChangePasswordRequest("password12!@", input, "password12!@");

        mockAuthentication(member);

        // when, then
        mockMvc.perform(patch("/api/members/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("비밀번호는 8자 이상 15자 이하의 영문, 숫자, 특수문자 조합이어야 합니다."));
    }

    @ParameterizedTest
    @DisplayName("비밀번호 변경 시 비밀번호 재입력 빈 값으로 예외 발생")
    @ValueSource(strings = {"", " "})
    void changePassword_invalidNotBlankRePassword(String input) throws Exception {
        // given
        ProfileImage profileImage = createProfileImage("memberImage");
        Member member = memberRepository.save(createNativeTypeMember("member@email.com", profileImage));

        ChangePasswordRequest request = createChangePasswordRequest("password12!@", "password12!@", input);

        mockAuthentication(member);

        // when, then
        mockMvc.perform(patch("/api/members/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("비밀번호 재입력은 필수 입력 값입니다.")));
    }

    @Test
    @DisplayName("비밀번호 변경 시 비밀번호 재입력 null 값이 들어와 예외 발생")
    void changePassword_invalidNullRePassword() throws Exception {
        // given
        ProfileImage profileImage = createProfileImage("memberImage");
        Member member = memberRepository.save(createNativeTypeMember("member@email.com", profileImage));

        ChangePasswordRequest request = createChangePasswordRequest("password12!@", "password12!@", null);

        mockAuthentication(member);

        // when, then
        mockMvc.perform(patch("/api/members/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("비밀번호 재입력은 필수 입력 값입니다.")));
    }

    @ParameterizedTest
    @DisplayName("비밀번호 변경 시 비밀번호 재입력 유효성 검사로 예외 발생")
    @ValueSource(strings = {"p", "p1@", "password", "1@", "passworddddddddd", "passworddddddddd!@", "password!@"})
    void changePassword_invalidRePassword(String input) throws Exception {
        // given
        ProfileImage profileImage = createProfileImage("memberImage");
        Member member = memberRepository.save(createNativeTypeMember("member@email.com", profileImage));

        ChangePasswordRequest request = createChangePasswordRequest("password12!@", "password12!@", input);

        mockAuthentication(member);

        // when, then
        mockMvc.perform(patch("/api/members/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("비밀번호는 8자 이상 15자 이하의 영문, 숫자, 특수문자 조합이어야 합니다."));
    }


    @Test
    @DisplayName("비밀번호 변경 시 변경 비밀번호와 재입력 비밀번호가 일치하지 않아 예외 발생")
    void changePassword_inCorrectNewPassword() throws Exception {
        // given
        ProfileImage profileImage = createProfileImage("memberImage");
        Member member = memberRepository.save(createNativeTypeMember("member@email.com", profileImage));

        ChangePasswordRequest request = createChangePasswordRequest(
                "password12!", "password12!@", "test456!"
        );

        mockAuthentication(member);

        // when, then
        mockMvc.perform(patch("/api/members/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.INCORRECT_PASSWORD_REPASSWORD.getMessage()));

    }

    @Test
    @DisplayName("비밀번호 변경 시 현재 비밀번호와 변경 비밀번호가 같아 예외 발생")
    void changePassword_correctNowPassword() throws Exception {
        // given
        ProfileImage profileImage = createProfileImage("memberImage");
        Member member = memberRepository.save(createNativeTypeMember("member@email.com", profileImage));

        ChangePasswordRequest request = createChangePasswordRequest(
                "password12!@", "password12!@", "password12!@"
        );

        mockAuthentication(member);

        // when, then
        mockMvc.perform(patch("/api/members/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.CORRECT_NOWPASSWORD_NEWPASSWORD.getMessage()));
    }

    @Test
    @DisplayName("비밀번호 변경 시 회원 정보를 찾을 수 없어 예외 발생")
    void changePassword_memberNotFound() throws Exception {
        // given
        ProfileImage profileImage = createProfileImage("memberImage");
        Member notMember = createNativeTypeMemberWithId(1000L, "member@email.com", profileImage);

        mockAuthentication(notMember);

        ChangePasswordRequest request = createChangePasswordRequest(
                "password12!", "password12!@", "password12!@"
        );

        // when, then
        mockMvc.perform(patch("/api/members/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.MEMBER_NOT_FOUND.getMessage()));
    }

    @Test
    @DisplayName("비밀번호 변경 시 소셜 회원으로 예외 발생")
    void changePassword_socialMember() throws Exception {
        // given
        ProfileImage profileImage = profileImageRepository.save(createProfileImage("memberImage"));
        Member member = memberRepository.save(createSocialTypeMember("member@email.com", profileImage));

        ChangePasswordRequest request = createChangePasswordRequest(
                "password12!", "password12!@", "password12!@"
        );

        mockAuthentication(member);

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
    @DisplayName("비밀번호 변경 시 저장된 비밀번호와 현재 비밀번호가 일치하지 않아 예외 발생")
    void changePassword_incorrectSavedPassword() throws Exception {
        // given
        ProfileImage profileImage = profileImageRepository.save(createProfileImage("memberImage"));
        String encodedPassword = passwordEncoder.encode("password12!@");
        Member member = memberRepository.save(createNativeTypeMember("member@email.com", encodedPassword, profileImage));

        ChangePasswordRequest request = createChangePasswordRequest(
                "password12!", "password12!@", "password12!@"
        );

        mockAuthentication(member);

        // when, then
        mockMvc.perform(patch("/api/members/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.INCORRECT_PASSWORD.getMessage()));
    }


    @Test
    @DisplayName("일반 회원 정보 조회")
    void getMemberInfo_nativeMember() throws Exception {
        // given
        ProfileImage profileImage = profileImageRepository.save(createProfileImage("memberImage"));
        String encodedPassword = passwordEncoder.encode("password12!@");
        Member member = memberRepository.save(createNativeTypeMember("member@email.com", encodedPassword, profileImage));

        mockAuthentication(member);

        // when, then
        mockMvc.perform(get("/api/members/info"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.email").value(member.getEmail()))
                .andExpect(jsonPath("$.data.nickname").value(member.getNickname()))
                .andExpect(jsonPath("$.data.profileImage").value(profileImage.getS3ObjectUrl()));
    }

    @Test
    @DisplayName("소셜 회원 정보 조회")
    void getMemberInfo_socialMember() throws Exception {
        // given
        ProfileImage profileImage = profileImageRepository.save(createProfileImage("memberImage"));
        Member member = memberRepository.save(createSocialTypeMember("member@email.com", profileImage));

        mockAuthentication(member);

        // when, then
        mockMvc.perform(get("/api/members/info"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.email").value(member.getEmail()))
                .andExpect(jsonPath("$.data.nickname").value(member.getNickname()))
                .andExpect(jsonPath("$.data.profileImage").value(profileImage.getS3ObjectUrl()));
    }


    @Test
    @DisplayName("통합 회원 정보 조회")
    void getMemberInfo_bothMember() throws Exception {
        // given
        ProfileImage profileImage = profileImageRepository.save(createProfileImage("memberImage"));
        String encodedPassword = passwordEncoder.encode("password12!@");
        Member member = memberRepository.save(createBothTypeMember("member@email.com", encodedPassword, profileImage));

        mockAuthentication(member);

        // when, then
        mockMvc.perform(get("/api/members/info"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.email").value(member.getEmail()))
                .andExpect(jsonPath("$.data.nickname").value(member.getNickname()))
                .andExpect(jsonPath("$.data.profileImage").value(profileImage.getS3ObjectUrl()));
    }

    @Test
    @DisplayName("회원 정보 조회 시 회원 데이터 없어 예외 발생")
    void getMemberInfo_memberNotFound() throws Exception {
        // given
        ProfileImage profileImage = createProfileImage("memberImage");
        Member notMember = createNativeTypeMemberWithId(1000L, "member@email.com", profileImage);

        mockAuthentication(notMember);

        // when, then
        mockMvc.perform(get("/api/members/info"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.MEMBER_NOT_FOUND.getMessage()));
    }


    @Test
    @DisplayName("회원 닉네임 변경")
    void changeNickname() throws Exception {
        // given
        ProfileImage profileImage = profileImageRepository.save(createProfileImage("memberImage"));
        Member member = memberRepository.save(createNativeTypeMember("member@email.com", profileImage));

        ChangeNicknameRequest request = createChangeNicknameRequest("newNickname");

        mockAuthentication(member);

        // when, then
        mockMvc.perform(patch("/api/members/change-nickname")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(SuccessCode.GENERAL_SUCCESS.getMessage()));

    }


    @ParameterizedTest
    @DisplayName("닉네임 변경 시 닉네임 빈 값으로 예외 발생")
    @ValueSource(strings = {"", " "})
    void changeNickname_invalidNotBlankNickname(String input) throws Exception {
        // given
        ProfileImage profileImage = profileImageRepository.save(createProfileImage("memberImage"));
        Member member = memberRepository.save(createNativeTypeMember("member@email.com", profileImage));

        ChangeNicknameRequest request = createChangeNicknameRequest(input);

        mockAuthentication(member);

        // when, then
        mockMvc.perform(patch("/api/members/change-nickname")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("닉네임은 필수 입력 값입니다.")));
    }

    @Test
    @DisplayName("닉네임 변경 시 닉네임 null 값이 들어와 예외 발생")
    void changeNickname_invalidNullNickname() throws Exception {
        // given
        ProfileImage profileImage = profileImageRepository.save(createProfileImage("memberImage"));
        Member member = memberRepository.save(createNativeTypeMember("member@email.com", profileImage));

        ChangeNicknameRequest request = createChangeNicknameRequest(null);

        mockAuthentication(member);

        // when, then
        mockMvc.perform(patch("/api/members/change-nickname")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("닉네임은 필수 입력 값입니다.")));
    }

    @ParameterizedTest
    @DisplayName("닉네임 변경 시 닉네임 입력값 검사로 예외 발생")
    @ValueSource(strings = {"n", "닉", "1", "1@", "닉네임임임임임임임임임임임임임임", "@@@@@@@@@@@@@@@"})
    void changeNickname_invalidNickname(String input) throws Exception {
        // given
        ProfileImage profileImage = profileImageRepository.save(createProfileImage("memberImage"));
        Member member = memberRepository.save(createNativeTypeMember("member@email.com", profileImage));

        ChangeNicknameRequest request = createChangeNicknameRequest(input);

        mockAuthentication(member);

        // when, then
        mockMvc.perform(patch("/api/members/change-nickname")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("닉네임은 4자 이상 15자 이하이며, 한글 또는 영문자가 반드시 포함되어야 합니다."));
    }

    @Test
    @DisplayName("닉네임 변경 시 회원 데이터 없어 예외 발생")
    void changeNickname_memberNotFound() throws Exception {
        // given
        ProfileImage profileImage = createProfileImage("memberImage");
        Member notMember = createNativeTypeMemberWithId(1000L, "member@email.com", profileImage);

        mockAuthentication(notMember);

        ChangeNicknameRequest request = createChangeNicknameRequest("newNickname");
        // when, then
        mockMvc.perform(patch("/api/members/change-nickname")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.MEMBER_NOT_FOUND.getMessage()));
    }

    @Test
    @DisplayName("회원 닉네임 변경 시 이미 존재하는 닉네임으로 예외 발생")
    void changeNickname_dataExist() throws Exception {
        // given
        ProfileImage profileImage = profileImageRepository.save(createProfileImage("memberImage"));
        Member member = memberRepository.save(createNativeTypeMember("member@email.com", profileImage));

        ChangeNicknameRequest request = createChangeNicknameRequest(member.getNickname());

        mockAuthentication(member);

        // when, then
        mockMvc.perform(patch("/api/members/change-nickname")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.ALREADY_EXISTED_NICKNAME.getMessage()));
    }


    @Test
    @DisplayName("이메일 변경")
    void changeEmail() throws Exception {
        // given
        ProfileImage profileImage = profileImageRepository.save(createProfileImage("memberImage"));
        Member member = memberRepository.save(createNativeTypeMember("member@email.com", profileImage));

        EmailRequest request = createEmailRequest("changeEmail@email.com");

        mockAuthentication(member);
        when(redisService.getEmailData(any(), anyString())).thenReturn("true");

        // when, then
        mockMvc.perform(patch("/api/members/change-email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(SuccessCode.GENERAL_SUCCESS.getMessage()));
    }


    @ParameterizedTest
    @DisplayName("이메일 변경 시 이메일 빈 값으로 예외 발생")
    @ValueSource(strings = {"", " "})
    void changeEmail_invalidNotBlankEmail(String input) throws Exception {
        // given
        ProfileImage profileImage = profileImageRepository.save(createProfileImage("memberImage"));
        Member member = memberRepository.save(createNativeTypeMember("member@email.com", profileImage));

        EmailRequest request = createEmailRequest(input);

        mockAuthentication(member);
        when(redisService.getEmailData(any(), anyString())).thenReturn("true");

        // when, then
        mockMvc.perform(patch("/api/members/change-email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("이메일은 필수 입력 값입니다.")));
    }

    @Test
    @DisplayName("이메일 변경 시 이메일 null 값이 들어와 예외 발생")
    void changeEmail_invalidNullEmail() throws Exception {
        // given
        ProfileImage profileImage = profileImageRepository.save(createProfileImage("memberImage"));
        Member member = memberRepository.save(createNativeTypeMember("member@email.com", profileImage));

        EmailRequest request = createEmailRequest(null);

        mockAuthentication(member);
        when(redisService.getEmailData(any(), anyString())).thenReturn("true");

        // when, then
        mockMvc.perform(patch("/api/members/change-email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("이메일은 필수 입력 값입니다.")));

    }

    @ParameterizedTest
    @DisplayName("이메일 변경 시 이메일 형식에 맞지 않아 예외 발생")
    @ValueSource(strings = {"test", "test@", "test$email.com"})
    void changeEmail_invalidEmail(String input) throws Exception {
        // given
        ProfileImage profileImage = profileImageRepository.save(createProfileImage("memberImage"));
        Member member = memberRepository.save(createNativeTypeMember("member@email.com", profileImage));

        EmailRequest request = createEmailRequest(input);

        mockAuthentication(member);
        when(redisService.getEmailData(any(), anyString())).thenReturn("true");


        // when, then
        mockMvc.perform(patch("/api/members/change-email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("이메일 형식에 맞지 않습니다.")));
    }

    @Test
    @DisplayName("이메일 변경 시 이미 존재하는 이메일로 예외 발생")
    void changeEmail_duplicateEmail() throws Exception {
        // given
        ProfileImage profileImage = profileImageRepository.save(createProfileImage("memberImage"));
        Member member = memberRepository.save(createNativeTypeMember("member@email.com", profileImage));

        EmailRequest request = createEmailRequest("member@email.com");

        mockAuthentication(member);

        // when, then
        mockMvc.perform(patch("/api/members/change-email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.ALREADY_EXISTED_EMAIL.getMessage()));
        ;
    }

    @Test
    @DisplayName("이메일 변경 시 인증되지 않은 이메일로 예외 발생")
    void changeEmail_notVerifiedEmail() throws Exception {
        // given
        ProfileImage profileImage = profileImageRepository.save(createProfileImage("memberImage"));
        Member member = memberRepository.save(createNativeTypeMember("member@email.com", profileImage));

        EmailRequest request = createEmailRequest("changeEmail@email.com");

        mockAuthentication(member);
        when(redisService.getEmailData(any(), anyString())).thenReturn(null);

        // when, then
        mockMvc.perform(patch("/api/members/change-email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.NOT_VERIFIED_EMAIL.getMessage()));
        ;
    }

    @Test
    @DisplayName("이메일 변경 시 회원 데이터 없어 예외 발생")
    void changeEmail_memberNotFound() throws Exception {
        // given
        ProfileImage profileImage = createProfileImage("memberImage");
        Member notMember = createNativeTypeMemberWithId(1000L, "member@email.com", profileImage);

        mockAuthentication(notMember);

        EmailRequest request = createEmailRequest("changeEmail@email.com");
        when(redisService.getEmailData(any(), anyString())).thenReturn("true");

        // when, then
        mockMvc.perform(patch("/api/members/change-email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.MEMBER_NOT_FOUND.getMessage()));
        ;
    }

    @Test
    @DisplayName("회원 북마크 조회 - 최신순")
    void getMemberBookmarks_sortNewest() throws Exception {
        // given
        ProfileImage profileImage = profileImageRepository.save(createProfileImage("memberImage"));
        Member member = memberRepository.save(createNativeTypeMember("member@email.com", profileImage));

        bookmarkRepository.save(createBookmark(member, place1));
        Thread.sleep(10);
        bookmarkRepository.save(createBookmark(member, place2));
        Thread.sleep(10);
        bookmarkRepository.save(createBookmark(member, place3));

        mockAuthentication(member);

        // when, then
        mockMvc.perform(get("/api/members/bookmark")
                        .param("page", "1")
                        .param("sort", "newest"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalElements").value(3))
                .andExpect(jsonPath("$.data.content[0].placeName").value(place3.getPlaceName()))
                .andExpect(jsonPath("$.data.content[0].thumbnailUrl").value(place3Thumbnail.getS3ObjectUrl()))
                .andExpect(jsonPath("$.data.content[1].placeName").value(place2.getPlaceName()))
                .andExpect(jsonPath("$.data.content[1].thumbnailUrl").value(place2Thumbnail.getS3ObjectUrl()))
                .andExpect(jsonPath("$.data.content[2].placeName").value(place1.getPlaceName()))
                .andExpect(jsonPath("$.data.content[2].thumbnailUrl").value(place1Thumbnail.getS3ObjectUrl()));
    }

    @Test
    @DisplayName("회원 북마크 조회 - 오래된순")
    void getMemberBookmarks_sortOldest() throws Exception {
        // given
        ProfileImage profileImage = profileImageRepository.save(createProfileImage("memberImage"));
        Member member = memberRepository.save(createNativeTypeMember("member@email.com", profileImage));

        bookmarkRepository.save(createBookmark(member, place1));
        Thread.sleep(10);
        bookmarkRepository.save(createBookmark(member, place2));
        Thread.sleep(10);
        bookmarkRepository.save(createBookmark(member, place3));

        mockAuthentication(member);

        // when, then
        mockMvc.perform(get("/api/members/bookmark")
                        .param("page", "1")
                        .param("sort", "oldest"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalElements").value(3))
                .andExpect(jsonPath("$.data.content[0].placeName").value(place1.getPlaceName()))
                .andExpect(jsonPath("$.data.content[0].thumbnailUrl").value(place1Thumbnail.getS3ObjectUrl()))
                .andExpect(jsonPath("$.data.content[1].placeName").value(place2.getPlaceName()))
                .andExpect(jsonPath("$.data.content[1].thumbnailUrl").value(place2Thumbnail.getS3ObjectUrl()))
                .andExpect(jsonPath("$.data.content[2].placeName").value(place3.getPlaceName()))
                .andExpect(jsonPath("$.data.content[2].thumbnailUrl").value(place3Thumbnail.getS3ObjectUrl()));
    }

    @Test
    @DisplayName("회원 북마크 조회 - 이름순")
    void getMemberBookmarks_sortName() throws Exception {
        // given
        ProfileImage profileImage = profileImageRepository.save(createProfileImage("memberImage"));
        Member member = memberRepository.save(createNativeTypeMember("member@email.com", profileImage));

        bookmarkRepository.saveAll(List.of(
                createBookmark(member, place1),
                createBookmark(member, place2),
                createBookmark(member, place3)
        ));

        mockAuthentication(member);

        // when, then
        mockMvc.perform(get("/api/members/bookmark")
                        .param("page", "1")
                        .param("sort", "name"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalElements").value(3))
                .andExpect(jsonPath("$.data.content[0].placeName").value(place1.getPlaceName()))
                .andExpect(jsonPath("$.data.content[0].thumbnailUrl").value(place1Thumbnail.getS3ObjectUrl()))
                .andExpect(jsonPath("$.data.content[1].placeName").value(place2.getPlaceName()))
                .andExpect(jsonPath("$.data.content[1].thumbnailUrl").value(place2Thumbnail.getS3ObjectUrl()))
                .andExpect(jsonPath("$.data.content[2].placeName").value(place3.getPlaceName()))
                .andExpect(jsonPath("$.data.content[2].thumbnailUrl").value(place3Thumbnail.getS3ObjectUrl()));
    }

    @Test
    @DisplayName("회원 북마크 조회 시 데이터 없는 경우")
    void getMemberBookmarks_emptyData() throws Exception {
        // given
        ProfileImage profileImage = profileImageRepository.save(createProfileImage("memberImage"));
        Member member = memberRepository.save(createNativeTypeMember("member@email.com", profileImage));

        mockAuthentication(member);

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

    @ParameterizedTest
    @DisplayName("회원 북마크 조회 시 정렬 파라미터 잘못된 값이 들어와 예외 발생")
    @ValueSource(strings = {"ne", "@n", " ", ""})
    void getMemberBookmarks_IllegalSortType(String input) throws Exception {
        // given
        ProfileImage profileImage = profileImageRepository.save(createProfileImage("memberImage"));
        Member member = memberRepository.save(createNativeTypeMember("member@email.com", profileImage));

        mockAuthentication(member);

        // when, then
        mockMvc.perform(get("/api/members/bookmark")
                        .param("page", "1")
                        .param("sort", input))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.ILLEGAL_BOOKMARK_SORT_TYPE.getMessage()));
    }

    @Test
    @DisplayName("일반 회원 탈퇴 - 작성자, 참석자 존재하는 경우")
    void deactivateMember_nativeMember1() throws Exception {
        // given
        String password = "password12!@";
        ProfileImage profileImage = profileImageRepository.save(createProfileImage("memberImage"));
        String encodedPassword = passwordEncoder.encode(password);
        Member member = memberRepository.save(createNativeTypeMember("member@email.com", encodedPassword, profileImage));

        TravelSchedule schedule1 = travelScheduleRepository.save(createTravelSchedule("테스트1"));
        TravelSchedule schedule2 = travelScheduleRepository.save(createTravelSchedule("테스트2"));

        travelAttendeeRepository.saveAll(List.of(
                createAuthorTravelAttendee(schedule1, member),
                createGuestTravelAttendee(schedule2, member, AttendeePermission.READ)
        ));

        chatMessageRepository.saveAll(List.of(
                createChatMessage(schedule1.getScheduleId(), member.getMemberId(), "테스트1"),
                createChatMessage(schedule1.getScheduleId(), member.getMemberId(), "테스트2")
        ));

        bookmarkRepository.saveAll(List.of(
                createBookmark(member, place1),
                createBookmark(member, place1)
        ));

        mockAuthentication(member);

        DeactivateRequest request = createDeactivateRequest(password);

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
        Cookie[] cookies = result.getResponse().getCookies();

        assertThat(cookies).isNotNull();
        assertThat(cookies.length).isEqualTo(3);
    }

    @Test
    @DisplayName("일반 회원 탈퇴 - 작성자만 존재하는 경우")
    void deactivateMember_nativeMember2() throws Exception {
        // given
        String password = "password12!@";
        ProfileImage profileImage = profileImageRepository.save(createProfileImage("memberImage"));
        String encodedPassword = passwordEncoder.encode(password);
        Member member = memberRepository.save(createNativeTypeMember("member@email.com", encodedPassword, profileImage));

        TravelSchedule schedule = travelScheduleRepository.save(createTravelSchedule("테스트1"));

        travelAttendeeRepository.save(createAuthorTravelAttendee(schedule, member));

        chatMessageRepository.saveAll(List.of(
                createChatMessage(schedule.getScheduleId(), member.getMemberId(), "테스트1"),
                createChatMessage(schedule.getScheduleId(), member.getMemberId(), "테스트2")
        ));

        bookmarkRepository.saveAll(List.of(
                createBookmark(member, place1),
                createBookmark(member, place1)
        ));

        mockAuthentication(member);

        DeactivateRequest request = createDeactivateRequest(password);

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
        Cookie[] cookies = result.getResponse().getCookies();

        assertThat(cookies).isNotNull();
        assertThat(cookies.length).isEqualTo(3);
    }

    @Test
    @DisplayName("일반 회원 탈퇴 - 참석자만 존재하는 경우")
    void deactivateMember_nativeMember3() throws Exception {
        // given
        String password = "password12!@";
        ProfileImage profileImage = profileImageRepository.save(createProfileImage("memberImage"));
        String encodedPassword = passwordEncoder.encode(password);
        Member member = memberRepository.save(createNativeTypeMember("member@email.com", encodedPassword, profileImage));

        TravelSchedule schedule = travelScheduleRepository.save(createTravelSchedule("테스트1"));

        travelAttendeeRepository.save(createGuestTravelAttendee(schedule, member, AttendeePermission.READ));

        chatMessageRepository.saveAll(List.of(
                createChatMessage(schedule.getScheduleId(), member.getMemberId(), "테스트1"),
                createChatMessage(schedule.getScheduleId(), member.getMemberId(), "테스트2")
        ));

        bookmarkRepository.saveAll(List.of(
                createBookmark(member, place1),
                createBookmark(member, place1)
        ));

        mockAuthentication(member);

        DeactivateRequest request = createDeactivateRequest(password);

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
        Cookie[] cookies = result.getResponse().getCookies();

        assertThat(cookies).isNotNull();
        assertThat(cookies.length).isEqualTo(3);
    }


    @Test
    @DisplayName("통합 회원 탈퇴")
    void deactivateMember_bothMember() throws Exception {
        // given
        String password = "password12!@";

        ProfileImage profileImage = profileImageRepository.save(createProfileImage("memberImage"));
        String encodedPassword = passwordEncoder.encode(password);
        Member member = memberRepository.save(createBothTypeMember("member@email.com", encodedPassword, profileImage));

        socialMemberRepository.saveAll(List.of(
                createSocialMember(member, SocialType.KAKAO, "kakao"),
                createSocialMember(member, SocialType.NAVER, "naver")
        ));

        TravelSchedule schedule1 = travelScheduleRepository.save(createTravelSchedule("테스트1"));
        TravelSchedule schedule2 = travelScheduleRepository.save(createTravelSchedule("테스트2"));

        travelAttendeeRepository.saveAll(List.of(
                createAuthorTravelAttendee(schedule1, member),
                createGuestTravelAttendee(schedule2, member, AttendeePermission.READ)
        ));

        chatMessageRepository.saveAll(List.of(
                createChatMessage(schedule1.getScheduleId(), member.getMemberId(), "테스트1"),
                createChatMessage(schedule1.getScheduleId(), member.getMemberId(), "테스트2")
        ));

        bookmarkRepository.saveAll(List.of(
                createBookmark(member, place1),
                createBookmark(member, place1)
        ));

        mockAuthentication(member);

        DeactivateRequest request = createDeactivateRequest(password);

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
        Cookie[] cookies = result.getResponse().getCookies();

        assertThat(cookies).isNotNull();
        assertThat(cookies.length).isEqualTo(3);
    }


    @ParameterizedTest
    @DisplayName("회원 탈퇴 시 비밀번호 빈 값으로 예외 발생")
    @ValueSource(strings = {"", " "})
    void deactivateMember_invalidNotBlankPassword(String input) throws Exception {
        // given
        ProfileImage profileImage = profileImageRepository.save(createProfileImage("memberImage"));
        Member member = memberRepository.save(createNativeTypeMember("member@email.com", profileImage));

        DeactivateRequest request = createDeactivateRequest(input);

        mockAuthentication(member);

        // when, then
        mockMvc.perform(patch("/api/members/deactivate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("비밀번호는 필수 입력 값입니다.")));
    }

    @Test
    @DisplayName("회원 탈퇴 시 비밀번호 null 값이 들어와 예외 발생")
    void deactivateMember_invalidNullPassword() throws Exception {
        // given
        ProfileImage profileImage = profileImageRepository.save(createProfileImage("memberImage"));
        Member member = memberRepository.save(createNativeTypeMember("member@email.com", profileImage));

        DeactivateRequest request = createDeactivateRequest(null);

        mockAuthentication(member);

        // when, then
        mockMvc.perform(patch("/api/members/deactivate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("비밀번호는 필수 입력 값입니다.")));
    }

    @ParameterizedTest
    @DisplayName("회원 탈퇴 시 비밀번호 유효성 검사로 예외 발생")
    @ValueSource(strings = {"p", "p1@", "password", "1@", "passworddddddddd", "passworddddddddd!@", "password!@"})
    void deactivateMember_invalidPassword(String input) throws Exception {
        // given
        ProfileImage profileImage = profileImageRepository.save(createProfileImage("memberImage"));
        Member member = memberRepository.save(createNativeTypeMember("member@email.com", profileImage));

        DeactivateRequest request = createDeactivateRequest(input);

        mockAuthentication(member);

        // when, then
        mockMvc.perform(patch("/api/members/deactivate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("비밀번호는 8자 이상 15자 이하의 영문, 숫자, 특수문자 조합이어야 합니다."));
    }

    @Test
    @DisplayName("회원 탈퇴 - 일정 데이터 없는 경우")
    void deactivateMember_emptySchedule() throws Exception {
        // given
        String password = "password12!@";

        ProfileImage profileImage = profileImageRepository.save(createProfileImage("memberImage"));
        String encodedPassword = passwordEncoder.encode(password);
        Member member = memberRepository.save(createNativeTypeMember("member@email.com", encodedPassword, profileImage));

        bookmarkRepository.saveAll(List.of(
                createBookmark(member, place1),
                createBookmark(member, place1)
        ));

        mockAuthentication(member);

        DeactivateRequest request = createDeactivateRequest(password);

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
        Cookie[] cookies = result.getResponse().getCookies();

        assertThat(cookies).isNotNull();
        assertThat(cookies.length).isEqualTo(3);
    }


    @Test
    @DisplayName("회원 탈퇴 시 회원 데이터를 찾을 수 없는 경우")
    void deactivateMember_memberNotFound() throws Exception {
        // given
        ProfileImage profileImage = createProfileImage("memberImage");
        Member notMember = createNativeTypeMemberWithId(1000L, "member@email.com", profileImage);

        mockAuthentication(notMember);

        DeactivateRequest request = createDeactivateRequest("test123@");

        // when, then
        mockMvc.perform(patch("/api/members/deactivate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.MEMBER_NOT_FOUND.getMessage()));
    }


    @Test
    @DisplayName("소셜 회원 탈퇴 요청으로 예외 발생")
    void deactivateMember_socialMember() throws Exception {
        // given
        ProfileImage profileImage = profileImageRepository.save(createProfileImage("memberImage"));
        Member member = memberRepository.save(createSocialTypeMember("member@email.com", profileImage));

        socialMemberRepository.saveAll(List.of(
                createSocialMember(member, SocialType.KAKAO, "kakao"),
                createSocialMember(member, SocialType.NAVER, "naver")
        ));

        mockAuthentication(member);

        DeactivateRequest request = createDeactivateRequest("test123@");

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



    @Test
    @DisplayName("회원 탈퇴 시 비밀번호가 맞지 않아 예외 발생")
    void deactivateMember_incorrectPassword() throws Exception {
        // given
        ProfileImage profileImage = profileImageRepository.save(createProfileImage("member"));
        String encodedPassword = passwordEncoder.encode("incorrect12!@");
        Member member = memberRepository.save(createNativeTypeMember("member@email.com", encodedPassword, profileImage));

        mockAuthentication(member);

        DeactivateRequest request = createDeactivateRequest("test123@");

        // when, then
        mockMvc.perform(patch("/api/members/deactivate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(ErrorCode.INCORRECT_PASSWORD.getMessage()));
    }

}
