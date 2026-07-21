package com.orchedule.team.application;

import com.orchedule.team.api.dto.DayPreferenceDto;
import com.orchedule.team.api.dto.SaveTeamPreferenceRequest;
import com.orchedule.team.api.dto.TimePreferenceDto;
import com.orchedule.team.application.exception.InvalidTeamPreferenceException;
import com.orchedule.team.application.exception.TeamNotFoundException;
import com.orchedule.team.domain.DayPreference;
import com.orchedule.team.domain.MatchDay;
import com.orchedule.team.domain.MatchHour;
import com.orchedule.team.domain.PreferencePriority;
import com.orchedule.team.domain.RestrictionType;
import com.orchedule.team.domain.Team;
import com.orchedule.team.domain.TeamPreference;
import com.orchedule.team.domain.TeamPreferenceRepository;
import com.orchedule.team.domain.TeamRepository;
import com.orchedule.team.domain.TimePreference;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SaveTeamPreferenceServiceTest {

    @Mock
    private TeamRepository teamRepository;

    @Mock
    private TeamPreferenceRepository teamPreferenceRepository;

    @InjectMocks
    private SaveTeamPreferenceService saveTeamPreferenceService;

    @Test
    void shouldSavePreferencesForExistingTeam() {
        UUID teamId = UUID.randomUUID();
        when(teamRepository.findById(teamId)).thenReturn(Optional.of(new Team(
                teamId,
                "Team A",
                true,
                OffsetDateTime.now(),
                OffsetDateTime.now()
        )));
        when(teamPreferenceRepository.findByTeamId(teamId)).thenReturn(Optional.empty());
        when(teamPreferenceRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        SaveTeamPreferenceRequest request = new SaveTeamPreferenceRequest(
                RestrictionType.EXCLUDED_HOUR,
                null,
                MatchHour.H18_00,
                List.of(
                        new DayPreferenceDto(MatchDay.MONDAY, PreferencePriority.PRIMARY),
                        new DayPreferenceDto(MatchDay.TUESDAY, PreferencePriority.PRIMARY),
                        new DayPreferenceDto(MatchDay.WEDNESDAY, PreferencePriority.PRIMARY),
                        new DayPreferenceDto(MatchDay.THURSDAY, PreferencePriority.SECONDARY)
                ),
                List.of(
                        new TimePreferenceDto(MatchHour.H19_00, PreferencePriority.PRIMARY),
                        new TimePreferenceDto(MatchHour.H20_00, PreferencePriority.PRIMARY),
                        new TimePreferenceDto(MatchHour.H21_00, PreferencePriority.PRIMARY),
                        new TimePreferenceDto(MatchHour.H22_00, PreferencePriority.PRIMARY)
                )
        );

        var response = saveTeamPreferenceService.save(teamId, request);

        assertEquals(teamId, response.teamId());
        assertEquals(RestrictionType.EXCLUDED_HOUR, response.restrictionType());
        assertEquals(MatchHour.H18_00, response.excludedHour());

        ArgumentCaptor<TeamPreference> captor = ArgumentCaptor.forClass(TeamPreference.class);
        verify(teamPreferenceRepository).save(captor.capture());
        assertEquals(4, captor.getValue().timePreferences().size());
    }

    @Test
    void shouldFailWhenTeamDoesNotExist() {
        UUID teamId = UUID.randomUUID();
        when(teamRepository.findById(teamId)).thenReturn(Optional.empty());

        SaveTeamPreferenceRequest request = new SaveTeamPreferenceRequest(
                RestrictionType.EXCLUDED_HOUR,
                null,
                MatchHour.H18_00,
                List.of(
                        new DayPreferenceDto(MatchDay.MONDAY, PreferencePriority.PRIMARY),
                        new DayPreferenceDto(MatchDay.TUESDAY, PreferencePriority.PRIMARY),
                        new DayPreferenceDto(MatchDay.WEDNESDAY, PreferencePriority.PRIMARY)
                ),
                List.of(
                        new TimePreferenceDto(MatchHour.H19_00, PreferencePriority.PRIMARY),
                        new TimePreferenceDto(MatchHour.H20_00, PreferencePriority.PRIMARY),
                        new TimePreferenceDto(MatchHour.H21_00, PreferencePriority.PRIMARY),
                        new TimePreferenceDto(MatchHour.H22_00, PreferencePriority.PRIMARY)
                )
        );

        assertThrows(TeamNotFoundException.class, () -> saveTeamPreferenceService.save(teamId, request));
    }

    @Test
    void shouldFailWhenPreferencesAreInvalid() {
        UUID teamId = UUID.randomUUID();
        when(teamRepository.findById(teamId)).thenReturn(Optional.of(new Team(
                teamId,
                "Team A",
                true,
                OffsetDateTime.now(),
                OffsetDateTime.now()
        )));

        SaveTeamPreferenceRequest request = new SaveTeamPreferenceRequest(
                RestrictionType.EXCLUDED_DAY,
                MatchDay.FRIDAY,
                null,
                List.of(
                        new DayPreferenceDto(MatchDay.MONDAY, PreferencePriority.PRIMARY),
                        new DayPreferenceDto(MatchDay.TUESDAY, PreferencePriority.PRIMARY),
                        new DayPreferenceDto(MatchDay.WEDNESDAY, PreferencePriority.PRIMARY)
                ),
                List.of(
                        new TimePreferenceDto(MatchHour.H18_00, PreferencePriority.SECONDARY),
                        new TimePreferenceDto(MatchHour.H19_00, PreferencePriority.PRIMARY),
                        new TimePreferenceDto(MatchHour.H20_00, PreferencePriority.PRIMARY),
                        new TimePreferenceDto(MatchHour.H21_00, PreferencePriority.PRIMARY)
                )
        );

        assertThrows(InvalidTeamPreferenceException.class, () -> saveTeamPreferenceService.save(teamId, request));
    }
}