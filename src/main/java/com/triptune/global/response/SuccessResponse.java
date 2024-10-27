package com.triptune.global.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.triptune.global.enumclass.SuccessCode;
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

    public static <T> SuccessResponse<T> of(){
        return SuccessResponse.<T>builder()
                .message(SuccessCode.GENERAL_SUCCESS.getMessage())
                .build();
    }

    public static <T> SuccessResponse<T> of(T object){
        return SuccessResponse.<T>builder()
                .data(object)
                .message(SuccessCode.GENERAL_SUCCESS.getMessage())
                .build();
    }



}
