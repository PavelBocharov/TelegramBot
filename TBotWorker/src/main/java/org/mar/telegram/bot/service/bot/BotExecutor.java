package org.mar.telegram.bot.service.bot;

import com.mar.dto.rest.PostInfoDtoRs;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.BaseRequest;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.BaseResponse;
import com.pengrad.telegrambot.response.SendResponse;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.mapstruct.factory.Mappers;
import org.mar.telegram.bot.mapper.DBIntegrationMapper;
import org.mar.telegram.bot.service.bot.db.PostService;
import org.mar.telegram.bot.service.logger.LoggerService;
import org.mar.telegram.bot.utils.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.function.Consumer;

import static com.mar.dto.mq.LogEvent.LogLevel.DEBUG;
import static com.mar.dto.mq.LogEvent.LogLevel.ERROR;
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
    private LoggerService loggerService;

    private DBIntegrationMapper mapper = Mappers.getMapper(DBIntegrationMapper.class);

    public BaseResponse sendPost(final String rqUuid, final long id, BaseRequest sendMessage) {
        return sendPost(
                sendMessage,
                baseResponse -> {
                    if (!baseResponse.isOk()) {
                        loggerService.sendLog(rqUuid, ERROR, "Not send msg: {} - {}", baseResponse.errorCode(), baseResponse.description());
                    } else {
                        if (baseResponse instanceof SendResponse) {
                            SendResponse rs = ((SendResponse) baseResponse);
                            loggerService.sendLog(rqUuid, DEBUG, "<< Send msg execute");
                            if (nonNull(rs.message()) && isNull(rs.message().editDate())) {
                                PostInfoDtoRs postInfo = postService.getPostById(rqUuid, id);
                                loggerService.sendLog(rqUuid, DEBUG, ">> Get post: {}", postInfo);

                                postInfo.setIsSend(true);
                                postInfo.setMessageId(rs.message().messageId());
                                postInfo.setChatId(rs.message().chat().id());
                                if (groupChatId.equals(rs.message().chat().id())) {
                                    final String filePath = postInfo.getMediaPath();
                                    String exStr = Utils.removeFile(filePath);
                                    if (isNotBlank(exStr)) {
                                        loggerService.sendLog(rqUuid, ERROR, "Not remove file. Path: {}\n{}", filePath, exStr);
                                    }

                                    postInfo.setMediaPath(String.format("https://t.me/%s/%d", rs.message().chat().username(), rs.message().messageId()));
                                }
                                loggerService.sendLog(rqUuid, DEBUG, ">> Upd post: {}", postInfo);
                                postService.save(rqUuid, mapper.mapRsToRq(postInfo));
                            }
                        }
                    }
                },
                throwable -> loggerService.sendLog(rqUuid, ERROR, "Not send msg: {}", ExceptionUtils.getStackTrace(throwable))
        );
    }

    public BaseResponse sendMessage(final String rqUuid, BaseRequest sendMessage) {
        if (sendMessage instanceof SendMessage) {
            ((SendMessage) sendMessage).parseMode(ParseMode.HTML);
        }

        return sendPost(
                sendMessage,
                baseResponse -> {
                    if (!baseResponse.isOk()) {
                        loggerService.sendLog(rqUuid, ERROR, "Not send msg: {} - {}", baseResponse.errorCode(), baseResponse.description());
                    }
                },
                throwable -> loggerService.sendLog(rqUuid, ERROR, "Not send msg: {}", ExceptionUtils.getStackTrace(throwable))
        );
    }

    public BaseResponse sendPost(BaseRequest sendMessage, Consumer<BaseResponse> doItWithRs, Consumer<Throwable> doItIfError) {
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
