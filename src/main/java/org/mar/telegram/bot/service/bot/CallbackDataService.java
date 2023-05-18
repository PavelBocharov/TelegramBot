package org.mar.telegram.bot.service.bot;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.CallbackQuery;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.request.*;
import com.pengrad.telegrambot.request.*;
import com.pengrad.telegrambot.response.GetFileResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.mar.telegram.bot.db.entity.PostInfo;
import org.mar.telegram.bot.utils.ContentType;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.UUID;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

@Slf4j
@Service
@RequiredArgsConstructor
public class CallbackDataService {

    private final TelegramBot bot;
    private final BotExecutor botExecutor;

    public void checkCallbackData(TelegramBot bot, CallbackQuery callbackQuery) {
        InputMedia media = getInputMedia(callbackQuery.message());
        EditMessageMedia msg = new EditMessageMedia(
                callbackQuery.message().chat().id(),
                callbackQuery.message().messageId(),
                media
        ).replyMarkup(getReplyKeyboard());

        botExecutor.execute(msg);
    }

    public void sendPost(Long groupChatID, PostInfo postInfo) {
        switch (postInfo.getType()) {
            case Video -> {
                sendVideoPost(groupChatID, postInfo.getMediaPath(), postInfo.getCaption());
                break;
            }
            case Gif -> {
                sendGifPost(groupChatID, postInfo.getMediaPath(), postInfo.getCaption());
                break;
            }
            case Picture -> {
                sendPhotoPost(groupChatID, postInfo.getMediaPath(), postInfo.getCaption());
                break;
            }
        }
    }

    private void sendVideoPost(Long chatId, String filePath, String caption) {
        SendVideo msg = new SendVideo(chatId, new File(filePath));
        msg.caption(caption);
        msg.replyMarkup(getReplyKeyboard());
        botExecutor.execute(msg);
    }

    private void sendGifPost(Long chatId, String filePath, String caption) {
        SendAnimation msg = new SendAnimation(chatId, new File(filePath));
        msg.caption(caption);
        msg.replyMarkup(getReplyKeyboard());
        botExecutor.execute(msg);
    }

    private void sendPhotoPost(Long chatId, String filePath, String caption) {
        SendPhoto msg = new SendPhoto(chatId, new File(filePath));
        msg.caption(caption);
        msg.replyMarkup(getReplyKeyboard());
        botExecutor.execute(msg);
    }

    private InputMedia getInputMedia(Message msg) {
        GetFile request = new GetFile(msg.video().fileId());
        GetFileResponse rs = (GetFileResponse) botExecutor.execute(request);

        ContentType type = null;
        if (nonNull(msg.video())) {
            type = ContentType.Video;
        }
        else if (nonNull(msg.animation())) {
            type = ContentType.Gif;
        } else if (nonNull(msg.photo())) {
            type = ContentType.Picture;
        }

        if (isNull(type)) {
            throw new RuntimeException("Unsupport type.");
        }

        return getInputMedia(bot.getFullFilePath(rs.file()), type, msg.caption() + "\n Edit msd: " + new Date());

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

//    private InputMedia getInputMedia(String filePath, ContentType mediaType, String actress, String tags) {
//        return getInputMedia(filePath, mediaType, getCaption(actress, tags));
//    }
//
//    private String getCaption(String actress, String tags) {
//        return format("Actress: %s\n\nTags: %s\nGroup: @SquzzyPizzy", actress, tags);
//    }

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

    private InlineKeyboardMarkup getReplyKeyboard() {
        return new InlineKeyboardMarkup(
                new InlineKeyboardButton[]{
                        new InlineKeyboardButton("❤️\u200D\uD83D\uDD25").callbackData("fire"),
                        new InlineKeyboardButton("\uD83D\uDE08").callbackData("devil"),
                        new InlineKeyboardButton("\uD83D\uDE31").callbackData("0_0"),
                        new InlineKeyboardButton("\uD83D\uDE15").callbackData("0-0")
                });
    }

}
