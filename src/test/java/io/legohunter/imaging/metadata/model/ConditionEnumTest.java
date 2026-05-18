package io.legohunter.imaging.metadata.model;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ConditionEnumTest {

    @Test
    void shouldExposeConditionIdsAndCodes() {
        assertThat(ConditionEnum.M.conditionId()).isEqualTo(1);
        assertThat(ConditionEnum.M.conditionCode()).isEqualTo("M");
        assertThat(ConditionEnum.M.id()).isEqualTo(1);
        assertThat(ConditionEnum.M.code()).isEqualTo("M");

        assertThat(ConditionEnum.SL.conditionId()).isEqualTo(11);
        assertThat(ConditionEnum.SL.conditionCode()).isEqualTo("SL");
        assertThat(ConditionEnum.SL.id()).isEqualTo(11);
        assertThat(ConditionEnum.SL.code()).isEqualTo("SL");
    }

    @Test
    void fromCode_shouldFindConditionByExactCode() {
        assertThat(ConditionEnum.fromCode("VG"))
                .contains(ConditionEnum.VG);
    }

    @Test
    void fromCode_shouldNormalizeCaseAndWhitespace() {
        assertThat(ConditionEnum.fromCode(" bw "))
                .contains(ConditionEnum.BW);
    }

    @Test
    void fromCode_shouldReturnEmptyForNullBlankAndUnknownCode() {
        assertThat(ConditionEnum.fromCode(null)).isEmpty();
        assertThat(ConditionEnum.fromCode("")).isEmpty();
        assertThat(ConditionEnum.fromCode("   ")).isEmpty();
        assertThat(ConditionEnum.fromCode("UNKNOWN")).isEmpty();
    }

    @Test
    void requireFromCode_shouldReturnConditionWhenKnown() {
        assertThat(ConditionEnum.requireFromCode("cc"))
                .isEqualTo(ConditionEnum.CC);
    }

    @Test
    void requireFromCode_shouldThrowWhenUnknown() {
        assertThatThrownBy(() -> ConditionEnum.requireFromCode("BAD"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unknown condition code: BAD");
    }

    @Test
    void fromId_shouldFindConditionById() {
        assertThat(ConditionEnum.fromId(1))
                .contains(ConditionEnum.M);

        assertThat(ConditionEnum.fromId(11))
                .contains(ConditionEnum.SL);
    }

    @Test
    void fromId_shouldReturnEmptyForNullAndUnknownId() {
        assertThat(ConditionEnum.fromId(null)).isEmpty();
        assertThat(ConditionEnum.fromId(999)).isEmpty();
    }

    @Test
    void requireFromId_shouldReturnConditionWhenKnown() {
        assertThat(ConditionEnum.requireFromId(7))
                .isEqualTo(ConditionEnum.F);
    }

    @Test
    void requireFromId_shouldThrowWhenUnknown() {
        assertThatThrownBy(() -> ConditionEnum.requireFromId(999))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unknown condition id: 999");
    }

    @Test
    void hasCode_shouldCompareUsingNormalizedCode() {
        assertThat(ConditionEnum.NA.hasCode(" na ")).isTrue();
        assertThat(ConditionEnum.NA.hasCode("M")).isFalse();
        assertThat(ConditionEnum.NA.hasCode(null)).isFalse();
    }

    @Test
    void isPhysicalCondition_shouldOnlyBeTrueForPhysicalGrades() {
        assertThat(Set.of(
                ConditionEnum.M,
                ConditionEnum.E,
                ConditionEnum.VG,
                ConditionEnum.G,
                ConditionEnum.F,
                ConditionEnum.P
        )).allMatch(ConditionEnum::isPhysicalCondition);

        assertThat(Set.of(
                ConditionEnum.NA,
                ConditionEnum.MS,
                ConditionEnum.CC,
                ConditionEnum.BW,
                ConditionEnum.SL
        )).noneMatch(ConditionEnum::isPhysicalCondition);
    }

    @Test
    void shouldClassifySpecialConditions() {
        assertThat(ConditionEnum.NA.isNotApplicable()).isTrue();
        assertThat(ConditionEnum.M.isNotApplicable()).isFalse();

        assertThat(ConditionEnum.MS.isMissing()).isTrue();
        assertThat(ConditionEnum.M.isMissing()).isFalse();

        assertThat(ConditionEnum.CC.isCopy()).isTrue();
        assertThat(ConditionEnum.BW.isCopy()).isTrue();
        assertThat(ConditionEnum.M.isCopy()).isFalse();

        assertThat(ConditionEnum.SL.isSealed()).isTrue();
        assertThat(ConditionEnum.M.isSealed()).isFalse();
    }

    @Test
    void isInventoryGrade_shouldIncludePhysicalConditionsAndSealedOnly() {
        assertThat(Set.of(
                ConditionEnum.M,
                ConditionEnum.E,
                ConditionEnum.VG,
                ConditionEnum.G,
                ConditionEnum.F,
                ConditionEnum.P,
                ConditionEnum.SL
        )).allMatch(ConditionEnum::isInventoryGrade);

        assertThat(Set.of(
                ConditionEnum.NA,
                ConditionEnum.MS,
                ConditionEnum.CC,
                ConditionEnum.BW
        )).noneMatch(ConditionEnum::isInventoryGrade);
    }

    @Test
    void isKnownCode_shouldMatchFromCodeBehavior() {
        assertThat(ConditionEnum.isKnownCode("m")).isTrue();
        assertThat(ConditionEnum.isKnownCode(" no ")).isFalse();
        assertThat(ConditionEnum.isKnownCode(null)).isFalse();
    }

    @Test
    void codes_shouldReturnEveryConditionCodeAsUnmodifiableSet() {
        assertThat(ConditionEnum.codes())
                .containsExactlyInAnyOrderElementsOf(
                        Arrays.stream(ConditionEnum.values())
                                .map(ConditionEnum::conditionCode)
                                .toList()
                );

        assertThatThrownBy(() -> ConditionEnum.codes().add("BAD"))
                .isInstanceOf(UnsupportedOperationException.class);
    }
}
