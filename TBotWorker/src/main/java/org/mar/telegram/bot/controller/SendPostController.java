package org.mar.telegram.bot.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.mar.telegram.bot.controller.dto.BaseRs;
import org.mar.telegram.bot.controller.dto.SendPost;
import org.mar.telegram.bot.controller.dto.TelegramMessage;
import org.mar.telegram.bot.service.bot.TelegramBotSendUtils;
import org.mar.telegram.bot.service.bot.TelegramBotService;
import org.mar.telegram.bot.service.bot.db.ActionService;
import org.mar.telegram.bot.service.bot.db.PostService;
import org.mar.telegram.bot.service.bot.db.UserService;
import org.mar.telegram.bot.service.db.dto.ActionPostDtoRs;
import org.mar.telegram.bot.service.db.dto.PostInfoDtoRs;
import org.mar.telegram.bot.service.db.dto.UserDto;
import org.mar.telegram.bot.service.jms.MQSender;
import org.mar.telegram.bot.service.jms.dto.URLInfo;
import org.mar.telegram.bot.utils.ContentType;
import org.mar.telegram.bot.utils.Utils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.logging.LogLevel;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static io.netty.handler.codec.http.HttpHeaders.Values.APPLICATION_JSON;
import static org.apache.commons.lang3.StringUtils.isBlank;

@RestController
@RequestMapping(value = "/post")
@RequiredArgsConstructor
@Tag(name = "Контроллер отправки сообщений", description = "API для отправки сообщений через бота в группу.")
public class SendPostController {

    private final MQSender mqSender;
    private final TelegramBotSendUtils sendUtils;
    private final TelegramBotService telegramBotWorker;
    private final PostService postService;
    private final ActionService actionService;
    private final UserService userService;

    @Value("${application.group.chat.id}")
    private Long groupChatId;

    @Value("${application.group.chat.textLine:}")
    private String textLine;

    @PostMapping(consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(summary = "Отправка сообщения", description = "Передача данных боту, чтоб он запостил это в группу.")
    public BaseRs sendMsg(@RequestBody @Valid SendPost rq) {
        mqSender.sendLog(rq.getRqUuid(), LogLevel.DEBUG, "REST API: {}", rq);

        URLInfo fileInfo = Utils.whatIsUrl(rq.getFilePath());

        if (Objects.nonNull(fileInfo) && !ContentType.Text.equals(fileInfo.getContentType())) {

            UserDto user = userService.getByUserId(rq.getRqUuid(), rq.getUserId());

            PostInfoDtoRs postInfoDto = postService.save(rq.getRqUuid(), new PostInfoDtoRs()
                    .withChatId(groupChatId)
                    .withMediaPath(rq.getFilePath())
                    .withCaption(getCaption(rq.getCaption(), rq.getHashTags()))
                    .withTypeDir(fileInfo.getContentType().getTypeDit())
            );

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

    private String getCaption(Map<String, String> captionMap, List<String> hashTags) {
        StringBuilder sb = new StringBuilder();

        for (String key : captionMap.keySet()) {
            if (isBlank(captionMap.get(key))) {
                sb.append("\n");
            } else {
                sb.append(key).append(": ").append(captionMap.get(key)).append("\n");
            }
        }

        sb.append("\n").append(String.join(" ", hashTags)).append("\n").append(textLine);

        return sb.toString();
    }

}
