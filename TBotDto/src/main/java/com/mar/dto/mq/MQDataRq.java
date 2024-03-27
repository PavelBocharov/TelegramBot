package com.mar.dto.mq;

import lombok.*;

import java.io.Serializable;
import java.util.Date;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class MQDataRq implements Serializable {

    private String rqUuid;
    private Date date;
    private MQDataType type;
    private Object body;

    public enum MQDataType {
        FILE_INFO, LOG
    }

}
