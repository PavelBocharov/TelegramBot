package com.mar.telegram.db.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.io.Serializable;
import java.util.Date;

@With
@Getter
@Setter
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BaseDto implements Serializable {

    @NotBlank
    protected String rqUuid;
    @NotNull
    protected Date rqTm;

    protected Integer errorCode;
    protected String errorMsg;

}
