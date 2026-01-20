package com.triptune.global.security.jwt.exception;

import com.triptune.global.exception.BusinessException;
import com.triptune.global.response.enums.ErrorCode;

public class CustomJwtUnAuthorizedException extends BusinessException {
    public CustomJwtUnAuthorizedException(ErrorCode errorCode){
        super(errorCode);
    }


}
