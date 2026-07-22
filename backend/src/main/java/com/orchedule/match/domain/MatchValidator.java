package com.orchedule.match.domain;

import com.orchedule.match.application.exception.InvalidMatchException;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

public final class MatchValidator {
    private MatchValidator() {}

    public static void validate(UUID seasonId, UUID homeTeamId, UUID awayTeamId, UUID venueId, OffsetDateTime scheduledAt, MatchStatus status, Integer round, String notes) {
        if (seasonId == null) throw new InvalidMatchException("Season is required");
        if (homeTeamId == null) throw new InvalidMatchException("Home team is required");
        if (awayTeamId == null) throw new InvalidMatchException("Away team is required");
        if (venueId == null) throw new InvalidMatchException("Venue is required");
        if (scheduledAt == null) throw new InvalidMatchException("Scheduled date is required");
        if (scheduledAt.isBefore(OffsetDateTime.now(ZoneOffset.UTC).minusMinutes(1))) throw new InvalidMatchException("Scheduled date cannot be in the past");
        if (homeTeamId.equals(awayTeamId)) throw new InvalidMatchException("Home team and away team must be different");
        if (status == null) throw new InvalidMatchException("Match status is required");
        if (round != null && round < 1) throw new InvalidMatchException("Round must be greater than 0");
        if (notes != null && notes.trim().length() > 500) throw new InvalidMatchException("Notes must not exceed 500 characters");
    }
}
