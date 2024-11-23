package com.triptune.domain.member.service;

import com.triptune.domain.email.service.EmailService;
import com.triptune.domain.member.dto.*;
import com.triptune.domain.member.dto.request.FindIdRequest;
import com.triptune.domain.member.dto.request.LoginRequest;
import com.triptune.domain.member.dto.request.MemberRequest;
import com.triptune.domain.member.dto.request.RefreshTokenRequest;
import com.triptune.domain.member.dto.response.FindIdResponse;
import com.triptune.domain.member.dto.response.LoginResponse;
import com.triptune.domain.member.dto.response.MemberResponse;
import com.triptune.domain.member.dto.response.RefreshTokenResponse;
import com.triptune.domain.member.entity.Member;
import com.triptune.domain.member.exception.ChangePasswordException;
import com.triptune.domain.member.exception.FailLoginException;
import com.triptune.domain.member.repository.MemberRepository;
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

    @Value("${spring.jwt.token.access-expiration-time}")
    private long accessExpirationTime;

    @Value("${spring.jwt.token.refresh-expiration-time}")
    private long refreshExpirationTime;


    public void join(MemberRequest memberRequest) {
        validateUniqueMemberInfo(memberRequest);
        Member member = Member.from(memberRequest, passwordEncoder.encode(memberRequest.getPassword()));

        memberRepository.save(member);
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

        member.setRefreshToken(refreshToken);

        return LoginResponse.of(accessToken, refreshToken, member.getNickname());
    }


    public void logout(LogoutDTO logoutDTO, String accessToken) {
        boolean isMember = memberRepository.existsByNickname(logoutDTO.getNickname());

        if (!isMember){
            throw new DataNotFoundException(ErrorCode.USER_NOT_FOUND);
        }

        memberRepository.deleteRefreshTokenByNickname(logoutDTO.getNickname());
        redisUtil.saveExpiredData(accessToken, "logout", LOGOUT_DURATION);
    }


    public RefreshTokenResponse refreshToken(RefreshTokenRequest refreshTokenRequest) throws ExpiredJwtException {
        String refreshToken = refreshTokenRequest.getRefreshToken();
        jwtUtil.validateToken(refreshToken);

        Claims claims = jwtUtil.parseClaims(refreshToken);
        Member member = memberRepository.findByUserId(claims.getSubject())
                .orElseThrow(() -> new DataNotFoundException(ErrorCode.USER_NOT_FOUND));

        if(!refreshToken.equals(member.getRefreshToken())){
            throw new CustomJwtBadRequestException(ErrorCode.MISMATCH_REFRESH_TOKEN);
        }

        String newAccessToken = jwtUtil.createToken(member.getUserId(), accessExpirationTime);

        return RefreshTokenResponse.of(newAccessToken);
    }


    public FindIdResponse findId(FindIdRequest findIdRequest) {
        Member member = getMemberByEmail(findIdRequest.getEmail());
        return FindIdResponse.of(member.getUserId());
    }


    public void findPassword(FindPasswordDTO findPasswordDTO) throws MessagingException {
        Member member = getMemberByEmail(findPasswordDTO.getEmail());

        if (!member.getUserId().equals(findPasswordDTO.getUserId())){
            throw new DataNotFoundException(ErrorCode.USER_NOT_FOUND);
        }

        emailService.findPassword(findPasswordDTO);
    }


    public void changePassword(ChangePasswordDTO changePasswordDTO) {
        String email = redisUtil.getData(changePasswordDTO.getPasswordToken());

        if (email == null) {
            throw new ChangePasswordException(ErrorCode.INVALID_CHANGE_PASSWORD);
        }

        Member member = getMemberByEmail(email);

        member.setPassword(passwordEncoder.encode(changePasswordDTO.getPassword()));
    }


    public void validateUniqueMemberInfo(MemberRequest memberRequest){
        if(memberRepository.existsByUserId(memberRequest.getUserId())){
            throw new DataExistException(ErrorCode.ALREADY_EXISTED_USERID);
        }

        if(memberRepository.existsByNickname(memberRequest.getNickname())){
            throw new DataExistException(ErrorCode.ALREADY_EXISTED_NICKNAME);
        }

        if(memberRepository.existsByEmail(memberRequest.getEmail())){
            throw new DataExistException(ErrorCode.ALREADY_EXISTED_EMAIL);
        }
    }


    public Member getMemberByEmail(String email){
        return memberRepository.findByEmail(email)
                .orElseThrow(() -> new DataNotFoundException(ErrorCode.USER_NOT_FOUND));
    }



}
