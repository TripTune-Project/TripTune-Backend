package com.triptune.global.response;

import com.triptune.global.message.SuccessCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@Getter
public class ApiResponse<T> extends ResponseEntity<SuccessResponse<T>> {
    public static <T> ApiResponse<T> okResponse(){
        return new ApiResponse<>(SuccessResponse.of(), SuccessCode.GENERAL_SUCCESS.getStatus());
    }

    public static <T> ApiResponse<T> dataResponse(T body){
        return new ApiResponse<>(SuccessResponse.of(body), SuccessCode.GENERAL_SUCCESS.getStatus());
    }

    public ApiResponse(SuccessResponse<T> body, HttpStatus status) {
        super(body, status);
    }

}
