package org.mar.telegram.bot.service.jms.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.mar.telegram.bot.service.jms.MQSender;
import org.mar.telegram.bot.service.jms.dto.LoadFileInfo;
import org.mar.telegram.bot.service.jms.dto.LogEvent;
import org.mar.telegram.bot.service.jms.dto.MQDataRq;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.logging.LogLevel;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;

import static org.mar.telegram.bot.config.MQConf.TELEGRAM_BOT_MQ;

@Service
@Profile("!local")
public class KafkaMQSender implements MQSender {

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @SneakyThrows
    @Override
    public void sendFileInfo(String rqUuid, LoadFileInfo sendFile) {
        kafkaTemplate.send(TELEGRAM_BOT_MQ, new ObjectMapper().writeValueAsString(
                new MQDataRq(rqUuid, new Date(), MQDataRq.MQDataType.FILE_INFO, sendFile)
        ));
    }

    @SneakyThrows
    @Override
    public void sendLog(String rqUuid, LogLevel logLevel, String message, Object... objects) {
        LogEvent event = new LogEvent(message, logLevel, objects, new Date());
        kafkaTemplate.send(TELEGRAM_BOT_MQ, new ObjectMapper().writeValueAsString(
                new MQDataRq(rqUuid, new Date(), MQDataRq.MQDataType.LOG, event)
        ));
    }

}
