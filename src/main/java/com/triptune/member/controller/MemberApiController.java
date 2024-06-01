package com.triptune.member.controller;

import com.triptune.common.response.ApiResponse;
import com.triptune.common.response.ErrorResponse;
import com.triptune.member.dto.MemberDTO;
import com.triptune.member.service.MemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/members")
public class MemberApiController {

    private final MemberService memberService;


    @PostMapping("/join")
    public ApiResponse<MemberDTO.Response> join(@Valid @RequestBody MemberDTO.Request memberDTO){
        memberService.join(memberDTO);

        MemberDTO.Response response = new MemberDTO.Response();
        return ApiResponse.dataResponse(response);
    }
}
