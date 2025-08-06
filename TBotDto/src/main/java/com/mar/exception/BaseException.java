package com.mar.exception;

import com.mar.dto.rest.BaseRs;
import lombok.Getter;

import java.util.Date;

@Getter
public class BaseException extends RuntimeException {

    protected String rqUuid;
    protected Date rqTm;
    protected Integer errorCode;
    protected String errorMsg;

    @Deprecated(since = "Use constructor with (.., Throwable cause). If you can.")
    public BaseException(String rqUuid, Date rqTm, Integer errorCode, String errorMsg) {
        super(errorMsg);
        this.rqUuid = rqUuid;
        this.rqTm = rqTm;
        this.errorCode = errorCode;
        this.errorMsg = errorMsg;
    }

    public BaseException(String rqUuid, Date rqTm, Integer errorCode, String errorMsg, Throwable cause) {
        super(cause);
        this.rqUuid = rqUuid;
        this.rqTm = rqTm;
        this.errorCode = errorCode;
        this.errorMsg = errorMsg;
    }

    public BaseRs getResponse() {
        return new BaseRs()
                .withErrorCode(errorCode)
                .withErrorMsg(errorMsg)
                .withRqTm(rqTm.getTime())
                .withRqUuid(rqUuid);
    }
}
