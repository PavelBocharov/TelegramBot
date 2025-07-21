package org.mar.telegram.bot.service.db.docker;

import com.mar.dto.mq.LogEvent;
import com.mar.dto.rest.PostInfoDtoRq;
import com.mar.dto.rest.PostInfoDtoRs;
import org.apache.commons.lang3.tuple.Pair;
import org.mar.telegram.bot.service.bot.db.PostService;
import org.mar.telegram.bot.service.db.RestApiService;
import org.mar.telegram.bot.service.logger.LoggerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import static com.mar.dto.mq.LogEvent.LogLevel.DEBUG;
import static java.util.Objects.isNull;

@Service
@Profile("!local")
public class PostInfoService implements PostService {

    @Value("${application.bot.db.url}")
    private String dbUrl;

    @Autowired
    private LoggerService loggerService;

    @Autowired
    private RestApiService restApiService;

    public PostInfoDtoRs getNotSendPost(String rqUuid) {
        final String url = dbUrl + "/post/info/isNotSend";
        PostInfoDtoRs rs = restApiService.get(rqUuid, url, PostInfoDtoRs.class, "getNotSendPost");

        if (isNull(rs)) {
            rs = save(rqUuid, new PostInfoDtoRq().withIsSend(false));
        }

        log(rqUuid, rs, DEBUG, "Get not send postInfo: {}", rs);
        return rs;
    }

    public PostInfoDtoRs save(String rqUuid, PostInfoDtoRq postInfo) {
        final String url = dbUrl + "/post/info";
        PostInfoDtoRs rs = restApiService.post(rqUuid, url, postInfo, PostInfoDtoRs.class, "Save postInfo");
        log(rqUuid, rs, DEBUG, "Save postInfo: {}", rs);
        return rs;
    }

    public PostInfoDtoRs getByChatIdAndMessageId(String rqUuid, Long chatId, Integer messageId) {
        final String url = String.format("%s/post/info/%d/%d", dbUrl, chatId, messageId);
        PostInfoDtoRs rs = restApiService.get(rqUuid, url, PostInfoDtoRs.class, "getByChatIdAndMessageId");

        log(rqUuid, rs, DEBUG, "Get post by chatId = {}, messageId = {}. RS: {}", chatId, messageId, rs);

        return rs;
    }

    private PostInfoDtoRs log(String rqUuid, PostInfoDtoRs postInfoDto, LogEvent.LogLevel logLevel, final String message, Object... objects) {
        loggerService.sendLog(rqUuid, logLevel, message, objects);
        return postInfoDto;
    }

    @Override
    public PostInfoDtoRs getPostById(String rqUuid, Long id) {
        final String url = dbUrl + "/post/info";
        PostInfoDtoRs rs = restApiService.get(
                rqUuid,
                url,
                PostInfoDtoRs.class,
                "getPostById",
                Pair.of("id", String.valueOf(id))
        );

        if (isNull(rs)) {
            rs = save(rqUuid, new PostInfoDtoRq());
        }

        log(rqUuid, rs, DEBUG, "Get not send postInfo: {}", rs);
        return rs;
    }
}
