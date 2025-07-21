package com.mar.telegram.db.controller;

import com.mar.dto.rest.HashTagListDtoRq;
import com.mar.dto.rest.HashTagListDtoRs;
import com.mar.exception.BaseException;
import com.mar.telegram.db.jpa.HashTagRepository;
import com.mar.telegram.db.mapper.HashtagMapper;
import com.mar.utils.RestApiUtils;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mapstruct.factory.Mappers;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
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
@RequestMapping(value = "/hashtag")
@RequiredArgsConstructor
public class HashtagController {

    private final HashTagRepository repository;

    private HashtagMapper mapper = Mappers.getMapper(HashtagMapper.class);

    @PostMapping
    public Mono<HashTagListDtoRs> create(@RequestBody @Valid HashTagListDtoRq rq) {
        return Flux.fromIterable(rq.getTags())
                .map(mapper::mapToEntity)
                .map(repository::save)
                .map(mapper::mapToDto)
                .collectList()
                .map(HashTagListDtoRs::new)
                .map(dto -> RestApiUtils.enrichRs(dto, rq.getRqUuid()))
                .onErrorResume(ex -> Mono.error(new BaseException(rq.getRqUuid(), new Date(), 500, ex.getMessage())))
                .doOnSuccess(userDto -> log.debug("<< create hashtag: {}", userDto));
    }

    @GetMapping
    public Mono<HashTagListDtoRs> getAll(@RequestHeader("RqUuid") @NotBlank String rqUuid) {
        return Mono.just(repository.findAll())
                .map(mapper::mapToDto)
                .map(HashTagListDtoRs::new)
                .map(dto -> RestApiUtils.enrichRs(dto, rqUuid))
                .onErrorResume(ex -> Mono.error(new BaseException(rqUuid, new Date(), 500, ex.getMessage())))
                .doOnSuccess(userDto -> log.debug("<< get all hashtag: {}", userDto));
    }

    @DeleteMapping
    public Mono<HashTagListDtoRs> removeById(@RequestHeader("RqUuid") @NotBlank String rqUuid, @RequestHeader("Id") @NotNull Long id) {
        return Mono.just(id)
                .map(idForDelete -> {
                    repository.deleteById(idForDelete);
                    return RestApiUtils.enrichRs(new HashTagListDtoRs(), rqUuid);
                })
                .onErrorResume(ex -> Mono.error(new BaseException(rqUuid, new Date(), 500, ex.getMessage())))
                .doOnSuccess(userDto -> log.debug("<< remove hashtag by id: {}", userDto));
    }

    @PutMapping
    public Mono<HashTagListDtoRs> update(@RequestBody @Valid HashTagListDtoRq rq) {
        return Flux.fromIterable(rq.getTags())
                .map(mapper::mapToEntity)
                .map(repository::save)
                .map(mapper::mapToDto)
                .collectList()
                .map(HashTagListDtoRs::new)
                .map(dto -> RestApiUtils.enrichRs(dto, rq.getRqUuid()))
                .onErrorResume(ex -> Mono.error(new BaseException(rq.getRqUuid(), new Date(), 500, ex.getMessage())))
                .doOnSuccess(userDto -> log.debug("<< update hashtag: {}", userDto));
    }

}
