package com.triptune.domain.member.service;

import com.triptune.domain.email.service.EmailService;
import com.triptune.domain.member.dto.*;
import com.triptune.domain.member.entity.Member;
import com.triptune.domain.member.exception.CustomUsernameNotFoundException;
import com.triptune.domain.member.exception.DataExistException;
import com.triptune.domain.member.exception.ChangePasswordException;
import com.triptune.domain.member.exception.FailLoginException;
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

    public void join(MemberRequest memberRequest) {

        if(memberRepository.existsByUserId(memberRequest.getUserId())){
            throw new DataExistException(ErrorCode.ALREADY_EXISTED_USERID);
        }

        if(memberRepository.existsByNickname(memberRequest.getNickname())){
            throw new DataExistException(ErrorCode.ALREADY_EXISTED_NICKNAME);
        }

        if(memberRepository.existsByEmail(memberRequest.getEmail())){
            throw new DataExistException(ErrorCode.ALREADY_EXISTED_EMAIL);
        }

        Member member = Member.builder()
                .userId(memberRequest.getUserId())
                .password(passwordEncoder.encode(memberRequest.getPassword()))
                .nickname(memberRequest.getNickname())
                .email(memberRequest.getEmail())
                .isSocialLogin(false)
                .createdAt(LocalDateTime.now())
                .build();

        memberRepository.save(member);
    }

    public LoginResponse login(LoginRequest loginRequest) {
        Member member = memberRepository.findByUserId(loginRequest.getUserId())
                .orElseThrow(() -> new FailLoginException(ErrorCode.FAILED_LOGIN));

        if (!passwordEncoder.matches(loginRequest.getPassword(), member.getPassword())) {
            throw new FailLoginException(ErrorCode.FAILED_LOGIN);
        }

        String accessToken = jwtUtil.createAccessToken(loginRequest.getUserId());
        String refreshToken = jwtUtil.createRefreshToken(loginRequest.getUserId());

        member.setRefreshToken(refreshToken);

        return LoginResponse.builder()
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
     * @param  findIdRequest
     * @return 사용자 정보 객체 {@link MemberResponse}
     * @throws CustomUsernameNotFoundException 이메일로 회원 정보를 찾기 못한 경우
     */
    public FindIdResponse findId(FindIdRequest findIdRequest) {
        Member member = memberRepository.findByEmail(findIdRequest.getEmail())
                .orElseThrow(() -> new CustomUsernameNotFoundException(ErrorCode.NOT_FOUND_USER));

        return FindIdResponse.builder()
                .userId(member.getUserId())
                .build();
    }


    /**
     * 이메일, 아이디 정보를 통해 비밀번호 찾기 이메일 요청
     * @param findPasswordDTO
     * @throws MessagingException
     */
    public void findPassword(FindPasswordDTO findPasswordDTO) throws MessagingException {
        Member member = memberRepository.findByEmail(findPasswordDTO.getEmail())
                .orElseThrow(() -> new CustomUsernameNotFoundException(ErrorCode.NOT_FOUND_USER));

        if (!member.getUserId().equals(findPasswordDTO.getUserId())){
            throw new CustomUsernameNotFoundException(ErrorCode.NOT_FOUND_USER);
        }

        emailService.findPassword(findPasswordDTO);
    }


    public void changePassword(ChangePasswordDTO changePasswordDTO) {
        String email = redisUtil.getData(changePasswordDTO.getPasswordToken());

        if (email == null) {
            throw new ChangePasswordException(ErrorCode.INVALID_CHANGE_PASSWORD);
        }

        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new CustomUsernameNotFoundException(ErrorCode.NOT_FOUND_USER));

        member.setPassword(passwordEncoder.encode(changePasswordDTO.getPassword()));
    }
}
