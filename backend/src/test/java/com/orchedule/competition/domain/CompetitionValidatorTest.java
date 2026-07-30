package com.orchedule.competition.domain;

import com.orchedule.competition.application.exception.InvalidCompetitionException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CompetitionValidatorTest {

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void shouldRejectBlankName(String name) {
        assertThatThrownBy(() -> CompetitionValidator.validateName(name))
                .isInstanceOf(InvalidCompetitionException.class)
                .hasMessageContaining("blank");
    }

    @Test
    void shouldRejectNameLongerThan150Characters() {
        String longName = "a".repeat(151);
        assertThatThrownBy(() -> CompetitionValidator.validateName(longName))
                .isInstanceOf(InvalidCompetitionException.class)
                .hasMessageContaining("150 characters");
    }

    @Test
    void shouldAcceptValidName() {
        assertThatCode(() -> CompetitionValidator.validateName("Liga Local")).doesNotThrowAnyException();
    }

    @Test
    void shouldRejectDescriptionLongerThan1000Characters() {
        String longDescription = "a".repeat(1001);
        assertThatThrownBy(() -> CompetitionValidator.validateDescription(longDescription))
                .isInstanceOf(InvalidCompetitionException.class)
                .hasMessageContaining("1000 characters");
    }

    @Test
    void shouldAcceptNullDescription() {
        assertThatCode(() -> CompetitionValidator.validateDescription(null)).doesNotThrowAnyException();
    }

    @Test
    void shouldRejectEmptyDefaultDays() {
        assertThatThrownBy(() -> CompetitionValidator.validateDefaultDays(Set.of()))
                .isInstanceOf(InvalidCompetitionException.class)
                .hasMessageContaining("day");
    }

    @Test
    void shouldRejectNullDefaultDays() {
        assertThatThrownBy(() -> CompetitionValidator.validateDefaultDays(null))
                .isInstanceOf(InvalidCompetitionException.class);
    }

    @Test
    void shouldAcceptValidDefaultDays() {
        assertThatCode(() -> CompetitionValidator.validateDefaultDays(Set.of(CompetitionDay.MONDAY)))
                .doesNotThrowAnyException();
    }

    @Test
    void shouldRejectEmptyDefaultHours() {
        assertThatThrownBy(() -> CompetitionValidator.validateDefaultHours(Set.of()))
                .isInstanceOf(InvalidCompetitionException.class)
                .hasMessageContaining("hour");
    }

    @Test
    void shouldAcceptValidDefaultHours() {
        assertThatCode(() -> CompetitionValidator.validateDefaultHours(Set.of(CompetitionHour.H18)))
                .doesNotThrowAnyException();
    }

    @Test
    void shouldRejectFieldCountBelowMinimum() {
        assertThatThrownBy(() -> CompetitionValidator.validateDefaultFieldCount(0))
                .isInstanceOf(InvalidCompetitionException.class)
                .hasMessageContaining("between 1 and 20");
    }

    @Test
    void shouldRejectFieldCountAboveMaximum() {
        assertThatThrownBy(() -> CompetitionValidator.validateDefaultFieldCount(21))
                .isInstanceOf(InvalidCompetitionException.class)
                .hasMessageContaining("between 1 and 20");
    }

    @Test
    void shouldAcceptValidFieldCount() {
        assertThatCode(() -> CompetitionValidator.validateDefaultFieldCount(3)).doesNotThrowAnyException();
    }
}
