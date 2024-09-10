package com.triptune.global.response;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;

public class ApiPageResponse<T> extends ResponseEntity<SuccessResponse<PageResponse<T>>> {
    public static <T> ApiPageResponse<T> okResponse(Page<T> response){
        // SuccessResponse 의 data 안에 PageResponse 담기 위해 생성
        PageResponse<T> pageResponse = PageResponse.<T>builder()
                .totalPages(response.getTotalPages())
                .currentPage(response.getNumber())
                .totalElements(response.getTotalElements())
                .pageSize(response.getSize())
                .content(response.getContent())
                .build();

        SuccessResponse<PageResponse<T>> successResponse = SuccessResponse.<PageResponse<T>>builder()
                .message("200(성공)")
                .data(pageResponse)
                .build();
        
        return new ApiPageResponse<>(successResponse, HttpStatus.OK);
    }

    public ApiPageResponse(HttpStatusCode status) {
        super(status);
    }

    public ApiPageResponse(SuccessResponse<PageResponse<T>> body, HttpStatusCode status) {
        super(body, status);
    }

    public ApiPageResponse(MultiValueMap<String, String> headers, HttpStatusCode status) {
        super(headers, status);
    }

    public ApiPageResponse(SuccessResponse<PageResponse<T>> body, MultiValueMap<String, String> headers, HttpStatusCode status) {
        super(body, headers, status);
    }

    public ApiPageResponse(SuccessResponse<PageResponse<T>> body, MultiValueMap<String, String> headers, int rawStatus) {
        super(body, headers, rawStatus);
    }

}
