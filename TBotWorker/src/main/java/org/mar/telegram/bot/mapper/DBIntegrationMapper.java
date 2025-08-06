package org.mar.telegram.bot.mapper;

import com.mar.dto.rest.ActionPostDtoRq;
import com.mar.dto.rest.ActionPostDtoRs;
import com.mar.dto.rest.PostInfoDtoRq;
import com.mar.dto.rest.PostInfoDtoRs;
import org.mapstruct.Mapper;

import java.util.Date;

@Mapper
public interface DBIntegrationMapper {

    PostInfoDtoRs mapRqToRs(PostInfoDtoRq rq);

    PostInfoDtoRq mapRsToRq(PostInfoDtoRs rq);

    ActionPostDtoRs mapRqToRs(ActionPostDtoRq rq);

    ActionPostDtoRq mapRsToRq(ActionPostDtoRs rq);

    default Long mapDate(Date date) {
        return date == null ? null : date.getTime();
    }

    default Date mapDate(Long date) {
        return date == null ? null : new Date(date);
    }

}
