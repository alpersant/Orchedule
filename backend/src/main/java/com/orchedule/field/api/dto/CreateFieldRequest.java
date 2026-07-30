package com.orchedule.field.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateFieldRequest(
        @NotNull UUID venueId,
        @NotBlank String name
) {}
