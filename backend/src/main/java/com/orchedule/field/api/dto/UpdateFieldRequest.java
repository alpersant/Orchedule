package com.orchedule.field.api.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdateFieldRequest(
        @NotBlank String name
) {}
