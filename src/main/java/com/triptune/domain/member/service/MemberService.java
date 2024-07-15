package com.triptune.domain.member.service;

import com.triptune.domain.email.dto.EmailDTO;
import com.triptune.domain.email.service.EmailService;
import com.triptune.domain.member.dto.LoginDTO;
import com.triptune.domain.member.dto.LogoutDTO;
import com.triptune.domain.member.dto.MemberDTO;
import com.triptune.domain.member.dto.TokenDTO;
import com.triptune.domain.member.entity.Member;
import com.triptune.domain.member.exception.CustomUsernameNotFoundException;
import com.triptune.domain.member.exception.DataExistException;
import com.triptune.domain.member.repository.MemberRepository;
import com.triptune.global.exception.CustomJwtException;
import com.triptune.global.exception.ErrorCode;
import com.triptune.global.util.JwtUtil;
import com.triptune.global.util.RedisUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
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

        if(memberRepository.existsByEmail(memberDTO.getEmail())){
            throw new DataExistException(ErrorCode.ALREADY_EXISTED_EMAIL);
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
        Member member = memberRepository.findByUserId(loginDTO.getUserId())
                .orElseThrow(() -> new CustomUsernameNotFoundException(ErrorCode.FAILED_LOGIN));

        if (!passwordEncoder.matches(loginDTO.getPassword(), member.getPassword())) {
            throw new CustomUsernameNotFoundException(ErrorCode.FAILED_LOGIN);
        }

        String accessToken = jwtUtil.createAccessToken(loginDTO.getUserId());
        String refreshToken = jwtUtil.createRefreshToken(loginDTO.getUserId());

        member.setRefreshToken(refreshToken);

        return LoginDTO.Response.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .userId(member.getUserId())
                .build();
    }


    public void logout(LogoutDTO logoutDTO, String accessToken) {
        memberRepository.deleteRefreshToken(logoutDTO.getUserId());
        redisUtil.setDataExpire(accessToken, "logout", 3600);
    }

    public TokenDTO refreshToken(TokenDTO tokenDTO) throws ExpiredJwtException {
        String refreshToken = tokenDTO.getRefreshToken();

        jwtUtil.validateToken(refreshToken);

        Claims claims = jwtUtil.parseClaims(refreshToken);

        Member member = memberRepository.findByUserId(claims.getSubject())
                .orElseThrow(() -> new CustomUsernameNotFoundException(ErrorCode.NOT_FOUND_USER));

        if(!refreshToken.equals(member.getRefreshToken())){
            throw new CustomJwtException(ErrorCode.MISMATCH_REFRESH_TOKEN);
        }

        String newAccessToken = jwtUtil.createAccessToken(member.getUserId());

        return TokenDTO.of(newAccessToken);
    }

    public MemberDTO.Response findId(EmailDTO.VerifyRequest emailDTO) {
        Member member = memberRepository.findByEmail(emailDTO.getEmail());

        if (member == null){
            throw new UsernameNotFoundException("가입정보가 존재하지 않습니다. 입력된 정보를 확인해주세요.");
        }

        return MemberDTO.Response.of(member.getUserId());
    }


}
