package com.triptune.domain.mypage.controller;

import com.triptune.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/mypage")
@Tag(name = "MyPage", description = "마이페이지 관련 API")
public class MyPageController {

    @GetMapping
    @Operation(summary = "(미완)마이페이지 조회", description = "마이페이지를 조회합니다.")
    public ApiResponse<?> getMyPage(){
        return ApiResponse.okResponse();
    }
}
