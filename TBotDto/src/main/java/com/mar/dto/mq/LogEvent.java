package com.mar.dto.mq;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class LogEvent implements Serializable {

    private String applicationName;
    private String msg;
    private LogLevel logLevel;
    private Object[] objects;
    private Date logDate;

    public enum LogLevel {
        INFO, DEBUG, WARN, ERROR
    }

}
