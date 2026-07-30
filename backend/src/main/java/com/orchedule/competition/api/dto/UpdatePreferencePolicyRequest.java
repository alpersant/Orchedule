package com.orchedule.competition.api.dto;

import jakarta.validation.constraints.NotNull;

public record UpdatePreferencePolicyRequest(
        @NotNull String preferencePolicy
) {}
