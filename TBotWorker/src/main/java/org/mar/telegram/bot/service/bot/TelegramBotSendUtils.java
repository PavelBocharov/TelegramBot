package org.mar.telegram.bot.service.bot;

import com.mar.dto.mq.LogEvent;
import com.mar.dto.rest.PostInfoDtoRs;
import com.mar.dto.tbot.ActionEnum;
import com.mar.dto.tbot.ContentType;
import com.pengrad.telegrambot.model.request.InlineKeyboardButton;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.SendPhoto;
import com.pengrad.telegrambot.request.SendVideo;
import lombok.RequiredArgsConstructor;
import org.mar.telegram.bot.service.bot.db.ActionService;
import org.mar.telegram.bot.service.logger.LoggerService;
import org.mar.telegram.bot.utils.Utils;
import org.mar.telegram.bot.utils.data.WatermarkInfo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.mar.dto.tbot.ActionEnum.BAD;
import static com.mar.dto.tbot.ActionEnum.BORING;
import static com.mar.dto.tbot.ActionEnum.DEVIL;
import static com.mar.dto.tbot.ActionEnum.FIRE_HEART;
import static com.mar.dto.tbot.ContentType.Doc;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.mar.telegram.bot.utils.Utils.getTypeByType;

@Service
@RequiredArgsConstructor
public class TelegramBotSendUtils {

    private final BotExecutor botExecutor;
    private final ActionService actionService;
    private final LoggerService loggerService;
    private final Utils utils;

    @Value("${application.bot.watermark.image.path:}")
    private String watermarkImagePath;

    @Value("${application.bot.watermark.image.size.x:}")
    private Integer watermarkImageSizeX;

    @Value("${application.bot.watermark.image.size.y:}")
    private Integer watermarkImageSizeY;

    @Value("${application.bot.watermark.text.value:}")
    private String watermarkTextValue;

    @Value("${application.group.chat.chatLink:}")
    private String chatLink;

    public void sendPost(String rqUuid, Long groupChatID, PostInfoDtoRs postInfo) {
        loggerService.sendLog(rqUuid, LogEvent.LogLevel.DEBUG, "Send post to chat ID: {}, post info: {}", groupChatID, postInfo);
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
                String newImageWithWatermark = utils.addWatermark(rqUuid, postInfo.getMediaPath(), getWatermarkInfo(rqUuid));
                if (isNotBlank(newImageWithWatermark)) {
                    postInfo.setMediaPath(newImageWithWatermark);
                    loggerService.sendLog(rqUuid, LogEvent.LogLevel.DEBUG, "Set new image path: {}", newImageWithWatermark);
                }

                sendPhotoPost(rqUuid, groupChatID, postInfo);
                break;
            }
        }
    }

    private void sendVideoPost(String rqUuid, Long chatId, PostInfoDtoRs postInfo) {
        SendVideo msg = new SendVideo(chatId, new File(postInfo.getMediaPath()));
        msg.caption(postInfo.getCaption());
        msg.replyMarkup(getReplyKeyboard(rqUuid, postInfo.getId()));
        msg.parseMode(ParseMode.HTML);
        botExecutor.sendPost(rqUuid, postInfo.getId(), msg);
    }

    private void sendPhotoPost(String rqUuid, Long chatId, PostInfoDtoRs postInfo) {
        SendPhoto msg = new SendPhoto(chatId, new File(postInfo.getMediaPath()));
        msg.caption(postInfo.getCaption());
        msg.replyMarkup(getReplyKeyboard(rqUuid, postInfo.getId()));
        msg.parseMode(ParseMode.HTML);
        botExecutor.sendPost(rqUuid, postInfo.getId(), msg);
    }

    public InlineKeyboardMarkup getReplyKeyboard(String rqUuid, Long postId) {
        Map<ActionEnum, Long> actionCountMap = actionService.countByPostIdAndAction(rqUuid, postId);
        ArrayList<InlineKeyboardButton> btns = new ArrayList<InlineKeyboardButton>(
                List.of(new InlineKeyboardButton[]{
                        new InlineKeyboardButton(getButtonCaption(FIRE_HEART, actionCountMap))
                                .callbackData(FIRE_HEART.getCallbackData()),
                        new InlineKeyboardButton(getButtonCaption(DEVIL, actionCountMap))
                                .callbackData(DEVIL.getCallbackData()),
                        new InlineKeyboardButton(getButtonCaption(BORING, actionCountMap))
                                .callbackData(BORING.getCallbackData()),
                        new InlineKeyboardButton(getButtonCaption(BAD, actionCountMap))
                                .callbackData(BAD.getCallbackData())
                }));

        if (isNotBlank(chatLink)) {
            btns.add(new InlineKeyboardButton("\uD83D\uDCAC").url(chatLink));
        }

        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup(btns.toArray(new InlineKeyboardButton[0]));
        return keyboardMarkup;
    }

    private String getButtonCaption(ActionEnum actionEnum, Map<ActionEnum, Long> actionCountMap) {
        Long count = actionCountMap.get(actionEnum);
        return nonNull(count) && count > 0
                ? actionEnum.getCode() + " " + count
                : actionEnum.getCode();
    }

    private WatermarkInfo getWatermarkInfo(String rqUuid) {
        WatermarkInfo watermarkInfo = new WatermarkInfo()
                .withText(watermarkTextValue)
                .withImagePath(watermarkImagePath)
                .withImageSizeX(watermarkImageSizeX)
                .withImageSizeY(watermarkImageSizeY);
        loggerService.sendLog(rqUuid, LogEvent.LogLevel.DEBUG, "Load watermark info: {}", watermarkInfo);
        return watermarkInfo;
    }


}
