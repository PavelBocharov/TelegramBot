package com.mar.telegram.db.resolver;

import com.mar.dto.rest.UserDtoRs;
import com.mar.exception.BaseException;
import com.mar.telegram.db.entity.UserInfo;
import com.mar.telegram.db.jpa.UserInfoRepository;
import com.mar.telegram.db.mapper.UserMapper;
import com.mar.utils.RestApiUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.mapstruct.factory.Mappers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Mono;

import java.util.Date;

@Slf4j
@Controller
public class UserResolver {

    private final UserMapper userMapper = Mappers.getMapper(UserMapper.class);
    @Autowired
    private UserInfoRepository userInfoRepository;

    //  {
    //      "query": "query getUser { getUser(rqUuid: \"{% uuid 'v4' %}\", userId: \"123456\") { rqUuid, rqTm, errorCode, errorMsg, id, userId, actionIds }}",
    //      "operationName": "getUser"
    //  }
    @QueryMapping
    public Mono<UserDtoRs> getUser(@Argument String rqUuid, @Argument Long userId) {
        log.debug("GraphQL >> getUser: rqUuid: {}, userId - {}", rqUuid, userId);
        return Mono.just(userId)
                .map(uId -> userInfoRepository.getByUserId(userId))
                .map(userMapper::mapToDto)
                .map(userDtoRs -> RestApiUtils.enrichRs(userDtoRs, rqUuid))
                .onErrorResume(ex -> Mono.error(new BaseException(rqUuid, new Date(), 500, ExceptionUtils.getMessage(ex), ex)))
                .doOnSuccess(userDto -> log.debug("GraphQL << getUser: {}", userDto));
    }

    //  {
    //      "query": "mutation createUser { createUser(rqUuid: \"{% uuid 'v4' %}\", userId: \"{% faker 'randomInt' %}\") { rqUuid, rqTm, errorCode, errorMsg, id, userId, actionIds }}",
    //      "operationName": "createUser"
    //  }
    @MutationMapping
    public Mono<UserDtoRs> createUser(@Argument String rqUuid, @Argument Long userId) {
        log.debug("GraphQL >> createUser: rqUuid: {}, userId - {}", rqUuid, userId);
        return Mono.just(UserInfo.builder().userId(userId).build())
                .mapNotNull(userInfoRepository::save)
                .map(userMapper::mapToDto)
                .map(userDtoRs -> RestApiUtils.enrichRs(userDtoRs, rqUuid))
                .onErrorResume(ex -> Mono.error(new BaseException(rqUuid, new Date(), 500, ex.getMessage(), ex)))
                .doOnSuccess(userDto -> log.debug("GraphQL << createUser: {}", userDto));
    }

}
