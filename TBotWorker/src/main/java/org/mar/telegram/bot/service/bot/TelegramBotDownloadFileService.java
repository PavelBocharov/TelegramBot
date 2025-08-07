package org.mar.telegram.bot.service.bot;

import com.mar.dto.mq.LoadFileInfo;
import com.mar.dto.rest.PostInfoDtoRs;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendMessage;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.mapstruct.factory.Mappers;
import org.mar.telegram.bot.mapper.DBIntegrationMapper;
import org.mar.telegram.bot.service.bot.db.PostService;
import org.mar.telegram.bot.service.logger.LoggerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import static com.mar.dto.mq.LogEvent.LogLevel.INFO;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Service
public class TelegramBotDownloadFileService {

    @Value("${application.bot.directory.path}")
    private String downloadPath;

    @Autowired
    private TelegramBot bot;
    @Autowired
    private PostService postInfoService;
    @Autowired
    private LoggerService loggerService;

    private DBIntegrationMapper mapper = Mappers.getMapper(DBIntegrationMapper.class);

    public void work(String rqUuid, LoadFileInfo fileInfo) {
        String diskPath = fileInfo.getSaveToPath();
        try {
            File file = new File(diskPath);
            String caption = fileInfo.getFileName();
            String typeDir = fileInfo.getTypeDir();
            int number = 0;
            if (isNotBlank(caption)) {
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

            loggerService.sendLog(rqUuid, INFO, "File name: {}, path: {}, save path: {}", file.getName(), fileInfo.getFileUrl(), diskPath);
            FileUtils.copyURLToFile(new URL(fileInfo.getFileUrl()), file);

            PostInfoDtoRs postInfo = postInfoService.getNotSendPost(rqUuid);
            postInfo.setMediaPath(diskPath);
            postInfo.setTypeDir(fileInfo.getMediaType().getTypeDir());
            postInfoService.save(rqUuid, mapper.mapRsToRq(postInfo));

            bot.execute(new SendMessage(fileInfo.getChatId(), "Save file: " + file.getName()));
        } catch (IOException e) {
            bot.execute(new SendMessage(fileInfo.getChatId(), "Save file failed: " + ExceptionUtils.getRootCauseMessage(e)));
            e.printStackTrace();
        }
    }

}
