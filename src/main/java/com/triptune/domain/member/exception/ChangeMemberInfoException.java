package com.triptune.domain.member.exception;

import com.triptune.global.enumclass.ErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ChangeMemberInfoException extends RuntimeException{
    private final HttpStatus httpStatus;

    public ChangeMemberInfoException(ErrorCode errorCode){
        super(errorCode.getMessage());
        this.httpStatus = errorCode.getStatus();
    }
}
