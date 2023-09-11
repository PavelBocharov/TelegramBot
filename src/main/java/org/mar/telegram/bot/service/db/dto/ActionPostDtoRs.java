package org.mar.telegram.bot.service.db.dto;

import lombok.*;
import org.mar.telegram.bot.controller.dto.BaseRs;

@With
@Getter
@Setter
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class ActionPostDtoRs extends BaseRs {

    private Long id;
    private Long postId;
    private Long userId;
    private String actionCallbackData;

}
