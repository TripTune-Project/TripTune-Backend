package com.triptune.domain.member.service;

import com.triptune.domain.email.service.EmailService;
import com.triptune.domain.member.dto.request.*;
import com.triptune.domain.member.dto.response.FindIdResponse;
import com.triptune.domain.member.dto.response.LoginResponse;
import com.triptune.domain.member.dto.response.MemberInfoResponse;
import com.triptune.domain.member.dto.response.RefreshTokenResponse;
import com.triptune.domain.member.entity.Member;
import com.triptune.domain.profile.entity.ProfileImage;
import com.triptune.domain.member.exception.ChangeMemberInfoException;
import com.triptune.domain.member.exception.FailLoginException;
import com.triptune.domain.member.repository.MemberRepository;
import com.triptune.domain.member.dto.request.ChangePasswordRequest;
import com.triptune.domain.profile.service.ProfileImageService;
import com.triptune.global.enumclass.ErrorCode;
import com.triptune.global.exception.CustomJwtBadRequestException;
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
        validateUniqueMemberInfo(joinRequest);

        ProfileImage profileImage = profileImageService.saveDefaultProfileImage();

        Member member = Member.from(joinRequest, passwordEncoder.encode(joinRequest.getPassword()), profileImage);
        memberRepository.save(member);
    }


    public void validateUniqueMemberInfo(JoinRequest joinRequest){
        if(isExistUserId(joinRequest.getUserId())){
            throw new DataExistException(ErrorCode.ALREADY_EXISTED_USERID);
        }

        if(isExistNickname(joinRequest.getNickname())){
            throw new DataExistException(ErrorCode.ALREADY_EXISTED_NICKNAME);
        }

        if(isExistEmail(joinRequest.getEmail())){
            throw new DataExistException(ErrorCode.ALREADY_EXISTED_EMAIL);
        }
    }

    public boolean isExistUserId(String userId){
        return memberRepository.existsByUserId(userId);
    }

    public boolean isExistNickname(String nickname){
        return memberRepository.existsByNickname(nickname);
    }

    public boolean isExistEmail(String email){
        return memberRepository.existsByEmail(email);
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

    public Member findMemberByUserId(String userId){
        return memberRepository.findByUserId(userId)
                .orElseThrow(() -> new DataNotFoundException(ErrorCode.MEMBER_NOT_FOUND));
    }

    public void validateSavedRefreshToken(Member member, String refreshToken){
        if(!member.isMatchRefreshToken(refreshToken)){
            throw new CustomJwtBadRequestException(ErrorCode.MISMATCH_REFRESH_TOKEN);
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

        emailService.findPassword(findPasswordRequest);
    }


    public void resetPassword(ResetPasswordRequest resetPasswordRequest) {
        String email = redisUtil.getData(resetPasswordRequest.getPasswordToken());

        if (email == null) {
            throw new ChangeMemberInfoException(ErrorCode.INVALID_CHANGE_PASSWORD);
        }

        Member member = findMemberByEmail(email);
        member.updatePassword(passwordEncoder.encode(resetPasswordRequest.getPassword()));
        member.updateUpdatedAt();

    }

    public Member findMemberByEmail(String email){
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
        member.updateUpdatedAt();
    }


    public MemberInfoResponse getMemberInfo(String userId) {
        Member member = findMemberByUserId(userId);
        return MemberInfoResponse.from(member);
    }

    public void changeNickname(String userId, ChangeNicknameRequest changeNicknameRequest) {
        Member member = findMemberByUserId(userId);

        if (isExistNickname(changeNicknameRequest.getNickname())){
            throw new DataExistException(ErrorCode.ALREADY_EXISTED_NICKNAME);
        }

        member.updateNickname(changeNicknameRequest.getNickname());
        member.updateUpdatedAt();
    }
}
