package com.orchedule.team.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.orchedule.team.api.dto.CreateTeamRequest;
import com.orchedule.team.api.dto.DayPreferenceDto;
import com.orchedule.team.api.dto.SaveTeamPreferenceRequest;
import com.orchedule.team.api.dto.TeamPreferenceResponse;
import com.orchedule.team.api.dto.TeamResponse;
import com.orchedule.team.api.dto.TimePreferenceDto;
import com.orchedule.team.application.CreateTeamService;
import com.orchedule.team.application.GetTeamPreferenceService;
import com.orchedule.team.application.GetTeamService;
import com.orchedule.team.application.SaveTeamPreferenceService;
import com.orchedule.team.application.UpdateTeamService;
import com.orchedule.team.domain.MatchDay;
import com.orchedule.team.domain.MatchHour;
import com.orchedule.team.domain.PreferencePriority;
import com.orchedule.team.domain.RestrictionType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TeamController.class)
class TeamControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CreateTeamService createTeamService;

    @MockitoBean
    private UpdateTeamService updateTeamService;

    @MockitoBean
    private GetTeamService getTeamService;

    @MockitoBean
    private SaveTeamPreferenceService saveTeamPreferenceService;

    @MockitoBean
    private GetTeamPreferenceService getTeamPreferenceService;

    @Test
    void shouldCreateTeam() throws Exception {
        UUID teamId = UUID.randomUUID();
        when(createTeamService.create(any())).thenReturn(new TeamResponse(teamId, "Real Madrid", true));

        mockMvc.perform(post("/api/teams")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateTeamRequest("Real Madrid"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(teamId.toString()))
                .andExpect(jsonPath("$.name").value("Real Madrid"))
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    void shouldReturnAllTeams() throws Exception {
        when(getTeamService.getAll()).thenReturn(List.of(
                new TeamResponse(UUID.randomUUID(), "Real Madrid", true),
                new TeamResponse(UUID.randomUUID(), "FC Barcelona", true)
        ));

        mockMvc.perform(get("/api/teams"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Real Madrid"))
                .andExpect(jsonPath("$[1].name").value("FC Barcelona"));
    }

    @Test
    void shouldSavePreference() throws Exception {
        UUID teamId = UUID.randomUUID();
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

        when(saveTeamPreferenceService.save(eq(teamId), any())).thenReturn(new TeamPreferenceResponse(
                teamId,
                RestrictionType.EXCLUDED_HOUR,
                null,
                MatchHour.H18_00,
                request.dayPreferences(),
                request.timePreferences()
        ));

        mockMvc.perform(put("/api/teams/{teamId}/preferences", teamId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.teamId").value(teamId.toString()))
                .andExpect(jsonPath("$.restrictionType").value("EXCLUDED_HOUR"))
                .andExpect(jsonPath("$.excludedHour").value("H18_00"));
    }
}