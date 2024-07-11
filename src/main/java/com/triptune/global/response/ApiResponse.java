package com.triptune.global.response;

import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;

@Getter
@Setter
public class ApiResponse<T> extends ResponseEntity<SuccessResponse<T>> {

    public static <T> ApiResponse<T> okResponse(){
        SuccessResponse<T> successResponse = SuccessResponse.<T>builder()
                .message("200(성공)")
                .build();

        return new ApiResponse<>(successResponse, HttpStatus.OK);
    }

    public static <T> ApiResponse<T> okResponse(String message){
        SuccessResponse<T> successResponse = SuccessResponse.<T>builder()
                .message(message)
                .build();

        return new ApiResponse<>(successResponse, HttpStatus.OK);
    }

    public static <T> ApiResponse<T> dataResponse(T body){
        SuccessResponse<T> successResponse = SuccessResponse.<T>builder()
                .message("200(성공)")
                .data(body)
                .build();

        return new ApiResponse<>(successResponse, HttpStatus.OK);
    }



    public ApiResponse(HttpStatus status) {
        super(status);
    }

    public ApiResponse(SuccessResponse<T> body, HttpStatus status) {
        super(body, status);
    }

    public ApiResponse(MultiValueMap<String, String> headers, HttpStatus status) {
        super(headers, status);
    }

    public ApiResponse(SuccessResponse<T> body, MultiValueMap<String, String> headers, HttpStatus status) {
        super(body, headers, status);
    }

    public ApiResponse(SuccessResponse<T> body, MultiValueMap<String, String> headers, int rawStatus) {
        super(body, headers, rawStatus);
    }
}
