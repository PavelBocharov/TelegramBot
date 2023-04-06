package org.mar.telegram.bot.service.bot;

import com.pengrad.telegrambot.Callback;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.PhotoSize;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.GetFile;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.GetFileResponse;
import jakarta.annotation.PostConstruct;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.mar.telegram.bot.service.jms.LoadFileInfo;
import org.mar.telegram.bot.service.jms.OrderSender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
public class TelegramBotController {

    private static String caption = null;

    @Value("${application.bot.directory.path}")
    private String downloadPath;

    @Value("${application.bot.token}")
    private String botToken;

    @Autowired
    private OrderSender orderSender;

    @Autowired
    private TelegramBot bot;

    @PostConstruct
    public void postInit() {
        bot.setUpdatesListener(updates -> {
            worker(bot, updates);
            return UpdatesListener.CONFIRMED_UPDATES_ALL;
        });

    }

    private void worker(TelegramBot bot, List<Update> updates) {
        if (updates != null) {
            for (Update update : updates) {
                if (update != null && update.message() != null) {
                    parsText(bot, update.message());
                    savePhoto(bot, update.message());
                    saveVideo(bot, update.message());
                    saveDocs(bot, update.message());
                }
            }
        }
    }

    private void parsText(TelegramBot bot, Message message) {
        String text = message.text();
        if (text != null) {
            text = text.trim();
            if (!text.isEmpty()) {
                caption = text;
                bot.execute(new SendMessage(message.chat().id(), "New caption - " + caption));
            }
        }
    }

    private void savePhoto(TelegramBot bot, Message message) {
        if (message.photo() != null && message.photo().length > 0) {
            PhotoSize ps = null;
            for (PhotoSize photoSize : message.photo()) {
                if (ps == null) {
                    ps = photoSize;
                } else {
                    if (ps.fileSize() < photoSize.fileSize()) {
                        ps = photoSize;
                    }
                }
            }
            if (ps != null) saveFile(bot, message, ps.fileId(), "photos");
        }
    }

    private void saveDocs(TelegramBot bot, Message message) {
        if (message.document() != null) {
            saveFile(bot, message, message.document().fileId(), "documents");
        }
    }

    private void saveVideo(TelegramBot bot, Message message) {
        if (message.video() != null) {
            saveFile(bot, message, message.video().fileId(), "videos");
        }
    }

    private void saveFile(TelegramBot bot, Message message, String fileId, String typeDir) {
        GetFile request = new GetFile(fileId);

        bot.execute(request, new Callback<GetFile, GetFileResponse>() {
            @Override
            public void onResponse(GetFile getFile, GetFileResponse getFileResponse) {
                try {
                    String savePath = downloadPath + getFileResponse.file().filePath();
                    saveToDisk(
                            bot.getFullFilePath(getFileResponse.file()),
                            savePath,
                            typeDir,
                            message.chat().id()
                    );
                    bot.execute(new SendMessage(message.chat().id(), "Work with file: " + getFileResponse.file().filePath()));
                } catch (Exception ex) {
                    bot.execute(new SendMessage(message.chat().id(), "Not save file: " + ExceptionUtils.getRootCauseMessage(ex)));
                }
            }

            @Override
            public void onFailure(GetFile getFile, IOException ex) {
                bot.execute(new SendMessage(message.chat().id(), "Not save file: " + ExceptionUtils.getMessage(ex)));
            }
        });

    }

    private void saveToDisk(String urlToFile, String saveDiskPath, String typeDir, Long chatId) {
        orderSender.send(LoadFileInfo.builder()
                .fileUrl(urlToFile)
                .saveToPath(saveDiskPath)
                .fileName(caption)
                .typeDir(typeDir)
                .chatId(chatId)
                .build()
        );
    }
}
