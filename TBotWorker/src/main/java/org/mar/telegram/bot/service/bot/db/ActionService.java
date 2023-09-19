package org.mar.telegram.bot.service.bot.db;

import org.mar.telegram.bot.service.db.dto.ActionEnum;
import org.mar.telegram.bot.service.db.dto.ActionPostDtoRs;

import java.util.Map;

public interface ActionService {

    ActionPostDtoRs getByPostIdAndUserInfoId(String rqUuid, Long postInfoId, Long userId);
    ActionPostDtoRs save(String rqUuid, ActionPostDtoRs actionPost);
    Map<ActionEnum, Long> countByPostIdAndAction(String rqUuid, Long postId);

}
