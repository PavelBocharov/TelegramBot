package org.mar.telegram.bot.controller.dto;

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
public class BaseRq implements Serializable {

    @NotBlank
    protected String rqUuid;
    @NotNull
    protected Date rqTm;

}
