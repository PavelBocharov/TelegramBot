package org.mar.telegram.bot.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.io.Serializable;
import java.util.Date;

@With
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Обязательные данные для сквозного процесса (RQ).")
public class BaseRq implements Serializable {

    @NotBlank
    @Schema(description = "Сквозной идентификатор", example = "04d6895d-4b12-46e4-8c7f-2a69e0c6bbe6")
    protected String rqUuid;

    @NotNull
    @Schema(description = "Время отправки сообщения клиентом", example = "1707740370142 или 2024-02-12T12:27:57.278Z")
    protected Date rqTm;

}
