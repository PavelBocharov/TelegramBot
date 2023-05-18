package org.mar.telegram.bot.service.bot;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.PhotoSize;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.GetFile;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.GetFileResponse;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.mar.telegram.bot.cache.BotCache;
import org.mar.telegram.bot.db.entity.PostInfo;
import org.mar.telegram.bot.db.service.PostInfoService;
import org.mar.telegram.bot.service.jms.MQSender;
import org.mar.telegram.bot.service.jms.dto.LoadFileInfo;
import org.mar.telegram.bot.service.jms.dto.URLInfo;
import org.mar.telegram.bot.utils.ContentType;
import org.mar.telegram.bot.utils.ParsingTextUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

import static java.lang.String.format;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Slf4j
@Service
@RequiredArgsConstructor
public class TelegramBotController {

    public static final String ACTION_CAPTION = "/caption";
    public static final String ACTION_SEND_POST = "/sendPost";

    @Value("${application.bot.directory.path}")
    private String downloadPath;

    @Value("${application.group.chat.id}")
    private Long groupChatId;

    private final MQSender<LoadFileInfo> telegramBotMqSender;
    private final BotCache cache;
    private final TelegramBot bot;
    private final CallbackDataService callbackDataService;
    private final BotExecutor botExecutor;
    private final PostInfoService postInfoService;

    @PostConstruct
    public void postInit() {
        bot.setUpdatesListener(updates -> {
            worker(bot, updates);
            return UpdatesListener.CONFIRMED_UPDATES_ALL;
        });
    }

    private void worker(TelegramBot bot, List<Update> updates) {
        try {
            if (updates != null) {
                for (Update update : updates) {
                    if (update != null && update.message() != null) {
                        parsText(bot, update.message());
                        savePhoto(bot, update.message());
                        saveVideo(bot, update.message());
                        saveAnimation(bot, update.message());
                        saveDocs(bot, update.message());
                    }
                    if (nonNull(update) && nonNull(update.callbackQuery())) {
                        callbackDataService.checkCallbackData(bot, update.callbackQuery());
                    }
                }
            }
        } catch (Throwable ex) {
            log.error(ExceptionUtils.getRootCauseMessage(ex));
        }
    }


    private void parsText(TelegramBot bot, Message message) {
        String text = message.text();
        if (text != null) {
            text = text.trim();
            if (!text.isEmpty()) {
                if (checkAction(message.chat().id(), text)) return;
                if (checkContent(message)) return;

                cache.setFileName(message.chat().id(), text);
                botExecutor.execute(new SendMessage(message.chat().id(), "New caption - " + text));
            }
        }
    }

    private boolean checkAction(Long chatId, String text) {
        if (text.startsWith(ACTION_CAPTION)) {
            String caption = text.substring(ACTION_CAPTION.length()).trim();
            if (isNotBlank(caption)) {
                PostInfo postInfo = postInfoService.getNotSendPost();
                postInfo.setCaption(caption);
                postInfoService.save(postInfo);
            } else {
                botExecutor.execute(new SendMessage(chatId, "For save post caption write '" + ACTION_CAPTION + " post_text'"));
            }
            return true;
        }
        if (text.equals(ACTION_SEND_POST)) {
            PostInfo postInfo = postInfoService.getNotSendPost();

            if (isNotBlank(postInfo.getMediaPath())) {
                callbackDataService.sendPost(groupChatId, postInfo);
                postInfo = postInfoService.getNotSendPost();
                postInfo.setIsSend(true);
                postInfoService.save(postInfo);
            }
            return true;
        }
        return false;
    }

    private boolean checkContent(Message message) {
        String text = message.text();
        URLInfo info = ParsingTextUtils.whatIsUrl(text);
        if (info != null
                && info.getContentType() != null
                && !info.getContentType().equals(ContentType.Text)
        ) {
            botExecutor.execute(new SendMessage(message.chat().id(), format("Detect '%s' URL - %s", info.getContentType().getTypeDit(), info.getUrl())));
            String savePath = downloadPath + info.getContentType().getTypeDit() + "//" + FilenameUtils.getName(info.getUrl());
            saveToDisk(info, savePath, message.chat().id(), info.getContentType());
            return true;
        }
        return false;
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
            if (ps != null) {
                saveFile(bot, message, ps.fileId(), ContentType.Picture);
            }
        }
    }

    private void saveDocs(TelegramBot bot, Message message) {
        if (message.document() != null) {
            saveFile(bot, message, message.document().fileId(), ContentType.Doc);
        }
    }

    private void saveVideo(TelegramBot bot, Message message) {
        if (message.video() != null) {
            saveFile(bot, message, message.video().fileId(), ContentType.Video);
        }
    }

    private void saveAnimation(TelegramBot bot, Message message) {
        if (message.animation() != null) {
            saveFile(bot, message, message.animation().fileId(), ContentType.Gif);
        }
    }

    private void saveFile(TelegramBot bot, Message message, String fileId, ContentType typeDir) {
        GetFile request = new GetFile(fileId);

        botExecutor.execute(
                request,
                baseResponse -> {
                    try {
                        GetFileResponse getFileResponse = (GetFileResponse) baseResponse;
                        if (getFileResponse.file() == null) {
                            throw new Exception(getFileResponse.description());
                        }
                        String savePath = downloadPath + getFileResponse.file().filePath();
                        saveToDisk(
                                URLInfo.builder().contentType(typeDir).url(bot.getFullFilePath(getFileResponse.file())).build(),
                                savePath,
                                message.chat().id(),
                                typeDir
                        );
                        botExecutor.execute(new SendMessage(message.chat().id(), "Work with file: " + getFileResponse.file().filePath()));
                    } catch (Exception ex) {
                        log.error(ExceptionUtils.getRootCauseMessage(ex));
                        botExecutor.execute(new SendMessage(message.chat().id(), "Not save file: " + ExceptionUtils.getRootCauseMessage(ex)));
                    }
                },
                throwable -> {
                    log.error(ExceptionUtils.getRootCauseMessage(throwable));
                    bot.execute(new SendMessage(message.chat().id(), "Not save file: " + ExceptionUtils.getMessage(throwable)));
                });
    }

    private void saveToDisk(URLInfo urlInfo, String saveDiskPath, Long chatId, ContentType typeDir) {
        telegramBotMqSender.send(LoadFileInfo.builder()
                .fileUrl(urlInfo.getUrl())
                .saveToPath(saveDiskPath)
                .fileName(cache.getFileName(chatId))
                .typeDir(urlInfo.getContentType().getTypeDit())
                .chatId(chatId)
                .fileType(urlInfo.getFileType())
                .mediaType(typeDir)
                .build()
        );
    }
}
