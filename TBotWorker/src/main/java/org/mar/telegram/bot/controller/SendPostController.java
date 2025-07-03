package org.mar.telegram.bot.controller;

import com.mar.dto.mq.URLInfo;
import com.mar.dto.rest.ActionPostDtoRs;
import com.mar.dto.rest.BaseRs;
import com.mar.dto.rest.PostInfoDtoRq;
import com.mar.dto.rest.PostInfoDtoRs;
import com.mar.dto.rest.SendPostRq;
import com.mar.dto.rest.UserDtoRs;
import com.mar.dto.tbot.ContentType;
import com.mar.dto.tbot.TelegramMessage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.mapstruct.factory.Mappers;
import org.mar.telegram.bot.mapper.DBIntegrationMapper;
import org.mar.telegram.bot.service.bot.TelegramBotSendUtils;
import org.mar.telegram.bot.service.bot.TelegramBotService;
import org.mar.telegram.bot.service.bot.db.ActionService;
import org.mar.telegram.bot.service.bot.db.PostService;
import org.mar.telegram.bot.service.bot.db.UserService;
import org.mar.telegram.bot.service.logger.LoggerService;
import org.mar.telegram.bot.utils.Utils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

import static com.mar.dto.mq.LogEvent.LogLevel.DEBUG;
import static io.netty.handler.codec.http.HttpHeaders.Values.APPLICATION_JSON;
import static org.apache.commons.lang3.ObjectUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.isBlank;

@RestController
@RequestMapping(value = "/post")
@RequiredArgsConstructor
@Tag(name = "Контроллер отправки сообщений", description = "API для отправки сообщений через бота в группу.")
public class SendPostController {

    private final LoggerService loggerService;
    private final TelegramBotSendUtils sendUtils;
    private final TelegramBotService telegramBotWorker;
    private final PostService postService;
    private final ActionService actionService;
    private final UserService userService;

    private DBIntegrationMapper mapper = Mappers.getMapper(DBIntegrationMapper.class);

    @Value("${application.group.chat.id}")
    private Long groupChatId;

    @Value("${application.group.chat.textLine:}")
    private String textLine;

    @PostMapping(consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(summary = "Отправка сообщения", description = "Передача данных боту, чтоб он запостил это в группу.")
    public BaseRs sendMsg(@RequestBody @Valid SendPostRq rq) {
        loggerService.sendLog(rq.getRqUuid(), DEBUG, "REST API: {}", rq);

        URLInfo fileInfo = Utils.whatIsUrl(rq.getFilePath());

        if (Objects.nonNull(fileInfo) && !ContentType.Text.equals(fileInfo.getContentType())) {

            UserDtoRs user = userService.getByUserId(rq.getRqUuid(), rq.getUserId());

            PostInfoDtoRq savePostRq = new PostInfoDtoRq()
                    .withChatId(groupChatId)
                    .withMediaPath(rq.getFilePath())
                    .withCaption(getCaption(rq.getCaption(), rq.getHashTags()))
                    .withTypeDir(fileInfo.getContentType().getTypeDir());
            savePostRq.setRqUuid(rq.getRqUuid());
            savePostRq.setRqTm(new Date());

            PostInfoDtoRs postInfoDto = postService.save(rq.getRqUuid(), savePostRq);

            if (postInfoDto.getErrorCode() != null && postInfoDto.getErrorCode() > 0) {
                throw new RuntimeException("Cannot create post info. TBotDB msg: " + postInfoDto.getErrorMsg());
            }

            actionService.save(
                    rq.getRqUuid(),
                    new ActionPostDtoRs().withPostId(postInfoDto.getId()).withUserId(user.getId())
            );

            sendUtils.sendPost(rq.getRqUuid(), groupChatId, postInfoDto);
        }
        return new BaseRs().withRqUuid(rq.getRqUuid()).withRqTm(new Date());
    }

    @PostMapping(value = "/msg", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(summary = "Обработка событий из группы")
    public BaseRs workWithMsg(@RequestBody @Valid TelegramMessage rq) {
        telegramBotWorker.workWithMessage(rq);
        return new BaseRs().withRqUuid(rq.getRqUuid()).withRqTm(new Date());
    }

    private String getCaption(Map<Long, String> captionMap, List<String> hashTags) {
        StringBuilder sb = new StringBuilder();
        Map<Long, String> map = new TreeMap<>(captionMap);

        for (Long order : map.keySet()) {
            String value = map.get(order);
            if (isBlank(value)) {
                sb.append("\n");
            } else {
                sb.append(value).append("\n");
            }
        }

        if (isEmpty(hashTags)) {
            sb.append("\n").append(textLine);
        } else {
            sb.append("\n").append(String.join(" ", hashTags)).append("\n").append(textLine);
        }

        return sb.toString();
    }

}
