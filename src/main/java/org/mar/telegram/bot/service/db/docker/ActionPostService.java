package org.mar.telegram.bot.service.db.docker;

import org.mar.telegram.bot.service.bot.db.ActionService;
import org.mar.telegram.bot.service.db.dto.ActionEnum;
import org.mar.telegram.bot.service.db.dto.ActionPostDto;
import org.mar.telegram.bot.service.jms.MQSender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.logging.LogLevel;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

import static java.util.Objects.isNull;

@Service
@Profile("!local")
public class ActionPostService implements ActionService {

    @Value("${application.bot.db.url}")
    private String dbUrl;

    @Autowired
    private MQSender mqSender;

    private WebClient webClient = WebClient.create();

    public ActionPostDto getByPostIdAndUserInfoId(String rqUuid, Long postInfoId, Long userId) {
        ActionPostDto actionPost = webClient.get()
                .uri(String.format("%s/action/%d/%d", dbUrl, postInfoId, userId))
                .retrieve()
                .bodyToMono(ActionPostDto.class)
                .doOnSuccess(actionPostDto ->
                        mqSender.sendLog(
                                rqUuid, LogLevel.DEBUG, "Get action by postId: {} and uerId: {}. Action: {}", postInfoId, userId, actionPostDto
                        )
                )
                .block();
        if (isNull(actionPost)) {
            actionPost = ActionPostDto.builder()
                    .userId(userId)
                    .postId(postInfoId)
                    .build();
        }
        return actionPost;
    }

    @Override
    public ActionPostDto save(String rqUuid, ActionPostDto actionPost) {
        return webClient.post()
                .uri(dbUrl + "/action")
                .body(Mono.just(actionPost), ActionPostDto.class)
                .retrieve()
                .bodyToMono(ActionPostDto.class)
                .doOnSuccess(actionPostDto -> mqSender.sendLog(rqUuid, LogLevel.DEBUG, "Save action: {}", actionPostDto))
                .block();
    }


    public Map<ActionEnum, Long> countByPostIdAndAction(String rqUuid, Long postId) {
        Map rs = webClient.get()
                .uri(String.format("%s/action/count/%d", dbUrl, postId))
                .retrieve()
                .bodyToMono(Map.class)
                .doOnSuccess(map -> mqSender.sendLog(rqUuid, LogLevel.DEBUG, "Get count action: {}", map))
                .block();

        Map<ActionEnum, Long> rez = new HashMap<>();

        for (Object callbackData : rs.keySet()) {
            ActionEnum action = ActionEnum.getActionByCallbackData(callbackData);
            Long count = Long.valueOf(String.valueOf(rs.get(callbackData)));
            rez.put(action, count);
        }

        return rez;
    }

}
