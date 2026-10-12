package com.orchedule.team.api.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record DeleteTeamBulkRequest(
        @NotEmpty(message = "At least one team id is required")
        List<@NotNull UUID> teamIds
) {
}
