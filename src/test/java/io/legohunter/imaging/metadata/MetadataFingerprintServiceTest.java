package io.legohunter.imaging.metadata;

import io.legohunter.imaging.metadata.model.ConditionEnum;
import io.legohunter.imaging.metadata.model.ImageMetadata;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MetadataFingerprintServiceTest {
    private final MetadataFingerprintService service = new MetadataFingerprintService();

    @Test
    void calculateHash_isStableForEquivalentNormalizedMetadata() {
        String first = service.calculateHash(metadata(" ABC123 ", " 4558-1 ", "Front view"));
        String second = service.calculateHash(metadata("abc123", "4558-1", "Front view"));

        assertThat(first)
                .hasSize(64)
                .isEqualTo(second);
    }

    @Test
    void calculateHash_changesWhenSupportedMetadataChanges() {
        String first = service.calculateHash(metadata("abc123", "4558-1", "Front view"));
        String second = service.calculateHash(metadata("abc123", "4558-1", "Rear view"));

        assertThat(first).isNotEqualTo(second);
    }

    @Test
    void calculateHash_requiresMetadata() {
        assertThatThrownBy(() -> service.calculateHash(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("metadata is required");
    }

    private ImageMetadata metadata(String uuid, String externalItemNumber, String caption) {
        return new ImageMetadata(
                uuid,
                externalItemNumber,
                true,
                false,
                true,
                ConditionEnum.M,
                ConditionEnum.E,
                ConditionEnum.G,
                caption
        );
    }
}
