package org.mar.telegram.bot.service.bot;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendMessage;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.mar.telegram.bot.service.jms.dto.LoadFileInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.net.URL;

@Slf4j
@Service
public class TelegramBotWorkService {

    @Value("${application.bot.directory.path}")
    private String downloadPath;

    @Autowired
    private TelegramBot bot;

    public void work(LoadFileInfo fileInfo) {
        String diskPath = fileInfo.getSaveToPath();
        try {
            File file = new File(diskPath);
            String caption = fileInfo.getFileName();
            String typeDir = fileInfo.getTypeDir();
            int number = 0;
            if (caption != null && !caption.isEmpty()) {
                String type = fileInfo.getFileType() == null ? '.' + FilenameUtils.getExtension(file.getName()) : fileInfo.getFileType();
                String fileName = null;
                String[] aStr = caption.split("\n");
                if (aStr != null && aStr.length > 0) {
                    fileName = aStr[0];
                }

                diskPath = downloadPath + typeDir + "//" + fileName + type;
                file = new File(diskPath);
                while (file.exists()) {
                    diskPath = downloadPath + typeDir + "//" + fileName + '_' + number++ + type;
                    file = new File(diskPath);
                }
            }

            log.info("File name: {}, path: {}, save path: {}", file.getName(), fileInfo.getFileUrl(), diskPath);
            FileUtils.copyURLToFile(new URL(fileInfo.getFileUrl()), file);
            bot.execute(new SendMessage(fileInfo.getChatId(), "Save file: " + file.getName()));
        } catch (IOException e) {
            bot.execute(new SendMessage(fileInfo.getChatId(), "Save file failed: " + ExceptionUtils.getRootCauseMessage(e)));
            e.printStackTrace();
        }
    }

}
