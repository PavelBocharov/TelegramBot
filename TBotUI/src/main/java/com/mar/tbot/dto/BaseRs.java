package com.mar.tbot.dto;

import com.mar.tbot.utils.Utils;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class BaseRs implements Serializable {

    @NotBlank
    protected String rqUuid;
    @NotNull
    protected Date rqTm;
    protected Integer errorCode = 0;
    protected String errorMsg = "OK";

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
