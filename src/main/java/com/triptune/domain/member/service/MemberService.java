package com.triptune.domain.member.service;

import com.triptune.domain.email.dto.EmailRequest;
import com.triptune.domain.email.exception.EmailVerifyException;
import com.triptune.domain.email.service.EmailService;
import com.triptune.domain.member.dto.request.*;
import com.triptune.domain.member.dto.response.FindIdResponse;
import com.triptune.domain.member.dto.response.LoginResponse;
import com.triptune.domain.member.dto.response.MemberInfoResponse;
import com.triptune.domain.member.dto.response.RefreshTokenResponse;
import com.triptune.domain.member.entity.Member;
import com.triptune.domain.member.exception.ChangeMemberInfoException;
import com.triptune.domain.member.exception.FailLoginException;
import com.triptune.domain.member.repository.MemberRepository;
import com.triptune.domain.profile.service.ProfileImageService;
import com.triptune.global.enumclass.ErrorCode;
import com.triptune.global.enumclass.RedisKeyType;
import com.triptune.global.exception.CustomJwtUnAuthorizedException;
import com.triptune.global.exception.DataExistException;
import com.triptune.global.exception.DataNotFoundException;
import com.triptune.global.util.JwtUtil;
import com.triptune.global.util.RedisUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.mail.MessagingException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@Transactional
@RequiredArgsConstructor
public class MemberService {
    private static final int LOGOUT_DURATION = 3600;

    private final MemberRepository memberRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final RedisUtil redisUtil;
    private final ProfileImageService profileImageService;

    @Value("${spring.jwt.token.access-expiration-time}")
    private long accessExpirationTime;

    @Value("${spring.jwt.token.refresh-expiration-time}")
    private long refreshExpirationTime;


    public void join(JoinRequest joinRequest) {
        checkDuplicateUserId(joinRequest.getUserId());
        checkDuplicateNickname(joinRequest.getNickname());
        checkDuplicateEmail(joinRequest.getEmail());
        validateVerifiedEmail(joinRequest.getEmail());

        Member member = Member.from(joinRequest, passwordEncoder.encode(joinRequest.getPassword()));
        Member savedMember = memberRepository.save(member);

        profileImageService.saveDefaultProfileImage(savedMember);
    }

    private void checkDuplicateUserId(String userId){
        if(memberRepository.existsByUserId(userId)){
            throw new DataExistException(ErrorCode.ALREADY_EXISTED_USERID);
        }
    }

    private void checkDuplicateNickname(String nickname){
        if(isExistNickname(nickname)){
            throw new DataExistException(ErrorCode.ALREADY_EXISTED_NICKNAME);
        }
    }

    private boolean isExistNickname(String nickname){
        return memberRepository.existsByNickname(nickname);
    }

    public void checkDuplicateEmail(String email){
        if(memberRepository.existsByEmail(email)){
            throw new DataExistException(ErrorCode.ALREADY_EXISTED_EMAIL);
        }
    }

    public void validateVerifiedEmail(String email){
        String isVerified = redisUtil.getEmailData(RedisKeyType.VERIFIED, email);

        if(isVerified == null || !isVerified.equals("true")){
            throw new EmailVerifyException(ErrorCode.NOT_VERIFIED_EMAIL);
        }
    }


    public LoginResponse login(LoginRequest loginRequest) {
        Member member = memberRepository.findByUserId(loginRequest.getUserId())
                .orElseThrow(() -> new FailLoginException(ErrorCode.FAILED_LOGIN));

        boolean isPasswordMatch = passwordEncoder.matches(loginRequest.getPassword(), member.getPassword());
        if (!isPasswordMatch) {
            throw new FailLoginException(ErrorCode.FAILED_LOGIN);
        }

        String accessToken = jwtUtil.createToken(loginRequest.getUserId(), accessExpirationTime);
        String refreshToken = jwtUtil.createToken(loginRequest.getUserId(), refreshExpirationTime);

        member.updateRefreshToken(refreshToken);

        return LoginResponse.of(accessToken, refreshToken, member.getNickname());
    }


    public void logout(LogoutRequest logoutRequest, String accessToken) {
        if (!isExistNickname(logoutRequest.getNickname())){
            throw new DataNotFoundException(ErrorCode.MEMBER_NOT_FOUND);
        }

        memberRepository.deleteRefreshTokenByNickname(logoutRequest.getNickname());
        redisUtil.saveExpiredData(accessToken, "logout", LOGOUT_DURATION);
    }


    public RefreshTokenResponse refreshToken(RefreshTokenRequest refreshTokenRequest) throws ExpiredJwtException {
        String refreshToken = refreshTokenRequest.getRefreshToken();
        jwtUtil.validateToken(refreshToken);

        Claims claims = jwtUtil.parseClaims(refreshToken);

        Member member = findMemberByUserId(claims.getSubject());
        validateSavedRefreshToken(member, refreshToken);

        String newAccessToken = jwtUtil.createToken(member.getUserId(), accessExpirationTime);
        return RefreshTokenResponse.of(newAccessToken);
    }

    private Member findMemberByUserId(String userId){
        return memberRepository.findByUserId(userId)
                .orElseThrow(() -> new DataNotFoundException(ErrorCode.MEMBER_NOT_FOUND));
    }

    public void validateSavedRefreshToken(Member member, String refreshToken){
        if(!member.isMatchRefreshToken(refreshToken)){
            throw new CustomJwtUnAuthorizedException(ErrorCode.MISMATCH_REFRESH_TOKEN);
        }
    }

    public FindIdResponse findId(FindIdRequest findIdRequest) {
        Member member = findMemberByEmail(findIdRequest.getEmail());
        return FindIdResponse.of(member.getUserId());
    }


    public void findPassword(FindPasswordRequest findPasswordRequest) throws MessagingException {
        boolean isExistsMember = memberRepository.existsByUserIdAndEmail(findPasswordRequest.getUserId(), findPasswordRequest.getEmail());
        if (!isExistsMember){
            throw new DataNotFoundException(ErrorCode.MEMBER_NOT_FOUND);
        }

        emailService.sendResetPasswordEmail(findPasswordRequest);
    }


    public void resetPassword(ResetPasswordRequest resetPasswordRequest) {
        String email = redisUtil.getData(resetPasswordRequest.getPasswordToken());

        if (email == null) {
            throw new ChangeMemberInfoException(ErrorCode.INVALID_CHANGE_PASSWORD);
        }

        Member member = findMemberByEmail(email);
        member.updatePassword(passwordEncoder.encode(resetPasswordRequest.getPassword()));
    }

    private Member findMemberByEmail(String email){
        return memberRepository.findByEmail(email)
                .orElseThrow(() -> new DataNotFoundException(ErrorCode.MEMBER_NOT_FOUND));
    }


    public void changePassword(String userId, ChangePasswordRequest passwordRequest){
        Member member = findMemberByUserId(userId);

        boolean isMatch = passwordEncoder.matches(passwordRequest.getNowPassword(), member.getPassword());
        if(!isMatch){
            throw new ChangeMemberInfoException(ErrorCode.INCORRECT_PASSWORD);
        }

        member.updatePassword(passwordEncoder.encode(passwordRequest.getNewPassword()));
    }


    public MemberInfoResponse getMemberInfo(String userId) {
        Member member = findMemberByUserId(userId);
        return MemberInfoResponse.from(member);
    }

    public void changeNickname(String userId, ChangeNicknameRequest changeNicknameRequest) {
        checkDuplicateNickname(changeNicknameRequest.getNickname());

        Member member = findMemberByUserId(userId);
        member.updateNickname(changeNicknameRequest.getNickname());
    }

    public void changeEmail(String userId, EmailRequest emailRequest) {
        checkDuplicateEmail(emailRequest.getEmail());
        validateVerifiedEmail(emailRequest.getEmail());

        Member member = findMemberByUserId(userId);
        member.updateEmail(emailRequest.getEmail());
    }
}
