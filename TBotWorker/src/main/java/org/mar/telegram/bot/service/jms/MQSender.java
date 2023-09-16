package org.mar.telegram.bot.service.jms;

import org.mar.telegram.bot.service.jms.dto.LoadFileInfo;
import org.springframework.boot.logging.LogLevel;

public interface MQSender {
    void sendFileInfo(String rqUuid, LoadFileInfo sendFile);
    void sendLog(String rqUuid, LogLevel logLevel, final String message, Object... objects);
}
