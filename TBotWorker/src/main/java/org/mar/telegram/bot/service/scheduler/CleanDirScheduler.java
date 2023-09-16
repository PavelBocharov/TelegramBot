package org.mar.telegram.bot.service.scheduler;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

@Slf4j
@Component
public class CleanDirScheduler {

    @Value("${application.bot.directory.path}")
    private String downloadPath;

    @Value("${logging.file.path}")
    private String logDir;

    @Scheduled(cron = "${application.scheduler.cron}")
    public void cleanDir() {

        File dir = new File(downloadPath);
        if (FileUtils.isDirectory(dir)) {
            Collection<File> files = FileUtils.listFiles(dir, null, true);
            for (File file : files) {
                if (!file.getAbsolutePath().startsWith(logDir)) {
                    try {
                        log.warn("Delete file - {}", file.getAbsolutePath());
                        FileUtils.delete(file);
                    } catch (IOException e) {
                        log.error("Delete file FAIL - {}, {}", file.getAbsolutePath(), ExceptionUtils.getRootCauseMessage(e));
                    }
                }
            }
        } else {
            log.warn("'{}' is not dir", downloadPath);
        }

    }

}
