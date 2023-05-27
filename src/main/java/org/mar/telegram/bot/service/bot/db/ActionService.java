package org.mar.telegram.bot.service.bot.db;

import org.mar.telegram.bot.service.db.dto.ActionEnum;
import org.mar.telegram.bot.service.db.dto.ActionPostDto;

import java.util.Map;

public interface ActionService {

    ActionPostDto getByPostIdAndUserInfoId(String rqUuid, Long postInfoId, Long userId);
    ActionPostDto save(String rqUuid, ActionPostDto actionPost);
    Map<ActionEnum, Long> countByPostIdAndAction(String rqUuid, Long postId);

}
