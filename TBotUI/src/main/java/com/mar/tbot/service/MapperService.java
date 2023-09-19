package com.mar.tbot.service;

import com.mar.tbot.utils.jsonDialog.mapper.ActionMapper;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Getter
@Service
@RequiredArgsConstructor
public class MapperService {

    @Deprecated(since = "For example")
    private final ActionMapper actionMapper;

}
