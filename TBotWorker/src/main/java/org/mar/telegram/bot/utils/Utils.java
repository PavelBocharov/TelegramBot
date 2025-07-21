package org.mar.telegram.bot.utils;

import com.mar.dto.mq.URLInfo;
import com.mar.dto.tbot.ContentType;
import com.mar.dto.tbot.PhotoSizeDto;
import com.mar.exception.BaseException;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.jsoup.Jsoup;
import org.jsoup.UnsupportedMimeTypeException;
import org.jsoup.nodes.Document;
import org.mar.telegram.bot.service.logger.LoggerService;
import org.mar.telegram.bot.utils.data.WatermarkInfo;
import org.springframework.stereotype.Component;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import javax.imageio.ImageIO;

import static com.mar.dto.mq.LogEvent.LogLevel.DEBUG;
import static com.mar.dto.tbot.ContentType.Gif;
import static com.mar.dto.tbot.ContentType.Picture;
import static com.mar.dto.tbot.ContentType.Text;
import static com.mar.dto.tbot.ContentType.Video;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.springframework.util.ObjectUtils.isEmpty;

@Component
@RequiredArgsConstructor
public class Utils {

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
    private final LoggerService loggerService;

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
            return URLInfo.builder().contentType(Video).url(url).fileType(type).build();
        }
        if (GIF_Type.endsWith(type)) {
            return URLInfo.builder().contentType(Gif).url(url).fileType(type).build();
        }
        if (PICTURE_Type_List.contains(type)) {
            return URLInfo.builder().contentType(Picture).url(url).fileType(type).build();
        }
        if ((rez = itsImgurHosting(url)) != null) {
            return rez;
        }

        return URLInfo.builder().contentType(Text).url(url).build();
    }

    public static ContentType getTypeByType(String dirPath) {
        return whatIsUrl(dirPath).getContentType();
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

    public static PhotoSizeDto getMaxPhotoSize(List<PhotoSizeDto> photoSizes) {
        if (isEmpty(photoSizes)) return null;

        PhotoSizeDto ps = null;
        for (PhotoSizeDto photoSize : photoSizes) {
            if (ps == null) {
                ps = photoSize;
            } else {
                if (ps.getFileSize() < photoSize.getFileSize()) {
                    ps = photoSize;
                }
            }
        }
        return ps;
    }

    public static String removeFile(final String filePath) {
        try {
            File file = new File(filePath);
            if (file.exists()) {
                file.delete();
            }
            return null;
        } catch (Exception ex) {
            return ExceptionUtils.getStackTrace(ex);
        }
    }

    public static int[] convertColor(String hex) {
        assert isNotBlank(hex);

        String hexCode = hex.replace("#", "");

        assert hexCode.length() == 6;

        return new int[]{
                Integer.valueOf(hexCode.substring(0, 2), 16),
                Integer.valueOf(hexCode.substring(2, 4), 16),
                Integer.valueOf(hexCode.substring(4, 6), 16)
        };
    }

    /**
     *
     * @param rqUuid
     * @param imagePath
     * @param watermark
     * @param position      NULL - не печатать, 1 - top left, 2 - top right, 3 - down left, 4 - down right.
     * @return
     */
    public String addWatermark(String rqUuid, String imagePath, WatermarkInfo watermark, Integer position) {
        if (position == null || (position != 1 && position != 2 && position != 3 && position != 4)) {
            loggerService.sendLog(rqUuid, DEBUG, "Not add watermark on image - watermark position: {}", position);
            return null;
        }
        BufferedImage img = null;
        File f = null;
        if (watermark != null) {
            f = new File(watermark.getImagePath());
            if (isBlank(watermark.getText()) && !f.exists()) {
                loggerService.sendLog(rqUuid, DEBUG, "Cannot add watermark on image - path: '{}', watermark info: {}", imagePath, watermark);
                return null;
            }
        }

        loggerService.sendLog(rqUuid, DEBUG, "Add watermark on image - path: '{}', watermark info: {}", imagePath, watermark);

        try {
            // IMAGE
            f = new File(imagePath);
            img = ImageIO.read(f);

            BufferedImage temp = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_ARGB_PRE);
            Graphics graphics = temp.getGraphics();
            graphics.drawImage(img, 0, 0, null);

            if (isNotBlank(watermark.getImagePath())) {
                loggerService.sendLog(rqUuid, DEBUG, "Add image on image - watermark image path: {}", watermark.getImagePath());
                addImageOnImage(graphics, img.getWidth(), img.getHeight(), watermark, position);
            }

            if (isNotBlank(watermark.getText())) {
                loggerService.sendLog(rqUuid, DEBUG, "Add text on image - watermark text: {}", watermark.getText());
                addTextOnImage(graphics, img.getWidth(), img.getHeight(), watermark);
            }

            graphics.dispose();

            f = new File(
                    f.getAbsolutePath().replace(f.getName(), "")
                            + new Date().getTime()
                            + ".png"
            );
            ImageIO.write(temp, "png", f);

            if (f.exists()) {
                return f.getAbsolutePath();
            } else {
                loggerService.sendLog(rqUuid, DEBUG, "Cannot used new image with watermark - used old file.");
                return null;
            }
        } catch (Exception e) {
            throw new BaseException(rqUuid, new Date(), 500, "Cannot add watermark on image: " + ExceptionUtils.getRootCauseMessage(e), e);
        }
    }

    private void addImageOnImage(Graphics mainImage, int imgWidth, int imgHeight, WatermarkInfo watermark, int position) throws IOException {
        BufferedImage mwImg = null;
        File wm = null;
        wm = new File(watermark.getImagePath());
        mwImg = ImageIO.read(wm);
        int posX = 0;
        int posY = 0;
        // 1 - top left, 2 - top right, 3 - down left, 4 - down right
        if (position == 1) {
            posX = 0;
            posY = 0;
        }
        if (position == 2) {
            posX = 0;
            posY = imgHeight - watermark.getImageSizeY();
        }
        if (position == 3) {
            posX = imgWidth - watermark.getImageSizeX();
            posY = 0;
        }
        if (position == 4) {
            posX = imgWidth - watermark.getImageSizeX();
            posY = imgHeight - watermark.getImageSizeY();
        }

        mainImage.drawImage(
                mwImg,
                posX, posY, // location
                watermark.getImageSizeX(), watermark.getImageSizeY(), // size
                null
        );
    }

    private void addTextOnImage(Graphics mainImage, int imgWidth, int imgHeight, WatermarkInfo watermark) {
        mainImage.setFont(new Font(watermark.getTextFontName(), Font.PLAIN, watermark.getTextFontSize()));
        int[] textColor = Utils.convertColor(watermark.getTextColorHex());
        mainImage.setColor(new Color(textColor[0], textColor[1], textColor[2], watermark.getTextColorAlpha()));

        mainImage.drawString(watermark.getText(), 1, watermark.getTextFontSize());
//        mainImage.drawString(watermark.getText(), imgWidth / 10, imgHeight / 10);
    }
}
