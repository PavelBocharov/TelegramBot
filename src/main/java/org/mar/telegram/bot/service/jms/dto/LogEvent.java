package org.mar.telegram.bot.service.jms.dto;

import lombok.*;
import org.springframework.boot.logging.LogLevel;

import java.io.Serializable;
import java.util.Date;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class LogEvent implements Serializable {

    private String msg;
    private LogLevel logLevel;
    private Object[] objects;
    private Date logDate;

}
