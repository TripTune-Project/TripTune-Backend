package com.triptune.member.service;

import com.triptune.bookmark.enums.BookmarkSortType;
import com.triptune.bookmark.repository.BookmarkRepository;
import com.triptune.common.entity.ApiCategory;
import com.triptune.common.entity.City;
import com.triptune.common.entity.Country;
import com.triptune.common.entity.District;
import com.triptune.email.dto.request.EmailRequest;
import com.triptune.email.exception.EmailVerifyException;
import com.triptune.email.service.EmailService;
import com.triptune.global.util.CookieUtils;
import com.triptune.member.MemberTest;
import com.triptune.member.dto.request.*;
import com.triptune.member.dto.response.LoginResponse;
import com.triptune.member.dto.response.MemberInfoResponse;
import com.triptune.member.dto.response.RefreshTokenResponse;
import com.triptune.member.entity.Member;
import com.triptune.member.enums.JoinType;
import com.triptune.member.exception.IncorrectPasswordException;
import com.triptune.member.exception.FailLoginException;
import com.triptune.member.exception.UnsupportedSocialMemberException;
import com.triptune.member.repository.MemberRepository;
import com.triptune.profile.entity.ProfileImage;
import com.triptune.profile.service.ProfileImageService;
import com.triptune.schedule.entity.TravelAttendee;
import com.triptune.schedule.entity.TravelSchedule;
import com.triptune.schedule.enums.AttendeePermission;
import com.triptune.schedule.enums.AttendeeRole;
import com.triptune.schedule.repository.ChatMessageRepository;
import com.triptune.schedule.repository.TravelAttendeeRepository;
import com.triptune.schedule.repository.TravelScheduleRepository;
import com.triptune.travel.dto.response.PlaceBookmarkResponse;
import com.triptune.travel.entity.TravelImage;
import com.triptune.travel.entity.TravelPlace;
import com.triptune.global.response.enums.ErrorCode;
import com.triptune.global.security.exception.CustomJwtUnAuthorizedException;
import com.triptune.global.exception.DataExistException;
import com.triptune.global.exception.DataNotFoundException;
import com.triptune.global.security.jwt.JwtUtils;
import com.triptune.global.util.PageUtils;
import com.triptune.global.redis.RedisService;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MemberServiceTest extends MemberTest {

    @InjectMocks private MemberService memberService;
    @Mock private MemberRepository memberRepository;
    @Mock private JwtUtils jwtUtils;
    @Mock private RedisService redisService;
    @Mock private EmailService emailService;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private ProfileImageService profileImageService;
    @Mock private TravelAttendeeRepository travelAttendeeRepository;
    @Mock private ChatMessageRepository chatMessageRepository;
    @Mock private TravelScheduleRepository travelScheduleRepository;
    @Mock private BookmarkRepository bookmarkRepository;
    @Spy private CookieUtils cookieUtils;

    private final String accessToken = "MemberAccessToken";
    private final String refreshToken = "MemberRefreshToken";
    private final String passwordToken = "MemberPasswordToken";

    private Member member;
    private TravelPlace travelPlace1;
    private TravelPlace travelPlace2;
    private TravelPlace travelPlace3;

    @BeforeEach
    void setUp(){
        Country country = createCountry();
        City city = createCity(country);
        District district = createDistrict(city, "강남구");
        ApiCategory apiCategory = createApiCategory();

        member = createMember(1L, "member@email.com");
        TravelImage travelImage1 = createTravelImage(travelPlace1, "test", true);
        TravelImage travelImage2 = createTravelImage(travelPlace2, "test", true);
        TravelImage travelImage3 = createTravelImage(travelPlace3, "test", true);
        travelPlace1 = createTravelPlace(1L, country, city, district, apiCategory, "가장소", List.of(travelImage1));
        travelPlace2 = createTravelPlace(2L, country, city, district, apiCategory, "나장소", List.of(travelImage2));
        travelPlace3 = createTravelPlace(3L, country, city, district, apiCategory, "다장소", List.of(travelImage3));
    }


    @Test
    @DisplayName("회원가입")
    void join(){
        // given
        JoinRequest request = createMemberRequest(
                "member@email.com",
                "password12!@",
                "password12!@",
                "nickname"
        );

        when(memberRepository.existsByEmail(anyString())).thenReturn(false);
        when(memberRepository.existsByNickname(anyString())).thenReturn(false);
        when(memberRepository.existsByEmail(anyString())).thenReturn(false);
        when(redisService.getEmailData(any(), anyString())).thenReturn("true");
        when(profileImageService.saveDefaultProfileImage()).thenReturn(createProfileImage(1L, "test.jpg"));

        // when
        assertDoesNotThrow(() -> memberService.join(request));

        // then
        verify(memberRepository, times(1)).save(any(Member.class));
    }


    @Test
    @DisplayName("회원가입 시 이미 가입한 이메일이 존재해 예외 발생")
    void join_duplicatedEmail(){
        // given
        JoinRequest request = createMemberRequest(
                "member@email.com",
                "password12!@",
                "password12!@",
                "nickname"
        );

        when(memberRepository.existsByEmail(anyString())).thenReturn(true);

        // when
        DataExistException fail = assertThrows(DataExistException.class, () -> memberService.join(request));

        // then
        verify(memberRepository, times(0)).save(any(Member.class));
        assertThat(fail.getHttpStatus()).isEqualTo(ErrorCode.ALREADY_EXISTED_EMAIL.getStatus());
        assertThat(fail.getMessage()).isEqualTo(ErrorCode.ALREADY_EXISTED_EMAIL.getMessage());
    }

    @Test
    @DisplayName("회원가입 시 이미 가입한 닉네임이 존재해 예외 발생")
    void join_duplicatedNickname(){
        // given
        JoinRequest request = createMemberRequest(
                "member@email.com",
                "password12!@",
                "password12!@",
                "nickname"
        );

        when(memberRepository.existsByEmail(anyString())).thenReturn(false);
        when(memberRepository.existsByNickname(anyString())).thenReturn(true);

        // when
        DataExistException fail = assertThrows(DataExistException.class, () -> memberService.join(request));

        // then
        verify(memberRepository, times(0)).save(any());
        assertThat(fail.getHttpStatus()).isEqualTo(ErrorCode.ALREADY_EXISTED_NICKNAME.getStatus());
        assertThat(fail.getMessage()).isEqualTo(ErrorCode.ALREADY_EXISTED_NICKNAME.getMessage());
    }



    @Test
    @DisplayName("회원가입 시 인증된 이메일이 아니여서 예외 발생")
    void join_notVerifiedEmail(){
        // given
        JoinRequest request = createMemberRequest(
                "member@email.com",
                "password12!@",
                "password12!@",
                "nickname"
        );

        when(memberRepository.existsByEmail(anyString())).thenReturn(false);
        when(memberRepository.existsByNickname(anyString())).thenReturn(false);
        when(redisService.getEmailData(any(), anyString())).thenReturn(null);

        // when
        EmailVerifyException fail = assertThrows(EmailVerifyException.class, () -> memberService.join(request));

        // then
        assertThat(fail.getHttpStatus()).isEqualTo(ErrorCode.NOT_VERIFIED_EMAIL.getStatus());
        assertThat(fail.getMessage()).isEqualTo(ErrorCode.NOT_VERIFIED_EMAIL.getMessage());

    }

    @Test
    @DisplayName("인증된 이메일인지 검증")
    void validateVerifiedEmail(){
        // given
        when(redisService.getEmailData(any(), anyString())).thenReturn("true");

        // when
        assertDoesNotThrow(() -> memberService.validateVerifiedEmail("member@email.com"));
    }

    @Test
    @DisplayName("인증된 이메일이 아니여서 예외 발생")
    void validateVerifiedEmail_notVerifiedEmail(){
        // given
        when(redisService.getEmailData(any(), anyString())).thenReturn(null);

        // when
        EmailVerifyException fail = assertThrows(EmailVerifyException.class, () -> memberService.validateVerifiedEmail("member@email.com"));

        // then
        assertThat(fail.getHttpStatus()).isEqualTo(ErrorCode.NOT_VERIFIED_EMAIL.getStatus());
        assertThat(fail.getMessage()).isEqualTo(ErrorCode.NOT_VERIFIED_EMAIL.getMessage());
    }


    @Test
    @DisplayName("로그인")
    void login(){
        // given
        MockHttpServletResponse mockHttpServletResponse = new MockHttpServletResponse();
        LoginRequest loginRequest = createLoginRequest(member.getEmail(), passwordToken);

        when(memberRepository.findByEmail(anyString())).thenReturn(Optional.of(member));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(jwtUtils.createAccessToken(anyLong())).thenReturn(accessToken);
        when(jwtUtils.createRefreshToken(anyLong())).thenReturn(refreshToken);

        // when
        LoginResponse response = memberService.login(loginRequest, mockHttpServletResponse);

        // then
        assertThat(response.getNickname()).isEqualTo(member.getNickname());
        assertThat(response.getAccessToken()).isNotNull();

        Cookie[] cookies = mockHttpServletResponse.getCookies();
        assertThat(cookies).isNotNull();
        assertThat(cookies.length).isEqualTo(1);
    }

    @Test
    @DisplayName("로그인 시 회원 데이터 없어 예외 발생")
    void login_memberNotFound(){
        // given
        MockHttpServletResponse mockHttpServletResponse = new MockHttpServletResponse();
        LoginRequest loginRequest = createLoginRequest(member.getEmail(), passwordToken);

        when(memberRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        // when
        FailLoginException fail = assertThrows(FailLoginException.class,
                () -> memberService.login(loginRequest, mockHttpServletResponse));

        // then
        assertThat(fail.getHttpStatus()).isEqualTo(ErrorCode.FAILED_LOGIN.getStatus());
        assertThat(fail.getMessage()).isEqualTo(ErrorCode.FAILED_LOGIN.getMessage());
    }

    @Test
    @DisplayName("로그인 시 비밀번호 맞지 않아 예외 발생")
    void login_mismatchPassword(){
        // given
        MockHttpServletResponse mockHttpServletResponse = new MockHttpServletResponse();
        LoginRequest loginRequest = createLoginRequest(member.getEmail(), passwordToken);

        when(memberRepository.findByEmail(anyString())).thenReturn(Optional.of(member));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        // when
        FailLoginException fail = assertThrows(FailLoginException.class,
                () -> memberService.login(loginRequest, mockHttpServletResponse));

        // then
        assertThat(fail.getHttpStatus()).isEqualTo(ErrorCode.FAILED_LOGIN.getStatus());
        assertThat(fail.getMessage()).isEqualTo(ErrorCode.FAILED_LOGIN.getMessage());
    }

    @Test
    @DisplayName("로그아웃")
    void logout(){
        // given
        LogoutRequest request = createLogoutRequest(member.getNickname());
        MockHttpServletResponse mockHttpServletResponse = new MockHttpServletResponse();

        when(memberRepository.existsByNickname(anyString())).thenReturn(true);

        // when
        memberService.logout(mockHttpServletResponse, request, accessToken);

        // then
        Cookie[] cookies = mockHttpServletResponse.getCookies();
        assertThat(cookies).isNotNull();
        assertThat(cookies.length).isEqualTo(3);

        verify(memberRepository, times(1)).deleteRefreshTokenByNickname(request.getNickname());
        verify(redisService, times(1)).saveExpiredData(accessToken, "logout", 3600);
    }

    @Test
    @DisplayName("로그아웃 요청 시 회원 데이터 없어 예외 발생")
    void logout_memberNotFound(){
        // given
        LogoutRequest request = createLogoutRequest("notMember");
        MockHttpServletResponse mockHttpServletResponse = new MockHttpServletResponse();

        when(memberRepository.existsByNickname(anyString())).thenReturn(false);

        // when
        DataNotFoundException fail = assertThrows(DataNotFoundException.class,
                () -> memberService.logout(mockHttpServletResponse, request, accessToken));


        // then
        assertThat(fail.getHttpStatus()).isEqualTo(ErrorCode.MEMBER_NOT_FOUND.getStatus());
        assertThat(fail.getMessage()).isEqualTo(ErrorCode.MEMBER_NOT_FOUND.getMessage());

    }


    @Test
    @DisplayName("refresh token 갱신")
    void refreshToken(){
        // given
        when(jwtUtils.validateToken(anyString())).thenReturn(true);
        when(jwtUtils.getMemberIdByToken(anyString())).thenReturn(member.getMemberId());
        when(memberRepository.findById(any())).thenReturn(Optional.of(member));
        when(jwtUtils.createAccessToken(anyLong())).thenReturn(accessToken);

        // when
        RefreshTokenResponse response = memberService.refreshToken(refreshToken);

        // then
        assertThat(response.getAccessToken()).isNotNull();
    }


    @Test
    @DisplayName("토큰 갱신 시 저장된 refresh token 과 요청 refresh token 이 불일치해 예외 발생")
    void refreshToken_misMatchRefreshToken() {
        // given
        String notEqualRefreshToken = "NotEqualRefreshToken";

        when(jwtUtils.validateToken(anyString())).thenReturn(true);
        when(jwtUtils.getMemberIdByToken(anyString())).thenReturn(member.getMemberId());
        when(memberRepository.findById(any())).thenReturn(Optional.of(member));

        // when
        CustomJwtUnAuthorizedException fail = assertThrows(CustomJwtUnAuthorizedException.class,
                () -> memberService.refreshToken(notEqualRefreshToken));

        // then
        assertThat(fail.getHttpStatus()).isEqualTo(ErrorCode.MISMATCH_REFRESH_TOKEN.getStatus());
        assertThat(fail.getMessage()).isEqualTo(ErrorCode.MISMATCH_REFRESH_TOKEN.getMessage());
    }

    @Test
    @DisplayName("비밀번호 찾기 성공")
    void findPassword() throws MessagingException {
        // given
        FindPasswordRequest findPasswordRequest = createFindPasswordRequest("test");

        when(memberRepository.existsByEmail(anyString())).thenReturn(true);

        // when
        assertDoesNotThrow(() -> memberService.findPassword(findPasswordRequest));

        // then
        verify(emailService, times(1)).sendResetPasswordEmail(findPasswordRequest);
    }

    @Test
    @DisplayName("비밀번호 찾기 시 회원 정보 존재하지 않아 예외 발생")
    void findPassword_memberNotFound() throws MessagingException {
        // given
        FindPasswordRequest findPasswordRequest = createFindPasswordRequest("fail");

        when(memberRepository.existsByEmail(anyString())).thenReturn(false);

        // when
        DataNotFoundException fail = assertThrows(DataNotFoundException.class, () -> memberService.findPassword(findPasswordRequest));

        // then
        assertThat(fail.getHttpStatus()).isEqualTo(ErrorCode.MEMBER_NOT_FOUND.getStatus());
        assertThat(fail.getMessage()).isEqualTo(ErrorCode.MEMBER_NOT_FOUND.getMessage());
        verify(emailService, times(0)).sendResetPasswordEmail(findPasswordRequest);
    }


    @Test
    @DisplayName("일반 회원 비밀번호 초기화")
    void resetPassword_nativeMember(){
        // given
        ResetPasswordRequest request = createResetPasswordRequest(passwordToken, "password12!@", "password12!@");

        String savedPassword = "savedPassword12!@";
        ProfileImage profileImage = createProfileImage(1L, "profileImage");
        Member member = createNativeTypeMember(1L, "member@email.com", savedPassword, profileImage);

        when(redisService.getData(anyString())).thenReturn(member.getEmail());
        when(memberRepository.findByEmail(anyString())).thenReturn(Optional.of(member));
        when(passwordEncoder.encode(anyString())).thenReturn(request.getPassword());

        // when
        memberService.resetPassword(request);

        // then
        verify(redisService, times(1)).getData(passwordToken);
        verify(memberRepository, times(1)).findByEmail(member.getEmail());
        assertThat(member.getPassword()).isEqualTo(request.getPassword());
        assertThat(member.getJoinType()).isEqualTo(JoinType.NATIVE);
        assertThat(member.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("소셜 회원 비밀번호 초기화")
    void resetPassword_socialMember(){
        // given
        ResetPasswordRequest request = createResetPasswordRequest(passwordToken, "password12!@", "password12!@");

        ProfileImage profileImage = createProfileImage(1L, "profileImage");
        Member member = createSocialTypeMember(1L, "member@email.com", profileImage);

        when(redisService.getData(anyString())).thenReturn(member.getEmail());
        when(memberRepository.findByEmail(anyString())).thenReturn(Optional.of(member));
        when(passwordEncoder.encode(anyString())).thenReturn(request.getPassword());

        // when
        memberService.resetPassword(request);

        // then
        verify(redisService, times(1)).getData(passwordToken);
        verify(memberRepository, times(1)).findByEmail(member.getEmail());
        assertThat(member.getPassword()).isEqualTo(request.getPassword());
        assertThat(member.getJoinType()).isEqualTo(JoinType.BOTH);
        assertThat(member.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("통합 회원 비밀번호 초기화")
    void resetPassword_bothMember(){
        // given
        ResetPasswordRequest request = createResetPasswordRequest(passwordToken, "password12!@", "password12!@");

        String savedPassword = "savedPassword12!@";
        ProfileImage profileImage = createProfileImage(1L, "profileImage");
        Member member = createBothTypeMember(1L, "member@email.com", savedPassword, profileImage);

        when(redisService.getData(anyString())).thenReturn(member.getEmail());
        when(memberRepository.findByEmail(anyString())).thenReturn(Optional.of(member));
        when(passwordEncoder.encode(anyString())).thenReturn(request.getPassword());

        // when
        memberService.resetPassword(request);

        // then
        verify(redisService, times(1)).getData(passwordToken);
        verify(memberRepository, times(1)).findByEmail(member.getEmail());
        assertThat(member.getPassword()).isEqualTo(request.getPassword());
        assertThat(member.getJoinType()).isEqualTo(JoinType.BOTH);
        assertThat(member.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("비밀번호 초기화 시 비밀번호 토큰 유효 시간이 만료되어 예외 발생")
    void resetPassword_expiredResetPasswordToken(){
        // given
        ResetPasswordRequest request = createResetPasswordRequest(passwordToken, "password12!@", "password12!@");

        when(redisService.getData(anyString())).thenReturn(null);


        // when
        IncorrectPasswordException fail = assertThrows(IncorrectPasswordException.class,
                () -> memberService.resetPassword(request));

        // then
        assertThat(fail.getHttpStatus()).isEqualTo(ErrorCode.INVALID_CHANGE_PASSWORD.getStatus());
        assertThat(fail.getMessage()).isEqualTo(ErrorCode.INVALID_CHANGE_PASSWORD.getMessage());
    }

    @Test
    @DisplayName("비밀번호 초기화 시 회원 정보를 찾을 수 없어 예외 발생")
    void resetPassword_memberNotFound(){
        // given
        ResetPasswordRequest request = createResetPasswordRequest(passwordToken, "password12!@", "password12!@");

        when(redisService.getData(anyString())).thenReturn(member.getEmail());
        when(memberRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        // when
        DataNotFoundException fail = assertThrows(DataNotFoundException.class,
                () -> memberService.resetPassword(request));

        // then
        assertThat(fail.getHttpStatus()).isEqualTo(ErrorCode.MEMBER_NOT_FOUND.getStatus());
        assertThat(fail.getMessage()).isEqualTo(ErrorCode.MEMBER_NOT_FOUND.getMessage());
    }


    @Test
    @DisplayName("비밀번호 변경")
    void changePassword(){
        // given
        ChangePasswordRequest request = createChangePasswordRequest("test123@", "test123!", "test123!");

        ProfileImage profileImage = createProfileImage(1L, "profileImage");
        Member member = createNativeTypeMember(1L, "member@email.com", request.getNowPassword(), profileImage);

        String newPassword = "newEncodingPassword";

        when(memberRepository.findById(anyLong())).thenReturn(Optional.of(member));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(passwordEncoder.encode(anyString())).thenReturn(newPassword);

        // when, then
        assertDoesNotThrow(() -> memberService.changePassword(member.getMemberId(), request));

        assertThat(member.getPassword()).isEqualTo(newPassword);
        assertThat(member.getUpdatedAt()).isNotNull();

    }

    @Test
    @DisplayName("비밀번호 변경 시 회원 정보 찾을 수 없어 예외 발생")
    void changePassword_memberNotFound(){
        // given
        ChangePasswordRequest request = createChangePasswordRequest("test123@", "test123!", "test123!");

        when(memberRepository.findById(anyLong())).thenReturn(Optional.empty());

        // when
        DataNotFoundException fail = assertThrows(DataNotFoundException.class,
                () -> memberService.changePassword(member.getMemberId(), request));

        // then
        assertThat(fail.getHttpStatus()).isEqualTo(ErrorCode.MEMBER_NOT_FOUND.getStatus());
        assertThat(fail.getMessage()).isEqualTo(ErrorCode.MEMBER_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("비밀번호 변경 시 소셜 회원으로 예외 발생")
    void changePassword_socialMember(){
        // given
        ChangePasswordRequest request = createChangePasswordRequest("test123@", "test123!", "test123!");
        ProfileImage profileImage = createProfileImage(1L, "profileImage");
        Member member = createSocialTypeMember(1L, "member@email.com", profileImage);

        when(memberRepository.findById(anyLong())).thenReturn(Optional.of(member));

        // when, then
        assertThrows(UnsupportedSocialMemberException.class,
                () -> memberService.changePassword(member.getMemberId(), request));

    }

    @Test
    @DisplayName("비밀번호 변경 시 저장된 비밀번호와 일치하지 않아 예외 발생")
    void changePassword_incorrectSavedPassword(){
        // given
        ChangePasswordRequest request = createChangePasswordRequest("incorrect123@", "test123!", "test123!");

        String encodePassword = request.getNowPassword();
        ProfileImage profileImage = createProfileImage(1L, "profileImage");
        Member member = createNativeTypeMember(1L, "member@email.com", encodePassword, profileImage);

        when(memberRepository.findById(anyLong())).thenReturn(Optional.of(member));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        // when
        IncorrectPasswordException fail = assertThrows(IncorrectPasswordException.class,
                () -> memberService.changePassword(member.getMemberId(), request));

        // then
        assertThat(fail.getHttpStatus()).isEqualTo(ErrorCode.INCORRECT_PASSWORD.getStatus());
        assertThat(fail.getMessage()).isEqualTo(ErrorCode.INCORRECT_PASSWORD.getMessage());
    }

    @Test
    @DisplayName("회원 정보 조회")
    void getMemberInfo(){
        // given
        ProfileImage savedProfileImage = createProfileImage(1L, "memberImage");
        Member savedMember = createMember(1L, "member@email.com", savedProfileImage);

        when(memberRepository.findById(anyLong())).thenReturn(Optional.of(savedMember));

        // when
        MemberInfoResponse response = memberService.getMemberInfo(member.getMemberId());

        // then
        assertThat(response.getEmail()).isEqualTo(savedMember.getEmail());
        assertThat(response.getNickname()).isEqualTo(savedMember.getNickname());
        assertThat(response.getProfileImage()).isEqualTo(savedProfileImage.getS3ObjectUrl());
    }

    @Test
    @DisplayName("회원 정보 조회 시 회원 데이터가 없어 예외 발생")
    void getMemberInfo_memberNotFound(){
        // given
        when(memberRepository.findById(anyLong())).thenReturn(Optional.empty());

        // when
        DataNotFoundException fail = assertThrows(DataNotFoundException.class,
                () -> memberService.getMemberInfo(member.getMemberId()));

        // then
        assertThat(fail.getHttpStatus()).isEqualTo(ErrorCode.MEMBER_NOT_FOUND.getStatus());
        assertThat(fail.getMessage()).isEqualTo(ErrorCode.MEMBER_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("회원 닉네임 변경")
    void changeNickname(){
        // given
        ChangeNicknameRequest request = createChangeNicknameRequest("newNickname");

        when(memberRepository.existsByNickname(request.getNickname())).thenReturn(false);
        when(memberRepository.findById(anyLong())).thenReturn(Optional.of(member));

        // when, then
        assertDoesNotThrow(() -> memberService.changeNickname(member.getMemberId(), request));
        assertThat(member.getNickname()).isEqualTo(request.getNickname());
    }

    @Test
    @DisplayName("회원 닉네임 변경 시 이미 존재하는 닉네임으로 예외 발생")
    void changeNickname_duplicatedNickname(){
        // given
        ChangeNicknameRequest request = createChangeNicknameRequest("newNickname");

        when(memberRepository.existsByNickname(anyString())).thenReturn(true);

        // when
        DataExistException fail = assertThrows(DataExistException.class,
                () -> memberService.changeNickname(member.getMemberId(), request));

        // then
        assertThat(fail.getHttpStatus()).isEqualTo(ErrorCode.ALREADY_EXISTED_NICKNAME.getStatus());
        assertThat(fail.getMessage()).isEqualTo(ErrorCode.ALREADY_EXISTED_NICKNAME.getMessage());
    }

    @Test
    @DisplayName("회원 닉네임 변경 시 회원 데이터가 없어 예외 발생")
    void changeNickname_memberNotFound(){
        // given
        ChangeNicknameRequest request = createChangeNicknameRequest("newNickname");

        when(memberRepository.existsByNickname(request.getNickname())).thenReturn(false);
        when(memberRepository.findById(anyLong())).thenReturn(Optional.empty());

        // when
        DataNotFoundException fail = assertThrows(DataNotFoundException.class,
                () -> memberService.changeNickname(member.getMemberId(), request));

        // then
        assertThat(fail.getHttpStatus()).isEqualTo(ErrorCode.MEMBER_NOT_FOUND.getStatus());
        assertThat(fail.getMessage()).isEqualTo(ErrorCode.MEMBER_NOT_FOUND.getMessage());
    }



    @Test
    @DisplayName("이메일 변경")
    void changeEmail(){
        // given
        EmailRequest emailRequest = createEmailRequest("changeMember@email.com");

        when(memberRepository.findById(anyLong())).thenReturn(Optional.of(member));
        when(memberRepository.existsByEmail(anyString())).thenReturn(false);
        when(redisService.getEmailData(any(), anyString())).thenReturn("true");

        // when
        assertThatCode(() -> memberService.changeEmail(member.getMemberId(), emailRequest))
                .doesNotThrowAnyException();

        // then
        assertThat(member.getEmail()).isEqualTo(emailRequest.getEmail());
    }

    @Test
    @DisplayName("이메일 변경 시 이미 존재하는 이메일로 예외 발생")
    void changeEmail_duplicatedEmail(){
        // given
        EmailRequest emailRequest = createEmailRequest("changeMember@email.com");

        when(memberRepository.existsByEmail(anyString())).thenReturn(true);

        // when
        assertThatThrownBy(() -> memberService.changeEmail(member.getMemberId(), emailRequest))
                .isInstanceOf(DataExistException.class)
                .hasMessage(ErrorCode.ALREADY_EXISTED_EMAIL.getMessage());

    }

    @Test
    @DisplayName("이메일 변경 시 인증된 이메일이 아니여서 예외 발생")
    void changeEmail_notVerifiedEmail(){
        // given
        EmailRequest emailRequest = createEmailRequest("changeMember@email.com");

        when(memberRepository.existsByEmail(anyString())).thenReturn(false);
        when(redisService.getEmailData(any(), anyString())).thenReturn(null);

        // when
        EmailVerifyException fail = assertThrows(EmailVerifyException.class,
                () -> memberService.changeEmail(member.getMemberId(), emailRequest));

        // then
        assertThat(fail.getHttpStatus()).isEqualTo(ErrorCode.NOT_VERIFIED_EMAIL.getStatus());
        assertThat(fail.getMessage()).isEqualTo(ErrorCode.NOT_VERIFIED_EMAIL.getMessage());
    }

    @Test
    @DisplayName("이메일 변경 시 회원 데이터 존재하지 않아 예외 발생")
    void changeEmail_memberNotFound(){
        // given
        EmailRequest emailRequest = createEmailRequest("changeMember@email.com");

        when(memberRepository.existsByEmail(anyString())).thenReturn(false);
        when(redisService.getEmailData(any(), anyString())).thenReturn("true");
        when(memberRepository.findById(anyLong())).thenReturn(Optional.empty());


        // when
        DataNotFoundException fail = assertThrows(DataNotFoundException.class,
                () -> memberService.changeEmail(member.getMemberId(), emailRequest));

        // then
        assertThat(fail.getHttpStatus()).isEqualTo(ErrorCode.MEMBER_NOT_FOUND.getStatus());
        assertThat(fail.getMessage()).isEqualTo(ErrorCode.MEMBER_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("북마크로 등록된 여행지 데이터 조회 - 최신순")
    void getMemberBookmarks_sortNewest(){
        // given
        Pageable pageable = PageUtils.bookmarkPageable(1);

        List<TravelPlace> travelPlaceList = List.of(travelPlace1, travelPlace2, travelPlace3);
        Page<TravelPlace> travelPlacePage = PageUtils.createPage(travelPlaceList, pageable, travelPlaceList.size());

        when(bookmarkRepository.findSortedMemberBookmarks(anyLong(), any(), any()))
                .thenReturn(travelPlacePage);

        // when
        Page<PlaceBookmarkResponse> response = memberService.getMemberBookmarks(1, member.getMemberId(), BookmarkSortType.NEWEST);

        // then
        List<PlaceBookmarkResponse> content = response.getContent();
        assertThat(response.getTotalElements()).isEqualTo(3);
        assertThat(content.get(0).getPlaceName()).isEqualTo(travelPlace1.getPlaceName());
        assertThat(content.get(0).getAddress()).isEqualTo(travelPlace1.getAddress());
        assertThat(content.get(1).getPlaceName()).isEqualTo(travelPlace2.getPlaceName());
        assertThat(content.get(1).getAddress()).isEqualTo(travelPlace2.getAddress());
        assertThat(content.get(2).getPlaceName()).isEqualTo(travelPlace3.getPlaceName());
        assertThat(content.get(2).getAddress()).isEqualTo(travelPlace3.getAddress());
    }


    @Test
    @DisplayName("북마크로 등록된 여행지 데이터 조회 - 이름 순")
    void getMemberBookmarks_sortName(){
        // given
        Pageable pageable = PageUtils.bookmarkPageable(1);

        List<TravelPlace> travelPlaceList = List.of(travelPlace1, travelPlace2, travelPlace3);
        Page<TravelPlace> travelPlacePage = PageUtils.createPage(travelPlaceList, pageable, travelPlaceList.size());

        when(bookmarkRepository.findSortedMemberBookmarks(anyLong(), any(), any()))
                .thenReturn(travelPlacePage);

        // when
        Page<PlaceBookmarkResponse> response = memberService.getMemberBookmarks(1, member.getMemberId(), BookmarkSortType.NAME);

        // then
        List<PlaceBookmarkResponse> content = response.getContent();
        assertThat(response.getTotalElements()).isEqualTo(3);
        assertThat(content.get(0).getPlaceName()).isEqualTo(travelPlace1.getPlaceName());
        assertThat(content.get(0).getAddress()).isEqualTo(travelPlace1.getAddress());
        assertThat(content.get(1).getPlaceName()).isEqualTo(travelPlace2.getPlaceName());
        assertThat(content.get(1).getAddress()).isEqualTo(travelPlace2.getAddress());
        assertThat(content.get(2).getPlaceName()).isEqualTo(travelPlace3.getPlaceName());
        assertThat(content.get(2).getAddress()).isEqualTo(travelPlace3.getAddress());
    }

    @Test
    @DisplayName("북마크로 등록된 여행지 데이터 조회 시 데이터 없는 경우")
    void getMemberBookmarks_emptyData(){
        // given
        Pageable pageable = PageUtils.bookmarkPageable(1);

        Page<TravelPlace> travelPlacePage = PageUtils.createPage(new ArrayList<>(), pageable, 0);

        when(bookmarkRepository.findSortedMemberBookmarks(anyLong(), any(), any()))
                .thenReturn(travelPlacePage);

        // when
        Page<PlaceBookmarkResponse> response = memberService.getMemberBookmarks(1, member.getMemberId(), BookmarkSortType.NAME);

        // then
        assertThat(response.getTotalElements()).isEqualTo(0);
        assertThat(response.getContent()).isEmpty();
    }


    @Test
    @DisplayName("회원 탈퇴")
    void deactivateMember1(){
        // given
        MockHttpServletResponse mockHttpServletResponse = new MockHttpServletResponse();
        DeactivateRequest request = createDeactivateRequest(member.getPassword());

        TravelSchedule schedule1 = createTravelSchedule(1L, "테스트1");
        TravelSchedule schedule2 = createTravelSchedule(2L, "테스트2");

        List<TravelAttendee> attendees = List.of(
                createTravelAttendee(1L, member, schedule1, AttendeeRole.AUTHOR, AttendeePermission.ALL),
                createTravelAttendee(2L, member, schedule2, AttendeeRole.GUEST, AttendeePermission.READ)
        );

        when(memberRepository.findById(anyLong())).thenReturn(Optional.of(member));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(travelAttendeeRepository.findAllByMember_MemberId(anyLong())).thenReturn(attendees);

        // when
        assertDoesNotThrow(() -> memberService.deactivateMember(mockHttpServletResponse, accessToken, member.getMemberId(), request));


        // then
        Cookie[] cookies = mockHttpServletResponse.getCookies();
        assertThat(cookies).isNotNull();
        assertThat(cookies.length).isEqualTo(3);
        assertThat(member.getEmail()).isEqualTo("알 수 없음");
        assertThat(member.getPassword()).isEqualTo("알 수 없음");
    }


    @Test
    @DisplayName("회원 탈퇴 시 일정 데이터가 존재하지 않는 경우")
    void deactivateMember_emptySchedule(){
        // given
        MockHttpServletResponse mockHttpServletResponse = new MockHttpServletResponse();
        DeactivateRequest request = createDeactivateRequest(member.getPassword());

        when(memberRepository.findById(anyLong())).thenReturn(Optional.of(member));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(travelAttendeeRepository.findAllByMember_MemberId(anyLong())).thenReturn(new ArrayList<>());

        // when
        assertDoesNotThrow(() -> memberService.deactivateMember(mockHttpServletResponse, accessToken, member.getMemberId(), request));


        // then
        assertThat(member.getEmail()).isEqualTo("알 수 없음");
        assertThat(member.getPassword()).isEqualTo("알 수 없음");
    }

    @Test
    @DisplayName("회원 탈퇴 시 회원 데이터가 존재하지 않아 예외 발생")
    void deactivateMember_memberNotFound(){
        // given
        MockHttpServletResponse mockHttpServletResponse = new MockHttpServletResponse();
        DeactivateRequest request = createDeactivateRequest(member.getPassword());

        when(memberRepository.findById(anyLong())).thenReturn(Optional.empty());

        // when
        DataNotFoundException fail = assertThrows(DataNotFoundException.class,
                () -> memberService.deactivateMember(mockHttpServletResponse, accessToken, member.getMemberId(), request));

        // then
        assertThat(fail.getHttpStatus()).isEqualTo(ErrorCode.MEMBER_NOT_FOUND.getStatus());
        assertThat(fail.getMessage()).isEqualTo(ErrorCode.MEMBER_NOT_FOUND.getMessage());

    }

    @Test
    @DisplayName("회원 탈퇴 요청 시 비밀번호가 맞지 않아 예외 발생")
    void deactivateMember_incorrectPassword(){
        // given
        MockHttpServletResponse mockHttpServletResponse = new MockHttpServletResponse();
        DeactivateRequest request = createDeactivateRequest("incorrect_password");

        when(memberRepository.findById(anyLong())).thenReturn(Optional.of(member));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        // when
        IncorrectPasswordException fail = assertThrows(IncorrectPasswordException.class,
                () -> memberService.deactivateMember(mockHttpServletResponse, accessToken, member.getMemberId(), request));

        // then
        assertThat(fail.getHttpStatus()).isEqualTo(ErrorCode.INCORRECT_PASSWORD.getStatus());
        assertThat(fail.getMessage()).isEqualTo(ErrorCode.INCORRECT_PASSWORD.getMessage());

    }


}
