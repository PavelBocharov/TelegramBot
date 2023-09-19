package org.mar.telegram.bot.service.jms.artemis;

import org.mar.telegram.bot.service.jms.MQSender;
import org.mar.telegram.bot.service.jms.dto.LoadFileInfo;
import org.mar.telegram.bot.service.jms.dto.LogEvent;
import org.mar.telegram.bot.service.jms.dto.MQDataRq;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.logging.LogLevel;
import org.springframework.context.annotation.Profile;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;

import static org.mar.telegram.bot.config.MQConf.TELEGRAM_BOT_MQ;

@Service
@Profile("local")
public class ArtemisMQSender implements MQSender {

    @Autowired
    private JmsTemplate jmsTemplate;

    @Override
    public void sendFileInfo(String rqUuid, LoadFileInfo fileInfo) {
        jmsTemplate.convertAndSend(TELEGRAM_BOT_MQ,
                new MQDataRq(rqUuid, new Date(), MQDataRq.MQDataType.FILE_INFO, fileInfo)
        );
    }

    @Override
    public void sendLog(String rqUuid, LogLevel logLevel, final String message, Object... objects) {
        LogEvent logEvent = new LogEvent(message, logLevel, objects, new Date());
        jmsTemplate.convertAndSend(TELEGRAM_BOT_MQ,
                new MQDataRq(rqUuid, new Date(), MQDataRq.MQDataType.LOG, logEvent)
        );
    }
}