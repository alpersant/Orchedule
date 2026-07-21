package com.orchedule.season.domain;

import com.orchedule.season.application.exception.InvalidSeasonException;

import java.time.LocalDate;

public final class SeasonValidator {

    private SeasonValidator() {
    }

    public static void validate(String name,
                                LocalDate startDate,
                                LocalDate endDate,
                                SeasonStatus status) {

        if (name == null || name.isBlank()) {
            throw new InvalidSeasonException("Season name is required");
        }

        if (startDate == null) {
            throw new InvalidSeasonException("Season startDate is required");
        }

        if (endDate == null) {
            throw new InvalidSeasonException("Season endDate is required");
        }

        if (endDate.isBefore(startDate)) {
            throw new InvalidSeasonException("Season endDate must be greater than or equal to startDate");
        }

        if (status == null) {
            throw new InvalidSeasonException("Season status is required");
        }
    }
}