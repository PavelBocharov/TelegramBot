package org.mar.telegram.bot.service.logger;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mar.dto.mq.LogEvent;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.stream.Stream;

@Slf4j
@Service
public class LoggerService {

    public void sendLog(String rqUuid, LogEvent.LogLevel logLevel, final String message, Object... objects) {
        printLog(rqUuid, new LogEvent("t_worker", message, logLevel, objects, new Date()));
    }


    @SneakyThrows
    public void printLog(String rqUuid, LogEvent event) {
        String logMessage = event.getApplicationName() + " >>> [" + rqUuid + "] " + event.getMsg();
        ObjectMapper objectMapper = new ObjectMapper().setSerializationInclusion(JsonInclude.Include.NON_NULL);
        Object[] args = Stream.of(event.getObjects()).map(obj -> getString(objectMapper, obj)).toArray();
        switch (event.getLogLevel()) {
            case INFO -> {
                log.info(logMessage, args);
                break;
            }
            case DEBUG -> {
                log.debug(logMessage, args);
                break;
            }
            case WARN -> {
                log.warn(logMessage, args);
                break;
            }
            case ERROR -> {
                log.error(logMessage, args);
                break;
            }
            default -> {
                log.trace(logMessage, args);
            }
        }
    }

    private String getString(ObjectMapper objectMapper, Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
        }
        return String.valueOf(obj);
    }

}
