package io.legohunter.imaging.metadata.impl;

import io.legohunter.imaging.metadata.model.ConditionEnum;
import io.legohunter.imaging.metadata.model.ImageMetadata;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MetadataExtractorServiceTest {

    private MetadataExtractorService service;

    @BeforeEach
    void setUp() {
        service = new MetadataExtractorService();
    }

    @Test
    void extractMetadata_shouldReturnEmptyImageMetadataWhenNoMetadataPresent() throws Exception {
        ImageMetadata metadata = service.extractMetadata(createTestImage());

        assertThat(metadata.uuid()).isNull();
        assertThat(metadata.externalItemNumber()).isNull();
        assertThat(metadata.primary()).isNull();
        assertThat(metadata.sealed()).isNull();
        assertThat(metadata.builtOnce()).isNull();
        assertThat(metadata.boxCondition()).isNull();
        assertThat(metadata.instructionsCondition()).isNull();
        assertThat(metadata.itemCondition()).isNull();
        assertThat(metadata.caption()).isNull();
        assertThat(metadata.isCompleteForIngestion()).isFalse();
        assertThat(metadata.hasInventoryUpdates()).isFalse();
        assertThat(metadata.hasPhotoUpdates()).isFalse();
    }

    @Test
    void extractMetadata_shouldExtractRealLightroomKeywordsFromJpeg() throws Exception {
        ImageMetadata metadata = service.extractMetadata(readTestResource(
                "actual-lego-photos-with-keywords/DSC_0504.JPG"
        ));

        assertThat(metadata.uuid()).isEqualTo("fdaa0638814727a42f005656f38b92c6");
        assertThat(metadata.externalItemNumber()).isEqualTo("6658-1");
        assertThat(metadata.primary()).isNull();
        assertThat(metadata.sealed()).isNull();
        assertThat(metadata.builtOnce()).isTrue();
        assertThat(metadata.boxCondition()).isEqualTo(ConditionEnum.E);
        assertThat(metadata.instructionsCondition()).isEqualTo(ConditionEnum.E);
        assertThat(metadata.itemCondition()).isNull();
        assertThat(metadata.caption()).isNull();
        assertThat(metadata.isCompleteForIngestion()).isTrue();
        assertThat(metadata.hasInventoryUpdates()).isTrue();
        assertThat(metadata.hasPhotoUpdates()).isFalse();
    }

    @Test
    void extractMetadata_shouldExtractCaptionWhenPresent() throws Exception {
        ImageMetadata metadata = service.extractMetadata(readTestResource(
                "lego-photo-with-metadata/DSC_3368.JPG"
        ));

        assertThat(metadata.captionOptional())
                .isPresent()
                .get()
                .asString()
                .isNotBlank();
    }

    @Test
    void extractMetadata_shouldWrapMetadataReaderFailures() {
        assertThatThrownBy(() -> service.extractMetadata("not-an-image".getBytes()))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Image metadata extraction failed");
    }

    @Test
    void calculateMd5_shouldCalculateKnownMd5Value() {
        String md5 = service.calculateMd5("test-data".getBytes());

        assertThat(md5).isEqualTo("24346e1b50066607059af36e3b684b24");
    }

    @Test
    void calculateMd5_shouldCalculateConsistentlyAndDifferentiateInputs() {
        String first = service.calculateMd5("a".getBytes());
        String second = service.calculateMd5("a".getBytes());
        String different = service.calculateMd5("b".getBytes());

        assertThat(first)
                .isEqualTo(second)
                .isNotEqualTo(different);
    }

    @Test
    void parseKeywordString_shouldReturnEmptyMapForNullBlankAndEmptyTokens() {
        assertThat(service.parseKeywordString(null)).isEmpty();
        assertThat(service.parseKeywordString("")).isEmpty();
        assertThat(service.parseKeywordString("   ")).isEmpty();
        assertThat(service.parseKeywordString(" ; ; ")).isEmpty();
    }

    @Test
    void parseKeywordString_shouldParseKeyValuePairsAndNormalizeKeys() {
        Map<String, String> result = service.parseKeywordString(
                " UUID : uuid-1 ; BL : 3001 ; IC:VG ; bc : m "
        );

        assertThat(result)
                .containsEntry("uuid", "uuid-1")
                .containsEntry("bl", "3001")
                .containsEntry("ic", "VG")
                .containsEntry("bc", "m");
    }

    @Test
    void parseKeywordString_shouldTreatPresenceOnlyKeywordsAsTrue() {
        Map<String, String> result = service.parseKeywordString(
                "primary; sealed ; bo"
        );

        assertThat(result)
                .containsEntry("primary", "true")
                .containsEntry("sealed", "true")
                .containsEntry("bo", "true");
    }

    @Test
    void parseKeywordString_shouldSkipMalformedValueOnlyTokenButKeepEmptyValueToken() {
        Map<String, String> result = service.parseKeywordString(
                ":valueonly; primary:; valid:value"
        );

        assertThat(result)
                .doesNotContainKey("")
                .containsEntry("primary", "")
                .containsEntry("valid", "value");
    }

    @Test
    void toImageMetadata_shouldMapKnownKeywordValuesToTypedMetadata() {
        ImageMetadata metadata = service.toImageMetadata(
                Map.of(
                        "uuid", " uuid-1 ",
                        "bl", " 3001 ",
                        "primary", "yes",
                        "sealed", "0",
                        "bo", "bo",
                        "bc", "m",
                        "ic", "VG",
                        "item", "SL"
                ),
                " Caption "
        );

        assertThat(metadata.uuid()).isEqualTo("uuid-1");
        assertThat(metadata.externalItemNumber()).isEqualTo("3001");
        assertThat(metadata.primary()).isTrue();
        assertThat(metadata.sealed()).isFalse();
        assertThat(metadata.builtOnce()).isTrue();
        assertThat(metadata.boxCondition()).isEqualTo(ConditionEnum.M);
        assertThat(metadata.instructionsCondition()).isEqualTo(ConditionEnum.VG);
        assertThat(metadata.itemCondition()).isEqualTo(ConditionEnum.SL);
        assertThat(metadata.caption()).isEqualTo("Caption");
    }

    @Test
    void toImageMetadata_shouldDefineFlickrPhase3KeywordTaxonomy() {
        ImageMetadata metadata = service.toImageMetadata(
                Map.of(
                        "uuid", "uuid-1",
                        "bl", "3001",
                        "primary", "primary",
                        "sealed", "false",
                        "bo", "true",
                        "bc", "M",
                        "ic", "E",
                        "item", "VG"
                ),
                "Displayed caption"
        );

        assertThat(metadata.uuid()).isEqualTo("uuid-1");
        assertThat(metadata.externalItemNumber()).isEqualTo("3001");
        assertThat(metadata.primary()).isTrue();
        assertThat(metadata.sealed()).isFalse();
        assertThat(metadata.builtOnce()).isTrue();
        assertThat(metadata.boxCondition()).isEqualTo(ConditionEnum.M);
        assertThat(metadata.instructionsCondition()).isEqualTo(ConditionEnum.E);
        assertThat(metadata.itemCondition()).isEqualTo(ConditionEnum.VG);
        assertThat(metadata.caption()).isEqualTo("Displayed caption");
        assertThat(metadata.hasInventoryUpdates()).isTrue();
        assertThat(metadata.hasPhotoUpdates()).isTrue();
    }

    @Test
    void toImageMetadata_shouldTreatCpAsLegacyCaptionFallback() {
        ImageMetadata metadata = service.toImageMetadata(
                Map.of(
                        "uuid", "uuid-1",
                        "bl", "3001",
                        "cp", " Legacy caption "
                ),
                null
        );

        assertThat(metadata.caption()).isEqualTo("Legacy caption");
    }

    @Test
    void toImageMetadata_shouldPreferExplicitCaptionOverLegacyCpKeyword() {
        ImageMetadata metadata = service.toImageMetadata(
                Map.of(
                        "uuid", "uuid-1",
                        "bl", "3001",
                        "cp", "Legacy caption"
                ),
                "Current caption"
        );

        assertThat(metadata.caption()).isEqualTo("Current caption");
    }

    @Test
    void toImageMetadata_shouldIgnoreLegacyRemarkKeywords() {
        ImageMetadata metadata = service.toImageMetadata(
                Map.of(
                        "uuid", "uuid-1",
                        "bl", "3001",
                        "rmk1", "Remark one",
                        "rmk2", "Remark two"
                ),
                null
        );

        assertThat(metadata.caption()).isNull();
        assertThat(metadata.hasPhotoUpdates()).isFalse();
    }

    @Test
    void toImageMetadata_shouldSupportAliasKeys() {
        ImageMetadata metadata = service.toImageMetadata(
                Map.of(
                        "built_once", "true",
                        "box-condition", "E",
                        "instructionscondition", "G",
                        "item_condition", "P"
                ),
                null
        );

        assertThat(metadata.builtOnce()).isTrue();
        assertThat(metadata.boxCondition()).isEqualTo(ConditionEnum.E);
        assertThat(metadata.instructionsCondition()).isEqualTo(ConditionEnum.G);
        assertThat(metadata.itemCondition()).isEqualTo(ConditionEnum.P);
    }

    @Test
    void parseBooleanKeyword_shouldReturnNullWhenKeywordIsAbsentOrBlank() {
        assertThat(service.parseBooleanKeyword(Map.of(), "primary")).isNull();
        assertThat(service.parseBooleanKeyword(Map.of("primary", ""), "primary")).isNull();
        assertThat(service.parseBooleanKeyword(Map.of("primary", " "), "primary")).isNull();
    }

    @Test
    void parseBooleanKeyword_shouldParseTrueValues() {
        assertThat(service.parseBooleanKeyword(Map.of("flag", "true"), "flag")).isTrue();
        assertThat(service.parseBooleanKeyword(Map.of("flag", "YES"), "flag")).isTrue();
        assertThat(service.parseBooleanKeyword(Map.of("flag", "y"), "flag")).isTrue();
        assertThat(service.parseBooleanKeyword(Map.of("flag", "1"), "flag")).isTrue();
        assertThat(service.parseBooleanKeyword(Map.of("flag", "on"), "flag")).isTrue();
        assertThat(service.parseBooleanKeyword(Map.of("primary", "primary"), "primary")).isTrue();
    }

    @Test
    void parseBooleanKeyword_shouldParseFalseValues() {
        assertThat(service.parseBooleanKeyword(Map.of("flag", "false"), "flag")).isFalse();
        assertThat(service.parseBooleanKeyword(Map.of("flag", "NO"), "flag")).isFalse();
        assertThat(service.parseBooleanKeyword(Map.of("flag", "n"), "flag")).isFalse();
        assertThat(service.parseBooleanKeyword(Map.of("flag", "0"), "flag")).isFalse();
        assertThat(service.parseBooleanKeyword(Map.of("flag", "off"), "flag")).isFalse();
    }

    @Test
    void parseBooleanKeyword_shouldThrowForInvalidBooleanValue() {
        assertThatThrownBy(() -> service.parseBooleanKeyword(Map.of("primary", "maybe"), "primary"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid boolean metadata value [maybe] for keyword [primary]");
    }

    @Test
    void parseConditionKeyword_shouldReturnNullWhenKeywordIsAbsentOrBlank() {
        assertThat(service.parseConditionKeyword(Map.of(), "bc")).isNull();
        assertThat(service.parseConditionKeyword(Map.of("bc", ""), "bc")).isNull();
        assertThat(service.parseConditionKeyword(Map.of("bc", " "), "bc")).isNull();
    }

    @Test
    void parseConditionKeyword_shouldParseConditionCode() {
        assertThat(service.parseConditionKeyword(Map.of("bc", "vg"), "bc"))
                .isEqualTo(ConditionEnum.VG);
    }

    @Test
    void parseConditionKeyword_shouldThrowForUnknownConditionCode() {
        assertThatThrownBy(() -> service.parseConditionKeyword(Map.of("bc", "BAD"), "bc"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unknown condition code: BAD");
    }

    @Test
    void helperMethods_shouldHandleNullInputsDefensively() {
        assertThat(service.parseBooleanKeyword(null, "primary")).isNull();
        assertThat(service.parseBooleanKeyword(Map.of("primary", "true"), (String[]) null)).isNull();
        assertThat(service.parseConditionKeyword(null, "bc")).isNull();
        assertThat(service.parseConditionKeyword(Map.of("bc", "M"), (String[]) null)).isNull();
    }

    private byte[] createTestImage() throws Exception {
        BufferedImage image = new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "jpg", baos);

        return baos.toByteArray();
    }

    private byte[] readTestResource(String relativePath) throws Exception {
        return Files.readAllBytes(Path.of("src", "test", "resources", relativePath));
    }
}
