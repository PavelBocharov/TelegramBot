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
import org.apache.commons.io.FileUtils;
import org.mar.telegram.bot.db.entity.ActionEnum;
import org.mar.telegram.bot.db.entity.ActionPost;
import org.mar.telegram.bot.db.entity.PostInfo;
import org.mar.telegram.bot.db.entity.UserInfo;
import org.mar.telegram.bot.service.bot.db.ActionService;
import org.mar.telegram.bot.service.bot.db.PostService;
import org.mar.telegram.bot.service.bot.db.UserService;
import org.mar.telegram.bot.utils.ContentType;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.UUID;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.mar.telegram.bot.db.entity.ActionEnum.*;
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
            PostInfo postInfo = postService.getByChatIdAndMessageId(chatId, messageId);
            UserInfo user = userService.getByUserId(userId);

            ActionPost actionPost = actionService.getByPostIdAndUserInfoId(postInfo, user);
            actionPost.setAction(ActionEnum.getActionByCode(callbackQuery.data()));
            actionService.save(actionPost);

            InputMedia media = getInputMedia(callbackQuery.message());
            EditMessageMedia msg = new EditMessageMedia(chatId, messageId, media).replyMarkup(getReplyKeyboard(postInfo.getId()));

            botExecutor.execute(msg);

            return true;
        }
        return false;
    }

    public void sendPost(Long groupChatID, PostInfo postInfo) {
        switch (postInfo.getType()) {
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

    private void sendVideoPost(Long chatId, PostInfo postInfo) {
        SendVideo msg = new SendVideo(chatId, new File(postInfo.getMediaPath()));
        msg.caption(postInfo.getCaption());
        msg.replyMarkup(getReplyKeyboard(postInfo.getId()));
        botExecutor.execute(msg);
    }

    private void sendGifPost(Long chatId, PostInfo postInfo) {
        SendAnimation msg = new SendAnimation(chatId, new File(postInfo.getMediaPath()));
        msg.caption(postInfo.getCaption());
        msg.replyMarkup(getReplyKeyboard(postInfo.getId()));
        botExecutor.execute(msg);
    }

    private void sendPhotoPost(Long chatId, PostInfo postInfo) {
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
            type = ContentType.Video;
        } else if (nonNull(msg.animation())) {
            request = new GetFile(msg.animation().fileId());
            type = ContentType.Gif;
        } else if (nonNull(msg.document())) {
            request = new GetFile(msg.document().fileId());
            type = ContentType.Gif;
        } else if (nonNull(msg.photo())) {
            PhotoSize ps = getMaxPhotoSize(msg.photo());
            if (nonNull(ps)) {
                request = new GetFile(ps.fileId());
                type = ContentType.Picture;
            }
        }

        if (isNull(type)) {
            throw new RuntimeException("Unsupport type.");
        }

        GetFileResponse rs = (GetFileResponse) botExecutor.execute(request);
        return getInputMedia(bot.getFullFilePath(rs.file()), type, msg.caption());

    }

    private InputMedia getInputMedia(String filePath, ContentType mediaType, String caption) {
        byte[] mediaFile;
        File file;
        try {
            file = new File(UUID.randomUUID().toString());
            FileUtils.copyURLToFile(new URL(filePath), file);
            mediaFile = FileUtils.readFileToByteArray(file);

            InputMedia inputMedia = getInputMediaByType(mediaType, mediaFile);
            inputMedia.caption(caption);

            FileUtils.delete(file);
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
        return new InlineKeyboardMarkup(
                new InlineKeyboardButton[]{
                        new InlineKeyboardButton(getButtonCaption(FIRE_HEART, postId)).callbackData(FIRE_HEART.getCallbackData()),
                        new InlineKeyboardButton(getButtonCaption(DEVIL, postId)).callbackData(DEVIL.getCallbackData()),
                        new InlineKeyboardButton(getButtonCaption(COOL, postId)).callbackData(COOL.getCallbackData()),
                        new InlineKeyboardButton(getButtonCaption(BORING, postId)).callbackData(BORING.getCallbackData())
                });
    }

    private String getButtonCaption(ActionEnum actionEnum, Long postId) {
        Long count = actionService.countByPostIdAndAction(postId, actionEnum);

        return nonNull(count) && count > 0
                ? actionEnum.getCode() + " " + count
                : actionEnum.getCode();
    }

}
