package org.mar.telegram.bot.service.jms.artemis;

import lombok.extern.slf4j.Slf4j;
import org.mar.telegram.bot.service.bot.TelegramBotWorkService;
import org.mar.telegram.bot.service.jms.MQConsumer;
import org.mar.telegram.bot.service.jms.dto.LoadFileInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Service;

import static org.mar.telegram.bot.config.MQConf.TELEGRAM_BOT_MQ;

@Slf4j
@Service
@Profile("local")
public class ArtemisMQConsumer implements MQConsumer {

    @Autowired
    private TelegramBotWorkService workService;

    @JmsListener(destination = TELEGRAM_BOT_MQ)
    public void onMessage(LoadFileInfo content){
        log.info("READ from Artemis MQ <- " + content);
        workService.work(content);
    }

}