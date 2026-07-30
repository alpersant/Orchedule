package com.orchedule.competition.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateCompetitionRequest(
        @NotBlank @Size(max = 150) String name,
        @Size(max = 1000) String description
) {}
