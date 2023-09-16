package com.mar.telegram.db.controller;

import com.mar.telegram.db.dto.BaseDto;
import com.mar.telegram.db.dto.PostInfoDto;
import com.mar.telegram.db.dto.PostTypeDto;
import com.mar.telegram.db.dto.PostTypeListDto;
import com.mar.telegram.db.exception.BaseException;
import com.mar.telegram.db.jpa.PostInfoRepository;
import com.mar.telegram.db.jpa.PostTypeRepository;
import com.mar.telegram.db.mapper.PostMapper;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mapstruct.factory.Mappers;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Date;

@Slf4j
@RestController()
@RequestMapping(value = "/post")
@RequiredArgsConstructor
public class PostController {

    private final PostInfoRepository postInfoRepository;
    private final PostTypeRepository postTypeRepository;

    private PostMapper postMapper = Mappers.getMapper(PostMapper.class);

    @GetMapping("/info/isNotSend")
    public Mono<PostInfoDto> getPostBySendFlag(@RequestHeader("RqUuid") @NotBlank String rqUuid) {
        log.debug(">> getPostBySendFlag: rqUuid - '{}'", rqUuid);
        return Mono.justOrEmpty(postInfoRepository.getByIsSend(false))
                .map(postMapper::mapToDto)
                .doOnSuccess(postInfoDto -> log.debug("<< getPostBySendFlag: {}", postInfoDto));
    }

    @GetMapping("/info/{chatId}/{messageId}")
    public Mono<PostInfoDto> getPostBySendFlag(
            @RequestHeader("RqUuid") @NotBlank String rqUuid,
            @PathVariable @NotNull Long chatId,
            @PathVariable @NotNull Integer messageId
    ) {
        log.debug(">> getPostBySendFlag: rqUuid - '{}', chatId - {}, messageId - {}", rqUuid, chatId, messageId);
        return Mono.justOrEmpty(postInfoRepository.getByChatIdAndMessageId(chatId, messageId))
                .map(postMapper::mapToDto)
                .doOnSuccess(postInfoDto -> log.debug("<< getPostBySendFlag: {}", postInfoDto));
    }

    @PostMapping("/info")
    public Mono<PostInfoDto> create(@RequestBody @Valid PostInfoDto postInfoDto) {
        log.debug(">> create/update post: {}", postInfoDto);
        return Mono.justOrEmpty(postMapper.mapToEntity(postInfoDto))
                .map(postInfo -> postInfo.withUpdateDate(new Date()))
                .map(postInfoRepository::save)
                .map(postMapper::mapToDto)
                .doOnSuccess(postInfoDto1 -> log.debug("<< create/update post: {}", postInfoDto1));
    }

    @PostMapping("/type")
    public Mono<PostTypeDto> createType(@RequestBody @Valid PostTypeDto rq) {
        log.debug(">> createType: {}", rq);
        return Mono.just(postMapper.mapToEntity(rq))
                .map(postTypeRepository::save)
                .map(postMapper::mapToDto)
                .map(dto -> updateRs(dto, rq.getRqUuid()))
                .onErrorResume(ex -> Mono.error(new BaseException(rq.getRqUuid(), new Date(), 500, ex.getMessage())))
                .doOnSuccess(postTypeDto -> log.debug("<< createType: {}", postTypeDto));
    }

    @GetMapping("/type")
    public Mono<PostTypeListDto> getAllType(@RequestHeader("RqUuid") @NotBlank String rqUuid) {
        log.debug(">> getAllType: rqUuid - {}", rqUuid);
        return Flux.fromIterable(postTypeRepository.findAll())
                .map(postMapper::mapToDto)
                .collectList()
                .map(PostTypeListDto::new)
                .map(dto -> updateRs(dto, rqUuid))
                .onErrorResume(ex -> Mono.error(new BaseException(rqUuid, new Date(), 500, ex.getMessage())))
                .doOnSuccess(postTypeListDto -> log.debug("<< getAllType: {}", postTypeListDto));
    }

    @DeleteMapping(value = "/type")
    public Mono<BaseDto> removeTypeById(@RequestHeader("RqUuid") @NotBlank String rqUuid, @RequestHeader("Id") @NotNull Long id) {
        log.debug(">> removeTypeById: rqUuid - {}, id - {}", rqUuid, id);
        return Mono.just(id)
                .map(postTypes -> {
                    postTypeRepository.deleteById(id);
                    return new BaseDto().withRqUuid(rqUuid).withRqTm(new Date());
                })
                .onErrorResume(ex -> Mono.error(new BaseException(rqUuid, new Date(), 500, ex.getMessage())))
                .doOnSuccess(baseDto -> log.debug("<< removeTypeById: {}", baseDto));
    }

    @PutMapping("/type")
    public Mono<PostTypeDto> updateType(@RequestBody @Valid PostTypeDto rq) {
        log.debug(">> updateType: {}", rq);
        return Mono.just(rq)
                .map(postMapper::mapToEntity)
                .map(postTypeRepository::save)
                .map(postMapper::mapToDto)
                .map(dto -> updateRs(dto, rq.getRqUuid()))
                .onErrorResume(ex -> Mono.error(new BaseException(rq.getRqUuid(), new Date(), 500, ex.getMessage())))
                .doOnSuccess(postTypeDto -> log.debug("<< updateType: {}", postTypeDto));
    }

    private PostTypeDto updateRs(PostTypeDto rs, String rqUuid) {
        rs.setRqUuid(rqUuid);
        rs.setRqTm(new Date());
        return rs;
    }

    private PostTypeListDto updateRs(PostTypeListDto rs, String rqUuid) {
        rs.setRqUuid(rqUuid);
        rs.setRqTm(new Date());
        return rs;
    }
}
