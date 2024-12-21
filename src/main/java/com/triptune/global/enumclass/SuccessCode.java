package com.triptune.global.enumclass;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum SuccessCode {

    // 공통
    GENERAL_SUCCESS(HttpStatus.OK, "200(성공)");

    private final HttpStatus status;
    private final String message;
}
