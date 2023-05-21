package org.mar.telegram.bot.service.bot.db;

import org.mar.telegram.bot.service.db.dto.ActionEnum;
import org.mar.telegram.bot.service.db.dto.ActionPostDto;

import java.util.Map;

public interface ActionService {

    ActionPostDto getByPostIdAndUserInfoId(Long postInfoId, Long userId);
    ActionPostDto save(ActionPostDto actionPost);
    Map<ActionEnum, Long> countByPostIdAndAction(Long postId);

}
