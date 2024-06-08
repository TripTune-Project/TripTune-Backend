package com.triptune.domain.member.controller;

import com.triptune.global.exception.ErrorCode;
import com.triptune.global.response.ApiResponse;
import com.triptune.domain.member.dto.MemberDTO;
import com.triptune.domain.member.exception.IncorrectPasswordException;
import com.triptune.domain.member.service.MemberService;
import com.triptune.domain.member.dto.LoginDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/members")
@Tag(name = "Member", description = "회원 관련 API")
public class MemberApiController {

    private final MemberService memberService;


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
    @Operation(summary = "로그인(미완)", description = "회원 로그인")
    public ApiResponse<?> login(@Valid @RequestBody LoginDTO.Request loginDTO){
        LoginDTO.Response response = memberService.login(loginDTO);
        return ApiResponse.dataResponse(response);
    }
}
