package com.orchedule.competition.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DuplicateCompetitionRequest(
        @NotBlank @Size(max = 120) String newName
) {}
