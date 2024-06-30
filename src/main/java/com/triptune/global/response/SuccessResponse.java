package com.triptune.global.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL) // null 값 응답에 추가 안하기 위해 사용
public class SuccessResponse<T> {
    private boolean success;
    private String message;
    private T data;

    @Builder
    public SuccessResponse(String message, T data){
        this.success = true;
        this.message = message;
        this.data = data;
    }

}
