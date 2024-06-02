package com.triptune.member.controller;

import com.triptune.common.response.ApiResponse;
import com.triptune.common.response.ErrorResponse;
import com.triptune.member.dto.MemberDTO;
import com.triptune.member.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/members")
@Tag(name = "Member", description = "회원 관련 API")
public class MemberApiController {

    private final MemberService memberService;


    @PostMapping("/join")
    @Operation(summary = "회원가입(미완)", description = "회원가입을 요청합니다.")
    public ApiResponse<MemberDTO.Response> join(@Valid @RequestBody MemberDTO.Request memberDTO){
        memberService.join(memberDTO);

        MemberDTO.Response response = new MemberDTO.Response();
        return ApiResponse.dataResponse(response);
    }
}
