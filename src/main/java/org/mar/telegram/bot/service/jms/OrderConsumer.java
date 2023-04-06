package org.mar.telegram.bot.service.jms;

import lombok.extern.slf4j.Slf4j;
import org.mar.telegram.bot.service.bot.TelegramBotWorkService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import static org.mar.telegram.bot.config.AppConf.TELEGRAM_BOT_MQ;

@Slf4j
@Component
public class OrderConsumer {

    @Autowired
    private TelegramBotWorkService workService;

    @JmsListener(destination = TELEGRAM_BOT_MQ)
    public void onMessage(LoadFileInfo content){
        log.info("READ from MQ <- " + content);
        workService.work(content);
    }

}