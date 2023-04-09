package org.mar.telegram.bot.utils;

import lombok.experimental.UtilityClass;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Arrays;
import java.util.List;

@UtilityClass
public class ParsingTextUtils {

    public static final String IMGUR_URL = "https://i.imgur.com/";
    public static final String IMGUR_GIFV_TYPE = ".gifv";
    public static final String MP4_Type = ".mp4";
    public static final String GIF_Type = ".gif";
    public static final String PNG_Type = ".png";
    public static final String JPG_Type = ".jpg";
    public static final String JPEG_Type = ".jpeg";
    public static final String BMP_Type = ".bmp";

    public static final List<String> PICTURE_Type_List = Arrays.stream(ArrayUtils.toArray(
            PNG_Type.substring(1),
            JPG_Type.substring(1),
            JPEG_Type.substring(1),
            BMP_Type.substring(1)
    )).toList();

    public static Pair<ContentType, String> whatIsUrl(String url) {
        String type = FilenameUtils.getExtension(url.toLowerCase());
        if (StringUtils.isEmpty(type)) {
            return new ImmutablePair(ContentType.Text, url);
        }
        if (MP4_Type.endsWith(type)) {
            return new ImmutablePair(ContentType.Video, url);
        }
        if (GIF_Type.endsWith(type)) {
            return new ImmutablePair(ContentType.Gif, url);
        }
        if (PICTURE_Type_List.contains(type)) {
            return new ImmutablePair(ContentType.Picture, url);
        }
        Pair<ContentType, String> rez = null;
        if ((rez = itsImgurHosting(url)) != null) {
            return rez;
        }
        if ((rez = itsXVideoHosting(url)) != null) {
            return rez;
        }

        return new ImmutablePair(ContentType.Text, url);
    }

    private static Pair<ContentType, String> itsImgurHosting(String url) {
        String fileName = FilenameUtils.getBaseName(url);
        url = url.toLowerCase();
        if (url.startsWith(IMGUR_URL) && url.endsWith(IMGUR_GIFV_TYPE)) {
            String tempUrl = null;
            if (!url.endsWith(MP4_Type)) {
                tempUrl = IMGUR_URL + fileName + MP4_Type;
            } else {
                tempUrl = url;
            }
            return new ImmutablePair(ContentType.Video, tempUrl);
        }
        return null;
    }

    private static Pair<ContentType, String> itsXVideoHosting(String url) {
        return null;
    }

}
