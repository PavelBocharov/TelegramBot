package org.mar.telegram.bot.controller.dto;


import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Информация об изображения.")
public class PhotoSizeDto implements Serializable {


    @Schema(description = "ID изображения в Telegram.")
    private String fileId;

    @Min(0)
    @Max(value = 10485760, message = "Должно быть 10МБ, но в байтах или килобайтах передается не помню.")
    @Schema(description = "Размер изображения в Telegram. Должно быть 10МБ, но в байтах или килобайтах передается - не помню.")
    private Long fileSize;
}
