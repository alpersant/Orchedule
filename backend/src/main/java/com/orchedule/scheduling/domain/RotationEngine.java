package com.orchedule.scheduling.domain;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.*;

/**
 * Core domain service implementing the rotation algorithm described in the
 * functional requirements: assign each team a day + hour for a round,
 * favoring FIRST-priority options while still rotating through SECOND
 * options over the season, and never repeating the same day or hour more
 * than two consecutive rounds for a given team.
 *
 * This engine is intentionally pure (no persistence, no Spring) so it can
 * be unit tested exhaustively and reused independently of infrastructure.
 */
public class RotationEngine {

    /**
     * Picks the best available day for a team, given the days actually open
     * this round (e.g. only Mon-Thu is enabled this week) and the team's
     * recent rotation history.
     */
    public DayOfWeek selectDay(TeamScheduleProfile profile, TeamRotationHistory history,
                                Set<DayOfWeek> openDaysThisRound) {
        List<DayOfWeek> firstChoice = filterOpen(profile.daysWithPriority(SlotPriority.FIRST), openDaysThisRound);
        List<DayOfWeek> secondChoice = filterOpen(profile.daysWithPriority(SlotPriority.SECOND), openDaysThisRound);

        Optional<DayOfWeek> pick = pickNonOverused(firstChoice, history::isDayOverused);
        if (pick.isPresent()) {
            return pick.get();
        }

        pick = pickNonOverused(secondChoice, history::isDayOverused);
        if (pick.isPresent()) {
            return pick.get();
        }

        // All options are "overused" (e.g. team only has one open day) —
        // fall back to any available option rather than failing the round.
        if (!firstChoice.isEmpty()) {
            return firstChoice.get(0);
        }
        if (!secondChoice.isEmpty()) {
            return secondChoice.get(0);
        }
        throw new IllegalStateException(
                "No day available for team within the days open this round");
    }

    public LocalTime selectHour(TeamScheduleProfile profile, TeamRotationHistory history,
                                 Set<LocalTime> openHoursThisRound) {
        List<LocalTime> firstChoice = filterOpen(profile.hoursWithPriority(SlotPriority.FIRST), openHoursThisRound);
        List<LocalTime> secondChoice = filterOpen(profile.hoursWithPriority(SlotPriority.SECOND), openHoursThisRound);

        Optional<LocalTime> pick = pickNonOverused(firstChoice, history::isHourOverused);
        if (pick.isPresent()) {
            return pick.get();
        }

        pick = pickNonOverused(secondChoice, history::isHourOverused);
        if (pick.isPresent()) {
            return pick.get();
        }

        if (!firstChoice.isEmpty()) {
            return firstChoice.get(0);
        }
        if (!secondChoice.isEmpty()) {
            return secondChoice.get(0);
        }
        throw new IllegalStateException(
                "No hour available for team within the hours open this round");
    }

    private <T> List<T> filterOpen(List<T> teamOptions, Set<T> openThisRound) {
        List<T> result = new ArrayList<>(teamOptions);
        result.retainAll(openThisRound);
        return result;
    }

    private <T> Optional<T> pickNonOverused(List<T> candidates, java.util.function.Predicate<T> isOverused) {
        return candidates.stream().filter(candidate -> !isOverused.test(candidate)).findFirst();
    }
}
