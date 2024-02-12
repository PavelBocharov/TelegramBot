package org.mar.telegram.bot.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
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
@Schema(description = "Обязательные данные для сквозного процесса (RS).")
public class BaseRs implements Serializable {

    @NotBlank
    @Schema(description = "Сквозной идентификатор", example = "04d6895d-4b12-46e4-8c7f-2a69e0c6bbe6")
    protected String rqUuid;

    @NotNull
    @Schema(description = "Время отправки сообщения клиентом")
    protected Date rqTm;

    @Min(value = 0)
    @Schema(description = "Код ошибки (0 - ошибки нет).")
    protected Integer errorCode;

    @Schema(description = "Текстовка ошибка")
    protected String errorMsg;

}
