package com.mar.telegram.db.mapper;

import com.mar.dto.rest.ActionPostDtoRs;
import com.mar.telegram.db.entity.ActionPost;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
public interface ActionMapper {

    @Mapping(target = "postId", source = "actionPost.post.id")
    @Mapping(target = "userId", source = "actionPost.userInfo.id")
    @Mapping(target = "actionCallbackData", source = "actionPost.action.callbackData")
    ActionPostDtoRs mapToDto(ActionPost actionPost);

}
