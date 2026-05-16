package io.legohunter.imaging.metadata.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ImageMetadataTest {

    @Test
    void shouldReportRequiredIdentifierPresence() {
        ImageMetadata metadata = fullMetadata();

        assertThat(metadata.hasUuid()).isTrue();
        assertThat(metadata.hasExternalItemNumber()).isTrue();
        assertThat(metadata.hasRequiredIdentifiers()).isTrue();
        assertThat(metadata.isCompleteForIngestion()).isTrue();
    }

    @Test
    void shouldReportMissingRequiredIdentifiers() {
        ImageMetadata missingUuid = new ImageMetadata(
                " ",
                "3001",
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );

        ImageMetadata missingExternalItemNumber = new ImageMetadata(
                "uuid-1",
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );

        assertThat(missingUuid.hasUuid()).isFalse();
        assertThat(missingUuid.hasRequiredIdentifiers()).isFalse();
        assertThat(missingUuid.isCompleteForIngestion()).isFalse();

        assertThat(missingExternalItemNumber.hasExternalItemNumber()).isFalse();
        assertThat(missingExternalItemNumber.hasRequiredIdentifiers()).isFalse();
        assertThat(missingExternalItemNumber.isCompleteForIngestion()).isFalse();
    }

    @Test
    void shouldRequireIdentifiersAndReturnTrimmedValues() {
        ImageMetadata metadata = new ImageMetadata(
                " uuid-1 ",
                " 3001 ",
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );

        assertThat(metadata.requireUuid()).isEqualTo("uuid-1");
        assertThat(metadata.requireExternalItemNumber()).isEqualTo("3001");
    }

    @Test
    void shouldThrowWhenRequiredIdentifiersAreMissing() {
        ImageMetadata metadata = new ImageMetadata(
                "",
                " ",
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );

        assertThatThrownBy(metadata::requireUuid)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("missing required uuid");

        assertThatThrownBy(metadata::requireExternalItemNumber)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("missing required external item number");
    }

    @Test
    void shouldExposeNullableBooleanPresenceAndSafeValues() {
        ImageMetadata trueValues = new ImageMetadata(
                "uuid-1",
                "3001",
                true,
                true,
                true,
                null,
                null,
                null,
                null
        );

        ImageMetadata falseValues = new ImageMetadata(
                "uuid-1",
                "3001",
                false,
                false,
                false,
                null,
                null,
                null,
                null
        );

        ImageMetadata absentValues = new ImageMetadata(
                "uuid-1",
                "3001",
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );

        assertThat(trueValues.hasPrimary()).isTrue();
        assertThat(trueValues.hasSealed()).isTrue();
        assertThat(trueValues.hasBuiltOnce()).isTrue();
        assertThat(trueValues.isPrimary()).isTrue();
        assertThat(trueValues.isSealed()).isTrue();
        assertThat(trueValues.isBuiltOnce()).isTrue();
        assertThat(trueValues.primaryOptional()).contains(true);
        assertThat(trueValues.sealedOptional()).contains(true);
        assertThat(trueValues.builtOnceOptional()).contains(true);

        assertThat(falseValues.hasPrimary()).isTrue();
        assertThat(falseValues.hasSealed()).isTrue();
        assertThat(falseValues.hasBuiltOnce()).isTrue();
        assertThat(falseValues.isPrimary()).isFalse();
        assertThat(falseValues.isSealed()).isFalse();
        assertThat(falseValues.isBuiltOnce()).isFalse();
        assertThat(falseValues.primaryOptional()).contains(false);
        assertThat(falseValues.sealedOptional()).contains(false);
        assertThat(falseValues.builtOnceOptional()).contains(false);

        assertThat(absentValues.hasPrimary()).isFalse();
        assertThat(absentValues.hasSealed()).isFalse();
        assertThat(absentValues.hasBuiltOnce()).isFalse();
        assertThat(absentValues.isPrimary()).isFalse();
        assertThat(absentValues.isSealed()).isFalse();
        assertThat(absentValues.isBuiltOnce()).isFalse();
        assertThat(absentValues.primaryOptional()).isEmpty();
        assertThat(absentValues.sealedOptional()).isEmpty();
        assertThat(absentValues.builtOnceOptional()).isEmpty();
    }

    @Test
    void shouldExposeConditionPresenceAndOptionals() {
        ImageMetadata metadata = fullMetadata();

        assertThat(metadata.hasBoxCondition()).isTrue();
        assertThat(metadata.hasInstructionsCondition()).isTrue();
        assertThat(metadata.hasItemCondition()).isTrue();
        assertThat(metadata.hasAnyCondition()).isTrue();

        assertThat(metadata.boxConditionOptional()).contains(ConditionEnum.M);
        assertThat(metadata.instructionsConditionOptional()).contains(ConditionEnum.VG);
        assertThat(metadata.itemConditionOptional()).contains(ConditionEnum.SL);
    }

    @Test
    void shouldReportNoConditionsWhenAllConditionFieldsAreAbsent() {
        ImageMetadata metadata = new ImageMetadata(
                "uuid-1",
                "3001",
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );

        assertThat(metadata.hasBoxCondition()).isFalse();
        assertThat(metadata.hasInstructionsCondition()).isFalse();
        assertThat(metadata.hasItemCondition()).isFalse();
        assertThat(metadata.hasAnyCondition()).isFalse();
        assertThat(metadata.boxConditionOptional()).isEmpty();
        assertThat(metadata.instructionsConditionOptional()).isEmpty();
        assertThat(metadata.itemConditionOptional()).isEmpty();
    }

    @Test
    void shouldReportAnyConditionWhenAtLeastOneConditionIsPresent() {
        ImageMetadata boxOnly = new ImageMetadata(
                "uuid-1",
                "3001",
                null,
                null,
                null,
                ConditionEnum.G,
                null,
                null,
                null
        );

        ImageMetadata instructionsOnly = new ImageMetadata(
                "uuid-1",
                "3001",
                null,
                null,
                null,
                null,
                ConditionEnum.G,
                null,
                null
        );

        ImageMetadata itemOnly = new ImageMetadata(
                "uuid-1",
                "3001",
                null,
                null,
                null,
                null,
                null,
                ConditionEnum.G,
                null
        );

        assertThat(boxOnly.hasAnyCondition()).isTrue();
        assertThat(instructionsOnly.hasAnyCondition()).isTrue();
        assertThat(itemOnly.hasAnyCondition()).isTrue();
    }

    @Test
    void shouldExposeCaptionPresenceAndTrimmedOptional() {
        ImageMetadata withCaption = new ImageMetadata(
                "uuid-1",
                "3001",
                null,
                null,
                null,
                null,
                null,
                null,
                " Front view "
        );

        ImageMetadata blankCaption = new ImageMetadata(
                "uuid-1",
                "3001",
                null,
                null,
                null,
                null,
                null,
                null,
                " "
        );

        ImageMetadata nullCaption = new ImageMetadata(
                "uuid-1",
                "3001",
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );

        assertThat(withCaption.hasCaption()).isTrue();
        assertThat(withCaption.captionOptional()).contains("Front view");

        assertThat(blankCaption.hasCaption()).isFalse();
        assertThat(blankCaption.captionOptional()).isEmpty();

        assertThat(nullCaption.hasCaption()).isFalse();
        assertThat(nullCaption.captionOptional()).isEmpty();
    }

    @Test
    void shouldReportInventoryUpdatesForInventoryMetadataOnly() {
        ImageMetadata noInventoryUpdates = new ImageMetadata(
                "uuid-1",
                "3001",
                true,
                null,
                null,
                null,
                null,
                null,
                "Caption"
        );

        ImageMetadata sealed = metadataWithInventoryFields(true, null, null, null, null);
        ImageMetadata builtOnce = metadataWithInventoryFields(null, false, null, null, null);
        ImageMetadata boxCondition = metadataWithInventoryFields(null, null, ConditionEnum.M, null, null);
        ImageMetadata instructionsCondition = metadataWithInventoryFields(null, null, null, ConditionEnum.E, null);
        ImageMetadata itemCondition = metadataWithInventoryFields(null, null, null, null, ConditionEnum.G);

        assertThat(noInventoryUpdates.hasInventoryUpdates()).isFalse();
        assertThat(sealed.hasInventoryUpdates()).isTrue();
        assertThat(builtOnce.hasInventoryUpdates()).isTrue();
        assertThat(boxCondition.hasInventoryUpdates()).isTrue();
        assertThat(instructionsCondition.hasInventoryUpdates()).isTrue();
        assertThat(itemCondition.hasInventoryUpdates()).isTrue();
    }

    @Test
    void shouldReportPhotoUpdatesForPhotoMetadataOnly() {
        ImageMetadata noPhotoUpdates = new ImageMetadata(
                "uuid-1",
                "3001",
                null,
                true,
                true,
                ConditionEnum.M,
                ConditionEnum.E,
                ConditionEnum.G,
                null
        );

        ImageMetadata primaryFalse = new ImageMetadata(
                "uuid-1",
                "3001",
                false,
                null,
                null,
                null,
                null,
                null,
                null
        );

        ImageMetadata caption = new ImageMetadata(
                "uuid-1",
                "3001",
                null,
                null,
                null,
                null,
                null,
                null,
                "Caption"
        );

        assertThat(noPhotoUpdates.hasPhotoUpdates()).isFalse();
        assertThat(primaryFalse.hasPhotoUpdates()).isTrue();
        assertThat(caption.hasPhotoUpdates()).isTrue();
    }

    private static ImageMetadata fullMetadata() {
        return new ImageMetadata(
                "uuid-1",
                "3001",
                true,
                false,
                true,
                ConditionEnum.M,
                ConditionEnum.VG,
                ConditionEnum.SL,
                "Front view"
        );
    }

    private static ImageMetadata metadataWithInventoryFields(
            Boolean sealed,
            Boolean builtOnce,
            ConditionEnum boxCondition,
            ConditionEnum instructionsCondition,
            ConditionEnum itemCondition
    ) {
        return new ImageMetadata(
                "uuid-1",
                "3001",
                null,
                sealed,
                builtOnce,
                boxCondition,
                instructionsCondition,
                itemCondition,
                null
        );
    }
}
