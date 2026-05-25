# Functional Requirements

## Product Purpose

Orchedule is a cross-platform sports scheduling application that assigns match days, fields, and time slots for multi-team competitions in a fair and optimized way.

The product must adapt to different competition sizes, different numbers of fields, different weekly calendars, and different availability patterns.

## Scope

The system must support:
- Variable fields by week.
- Variable days of play by week.
- Variable available hours by day and by week.
- Team preferences for days and hours.
- Fair rotation across the season.
- Competition templates for easier data entry.
- Audit and validation of generated schedules.
- A friendly UX for non-technical and advanced users.

## Global Scheduling Model

The system must allow each competition to define:
- Which days of the week are active.
- Which hours are active.
- Which fields are available.
- Whether availability is global for the whole competition or customized by week.

The default configuration should be:
- Monday to Friday.
- 18:00, 19:00, 20:00, 21:00, and 22:00.

This default must be editable by the user.

## Field Requirements

### FR-01: Field management
The user must be able to create, edit, activate, and deactivate fields.

### FR-02: Weekly field availability
The user must be able to define:
- a global field set for the whole competition,
- or a different field set for each week.

### FR-03: Variable field count
The number of playable fields may change from one week to another.

## Day Requirements

### FR-04: Weekly day availability
The user must be able to define which days are active for each competition week.

### FR-05: Default day set
The default week configuration should be Monday to Friday.

### FR-06: Full-week support
The system must support competitions that play Monday to Sunday.

## Hour Requirements

### FR-07: Hour management
The user must be able to define the available hours for the competition or for each week.

### FR-08: Default hour set
The default hour set should be:
- 18:00
- 19:00
- 20:00
- 21:00
- 22:00

### FR-09: Expanded hour ranges
The system must support:
- all hours from 09:00 to 22:00,
- partial daily availability,
- week-specific hour availability.

## Team Preference Requirements

### FR-10: Team creation
The user must be able to create and manage teams.

### FR-11: Team preference setup
Each team must be able to define preferred and non-preferred days and hours.

### FR-12: Single exclusion rule
For the MVP, each team may exclude only one option at a time:
- either one day, or
- one hour.

### FR-13: Preference categorization
The system must classify options into:
- first-choice preferences,
- second-choice preferences,
- excluded options.

## Preference Logic Requirements

### FR-14: Hour exclusion behavior
If a team excludes one hour:
- that hour must never be assigned to that team,
- the remaining hours become first-choice options,
- the schedule should rotate across the preferred hours,
- the same hour should not repeat more than two consecutive times whenever possible.

### FR-15: Day exclusion behavior
If a team excludes one day:
- that day becomes forbidden,
- the user must define at least three preferred days,
- the remaining days may be treated as second-choice days if needed,
- the engine should rotate weekly across days,
- first-choice days should be favored over second-choice days.

### FR-16: Combined preference model
When a day is excluded:
- the user must be able to choose at least three preferred days,
- and at least four preferred hours.

### FR-17: Consecutive repetition control
The engine should avoid assigning the same day or hour more than two times in a row unless there is no valid alternative.

## Scheduling Engine Requirements

### FR-18: Schedule generation
The system must generate schedules automatically from competition data, field availability, time availability, and team preferences.

### FR-19: Optimization priority
The engine must optimize in this order:
1. Hard constraints.
2. Preference satisfaction.
3. Fair rotation.
4. Deterministic tie-breaking.

### FR-20: Fair rotation
The engine must distribute desirable and undesirable slots across all teams as evenly as possible over the season.

### FR-21: Scarcity handling
The engine must work correctly even when field and hour availability is very limited.

### FR-22: Performance
The engine must be efficient enough to handle large and constrained schedules without noticeable delay in normal usage.

## Audit Requirements

### FR-23: Audit section
The system must include an audit section for every generated schedule.

### FR-24: Rule compliance report
The audit must show:
- whether hard constraints were met,
- whether preferences were respected,
- how fairness was distributed,
- where manual edits affected the result.

### FR-25: Explainability
The system must be able to explain why a team received a specific day or hour.

## Template Requirements

### FR-26: Competition templates
The system must support templates for:
- fields,
- days,
- hours,
- team preference patterns,
- competition configurations.

### FR-27: Data reuse
The user must be able to load a template and adapt it to a new competition.

## UX Requirements

### FR-28: Friendly interface
The UI must be easy to understand for non-technical users.

### FR-29: Advanced mode
The UI must still allow advanced users to configure detailed options.

### FR-30: Guided setup
The system should guide the user step by step through:
- competition setup,
- fields,
- availability,
- teams,
- preferences,
- generation,
- audit.

## Out of Scope for MVP

- Complex permission roles.
- Multi-organization administration.
- AI agents as a core dependency.
- External calendar integrations.
- Public APIs for third parties.
- Deep statistical dashboards.

## Open Points to Confirm

- Exact definition of fairness scoring.
- Whether templates can be shared across competitions.
- Whether a team can exclude both a day and an hour in future versions.
- Whether schedule generation is locked after publishing.
- Whether the user can compare multiple generated versions.