package org.mar.telegram.bot.service.db.docker;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mar.dto.graphql.CreateUserRequest;
import com.mar.dto.graphql.GetUserRequest;
import com.mar.dto.graphql.GraphQLResponse;
import com.mar.dto.rest.UserDtoRs;
import org.mar.telegram.bot.service.bot.db.UserService;
import org.mar.telegram.bot.service.db.RestApiService;
import org.mar.telegram.bot.service.logger.LoggerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.Date;

import static com.mar.dto.mq.LogEvent.LogLevel.DEBUG;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

@Service
@Profile("!local")
public class UserInfoService implements UserService {


    @Value("${application.bot.db.url}")
    private String dbUrl;

    @Autowired
    private LoggerService loggerService;
    @Autowired
    private RestApiService restApiService;

    public UserDtoRs getByUserId(String rqUuid, long userId) {
        final String url = dbUrl + "/api/gphql";
        GraphQLResponse rs = restApiService.post(
                rqUuid,
                url,
                new GetUserRequest(rqUuid, new Date()).setVariablesData(rqUuid, userId),
                GraphQLResponse.class,
                "getByUserId by user id " + userId
        );
        loggerService.sendLog(rqUuid, DEBUG, "Get user with GraphQL RS - {}", rs);
        UserDtoRs userDto = getUser(rs, GraphQLResponse.GET_USER_METHOD);
        loggerService.sendLog(rqUuid, DEBUG, "Get user with GraphQL user DTO - {}", userDto);
        if (isNull(userDto) || (nonNull(userDto.getErrorCode()) && userDto.getErrorCode() > 0)) {
            userDto = create(rqUuid, new UserDtoRs().withUserId(userId));
        }
        loggerService.sendLog(rqUuid, DEBUG, "Load user by userID: {}, dto: {}", userId, userDto);
        return userDto;
    }

    public UserDtoRs create(String rqUuid, UserDtoRs user) {
        final String url = dbUrl + "/api/gphql";
        GraphQLResponse rs = restApiService.post(
                rqUuid,
                url,
                new CreateUserRequest(rqUuid, new Date()).setVariablesData(rqUuid, user.getUserId()),
                GraphQLResponse.class,
                "Create user by user id " + user.getUserId()
        );
        loggerService.sendLog(rqUuid, DEBUG, "Create user with GraphQL user RS - {}", rs);
        UserDtoRs dto = getUser(rs, GraphQLResponse.CREATE_USER_METHOD);
        loggerService.sendLog(rqUuid, DEBUG, "Create user with GraphQL user DTO - {}", dto);
        return dto;
    }

    private UserDtoRs getUser(GraphQLResponse rs, String method) {
        if (rs == null || rs.getData() == null || rs.getData().get(method) == null) {
            return null;
        }
        return new ObjectMapper().convertValue(rs.getData().get(method), UserDtoRs.class);
    }

}
