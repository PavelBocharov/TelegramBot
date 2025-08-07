package com.mar.telegram.db.mapper;

import com.mar.dto.rest.HashTagDto;
import com.mar.telegram.db.entity.HashTag;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper
public interface HashtagMapper {

    HashTagDto mapToDto(HashTag tag);

    List<HashTagDto> mapToDto(List<HashTag> tags);

    HashTag mapToEntity(HashTagDto tag);

    List<HashTag> mapToEntity(List<HashTagDto> list);

}
