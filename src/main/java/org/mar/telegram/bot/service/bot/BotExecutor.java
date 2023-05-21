package org.mar.telegram.bot.service.bot;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.BaseRequest;
import com.pengrad.telegrambot.response.BaseResponse;
import com.pengrad.telegrambot.response.SendResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.mar.telegram.bot.service.bot.db.PostService;
import org.mar.telegram.bot.service.db.dto.PostInfoDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.function.Consumer;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

@Slf4j
@Service
public class BotExecutor {

    @Autowired
    private TelegramBot bot;
    @Autowired
    private PostService postService;

    public BaseResponse execute(BaseRequest sendMessage) {
        return execute(
                sendMessage,
                baseResponse -> {
                    if (!baseResponse.isOk()) {
                        log.error(baseResponse.description());
                    } else {
                        if (baseResponse instanceof SendResponse) {
                            SendResponse rs = ((SendResponse) baseResponse);
                            if (nonNull(rs.message()) && isNull(rs.message().editDate())) {
                                PostInfoDto postInfo = postService.getNotSendPost();

                                postInfo.setMessageId(rs.message().messageId());
                                postInfo.setChatId(rs.message().chat().id());

                                postService.save(postInfo);
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
