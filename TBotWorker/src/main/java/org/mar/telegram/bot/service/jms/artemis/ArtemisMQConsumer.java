package org.mar.telegram.bot.service.jms.artemis;

import com.mar.Const;
import com.mar.dto.mq.LoadFileInfo;
import com.mar.dto.mq.LogEvent;
import com.mar.dto.mq.MQDataRq;
import com.mar.interfaces.mq.MQConsumer;
import org.mar.telegram.bot.service.bot.TelegramBotDownloadFileService;
import org.mar.telegram.bot.service.logger.LoggerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Service;

@Service
@Profile("local")
public class ArtemisMQConsumer implements MQConsumer {

    @Autowired
    private TelegramBotDownloadFileService workService;
    @Autowired
    private LoggerService loggerService;

    @JmsListener(destination = Const.TELEGRAM_BOT_MQ)
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