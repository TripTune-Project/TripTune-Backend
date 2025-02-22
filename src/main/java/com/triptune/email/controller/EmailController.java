package com.triptune.email.controller;

import com.triptune.email.dto.VerifyAuthRequest;
import com.triptune.email.dto.EmailRequest;
import com.triptune.member.service.MemberService;
import com.triptune.email.exception.EmailVerifyException;
import com.triptune.email.service.EmailService;
import com.triptune.global.enumclass.ErrorCode;
import com.triptune.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/emails")
@RequiredArgsConstructor
@Tag(name = "Email", description = "이메일 관련 API")
public class EmailController {

    private final MemberService memberService;
    private final EmailService emailService;

    @PostMapping("/verify-request")
    @Operation(summary = "이메일 인증 요청", description = "이메일 인증을 요청합니다.")
    public ApiResponse<?> verifyRequest(@Valid @RequestBody EmailRequest emailRequest) throws MessagingException {
        memberService.checkDuplicateEmail(emailRequest.getEmail());
        emailService.sendCertificationEmail(emailRequest.getEmail());

        return ApiResponse.okResponse();
    }

    @PostMapping("/verify")
    @Operation(summary = "이메일 인증 번호 검증", description = "발급된 이메일 인증 번호를 검증합니다.")
    public ApiResponse<?> verify(@Valid @RequestBody VerifyAuthRequest verifyAuthRequest) {
        boolean isVerify = emailService.verifyAuthCode(verifyAuthRequest);

        if (!isVerify){
            throw new EmailVerifyException(ErrorCode.FAIL_VERIFIED_EMAIL);
        }

        return ApiResponse.okResponse();
    }
}
