package com.mar.tbot.dto;

import lombok.*;

import java.util.List;

@With
@Getter
@Setter
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class HashTagListDtoRs extends BaseRs {

    private List<HashTagDto> tags;

}
