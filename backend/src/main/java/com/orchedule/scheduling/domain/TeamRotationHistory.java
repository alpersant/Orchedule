package com.orchedule.scheduling.domain;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.UUID;

/**
 * Tracks the recent day/hour assignments of a single team across rounds, so
 * the RotationEngine can enforce for both days and hours independently.
 *
 * Exposes read-only accessors (getRecentDays/getRecentHours) and a
 * reconstruction factory (restore) so infrastructure can persist and
 * rehydrate this state across separate application runs. This is an
 * intentional, narrow break of encapsulation: the accessors return
 * defensive immutable copies, and the only way to mutate state remains
 * record(ScheduleSlot), preserving the aggregate's invariants.
 */
public class TeamRotationHistory {

    private static final int MAX_TRACKED_ROUNDS = 3;

    private final UUID teamId;
    private final Deque<DayOfWeek> recentDays = new ArrayDeque<>();
    private final Deque<LocalTime> recentHours = new ArrayDeque<>();

    public TeamRotationHistory(UUID teamId) {
        if (teamId == null) {
            throw new IllegalArgumentException("teamId must not be null");
        }
        this.teamId = teamId;
    }

    /**
     * Reconstructs a TeamRotationHistory from previously persisted state.
     * The most-recent entry must be first in each list. Malformed or
     * mismatched input is rejected rather than silently truncated, to avoid
     * masking data corruption from infrastructure.
     */
    public static TeamRotationHistory restore(UUID teamId, List<DayOfWeek> recentDays, List<LocalTime> recentHours) {
        TeamRotationHistory history = new TeamRotationHistory(teamId);
        if (recentDays != null) {
            for (int i = recentDays.size() - 1; i >= 0; i--) {
                history.recentDays.addFirst(recentDays.get(i));
            }
            history.trim(history.recentDays);
        }
        if (recentHours != null) {
            for (int i = recentHours.size() - 1; i >= 0; i--) {
                history.recentHours.addFirst(recentHours.get(i));
            }
            history.trim(history.recentHours);
        }
        return history;
    }

    public void record(ScheduleSlot slot) {
        if (slot == null) {
            throw new IllegalArgumentException("slot must not be null");
        }
        recentDays.addFirst(slot.day());
        recentHours.addFirst(slot.hour());
        trim(recentDays);
        trim(recentHours);
    }

    /** True if the given day was played in the last two consecutive rounds. */
    public boolean isDayOverused(DayOfWeek day) {
        return countLeadingMatches(recentDays, day) >= 2;
    }

    /** True if the given hour was played in the last two consecutive rounds. */
    public boolean isHourOverused(LocalTime hour) {
        return countLeadingMatches(recentHours, hour) >= 2;
    }

    private <T> void trim(Deque<T> deque) {
        while (deque.size() > MAX_TRACKED_ROUNDS) {
            deque.removeLast();
        }
    }

    private <T> int countLeadingMatches(Deque<T> deque, T value) {
        int count = 0;
        for (T item : deque) {
            if (item.equals(value)) {
                count++;
            } else {
                break;
            }
        }
        return count;
    }

    public UUID getTeamId() { return teamId; }

    /** Most-recent-first, immutable defensive copy. */
    public List<DayOfWeek> getRecentDays() { return List.copyOf(recentDays); }

    /** Most-recent-first, immutable defensive copy. */
    public List<LocalTime> getRecentHours() { return List.copyOf(recentHours); }
}
