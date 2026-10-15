package com.orchedule.season.domain;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;
class SeasonValidatorTest {
    @Test void permitsEditingHistoricalDates() {
        assertDoesNotThrow(() -> SeasonValidator.validate("2020", LocalDate.of(2020,1,1),
            LocalDate.of(2020,12,31), SeasonStatus.CLOSED));
    }
    @Test void rejectsReversedDates() {
        assertThrows(RuntimeException.class, () -> SeasonValidator.validate("Invalid",
            LocalDate.of(2026,12,31), LocalDate.of(2026,1,1), SeasonStatus.DRAFT));
    }
}
