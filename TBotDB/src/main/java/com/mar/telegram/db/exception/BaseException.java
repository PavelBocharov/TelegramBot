package com.mar.telegram.db.exception;

import com.mar.telegram.db.dto.BaseDto;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Date;

@Getter
@AllArgsConstructor
public class BaseException extends RuntimeException {

    protected String rqUuid;
    protected Date rqTm;
    protected Integer errorCode;
    protected String errorMsg;

    public BaseDto getResponse() {
        return new BaseDto()
                .withErrorCode(errorCode)
                .withErrorMsg(errorMsg)
                .withRqTm(rqTm)
                .withRqUuid(rqUuid);
    }
}
