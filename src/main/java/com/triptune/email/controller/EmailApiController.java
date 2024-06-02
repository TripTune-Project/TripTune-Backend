package com.triptune.email.controller;

import com.triptune.common.exception.ErrorCode;
import com.triptune.common.response.ApiResponse;
import com.triptune.email.dto.EmailDTO;
import com.triptune.email.exception.EmailVerifyException;
import com.triptune.email.service.EmailService;
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
    public ApiResponse<?> verifyRequest(@Valid @RequestBody EmailDTO.VerifyRequest emailDTO) throws MessagingException {
        emailService.verifyRequest(emailDTO.getEmail());

        return ApiResponse.okResponse();
    }

    @PostMapping("/verify")
    public ApiResponse<?> verify(@Valid @RequestBody EmailDTO.Verify emailDTO) {
        boolean isVerify = emailService.verify(emailDTO);

        if (!isVerify){
            throw new EmailVerifyException(ErrorCode.EMAIL_VERIFY_FAIL);
        }

        return ApiResponse.okResponse();
    }
}
