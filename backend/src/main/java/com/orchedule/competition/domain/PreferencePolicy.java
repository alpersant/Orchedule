package com.orchedule.competition.domain;

/**
 * Governs how strictly team day/hour preferences are validated for a
 * competition. The application is generic — most organizers just want
 * teams to pick FIRST/SECOND freely. The strict elimination rule
 * described in the original requirements doc (exactly one option removed,
 * with type-dependent minimums) is an OPT-IN policy, not a hardcoded
 * system rule, so the same codebase serves any competition shape.
 */
public enum PreferencePolicy {

    /**
     * No structural constraints beyond "at least one FIRST day/hour and
     * one SECOND is recommended but not required". Default for new
     * competitions — maximizes flexibility.
     */
    FLEXIBLE,

    /**
     * Enforces the original strict rule: a team must drop exactly one
     * option (one day OR one hour, never both) from the 10 available
     * (5 week days + 5 hours), and depending on which type was dropped,
     * apply the matching FIRST/SECOND minimums:
     *   - Dropped an HOUR  -> remaining 4 hours all FIRST; days: >=3 FIRST, rest SECOND
     *   - Dropped a DAY    -> remaining 4 days: >=3 FIRST, rest SECOND; hours: >=4 FIRST, 1 SECOND
     */
    STRICT_SINGLE_ELIMINATION
}
