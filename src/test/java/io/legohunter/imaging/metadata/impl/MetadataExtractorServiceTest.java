package io.legohunter.imaging.metadata.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

import static org.apache.commons.lang3.BooleanUtils.TRUE;
import static org.assertj.core.api.Assertions.assertThat;

class MetadataExtractorServiceTest {

    private MetadataExtractorService service;

    @BeforeEach
    void setUp() {
        service = new MetadataExtractorService();
    }

    // =========================
    // MD5 Tests
    // =========================

    @Test
    void shouldCalculateMd5Consistently() {
        byte[] data = "test-data".getBytes();

        String md5_1 = service.calculateMd5(data);
        String md5_2 = service.calculateMd5(data);

        assertThat(md5_1)
                .isNotNull()
                .isEqualTo(md5_2);
    }

    @Test
    void shouldProduceDifferentMd5ForDifferentInputs() {
        String md5_1 = service.calculateMd5("a".getBytes());
        String md5_2 = service.calculateMd5("b".getBytes());

        assertThat(md5_1)
                .isNotEqualTo(md5_2);
    }

    // =========================
    // Keyword Extraction Tests
    // =========================

    @Test
    void shouldReturnEmptyKeywordsWhenNoMetadataPresent() throws Exception {
        byte[] imageBytes = createTestImage();

        Map<String, String> result = service.extractKeywords(imageBytes);

        assertThat(result)
                .isNotNull()
                .isEmpty();
    }

    @Test
    void shouldParseKeywordsCorrectly() {
        Map<String, String> map = new HashMap<>();

        invokeParseKeywords("uuid:123; bl:3001;ic:M;sealed:true", map);

        assertThat(map)
                .hasSize(4)
                .containsEntry("uuid", "123")
                .containsEntry("bl", "3001")
                .containsEntry("ic", "M")
                .containsEntry("sealed", "true");
    }

    @Test
    void shouldHandleWhitespaceAndFormatting() {
        Map<String, String> map = new HashMap<>();

        invokeParseKeywords(" uuid : 123 ; bl : 3001 ", map);

        assertThat(map)
                .hasSize(2)
                .containsEntry("uuid", "123")
                .containsEntry("bl", "3001");
    }

    @Test
    void shouldHandleNameOnlyKeywords() {
        Map<String, String> map = new HashMap<>();

        invokeParseKeywords("no-value-key-1; no-value-key-2:; :valueonly", map);

        assertThat(map)
                .hasSize(2)
                .containsEntry("no-value-key-1", "true")
                .containsEntry("no-value-key-2", "");
    }

    // =========================
    // Caption Extraction Tests
    // =========================

    @Test
    void shouldReturnNullWhenNoCaptionPresent() throws Exception {
        byte[] imageBytes = createTestImage();

        String caption = service.extractCaption(imageBytes);

        assertThat(caption).isNull();
    }

    // =========================
    // Helpers
    // =========================

    private byte[] createTestImage() throws Exception {
        BufferedImage image = new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "jpg", baos);

        return baos.toByteArray();
    }

    /**
     * Reflection helper to test private parseKeywords method
     */
    private void invokeParseKeywords(String raw, Map<String, String> map) {
        try {
            var method = MetadataExtractorService.class.getDeclaredMethod("parseKeywordString", String.class);

            method.setAccessible(true);
            Object ret = method.invoke(service, raw);
            map.putAll((Map) ret);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}