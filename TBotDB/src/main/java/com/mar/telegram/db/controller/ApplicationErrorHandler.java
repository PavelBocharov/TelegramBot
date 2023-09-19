package com.mar.telegram.db.controller;

import com.mar.telegram.db.dto.BaseDto;
import com.mar.telegram.db.exception.BaseException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@Slf4j
@ControllerAdvice
public class ApplicationErrorHandler {

    @ExceptionHandler(BaseException.class)
    public ResponseEntity<BaseDto> handleRuntimeException(BaseException ex){
        BaseDto rs = ex.getResponse();
        log.error("<<< Response BaseException: {}", rs);
        ex.printStackTrace();
        return ResponseEntity.status(HttpStatus.OK).body(rs);
    }

    @ExceptionHandler(Throwable.class)
    public ResponseEntity<BaseDto> handleRuntimeException(Throwable ex){
        log.error("<<< Response Throwable: {}", ExceptionUtils.getRootCauseMessage(ex));
        ex.printStackTrace();
        return ResponseEntity.status(HttpStatus.OK)
                .body(new BaseDto().withErrorCode(HttpStatus.INTERNAL_SERVER_ERROR.value()).withErrorMsg(ExceptionUtils.getRootCauseMessage(ex)));
    }

}
