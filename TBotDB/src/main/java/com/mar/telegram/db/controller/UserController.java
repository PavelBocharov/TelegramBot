package com.mar.telegram.db.controller;

import com.mar.telegram.db.dto.UserDto;
import com.mar.telegram.db.entity.UserInfo;
import com.mar.telegram.db.exception.BaseException;
import com.mar.telegram.db.jpa.UserInfoRepository;
import com.mar.telegram.db.mapper.UserMapper;
import lombok.extern.slf4j.Slf4j;
import org.mapstruct.factory.Mappers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Date;

import static java.lang.String.valueOf;

@Slf4j
@RestController
@RequestMapping(value = "/user")
public class UserController {

    @Autowired
    private UserInfoRepository userInfoRepository;

    private final UserMapper userMapper = Mappers.getMapper(UserMapper.class);

    @GetMapping(value = "/uid/{userId}")
    public Mono<UserDto> getUserById(@PathVariable Long userId) {
        log.debug(">> getUserById: userId - {}", userId);
        return Mono.just(userInfoRepository.getByUserId(userId))
                .map(userMapper::mapToDto)
                .onErrorResume(ex -> Mono.error(new BaseException(valueOf(userId), new Date(), 500, ex.getMessage())))
                .doOnSuccess(userDto -> log.debug("<< getUserById: {}", userDto));
    }

    @GetMapping(value = "/create/uid/{userId}")
    public Mono<UserDto> createUserWithUId(@PathVariable Long userId) {
        log.debug(">> createUserWithUId: userId - {}", userId);
        return Mono.just(UserInfo.builder().userId(userId).build())
                .map(userInfoRepository::save)
                .map(userMapper::mapToDto)
                .onErrorResume(ex -> Mono.error(new BaseException(valueOf(userId), new Date(), 500, ex.getMessage())))
                .doOnSuccess(userDto -> log.debug("<< createUserWithUId: {}", userDto));
    }

}
