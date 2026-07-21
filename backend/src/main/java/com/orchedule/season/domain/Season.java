package com.orchedule.season.domain;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

public record Season(
        UUID id,
        String name,
        LocalDate startDate,
        LocalDate endDate,
        SeasonStatus status,
        boolean active,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}