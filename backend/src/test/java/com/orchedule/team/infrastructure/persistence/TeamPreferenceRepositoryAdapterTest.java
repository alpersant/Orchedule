package com.orchedule.team.infrastructure.persistence;

import com.orchedule.team.domain.DayPreference;
import com.orchedule.team.domain.MatchDay;
import com.orchedule.team.domain.MatchHour;
import com.orchedule.team.domain.PreferencePriority;
import com.orchedule.team.domain.RestrictionType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
@Import({TeamPreferenceRepositoryAdapter.class, TeamRepositoryAdapter.class})
class TeamPreferenceRepositoryAdapterTest {

    @Autowired
    private TeamPreferenceRepositoryAdapter preferenceAdapter;

    @Autowired
    private TeamRepositoryAdapter teamAdapter;

    @Test
    void shouldSaveAndLoadTeamPreference() {
        UUID teamId = teamAdapter.create("Real Madrid");
        OffsetDateTime now = OffsetDateTime.now();

        TeamPreference saved = preferenceAdapter.save(new TeamPreference(
                teamId,
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
                        new com.orchedule.team.domain.TimePreference(MatchHour.H19_00, PreferencePriority.PRIMARY),
                        new com.orchedule.team.domain.TimePreference(MatchHour.H20_00, PreferencePriority.PRIMARY),
                        new com.orchedule.team.domain.TimePreference(MatchHour.H21_00, PreferencePriority.PRIMARY),
                        new com.orchedule.team.domain.TimePreference(MatchHour.H22_00, PreferencePriority.PRIMARY)
                ),
                now,
                now
        ));

        TeamPreference loaded = preferenceAdapter.findByTeamId(teamId).orElseThrow();

        assertEquals(teamId, loaded.teamId());
        assertEquals(RestrictionType.EXCLUDED_HOUR, loaded.restrictionType());
        assertEquals(4, loaded.timePreferences().size());
        assertEquals(4, saved.timePreferences().size());
    }
}