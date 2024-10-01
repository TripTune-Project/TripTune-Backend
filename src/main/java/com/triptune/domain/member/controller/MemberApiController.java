package com.triptune.domain.member.controller;

import com.triptune.domain.member.dto.*;
import com.triptune.global.enumclass.ErrorCode;
import com.triptune.global.exception.CustomJwtBadRequestException;
import com.triptune.global.exception.CustomNotValidException;
import com.triptune.global.response.ApiResponse;
import com.triptune.domain.member.exception.FailLoginException;
import com.triptune.domain.member.service.MemberService;
import com.triptune.global.service.CustomUserDetails;
import com.triptune.global.util.JwtUtil;
import io.jsonwebtoken.ExpiredJwtException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/members")
@Tag(name = "Member", description = "회원 관련 API")
public class MemberApiController {

    private final MemberService memberService;
    private final JwtUtil jwtUtil;


    @PostMapping("/join")
    @Operation(summary = "회원가입", description = "회원가입을 요청합니다.")
    public ApiResponse<?> join(@Valid @RequestBody MemberRequest memberRequest){
        if(!memberRequest.getPassword().equals(memberRequest.getRepassword())){
            throw new CustomNotValidException(ErrorCode.INCORRECT_PASSWORD_REPASSWORD);
        }

        memberService.join(memberRequest);

        return ApiResponse.okResponse();
    }

    @PostMapping("/login")
    @Operation(summary = "로그인", description = "로그인을 실행합니다.")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest loginRequest){
        LoginResponse response = memberService.login(loginRequest);
        return ApiResponse.dataResponse(response);
    }


    @PatchMapping("/logout")
    @Operation(summary = "로그아웃", description = "로그아웃을 실행합니다.")
    public ApiResponse<?> logout(HttpServletRequest request, @RequestBody LogoutDTO logoutDTO){
        String accessToken = jwtUtil.resolveToken(request);

        if (accessToken == null){
            throw new CustomJwtBadRequestException(ErrorCode.INVALID_JWT_TOKEN);
        }

        memberService.logout(logoutDTO, accessToken);
        return ApiResponse.okResponse();
    }


    @PostMapping("/refresh")
    @Operation(summary = "토큰 갱신", description = "Refresh Token 을 이용해 만료된 Access Token을 갱신합니다.")
    public ApiResponse<RefreshTokenResponse> refreshToken(@RequestBody RefreshTokenRequest refreshTokenRequest) throws ExpiredJwtException {
        RefreshTokenResponse response = memberService.refreshToken(refreshTokenRequest);
        return ApiResponse.dataResponse(response);
    }

    @PostMapping("/find-id")
    @Operation(summary = "아이디 찾기", description = "아이디 찾기를 실행합니다.")
    public ApiResponse<FindIdResponse> findId(@RequestBody FindIdRequest findIdRequest) {
        FindIdResponse response = memberService.findId(findIdRequest);
        return ApiResponse.dataResponse(response);
    }

    @PostMapping("/find-password")
    @Operation(summary = "비밀번호 찾기", description = "비밀번호 찾기를 요청합니다. 비밀번호 변경 화면으로 연결되는 링크가 이메일을 통해서 제공됩니다.")
    public ApiResponse<?> findPassword(@RequestBody FindPasswordDTO findPasswordDTO) throws MessagingException {
        memberService.findPassword(findPasswordDTO);
        return ApiResponse.okResponse();
    }

    @PatchMapping("/change-password")
    @Operation(summary = "비밀번호 변경", description = "비밀번호를 변경합니다.")
    public ApiResponse<?> changePassword(@RequestBody ChangePasswordDTO changePasswordDTO){
        if(!changePasswordDTO.getPassword().equals(changePasswordDTO.getRepassword())){
            throw new FailLoginException(ErrorCode.INCORRECT_PASSWORD_REPASSWORD);
        }

        memberService.changePassword(changePasswordDTO);
        return ApiResponse.okResponse();
    }

    @GetMapping("/test")
    public String test(@AuthenticationPrincipal CustomUserDetails userDetails){
        System.out.println(">>>>>>>>>>" + userDetails.getUsername());
        return "hello";
    }


}
