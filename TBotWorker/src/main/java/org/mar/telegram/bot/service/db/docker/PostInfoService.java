package org.mar.telegram.bot.service.db.docker;

import org.mapstruct.factory.Mappers;
import org.mar.telegram.bot.mapper.DBIntegrationMapper;
import org.mar.telegram.bot.service.bot.db.PostService;
import org.mar.telegram.bot.service.db.RestApiService;
import org.mar.telegram.bot.service.db.dto.PostInfoDtoRs;
import org.mar.telegram.bot.service.db.dto.PostInfoDtoRq;
import org.mar.telegram.bot.service.jms.MQSender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.logging.LogLevel;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import static java.util.Objects.isNull;

@Service
@Profile("!local")
public class PostInfoService implements PostService {

    @Value("${application.bot.db.url}")
    private String dbUrl;

    @Autowired
    private MQSender mqSender;

    @Autowired
    private RestApiService restApiService;

    private DBIntegrationMapper mapper = Mappers.getMapper(DBIntegrationMapper.class);

    public PostInfoDtoRs getNotSendPost(String rqUuid) {
        final String url = dbUrl + "/post/info/isNotSend";
        PostInfoDtoRs rs = restApiService.get(rqUuid, url, PostInfoDtoRs.class, "getNotSendPost");

        if (isNull(rs)) {
            rs = save(rqUuid, new PostInfoDtoRs().withIsSend(false));
        }

        log(rqUuid, rs, LogLevel.DEBUG, "Get not send postInfo: {}", rs);
        return rs;
    }

    public PostInfoDtoRs save(String rqUuid, PostInfoDtoRs postInfo) {
        final String url = dbUrl + "/post/info";
        PostInfoDtoRs rs =  restApiService.post(rqUuid, url, mapper.mapRsToRq(postInfo), PostInfoDtoRs.class, "Save postInfo");
        log(rqUuid, rs, LogLevel.DEBUG, "Save postInfo: {}", rs);
        return rs;
    }

    public PostInfoDtoRs getByChatIdAndMessageId(String rqUuid, Long chatId, Integer messageId) {
        final String url = String.format("%s/post/info/%d/%d", dbUrl, chatId, messageId);
        PostInfoDtoRs rs = restApiService.get(rqUuid, url, PostInfoDtoRs.class, "getByChatIdAndMessageId");

        log(rqUuid, rs, LogLevel.DEBUG, "Get post by chatId = {}, messageId = {}. RS: {}", chatId, messageId, rs);

        return rs;
    }

    private PostInfoDtoRs log(String rqUuid, PostInfoDtoRs postInfoDto, LogLevel logLevel, final String message, Object... objects) {
        mqSender.sendLog(rqUuid, logLevel, message, objects);
        return postInfoDto;
    }

}
