package com.mar.telegram.db.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mar.dto.rest.PostInfoActionRs;
import com.mar.dto.rest.PostInfoDtoRq;
import com.mar.dto.rest.PostInfoDtoRs;
import com.mar.dto.rest.PostTypeDtoRq;
import com.mar.dto.rest.PostTypeDtoRs;
import com.mar.dto.tbot.ContentType;
import com.mar.telegram.db.entity.PostInfo;
import com.mar.telegram.db.entity.PostInfoAction;
import com.mar.telegram.db.entity.PostType;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;
import java.util.Map;

@Mapper
public interface PostMapper {

    @Mapping(target = "typeDir", source = "postInfo.type.typeDir")
    PostInfoDtoRs mapToDto(PostInfo postInfo);

    @Mapping(target = "type", source = "dto.typeDir")
    PostInfo mapToEntity(PostInfoDtoRq dto);

    PostType mapToEntity(PostTypeDtoRq dto);

    PostTypeDtoRs mapToDto(PostType entity);

    default PostInfoAction convert(Object[] map) {
        PostInfoAction pia = new PostInfoAction();

        pia.setId((Long) map[0]);
        pia.setAction0((Long) map[1]);
        pia.setAction1((Long) map[2]);
        pia.setAction2((Long) map[3]);
        pia.setAction3((Long) map[4]);
        pia.setCaption((String) map[5]);
        pia.setAdminAction((Boolean) map[6]);

        return pia;
    };

    PostInfoActionRs convert (PostInfoAction entity);

    List<PostInfoActionRs> convert(List<Object[]> map);

    default ContentType getContentTypeByDir(String typeDir) {
        return ContentType.getTypeByDir(typeDir);
    }

}
