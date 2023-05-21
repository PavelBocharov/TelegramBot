package org.mar.telegram.bot.service.db;

import org.mar.telegram.bot.service.bot.db.ActionService;
import org.mar.telegram.bot.service.db.dto.ActionEnum;
import org.mar.telegram.bot.service.db.dto.ActionPostDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

import static java.util.Objects.isNull;

@Service
public class ActionPostService implements ActionService {

    @Value("${application.bot.db.url}")
    private String dbUrl;

    private WebClient webClient = WebClient.create();

    public ActionPostDto getByPostIdAndUserInfoId(Long postInfoId, Long userId) {
        Mono<ActionPostDto> dtoMono = webClient.get()
                .uri(String .format("%s/action/%d/%d", dbUrl, postInfoId, userId))
                .retrieve()
                .bodyToMono(ActionPostDto.class);

        ActionPostDto actionPost = dtoMono.block();
        if (isNull(actionPost)) {
            actionPost = ActionPostDto.builder()
                    .userId(userId)
                    .postId(postInfoId)
                    .build();
        }
        return actionPost;
    }

    @Override
    public ActionPostDto save(ActionPostDto actionPost) {
        return webClient.post()
                .uri(dbUrl+ "/action")
                .body(Mono.just(actionPost), ActionPostDto.class)
                .retrieve()
                .bodyToMono(ActionPostDto.class)
                .block();
    }


    public Map<ActionEnum, Long> countByPostIdAndAction(Long postId) {
        Map rs = webClient.get()
                .uri(String .format("%s/action/count/%d", dbUrl, postId))
                .retrieve()
                .bodyToMono(Map.class)
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
