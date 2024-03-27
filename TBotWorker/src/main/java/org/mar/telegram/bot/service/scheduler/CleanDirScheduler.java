package org.mar.telegram.bot.service.scheduler;

import com.mar.dto.mq.LogEvent;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import com.mar.interfaces.mq.MQSender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.UUID;

@Slf4j
@Component
public class CleanDirScheduler {

    @Value("${application.bot.directory.path}")
    private String downloadPath;

    @Value("${logging.file.path}")
    private String logDirPath;

    @Autowired
    private MQSender sender;

    @Scheduled(cron = "${application.scheduler.cron}")
    public void cleanDir() {

        File dir = new File(downloadPath);
        File logDir = new File(logDirPath);
        if (FileUtils.isDirectory(dir)) {
            Collection<File> files = FileUtils.listFiles(dir, null, true);
            for (File file : files) {
                if (!file.getAbsolutePath().startsWith(logDir.getAbsolutePath())) {
                    try {
                        sender.sendLog(UUID.randomUUID().toString(), LogEvent.LogLevel.WARN, "Delete file - {}", file.getAbsolutePath());
                        FileUtils.delete(file);
                    } catch (IOException e) {
                        sender.sendLog(UUID.randomUUID().toString(), LogEvent.LogLevel.ERROR, "Delete file FAIL - {}, {}", file.getAbsolutePath(), ExceptionUtils.getRootCauseMessage(e));
                    }
                }
            }
        } else {
            sender.sendLog(UUID.randomUUID().toString(), LogEvent.LogLevel.ERROR, "'{}' is not dir", downloadPath);
        }

    }

}
