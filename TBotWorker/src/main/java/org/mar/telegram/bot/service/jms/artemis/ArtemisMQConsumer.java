package org.mar.telegram.bot.service.jms.artemis;

import org.mar.telegram.bot.service.bot.TelegramBotDownloadFileService;
import org.mar.telegram.bot.service.jms.MQConsumer;
import org.mar.telegram.bot.service.jms.dto.LoadFileInfo;
import org.mar.telegram.bot.service.jms.dto.LogEvent;
import org.mar.telegram.bot.service.jms.dto.MQDataRq;
import org.mar.telegram.bot.service.logger.LoggerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Service;

import static org.mar.telegram.bot.config.MQConf.TELEGRAM_BOT_MQ;

@Service
@Profile("local")
public class ArtemisMQConsumer implements MQConsumer {

    @Autowired
    private TelegramBotDownloadFileService workService;
    @Autowired
    private LoggerService loggerService;

    @JmsListener(destination = TELEGRAM_BOT_MQ)
    public void readFileInfo(MQDataRq rq) {
        if (rq != null) {
            if (MQDataRq.MQDataType.FILE_INFO.equals(rq.getType())) {
                workService.work(rq.getRqUuid(), (LoadFileInfo) rq.getBody());
            }
            if (MQDataRq.MQDataType.LOG.equals(rq.getType())) {
                loggerService.printLog(rq.getRqUuid(), (LogEvent) rq.getBody());
            }
        }
    }

}