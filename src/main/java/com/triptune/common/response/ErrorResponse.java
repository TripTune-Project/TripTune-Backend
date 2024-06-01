package com.triptune.common.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ErrorResponse  {

    private boolean success;
    private int errorCode;
    private String message;

    @Builder
    public ErrorResponse(boolean success, int errorCode, String message) {
        this.success = success;
        this.errorCode = errorCode;
        this.message = message;
    }
}
