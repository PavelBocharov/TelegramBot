package org.mar.telegram.bot.service.bot;

import com.pengrad.telegrambot.Callback;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.PhotoSize;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.GetFile;
import com.pengrad.telegrambot.request.GetMe;
import com.pengrad.telegrambot.request.GetUpdates;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.GetFileResponse;
import com.pengrad.telegrambot.response.GetMeResponse;
import jakarta.annotation.PostConstruct;
import okhttp3.OkHttpClient;
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

    public static final String START_TAG = "/start";
    private static String caption = null;
    private static volatile int number = 0;


    @Value("${application.bot.directory.path}")
    private String downloadPath;

    @Value("${application.bot.token}")
    private String BOT_TOKEN;

    @Autowired
    private OrderSender orderSender;

    @PostConstruct
    public void postInit() {
        OkHttpClient client = new OkHttpClient();
        TelegramBot bot = new TelegramBot.Builder(BOT_TOKEN).okHttpClient(client).build();

        GetMe getMe = new GetMe();
        bot.execute(getMe, new Callback<GetMe, GetMeResponse>() {
            @Override
            public void onResponse(GetMe getMe, GetMeResponse getMeResponse) {
                getMeResponse.user();
            }

            @Override
            public void onFailure(GetMe getMe, IOException e) {
                e.printStackTrace();
            }
        });

        bot.setUpdatesListener(updates -> {
            worker(bot, updates);
            return UpdatesListener.CONFIRMED_UPDATES_ALL;
        });

        GetUpdates getUpdates = new GetUpdates().limit(999).offset(0).timeout(0);
        worker(bot, bot.execute(getUpdates).updates());
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
                number = 0;
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
                            typeDir
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

    private void saveToDisk(String urlToFile, String saveDiskPath, String typeDir) {
            orderSender.send(LoadFileInfo.builder()
                    .fileUrl(urlToFile)
                    .saveToPath(saveDiskPath)
                    .fileName(caption)
                    .typeDir(typeDir)
                    .build()
            );
    }
}
