package org.mar.telegram.bot.utils;

import lombok.experimental.UtilityClass;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.UnsupportedMimeTypeException;
import org.jsoup.nodes.Document;
import org.mar.telegram.bot.service.jms.dto.URLInfo;

import java.util.Arrays;
import java.util.List;

import static org.mar.telegram.bot.utils.ContentType.*;

@UtilityClass
public class ParsingTextUtils {

    public static final String IMGUR_URL = "https://i.imgur.com/";
    public static final String IMGUR_GIFV_TYPE = ".gifv";
    // Video
    public static final String MP4_Type = ".mp4";
    public static final String MPEG_Type = ".mpeg";
    public static final String OGG_Type = ".ogg";
    public static final String QT_Type = ".quicktime";
    public static final String WEBM_Type = ".webm";
    // Gif
    public static final String GIF_Type = ".gif";
    // Picture
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

    public static URLInfo whatIsUrl(String url) {
        URLInfo rez = null;
        if ((rez = checkByMimeType(url)) != null) {
            return rez;
        }

        String type = FilenameUtils.getExtension(url.toLowerCase());
        if (StringUtils.isEmpty(type)) {
            return URLInfo.builder().contentType(Text).url(url).build();
        }
        if (MP4_Type.endsWith(type)) {
            return URLInfo.builder().contentType(Video).url(url).build();
        }
        if (GIF_Type.endsWith(type)) {
            return URLInfo.builder().contentType(Gif).url(url).build();
        }
        if (PICTURE_Type_List.contains(type)) {
            return URLInfo.builder().contentType(Picture).url(url).build();
        }
        if ((rez = itsImgurHosting(url)) != null
                || (rez = itsXVideoHosting(url)) != null
                || (rez = itsRedGifHosting(url)) != null
        ) {
            return rez;
        }

        return URLInfo.builder().contentType(Text).url(url).build();
    }

    private static URLInfo checkByMimeType(String url) {
        try {
            Document document = Jsoup.connect(url).get();
            System.out.println(document.documentType());
        } catch (UnsupportedMimeTypeException ex) {
            String mimeType = ex.getMimeType().toLowerCase();
            if (mimeType.startsWith("image")) {
                if (mimeType.endsWith("png")) return new URLInfo(Picture, url, PNG_Type);
                if (mimeType.endsWith("jpg")) return new URLInfo(Picture, url, JPG_Type);
                if (mimeType.endsWith("jpeg")) return new URLInfo(Picture, url, JPEG_Type);
                if (mimeType.endsWith("pjpeg")) return new URLInfo(Picture, url, JPEG_Type);
                if (mimeType.endsWith("gif")) return new URLInfo(Gif, url, GIF_Type);
            }
            if (mimeType.startsWith("video")) {
                if (mimeType.endsWith("mpeg")) return new URLInfo(Video, url, MPEG_Type);
                if (mimeType.endsWith("mp4")) return new URLInfo(Video, url, MP4_Type);
                if (mimeType.endsWith("ogg")) return new URLInfo(Video, url, OGG_Type);
                if (mimeType.endsWith("quicktime")) return new URLInfo(Video, url, QT_Type);
                if (mimeType.endsWith("webm")) return new URLInfo(Video, url, WEBM_Type);
            }
        } catch (Exception ex) {
            return null;
        }
        return null;
    }

    private static URLInfo itsImgurHosting(String url) {
        String fileName = FilenameUtils.getBaseName(url);
        url = url.toLowerCase();
        if (url.startsWith(IMGUR_URL) && url.endsWith(IMGUR_GIFV_TYPE)) {
            String tempUrl = null;
            if (!url.endsWith(MP4_Type)) {
                tempUrl = IMGUR_URL + fileName + MP4_Type;
            } else {
                tempUrl = url;
            }
            return URLInfo.builder().contentType(Video).url(tempUrl).build();
        }
        return null;
    }

    private static URLInfo itsXVideoHosting(String url) {
        // TODO Load from XVideo
        return null;
    }

    private static URLInfo itsRedGifHosting(String url) {
        // TODO Load from RedGif
        return null;
    }

}
