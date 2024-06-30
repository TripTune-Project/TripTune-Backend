package com.triptune.domain.member.controller;

import com.triptune.domain.email.dto.EmailDTO;
import com.triptune.domain.member.dto.TokenDTO;
import com.triptune.global.exception.ErrorCode;
import com.triptune.global.response.ApiResponse;
import com.triptune.domain.member.dto.MemberDTO;
import com.triptune.domain.member.exception.IncorrectPasswordException;
import com.triptune.domain.member.service.MemberService;
import com.triptune.domain.member.dto.LoginDTO;
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
import org.springframework.web.servlet.support.JstlUtils;

import javax.security.auth.RefreshFailedException;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/members")
@Tag(name = "Member", description = "회원 관련 API")
public class MemberApiController {

    private final MemberService memberService;
    private final JwtUtil jwtUtil;


    @PostMapping("/join")
    @Operation(summary = "회원가입", description = "회원가입을 요청합니다.")
    public ApiResponse<?> join(@Valid @RequestBody MemberDTO.Request memberDTO){
        if(!memberDTO.getPassword().equals(memberDTO.getRepassword())){
            throw new IncorrectPasswordException(ErrorCode.INCORRECT_PASSWORD_REPASSWORD);
        }

        memberService.join(memberDTO);

        return ApiResponse.okResponse();
    }

    @PostMapping("/login")
    @Operation(summary = "로그인", description = "로그인을 실행합니다.")
    public ApiResponse<LoginDTO.Response> login(@Valid @RequestBody LoginDTO.Request loginDTO){
        LoginDTO.Response response = memberService.login(loginDTO);
        return ApiResponse.dataResponse(response);
    }


    @PatchMapping("/logout")
    @Operation(summary = "로그아웃", description = "로그아웃을 실행합니다.")
    public ApiResponse<?> logout(@AuthenticationPrincipal CustomUserDetails userDetails){
        memberService.logout(userDetails);

        return ApiResponse.okResponse();
    }


    @PostMapping("/refresh")
    @Operation(summary = "토큰 갱신", description = "Refresh Token 을 이용해 만료된 Access Token을 갱신합니다.")
    public ApiResponse<TokenDTO.RefreshResponse> refreshToken(@RequestBody TokenDTO.Request tokenDTO) throws ExpiredJwtException {
        TokenDTO.RefreshResponse response = memberService.refreshToken(tokenDTO);
        return ApiResponse.dataResponse(response);
    }



    @GetMapping("/test")
    public String test(@AuthenticationPrincipal CustomUserDetails userDetails){
        System.out.println(">>>>>>>>>>" + userDetails.getUsername());
        return "hello";
    }


}
