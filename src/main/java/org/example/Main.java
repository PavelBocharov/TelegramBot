package org.example;

import com.pengrad.telegrambot.Callback;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.PhotoSize;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.Video;
import com.pengrad.telegrambot.request.*;
import com.pengrad.telegrambot.response.GetFileResponse;
import com.pengrad.telegrambot.response.GetMeResponse;
import okhttp3.OkHttpClient;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;

public class Main {

    public static final String START_TAG = "/start";
    public static final String END_TAG = "/end";
    public static final String NEW_FILE_NAME_TAG = "/fname";

    public static final String START_MSG = NEW_FILE_NAME_TAG + " filesname - set name for next files;\n" +
            END_TAG + " - reset all" +
            "\nCaption - ";
    public static String caption = null;
    public static volatile int number = 0;

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
                            "\n\tCaption: " + update.message().caption() +
                            "\n\tDoc: " + update.message().document() +
                            "\n\tVideo: " + update.message().video() +
                            "\n\tAnim: " + update.message().animation() +
                            "\n\tPhoto: " + update.message().photo()
                    );

                    savePhoto(bot, update.message());
                    saveDocs(bot, update.message());
                    saveVideo(bot, update.message());
                    parsText(bot, update.message());

                }
            }
        }
    }

    private static void parsText(TelegramBot bot, Message message) {
        String text = message.text();
        if (text != null) {
            text = text.trim();
            if (!text.isEmpty()) {
                SendMessage sendMessage = null;
                if (text.equals(START_TAG))
                    sendMessage = new SendMessage(message.chat().id(), START_MSG + caption);

                if (text.equals(END_TAG)) {
                    caption = null;
                    sendMessage = new SendMessage(message.chat().id(), "Reset OK");
                }

                if (text.startsWith(NEW_FILE_NAME_TAG)) {
                    String endPrefix = text.replaceFirst(NEW_FILE_NAME_TAG, "").trim();
                    if (endPrefix.isEmpty()) {
                        sendMessage = new SendMessage(message.chat().id(), "Caption is empty");
                    } else {
                        caption = endPrefix;
                        number = 0;
                        sendMessage = new SendMessage(message.chat().id(), "New caption - " + caption);
                    }
                }

                if (sendMessage != null) {
                    bot.execute(sendMessage);
                }
            }
        }

    }

    private static void savePhoto(TelegramBot bot, Message message) {
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
            if (ps != null) saveFile(bot, ps.fileId(), "photos");
        }
    }

    private static void saveDocs(TelegramBot bot, Message message) {
        if (message.document() != null) {
            saveFile(bot, message.document().fileId(), "documents");
        }
    }

    private static void saveVideo(TelegramBot bot, Message message) {
        if (message.video() != null) {
            saveFile(bot, message.video().fileId(), "videos");
        }
    }

    private static void saveFile(TelegramBot bot, String fileId, String typeDir) {
        GetFile request = new GetFile(fileId);

        bot.execute(request, new Callback<GetFile, GetFileResponse>() {
            @Override
            public void onResponse(GetFile getFile, GetFileResponse getFileResponse) {
                String savePath = System.getenv("DOWNLOAD_PATH") + getFileResponse.file().filePath();
                saveToDisk(
                        bot.getFullFilePath(getFileResponse.file()),
                        savePath,
                        typeDir
                );
            }

            @Override
            public void onFailure(GetFile getFile, IOException e) {
                e.printStackTrace();
            }
        });

    }

    private synchronized static void saveToDisk(String urlToFile, String saveDiskPath, String typeDir) {
        String diskPath = saveDiskPath;
        try {
            File file = new File(diskPath);

            if (caption != null && !caption.isEmpty()) {
                String type = FilenameUtils.getExtension(file.getName());
                String fileName = null;
                String[] aStr = caption.split("\n");
                if (aStr != null && aStr.length > 0) {
                    fileName = aStr[0];
                }

                diskPath = System.getenv("DOWNLOAD_PATH") + typeDir + "/" + fileName + '.' + type;
                file = new File(diskPath);
                while (file.exists()) {
                    diskPath = System.getenv("DOWNLOAD_PATH") + typeDir + "//" + fileName + '_' + number++ + '.' + type;
                    file = new File(diskPath);
                }
            }

            System.out.println("File name: " + file.getName() + ", path: " + urlToFile + ", save path: " + diskPath);
            FileUtils.copyURLToFile(new URL(urlToFile), file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}