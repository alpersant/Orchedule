package com.orchedule.competition.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.orchedule.competition.api.dto.CreateCompetitionRequest;
import com.orchedule.competition.api.dto.UpdateCompetitionDefaultsRequest;
import com.orchedule.competition.application.*;
import com.orchedule.competition.application.exception.CompetitionNotFoundException;
import com.orchedule.competition.domain.Competition;
import com.orchedule.competition.domain.CompetitionDay;
import com.orchedule.competition.domain.CompetitionHour;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CompetitionController.class)
class CompetitionControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean private CreateCompetitionService createCompetitionService;
    @MockitoBean private GetCompetitionService getCompetitionService;
    @MockitoBean private UpdateCompetitionService updateCompetitionService;
    @MockitoBean private UpdateCompetitionDefaultsService updateCompetitionDefaultsService;
    @MockitoBean private ActivateCompetitionService activateCompetitionService;
    @MockitoBean private CloseCompetitionService closeCompetitionService;

    @Test
    void shouldCreateCompetitionAndReturn201() throws Exception {
        Competition competition = Competition.createWithDefaults("Liga Local", "Desc");
        when(createCompetitionService.create("Liga Local", "Desc")).thenReturn(competition);

        CreateCompetitionRequest request = new CreateCompetitionRequest("Liga Local", "Desc");

        mockMvc.perform(post("/api/competitions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Liga Local"))
                .andExpect(jsonPath("$.status").value("DRAFT"));
    }

    @Test
    void shouldReturn400WhenNameIsBlank() throws Exception {
        CreateCompetitionRequest request = new CreateCompetitionRequest("", "Desc");

        mockMvc.perform(post("/api/competitions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldGetCompetitionById() throws Exception {
        UUID id = UUID.randomUUID();
        Competition competition = Competition.createWithDefaults("Liga Local", "Desc");
        when(getCompetitionService.getById(id)).thenReturn(competition);

        mockMvc.perform(get("/api/competitions/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Liga Local"));
    }

    @Test
    void shouldReturn404WhenCompetitionNotFound() throws Exception {
        UUID id = UUID.randomUUID();
        when(getCompetitionService.getById(id)).thenThrow(new CompetitionNotFoundException(id));

        mockMvc.perform(get("/api/competitions/{id}", id))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldListAllCompetitionsWhenNoStatusProvided() throws Exception {
        Competition competition = Competition.createWithDefaults("Liga Local", "Desc");
        when(getCompetitionService.getAll()).thenReturn(List.of(competition));

        mockMvc.perform(get("/api/competitions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Liga Local"));
    }

    @Test
    void shouldUpdateDefaults() throws Exception {
        UUID id = UUID.randomUUID();
        Competition competition = Competition.createWithDefaults("Liga Local", "Desc");
        when(updateCompetitionDefaultsService.updateDefaults(any(), any(), any(), any(Integer.class)))
                .thenReturn(competition);

        UpdateCompetitionDefaultsRequest request = new UpdateCompetitionDefaultsRequest(
                Set.of(CompetitionDay.SATURDAY), Set.of(CompetitionHour.H18), 2);

        mockMvc.perform(put("/api/competitions/{id}/defaults", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void shouldActivateCompetition() throws Exception {
        UUID id = UUID.randomUUID();
        Competition competition = Competition.createWithDefaults("Liga Local", "Desc");
        competition.activate();
        when(activateCompetitionService.activate(id)).thenReturn(competition);

        mockMvc.perform(post("/api/competitions/{id}/activate", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void shouldCloseCompetition() throws Exception {
        UUID id = UUID.randomUUID();
        Competition competition = Competition.createWithDefaults("Liga Local", "Desc");
        competition.close();
        when(closeCompetitionService.close(id)).thenReturn(competition);

        mockMvc.perform(post("/api/competitions/{id}/close", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CLOSED"));
    }
}
