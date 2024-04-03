package com.mar.telegram.db.controller;

import com.mar.dto.rest.BaseRs;
import com.mar.exception.BaseException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@ControllerAdvice
public class ApplicationErrorHandler {

    @ExceptionHandler(BaseException.class)
    public ResponseEntity<BaseRs> handleRuntimeException(BaseException ex) {
        BaseRs rs = ex.getResponse();
        log.error("<<< Response BaseException: {}", rs);
        ex.printStackTrace();
        return ResponseEntity.status(HttpStatus.OK).body(rs);
    }

    @ExceptionHandler(Throwable.class)
    public ResponseEntity<BaseRs> handleRuntimeException(Throwable ex) {
        log.error("<<< Response Throwable: {}", ExceptionUtils.getRootCauseMessage(ex));
        ex.printStackTrace();

        if (ex instanceof ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode())
                    .body(new BaseRs()
                            .withErrorCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .withErrorMsg(ExceptionUtils.getRootCauseMessage(ex))
                    );
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new BaseRs()
                        .withErrorCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                        .withErrorMsg(ExceptionUtils.getRootCauseMessage(ex))
                );
    }

}
