package com.mar.telegram.db.mapper;

import com.mar.telegram.db.dto.UserDto;
import com.mar.telegram.db.entity.ActionPost;
import com.mar.telegram.db.entity.UserInfo;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
public interface UserMapper {

    @Mapping(target = "actionIds", source = "user.actionPosts")
    UserDto mapToDto(UserInfo user);

    default Long getActionId(ActionPost actionPost) {
        if (actionPost != null) return actionPost.getId();
        return null;
    }

}
