package com.triptune.schedule.exception;

import com.triptune.global.exception.BusinessException;
import com.triptune.global.response.enums.ErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ForbiddenAttendeeException extends BusinessException {

    public ForbiddenAttendeeException(ErrorCode errorCode) {
        super(errorCode);
    }
}
