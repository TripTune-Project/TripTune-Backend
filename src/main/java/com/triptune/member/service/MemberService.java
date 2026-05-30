package com.triptune.member.service;

import com.triptune.bookmark.enums.BookmarkSortType;
import com.triptune.bookmark.repository.BookmarkRepository;
import com.triptune.bookmark.repository.dto.PlaceBookmarkQueryDto;
import com.triptune.email.dto.request.EmailRequest;
import com.triptune.email.exception.EmailVerifyException;
import com.triptune.email.service.EmailService;
import com.triptune.global.exception.DataExistException;
import com.triptune.global.exception.DataNotFoundException;
import com.triptune.global.redis.RedisService;
import com.triptune.global.redis.eums.RedisKeyType;
import com.triptune.global.message.ErrorCode;
import com.triptune.global.s3.S3ObjectManager;
import com.triptune.global.security.jwt.exception.CustomJwtUnAuthorizedException;
import com.triptune.global.security.jwt.JwtUtils;
import com.triptune.global.util.PageUtils;
import com.triptune.member.exception.InvalidPasswordResetTokenException;
import com.triptune.member.service.dto.LoginResult;
import com.triptune.member.dto.request.*;
import com.triptune.member.dto.response.MemberInfoResponse;
import com.triptune.member.dto.response.RefreshTokenResponse;
import com.triptune.member.entity.Member;
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
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@Transactional(readOnly = true)
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
    private final S3ObjectManager s3ObjectManager;


    @Transactional
    public void join(JoinRequest joinRequest) {
        validateUniqueEmail(joinRequest.getEmail());
        validateUniqueNickname(joinRequest.getNickname());
        validateVerifiedEmail(joinRequest.getEmail());

        ProfileImage profileImage = profileImageService.saveDefaultProfileImage();

        Member member = Member.createNativeMember(
                joinRequest.getEmail(),
                passwordEncoder.encode(joinRequest.getPassword()),
                joinRequest.getNickname(),
                profileImage
        );
        memberRepository.save(member);
    }


    @Transactional
    public LoginResult login(LoginRequest loginRequest) {
        Member member = memberRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new FailLoginException(ErrorCode.FAILED_LOGIN));

        validatePassword(loginRequest.getPassword(), member.getPassword(),
                new FailLoginException(ErrorCode.FAILED_LOGIN));

        String accessToken = jwtUtils.createAccessToken(member.getMemberId());
        String refreshToken = jwtUtils.createRefreshToken(member.getMemberId());

        member.updateRefreshToken(refreshToken);
        return new LoginResult(accessToken, refreshToken, member.getNickname());
    }


    @Transactional
    public void logout(LogoutRequest logoutRequest, String accessToken) {
        Member member = getMemberByNickname(logoutRequest.getNickname());
        member.logout();

        redisService.saveExpiredData(accessToken, "logout", LOGOUT_DURATION);
    }

    private Member getMemberByNickname(String nickname){
        return memberRepository.findByNickname(nickname)
                .orElseThrow(() ->  new DataNotFoundException(ErrorCode.MEMBER_NOT_FOUND));
    }


    public RefreshTokenResponse refreshToken(String refreshToken) {
        jwtUtils.validateToken(refreshToken);
        Long memberId = jwtUtils.getMemberIdByToken(refreshToken);

        Member member = getMemberById(memberId);
        validateSavedRefreshToken(member, refreshToken);

        String newAccessToken = jwtUtils.createAccessToken(member.getMemberId());
        log.info("access token 재발급 완료 - memberId: {}", member.getMemberId());
        return RefreshTokenResponse.of(newAccessToken);
    }


    private void validateSavedRefreshToken(Member member, String refreshToken){
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

    @Transactional
    public void resetPassword(ResetPasswordRequest resetPasswordRequest) {
        String email = redisService.getData(resetPasswordRequest.getPasswordToken());

        if (email == null) {
            throw new InvalidPasswordResetTokenException(ErrorCode.INVALID_CHANGE_PASSWORD_TOKEN);
        }

        Member member = getMemberByEmail(email);
        String encodedPassword = passwordEncoder.encode(resetPasswordRequest.getPassword());
        member.resetPassword(encodedPassword);
    }


    private Member getMemberByEmail(String email){
        return memberRepository.findByEmail(email)
                .orElseThrow(() -> new DataNotFoundException(ErrorCode.MEMBER_NOT_FOUND));
    }

    @Transactional
    public void changePassword(Long memberId, ChangePasswordRequest passwordRequest){
        Member member = getMemberById(memberId);

        validateSocialMember(member, ErrorCode.SOCIAL_MEMBER_PASSWORD_CHANGE_NOT_ALLOWED);
        validatePassword(passwordRequest.getNowPassword(),
                member.getPassword(),
                new IncorrectPasswordException(ErrorCode.INCORRECT_PASSWORD));

        member.updatePassword(passwordEncoder.encode(passwordRequest.getNewPassword()));
    }


    public MemberInfoResponse getMemberInfo(Long memberId) {
        Member member = getMemberById(memberId);
        String profileImageUrl = s3ObjectManager.generateS3ObjectUrl(member.getProfileImage().getS3ObjectKey());
        return MemberInfoResponse.from(member, profileImageUrl);
    }


    @Transactional
    public void changeNickname(Long memberId, ChangeNicknameRequest changeNicknameRequest) {
        validateUniqueNickname(changeNicknameRequest.getNickname());

        Member member = getMemberById(memberId);
        member.updateNickname(changeNicknameRequest.getNickname());
    }

    @Transactional
    public void changeEmail(Long memberId, EmailRequest emailRequest) {
        validateUniqueEmail(emailRequest.getEmail());
        validateVerifiedEmail(emailRequest.getEmail());

        Member member = getMemberById(memberId);
        member.updateEmail(emailRequest.getEmail());
    }

    public Page<PlaceBookmarkResponse> getMemberBookmarks(int page, Long memberId, BookmarkSortType sortType) {
        Pageable pageable = PageUtils.bookmarkPageable(page);
        Page<PlaceBookmarkQueryDto> travelPlaces = bookmarkRepository.findSortedMemberBookmarks(memberId, pageable, sortType);

        List<PlaceBookmarkResponse> contents = travelPlaces.getContent()
                .stream()
                .map(place -> {
                    String thumbnailUrl = s3ObjectManager.generateS3ObjectUrl(place.getThumbnailS3ObjectKey());
                    return PlaceBookmarkResponse.of(place, thumbnailUrl);
                })
                .toList();

        return PageUtils.createPage(contents, travelPlaces.getPageable(), travelPlaces.getTotalElements());
    }

    @Transactional
    public void deactivateMember(DeactivateRequest deactivateRequest, Long memberId, String accessToken) {
        // 1. 회원 비밀번호 확인
        Member member = getMemberWithSocialMembers(memberId);

        // 2. 소셜 회원인지, 비밀번호 맞는지 먼저 확인
        validateSocialMember(member, ErrorCode.SOCIAL_MEMBER_DEACTIVATE_NOT_ALLOWED);
        validatePassword(deactivateRequest.getPassword(), member.getPassword(),
                new IncorrectPasswordException(ErrorCode.INCORRECT_PASSWORD));

        // 3. 프로필 이미지 기본으로 변경
        profileImageService.updateDefaultProfileImage(member);

        // 4-1. 작성자인 경우 참석자, 일정, 채팅방 삭제
        // 4-2. 참석자인 경우 참석자 삭제
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

        // 5. 북마크 삭제
        bookmarkRepository.deleteAllByMember_MemberId(memberId);

        // 6. 익명 데이터로 변경 (닉네임, 아이디, 비밀번호, 리프레시 토큰, 이메일)
        member.deactivate();

        // 7. 로그아웃
        member.logout();
        redisService.saveExpiredData(accessToken, "logout", LOGOUT_DURATION);
    }

    private void validateSocialMember(Member member, ErrorCode errorCode) {
        if (member.isSocialMember()){
            throw new UnsupportedSocialMemberException(errorCode);
        }
    }

    private Member getMemberWithSocialMembers(Long memberId){
        return memberRepository.findByIdWithSocialMembers(memberId)
                .orElseThrow(() -> new DataNotFoundException(ErrorCode.MEMBER_NOT_FOUND));
    }


    private void validateUniqueEmail(String email){
        if(memberRepository.existsByEmail(email)){
            throw new DataExistException(ErrorCode.ALREADY_EXISTED_EMAIL);
        }
    }

    private void validateUniqueNickname(String nickname){
        if(memberRepository.existsByNickname(nickname)){
            throw new DataExistException(ErrorCode.ALREADY_EXISTED_NICKNAME);
        }
    }

    private void validateVerifiedEmail(String email){
        String isVerified = redisService.getEmailData(RedisKeyType.VERIFIED, email);

        if(isVerified == null || !isVerified.equals("true")){
            throw new EmailVerifyException(ErrorCode.NOT_VERIFIED_EMAIL);
        }
    }

    private Member getMemberById(Long memberId){
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new DataNotFoundException(ErrorCode.MEMBER_NOT_FOUND));
    }

    private void validatePassword(String rawPassword, String encodedPassword, RuntimeException exception){
        if(!passwordEncoder.matches(rawPassword, encodedPassword)){
            throw exception;
        }
    }




}
