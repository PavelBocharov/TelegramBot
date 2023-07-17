package org.mar.telegram.bot.controller;

import lombok.RequiredArgsConstructor;
import org.mar.telegram.bot.controller.dto.SendPost;
import org.mar.telegram.bot.service.bot.TelegramBotSendUtils;
import org.mar.telegram.bot.service.bot.db.ActionService;
import org.mar.telegram.bot.service.bot.db.PostService;
import org.mar.telegram.bot.service.bot.db.UserService;
import org.mar.telegram.bot.service.db.dto.ActionPostDto;
import org.mar.telegram.bot.service.db.dto.PostInfoDto;
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

import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.apache.commons.lang3.StringUtils.isBlank;

@RestController
@RequestMapping(value = "/post")
@RequiredArgsConstructor
public class SendPostController {

    private final MQSender mqSender;
    private final TelegramBotSendUtils sendUtils;
    private final PostService postService;
    private final ActionService actionService;
    private final UserService userService;

    @Value("${application.group.chat.id}")
    private Long groupChatId;

    @PostMapping()
    public void sendMsg(@RequestBody SendPost rq) {
        mqSender.sendLog(rq.getRqUuid(), LogLevel.DEBUG, "REST API: {}", rq);

        URLInfo fileInfo = Utils.whatIsUrl(rq.getFilePath());

        if (Objects.nonNull(fileInfo) && !ContentType.Text.equals(fileInfo.getContentType())) {

            UserDto user = userService.getByUserId(rq.getRqUuid(), rq.getUserId());

            PostInfoDto postInfoDto = postService.save(rq.getRqUuid(), PostInfoDto.builder()
                    .isSend(false)
                    .chatId(groupChatId)
                    .mediaPath(rq.getFilePath())
                    .caption(getCaption(rq.getCaption(), rq.getHashTags()))
                    .typeDir(fileInfo.getContentType().getTypeDit())
                    .build()
            );

            actionService.save(
                    rq.getRqUuid(),
                    ActionPostDto.builder().postId(postInfoDto.getId()).userId(user.getId()).build()
            );

            sendUtils.sendPost(rq.getRqUuid(), groupChatId, postInfoDto);

            postInfoDto.setIsSend(true);
            postService.save(rq.getRqUuid(), postInfoDto);
        }
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

        sb.append(String.join(" ", hashTags));

        return sb.toString();
    }

}
