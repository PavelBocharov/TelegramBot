package com.mar.telegram.db.controller;

import com.mar.telegram.db.dto.BaseDto;
import com.mar.telegram.db.dto.HashTagListDto;
import com.mar.telegram.db.exception.BaseException;
import com.mar.telegram.db.jpa.HashTagRepository;
import com.mar.telegram.db.mapper.HashtagMapper;
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
@RequestMapping(value = "/hashtag")
@RequiredArgsConstructor
public class HashtagController {

    private final HashTagRepository repository;

    private HashtagMapper mapper = Mappers.getMapper(HashtagMapper.class);

    @PostMapping
    public Mono<HashTagListDto> create(@RequestBody @Valid HashTagListDto rq) {
        return Flux.fromIterable(rq.getTags())
                .map(mapper::mapToEntity)
                .map(repository::save)
                .map(mapper::mapToDto)
                .collectList()
                .map(HashTagListDto::new)
                .map(dto -> updateRs(dto, rq.getRqUuid()))
                .onErrorResume(ex -> Mono.error(new BaseException(rq.getRqUuid(), new Date(), 500, ex.getMessage())));
    }

    @GetMapping
    public Mono<HashTagListDto> getAll(@RequestHeader("RqUuid") @NotBlank String rqUuid) {
        return Mono.just(repository.findAll())
                .map(mapper::mapToDto)
                .map(HashTagListDto::new)
                .map(dto -> updateRs(dto, rqUuid))
                .onErrorResume(ex -> Mono.error(new BaseException(rqUuid, new Date(), 500, ex.getMessage())));
    }

    @DeleteMapping
    public Mono<BaseDto> removeById(@RequestHeader("RqUuid") @NotBlank String rqUuid, @RequestHeader("Id") @NotNull Long id) {
        return Mono.just(id)
                .map(idForDelete -> {
                    repository.deleteById(idForDelete);
                    return new BaseDto().withRqUuid(rqUuid).withRqTm(new Date());
                })
                .onErrorResume(ex -> Mono.error(new BaseException(rqUuid, new Date(), 500, ex.getMessage())));
    }

    @PutMapping
    public Mono<HashTagListDto> update(@RequestBody @Valid HashTagListDto rq) {
        return Flux.fromIterable(rq.getTags())
                .map(mapper::mapToEntity)
                .map(repository::save)
                .map(mapper::mapToDto)
                .collectList()
                .map(HashTagListDto::new)
                .map(dto -> updateRs(dto, rq.getRqUuid()))
                .onErrorResume(ex -> Mono.error(new BaseException(rq.getRqUuid(), new Date(), 500, ex.getMessage())));
    }

    private HashTagListDto updateRs(HashTagListDto rs, String rqUuid) {
        rs.setRqUuid(rqUuid);
        rs.setRqTm(new Date());
        return rs;
    }
}
