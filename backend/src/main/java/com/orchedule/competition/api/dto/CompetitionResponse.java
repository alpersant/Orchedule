package com.orchedule.competition.api.dto;

import com.orchedule.competition.domain.Competition;
import com.orchedule.competition.domain.CompetitionDay;
import com.orchedule.competition.domain.CompetitionHour;

import java.time.OffsetDateTime;
import java.util.Set;
import java.util.UUID;

public record CompetitionResponse(
        UUID id,
        String name,
        String description,
        String status,
        Set<CompetitionDay> defaultDays,
        Set<CompetitionHour> defaultHours,
        int defaultFieldCount,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
    public static CompetitionResponse from(Competition competition) {
        return new CompetitionResponse(
                competition.getId(), competition.getName(), competition.getDescription(),
                competition.getStatus().name(), competition.getDefaultDays(), competition.getDefaultHours(),
                competition.getDefaultFieldCount(), competition.getCreatedAt(), competition.getUpdatedAt());
    }
}
