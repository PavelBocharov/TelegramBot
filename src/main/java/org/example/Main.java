package org.example;

import com.pengrad.telegrambot.Callback;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.PhotoSize;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.GetFile;
import com.pengrad.telegrambot.request.GetMe;
import com.pengrad.telegrambot.request.GetUpdates;
import com.pengrad.telegrambot.response.GetFileResponse;
import com.pengrad.telegrambot.response.GetMeResponse;
import okhttp3.OkHttpClient;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

public class Main {


    public static void main(String[] args) {
        OkHttpClient client = new OkHttpClient();
        TelegramBot bot = new TelegramBot.Builder(System.getenv("BOT_TOKEN")).okHttpClient(client).build();

        GetMe getMe = new GetMe();
        bot.execute(getMe, new Callback<GetMe, GetMeResponse>() {
            @Override
            public void onResponse(GetMe getMe, GetMeResponse getMeResponse) {
                getMeResponse.user();
            }

            @Override
            public void onFailure(GetMe getMe, IOException e) {

            }
        });

        bot.setUpdatesListener(updates -> {
            printUpdates(bot, updates);
            return UpdatesListener.CONFIRMED_UPDATES_ALL;
        });

        GetUpdates getUpdates = new GetUpdates().limit(999).offset(0).timeout(0);
        printUpdates(bot, bot.execute(getUpdates).updates());

    }

    private static void printUpdates(TelegramBot bot, List<Update> updates) {
        if (updates != null) {
            for (Update update : updates) {
                if (update != null && update.message() != null) {
                    System.out.println("MsgId: " + update.message().messageId() +
                            "\n\tText: " + update.message().text() +
                            "\n\tDoc: " + update.message().document() +
                            "\n\tVideo: " + update.message().video() +
                            "\n\tAnim: " + update.message().animation() +
                            "\n\tPhoto: " + Arrays.stream(update.message().photo()).count()
                    );
                    if (update.message().photo() != null && update.message().photo().length > 0) {
                        PhotoSize ps = null;
                        for (PhotoSize photoSize : update.message().photo()) {
                            if (ps == null) {
                                ps = photoSize;
                            } else {
                                if (ps.fileSize() < photoSize.fileSize()) {
                                    ps = photoSize;
                                }
                            }
                        }
                        if (ps != null) saveFile(bot, ps.fileId());
                    }
                    if (update.message().document() != null) {
                        saveFile(bot, update.message().document().fileId());
                    }
                    if (update.message().video() != null) {
                        saveFile(bot, update.message().video().fileId());
                    }
                }
            }
        }
    }

    private static void saveFile(TelegramBot bot, String fileId) {
        GetFile request = new GetFile(fileId);
        bot.execute(request, new Callback<GetFile, GetFileResponse>() {
            @Override
            public void onResponse(GetFile getFile, GetFileResponse getFileResponse) {
                String fullPath = bot.getFullFilePath(getFileResponse.file());
                System.out.println("File name: " + getFileResponse.file().fileId() + ", path: " + fullPath);

                try {
                    FileUtils.copyURLToFile(
                            new URL(fullPath),
                            new File(System.getenv("DOWNLOAD_PATH") + getFileResponse.file().filePath())
                    );
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public void onFailure(GetFile getFile, IOException e) {
                e.printStackTrace();
            }
        });

    }
}