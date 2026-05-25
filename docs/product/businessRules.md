# Business Rules

## Purpose

This document defines the business rules that guide schedule generation in Orchedule.

The goal is to create schedules that respect competition constraints, team preferences, and fair distribution of desirable and undesirable time slots.

## Rule Categories

### Hard Rules
Hard rules must never be violated.

Examples:
- A field cannot host two matches at the same time.
- A team cannot play two matches in the same time slot.
- A time slot must belong to the competition calendar.
- A schedule entry must reference valid teams, fields, and time slots.
- Inactive teams, fields, or slots cannot be used.

### Soft Rules
Soft rules should be satisfied whenever possible.

Examples:
- Prefer the time slots selected by each team.
- Rotate prime-time and low-demand slots fairly.
- Balance the distribution of early and late matches.
- Minimize the number of undesirable assignments per team.
- Reduce repeated unfavorable patterns across the season.

## Fairness Rules

- No team should be systematically assigned only the least desirable time slots.
- Over the whole competition, desirable and undesirable slots should be distributed as evenly as possible.
- When two solutions are similar, the one with better fairness should win.
- Manual edits should not silently break fairness; they must be visible in audit results.

## Assignment Rules

- The generator must prioritize hard constraints first.
- If multiple valid assignments exist, the engine should choose the one with the best preference fit.
- If preference fit is equal, the engine should choose the one with better fairness distribution.
- If fairness is equal, the engine may use a deterministic tie-breaker.

## Validation Rules

- Every generated schedule must be validated.
- Validation must identify hard violations, soft deviations, and warning conditions.
- Audit results must be stored and visible to the user.
- Invalid schedules must never be marked as final.

## Editing Rules

- Users can modify generated assignments manually.
- Manual changes must be revalidated.
- The system must preserve a version history of schedule changes.
- When a change affects fairness, the audit must reflect it.

## Versioning Rules

- A schedule generation run creates a new version.
- A regenerated schedule must not overwrite previous versions silently.
- The user must be able to compare versions if needed.

## Audit Rules

- Every schedule generation must produce an audit result.
- The audit must explain why assignments were made.
- The audit must highlight missing preferences, constraint conflicts, and fairness imbalances.
- The audit must support decision traceability.

## Open Questions

- How should preference weights be configured?
- What is the exact fairness metric?
- How much manual override should be allowed?
- Which conflicts are acceptable as warnings instead of errors?