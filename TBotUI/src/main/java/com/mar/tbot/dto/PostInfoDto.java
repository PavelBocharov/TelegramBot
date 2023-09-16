package com.mar.tbot.dto;

import lombok.*;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class PostInfoDto extends BaseRq {

    private Long userId;
    private String filePath;
    private Map<String, String> caption;
    private List<String> hashTags;

}