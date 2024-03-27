package org.mar.telegram.bot.service.jms.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mar.Const;
import com.mar.dto.mq.LoadFileInfo;
import com.mar.dto.mq.LogEvent;
import com.mar.dto.mq.MQDataRq;
import com.mar.interfaces.mq.MQSender;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
@Profile("!local")
public class KafkaMQSender implements MQSender {

    @Value("${spring.application.name}")
    private String appName;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @SneakyThrows
    @Override
    public void sendFileInfo(String rqUuid, LoadFileInfo sendFile) {
        kafkaTemplate.send(Const.TELEGRAM_BOT_MQ, new ObjectMapper().writeValueAsString(
                new MQDataRq(rqUuid, new Date(), MQDataRq.MQDataType.FILE_INFO, sendFile)
        ));
    }

    @SneakyThrows
    @Override
    public void sendLog(String rqUuid, LogEvent.LogLevel logLevel, String message, Object... objects) {
        LogEvent event = new LogEvent(appName, message, logLevel, objects, new Date());
        kafkaTemplate.send(Const.TELEGRAM_BOT_MQ, new ObjectMapper().writeValueAsString(
                new MQDataRq(rqUuid, new Date(), MQDataRq.MQDataType.LOG, event)
        ));
    }

}
