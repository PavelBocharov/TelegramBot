package org.mar.telegram.bot.controller.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class MessageDto implements Serializable {

    @NotNull
    private Long fromUserId;
    private String text;
    private Long chatId;
    private List<PhotoSizeDto> photoSizeList;
    private String documentFileId;
    private String videoFileId;
    private String animationFileId;

}
