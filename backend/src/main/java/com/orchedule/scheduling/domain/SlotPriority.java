package com.orchedule.scheduling.domain;

/**
 * Represents how much a team favors a given day or hour.
 * FIRST = preferred option, SECOND = fallback option.
 * A day/hour removed entirely by the team (per functional requirement:
 * "quita una opción") is simply absent from the team's profile.
 */
public enum SlotPriority {
    FIRST,
    SECOND
}
