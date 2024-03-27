package com.mar.dto.rest;

import lombok.*;

import java.util.List;

@With
@Getter
@Setter
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class PostTypeListDtoRs extends BaseRs {

    private List<PostTypeDtoRs> postTypeList;

}
