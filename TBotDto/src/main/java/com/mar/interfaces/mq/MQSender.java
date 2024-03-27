package com.mar.interfaces.mq;

import com.mar.dto.mq.LoadFileInfo;
import com.mar.dto.mq.LogEvent;

public interface MQSender {

    void sendFileInfo(String rqUuid, LoadFileInfo sendFile);

    void sendLog(String rqUuid, LogEvent.LogLevel logLevel, final String message, Object... objects);

}
