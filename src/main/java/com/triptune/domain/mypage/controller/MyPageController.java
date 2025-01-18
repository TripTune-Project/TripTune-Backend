package com.triptune.domain.mypage.controller;

import com.triptune.domain.member.exception.ChangePasswordException;
import com.triptune.domain.member.exception.FailLoginException;
import com.triptune.domain.mypage.dto.request.MyPagePasswordRequest;
import com.triptune.domain.mypage.service.MyPageService;
import com.triptune.global.enumclass.ErrorCode;
import com.triptune.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/mypage")
@RequiredArgsConstructor
@Tag(name = "MyPage", description = "마이페이지 관련 API")
public class MyPageController {

    private final MyPageService myPageService;

    @GetMapping
    @Operation(summary = "(미완)마이페이지 조회", description = "마이페이지를 조회합니다.")
    public ApiResponse<?> getMyPage(){
        return ApiResponse.okResponse();
    }

    @PatchMapping("/account/change-password")
    @Operation(summary = "비밀번호 변경", description = "비밀번호를 변경합니다.")
    public ApiResponse<?> changePassword(@Valid @RequestBody MyPagePasswordRequest passwordRequest){
        if(!passwordRequest.isMatchNewPassword()){
            throw new ChangePasswordException(ErrorCode.INCORRECT_PASSWORD_REPASSWORD);
        }

        if(passwordRequest.isMatchNowPassword()){
            throw new ChangePasswordException(ErrorCode.CORRECT_NOWPASSWORD_NEWPASSWORD);
        }

        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        myPageService.changePassword(userId, passwordRequest);

        return ApiResponse.okResponse();
    }
}
