package com.mar.telegram.db.mapper;

import com.mar.telegram.db.dto.ActionPostDto;
import com.mar.telegram.db.dto.HashTagDto;
import com.mar.telegram.db.dto.HashTagListDto;
import com.mar.telegram.db.entity.ActionPost;
import com.mar.telegram.db.entity.HashTag;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper
public interface HashtagMapper {

    HashTagDto mapToDto(HashTag tag);

    List<HashTagDto> mapToDto(List<HashTag> tags);

    HashTag mapToEntity(HashTagDto tag);

    List<HashTag> mapToEntity(List<HashTagDto> list);

}
