package com.triptune.global.response;

import com.triptune.global.message.ErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ErrorDataResponse<T> {

    private boolean success;
    private int errorCode;
    private String message;
    private T data;


    private ErrorDataResponse(int errorCode, String message, T data) {
        this.success = false;
        this.errorCode = errorCode;
        this.message = message;
        this.data = data;
    }


    public static <T> ErrorDataResponse<T> of(ErrorCode errorCode, T data){
        return new ErrorDataResponse<>(
                errorCode.getStatus().value(),
                errorCode.getMessage(),
                data
        );
    }
}
