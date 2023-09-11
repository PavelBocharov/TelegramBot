package org.mar.telegram.bot.service.bot;

import com.pengrad.telegrambot.request.EditMessageReplyMarkup;
import lombok.RequiredArgsConstructor;
import org.mar.telegram.bot.service.bot.db.ActionService;
import org.mar.telegram.bot.service.bot.db.PostService;
import org.mar.telegram.bot.service.bot.db.UserService;
import org.mar.telegram.bot.service.bot.dto.CallbackQueryDto;
import org.mar.telegram.bot.service.db.dto.ActionPostDtoRs;
import org.mar.telegram.bot.service.db.dto.PostInfoDtoRs;
import org.mar.telegram.bot.service.db.dto.UserDto;
import org.mar.telegram.bot.service.jms.MQSender;
import org.springframework.boot.logging.LogLevel;
import org.springframework.stereotype.Service;

import static java.util.Objects.nonNull;

@Service
@RequiredArgsConstructor
public class CallbackDataService {

    private final TelegramBotSendUtils sendUtils;
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
            PostInfoDtoRs postInfo = postService.getByChatIdAndMessageId(rqUuid, chatId, messageId);
            UserDto user = userService.getByUserId(rqUuid, userId);

            ActionPostDtoRs actionPost = actionService.getByPostIdAndUserInfoId(rqUuid, postInfo.getId(), user.getId());
            actionPost.setActionCallbackData(query.getActionCallbackData());
            actionService.save(rqUuid, actionPost);

            EditMessageReplyMarkup msg = new EditMessageReplyMarkup(chatId, messageId);
            msg.replyMarkup(sendUtils.getReplyKeyboard(rqUuid, postInfo.getId()));

            botExecutor.execute(rqUuid, msg);

            return true;
        }
        return false;
    }

}
