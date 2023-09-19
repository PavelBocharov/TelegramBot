package org.mar.telegram.bot.controller.dto;

import lombok.*;

import java.io.Serializable;
import java.util.Date;

@With
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class BaseRs implements Serializable {

    protected String rqUuid;
    protected Date rqTm;
    protected Integer errorCode;
    protected String errorMsg;

}
