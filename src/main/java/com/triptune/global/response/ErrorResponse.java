package com.triptune.global.response;

import com.triptune.global.response.enums.ErrorCode;
import lombok.Builder;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ErrorResponse  {

    private boolean success;
    private int errorCode;
    private String message;

    @Builder
    public ErrorResponse(int errorCode, String message) {
        this.success = false;
        this.errorCode = errorCode;
        this.message = message;
    }

    public static ErrorResponse of(ErrorCode errorCode){
        return new ErrorResponse(errorCode.getStatus().value(), errorCode.getMessage());
    }

    public static ErrorResponse of(HttpStatus httpStatus, String message){
        return new ErrorResponse(httpStatus.value(), message);
    }
}
