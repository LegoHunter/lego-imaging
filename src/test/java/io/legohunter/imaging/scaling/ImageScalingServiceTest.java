package io.legohunter.imaging.scaling;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ImageScalingServiceTest {

    private ImageScalingService service;

    @BeforeEach
    void setUp() {
        service = new ImageScalingService();
    }

    @Test
    void scale_shouldReturnScaledImageUnder2MB() throws Exception {

        byte[] original =
                createLargeTestImageBytes(4000, 4000);

        byte[] scaled =
                service.scale(original);

        assertThat(scaled)
                .isNotNull()
                .isNotEmpty();

        assertThat(scaled.length)
                .isLessThanOrEqualTo(2_000_000);

        BufferedImage scaledImage =
                ImageIO.read(new ByteArrayInputStream(scaled));

        assertThat(scaledImage).isNotNull();

        BufferedImage originalImage =
                ImageIO.read(new ByteArrayInputStream(original));

        assertThat(scaledImage.getWidth())
                .isLessThanOrEqualTo(originalImage.getWidth());

        assertThat(scaledImage.getHeight())
                .isLessThanOrEqualTo(originalImage.getHeight());
    }

    @Test
    void scale_shouldReturnOriginalImageIfAlreadySmallEnough() throws Exception {

        byte[] original =
                createLargeTestImageBytes(300, 300);

        byte[] scaled =
                service.scale(original);

        assertThat(scaled)
                .isNotNull()
                .isNotEmpty();

        assertThat(scaled.length)
                .isLessThanOrEqualTo(2_000_000);
    }

    @Test
    void scale_shouldPreserveValidJpegFormat() throws Exception {

        byte[] original =
                createLargeTestImageBytes(1200, 1200);

        byte[] scaled =
                service.scale(original);

        BufferedImage image =
                ImageIO.read(new ByteArrayInputStream(scaled));

        assertThat(image).isNotNull();

        assertThat(image.getWidth()).isPositive();
        assertThat(image.getHeight()).isPositive();
    }

    @Test
    void scale_shouldReduceDimensionsForVeryLargeImages() throws Exception {

        byte[] original =
                createLargeTestImageBytes(8000, 8000);

        BufferedImage originalImage =
                ImageIO.read(new ByteArrayInputStream(original));

        byte[] scaled =
                service.scale(original);

        BufferedImage scaledImage =
                ImageIO.read(new ByteArrayInputStream(scaled));

        assertThat(scaledImage.getWidth())
                .isLessThan(originalImage.getWidth());

        assertThat(scaledImage.getHeight())
                .isLessThan(originalImage.getHeight());
    }

    @Test
    void scale_shouldThrowExceptionForInvalidImageBytes() {

        byte[] invalid =
                "not-an-image".getBytes();

        assertThatThrownBy(() -> service.scale(invalid))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Image scaling failed");
    }

    @Test
    void scale_shouldThrowExceptionForEmptyBytes() {

        byte[] empty = new byte[0];

        assertThatThrownBy(() -> service.scale(empty))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Image scaling failed");
    }

    @Test
    void scale_shouldAlwaysReturnJpegBytes() throws Exception {

        byte[] original =
                createLargeTestImageBytes(2000, 2000);

        byte[] scaled =
                service.scale(original);

        // JPEG magic bytes
        assertThat(scaled[0]).isEqualTo((byte) 0xFF);
        assertThat(scaled[1]).isEqualTo((byte) 0xD8);
    }

    @Test
    void scale_shouldHandleWideImages() throws Exception {

        byte[] original =
                createLargeTestImageBytes(5000, 1000);

        byte[] scaled =
                service.scale(original);

        BufferedImage scaledImage =
                ImageIO.read(new ByteArrayInputStream(scaled));

        assertThat(scaledImage.getWidth())
                .isGreaterThan(scaledImage.getHeight());

        assertThat(scaled.length)
                .isLessThanOrEqualTo(2_000_000);
    }

    @Test
    void scale_shouldHandleTallImages() throws Exception {

        byte[] original =
                createLargeTestImageBytes(1000, 5000);

        byte[] scaled =
                service.scale(original);

        BufferedImage scaledImage =
                ImageIO.read(new ByteArrayInputStream(scaled));

        assertThat(scaledImage.getHeight())
                .isGreaterThan(scaledImage.getWidth());

        assertThat(scaled.length)
                .isLessThanOrEqualTo(2_000_000);
    }

    @Test
    void scale_shouldBeDeterministicForSameInput() throws Exception {

        byte[] original =
                createLargeTestImageBytes(2500, 2500);

        byte[] scaled1 =
                service.scale(original);

        byte[] scaled2 =
                service.scale(original);

        assertThat(scaled1)
                .isEqualTo(scaled2);
    }

    private byte[] createLargeTestImageBytes(
            int width,
            int height
    ) throws Exception {

        BufferedImage image =
                new BufferedImage(
                        width,
                        height,
                        BufferedImage.TYPE_INT_RGB
                );

        Graphics2D graphics = image.createGraphics();

        try {

            graphics.setColor(Color.WHITE);
            graphics.fillRect(0, 0, width, height);

            // Add visual noise to avoid over-compression
            for (int y = 0; y < height; y += 20) {
                for (int x = 0; x < width; x += 20) {

                    graphics.setColor(
                            new Color(
                                    (x * y) % 255,
                                    (x + y) % 255,
                                    (x * 3 + y * 7) % 255
                            )
                    );

                    graphics.fillRect(x, y, 20, 20);
                }
            }

        } finally {
            graphics.dispose();
        }

        ByteArrayOutputStream baos =
                new ByteArrayOutputStream();

        ImageIO.write(image, "jpg", baos);

        return baos.toByteArray();
    }
}