package com.mar.tbot.dto.sendMsg;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class PhotoSizeDto implements Serializable {
    private String fileId;
    private Long fileSize;
}
