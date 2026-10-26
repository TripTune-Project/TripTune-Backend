package com.triptune.schedule.exception;

import com.triptune.global.message.ErrorCode;
import com.triptune.schedule.dto.response.ScheduleUpdateEventResponse;
import lombok.Getter;

@Getter
public class ConflictScheduleVersionException extends RuntimeException {
    private ErrorCode errorCode;
    private ScheduleUpdateEventResponse eventResponse;

    public ConflictScheduleVersionException(ErrorCode errorCode, ScheduleUpdateEventResponse eventResponse) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.eventResponse = eventResponse;
    }
}
