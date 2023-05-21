package org.mar.telegram.bot.service.db;

import org.mar.telegram.bot.service.bot.db.PostService;
import org.mar.telegram.bot.service.db.dto.PostInfoDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import static java.util.Objects.isNull;

@Service
public class PostInfoService implements PostService {

    @Value("${application.bot.db.url}")
    private String dbUrl;

    private WebClient webClient = WebClient.create();

    public PostInfoDto getNotSendPost() {
        PostInfoDto rs = webClient.get()
                .uri(dbUrl + "/postinfo/isNotSend")
                .retrieve()
                .bodyToMono(PostInfoDto.class)
                .block();

        if (isNull(rs)) {
            rs = save(PostInfoDto.builder().isSend(false).build());
        }
        return rs;
    }

    public PostInfoDto save(PostInfoDto postInfo) {
        return webClient.post()
                .uri(dbUrl + "/postinfo")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(postInfo), PostInfoDto.class)
                .retrieve()
                .bodyToMono(PostInfoDto.class)
                .block();
    }

    public PostInfoDto getByChatIdAndMessageId(Long chatId, Integer messageId) {
        return  webClient.get()
                .uri(String.format("%s/postinfo/%d/%d", dbUrl, chatId, messageId))
                .retrieve()
                .bodyToMono(PostInfoDto.class)
                .block();
    }

}
