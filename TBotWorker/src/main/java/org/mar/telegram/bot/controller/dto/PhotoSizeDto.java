package org.mar.telegram.bot.controller.dto;


import lombok.*;

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
