package org.mar.telegram.bot.service.bot;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.CallbackQuery;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.PhotoSize;
import com.pengrad.telegrambot.model.request.*;
import com.pengrad.telegrambot.request.*;
import com.pengrad.telegrambot.response.GetFileResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mar.telegram.bot.service.bot.db.ActionService;
import org.mar.telegram.bot.service.bot.db.PostService;
import org.mar.telegram.bot.service.bot.db.UserService;
import org.mar.telegram.bot.service.db.dto.ActionEnum;
import org.mar.telegram.bot.service.db.dto.ActionPostDto;
import org.mar.telegram.bot.service.db.dto.PostInfoDto;
import org.mar.telegram.bot.service.db.dto.UserDto;
import org.mar.telegram.bot.utils.ContentType;
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
import static org.mar.telegram.bot.service.db.dto.ActionEnum.*;
import static org.mar.telegram.bot.utils.ContentType.*;
import static org.mar.telegram.bot.utils.Utils.getMaxPhotoSize;

@Slf4j
@Service
@RequiredArgsConstructor
public class CallbackDataService {

    private final TelegramBot bot;
    private final BotExecutor botExecutor;

    private final UserService userService;
    private final PostService postService;
    private final ActionService actionService;

    public boolean checkCallbackData(CallbackQuery callbackQuery) {
        if (nonNull(callbackQuery)) {
            Long chatId = callbackQuery.message().chat().id();
            Integer messageId = callbackQuery.message().messageId();
            Long userId = callbackQuery.from().id();

            // create/update user action
            PostInfoDto postInfo = postService.getByChatIdAndMessageId(chatId, messageId);
            UserDto user = userService.getByUserId(userId);

            ActionPostDto actionPost = actionService.getByPostIdAndUserInfoId(postInfo.getId(), user.getId());
            actionPost.setActionCallbackData(callbackQuery.data());
            actionService.save(actionPost);

            InputMedia media = getInputMedia(callbackQuery.message());
            EditMessageMedia msg = new EditMessageMedia(chatId, messageId, media).replyMarkup(getReplyKeyboard(postInfo.getId()));

            botExecutor.execute(msg);

            return true;
        }
        return false;
    }

    public void sendPost(Long groupChatID, PostInfoDto postInfo) {
        ContentType postType = ContentType.getTypeByDir(postInfo.getTypeDir());
        switch (postType) {
            case Video -> {
                sendVideoPost(groupChatID, postInfo);
                break;
            }
            case Gif -> {
                sendGifPost(groupChatID, postInfo);
                break;
            }
            case Picture -> {
                sendPhotoPost(groupChatID, postInfo);
                break;
            }
        }
    }

    private void sendVideoPost(Long chatId, PostInfoDto postInfo) {
        SendVideo msg = new SendVideo(chatId, new File(postInfo.getMediaPath()));
        msg.caption(postInfo.getCaption());
        msg.replyMarkup(getReplyKeyboard(postInfo.getId()));
        botExecutor.execute(msg);
    }

    private void sendGifPost(Long chatId, PostInfoDto postInfo) {
        SendAnimation msg = new SendAnimation(chatId, new File(postInfo.getMediaPath()));
        msg.caption(postInfo.getCaption());
        msg.replyMarkup(getReplyKeyboard(postInfo.getId()));
        botExecutor.execute(msg);
    }

    private void sendPhotoPost(Long chatId, PostInfoDto postInfo) {
        SendPhoto msg = new SendPhoto(chatId, new File(postInfo.getMediaPath()));
        msg.caption(postInfo.getCaption());
        msg.replyMarkup(getReplyKeyboard(postInfo.getId()));
        botExecutor.execute(msg);
    }

    private InputMedia getInputMedia(Message msg) {
        GetFile request = null;
        ContentType type = null;

        if (nonNull(msg.video())) {
            request = new GetFile(msg.video().fileId());
            type = Video;
        } else if (nonNull(msg.animation())) {
            request = new GetFile(msg.animation().fileId());
            type = Gif;
        } else if (nonNull(msg.document())) {
            request = new GetFile(msg.document().fileId());
            type = Gif;
        } else if (nonNull(msg.photo())) {
            PhotoSize ps = getMaxPhotoSize(msg.photo());
            if (nonNull(ps)) {
                request = new GetFile(ps.fileId());
                type = Picture;
            }
        }

        if (isNull(type)) {
            throw new RuntimeException("Unsupport type.");
        }

        GetFileResponse rs = (GetFileResponse) botExecutor.execute(request);
        return getInputMedia(bot.getFullFilePath(rs.file()), type, msg.caption());

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
            case Gif -> {
                return new InputMediaAnimation(file);
            }
            case Picture -> {
                return new InputMediaPhoto(file);
            }
            case Video -> {
                return new InputMediaVideo(file);
            }
        }
        return null;
    }

    private InlineKeyboardMarkup getReplyKeyboard(Long postId) {
        Map<ActionEnum, Long> actionCountMap = actionService.countByPostIdAndAction(postId);

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
