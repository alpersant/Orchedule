package com.orchedule.competition.api.dto;

import com.orchedule.competition.domain.CompetitionDay;
import com.orchedule.competition.domain.CompetitionHour;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;

import java.util.Set;

public record UpdateCompetitionDefaultsRequest(
        @NotEmpty Set<CompetitionDay> defaultDays,
        @NotEmpty Set<CompetitionHour> defaultHours,
        @Min(1) int defaultFieldCount
) {}
