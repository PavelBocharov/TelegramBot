package org.mar.telegram.bot.utils;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@AllArgsConstructor
public enum ContentType {
    Text(null),
    Picture("pictures"),
    Video("videos"),
    Gif("gif"),
    Doc("documents");

    private String typeDit;
}
