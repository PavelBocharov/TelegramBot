package com.mar.telegram.db.mapper;

import com.mar.dto.tbot.ContentType;
import com.mar.dto.rest.PostInfoDtoRq;
import com.mar.dto.rest.PostInfoDtoRs;
import com.mar.dto.rest.PostTypeDtoRq;
import com.mar.dto.rest.PostTypeDtoRs;
import com.mar.telegram.db.entity.PostInfo;
import com.mar.telegram.db.entity.PostType;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
public interface PostMapper {

    @Mapping(target = "typeDir", source = "postInfo.type.typeDir")
    PostInfoDtoRs mapToDto(PostInfo postInfo);

    @Mapping(target = "type", source = "dto.typeDir")
    PostInfo mapToEntity(PostInfoDtoRq dto);

    PostType mapToEntity(PostTypeDtoRq dto);

    PostTypeDtoRs mapToDto(PostType entity);

    default ContentType getContentTypeByDir(String typeDir) {
        return ContentType.getTypeByDir(typeDir);
    }

}
