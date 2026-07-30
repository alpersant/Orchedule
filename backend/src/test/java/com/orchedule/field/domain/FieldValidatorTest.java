package com.orchedule.field.domain;

import com.orchedule.field.application.exception.InvalidFieldException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FieldValidatorTest {

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void shouldRejectBlankName(String name) {
        assertThatThrownBy(() -> FieldValidator.validateName(name))
                .isInstanceOf(InvalidFieldException.class)
                .hasMessageContaining("blank");
    }

    @Test
    void shouldRejectNameLongerThan100Characters() {
        String longName = "a".repeat(101);
        assertThatThrownBy(() -> FieldValidator.validateName(longName))
                .isInstanceOf(InvalidFieldException.class)
                .hasMessageContaining("100 characters");
    }

    @Test
    void shouldAcceptValidName() {
        assertThatCode(() -> FieldValidator.validateName("Field 1"))
                .doesNotThrowAnyException();
    }

    @Test
    void shouldRejectWeekNumberBelowMinimum() {
        assertThatThrownBy(() -> FieldValidator.validateWeekNumber(0))
                .isInstanceOf(InvalidFieldException.class)
                .hasMessageContaining("between 1 and 60");
    }

    @Test
    void shouldRejectWeekNumberAboveMaximum() {
        assertThatThrownBy(() -> FieldValidator.validateWeekNumber(61))
                .isInstanceOf(InvalidFieldException.class)
                .hasMessageContaining("between 1 and 60");
    }

    @Test
    void shouldAcceptValidWeekNumber() {
        assertThatCode(() -> FieldValidator.validateWeekNumber(1)).doesNotThrowAnyException();
        assertThatCode(() -> FieldValidator.validateWeekNumber(60)).doesNotThrowAnyException();
    }
}
