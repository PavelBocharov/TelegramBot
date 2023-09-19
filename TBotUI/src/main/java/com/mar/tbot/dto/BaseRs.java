package com.mar.tbot.dto;

import lombok.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Date;

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

}
