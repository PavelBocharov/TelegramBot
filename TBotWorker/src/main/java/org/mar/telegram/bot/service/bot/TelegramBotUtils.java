package org.mar.telegram.bot.service.bot;

import com.mar.dto.mq.LoadFileInfo;
import com.mar.dto.mq.URLInfo;
import com.mar.dto.rest.PostInfoDtoRs;
import com.mar.dto.tbot.ContentType;
import com.mar.dto.tbot.MessageDto;
import com.mar.dto.tbot.MessageStatus;
import com.mar.dto.tbot.PhotoSizeDto;
import com.mar.dto.tbot.TelegramMessage;
import com.mar.interfaces.cache.BotCache;
import com.mar.interfaces.mq.MQSender;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.GetFile;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.GetFileResponse;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.mapstruct.factory.Mappers;
import org.mar.telegram.bot.mapper.DBIntegrationMapper;
import org.mar.telegram.bot.service.bot.db.PostService;
import org.mar.telegram.bot.service.bot.db.UserService;
import org.mar.telegram.bot.utils.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import static com.mar.dto.mq.LogEvent.LogLevel.DEBUG;
import static com.mar.dto.mq.LogEvent.LogLevel.ERROR;
import static com.mar.dto.mq.LogEvent.LogLevel.WARN;
import static java.util.Objects.nonNull;
import static org.apache.commons.collections.CollectionUtils.isNotEmpty;
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
    protected TelegramBotSendUtils sendUtils;
    @Autowired
    protected BotExecutor botExecutor;
    @Autowired
    protected PostService postInfoService;
    @Autowired
    protected UserService userInfoService;

    private DBIntegrationMapper mapper = Mappers.getMapper(DBIntegrationMapper.class);

    protected MessageStatus createMessageStatus(TelegramMessage msg) {
        if (nonNull(msg)) {
            MessageStatus messageStatus = new MessageStatus();
            messageStatus.setRqUuid(msg.getRqUuid());
            if (nonNull(msg.getMsg())) {
                messageStatus.setMsg(msg.getMsg());
                messageStatus.setMsgUserId(msg.getMsg().getFromUserId());
            }
            if (nonNull(msg.getCallbackQuery())) {
                messageStatus.setQuery(msg.getCallbackQuery());
                messageStatus.setMsgUserId(messageStatus.getQuery().getFromUserId());
            }
            mqSender.sendLog(messageStatus.getRqUuid(), DEBUG, "Get message: {}", messageStatus);
            return messageStatus;
        }
        throw new RuntimeException("update is null");
    }

    protected void checkAdmin(MessageStatus messageStatus) {
        if (messageStatus.getIsSuccess()) {
            return;
        }
        mqSender.sendLog(messageStatus.getRqUuid(), DEBUG, "Check admin: {}", messageStatus);
        if (nonNull(messageStatus.getMsgUserId()) && !adminId.equals(messageStatus.getMsgUserId())) {
            messageStatus.setIsSuccess(true);
            mqSender.sendLog(messageStatus.getRqUuid(), DEBUG, "Check admin FAIL: {}", messageStatus);
        }
    }

    protected void checkCallbackQuery(MessageStatus messageStatus) {
        if (messageStatus.getIsSuccess()) {
            return;
        }

        mqSender.sendLog(messageStatus.getRqUuid(), DEBUG, "Check callback query: {}", messageStatus);
        messageStatus.setIsSuccess(
                callbackDataService.checkCallbackData(messageStatus.getRqUuid(), messageStatus.getQuery())
        );
    }

    protected void parsText(MessageStatus messageStatus) {
        if (messageStatus.getIsSuccess()) {
            return;
        }

        mqSender.sendLog(messageStatus.getRqUuid(), DEBUG, "Pars text: {}", messageStatus);
        MessageDto message = messageStatus.getMsg();
        if (nonNull(message)) {
            String text = message.getText();
            if (isNotBlank(text)) {
                text = text.trim();
                if (isNotBlank(text)) {
                    if (checkAction(messageStatus.getRqUuid(), message.getChatId(), text)) {
                        messageStatus.setIsSuccess(true);
                        return;
                    }
                    if (checkContent(messageStatus.getRqUuid(), message)) {
                        messageStatus.setIsSuccess(true);
                        return;
                    }

                    cache.setFileName(message.getChatId(), text);
                    botExecutor.sendMessage(messageStatus.getRqUuid(), new SendMessage(message.getChatId(), "New caption - " + text));
                    messageStatus.setIsSuccess(true);
                }
            }
        }
    }

    private boolean checkAction(String rqUuid, Long chatId, String text) {
        if (text.startsWith(ACTION_CAPTION)) {
            String caption = text.substring(ACTION_CAPTION.length()).trim();
            if (isNotBlank(caption)) {
                mqSender.sendLog(rqUuid, DEBUG, "Action: {}, caption: {}", ACTION_CAPTION, caption.replace("\n", " <br> "));
                PostInfoDtoRs postInfo = postInfoService.getNotSendPost(rqUuid);
                postInfo.setCaption(caption + "\n" + textLine);
                postInfoService.save(rqUuid, mapper.mapRsToRq(postInfo));
            } else {
                botExecutor.sendMessage(rqUuid, new SendMessage(chatId, "For save post caption write '" + ACTION_CAPTION + " post_text'"));
            }
            return true;
        }
        if (text.equals(ACTION_SEND_POST)) {
            PostInfoDtoRs postInfo = postInfoService.getNotSendPost(rqUuid);

            if (isNotBlank(postInfo.getMediaPath()) && isNotBlank(postInfo.getCaption())) {
                mqSender.sendLog(rqUuid, DEBUG, "Action: {}", ACTION_SEND_POST);
                sendUtils.sendPost(rqUuid, groupChatId, postInfo);
                postInfo = postInfoService.getNotSendPost(rqUuid);
                postInfo.setIsSend(true);
                postInfoService.save(rqUuid, mapper.mapRsToRq(postInfo));
            } else {
                String helpMsg = "";
                if (isBlank(postInfo.getMediaPath()))
                    helpMsg += "Send media file (photo, gif, video) for create post.\n\n";
                if (isBlank(postInfo.getCaption()))
                    helpMsg += "Send caption for create post. \nExample - '/caption your text with http://url and #hashtag.'";
                botExecutor.sendMessage(rqUuid, new SendMessage(chatId, helpMsg));
            }
            return true;
        }
        return false;
    }

    private boolean checkContent(String rqUuid, MessageDto message) {
        String text = message.getText();
        URLInfo info = Utils.whatIsUrl(text);
        if (info != null
                && info.getContentType() != null
                && !info.getContentType().equals(ContentType.Text)
        ) {
            botExecutor.sendMessage(
                    rqUuid,
                    new SendMessage(
                            message.getChatId(),
                            String.format("Detect '%s' URL - %s", info.getContentType().getTypeDir(),
                                    info.getUrl()
                            )
                    )
            );
            String savePath = downloadPath + info.getContentType().getTypeDir() + "//" + FilenameUtils.getName(info.getUrl());
            saveToDisk(rqUuid, info, savePath, message.getChatId(), info.getContentType());
            return true;
        }
        return false;
    }

    protected void savePhoto(MessageStatus messageStatus) {
        if (messageStatus.getIsSuccess()) {
            return;
        }

        mqSender.sendLog(messageStatus.getRqUuid(), DEBUG, "Check photo: {}", messageStatus);
        MessageDto message = messageStatus.getMsg();
        if (nonNull(message) && isNotEmpty(message.getPhotoSizeList())) {
            PhotoSizeDto ps = getMaxPhotoSize(message.getPhotoSizeList());
            if (ps != null) {
                saveFile(messageStatus.getRqUuid(), message, ps.getFileId(), ContentType.Picture);
                messageStatus.setIsSuccess(true);
            }
        }
    }

    protected void saveDocs(MessageStatus messageStatus) {
        if (messageStatus.getIsSuccess()) {
            return;
        }

        mqSender.sendLog(messageStatus.getRqUuid(), DEBUG, "Check document: {}", messageStatus);
        MessageDto message = messageStatus.getMsg();
        if (nonNull(message) && isNotBlank(message.getDocumentFileId())) {
            saveFile(messageStatus.getRqUuid(), message, message.getDocumentFileId(), ContentType.Doc);
            messageStatus.setIsSuccess(true);
        }
    }

    protected void saveVideo(MessageStatus messageStatus) {
        if (messageStatus.getIsSuccess()) {
            return;
        }

        mqSender.sendLog(messageStatus.getRqUuid(), DEBUG, "Check video: {}", messageStatus);
        MessageDto message = messageStatus.getMsg();
        if (nonNull(message) && isNotBlank(message.getVideoFileId())) {
            saveFile(messageStatus.getRqUuid(), message, message.getVideoFileId(), ContentType.Video);
            messageStatus.setIsSuccess(true);
        }
    }

    protected void saveAnimation(MessageStatus messageStatus) {
        if (messageStatus.getIsSuccess()) {
            return;
        }

        mqSender.sendLog(messageStatus.getRqUuid(), DEBUG, "Check gif (animation): {}", messageStatus);
        MessageDto message = messageStatus.getMsg();
        if (nonNull(message) && isNotBlank(message.getAnimationFileId())) {
            saveFile(messageStatus.getRqUuid(), message, message.getAnimationFileId(), ContentType.Gif);
            messageStatus.setIsSuccess(true);
        }
    }

    private void saveFile(String rqUuid, MessageDto message, String fileId, ContentType typeDir) {
        GetFile request = new GetFile(fileId);

        botExecutor.sendPost(
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
                                message.getChatId(),
                                typeDir
                        );
                        botExecutor.sendMessage(rqUuid, new SendMessage(message.getChatId(), "Work with file: " + getFileResponse.file().filePath()));
                    } catch (Exception ex) {
                        mqSender.sendLog(rqUuid, ERROR, ExceptionUtils.getStackTrace(ex));
                        botExecutor.sendMessage(rqUuid, new SendMessage(message.getChatId(), "Not save file: " + ExceptionUtils.getRootCauseMessage(ex)));
                    }
                },
                throwable -> {
                    mqSender.sendLog(rqUuid, ERROR, ExceptionUtils.getStackTrace(throwable));
                    bot.execute(new SendMessage(message.getChatId(), "Not save file: " + ExceptionUtils.getMessage(throwable)));
                });
    }

    private void saveToDisk(String rqUuid, URLInfo urlInfo, String saveDiskPath, Long chatId, ContentType typeDir) {
        LoadFileInfo fileInfo = LoadFileInfo.builder()
                .fileUrl(urlInfo.getUrl())
                .saveToPath(saveDiskPath)
                .fileName(cache.getFileName(chatId))
                .typeDir(urlInfo.getContentType().getTypeDir())
                .chatId(chatId)
                .fileType(urlInfo.getFileType())
                .mediaType(typeDir)
                .build();

        mqSender.sendLog(rqUuid, DEBUG, "Save file to disk: {}", fileInfo);
        mqSender.sendFileInfo(rqUuid, fileInfo);
    }

    protected void checkEndStatus(MessageStatus messageStatus) {
        if (messageStatus.getIsSuccess().equals(Boolean.FALSE)) {
            mqSender.sendLog(messageStatus.getRqUuid(), WARN, "[!!!] So, interesting situation: {}", messageStatus);
        }
    }

    protected void checkUser(MessageStatus status) {
        if (nonNull(status.getMsgUserId())) {
            userInfoService.getByUserId(status.getRqUuid(), status.getMsgUserId());
        } else {
            mqSender.sendLog(status.getRqUuid(), WARN, "User ID is null.");
            status.setIsSuccess(true);
        }
    }
}
