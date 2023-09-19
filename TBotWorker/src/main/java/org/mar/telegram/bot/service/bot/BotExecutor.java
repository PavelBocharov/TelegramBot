package org.mar.telegram.bot.service.bot;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.BaseRequest;
import com.pengrad.telegrambot.response.BaseResponse;
import com.pengrad.telegrambot.response.SendResponse;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.mar.telegram.bot.service.bot.db.PostService;
import org.mar.telegram.bot.service.db.dto.PostInfoDtoRs;
import org.mar.telegram.bot.service.jms.MQSender;
import org.mar.telegram.bot.utils.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.logging.LogLevel;
import org.springframework.stereotype.Service;

import java.util.function.Consumer;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Service
public class BotExecutor {

    @Value("${application.group.chat.id}")
    private Long groupChatId;

    @Autowired
    private TelegramBot bot;
    @Autowired
    private PostService postService;
    @Autowired
    private MQSender mqSender;

    public BaseResponse execute(final String rqUuid, BaseRequest sendMessage) {
        return execute(
                sendMessage,
                baseResponse -> {
                    if (!baseResponse.isOk()) {
                        mqSender.sendLog(rqUuid, LogLevel.ERROR, "Not send msg: {} - {}", baseResponse.errorCode(), baseResponse.description());
                    } else {
                        if (baseResponse instanceof SendResponse) {
                            SendResponse rs = ((SendResponse) baseResponse);
                            mqSender.sendLog(rqUuid, LogLevel.DEBUG, "<< Send msg execute");
                            if (nonNull(rs.message()) && isNull(rs.message().editDate())) {
                                // TODO get by ID or rqUuid?
                                PostInfoDtoRs postInfo = postService.getNotSendPost(rqUuid);
                                mqSender.sendLog(rqUuid, LogLevel.DEBUG, ">> Get post: {}", postInfo);

                                postInfo.setMessageId(rs.message().messageId());
                                postInfo.setChatId(rs.message().chat().id());
                                if (groupChatId.equals(rs.message().chat().id())) {
                                    final String filePath = postInfo.getMediaPath();
                                    String exStr = Utils.removeFile(filePath);
                                    if(isNotBlank(exStr)) {
                                        mqSender.sendLog(rqUuid, LogLevel.ERROR, "Not remove file. Path: {}\n{}", filePath, exStr);
                                    }
                                    
                                    postInfo.setMediaPath(String.format("https://t.me/%s/%d", rs.message().chat().username(), rs.message().messageId()));
                                }
                                mqSender.sendLog(rqUuid, LogLevel.DEBUG, ">> Upd post: {}", postInfo);
                                postService.save(rqUuid, postInfo);
                            }
                        }
                    }
                },
                throwable -> mqSender.sendLog(rqUuid, LogLevel.ERROR, "Not send msg: {}", ExceptionUtils.getStackTrace(throwable))
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