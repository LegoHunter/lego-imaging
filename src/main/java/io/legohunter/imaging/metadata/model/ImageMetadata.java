package io.legohunter.imaging.metadata.model;

import java.util.Optional;

public record ImageMetadata(
        String uuid,
        String externalItemNumber,

        Boolean primary,
        Boolean sealed,
        Boolean builtOnce,

        ConditionEnum boxCondition,
        ConditionEnum instructionsCondition,
        ConditionEnum itemCondition,

        String caption
) {

    public boolean hasUuid() {
        return hasText(uuid);
    }

    public boolean hasExternalItemNumber() {
        return hasText(externalItemNumber);
    }

    public boolean hasRequiredIdentifiers() {
        return hasUuid() && hasExternalItemNumber();
    }

    public boolean hasPrimary() {
        return primary != null;
    }

    public boolean hasSealed() {
        return sealed != null;
    }

    public boolean hasBuiltOnce() {
        return builtOnce != null;
    }

    public boolean isPrimary() {
        return Boolean.TRUE.equals(primary);
    }

    public boolean isSealed() {
        return Boolean.TRUE.equals(sealed);
    }

    public boolean isBuiltOnce() {
        return Boolean.TRUE.equals(builtOnce);
    }

    public Optional<Boolean> primaryOptional() {
        return Optional.ofNullable(primary);
    }

    public Optional<Boolean> sealedOptional() {
        return Optional.ofNullable(sealed);
    }

    public Optional<Boolean> builtOnceOptional() {
        return Optional.ofNullable(builtOnce);
    }

    public boolean hasBoxCondition() {
        return boxCondition != null;
    }

    public boolean hasInstructionsCondition() {
        return instructionsCondition != null;
    }

    public boolean hasItemCondition() {
        return itemCondition != null;
    }

    public boolean hasAnyCondition() {
        return hasBoxCondition()
                || hasInstructionsCondition()
                || hasItemCondition();
    }

    public Optional<ConditionEnum> boxConditionOptional() {
        return Optional.ofNullable(boxCondition);
    }

    public Optional<ConditionEnum> instructionsConditionOptional() {
        return Optional.ofNullable(instructionsCondition);
    }

    public Optional<ConditionEnum> itemConditionOptional() {
        return Optional.ofNullable(itemCondition);
    }

    public boolean hasCaption() {
        return hasText(caption);
    }

    public Optional<String> captionOptional() {
        return Optional.ofNullable(trimToNull(caption));
    }

    public boolean hasInventoryUpdates() {
        return hasSealed()
                || hasBuiltOnce()
                || hasAnyCondition();
    }

    public boolean hasPhotoUpdates() {
        return hasPrimary()
                || hasCaption();
    }

    public boolean isCompleteForIngestion() {
        return hasRequiredIdentifiers();
    }

    public String requireUuid() {
        if (!hasUuid()) {
            throw new IllegalStateException("Image metadata is missing required uuid");
        }

        return uuid.trim();
    }

    public String requireExternalItemNumber() {
        if (!hasExternalItemNumber()) {
            throw new IllegalStateException("Image metadata is missing required external item number");
        }

        return externalItemNumber.trim();
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private static String trimToNull(String value) {
        if (!hasText(value)) {
            return null;
        }

        return value.trim();
    }
}
