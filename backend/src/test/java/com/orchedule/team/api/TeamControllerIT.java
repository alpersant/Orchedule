package com.orchedule.team.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.orchedule.team.api.dto.CreateTeamRequest;
import com.orchedule.team.api.dto.DayPreferenceDto;
import com.orchedule.team.api.dto.SaveTeamPreferenceRequest;
import com.orchedule.team.api.dto.TimePreferenceDto;
import com.orchedule.team.domain.MatchDay;
import com.orchedule.team.domain.MatchHour;
import com.orchedule.team.domain.PreferencePriority;
import com.orchedule.team.domain.RestrictionType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class TeamControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldCreateTeamAndSavePreferencesEndToEnd() throws Exception {
        String createResponse = mockMvc.perform(post("/api/teams")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateTeamRequest("Integration Team"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Integration Team"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        String teamId = objectMapper.readTree(createResponse).get("id").asText();

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

        mockMvc.perform(put("/api/teams/{teamId}/preferences", teamId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.teamId").value(teamId))
                .andExpect(jsonPath("$.restrictionType").value("EXCLUDED_HOUR"));

        mockMvc.perform(get("/api/teams/{teamId}/preferences", teamId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.teamId").value(teamId))
                .andExpect(jsonPath("$.excludedHour").value("H18_00"))
                .andExpect(jsonPath("$.dayPreferences.length()").value(4))
                .andExpect(jsonPath("$.timePreferences.length()").value(4));
    }
}