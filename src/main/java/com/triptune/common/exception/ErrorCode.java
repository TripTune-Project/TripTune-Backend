package com.triptune.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {

    // 입력 검증
    INCORRECT_PASSWORD(HttpStatus.BAD_REQUEST, "비밀번호와 재입력 비밀번호가 일치하지 않습니다."),
    
    // 이메일
    EMAIL_VERIFY_FAIL(HttpStatus.BAD_REQUEST, "이메일 인증에 실패했습니다.");

    private final HttpStatus status;
    private final String message;
}
