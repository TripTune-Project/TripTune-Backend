package com.triptune.global.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {

    // 입력 검증
    INCORRECT_PASSWORD_REPASSWORD(HttpStatus.BAD_REQUEST, "비밀번호와 재입력 비밀번호가 일치하지 않습니다."),
    ALREADY_EXISTED_USERID(HttpStatus.BAD_REQUEST, "이미 존재하는 아이디입니다."),
    ALREADY_EXISTED_NICKNAME(HttpStatus.BAD_REQUEST, "이미 존재하는 닉네임입니다."),
    
    // 이메일
    EMAIL_VERIFY_FAIL(HttpStatus.BAD_REQUEST, "이메일 인증에 실패했습니다."),

    // 토큰
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "토큰이 만료되었습니다."),
    FAILED_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "토큰 갱신에 실패했습니다.");

    private final HttpStatus status;
    private final String message;
}
