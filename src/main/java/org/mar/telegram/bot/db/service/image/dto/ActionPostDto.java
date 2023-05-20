package org.mar.telegram.bot.db.service.image.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActionPostDto implements Serializable {

    private Long id;
    private Long postId;
    private Long userId;
    private String actionCallbackData;

}
