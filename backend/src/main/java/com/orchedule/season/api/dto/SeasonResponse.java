package com.orchedule.season.api.dto;

import com.orchedule.season.domain.SeasonStatus;

import java.time.LocalDate;
import java.util.UUID;

public record SeasonResponse(
        UUID id,
        String name,
        LocalDate startDate,
        LocalDate endDate,
        SeasonStatus status,
        boolean active
) {
}