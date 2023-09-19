package com.mar.tbot.dto;

import lombok.*;

import java.util.List;

@With
@Getter
@Setter
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class PostTypeDtoRs extends BaseRs {

    private Long id;
    private String title;
    private List<String> lines;

}
