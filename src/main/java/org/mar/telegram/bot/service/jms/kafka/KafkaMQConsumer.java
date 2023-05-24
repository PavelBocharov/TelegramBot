package org.mar.telegram.bot.service.jms.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.mar.telegram.bot.service.bot.TelegramBotWorkService;
import org.mar.telegram.bot.service.jms.MQConsumer;
import org.mar.telegram.bot.service.jms.dto.LoadFileInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import static org.mar.telegram.bot.config.MQConf.TELEGRAM_BOT_MQ;
import static org.mar.telegram.bot.config.MQConf.TELEGRAM_BOT_MQ_GROUP;

@Slf4j
@Service
@Profile("!local")
public class KafkaMQConsumer implements MQConsumer {

    @Autowired
    private TelegramBotWorkService workService;

    @SneakyThrows
    @KafkaListener(topics = TELEGRAM_BOT_MQ, groupId = TELEGRAM_BOT_MQ_GROUP)
    public void listenWithHeaders(String message) {
        log.info("READ from Kafka MQ <- Message: {}", message);
        workService.work(new ObjectMapper().readValue(message, LoadFileInfo.class));
    }

}
