package com.triptune.domain.email.controller;

import com.triptune.global.exception.ErrorCode;
import com.triptune.global.response.ApiResponse;
import com.triptune.domain.email.dto.EmailDTO;
import com.triptune.domain.email.exception.EmailVerifyException;
import com.triptune.domain.email.service.EmailService;
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
public class EmailApiController {

    private final EmailService emailService;

    @PostMapping("/verify-request")
    @Operation(summary = "이메일 인증 요청", description = "이메일 인증을 요청합니다.")
    public ApiResponse<?> verifyRequest(@Valid @RequestBody EmailDTO.VerifyRequest emailDTO) throws MessagingException {
        emailService.verifyRequest(emailDTO.getEmail());

        return ApiResponse.okResponse();
    }

    @PostMapping("/verify")
    @Operation(summary = "이메일 인증 번호 검증", description = "발급된 이메일 인증 번호를 검증합니다.")
    public ApiResponse<?> verify(@Valid @RequestBody EmailDTO.Verify emailDTO) {
        boolean isVerify = emailService.verify(emailDTO);

        if (!isVerify){
            throw new EmailVerifyException(ErrorCode.EMAIL_VERIFY_FAIL);
        }

        return ApiResponse.okResponse();
    }
}
