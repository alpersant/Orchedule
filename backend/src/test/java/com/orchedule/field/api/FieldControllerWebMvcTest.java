package com.orchedule.field.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.orchedule.field.api.dto.CreateFieldRequest;
import com.orchedule.field.api.dto.SetWeeklyAvailabilityRequest;
import com.orchedule.field.application.*;
import com.orchedule.field.application.exception.FieldNotFoundException;
import com.orchedule.field.domain.Field;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(FieldController.class)
class FieldControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean private CreateFieldService createFieldService;
    @MockBean private GetFieldService getFieldService;
    @MockBean private UpdateFieldService updateFieldService;
    @MockBean private ActivateFieldService activateFieldService;
    @MockBean private DeactivateFieldService deactivateFieldService;
    @MockBean private SetFieldAvailabilityForWeekService setAvailabilityService;
    @MockBean private GetFieldAvailabilityService getAvailabilityService;

    @Test
    void shouldCreateFieldAndReturn201() throws Exception {
        UUID venueId = UUID.randomUUID();
        Field field = Field.create(venueId, "Field 1");
        when(createFieldService.create(venueId, "Field 1")).thenReturn(field);

        CreateFieldRequest request = new CreateFieldRequest(venueId, "Field 1");

        mockMvc.perform(post("/api/fields")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Field 1"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void shouldReturn400WhenNameIsBlank() throws Exception {
        CreateFieldRequest request = new CreateFieldRequest(UUID.randomUUID(), "");

        mockMvc.perform(post("/api/fields")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldGetFieldById() throws Exception {
        UUID id = UUID.randomUUID();
        Field field = Field.create(UUID.randomUUID(), "Field 1");
        when(getFieldService.getById(id)).thenReturn(field);

        mockMvc.perform(get("/api/fields/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Field 1"));
    }

    @Test
    void shouldReturn404WhenFieldNotFound() throws Exception {
        UUID id = UUID.randomUUID();
        when(getFieldService.getById(id)).thenThrow(new FieldNotFoundException("Field not found: " + id));

        mockMvc.perform(get("/api/fields/{id}", id))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldListActiveFieldsWhenNoVenueProvided() throws Exception {
        Field field = Field.create(UUID.randomUUID(), "Field 1");
        when(getFieldService.getAllActive()).thenReturn(List.of(field));

        mockMvc.perform(get("/api/fields"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Field 1"));
    }

    @Test
    void shouldActivateField() throws Exception {
        UUID id = UUID.randomUUID();
        Field field = Field.create(UUID.randomUUID(), "Field 1");
        when(activateFieldService.activate(id)).thenReturn(field);

        mockMvc.perform(post("/api/fields/{id}/activate", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void shouldDeactivateField() throws Exception {
        UUID id = UUID.randomUUID();
        Field field = Field.create(UUID.randomUUID(), "Field 1");
        field.deactivate();
        when(deactivateFieldService.deactivate(id)).thenReturn(field);

        mockMvc.perform(post("/api/fields/{id}/deactivate", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("INACTIVE"));
    }

    @Test
    void shouldSetWeeklyAvailability() throws Exception {
        UUID seasonId = UUID.randomUUID();
        UUID fieldId = UUID.randomUUID();
        when(setAvailabilityService.setForWeek(any(), any(Integer.class), any()))
                .thenReturn(List.of());

        SetWeeklyAvailabilityRequest request =
                new SetWeeklyAvailabilityRequest(seasonId, 2, List.of(fieldId));

        mockMvc.perform(put("/api/fields/availability")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void shouldGetWeeklyAvailability() throws Exception {
        UUID seasonId = UUID.randomUUID();
        when(getAvailabilityService.getForSeason(seasonId)).thenReturn(List.of());

        mockMvc.perform(get("/api/fields/availability").param("seasonId", seasonId.toString()))
                .andExpect(status().isOk());
    }
}
