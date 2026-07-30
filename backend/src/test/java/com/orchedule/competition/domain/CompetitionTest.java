package com.orchedule.competition.domain;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CompetitionTest {

    @Test
    void shouldCreateCompetitionWithDefaultsAsDraft() {
        Competition competition = Competition.createWithDefaults("Liga Local", "Desc");

        assertThat(competition.getId()).isNotNull();
        assertThat(competition.getName()).isEqualTo("Liga Local");
        assertThat(competition.getStatus()).isEqualTo(CompetitionStatus.DRAFT);
        assertThat(competition.getDefaultDays()).isEqualTo(Competition.DEFAULT_DAYS);
        assertThat(competition.getDefaultHours()).isEqualTo(Competition.DEFAULT_HOURS);
        assertThat(competition.getDefaultFieldCount()).isEqualTo(Competition.DEFAULT_FIELD_COUNT);
    }

    @Test
    void shouldUpdateDetails() {
        Competition competition = Competition.createWithDefaults("Old Name", "Old Desc");
        var before = competition.getUpdatedAt();

        competition.updateDetails("New Name", "New Desc");

        assertThat(competition.getName()).isEqualTo("New Name");
        assertThat(competition.getDescription()).isEqualTo("New Desc");
        assertThat(competition.getUpdatedAt()).isAfterOrEqualTo(before);
    }

    @Test
    void shouldUpdateDefaultDaysWithoutAffectingOriginalSetReference() {
        Competition competition = Competition.createWithDefaults("Liga", "Desc");
        Set<CompetitionDay> newDays = Set.of(CompetitionDay.SATURDAY, CompetitionDay.SUNDAY);

        competition.updateDefaultDays(newDays);

        assertThat(competition.getDefaultDays()).isEqualTo(newDays);
    }

    @Test
    void shouldReturnImmutableDefensiveCopyOfDays() {
        Competition competition = Competition.createWithDefaults("Liga", "Desc");

        assertThatThrownBy(() -> competition.getDefaultDays().add(CompetitionDay.SUNDAY))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void shouldUpdateDefaultHours() {
        Competition competition = Competition.createWithDefaults("Liga", "Desc");
        Set<CompetitionHour> newHours = Set.of(CompetitionHour.H20, CompetitionHour.H21);

        competition.updateDefaultHours(newHours);

        assertThat(competition.getDefaultHours()).isEqualTo(newHours);
    }

    @Test
    void shouldUpdateDefaultFieldCount() {
        Competition competition = Competition.createWithDefaults("Liga", "Desc");

        competition.updateDefaultFieldCount(3);

        assertThat(competition.getDefaultFieldCount()).isEqualTo(3);
    }

    @Test
    void shouldActivateDraftCompetition() {
        Competition competition = Competition.createWithDefaults("Liga", "Desc");

        competition.activate();

        assertThat(competition.getStatus()).isEqualTo(CompetitionStatus.ACTIVE);
        assertThat(competition.isActive()).isTrue();
    }

    @Test
    void shouldReactivateActiveCompetitionWithoutError() {
        Competition competition = Competition.createWithDefaults("Liga", "Desc");
        competition.activate();

        competition.activate();

        assertThat(competition.getStatus()).isEqualTo(CompetitionStatus.ACTIVE);
    }

    @Test
    void shouldRejectActivatingClosedCompetition() {
        Competition competition = Competition.createWithDefaults("Liga", "Desc");
        competition.close();

        assertThatThrownBy(competition::activate)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("closed");
    }

    @Test
    void shouldCloseCompetition() {
        Competition competition = Competition.createWithDefaults("Liga", "Desc");

        competition.close();

        assertThat(competition.getStatus()).isEqualTo(CompetitionStatus.CLOSED);
        assertThat(competition.isClosed()).isTrue();
        assertThat(competition.isActive()).isFalse();
    }
}
