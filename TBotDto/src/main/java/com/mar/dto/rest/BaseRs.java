package com.mar.dto.rest;

import com.mar.utils.Utils;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.With;

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

    public String getHTML() {
        StringBuilder sb = new StringBuilder();
        sb.append("RqUUID: ").append(rqUuid).append("</br>");
        sb.append("RqTm: ").append(Utils.getISOFormat(rqTm)).append("</br>");
        if (errorCode != null && errorCode > 0) {
            sb.append("Error code: ").append(errorCode).append("</br>");
            sb.append("Error msg: ").append(errorMsg);
        }
        return sb.toString();
    }
}
