package org.mar.telegram.bot.service.bot;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.PhotoSize;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.GetFile;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.GetFileResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.mar.telegram.bot.cache.BotCache;
import org.mar.telegram.bot.service.bot.db.PostService;
import org.mar.telegram.bot.service.db.dto.PostInfoDto;
import org.mar.telegram.bot.service.jms.MQSender;
import org.mar.telegram.bot.service.jms.dto.LoadFileInfo;
import org.mar.telegram.bot.service.jms.dto.URLInfo;
import org.mar.telegram.bot.utils.ContentType;
import org.mar.telegram.bot.utils.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import static java.lang.String.format;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.mar.telegram.bot.utils.Utils.getMaxPhotoSize;

@Slf4j
public class TelegramBotUtils {

    public static final String ACTION_CAPTION = "/caption";
    public static final String ACTION_SEND_POST = "/sendPost";

    @Value("${application.bot.directory.path}")
    private String downloadPath;

    @Value("${application.bot.admin.id}")
    private Long adminId;

    @Value("${application.group.chat.id}")
    private Long groupChatId;

    @Value("${application.group.chat.textLine}")
    private String textLine;

    @Autowired
    protected MQSender<LoadFileInfo> telegramBotMqSender;
    @Autowired
    protected BotCache cache;
    @Autowired
    protected TelegramBot bot;
    @Autowired
    protected CallbackDataService callbackDataService;
    @Autowired
    protected BotExecutor botExecutor;
    @Autowired
    protected PostService postInfoService;
    
    protected MessageStatus createMessageStatus(Update update) {
        if (nonNull(update)) {
            MessageStatus messageStatus = new MessageStatus();
            if (nonNull(update.message())) {
                messageStatus.setMsg(update.message());
                messageStatus.setMsgUserId(update.message().from().id());
            }
            if (nonNull(update.callbackQuery())) {
                messageStatus.setCallbackQuery(update.callbackQuery());
                messageStatus.setMsgUserId(update.callbackQuery().from().id());
            }
            return messageStatus;
        }
        throw new RuntimeException("update is null");
    }

    protected MessageStatus checkAdmin(MessageStatus messageStatus) {
        if (nonNull(messageStatus.getMsgUserId()) && !adminId.equals(messageStatus.getMsgUserId())) {
            messageStatus.setIsSuccess(true);
        }
        return messageStatus;
    }

    protected MessageStatus checkCallbackQuery(MessageStatus messageStatus) {
        if (messageStatus.getIsSuccess()) return messageStatus;

        return messageStatus
                .withIsSuccess(
                        callbackDataService.checkCallbackData(messageStatus.getCallbackQuery())
                );
    }

    protected MessageStatus parsText(MessageStatus messageStatus) {
        if (messageStatus.getIsSuccess()) return messageStatus;

        Message message = messageStatus.getMsg();
        String text = message.text();
        if (text != null) {
            text = text.trim();
            if (!text.isEmpty()) {
                if (checkAction(message.chat().id(), text)) return messageStatus.withIsSuccess(true);
                if (checkContent(message)) return messageStatus.withIsSuccess(true);

                cache.setFileName(message.chat().id(), text);
                botExecutor.execute(new SendMessage(message.chat().id(), "New caption - " + text));
                messageStatus.setIsSuccess(true);
            }
        }
        return messageStatus;
    }

    private boolean checkAction(Long chatId, String text) {
        if (text.startsWith(ACTION_CAPTION)) {
            String caption = text.substring(ACTION_CAPTION.length()).trim();
            if (isNotBlank(caption)) {
                PostInfoDto postInfo = postInfoService.getNotSendPost();
                postInfo.setCaption(caption + "\n" + textLine);
                postInfoService.save(postInfo);
            } else {
                botExecutor.execute(new SendMessage(chatId, "For save post caption write '" + ACTION_CAPTION + " post_text'"));
            }
            return true;
        }
        if (text.equals(ACTION_SEND_POST)) {
            PostInfoDto postInfo = postInfoService.getNotSendPost();

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
        URLInfo info = Utils.whatIsUrl(text);
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

    protected MessageStatus savePhoto(MessageStatus messageStatus) {
        if (messageStatus.getIsSuccess()) return messageStatus;

        Message message = messageStatus.getMsg();
        if (message.photo() != null && message.photo().length > 0) {
            PhotoSize ps = getMaxPhotoSize(message.photo());
            if (ps != null) {
                saveFile(message, ps.fileId(), ContentType.Picture);
                messageStatus.setIsSuccess(true);
            }
        }
        return messageStatus;
    }

    protected MessageStatus saveDocs(MessageStatus messageStatus) {
        if (messageStatus.getIsSuccess()) return messageStatus;

        Message message = messageStatus.getMsg();
        if (message.document() != null) {
            saveFile(message, message.document().fileId(), ContentType.Doc);
            messageStatus.setIsSuccess(true);
        }
        return messageStatus;
    }

    protected MessageStatus saveVideo(MessageStatus messageStatus) {
        if (messageStatus.getIsSuccess()) return messageStatus;

        Message message = messageStatus.getMsg();
        if (message.video() != null) {
            saveFile(message, message.video().fileId(), ContentType.Video);
            messageStatus.setIsSuccess(true);
        }
        return messageStatus;
    }

    protected MessageStatus saveAnimation(MessageStatus messageStatus) {
        if (messageStatus.getIsSuccess()) return messageStatus;

        Message message = messageStatus.getMsg();
        if (message.animation() != null) {
            saveFile(message, message.animation().fileId(), ContentType.Gif);
            messageStatus.setIsSuccess(true);
        }
        return messageStatus;
    }

    private void saveFile(Message message, String fileId, ContentType typeDir) {
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
