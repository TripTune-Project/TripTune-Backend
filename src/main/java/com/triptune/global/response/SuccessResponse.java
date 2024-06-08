package com.triptune.global.response;

import lombok.*;

@Getter
@Setter
public class SuccessResponse<T> {
    private boolean success;
    private String message;
    private T data;

    @Builder
    public SuccessResponse(boolean success, String message, T data){
        this.success = success;
        this.message = message;
        this.data = data;
    }

}
