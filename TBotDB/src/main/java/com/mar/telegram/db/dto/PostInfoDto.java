package com.mar.telegram.db.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class PostInfoDto extends BaseDto {

    private Long id;
    private String mediaPath;
    private String caption;
    private Long chatId;
    private Integer messageId;
    private Boolean isSend = false;
    private String typeDir;

}