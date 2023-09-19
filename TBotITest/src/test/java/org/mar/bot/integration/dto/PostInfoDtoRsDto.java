package org.mar.bot.integration.dto;

import lombok.*;

import java.io.Serializable;
import java.util.Date;

@With
@Getter
@Setter
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class PostInfoDtoRsDto implements Serializable {

    protected String rqUuid;
    protected Date rqTm;
    private Long id;
    private String mediaPath;
    private String caption;
    private Long chatId;
    private Integer messageId;
    private Boolean isSend = false;
    private String typeDir;

}