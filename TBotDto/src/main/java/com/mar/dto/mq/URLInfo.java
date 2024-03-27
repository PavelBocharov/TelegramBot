package com.mar.dto.mq;

import com.mar.dto.tbot.ContentType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class URLInfo {

    private ContentType contentType;
    private String url;
    private String fileType;

}
