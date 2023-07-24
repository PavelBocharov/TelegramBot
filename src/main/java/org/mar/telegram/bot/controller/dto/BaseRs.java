package org.mar.telegram.bot.controller.dto;

import lombok.*;

import java.io.Serializable;
import java.util.Date;

@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class BaseRs implements Serializable {

    private String rqUuid;
    private Date rqTm;
}
