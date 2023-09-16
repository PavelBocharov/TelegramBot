package com.mar.telegram.db.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import java.util.Arrays;

@Getter
@ToString
@AllArgsConstructor
public enum ContentType {
    Text(null),
    Picture("photos"),
    Video("videos"),
    Gif("gif"),
    Doc("documents");

    private String typeDir;

    public static ContentType getTypeByDir(String dir) {
        if (dir == null) {
            return null;
        }
        return Arrays.stream(ContentType.values())
                .filter(contentType -> dir.equals(contentType.getTypeDir()))
                .findFirst()
                .get();
    }
}
