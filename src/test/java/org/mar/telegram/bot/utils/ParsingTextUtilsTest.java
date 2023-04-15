package org.mar.telegram.bot.utils;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mar.telegram.bot.service.jms.dto.URLInfo;

import java.util.UUID;
import java.util.stream.Stream;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.junit.jupiter.api.Assertions.*;
import static org.mar.telegram.bot.utils.ParsingTextUtils.MP4_Type;

class ParsingTextUtilsTest {

    public static final String TEXT = UUID.randomUUID().toString();
    public static final String TEXT_UNKNOWN_TYPE = UUID.randomUUID().toString() + ".testtestet";
    public static final String GIF_TEST_URL = "https://javatutorialhq.com/wp-content/uploads/2018/02/Java-Random-doublesorigin-bound-example-output.gif";
    public static final String MP4_TEST_URL = "https://i.imgur.com/7xTFup7.mp4";
    public static final String GIFV_TEST_URL = "https://i.imgur.com/7xTFup7.gifv";
    public static final String JPG_TEST_URL = "https://preview.redd.it/ovg8fung9osa1.jpg";
    public static final String JPEG_TEST_URL = "https://test.test/testtest.jpeg";
    public static final String PNG_TEST_URL = "https://gmlwjd9405.github.io/images/web/jsp-scriptlet-example.png";
    public static final String BMP_TEST_URL = "https://test.test/testtest.bmp";

    @ParameterizedTest(name = "{index} - {0}")
    @MethodSource("argsProviderFactory")
    void whatIsUrl_test(Pair<String, ContentType> testUrl) {
        URLInfo content = ParsingTextUtils.whatIsUrl(testUrl.getKey());

        assertNotNull(content);
        assertEquals(testUrl.getValue(), content.getContentType());
        assertFalse(isBlank(content.getUrl()));
        if (testUrl.getLeft().equals(GIFV_TEST_URL)) {
            assertNotEquals(content.getUrl(), testUrl.getKey());
            assertTrue(content.getUrl().endsWith(MP4_Type));
        } else {
            assertEquals(content.getUrl(), testUrl.getKey());
        }
    }

    public static Stream<Pair<String, ContentType>> argsProviderFactory() {
        return Stream.of(
                new ImmutablePair<>(TEXT, ContentType.Text),
                new ImmutablePair<>(TEXT_UNKNOWN_TYPE, ContentType.Text),
                new ImmutablePair<>(GIF_TEST_URL, ContentType.Gif),
                new ImmutablePair<>(MP4_TEST_URL, ContentType.Video),
                new ImmutablePair<>(GIFV_TEST_URL, ContentType.Video),
                new ImmutablePair<>(JPG_TEST_URL, ContentType.Picture),
                new ImmutablePair<>(JPEG_TEST_URL, ContentType.Picture),
                new ImmutablePair<>(PNG_TEST_URL, ContentType.Picture),
                new ImmutablePair<>(BMP_TEST_URL, ContentType.Picture)
        );
    }

}