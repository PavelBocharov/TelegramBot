package org.mar.telegram.bot.service.bot;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.PhotoSize;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.GetFile;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.GetFileResponse;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.mar.telegram.bot.cache.BotCache;
import org.mar.telegram.bot.service.bot.db.PostService;
import org.mar.telegram.bot.service.bot.db.UserService;
import org.mar.telegram.bot.service.bot.dto.MessageStatus;
import org.mar.telegram.bot.service.bot.dto.mapper.CallbackQueryMapper;
import org.mar.telegram.bot.service.db.dto.PostInfoDto;
import org.mar.telegram.bot.service.jms.MQSender;
import org.mar.telegram.bot.service.jms.dto.LoadFileInfo;
import org.mar.telegram.bot.service.jms.dto.URLInfo;
import org.mar.telegram.bot.utils.ContentType;
import org.mar.telegram.bot.utils.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.logging.LogLevel;

import static java.lang.String.format;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.ArrayUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.mar.telegram.bot.utils.Utils.getMaxPhotoSize;

public class TelegramBotUtils {

    public static final String ACTION_CAPTION = "/caption";
    public static final String ACTION_SEND_POST = "/sendPost";

    @Value("${application.bot.directory.path}")
    private String downloadPath;

    @Value("${application.bot.admin.id}")
    private Long adminId;

    @Value("${application.group.chat.id}")
    private Long groupChatId;

    @Value("${application.group.chat.textLine:}")
    private String textLine;

    @Autowired
    protected MQSender mqSender;
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
    @Autowired
    protected UserService userInfoService;

    protected MessageStatus createMessageStatus(String rqUuid, Update update) {
        if (nonNull(update)) {
            MessageStatus messageStatus = new MessageStatus();
            messageStatus.setRqUuid(rqUuid);
            if (nonNull(update.message())) {
                messageStatus.setMsg(update.message());
                messageStatus.setMsgUserId(update.message().from().id());
            }
            if (nonNull(update.callbackQuery())) {
                messageStatus.setQuery(CallbackQueryMapper.toDto(update.callbackQuery()));
                messageStatus.setMsgUserId(messageStatus.getQuery().getFromUserId());
            }
            mqSender.sendLog(messageStatus.getRqUuid(), LogLevel.DEBUG, "Get message: {}", messageStatus);
            return messageStatus;
        }
        throw new RuntimeException("update is null");
    }

    protected void checkAdmin(MessageStatus messageStatus) {
        if (messageStatus.getIsSuccess()) {
            return;
        }
        mqSender.sendLog(messageStatus.getRqUuid(), LogLevel.DEBUG, "Check admin: {}", messageStatus);
        if (nonNull(messageStatus.getMsgUserId()) && !adminId.equals(messageStatus.getMsgUserId())) {
            messageStatus.setIsSuccess(true);
            mqSender.sendLog(messageStatus.getRqUuid(), LogLevel.DEBUG, "Check admin FAIL: {}", messageStatus);
        }
    }

    protected void checkCallbackQuery(MessageStatus messageStatus) {
        if (messageStatus.getIsSuccess()) {
            return;
        }

        mqSender.sendLog(messageStatus.getRqUuid(), LogLevel.DEBUG, "Check callback query: {}", messageStatus);
        messageStatus.setIsSuccess(
                callbackDataService.checkCallbackData(messageStatus.getRqUuid(), messageStatus.getQuery())
        );
    }

    protected void parsText(MessageStatus messageStatus) {
        if (messageStatus.getIsSuccess()) {
            return;
        }

        mqSender.sendLog(messageStatus.getRqUuid(), LogLevel.DEBUG, "Pars text: {}", messageStatus);
        Message message = messageStatus.getMsg();
        if (nonNull(message)) {
            String text = message.text();
            if (isNotBlank(text)) {
                text = text.trim();
                if (isNotBlank(text)) {
                    if (checkAction(messageStatus.getRqUuid(), message.chat().id(), text)) {
                        messageStatus.setIsSuccess(true);
                        return;
                    }
                    if (checkContent(messageStatus.getRqUuid(), message)) {
                        messageStatus.setIsSuccess(true);
                        return;
                    }

                    cache.setFileName(message.chat().id(), text);
                    botExecutor.execute(messageStatus.getRqUuid(), new SendMessage(message.chat().id(), "New caption - " + text));
                    messageStatus.setIsSuccess(true);
                }
            }
        }
    }

    private boolean checkAction(String rqUuid, Long chatId, String text) {
        if (text.startsWith(ACTION_CAPTION)) {
            String caption = text.substring(ACTION_CAPTION.length()).trim();
            if (isNotBlank(caption)) {
                mqSender.sendLog(rqUuid, LogLevel.DEBUG, "Action: {}, caption: {}", ACTION_CAPTION, caption.replace("\n", " <br> "));
                PostInfoDto postInfo = postInfoService.getNotSendPost(rqUuid);
                postInfo.setCaption(caption + "\n" + textLine);
                postInfoService.save(rqUuid, postInfo);
            } else {
                botExecutor.execute(rqUuid, new SendMessage(chatId, "For save post caption write '" + ACTION_CAPTION + " post_text'"));
            }
            return true;
        }
        if (text.equals(ACTION_SEND_POST)) {
            PostInfoDto postInfo = postInfoService.getNotSendPost(rqUuid);

            if (isNotBlank(postInfo.getMediaPath()) && isNotBlank(postInfo.getCaption())) {
                mqSender.sendLog(rqUuid, LogLevel.DEBUG, "Action: {}", ACTION_SEND_POST);
                callbackDataService.sendPost(rqUuid, groupChatId, postInfo);
                postInfo = postInfoService.getNotSendPost(rqUuid);
                postInfo.setIsSend(true);
                postInfoService.save(rqUuid, postInfo);
            } else {
                String helpMsg = "";
                if (isBlank(postInfo.getMediaPath()))
                    helpMsg += "Send media file (photo, gif, video) for create post.\n\n";
                if (isBlank(postInfo.getCaption()))
                    helpMsg += "Send caption for create post. \nExample - '/caption your text with http://url and #hashtag.'";
                botExecutor.execute(rqUuid, new SendMessage(chatId, helpMsg));
            }
            return true;
        }
        return false;
    }

    private boolean checkContent(String rqUuid, Message message) {
        String text = message.text();
        URLInfo info = Utils.whatIsUrl(text);
        if (info != null
                && info.getContentType() != null
                && !info.getContentType().equals(ContentType.Text)
        ) {
            botExecutor.execute(rqUuid, new SendMessage(message.chat().id(), format("Detect '%s' URL - %s", info.getContentType().getTypeDit(), info.getUrl())));
            String savePath = downloadPath + info.getContentType().getTypeDit() + "//" + FilenameUtils.getName(info.getUrl());
            saveToDisk(rqUuid, info, savePath, message.chat().id(), info.getContentType());
            return true;
        }
        return false;
    }

    protected void savePhoto(MessageStatus messageStatus) {
        if (messageStatus.getIsSuccess()) {
            return;
        }

        mqSender.sendLog(messageStatus.getRqUuid(), LogLevel.DEBUG, "Check photo: {}", messageStatus);
        Message message = messageStatus.getMsg();
        if (nonNull(message) && nonNull(message.photo()) && isNotEmpty(message.photo())) {
            PhotoSize ps = getMaxPhotoSize(message.photo());
            if (ps != null) {
                saveFile(messageStatus.getRqUuid(), message, ps.fileId(), ContentType.Picture);
                messageStatus.setIsSuccess(true);
            }
        }
    }

    protected void saveDocs(MessageStatus messageStatus) {
        if (messageStatus.getIsSuccess()) {
            return;
        }

        mqSender.sendLog(messageStatus.getRqUuid(), LogLevel.DEBUG, "Check document: {}", messageStatus);
        Message message = messageStatus.getMsg();
        if (nonNull(message) && nonNull(message.document())) {
            saveFile(messageStatus.getRqUuid(), message, message.document().fileId(), ContentType.Doc);
            messageStatus.setIsSuccess(true);
        }
    }

    protected void saveVideo(MessageStatus messageStatus) {
        if (messageStatus.getIsSuccess()) {
            return;
        }

        mqSender.sendLog(messageStatus.getRqUuid(), LogLevel.DEBUG, "Check video: {}", messageStatus);
        Message message = messageStatus.getMsg();
        if (nonNull(message) && nonNull(message.video())) {
            saveFile(messageStatus.getRqUuid(), message, message.video().fileId(), ContentType.Video);
            messageStatus.setIsSuccess(true);
        }
    }

    protected void saveAnimation(MessageStatus messageStatus) {
        if (messageStatus.getIsSuccess()) {
            return;
        }

        mqSender.sendLog(messageStatus.getRqUuid(), LogLevel.DEBUG, "Check gif (animation): {}", messageStatus);
        Message message = messageStatus.getMsg();
        if (nonNull(message) && nonNull(message.animation())) {
            saveFile(messageStatus.getRqUuid(), message, message.animation().fileId(), ContentType.Gif);
            messageStatus.setIsSuccess(true);
        }
    }

    private void saveFile(String rqUuid, Message message, String fileId, ContentType typeDir) {
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
                                rqUuid,
                                URLInfo.builder().contentType(typeDir).url(bot.getFullFilePath(getFileResponse.file())).build(),
                                savePath,
                                message.chat().id(),
                                typeDir
                        );
                        botExecutor.execute(rqUuid, new SendMessage(message.chat().id(), "Work with file: " + getFileResponse.file().filePath()));
                    } catch (Exception ex) {
                        mqSender.sendLog(rqUuid, LogLevel.ERROR, ExceptionUtils.getStackTrace(ex));
                        botExecutor.execute(rqUuid, new SendMessage(message.chat().id(), "Not save file: " + ExceptionUtils.getRootCauseMessage(ex)));
                    }
                },
                throwable -> {
                    mqSender.sendLog(rqUuid, LogLevel.ERROR, ExceptionUtils.getStackTrace(throwable));
                    bot.execute(new SendMessage(message.chat().id(), "Not save file: " + ExceptionUtils.getMessage(throwable)));
                });
    }

    private void saveToDisk(String rqUuid, URLInfo urlInfo, String saveDiskPath, Long chatId, ContentType typeDir) {
        LoadFileInfo fileInfo = LoadFileInfo.builder()
                .fileUrl(urlInfo.getUrl())
                .saveToPath(saveDiskPath)
                .fileName(cache.getFileName(chatId))
                .typeDir(urlInfo.getContentType().getTypeDit())
                .chatId(chatId)
                .fileType(urlInfo.getFileType())
                .mediaType(typeDir)
                .build();

        mqSender.sendLog(rqUuid, LogLevel.DEBUG, "Save file to disk: {}", fileInfo);
        mqSender.sendFileInfo(rqUuid, fileInfo);
    }

    protected void checkEndStatus(MessageStatus messageStatus) {
        if (messageStatus.getIsSuccess().equals(Boolean.FALSE)) {
            mqSender.sendLog(messageStatus.getRqUuid(), LogLevel.WARN, "[!!!] So, interesting situation: {}", messageStatus);
        }
    }

    protected void checkUser(MessageStatus status) {
        if (nonNull(status.getMsgUserId())) {
            userInfoService.getByUserId(status.getRqUuid(), status.getMsgUserId());
        } else {
            mqSender.sendLog(status.getRqUuid(), LogLevel.WARN, "User ID is null.");
            status.setIsSuccess(true);
        }
    }
}
