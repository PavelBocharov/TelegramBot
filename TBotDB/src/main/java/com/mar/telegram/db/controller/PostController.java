package com.mar.telegram.db.controller;

import com.mar.dto.rest.BaseRs;
import com.mar.dto.rest.PostInfoDtoRq;
import com.mar.dto.rest.PostInfoDtoRs;
import com.mar.dto.rest.PostTypeDtoRq;
import com.mar.dto.rest.PostTypeDtoRs;
import com.mar.dto.rest.PostTypeListDtoRs;
import com.mar.exception.BaseException;
import com.mar.telegram.db.jpa.PostInfoRepository;
import com.mar.telegram.db.jpa.PostTypeRepository;
import com.mar.telegram.db.mapper.PostMapper;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mapstruct.factory.Mappers;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
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
    public Mono<PostInfoDtoRs> getPostBySendFlag(@RequestHeader("RqUuid") @NotBlank String rqUuid) {
        log.debug(">> getPostBySendFlag: rqUuid - '{}'", rqUuid);
        return Mono.justOrEmpty(postInfoRepository.getByIsSend(false))
                .map(postMapper::mapToDto)
                .map(dto -> updateRs(dto, rqUuid))
                .doOnSuccess(postInfoDto -> log.debug("<< getPostBySendFlag: {}", postInfoDto));
    }

    @GetMapping("/info/{chatId}/{messageId}")
    public Mono<PostInfoDtoRs> getPostBySendFlag(
            @RequestHeader("RqUuid") @NotBlank String rqUuid,
            @PathVariable @NotNull Long chatId,
            @PathVariable @NotNull Integer messageId
    ) {
        log.debug(">> getPostBySendFlag: rqUuid - '{}', chatId - {}, messageId - {}", rqUuid, chatId, messageId);
        return Mono.justOrEmpty(postInfoRepository.getByChatIdAndMessageId(chatId, messageId))
                .map(postMapper::mapToDto)
                .map(dto -> updateRs(dto, rqUuid))
                .doOnSuccess(postInfoDto -> log.debug("<< getPostBySendFlag: {}", postInfoDto));
    }

    @PostMapping("/info")
    public Mono<PostInfoDtoRs> save(@RequestBody @Valid PostInfoDtoRq rq) {
        log.debug(">> save post: {}", rq);
        return Mono.justOrEmpty(postMapper.mapToEntity(rq))
                .map(postInfo -> postInfo.withUpdateDate(new Date()))
                .map(postInfoRepository::save)
                .map(postMapper::mapToDto)
                .map(dto -> updateRs(dto, rq.getRqUuid()))
                .doOnSuccess(postInfoDto1 -> log.debug("<< save post: {}", postInfoDto1));
    }

    @GetMapping("/info")
    public Mono<PostInfoDtoRs> getById(@RequestHeader("RqUuid") @NotBlank String rqUuid, @RequestHeader("id") @NotBlank Long id) {
        log.debug(">> getById post: {}", id);
        return Mono.just(postInfoRepository.findById(id).orElseThrow())
                .map(postMapper::mapToDto)
                .map(dto -> updateRs(dto, rqUuid))
                .doOnSuccess(postInfoDto1 -> log.debug("<< getById post: {}", postInfoDto1));
    }

    @PostMapping("/type")
    public Mono<PostTypeDtoRs> createType(@RequestBody @Valid PostTypeDtoRq rq) {
        log.debug(">> createType: {}", rq);
        return Mono.just(postMapper.mapToEntity(rq))
                .map(postTypeRepository::save)
                .map(postMapper::mapToDto)
                .map(dto -> updateRs(dto, rq.getRqUuid()))
                .onErrorResume(ex -> Mono.error(new BaseException(rq.getRqUuid(), new Date(), 500, ex.getMessage())))
                .doOnSuccess(postTypeDto -> log.debug("<< createType: {}", postTypeDto));
    }

    @GetMapping("/type")
    public Mono<PostTypeListDtoRs> getAllType(@RequestHeader("RqUuid") @NotBlank String rqUuid) {
        log.debug(">> getAllType: rqUuid - {}", rqUuid);
        return Flux.fromIterable(postTypeRepository.findAll())
                .map(postMapper::mapToDto)
                .collectList()
                .map(PostTypeListDtoRs::new)
                .map(dto -> updateRs(dto, rqUuid))
                .onErrorResume(ex -> Mono.error(new BaseException(rqUuid, new Date(), 500, ex.getMessage())))
                .doOnSuccess(postTypeListDto -> log.debug("<< getAllType: {}", postTypeListDto));
    }

    @DeleteMapping(value = "/type")
    public Mono<PostTypeDtoRs> removeTypeById(@RequestHeader("RqUuid") @NotBlank String rqUuid, @RequestHeader("Id") @NotNull Long id) {
        log.debug(">> removeTypeById: rqUuid - {}, id - {}", rqUuid, id);
        return Mono.just(id)
                .map(postTypeId -> {
                    postTypeRepository.deleteById(postTypeId);
                    return new PostTypeDtoRs();
                })
                .map(rs -> updateRs(rs, rqUuid))
                .onErrorResume(ex -> Mono.error(new BaseException(rqUuid, new Date(), 500, ex.getMessage())))
                .doOnSuccess(rs -> log.debug("<< removeTypeById: {}", rs));
    }

    @PutMapping("/type")
    public Mono<PostTypeDtoRs> updateType(@RequestBody @Valid PostTypeDtoRq rq) {
        log.debug(">> updateType: {}", rq);
        return Mono.just(rq)
                .map(postMapper::mapToEntity)
                .map(postTypeRepository::save)
                .map(postMapper::mapToDto)
                .map(dto -> updateRs(dto, rq.getRqUuid()))
                .onErrorResume(ex -> Mono.error(new BaseException(rq.getRqUuid(), new Date(), 500, ex.getMessage())))
                .doOnSuccess(postTypeDto -> log.debug("<< updateType: {}", postTypeDto));
    }

    private <T extends BaseRs> T updateRs(T rs, String rqUuid) {
        rs.setRqUuid(rqUuid);
        rs.setRqTm(new Date());
        return rs;
    }

}
