package org.mar.telegram.bot.service.bot;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.BaseRequest;
import com.pengrad.telegrambot.response.BaseResponse;
import com.pengrad.telegrambot.response.SendResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.mar.telegram.bot.db.entity.PostInfo;
import org.mar.telegram.bot.db.service.PostInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.function.Consumer;

import static java.util.Objects.nonNull;

@Slf4j
@Service
public class BotExecutor {

    @Autowired
    private TelegramBot bot;
    @Autowired
    private PostInfoService postInfoService;

    public BaseResponse execute(BaseRequest sendMessage) {
        return execute(
                sendMessage,
                baseResponse -> {
                    if (!baseResponse.isOk()) {
                        log.error(baseResponse.description());
                    } else {
                        log.debug("Send message params: {}", sendMessage.getParameters());
                        if (baseResponse instanceof SendResponse) {
                            SendResponse rs = ((SendResponse) baseResponse);
                            log.debug("Send message RS: {}", rs);
                            if (nonNull(rs.message())) {
                                PostInfo postInfo = postInfoService.getNotSendPost();

                                postInfo.setMessageId(rs.message().messageId());
                                postInfo.setChatId(rs.message().chat().id());

                                postInfoService.save(postInfo);
                            }
                        }
                    }
                },
                throwable -> log.error(ExceptionUtils.getRootCauseMessage(throwable))
        );
    }

    public BaseResponse execute(BaseRequest sendMessage, Consumer<BaseResponse> doItWithRs, Consumer<Throwable> doItIfError) {
        try {
            BaseResponse rs = bot.execute(sendMessage);
            doItWithRs.accept(rs);
            return rs;
        } catch (Throwable ex) {
            doItIfError.accept(ex);
        }
        return null;
    }
}
