package org.mar.telegram.bot.service.jms.artemis;

import com.mar.Const;
import com.mar.dto.mq.LoadFileInfo;
import com.mar.dto.mq.LogEvent;
import com.mar.dto.mq.MQDataRq;
import com.mar.interfaces.mq.MQSender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
@Profile("local")
public class ArtemisMQSender implements MQSender {

    @Value("${spring.application.name}")
    private String appName;

    @Autowired
    private JmsTemplate jmsTemplate;

    @Override
    public void sendFileInfo(String rqUuid, LoadFileInfo fileInfo) {
        jmsTemplate.convertAndSend(
                Const.TELEGRAM_BOT_MQ,
                new MQDataRq(rqUuid, new Date(), MQDataRq.MQDataType.FILE_INFO, fileInfo)
        );
    }

    @Override
    public void sendLog(String rqUuid, LogEvent.LogLevel logLevel, final String message, Object... objects) {
        LogEvent logEvent = new LogEvent(appName, message, logLevel, objects, new Date());
        jmsTemplate.convertAndSend(
                Const.TELEGRAM_BOT_MQ,
                new MQDataRq(rqUuid, new Date(), MQDataRq.MQDataType.LOG, logEvent)
        );
    }
}