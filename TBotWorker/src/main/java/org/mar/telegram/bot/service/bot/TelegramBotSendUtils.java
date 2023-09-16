package org.mar.telegram.bot.service.bot;

import com.pengrad.telegrambot.model.request.InlineKeyboardButton;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
import com.pengrad.telegrambot.request.SendPhoto;
import com.pengrad.telegrambot.request.SendVideo;
import lombok.RequiredArgsConstructor;
import org.mar.telegram.bot.service.bot.db.ActionService;
import org.mar.telegram.bot.service.db.dto.ActionEnum;
import org.mar.telegram.bot.service.db.dto.PostInfoDtoRs;
import org.mar.telegram.bot.service.jms.MQSender;
import org.mar.telegram.bot.utils.ContentType;
import org.springframework.boot.logging.LogLevel;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.Map;

import static java.util.Objects.nonNull;
import static org.mar.telegram.bot.service.db.dto.ActionEnum.*;
import static org.mar.telegram.bot.utils.ContentType.Doc;
import static org.mar.telegram.bot.utils.Utils.getTypeByType;

@Service
@RequiredArgsConstructor
public class TelegramBotSendUtils {

    private final BotExecutor botExecutor;
    private final ActionService actionService;
    private final MQSender mqSender;

    public void sendPost(String rqUuid, Long groupChatID, PostInfoDtoRs postInfo) {
        mqSender.sendLog(rqUuid, LogLevel.DEBUG, "Send post to chat ID: {}, post info: {}", groupChatID, postInfo);
        ContentType postType = ContentType.getTypeByDir(postInfo.getTypeDir());
        if (Doc.equals(postType)) {
            postType = getTypeByType(postInfo.getMediaPath());
        }
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

    private void sendVideoPost(String rqUuid, Long chatId, PostInfoDtoRs postInfo) {
        SendVideo msg = new SendVideo(chatId, new File(postInfo.getMediaPath()));
        msg.caption(postInfo.getCaption());
        msg.replyMarkup(getReplyKeyboard(rqUuid, postInfo.getId()));
        botExecutor.execute(rqUuid, msg);
    }

    private void sendPhotoPost(String rqUuid, Long chatId, PostInfoDtoRs postInfo) {
        SendPhoto msg = new SendPhoto(chatId, new File(postInfo.getMediaPath()));
        msg.caption(postInfo.getCaption());
        msg.replyMarkup(getReplyKeyboard(rqUuid, postInfo.getId()));
        botExecutor.execute(rqUuid, msg);
    }

    public InlineKeyboardMarkup getReplyKeyboard(String rqUuid, Long postId) {
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
