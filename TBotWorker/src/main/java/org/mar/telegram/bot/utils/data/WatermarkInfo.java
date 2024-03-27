package org.mar.telegram.bot.utils.data;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.With;

import java.io.Serializable;

@With
@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class WatermarkInfo implements Serializable {

    private String text;
    private String textFontName = "Areal";
    private Integer textFontSize = 64;
    private String textColorHex = "000000";
    private Integer textColorAlpha = 100;
    private String textLocation;

    private String imagePath;
    private String imageLocation;
    private Integer imageSizeX;
    private Integer imageSizeY;

}
