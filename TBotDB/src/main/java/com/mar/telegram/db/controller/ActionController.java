package com.mar.telegram.db.controller;

import com.mar.telegram.db.dto.ActionPostDto;
import com.mar.telegram.db.entity.ActionEnum;
import com.mar.telegram.db.entity.ActionPost;
import com.mar.telegram.db.jpa.ActionPostRepository;
import com.mar.telegram.db.jpa.PostInfoRepository;
import com.mar.telegram.db.jpa.UserInfoRepository;
import com.mar.telegram.db.mapper.ActionMapper;
import lombok.extern.slf4j.Slf4j;
import org.mapstruct.factory.Mappers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static com.mar.telegram.db.entity.ActionEnum.*;

@Slf4j
@RestController
@RequestMapping(value = "/action")
public class ActionController {

    @Autowired
    private ActionPostRepository actionPostRepository;
    @Autowired
    private PostInfoRepository postInfoRepository;
    @Autowired
    private UserInfoRepository userInfoRepository;

    private ActionMapper actionMapper = Mappers.getMapper(ActionMapper.class);

    @GetMapping("/{postId}/{userId}")
    public Mono<ActionPostDto> getAction(@PathVariable Long postId, @PathVariable Long userId) {
        ActionPost actionPost = actionPostRepository.findByPostIdAndUserInfoId(postId, userId);
        ActionPostDto dto = actionMapper.mapToDto(actionPost);
        return Mono.justOrEmpty(dto);
    }

    @PostMapping()
    public Mono<ActionPostDto> createAction(@RequestBody ActionPostDto actionPostDto) {
        return Mono.just(ActionPost.builder()
                        .id(actionPostDto.getId())
                        .action(
                                ActionEnum.getActionByCallbackData(actionPostDto.getActionCallbackData())
                        )
                        .updateDate(new Date())
                        .build()
                )
                .map(actionPost -> actionPost.withPost(
                        postInfoRepository.findById(actionPostDto.getPostId()).get())
                )
                .map(actionPost -> actionPost.withUserInfo(
                        userInfoRepository.findById(actionPostDto.getUserId()).get())
                )

                .map(actionPost -> actionPostRepository.save(actionPost))
                .map(actionMapper::mapToDto);
    }

    @GetMapping("/count/{postId}")
    public Mono<Map<String, Long>> countActionInPost(@PathVariable Long postId) {
        return Mono.just(new HashMap<String, Long>())
                .map(countMap -> putInMap(countMap, postId, FIRE_HEART))
                .map(countMap -> putInMap(countMap, postId, DEVIL))
                .map(countMap -> putInMap(countMap, postId, COOL))
                .map(countMap -> putInMap(countMap, postId, BORING));
    }

    private Map<String, Long> putInMap(Map<String, Long> map, Long postId, ActionEnum action) {
        map.put(
                action.getCallbackData(),
                actionPostRepository.countByPostIdAndAction(postId, action)
        );
        return map;
    }

}
