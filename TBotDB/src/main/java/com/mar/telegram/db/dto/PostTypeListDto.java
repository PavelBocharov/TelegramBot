package com.mar.telegram.db.dto;

import lombok.*;

import java.util.List;

@With
@Getter
@Setter
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class PostTypeListDto extends BaseDto {

    private List<PostTypeDto> postTypeList;

}
