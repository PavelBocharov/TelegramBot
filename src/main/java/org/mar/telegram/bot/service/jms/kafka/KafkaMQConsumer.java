package org.mar.telegram.bot.service.jms.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.mar.telegram.bot.service.bot.TelegramBotDownloadFileService;
import org.mar.telegram.bot.service.jms.MQConsumer;
import org.mar.telegram.bot.service.jms.dto.LoadFileInfo;
import org.mar.telegram.bot.service.jms.dto.LogEvent;
import org.mar.telegram.bot.service.jms.dto.MQDataRq;
import org.mar.telegram.bot.service.logger.LoggerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import static org.mar.telegram.bot.config.MQConf.TELEGRAM_BOT_MQ;
import static org.mar.telegram.bot.config.MQConf.TELEGRAM_BOT_MQ_GROUP;

@Service
@Profile("!local")
public class KafkaMQConsumer implements MQConsumer {

    @Autowired
    private TelegramBotDownloadFileService workService;
    @Autowired
    private LoggerService loggerService;

    @SneakyThrows
    @KafkaListener(topics = TELEGRAM_BOT_MQ, groupId = TELEGRAM_BOT_MQ_GROUP)
    public void readFileInfo(String message) {
        ObjectMapper objectMapper = new ObjectMapper();
        MQDataRq rq = objectMapper.readValue(message, MQDataRq.class);
        if (rq != null) {
            if (rq.getType().equals(MQDataRq.MQDataType.FILE_INFO)) {
                LoadFileInfo fileInfo = objectMapper.readValue(
                        objectMapper.writeValueAsString(rq.getBody()),
                        LoadFileInfo.class
                );
                workService.work(rq.getRqUuid(), fileInfo);
            }
            if (rq.getType().equals(MQDataRq.MQDataType.LOG)) {
                LogEvent event = objectMapper.readValue(
                        objectMapper.writeValueAsString(rq.getBody()),
                        LogEvent.class
                );
                loggerService.printLog(rq.getRqUuid(), event);
            }
        }
    }
}
