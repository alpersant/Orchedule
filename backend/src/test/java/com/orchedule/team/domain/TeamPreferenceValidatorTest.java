package com.orchedule.team.domain;

import com.orchedule.team.application.exception.InvalidTeamPreferenceException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TeamPreferenceValidatorTest {

    @Test
    void shouldAcceptExcludedHourWithAtLeastThreePrimaryDays() {
        assertDoesNotThrow(() -> TeamPreferenceValidator.validate(
                RestrictionType.EXCLUDED_HOUR,
                null,
                MatchHour.H18_00,
                List.of(
                        new DayPreference(MatchDay.MONDAY, PreferencePriority.PRIMARY),
                        new DayPreference(MatchDay.TUESDAY, PreferencePriority.PRIMARY),
                        new DayPreference(MatchDay.WEDNESDAY, PreferencePriority.PRIMARY),
                        new DayPreference(MatchDay.THURSDAY, PreferencePriority.SECONDARY)
                ),
                List.of(
                        new TimePreference(MatchHour.H19_00, PreferencePriority.PRIMARY),
                        new TimePreference(MatchHour.H20_00, PreferencePriority.PRIMARY),
                        new TimePreference(MatchHour.H21_00, PreferencePriority.PRIMARY),
                        new TimePreference(MatchHour.H22_00, PreferencePriority.PRIMARY)
                )
        ));
    }

    @Test
    void shouldRejectExcludedDayWithoutFourPrimaryHours() {
        assertThrows(InvalidTeamPreferenceException.class, () -> TeamPreferenceValidator.validate(
                RestrictionType.EXCLUDED_DAY,
                MatchDay.FRIDAY,
                null,
                List.of(
                        new DayPreference(MatchDay.MONDAY, PreferencePriority.PRIMARY),
                        new DayPreference(MatchDay.TUESDAY, PreferencePriority.PRIMARY),
                        new DayPreference(MatchDay.WEDNESDAY, PreferencePriority.PRIMARY)
                ),
                List.of(
                        new TimePreference(MatchHour.H18_00, PreferencePriority.SECONDARY),
                        new TimePreference(MatchHour.H19_00, PreferencePriority.PRIMARY),
                        new TimePreference(MatchHour.H20_00, PreferencePriority.PRIMARY),
                        new TimePreference(MatchHour.H21_00, PreferencePriority.PRIMARY)
                )
        ));
    }

    @Test
    void shouldRejectDuplicatedDays() {
        assertThrows(InvalidTeamPreferenceException.class, () -> TeamPreferenceValidator.validate(
                RestrictionType.EXCLUDED_HOUR,
                null,
                MatchHour.H18_00,
                List.of(
                        new DayPreference(MatchDay.MONDAY, PreferencePriority.PRIMARY),
                        new DayPreference(MatchDay.MONDAY, PreferencePriority.SECONDARY),
                        new DayPreference(MatchDay.WEDNESDAY, PreferencePriority.PRIMARY)
                ),
                List.of(
                        new TimePreference(MatchHour.H19_00, PreferencePriority.PRIMARY),
                        new TimePreference(MatchHour.H20_00, PreferencePriority.PRIMARY),
                        new TimePreference(MatchHour.H21_00, PreferencePriority.PRIMARY),
                        new TimePreference(MatchHour.H22_00, PreferencePriority.PRIMARY)
                )
        ));
    }
}