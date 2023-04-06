package org.mar.telegram.bot.service.jms;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;

import static org.mar.telegram.bot.config.AppConf.TELEGRAM_BOT_MQ;

@Slf4j
@Service
public class OrderSender {

    @Autowired
    private JmsTemplate jmsTemplate;

    public void send(LoadFileInfo fileInfo) {
        log.info("WRITE to MQ -> {}", fileInfo);
        jmsTemplate.convertAndSend(TELEGRAM_BOT_MQ, fileInfo);
    }
}