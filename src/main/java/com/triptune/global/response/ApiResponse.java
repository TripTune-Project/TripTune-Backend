package com.triptune.global.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.triptune.global.message.SuccessCode;
import com.triptune.global.response.page.PageResponse;
import com.triptune.global.response.page.SchedulePageResponse;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
    private boolean success;
    private String message;
    private T data;

    private ApiResponse(String message) {
        this.success = true;
        this.message = message;
    }

    private ApiResponse(String message, T data){
        this.success = true;
        this.message = message;
        this.data = data;
    }

    public static <T> ApiResponse<T> okResponse(){
        return new ApiResponse<>(
                SuccessCode.GENERAL_SUCCESS.getMessage()
        );
    }

    public static <T> ApiResponse<T> dataResponse(T object){
        return new ApiResponse<>(
                SuccessCode.GENERAL_SUCCESS.getMessage(),
                object
        );
    }

    public static <T> ApiResponse<PageResponse<T>> pageResponse(Page<T> object) {
        return new ApiResponse<PageResponse<T>>(
                SuccessCode.GENERAL_SUCCESS.getMessage(),
                PageResponse.of(object)
        );
    }

    public static <T> ApiResponse<SchedulePageResponse<T>> schedulePageResponse(SchedulePageResponse<T> object) {
        return new ApiResponse<SchedulePageResponse<T>>(
                SuccessCode.GENERAL_SUCCESS.getMessage(),
                object
        );
    }

}
