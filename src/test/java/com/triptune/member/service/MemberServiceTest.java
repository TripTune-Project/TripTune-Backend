package com.triptune.member.service;

import com.triptune.bookmark.enumclass.BookmarkSortType;
import com.triptune.bookmark.repository.BookmarkRepository;
import com.triptune.bookmark.service.BookmarkService;
import com.triptune.common.entity.ApiCategory;
import com.triptune.common.entity.City;
import com.triptune.common.entity.Country;
import com.triptune.common.entity.District;
import com.triptune.email.dto.request.EmailRequest;
import com.triptune.email.exception.EmailVerifyException;
import com.triptune.email.service.EmailService;
import com.triptune.member.MemberTest;
import com.triptune.member.dto.request.*;
import com.triptune.member.dto.response.LoginResponse;
import com.triptune.member.dto.response.MemberInfoResponse;
import com.triptune.member.dto.response.RefreshTokenResponse;
import com.triptune.member.entity.Member;
import com.triptune.member.exception.IncorrectPasswordException;
import com.triptune.member.exception.FailLoginException;
import com.triptune.member.repository.MemberRepository;
import com.triptune.profile.entity.ProfileImage;
import com.triptune.profile.service.ProfileImageService;
import com.triptune.schedule.entity.TravelAttendee;
import com.triptune.schedule.entity.TravelSchedule;
import com.triptune.schedule.enumclass.AttendeePermission;
import com.triptune.schedule.enumclass.AttendeeRole;
import com.triptune.schedule.repository.ChatMessageRepository;
import com.triptune.schedule.repository.TravelAttendeeRepository;
import com.triptune.schedule.repository.TravelScheduleRepository;
import com.triptune.travel.dto.response.PlaceBookmarkResponse;
import com.triptune.travel.entity.TravelImage;
import com.triptune.travel.entity.TravelPlace;
import com.triptune.global.enumclass.ErrorCode;
import com.triptune.global.exception.CustomJwtUnAuthorizedException;
import com.triptune.global.exception.DataExistException;
import com.triptune.global.exception.DataNotFoundException;
import com.triptune.global.util.JwtUtils;
import com.triptune.global.util.PageUtils;
import com.triptune.global.util.RedisUtils;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.mail.MessagingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    @Mock private RedisUtils redisUtils;
    @Mock private EmailService emailService;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private ProfileImageService profileImageService;
    @Mock private BookmarkService bookmarkService;
    @Mock private TravelAttendeeRepository travelAttendeeRepository;
    @Mock private ChatMessageRepository chatMessageRepository;
    @Mock private TravelScheduleRepository travelScheduleRepository;
    @Mock private BookmarkRepository bookmarkRepository;

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

        member = createMember(null, "member");
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
        JoinRequest request = createMemberRequest("member", "password12!@", "password12!@");

        when(memberRepository.existsByEmail(anyString())).thenReturn(false);
        when(memberRepository.existsByNickname(anyString())).thenReturn(false);
        when(memberRepository.existsByEmail(anyString())).thenReturn(false);
        when(redisUtils.getEmailData(any(), anyString())).thenReturn("true");
        when(profileImageService.saveDefaultProfileImage()).thenReturn(createProfileImage(1L, "test.jpg"));

        // when
        assertDoesNotThrow(() -> memberService.join(request));

        // then
        verify(memberRepository, times(1)).save(any(Member.class));
    }


    @Test
    @DisplayName("회원가입 시 이미 가입한 이메일이 존재해 예외 발생")
    void join_emailExistException(){
        // given
        JoinRequest request = createMemberRequest("member", "password12!@", "password12!@");

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
    void join_nicknameExistException(){
        // given
        JoinRequest request = createMemberRequest("member", "password12!@", "password12!@");

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
        JoinRequest request = createMemberRequest("member", "password12!@", "password12!@");

        when(memberRepository.existsByEmail(anyString())).thenReturn(false);
        when(memberRepository.existsByNickname(anyString())).thenReturn(false);
        when(redisUtils.getEmailData(any(), anyString())).thenReturn(null);

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
        when(redisUtils.getEmailData(any(), anyString())).thenReturn("true");

        // when
        assertDoesNotThrow(() -> memberService.validateVerifiedEmail("member@email.com"));
    }

    @Test
    @DisplayName("인증된 이메일이 아니여서 예외 발생")
    void validateVerifiedEmail_notVerifiedEmail (){
        // given
        when(redisUtils.getEmailData(any(), anyString())).thenReturn(null);

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
        LoginRequest loginRequest = createLoginRequest(member.getEmail(), passwordToken);

        when(memberRepository.findByEmail(anyString())).thenReturn(Optional.of(member));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(jwtUtils.createToken(anyString(), anyLong())).thenReturn(accessToken);
        when(jwtUtils.createToken(anyString(), anyLong())).thenReturn(refreshToken);

        // when
        LoginResponse response = memberService.login(loginRequest);

        // then
        assertThat(response.getNickname()).isEqualTo(member.getNickname());
        assertThat(response.getAccessToken()).isNotNull();
        assertThat(response.getRefreshToken()).isNotNull();
    }

    @Test
    @DisplayName("로그인 시 사용자 데이터 없어 예외 발생")
    void loginNotFoundUser_failLoginException(){
        // given
        LoginRequest loginRequest = createLoginRequest(member.getEmail(), passwordToken);

        when(memberRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        // when
        FailLoginException fail = assertThrows(FailLoginException.class, () -> memberService.login(loginRequest));

        // then
        assertThat(fail.getHttpStatus()).isEqualTo(ErrorCode.FAILED_LOGIN.getStatus());
        assertThat(fail.getMessage()).isEqualTo(ErrorCode.FAILED_LOGIN.getMessage());
    }

    @Test
    @DisplayName("로그인 시 비밀번호 맞지 않아 예외 발생")
    void loginMismatchPassword_failLoginException(){
        // given
        LoginRequest loginRequest = createLoginRequest(member.getEmail(), passwordToken);

        when(memberRepository.findByEmail(anyString())).thenReturn(Optional.of(member));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        // when
        FailLoginException fail = assertThrows(FailLoginException.class, () -> memberService.login(loginRequest));

        // then
        assertThat(fail.getHttpStatus()).isEqualTo(ErrorCode.FAILED_LOGIN.getStatus());
        assertThat(fail.getMessage()).isEqualTo(ErrorCode.FAILED_LOGIN.getMessage());
    }



    @Test
    @DisplayName("로그아웃")
    void logout(){
        // given
        LogoutRequest request = createLogoutRequest(member.getNickname());

        when(memberRepository.existsByNickname(anyString())).thenReturn(true);

        // when
        memberService.logout(request, accessToken);

        // then
        verify(memberRepository, times(1)).deleteRefreshTokenByNickname(request.getNickname());
        verify(redisUtils, times(1)).saveExpiredData(accessToken, "logout", 3600);
    }

    @Test
    @DisplayName("로그아웃 요청 시 사용자 데이터 없어 예외 발생")
    void logout_dataNotFoundException(){
        // given
        LogoutRequest request = createLogoutRequest("notMember");

        when(memberRepository.existsByNickname(anyString())).thenReturn(false);

        // when
        DataNotFoundException fail = assertThrows(DataNotFoundException.class, () -> memberService.logout(request, accessToken));

        // then
        assertThat(fail.getHttpStatus()).isEqualTo(ErrorCode.MEMBER_NOT_FOUND.getStatus());
        assertThat(fail.getMessage()).isEqualTo(ErrorCode.MEMBER_NOT_FOUND.getMessage());

    }


    @Test
    @DisplayName("refresh token 갱신")
    void refreshToken(){
        // given
        Claims mockClaims = Jwts.claims().setSubject("test");

        when(jwtUtils.validateToken(anyString())).thenReturn(true);
        when(jwtUtils.parseClaims(anyString())).thenReturn(mockClaims);
        when(memberRepository.findByEmail(any())).thenReturn(Optional.of(member));
        when(jwtUtils.createToken(anyString(), anyLong())).thenReturn(accessToken);

        RefreshTokenRequest request = createRefreshTokenRequest(refreshToken);

        // when
        RefreshTokenResponse response = memberService.refreshToken(request);

        // then
        assertThat(response.getAccessToken()).isNotNull();

    }


    @Test
    @DisplayName("토큰 갱신 시 저장된 refresh token 과 요청 refresh token 이 불일치해 예외 발생")
    void misMatchRefreshToken_customJwtBadRequestException() {
        // given
        String notEqualRefreshToken = "NotEqualRefreshToken";
        Claims mockClaims = Jwts.claims().setSubject("test");

        RefreshTokenRequest request = createRefreshTokenRequest(notEqualRefreshToken);

        when(jwtUtils.validateToken(anyString())).thenReturn(true);
        when(jwtUtils.parseClaims(anyString())).thenReturn(mockClaims);
        when(memberRepository.findByEmail(any())).thenReturn(Optional.of(member));

        // when
        CustomJwtUnAuthorizedException fail = assertThrows(CustomJwtUnAuthorizedException.class, () -> memberService.refreshToken(request));

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
    @DisplayName("비밀번호 찾기 시 사용자 정보 존재하지 않아 예외 발생")
    void findPassword_DataNotFoundException() throws MessagingException {
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
    @DisplayName("비밀번호 초기화")
    void resetPassword(){
        // given
        String newPassword = "newPassword";
        String encodedPassword = "encodedPassword";

        when(redisUtils.getData(anyString())).thenReturn(member.getEmail());
        when(memberRepository.findByEmail(anyString())).thenReturn(Optional.of(member));
        when(passwordEncoder.encode(newPassword)).thenReturn(encodedPassword);


        // when
        memberService.resetPassword(createResetPasswordRequest(passwordToken, newPassword, newPassword));

        // then
        verify(redisUtils, times(1)).getData(passwordToken);
        verify(memberRepository, times(1)).findByEmail(member.getEmail());
        verify(passwordEncoder, times(1)).encode(newPassword);
        assertThat(encodedPassword).isEqualTo(member.getPassword());
    }

    @Test
    @DisplayName("비밀번호 초기화 시 비밀번호 토큰 유효 시간이 만료되어 예외 발생")
    void changePassword_resetPasswordException(){
        // given
        String newPassword = "newPassword";
        when(redisUtils.getData(anyString())).thenReturn(null);

        // when
        IncorrectPasswordException fail = assertThrows(IncorrectPasswordException.class,
                () -> memberService.resetPassword(createResetPasswordRequest(accessToken, newPassword, newPassword)));

        // then
        assertThat(fail.getHttpStatus()).isEqualTo(ErrorCode.INVALID_CHANGE_PASSWORD.getStatus());
        assertThat(fail.getMessage()).isEqualTo(ErrorCode.INVALID_CHANGE_PASSWORD.getMessage());
    }

    @Test
    @DisplayName("비밀번호 초기화 시 사용자 정보를 찾을 수 없어 얘외 발생")
    void resetPassword_dataNotFoundException(){
        // given
        String newPassword = "newPassword";

        when(redisUtils.getData(anyString())).thenReturn(member.getEmail());
        when(memberRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        // when
        DataNotFoundException fail = assertThrows(DataNotFoundException.class,
                () -> memberService.resetPassword(createResetPasswordRequest(accessToken, newPassword, newPassword)));

        // then
        assertThat(fail.getHttpStatus()).isEqualTo(ErrorCode.MEMBER_NOT_FOUND.getStatus());
        assertThat(fail.getMessage()).isEqualTo(ErrorCode.MEMBER_NOT_FOUND.getMessage());
    }





    @Test
    @DisplayName("비밀번호 변경")
    void changePassword(){
        // given
        ChangePasswordRequest request = createChangePasswordRequest("test123@", "test123!", "test123!");
        Member member = createMember(1L, "member");

        when(memberRepository.findByEmail(anyString())).thenReturn(Optional.of(member));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(passwordEncoder.encode(anyString())).thenReturn("encodingPassword");

        // when, then
        assertDoesNotThrow(() -> memberService.changePassword("member", request));

    }

    @Test
    @DisplayName("비밀번호 변경 시 사용자 정보 찾을 수 없어 예외 발생")
    void changePasswordMemberNotFoundException(){
        // given
        ChangePasswordRequest request = createChangePasswordRequest("test123@", "test123!", "test123!");

        when(memberRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        // when
        DataNotFoundException fail = assertThrows(DataNotFoundException.class, () -> memberService.changePassword("member", request));

        // then
        assertThat(fail.getHttpStatus()).isEqualTo(ErrorCode.MEMBER_NOT_FOUND.getStatus());
        assertThat(fail.getMessage()).isEqualTo(ErrorCode.MEMBER_NOT_FOUND.getMessage());
    }


    @Test
    @DisplayName("비밀번호 변경 시 저장된 비밀번호와 일치하지 않아 예외 발생")
    void changePasswordIncorrectSavedPassword(){
        // given
        ChangePasswordRequest request = createChangePasswordRequest("incorrect123@", "test123!", "test123!");
        Member member = createMember(1L, "member");

        when(memberRepository.findByEmail(anyString())).thenReturn(Optional.of(member));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        // when
        IncorrectPasswordException fail = assertThrows(IncorrectPasswordException.class, () -> memberService.changePassword("member", request));

        // then
        assertThat(fail.getHttpStatus()).isEqualTo(ErrorCode.INCORRECT_PASSWORD.getStatus());
        assertThat(fail.getMessage()).isEqualTo(ErrorCode.INCORRECT_PASSWORD.getMessage());
    }

    @Test
    @DisplayName("사용자 정보 조회")
    void getMemberInfo(){
        // given
        ProfileImage savedProfileImage = createProfileImage(1L, "memberImage");
        Member savedMember = createMember(1L, "member", savedProfileImage);

        when(memberRepository.findByEmail(anyString())).thenReturn(Optional.of(savedMember));

        // when
        MemberInfoResponse response = memberService.getMemberInfo(member.getEmail());

        // then
        assertThat(response.getEmail()).isEqualTo(savedMember.getEmail());
        assertThat(response.getNickname()).isEqualTo(savedMember.getNickname());
        assertThat(response.getProfileImage()).isEqualTo(savedProfileImage.getS3ObjectUrl());
    }

    @Test
    @DisplayName("사용자 정보 조회 시 사용자 데이터가 없어 예외 발생")
    void getMemberInfo_memberNotFoundException(){
        // given
        when(memberRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        // when
        DataNotFoundException fail = assertThrows(DataNotFoundException.class,
                () -> memberService.getMemberInfo(member.getEmail()));

        // then
        assertThat(fail.getHttpStatus()).isEqualTo(ErrorCode.MEMBER_NOT_FOUND.getStatus());
        assertThat(fail.getMessage()).isEqualTo(ErrorCode.MEMBER_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("사용자 닉네임 변경")
    void changeNickname(){
        // given
        ChangeNicknameRequest request = createChangeNicknameRequest("newNickname");

        when(memberRepository.findByEmail(anyString())).thenReturn(Optional.of(member));
        when(memberRepository.existsByNickname(request.getNickname())).thenReturn(false);

        // when, then
        assertDoesNotThrow(() -> memberService.changeNickname(member.getEmail(), request));
        assertThat(member.getNickname()).isEqualTo(request.getNickname());
    }

    @Test
    @DisplayName("사용자 닉네임 변경 시 사용자 데이터가 없어 예외 발생")
    void changeNickname_memberNotFoundException(){
        // given
        ChangeNicknameRequest request = createChangeNicknameRequest("newNickname");

        when(memberRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        // when
        DataNotFoundException fail = assertThrows(DataNotFoundException.class,
                () -> memberService.changeNickname(member.getEmail(), request));

        // then
        assertThat(fail.getHttpStatus()).isEqualTo(ErrorCode.MEMBER_NOT_FOUND.getStatus());
        assertThat(fail.getMessage()).isEqualTo(ErrorCode.MEMBER_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("사용자 닉네임 변경 시 이미 존재하는 닉네임으로 예외 발생")
    void changeNickname_dataExistException(){
        // given
        ChangeNicknameRequest request = createChangeNicknameRequest("newNickname");

        when(memberRepository.existsByNickname(anyString())).thenReturn(true);

        // when
        DataExistException fail = assertThrows(DataExistException.class,
                () -> memberService.changeNickname(member.getEmail(), request));

        // then
        assertThat(fail.getHttpStatus()).isEqualTo(ErrorCode.ALREADY_EXISTED_NICKNAME.getStatus());
        assertThat(fail.getMessage()).isEqualTo(ErrorCode.ALREADY_EXISTED_NICKNAME.getMessage());
    }

    @Test
    @DisplayName("이메일 변경")
    void changeEmail(){
        // given
        EmailRequest emailRequest = createEmailRequest("changeMember@email.com");

        when(memberRepository.existsByEmail(anyString())).thenReturn(false);
        when(memberRepository.findByEmail(anyString())).thenReturn(Optional.of(member));
        when(redisUtils.getEmailData(any(), anyString())).thenReturn("true");

        // when
        assertThatCode(() -> memberService.changeEmail("member", emailRequest))
                .doesNotThrowAnyException();

        // then
        assertThat(member.getEmail()).isEqualTo(emailRequest.getEmail());
    }

    @Test
    @DisplayName("이메일 변경 시 이미 존재하는 이메일로 예외 발생")
    void changeEmail_duplicateEmailException(){
        // given
        EmailRequest emailRequest = createEmailRequest("changeMember@email.com");

        when(memberRepository.existsByEmail(anyString())).thenReturn(true);

        // when
        assertThatThrownBy(() -> memberService.changeEmail("member", emailRequest))
                .isInstanceOf(DataExistException.class)
                .hasMessage(ErrorCode.ALREADY_EXISTED_EMAIL.getMessage());

    }

    @Test
    @DisplayName("이메일 변경 시 인증된 이메일이 아니여서 예외 발생")
    void changeEmail_notVerifiedEmailException(){
        // given
        EmailRequest emailRequest = createEmailRequest("changeMember@email.com");

        when(memberRepository.existsByEmail(anyString())).thenReturn(false);
        when(redisUtils.getEmailData(any(), anyString())).thenReturn(null);

        // when
        assertThatThrownBy(() -> memberService.changeEmail("member", emailRequest))
                .isInstanceOf(EmailVerifyException.class)
                .hasMessage(ErrorCode.NOT_VERIFIED_EMAIL.getMessage());

    }

    @Test
    @DisplayName("이메일 변경 시 사용자 데이터 존재하지 않아 예외 발생")
    void changeEmail_memberNotFoundException(){
        // given
        EmailRequest emailRequest = createEmailRequest("changeMember@email.com");

        when(memberRepository.existsByEmail(anyString())).thenReturn(false);
        when(redisUtils.getEmailData(any(), anyString())).thenReturn("true");
        when(memberRepository.findByEmail(anyString())).thenReturn(Optional.empty());


        // when, then
        assertThatThrownBy(() -> memberService.changeEmail("member", emailRequest))
                .isInstanceOf(DataNotFoundException.class)
                .hasMessage(ErrorCode.MEMBER_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("북마크로 등록된 여행지 데이터 조회 - 최신순")
    void getMemberBookmarks_sortNewest(){
        // given
        Pageable pageable = PageUtils.bookmarkPageable(1);

        List<TravelPlace> travelPlaceList = List.of(travelPlace1, travelPlace2, travelPlace3);
        Page<TravelPlace> travelPlacePage = PageUtils.createPage(travelPlaceList, pageable, travelPlaceList.size());

        when(bookmarkService.getBookmarkTravelPlaces(anyString(), any(), any()))
                .thenReturn(travelPlacePage);

        // when
        Page<PlaceBookmarkResponse> response = memberService.getMemberBookmarks(1, "member", BookmarkSortType.NEWEST);

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

        when(bookmarkService.getBookmarkTravelPlaces(anyString(), any(), any()))
                .thenReturn(travelPlacePage);

        // when
        Page<PlaceBookmarkResponse> response = memberService.getMemberBookmarks(1, "member", BookmarkSortType.NAME);

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

        when(bookmarkService.getBookmarkTravelPlaces(anyString(), any(), any()))
                .thenReturn(travelPlacePage);

        // when
        Page<PlaceBookmarkResponse> response = memberService.getMemberBookmarks(1, "member", BookmarkSortType.NAME);

        // then
        assertThat(response.getTotalElements()).isEqualTo(0);
        assertThat(response.getContent()).isEmpty();
    }

    @Test
    @DisplayName("회원 탈퇴")
    void deactivateMember1(){
        // given
        DeactivateRequest request = createDeactivateRequest(member.getPassword());

        TravelSchedule schedule1 = createTravelSchedule(1L, "테스트1");
        TravelSchedule schedule2 = createTravelSchedule(2L, "테스트2");

        List<TravelAttendee> attendees = List.of(
                createTravelAttendee(1L, member, schedule1, AttendeeRole.AUTHOR, AttendeePermission.ALL),
                createTravelAttendee(2L, member, schedule2, AttendeeRole.GUEST, AttendeePermission.READ)
        );

        when(memberRepository.findByEmail(anyString())).thenReturn(Optional.of(member));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(travelAttendeeRepository.findAllByMember_Email(anyString())).thenReturn(attendees);

        // when
        assertDoesNotThrow(() -> memberService.deactivateMember(accessToken, member.getEmail(), request));


        // then
        assertThat(member.getEmail()).isEqualTo("알 수 없음");
        assertThat(member.getPassword()).isEqualTo("알 수 없음");
    }


    @Test
    @DisplayName("회원 탈퇴 시 일정 데이터가 존재하지 않는 경우")
    void deactivateMember_emptySchedule(){
        // given
        DeactivateRequest request = createDeactivateRequest(member.getPassword());

        when(memberRepository.findByEmail(anyString())).thenReturn(Optional.of(member));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(travelAttendeeRepository.findAllByMember_Email(anyString())).thenReturn(new ArrayList<>());

        // when
        assertDoesNotThrow(() -> memberService.deactivateMember(accessToken, member.getEmail(), request));


        // then
        assertThat(member.getEmail()).isEqualTo("알 수 없음");
        assertThat(member.getPassword()).isEqualTo("알 수 없음");
    }

    @Test
    @DisplayName("회원 탈퇴 시 사용자 데이터가 존재하지 않아 예외 발생")
    void deactivateMember_MemberDataNotFoundException(){
        // given
        DeactivateRequest request = createDeactivateRequest(member.getPassword());

        when(memberRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        // when
        DataNotFoundException fail = assertThrows(DataNotFoundException.class,
                () -> memberService.deactivateMember(accessToken, member.getEmail(), request));

        // then
        assertThat(fail.getHttpStatus()).isEqualTo(ErrorCode.MEMBER_NOT_FOUND.getStatus());
        assertThat(fail.getMessage()).isEqualTo(ErrorCode.MEMBER_NOT_FOUND.getMessage());

    }

    @Test
    @DisplayName("회원 탈퇴 요청 시 비밀번호가 맞지 않아 예외 발생")
    void deactivateMember_incorrectPasswordException(){
        // given
        DeactivateRequest request = createDeactivateRequest("incorrect_password");

        when(memberRepository.findByEmail(anyString())).thenReturn(Optional.of(member));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        // when
        IncorrectPasswordException fail = assertThrows(IncorrectPasswordException.class,
                () -> memberService.deactivateMember(accessToken, member.getEmail(), request));

        // then
        assertThat(fail.getHttpStatus()).isEqualTo(ErrorCode.INCORRECT_PASSWORD.getStatus());
        assertThat(fail.getMessage()).isEqualTo(ErrorCode.INCORRECT_PASSWORD.getMessage());

    }

}
