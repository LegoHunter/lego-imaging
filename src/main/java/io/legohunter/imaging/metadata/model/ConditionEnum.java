package io.legohunter.imaging.metadata.model;

import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public enum ConditionEnum {

    M(1, "M"),
    E(2, "E"),
    VG(3, "VG"),
    G(4, "G"),
    P(5, "P"),
    NA(6, "NA"),
    F(7, "F"),
    MS(8, "MS"),
    CC(9, "CC"),
    BW(10, "BW"),
    SL(11, "SL");

    private final int conditionId;
    private final String conditionCode;

    ConditionEnum(
            int conditionId,
            String conditionCode
    ) {
        this.conditionId = conditionId;
        this.conditionCode = conditionCode;
    }

    public int conditionId() {
        return conditionId;
    }

    public String conditionCode() {
        return conditionCode;
    }

    public int id() {
        return conditionId;
    }

    public String code() {
        return conditionCode;
    }

    public static Optional<ConditionEnum> fromCode(String code) {
        if (code == null || code.isBlank()) {
            return Optional.empty();
        }

        String normalizedCode = code.trim().toUpperCase();

        return Arrays.stream(values())
                .filter(condition -> condition.conditionCode.equals(normalizedCode))
                .findFirst();
    }

    public static ConditionEnum requireFromCode(String code) {
        return fromCode(code)
                .orElseThrow(() -> new IllegalArgumentException("Unknown condition code: " + code));
    }

    public static Optional<ConditionEnum> fromId(Integer conditionId) {
        if (conditionId == null) {
            return Optional.empty();
        }

        return Arrays.stream(values())
                .filter(condition -> condition.conditionId == conditionId)
                .findFirst();
    }

    public static ConditionEnum requireFromId(Integer conditionId) {
        return fromId(conditionId)
                .orElseThrow(() -> new IllegalArgumentException("Unknown condition id: " + conditionId));
    }

    public boolean hasCode(String code) {
        return fromCode(code)
                .filter(this::equals)
                .isPresent();
    }

    public boolean isPhysicalCondition() {
        return switch (this) {
            case M, E, VG, G, F, P -> true;
            case NA, MS, CC, BW, SL -> false;
        };
    }

    public boolean isNotApplicable() {
        return this == NA;
    }

    public boolean isMissing() {
        return this == MS;
    }

    public boolean isCopy() {
        return this == CC || this == BW;
    }

    public boolean isSealed() {
        return this == SL;
    }

    public boolean isInventoryGrade() {
        return isPhysicalCondition() || isSealed();
    }

    public static boolean isKnownCode(String code) {
        return fromCode(code).isPresent();
    }

    public static Set<String> codes() {
        return Arrays.stream(values())
                .map(ConditionEnum::conditionCode)
                .collect(Collectors.toUnmodifiableSet());
    }
}
