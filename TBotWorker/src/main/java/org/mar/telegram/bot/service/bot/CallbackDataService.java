package org.mar.telegram.bot.service.bot;

import com.mar.dto.rest.ActionPostDtoRs;
import com.mar.dto.rest.PostInfoDtoRs;
import com.mar.dto.rest.UserDtoRs;
import com.mar.dto.tbot.CallbackQueryDto;
import com.pengrad.telegrambot.request.EditMessageReplyMarkup;
import lombok.RequiredArgsConstructor;
import org.mar.telegram.bot.service.bot.db.ActionService;
import org.mar.telegram.bot.service.bot.db.PostService;
import org.mar.telegram.bot.service.bot.db.UserService;
import com.mar.interfaces.mq.MQSender;
import org.springframework.stereotype.Service;

import static com.mar.dto.mq.LogEvent.LogLevel.DEBUG;
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
            mqSender.sendLog(rqUuid, DEBUG, "Callback query: {}", query);
            Long chatId = query.getMsgChatId();
            Integer messageId = query.getMessageId();
            Long userId = query.getFromUserId();

            // create/update user action
            PostInfoDtoRs postInfo = postService.getByChatIdAndMessageId(rqUuid, chatId, messageId);
            UserDtoRs user = userService.getByUserId(rqUuid, userId);

            ActionPostDtoRs actionPost = actionService.getByPostIdAndUserInfoId(rqUuid, postInfo.getId(), user.getId());
            actionPost.setActionCallbackData(query.getActionCallbackData());
            actionService.save(rqUuid, actionPost);

            EditMessageReplyMarkup msg = new EditMessageReplyMarkup(chatId, messageId);
            msg.replyMarkup(sendUtils.getReplyKeyboard(rqUuid, postInfo.getId()));

            botExecutor.sendMessage(rqUuid, msg);

            return true;
        }
        return false;
    }

}
