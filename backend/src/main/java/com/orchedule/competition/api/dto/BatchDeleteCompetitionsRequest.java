package com.orchedule.competition.api.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.UUID;

public record BatchDeleteCompetitionsRequest(
        @NotEmpty
        @Size(max = 100, message = "A maximum of 100 competitions can be deleted per request")
        List<UUID> competitionIds
) {}
