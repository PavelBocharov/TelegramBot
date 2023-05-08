package org.mar.telegram.bot.service.jms.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.mar.telegram.bot.service.jms.MQSender;
import org.mar.telegram.bot.service.jms.dto.LoadFileInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import static org.mar.telegram.bot.config.MQConf.TELEGRAM_BOT_MQ;

@Slf4j
@Service
@Profile("image")
public class KafkaMQSender implements MQSender<LoadFileInfo> {

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Override
    public void send(LoadFileInfo sendFile) {
        try {
            log.info("WRITE to Kafka MQ -> {}", sendFile);
            kafkaTemplate.send(TELEGRAM_BOT_MQ, new ObjectMapper().writeValueAsString(sendFile));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

}
