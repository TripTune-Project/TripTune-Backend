package com.triptune.domain.member.service;

import com.triptune.domain.email.dto.EmailDTO;
import com.triptune.domain.email.service.EmailService;
import com.triptune.domain.member.dto.LoginDTO;
import com.triptune.domain.member.dto.MemberDTO;
import com.triptune.domain.member.dto.TokenDTO;
import com.triptune.domain.member.entity.Member;
import com.triptune.domain.member.exception.DataExistException;
import com.triptune.domain.member.exception.RefreshTokenException;
import com.triptune.domain.member.repository.MemberRepository;
import com.triptune.global.exception.ErrorCode;
import com.triptune.global.service.CustomUserDetails;
import com.triptune.global.util.JwtUtil;
import com.triptune.global.util.RedisUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.mail.MessagingException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class MemberService {

    private final MemberRepository memberRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final RedisUtil redisUtil;

    public void join(MemberDTO.Request memberDTO) {

        if(memberRepository.existsByUserId(memberDTO.getUserId())){
            throw new DataExistException(ErrorCode.ALREADY_EXISTED_USERID);
        }

        if(memberRepository.existsByNickname(memberDTO.getNickname())){
            throw new DataExistException(ErrorCode.ALREADY_EXISTED_NICKNAME);
        }

        Member member = Member.builder()
                .userId(memberDTO.getUserId())
                .password(passwordEncoder.encode(memberDTO.getPassword()))
                .nickname(memberDTO.getNickname())
                .email(memberDTO.getEmail())
                .isSocialLogin(false)
                .createdAt(LocalDateTime.now())
                .build();

        memberRepository.save(member);
    }

    public LoginDTO.Response login(LoginDTO.Request loginDTO) {
        Member member = memberRepository.findByUserId(loginDTO.getUserId());

        if (member == null || !passwordEncoder.matches(loginDTO.getPassword(), member.getPassword())) {
            throw new UsernameNotFoundException("아이디 또는 비밀번호가 일치하지 않습니다.");
        }

        String accessToken = jwtUtil.createAccessToken(loginDTO.getUserId());
        String refreshToken = jwtUtil.createRefreshToken(loginDTO.getUserId());

        member.setRefreshToken(refreshToken);

        return LoginDTO.Response.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }


    public void logout(CustomUserDetails userDetails, String accessToken) {
        memberRepository.deleteRefreshToken(userDetails.getUsername());

        Long expiration = jwtUtil.getExpiration(accessToken);

        if (expiration > 0) {
            redisUtil.setDataExpire(accessToken, "logout", expiration);
        }

    }

    public TokenDTO.RefreshResponse refreshToken(TokenDTO.Request tokenDTO) throws ExpiredJwtException {
        String refreshToken = tokenDTO.getRefreshToken();

        jwtUtil.validateToken(refreshToken);
        Claims claims = jwtUtil.parseClaims(refreshToken);

        Member member = memberRepository.findByUserId(claims.getSubject());

        if(!refreshToken.equals(member.getRefreshToken())){
            throw new RefreshTokenException(ErrorCode.FAILED_REFRESH_TOKEN);
        }

        String newAccessToken = jwtUtil.createAccessToken(member.getUserId());

        return TokenDTO.RefreshResponse.builder()
                .accessToken(newAccessToken)
                .build();
    }

    public void findId(EmailDTO.VerifyRequest emailDTO) throws MessagingException {
        Member member = memberRepository.findByEmail(emailDTO.getEmail());

        if (member == null){
            throw new UsernameNotFoundException("가입정보가 존재하지 않습니다. 입력된 정보를 확인해주세요.");
        }

        emailService.findId(member.getUserId(), member.getEmail());

    }


}
