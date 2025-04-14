package com.triptune.member.service;

import com.triptune.bookmark.enumclass.BookmarkSortType;
import com.triptune.bookmark.repository.BookmarkRepository;
import com.triptune.bookmark.service.BookmarkService;
import com.triptune.email.dto.request.EmailRequest;
import com.triptune.email.exception.EmailVerifyException;
import com.triptune.email.service.EmailService;
import com.triptune.global.enumclass.ErrorCode;
import com.triptune.global.enumclass.RedisKeyType;
import com.triptune.global.exception.CustomJwtUnAuthorizedException;
import com.triptune.global.exception.DataExistException;
import com.triptune.global.exception.DataNotFoundException;
import com.triptune.global.util.JwtUtils;
import com.triptune.global.util.PageUtils;
import com.triptune.global.util.RedisUtils;
import com.triptune.member.dto.request.*;
import com.triptune.member.dto.response.LoginResponse;
import com.triptune.member.dto.response.MemberInfoResponse;
import com.triptune.member.dto.response.RefreshTokenResponse;
import com.triptune.member.entity.Member;
import com.triptune.member.exception.FailLoginException;
import com.triptune.member.exception.IncorrectPasswordException;
import com.triptune.member.repository.MemberRepository;
import com.triptune.profile.entity.ProfileImage;
import com.triptune.profile.service.ProfileImageService;
import com.triptune.schedule.entity.TravelAttendee;
import com.triptune.schedule.repository.ChatMessageRepository;
import com.triptune.schedule.repository.TravelAttendeeRepository;
import com.triptune.schedule.repository.TravelScheduleRepository;
import com.triptune.travel.dto.response.PlaceBookmarkResponse;
import com.triptune.travel.entity.TravelPlace;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.mail.MessagingException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class MemberService {
    private static final int LOGOUT_DURATION = 3600;

    private final MemberRepository memberRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final RedisUtils redisUtils;
    private final ProfileImageService profileImageService;
    private final BookmarkService bookmarkService;
    private final TravelAttendeeRepository travelAttendeeRepository;
    private final TravelScheduleRepository travelScheduleRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final BookmarkRepository bookmarkRepository;



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
        String isVerified = redisUtils.getEmailData(RedisKeyType.VERIFIED, email);

        if(isVerified == null || !isVerified.equals("true")){
            throw new EmailVerifyException(ErrorCode.NOT_VERIFIED_EMAIL);
        }
    }


    public LoginResponse login(LoginRequest loginRequest) {
        Member member = memberRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new FailLoginException(ErrorCode.FAILED_LOGIN));

        if (!isPasswordMatch(loginRequest.getPassword(), member.getPassword())) {
            throw new FailLoginException(ErrorCode.FAILED_LOGIN);
        }

        String accessToken = jwtUtils.createAccessToken(member.getEmail());
        String refreshToken = jwtUtils.createRefreshToken(member.getEmail());

        member.updateRefreshToken(refreshToken);

        return LoginResponse.of(accessToken, refreshToken, member.getNickname());
    }

    private boolean isPasswordMatch(String requestPassword, String savedPassword){
        return passwordEncoder.matches(requestPassword, savedPassword);
    }

    public void logout(LogoutRequest logoutRequest, String accessToken) {
        if (!isExistNickname(logoutRequest.getNickname())){
            throw new DataNotFoundException(ErrorCode.MEMBER_NOT_FOUND);
        }

        memberRepository.deleteRefreshTokenByNickname(logoutRequest.getNickname());
        redisUtils.saveExpiredData(accessToken, "logout", LOGOUT_DURATION);
    }

    public RefreshTokenResponse refreshToken(RefreshTokenRequest refreshTokenRequest) throws ExpiredJwtException {
        String refreshToken = refreshTokenRequest.getRefreshToken();
        jwtUtils.validateToken(refreshToken);

        Claims claims = jwtUtils.parseClaims(refreshToken);

        Member member = getMemberByEmail(claims.getSubject());
        validateSavedRefreshToken(member, refreshToken);

        String newAccessToken = jwtUtils.createAccessToken(member.getEmail());
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
        String email = redisUtils.getData(resetPasswordRequest.getPasswordToken());

        if (email == null) {
            throw new IncorrectPasswordException(ErrorCode.INVALID_CHANGE_PASSWORD);
        }

        Member member = getMemberByEmail(email);
        member.updatePassword(passwordEncoder.encode(resetPasswordRequest.getPassword()));
    }


    public void changePassword(String email, ChangePasswordRequest passwordRequest){
        Member member = getMemberByEmail(email);

        if(!isPasswordMatch(passwordRequest.getNowPassword(), member.getPassword())){
            throw new IncorrectPasswordException(ErrorCode.INCORRECT_PASSWORD);
        }

        member.updatePassword(passwordEncoder.encode(passwordRequest.getNewPassword()));
    }


    public MemberInfoResponse getMemberInfo(String email) {
        Member member = getMemberByEmail(email);
        return MemberInfoResponse.from(member);
    }

    public void changeNickname(String email, ChangeNicknameRequest changeNicknameRequest) {
        validateUniqueNickname(changeNicknameRequest.getNickname());

        Member member = getMemberByEmail(email);
        member.updateNickname(changeNicknameRequest.getNickname());
    }

    public void changeEmail(String email, EmailRequest emailRequest) {
        validateUniqueEmail(emailRequest.getEmail());
        validateVerifiedEmail(emailRequest.getEmail());

        Member member = getMemberByEmail(email);
        member.updateEmail(emailRequest.getEmail());
    }

    public Page<PlaceBookmarkResponse> getMemberBookmarks(int page, String email, BookmarkSortType sortType) {
        Pageable pageable = PageUtils.bookmarkPageable(page);
        Page<TravelPlace> travelPlaces = bookmarkService.getBookmarkTravelPlaces(email, pageable, sortType);

        return travelPlaces.map(PlaceBookmarkResponse::from);
    }


    public void deactivateMember(String accessToken, String email, DeactivateRequest deactivateRequest) {
        // 1. 사용자 비밀번호 확인
        Member member = getMemberByEmail(email);

        if(!isPasswordMatch(deactivateRequest.getPassword(), member.getPassword())){
            throw new IncorrectPasswordException(ErrorCode.INCORRECT_PASSWORD);
        }

        // 2. 프로필 이미지 기본으로 변경
        profileImageService.updateDefaultProfileImage(member);

        // 3-1. 작성자인 경우 참석자, 일정, 채팅방 삭제
        // 3-2. 참석자인 경우 참석자 삭제
        List<TravelAttendee> attendees = travelAttendeeRepository.findAllByMember_Email(email);

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
        bookmarkRepository.deleteAllByMember_Email(email);

        // 5. 익명 데이터로 변경 (닉네임, 아이디, 비밀번호, 리프레시 토큰, 이메일)
        member.updateDeactivate();

        // 6. 로그아웃
        redisUtils.saveExpiredData(accessToken, "logout", LOGOUT_DURATION);
    }


}
