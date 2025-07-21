package com.mar.exception;

import lombok.Getter;

import java.util.Date;

@Getter
public class TbotException extends RuntimeException {

    private final String rqUuid;
    private final Date rqTm;

    public TbotException(String rqUuid, Date rqTm, String msg) {
        super(msg);
        this.rqUuid = rqUuid;
        this.rqTm = rqTm;
    }

    public TbotException(String rqUuid, Date rqTm, String msg, Throwable ex) {
        super(msg, ex);
        this.rqUuid = rqUuid;
        this.rqTm = rqTm;
    }
}
