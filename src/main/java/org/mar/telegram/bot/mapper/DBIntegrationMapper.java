package org.mar.telegram.bot.mapper;

import org.mapstruct.Mapper;
import org.mar.telegram.bot.service.db.dto.ActionPostDtoRq;
import org.mar.telegram.bot.service.db.dto.ActionPostDtoRs;
import org.mar.telegram.bot.service.db.dto.PostInfoDtoRs;
import org.mar.telegram.bot.service.db.dto.PostInfoDtoRq;

@Mapper
public interface DBIntegrationMapper {

    PostInfoDtoRs mapRqToRs(PostInfoDtoRq rq);
    PostInfoDtoRq mapRsToRq(PostInfoDtoRs rq);
    ActionPostDtoRs mapRqToRs(ActionPostDtoRq rq);
    ActionPostDtoRq mapRsToRq(ActionPostDtoRs rq);

}
