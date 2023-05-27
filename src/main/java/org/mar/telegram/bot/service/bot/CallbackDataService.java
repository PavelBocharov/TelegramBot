package org.mar.telegram.bot.service.bot;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.request.*;
import com.pengrad.telegrambot.request.EditMessageMedia;
import com.pengrad.telegrambot.request.GetFile;
import com.pengrad.telegrambot.request.SendPhoto;
import com.pengrad.telegrambot.request.SendVideo;
import com.pengrad.telegrambot.response.GetFileResponse;
import lombok.RequiredArgsConstructor;
import org.mar.telegram.bot.service.bot.db.ActionService;
import org.mar.telegram.bot.service.bot.db.PostService;
import org.mar.telegram.bot.service.bot.db.UserService;
import org.mar.telegram.bot.service.bot.dto.CallbackQueryDto;
import org.mar.telegram.bot.service.db.dto.ActionEnum;
import org.mar.telegram.bot.service.db.dto.ActionPostDto;
import org.mar.telegram.bot.service.db.dto.PostInfoDto;
import org.mar.telegram.bot.service.db.dto.UserDto;
import org.mar.telegram.bot.service.jms.MQSender;
import org.mar.telegram.bot.utils.ContentType;
import org.springframework.boot.logging.LogLevel;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Map;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.mar.telegram.bot.service.db.dto.ActionEnum.*;
import static org.mar.telegram.bot.utils.ContentType.*;

@Service
@RequiredArgsConstructor
public class CallbackDataService {

    private final TelegramBot bot;
    private final BotExecutor botExecutor;

    private final UserService userService;
    private final PostService postService;
    private final ActionService actionService;
    private final MQSender mqSender;

    public boolean checkCallbackData(String rqUuid, CallbackQueryDto query) {
        if (nonNull(query)) {
            mqSender.sendLog(rqUuid, LogLevel.DEBUG, "Callback query: {}", query);
            Long chatId = query.getMsgChatId();
            Integer messageId = query.getMessageId();
            Long userId = query.getFromUserId();

            // create/update user action
            PostInfoDto postInfo = postService.getByChatIdAndMessageId(rqUuid, chatId, messageId);
            UserDto user = userService.getByUserId(rqUuid, userId);

            ActionPostDto actionPost = actionService.getByPostIdAndUserInfoId(rqUuid, postInfo.getId(), user.getId());
            actionPost.setActionCallbackData(query.getActionCallbackData());
            actionService.save(rqUuid, actionPost);

            InputMedia media = getInputMedia(rqUuid, query);
            EditMessageMedia msg = new EditMessageMedia(chatId, messageId, media)
                    .replyMarkup(getReplyKeyboard(rqUuid, postInfo.getId()));

            botExecutor.execute(rqUuid, msg);

            return true;
        }
        return false;
    }

    public void sendPost(String rqUuid, Long groupChatID, PostInfoDto postInfo) {
        mqSender.sendLog(rqUuid, LogLevel.DEBUG, "Send post to chat ID: {}, post info: {}", groupChatID, postInfo);
        ContentType postType = ContentType.getTypeByDir(postInfo.getTypeDir());
        switch (postType) {
            case Video, Gif -> {
                sendVideoPost(rqUuid, groupChatID, postInfo);
                break;
            }
            case Picture -> {
                sendPhotoPost(rqUuid, groupChatID, postInfo);
                break;
            }
        }
    }

    private void sendVideoPost(String rqUuid, Long chatId, PostInfoDto postInfo) {
        SendVideo msg = new SendVideo(chatId, new File(postInfo.getMediaPath()));
        msg.caption(postInfo.getCaption());
        msg.replyMarkup(getReplyKeyboard(rqUuid, postInfo.getId()));
        botExecutor.execute(rqUuid, msg);
    }

    private void sendPhotoPost(String rqUuid, Long chatId, PostInfoDto postInfo) {
        SendPhoto msg = new SendPhoto(chatId, new File(postInfo.getMediaPath()));
        msg.caption(postInfo.getCaption());
        msg.replyMarkup(getReplyKeyboard(rqUuid, postInfo.getId()));
        botExecutor.execute(rqUuid, msg);
    }

    private InputMedia getInputMedia(String rqUuid, CallbackQueryDto query) {
        GetFile request = null;
        ContentType type = null;

        if (isNotBlank(query.getVideoFieldId())) {
            request = new GetFile(query.getVideoFieldId());
            type = Video;
        } else if (isNotBlank(query.getAnimationFieldId())) {
            request = new GetFile(query.getAnimationFieldId());
            type = Gif;
        } else if (isNotBlank(query.getDocumentFieldId())) {
            request = new GetFile(query.getDocumentFieldId());
            type = Gif;
        } else if (isNotBlank(query.getPhotoFieldId())) {
            request = new GetFile(query.getPhotoFieldId());
            type = Picture;
        }

        if (isNull(type)) {
            throw new RuntimeException("Unsupport type.");
        }

        GetFileResponse rs = (GetFileResponse) botExecutor.execute(rqUuid, request);
        return getInputMedia(bot.getFullFilePath(rs.file()), type, query.getMessageCaption());

    }

    private InputMedia getInputMedia(String filePath, ContentType mediaType, String caption) {
        try {
            URLConnection connection = new URL(filePath).openConnection();
            InputStream byteStream = connection.getInputStream();
            byte[] allB = byteStream.readAllBytes();
            byteStream.close();

            InputMedia inputMedia = getInputMediaByType(mediaType, allB);
            inputMedia.caption(caption);
            return inputMedia;
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private InputMedia getInputMediaByType(ContentType mediaType, byte[] file) {
        switch (mediaType) {
            case Video, Gif -> {
                return new InputMediaVideo(file);
            }
            case Picture -> {
                return new InputMediaPhoto(file);
            }
        }
        return null;
    }

    private InlineKeyboardMarkup getReplyKeyboard(String rqUuid, Long postId) {
        Map<ActionEnum, Long> actionCountMap = actionService.countByPostIdAndAction(rqUuid, postId);

        return new InlineKeyboardMarkup(
                new InlineKeyboardButton[]{
                        new InlineKeyboardButton(getButtonCaption(FIRE_HEART, actionCountMap))
                                .callbackData(FIRE_HEART.getCallbackData()),
                        new InlineKeyboardButton(getButtonCaption(DEVIL, actionCountMap))
                                .callbackData(DEVIL.getCallbackData()),
                        new InlineKeyboardButton(getButtonCaption(COOL, actionCountMap))
                                .callbackData(COOL.getCallbackData()),
                        new InlineKeyboardButton(getButtonCaption(BORING, actionCountMap))
                                .callbackData(BORING.getCallbackData())
                });
    }

    private String getButtonCaption(ActionEnum actionEnum, Map<ActionEnum, Long> actionCountMap) {
        Long count = actionCountMap.get(actionEnum);
        return nonNull(count) && count > 0
                ? actionEnum.getCode() + " " + count
                : actionEnum.getCode();
    }

}
