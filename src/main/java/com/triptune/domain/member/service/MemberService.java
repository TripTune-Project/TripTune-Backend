package com.triptune.domain.member.service;

import com.triptune.domain.email.service.EmailService;
import com.triptune.domain.member.dto.*;
import com.triptune.domain.member.entity.Member;
import com.triptune.domain.member.exception.CustomUsernameNotFoundException;
import com.triptune.domain.member.exception.DataExistException;
import com.triptune.domain.member.exception.ChangePasswordException;
import com.triptune.domain.member.repository.MemberRepository;
import com.triptune.global.exception.CustomJwtException;
import com.triptune.global.exception.ErrorCode;
import com.triptune.global.util.JwtUtil;
import com.triptune.global.util.RedisUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.mail.MessagingException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
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

    /**
     * 이메일 주소로 회원 정보 찾아서 아이디 반환
     * @param  findIdDTO
     * @return 사용자 정보 객체 {@link MemberDTO.Response}
     * @throws CustomUsernameNotFoundException 이메일로 회원 정보를 찾기 못한 경우
     */
    public MemberDTO.Response findId(FindDTO.FindId findIdDTO) {
        Member member = memberRepository.findByEmail(findIdDTO.getEmail())
                .orElseThrow(() -> new CustomUsernameNotFoundException(ErrorCode.NOT_FOUND_USER));

        return MemberDTO.Response.of(member.getUserId());
    }


    /**
     * 이메일, 아이디 정보를 통해 비밀번호 찾기 이메일 요청
     * @param findPasswordDTO
     * @throws MessagingException
     */
    public void findPassword(FindDTO.FindPassword findPasswordDTO) throws MessagingException {
        Member member = memberRepository.findByEmail(findPasswordDTO.getEmail())
                .orElseThrow(() -> new CustomUsernameNotFoundException(ErrorCode.NOT_FOUND_USER));

        if (!member.getUserId().equals(findPasswordDTO.getUserId())){
            throw new CustomUsernameNotFoundException(ErrorCode.NOT_FOUND_USER);
        }

        emailService.findPassword(findPasswordDTO);
    }


    public void changePassword(PasswordDTO passwordDTO) {
        String email = redisUtil.getData(passwordDTO.getPasswordToken());

        if (email == null) {
            throw new ChangePasswordException(ErrorCode.INVALID_CHANGE_PASSWORD);
        }

        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new CustomUsernameNotFoundException(ErrorCode.NOT_FOUND_USER));

        member.setPassword(passwordEncoder.encode(passwordDTO.getPassword()));
    }
}
