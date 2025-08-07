package com.mar.dto.rest;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Информация для размещения поста.")
public class SendPostRq extends BaseRq {

    @NotNull
    @Schema(description = "ID администратора. Берется из https://t.me/username_to_id_bot")
    private Long userId;

    @NotBlank
    @Schema(description = "Путь до файла для размещения в группе. Типы: video (.gifv, .mp4, .mpeg, .ogg, .quicktime, .webm), .gif, picture (.png, .jpg, .jpeg, .bmp).",
            example = "G:\\Cache\\dev\\dev_temp\\n2TMq-IJr3E.jpg")
    private String filePath;

    @Schema(description = "Сообщение: строки ключ-значение. Пример - \"caption\": {\n" +
            "    \"1\": \"Name: Thomas\",\n" +
            "    \"2\": \"Bread: Street cat\"\n" +
            "  }")
    private Map<Long, String> caption;

    @Schema(description = "Список хештегов. Пример - \"hashTags\": [\n" +
            "    \"string\", \"string 1\"\n" +
            "  ]")
    private List<String> hashTags;

    @Schema(description = "Флаг публикации с ватермаркой. NULL - не печатать, 1 - top left, 2 - top right, 3 - down left, 4 - down right.")
    private Integer printWatermark;

}
