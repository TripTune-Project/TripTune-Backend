package com.triptune.global.response.pagination;

import com.triptune.global.enumclass.SuccessCode;
import com.triptune.global.response.SuccessResponse;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;

public class ApiPageResponse<T> extends ResponseEntity<SuccessResponse<PageResponse<T>>> {
    public static <T> ApiPageResponse<T> dataResponse(Page<T> response){
        // SuccessResponse 의 data 안에 PageResponse 담기 위해 생성
        PageResponse<T> pageResponse = PageResponse.of(response);

        return new ApiPageResponse<>(
                SuccessResponse.of(pageResponse),
                SuccessCode.GENERAL_SUCCESS.getStatus()
        );
    }


    public ApiPageResponse(SuccessResponse<PageResponse<T>> body, HttpStatusCode status) {
        super(body, status);
    }


}
