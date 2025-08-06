package com.mar.dto.tbot;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Информация об изменяемом сообщение.")
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
