package org.mar.telegram.bot.service.db.docker;

import org.mar.telegram.bot.service.bot.db.PostService;
import org.mar.telegram.bot.service.db.dto.PostInfoDto;
import org.mar.telegram.bot.service.jms.MQSender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.logging.LogLevel;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import static java.util.Objects.isNull;

@Service
@Profile("!local")
public class PostInfoService implements PostService {

    @Value("${application.bot.db.url}")
    private String dbUrl;

    @Autowired
    private MQSender mqSender;

    private WebClient webClient = WebClient.create();

    public PostInfoDto getNotSendPost(String rqUuid) {
        PostInfoDto rs = webClient.get()
                .uri(dbUrl + "/postinfo/isNotSend")
                .retrieve()
                .bodyToMono(PostInfoDto.class)
                .doOnSuccess(postInfoDto ->
                        log(rqUuid, postInfoDto, LogLevel.DEBUG, "Get not send postInfo: {}", postInfoDto)
                )
                .block();

        if (isNull(rs)) {
            rs = save(rqUuid, PostInfoDto.builder().isSend(false).build());
        }
        return rs;
    }

    public PostInfoDto save(String rqUuid, PostInfoDto postInfo) {
        return webClient.post()
                .uri(dbUrl + "/postinfo")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(postInfo), PostInfoDto.class)
                .retrieve()
                .bodyToMono(PostInfoDto.class)
                .doOnSuccess(postInfoDto ->
                        log(rqUuid, postInfoDto, LogLevel.DEBUG, "Save postInfo: {}", postInfoDto)
                )
                .block();
    }

    public PostInfoDto getByChatIdAndMessageId(String rqUuid, Long chatId, Integer messageId) {
        return webClient.get()
                .uri(String.format("%s/postinfo/%d/%d", dbUrl, chatId, messageId))
                .retrieve()
                .bodyToMono(PostInfoDto.class)
                .doOnSuccess(postInfoDto ->
                        log(rqUuid, postInfoDto, LogLevel.DEBUG,
                                "Get post by chatId = {}, messageId = {}. RS: {}", chatId, messageId, postInfoDto)
                )
                .block();
    }

    private PostInfoDto log(String rqUuid, PostInfoDto postInfoDto, LogLevel logLevel, final String message, Object... objects) {
        mqSender.sendLog(rqUuid, logLevel, message, objects);
        return postInfoDto;
    }

}
