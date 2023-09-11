package org.mar.telegram.bot.service.db.dto;

import lombok.*;
import org.mar.telegram.bot.controller.dto.BaseRq;
import org.mar.telegram.bot.controller.dto.BaseRs;

@With
@Getter
@Setter
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class PostInfoDtoRq extends BaseRq {

    private Long id;
    private String mediaPath;
    private String caption;
    private Long chatId;
    private Integer messageId;
    private Boolean isSend = false;
    private String typeDir;

}