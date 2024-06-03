package com.triptune.member.controller;

import com.triptune.common.exception.ErrorCode;
import com.triptune.common.response.ApiResponse;
import com.triptune.member.dto.MemberDTO;
import com.triptune.member.exception.IncorrectPasswordException;
import com.triptune.member.service.MemberService;
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
    @Operation(summary = "회원가입(미완)", description = "회원가입을 요청합니다.")
    public ApiResponse<?> join(@Valid @RequestBody MemberDTO.Request memberDTO){
        if(!memberDTO.getPassword().equals(memberDTO.getRepassword())){
            throw new IncorrectPasswordException(ErrorCode.INCORRECT_PASSWORD_REPASSWORD);
        }

        memberService.join(memberDTO);

        return ApiResponse.okResponse();
    }
}
