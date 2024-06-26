package org.mar.telegram.bot.service.db.docker;

import com.mar.dto.tbot.ActionEnum;
import com.mar.dto.rest.ActionPostDtoRs;
import org.mapstruct.factory.Mappers;
import org.mar.telegram.bot.mapper.DBIntegrationMapper;
import org.mar.telegram.bot.service.bot.db.ActionService;
import org.mar.telegram.bot.service.db.RestApiService;
import com.mar.interfaces.mq.MQSender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

import static com.mar.dto.mq.LogEvent.LogLevel.DEBUG;
import static java.util.Objects.isNull;

@Service
@Profile("!local")
public class ActionPostService implements ActionService {

    @Value("${application.bot.db.url}")
    private String dbUrl;

    @Autowired
    private MQSender mqSender;
    @Autowired
    private RestApiService restApiService;

    private DBIntegrationMapper mapper = Mappers.getMapper(DBIntegrationMapper.class);

    public ActionPostDtoRs getByPostIdAndUserInfoId(String rqUuid, Long postInfoId, Long userId) {
        final String url = String.format("%s/action/%d/%d", dbUrl, postInfoId, userId);
        ActionPostDtoRs actionPost = restApiService.get(rqUuid, url, ActionPostDtoRs.class, "getByPostIdAndUserInfoId");

        if (isNull(actionPost)) {
            actionPost = new ActionPostDtoRs().withUserId(userId).withPostId(postInfoId);
        }
        mqSender.sendLog(rqUuid, DEBUG, "Get action by postId: {} and userId: {}. Action: {}", postInfoId, userId, actionPost);
        return actionPost;
    }

    @Override
    public ActionPostDtoRs save(String rqUuid, ActionPostDtoRs actionPost) {
        final String url = dbUrl + "/action";
        ActionPostDtoRs rs = restApiService.post(rqUuid, url, mapper.mapRsToRq(actionPost), ActionPostDtoRs.class, "save action post");
        mqSender.sendLog(rqUuid, DEBUG, "Save action: {}", rs);
        return rs;
    }


    public Map<ActionEnum, Long> countByPostIdAndAction(String rqUuid, Long postId) {
        final String url = String.format("%s/action/count/%d", dbUrl, postId);
        Map rs = restApiService.get(rqUuid, url, Map.class, "get countByPostIdAndAction");
        mqSender.sendLog(rqUuid, DEBUG, "Get count action: {}", rs);

        Map<ActionEnum, Long> rez = new HashMap<>();

        for (Object callbackData : rs.keySet()) {
            ActionEnum action = ActionEnum.getActionByCallbackData(callbackData);
            Long count = Long.valueOf(String.valueOf(rs.get(callbackData)));
            rez.put(action, count);
        }

        return rez;
    }

}
