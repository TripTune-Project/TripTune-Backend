package com.triptune.member.service;

import com.triptune.CookieType;
import com.triptune.bookmark.enums.BookmarkSortType;
import com.triptune.bookmark.repository.BookmarkRepository;
import com.triptune.email.dto.request.EmailRequest;
import com.triptune.email.exception.EmailVerifyException;
import com.triptune.email.service.EmailService;
import com.triptune.global.exception.CustomIllegalArgumentException;
import com.triptune.global.response.enums.ErrorCode;
import com.triptune.global.redis.eums.RedisKeyType;
import com.triptune.global.security.exception.CustomJwtUnAuthorizedException;
import com.triptune.global.exception.DataExistException;
import com.triptune.global.exception.DataNotFoundException;
import com.triptune.global.security.jwt.JwtUtils;
import com.triptune.global.util.CookieUtils;
import com.triptune.global.util.PageUtils;
import com.triptune.global.redis.RedisService;
import com.triptune.member.dto.request.*;
import com.triptune.member.dto.response.LoginResponse;
import com.triptune.member.dto.response.MemberInfoResponse;
import com.triptune.member.dto.response.RefreshTokenResponse;
import com.triptune.member.entity.Member;
import com.triptune.member.enums.JoinType;
import com.triptune.member.exception.FailLoginException;
import com.triptune.member.exception.IncorrectPasswordException;
import com.triptune.member.exception.UnsupportedSocialMemberException;
import com.triptune.member.repository.MemberRepository;
import com.triptune.profile.entity.ProfileImage;
import com.triptune.profile.service.ProfileImageService;
import com.triptune.schedule.entity.TravelAttendee;
import com.triptune.schedule.repository.ChatMessageRepository;
import com.triptune.schedule.repository.TravelAttendeeRepository;
import com.triptune.schedule.repository.TravelScheduleRepository;
import com.triptune.travel.dto.response.PlaceBookmarkResponse;
import com.triptune.travel.entity.TravelPlace;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Stream;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class MemberService {
    private static final int LOGOUT_DURATION = 3600;

    private final MemberRepository memberRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final RedisService redisService;
    private final ProfileImageService profileImageService;
    private final TravelAttendeeRepository travelAttendeeRepository;
    private final TravelScheduleRepository travelScheduleRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final BookmarkRepository bookmarkRepository;
    private final CookieUtils cookieUtils;


    public void join(JoinRequest joinRequest) {
        validateUniqueEmail(joinRequest.getEmail());
        validateUniqueNickname(joinRequest.getNickname());
        validateVerifiedEmail(joinRequest.getEmail());

        ProfileImage profileImage = profileImageService.saveDefaultProfileImage();

        Member member = Member.from(joinRequest, profileImage, passwordEncoder.encode(joinRequest.getPassword()));
        memberRepository.save(member);
    }


    public void validateUniqueEmail(String email){
        if(memberRepository.existsByEmail(email)){
            throw new DataExistException(ErrorCode.ALREADY_EXISTED_EMAIL);
        }
    }

    public void validateUniqueNickname(String nickname){
        if(isExistNickname(nickname)){
            throw new DataExistException(ErrorCode.ALREADY_EXISTED_NICKNAME);
        }
    }

    private boolean isExistNickname(String nickname){
        return memberRepository.existsByNickname(nickname);
    }


    public void validateVerifiedEmail(String email){
        String isVerified = redisService.getEmailData(RedisKeyType.VERIFIED, email);

        if(isVerified == null || !isVerified.equals("true")){
            throw new EmailVerifyException(ErrorCode.NOT_VERIFIED_EMAIL);
        }
    }


    public LoginResponse login(LoginRequest loginRequest, HttpServletResponse response) {
        Member member = memberRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new FailLoginException(ErrorCode.FAILED_LOGIN));

        if (!isPasswordMatch(loginRequest.getPassword(), member.getPassword())) {
            throw new FailLoginException(ErrorCode.FAILED_LOGIN);
        }

        String accessToken = jwtUtils.createAccessToken(member.getMemberId());
        String refreshToken = jwtUtils.createRefreshToken(member.getMemberId());

        member.updateRefreshToken(refreshToken);

        response.addHeader("Set-Cookie", cookieUtils.createCookie(CookieType.REFRESH_TOKEN, refreshToken));

        return LoginResponse.of(accessToken, member.getNickname());
    }

    private boolean isPasswordMatch(String requestPassword, String savedPassword){
        return passwordEncoder.matches(requestPassword, savedPassword);
    }


    public void logout(HttpServletResponse response, LogoutRequest logoutRequest, String accessToken) {
        if (!isExistNickname(logoutRequest.getNickname())){
            throw new DataNotFoundException(ErrorCode.MEMBER_NOT_FOUND);
        }

        memberRepository.deleteRefreshTokenByNickname(logoutRequest.getNickname());
        redisService.saveExpiredData(accessToken, "logout", LOGOUT_DURATION);

        deleteCookies(response);
    }


    public RefreshTokenResponse refreshToken(String refreshToken) throws ExpiredJwtException {
        jwtUtils.validateToken(refreshToken);
        Long memberId = jwtUtils.getMemberIdByToken(refreshToken);

        Member member = getMemberById(memberId);
        validateSavedRefreshToken(member, refreshToken);

        String newAccessToken = jwtUtils.createAccessToken(member.getMemberId());
        log.info("Access Token 재발급 완료 - memberId: {}", member.getMemberId());
        return RefreshTokenResponse.of(newAccessToken);
    }

    private Member getMemberByEmail(String email){
        return memberRepository.findByEmail(email)
                .orElseThrow(() -> new DataNotFoundException(ErrorCode.MEMBER_NOT_FOUND));
    }

    public void validateSavedRefreshToken(Member member, String refreshToken){
        if(!member.isMatchRefreshToken(refreshToken)){
            throw new CustomJwtUnAuthorizedException(ErrorCode.MISMATCH_REFRESH_TOKEN);
        }
    }

    public void findPassword(FindPasswordRequest findPasswordRequest) throws MessagingException {
        boolean isExistsMember = memberRepository.existsByEmail(findPasswordRequest.getEmail());
        if (!isExistsMember){
            throw new DataNotFoundException(ErrorCode.MEMBER_NOT_FOUND);
        }

        emailService.sendResetPasswordEmail(findPasswordRequest);
    }


    public void resetPassword(ResetPasswordRequest resetPasswordRequest) {
        String email = redisService.getData(resetPasswordRequest.getPasswordToken());

        if (email == null) {
            throw new IncorrectPasswordException(ErrorCode.INVALID_CHANGE_PASSWORD);
        }

        Member member = getMemberByEmail(email);
        String encodedPassword = passwordEncoder.encode(resetPasswordRequest.getPassword());
        member.resetPassword(encodedPassword);
    }


    public void changePassword(Long memberId, ChangePasswordRequest passwordRequest){
        Member member = getMemberById(memberId);

        if (member.isSocialMember()){
            throw new UnsupportedSocialMemberException(ErrorCode.SOCIAL_MEMBER_PASSWORD_CHANGE_NOT_ALLOWED);
        }

        if(!isPasswordMatch(passwordRequest.getNowPassword(), member.getPassword())){
            throw new IncorrectPasswordException(ErrorCode.INCORRECT_PASSWORD);
        }

        member.updatePassword(passwordEncoder.encode(passwordRequest.getNewPassword()));
    }

    public void validateSocialMember(Member member){

    }


    public MemberInfoResponse getMemberInfo(Long memberId) {
        Member member = getMemberById(memberId);
        return MemberInfoResponse.from(member);
    }

    private Member getMemberById(Long memberId){
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new DataNotFoundException(ErrorCode.MEMBER_NOT_FOUND));
    }

    public void changeNickname(Long memberId, ChangeNicknameRequest changeNicknameRequest) {
        validateUniqueNickname(changeNicknameRequest.getNickname());

        Member member = getMemberById(memberId);
        member.updateNickname(changeNicknameRequest.getNickname());
    }

    public void changeEmail(Long memberId, EmailRequest emailRequest) {
        validateUniqueEmail(emailRequest.getEmail());
        validateVerifiedEmail(emailRequest.getEmail());

        Member member = getMemberById(memberId);
        member.updateEmail(emailRequest.getEmail());
    }

    public Page<PlaceBookmarkResponse> getMemberBookmarks(int page, Long memberId, BookmarkSortType sortType) {
        Pageable pageable = PageUtils.bookmarkPageable(page);
        Page<TravelPlace> travelPlaces = bookmarkRepository.findSortedMemberBookmarks(memberId, pageable, sortType);

        return travelPlaces.map(PlaceBookmarkResponse::from);
    }


    public void deactivateMember(HttpServletResponse response, String accessToken, Long memberId, DeactivateRequest deactivateRequest) {
        // 1. 회원 비밀번호 확인
        Member member = getMemberById(memberId);

        if(!isPasswordMatch(deactivateRequest.getPassword(), member.getPassword())){
            throw new IncorrectPasswordException(ErrorCode.INCORRECT_PASSWORD);
        }

        // 2. 프로필 이미지 기본으로 변경
        profileImageService.updateDefaultProfileImage(member);

        // 3-1. 작성자인 경우 참석자, 일정, 채팅방 삭제
        // 3-2. 참석자인 경우 참석자 삭제
        List<TravelAttendee> attendees = travelAttendeeRepository.findAllByMember_MemberId(memberId);

        for (TravelAttendee attendee : attendees) {
            if (attendee.getRole().isAuthor()) {
                Long scheduleId = attendee.getTravelSchedule().getScheduleId();

                chatMessageRepository.deleteAllByScheduleId(scheduleId);
                travelScheduleRepository.deleteById(scheduleId);
            } else{
                travelAttendeeRepository.delete(attendee);
            }
        }

        // 4. 북마크 삭제
        bookmarkRepository.deleteAllByMember_MemberId(memberId);

        // 5. 익명 데이터로 변경 (닉네임, 아이디, 비밀번호, 리프레시 토큰, 이메일)
        member.updateDeactivate();

        // 6. 로그아웃
        member.updateRefreshToken(null);
        redisService.saveExpiredData(accessToken, "logout", LOGOUT_DURATION);

        deleteCookies(response);
    }


    public void deleteCookies(HttpServletResponse response){
        Stream.of(CookieType.ACCESS_TOKEN, CookieType.REFRESH_TOKEN, CookieType.NICKNAME)
                .forEach(type -> response.addHeader("Set-Cookie", cookieUtils.deleteCookie(type)));
    }


}
