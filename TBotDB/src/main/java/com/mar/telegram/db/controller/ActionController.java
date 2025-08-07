package com.mar.telegram.db.controller;

import com.mar.dto.rest.ActionPostDtoRq;
import com.mar.dto.rest.ActionPostDtoRs;
import com.mar.exception.BaseException;
import com.mar.telegram.db.entity.ActionEnum;
import com.mar.telegram.db.entity.ActionPost;
import com.mar.telegram.db.jpa.ActionPostRepository;
import com.mar.telegram.db.jpa.PostInfoRepository;
import com.mar.telegram.db.jpa.UserInfoRepository;
import com.mar.telegram.db.mapper.ActionMapper;
import com.mar.utils.RestApiUtils;
import jakarta.validation.constraints.NotBlank;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.mapstruct.factory.Mappers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static com.mar.telegram.db.entity.ActionEnum.BORING;
import static com.mar.telegram.db.entity.ActionEnum.COOL;
import static com.mar.telegram.db.entity.ActionEnum.DEVIL;
import static com.mar.telegram.db.entity.ActionEnum.FIRE_HEART;

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
    public Mono<ActionPostDtoRs> getAction(
            @RequestHeader("RqUuid") @NotBlank String rqUuid,
            @PathVariable Long postId,
            @PathVariable Long userId
    ) {
        ActionPostDtoRs dto = null;
        try {
            ActionPost actionPost = actionPostRepository.findByPostIdAndUserInfoId(postId, userId);
            dto = actionMapper.mapToDto(actionPost);
        } catch (Exception e) {
            return Mono.error(new BaseException(rqUuid, new Date(), 500, e.getMessage()));
        }
        dto = RestApiUtils.enrichRs(dto, rqUuid);
        log.debug("<< get action: {}", dto);
        return Mono.justOrEmpty(dto);
    }

    @PostMapping()
    public Mono<ActionPostDtoRs> createAction(@RequestBody ActionPostDtoRq actionPostDto) {
        log.debug(">> createAction rq: {}", actionPostDto);
        if (actionPostDto != null && actionPostDto.getId() != null) {
            ActionEnum action = ActionEnum.getActionByCallbackData(actionPostDto.getActionCallbackData());
            ActionPost actionPost = actionPostRepository.findById(actionPostDto.getId()).orElse(null);
            if (actionPost != null && action.equals(actionPost.getAction())) {
                log.debug(">> createAction -> dropAction");
                return dropAction(actionPostDto);
            }
        }
        return Mono.just(actionPostDto)
                .map(dto -> Pair.of(
                        dto,
                        ActionPost.builder()
                                .id(actionPostDto.getId())
                                .action(ActionEnum.getActionByCallbackData(dto.getActionCallbackData()))
                                .updateDate(new Date())
                                .build())
                )
                .map(pair -> {
                            pair.getValue().setPost(postInfoRepository.findById(pair.getKey().getPostId()).get());
                            return pair;
                        }
                )
                .map(pair -> {
                            pair.getValue().setUserInfo(userInfoRepository.findById(pair.getKey().getUserId()).get());
                            return pair;
                        }
                )
                .map(pair -> actionPostRepository.save(pair.getValue()))
                .map(actionMapper::mapToDto)
                .map(actionPostDtoRs -> RestApiUtils.enrichRs(actionPostDtoRs, actionPostDto.getRqUuid()))
                .onErrorResume(ex -> Mono.error(new BaseException(actionPostDto.getRqUuid(), new Date(), 500, ex.getMessage())))
                .doOnSuccess(userDto -> log.debug("<< createAction: {}", userDto));
    }

    @PostMapping("/drop")
    public Mono<ActionPostDtoRs> dropAction(@RequestBody ActionPostDtoRq actionPostDto) {
        return Mono.just(actionPostDto)
                .map(rq -> {
                    actionPostRepository.deleteById(rq.getId());
                    return new ActionPostDtoRs();
                })
                .map(rs -> RestApiUtils.enrichRs(rs, actionPostDto.getRqUuid()))
                .onErrorResume(ex -> Mono.error(new BaseException(actionPostDto.getRqUuid(), new Date(), 500, ex.getMessage())))
                .doOnSuccess(rs -> log.debug("<< dropAction: {}", rs));
    }

    @GetMapping("/count/{postId}")
    public Mono<Map<String, Long>> countActionInPost(@RequestHeader("RqUuid") @NotBlank String rqUuid, @PathVariable Long postId) {
        return Mono.just(new HashMap<String, Long>())
                .map(countMap -> putInMap(countMap, postId, FIRE_HEART))
                .map(countMap -> putInMap(countMap, postId, DEVIL))
                .map(countMap -> putInMap(countMap, postId, COOL))
                .map(countMap -> putInMap(countMap, postId, BORING))
                .onErrorResume(ex -> Mono.error(new BaseException(rqUuid, new Date(), 500, ex.getMessage())))
                .doOnSuccess(userDto -> log.debug("<< countActionInPost: {}", userDto));
    }

    private Map<String, Long> putInMap(Map<String, Long> map, Long postId, ActionEnum action) {
        map.put(
                action.getCallbackData(),
                actionPostRepository.countByPostIdAndAction(postId, action)
        );
        return map;
    }

}
