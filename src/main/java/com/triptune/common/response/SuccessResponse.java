package com.triptune.common.response;

import lombok.*;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;

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
