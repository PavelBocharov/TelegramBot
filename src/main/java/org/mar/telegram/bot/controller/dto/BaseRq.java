package org.mar.telegram.bot.controller.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.io.Serializable;
import java.util.Date;

@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class BaseRq implements Serializable {

    @NotBlank
    private String rqUuid;
    @NotNull
    private Date rqTm;

}
