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
    public ErrorResponse(int errorCode, String message) {
        this.success = false;
        this.errorCode = errorCode;
        this.message = message;
    }
}
