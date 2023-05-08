package org.mar.telegram.bot.service.jms.artemis;

import lombok.extern.slf4j.Slf4j;
import org.mar.telegram.bot.service.jms.MQSender;
import org.mar.telegram.bot.service.jms.dto.LoadFileInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;

import static org.mar.telegram.bot.config.MQConf.TELEGRAM_BOT_MQ;

@Slf4j
@Service
@Profile("!image")
public class ArtemisMQSender implements MQSender<LoadFileInfo> {

    @Autowired
    private JmsTemplate jmsTemplate;

    public void send(LoadFileInfo fileInfo) {
        log.info("WRITE to Artemis MQ -> {}", fileInfo);
        jmsTemplate.convertAndSend(TELEGRAM_BOT_MQ, fileInfo);
    }
}