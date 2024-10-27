package com.triptune.global.response.pagination;

import com.triptune.global.enumclass.SuccessCode;
import com.triptune.global.response.SuccessResponse;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;

public class ApiSchedulePageResponse<T> extends ResponseEntity<SuccessResponse<SchedulePageResponse<T>>> {
    public static <T> ApiSchedulePageResponse<T> dataResponse(SchedulePageResponse<T> schedulePageResponse){
        return new ApiSchedulePageResponse<>(
                SuccessResponse.of(schedulePageResponse),
                SuccessCode.GENERAL_SUCCESS.getStatus()
        );
    }

    public ApiSchedulePageResponse(SuccessResponse<SchedulePageResponse<T>> body, HttpStatusCode status) {
        super(body, status);
    }


}
