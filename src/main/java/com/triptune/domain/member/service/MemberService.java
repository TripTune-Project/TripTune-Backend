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

    /**
     * 회원가입
     * @param memberRequest : 가입자 정보 들어있는 dto
     */
    public void join(MemberRequest memberRequest) {
        validateUniqueMemberInfo(memberRequest);
        Member member = Member.from(memberRequest, passwordEncoder.encode(memberRequest.getPassword()));

        memberRepository.save(member);
    }

    /**
     * 로그인
     * @param loginRequest : 로그인 정보 들어있는 dto
     * @return LoginResponse : 토큰 정보가 들어있는 dto
     */
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


    /**
     * 로그아웃
     * @param logoutDTO : 로그아웃을 요청한 사용자 아이디 들어있는 dto
     * @param accessToken : 로그아웃을 요청한 사용자의 access token 데이터
     */
    public void logout(LogoutDTO logoutDTO, String accessToken) {
        boolean isMember = memberRepository.existsByNickname(logoutDTO.getNickname());

        if (!isMember){
            throw new DataNotFoundException(ErrorCode.USER_NOT_FOUND);
        }

        memberRepository.deleteRefreshTokenByNickname(logoutDTO.getNickname());
        redisUtil.saveExpiredData(accessToken, "logout", LOGOUT_DURATION);
    }

    /**
     * 토큰 갱신
     * @param refreshTokenRequest : 갱신에 필요한 refresh token 담긴 dto
     * @return RefreshTokenResponse : 갱신된 access token 담긴 dto
     * @throws ExpiredJwtException : jwt 만료 예외
     */
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

    /**
     * 이메일 주소로 회원 정보 찾아서 아이디 반환
     * @param  findIdRequest : 아이디 찾기 위한 사용자 정보 담긴 dto
     * @return 사용자 정보 객체 {@link MemberResponse}
     * @throws DataNotFoundException : 회원 정보 찾지 못해 예외 발생
     */
    public FindIdResponse findId(FindIdRequest findIdRequest) {
        Member member = getMemberByEmail(findIdRequest.getEmail());
        return FindIdResponse.of(member.getUserId());
    }


    /**
     * 이메일, 아이디 정보를 통해 비밀번호 찾기 이메일 요청
     * @param findPasswordDTO : 비밀번호 찾기 위한 사용자 정보 담긴 dto
     * @throws MessagingException : 이메일 요청 실패 예외
     */
    public void findPassword(FindPasswordDTO findPasswordDTO) throws MessagingException {
        Member member = getMemberByEmail(findPasswordDTO.getEmail());

        if (!member.getUserId().equals(findPasswordDTO.getUserId())){
            throw new DataNotFoundException(ErrorCode.USER_NOT_FOUND);
        }

        emailService.findPassword(findPasswordDTO);
    }

    /**
     * 비밀번호 변경
     * @param changePasswordDTO : 변경할 비밀번호 정보 담긴 dto
     */
    public void changePassword(ChangePasswordDTO changePasswordDTO) {
        String email = redisUtil.getData(changePasswordDTO.getPasswordToken());

        if (email == null) {
            throw new ChangePasswordException(ErrorCode.INVALID_CHANGE_PASSWORD);
        }

        Member member = getMemberByEmail(email);

        member.setPassword(passwordEncoder.encode(changePasswordDTO.getPassword()));
    }

    /**
     * 중복된 사용자 정보 체크
     * @param memberRequest : 사용자 정보 담긴 dto
     */
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

    /**
     * 이메일을 이용해 저장된 사용자 정보 조회
     * @param email : 사용자 이메일
     * @return Member :  사용자 객체
     */
    public Member getMemberByEmail(String email){
        return memberRepository.findByEmail(email)
                .orElseThrow(() -> new DataNotFoundException(ErrorCode.USER_NOT_FOUND));
    }



}
