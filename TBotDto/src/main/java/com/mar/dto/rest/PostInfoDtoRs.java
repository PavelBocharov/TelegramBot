package com.mar.dto.rest;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.With;

import java.util.Date;

@With
@Getter
@Setter
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class PostInfoDtoRs extends BaseRs {

    private Long id;
    private String mediaPath;
    private String caption;
    private Long chatId;
    private Integer messageId;
    private Boolean isSend = false;
    private String typeDir;
    private Boolean sendFlag = false;
    private Date scheduleSendTime;

}