# Domain Model

## Purpose

This document defines the core domain concepts of Orchedule and how they relate to each other.

The domain must remain simple enough for an MVP, but flexible enough to evolve later.

## Core Concepts

Orchedule is built around a few core business objects that represent competitions, availability, preferences, generated schedules, and audit results.

## Main Entities

### Competition
Represents a competition or league being scheduled.

Attributes:
- id
- name
- description
- startDate
- endDate
- status
- defaultWeeklyPattern

### CompetitionWeek
Represents one configurable week inside a competition.

Attributes:
- id
- competitionId
- weekNumber
- activeDays
- activeHours
- activeFields
- overridesEnabled

### Team
Represents a participating team.

Attributes:
- id
- competitionId
- name
- category
- active

### Field
Represents a place where matches are played.

Attributes:
- id
- competitionId
- name
- location
- capacity
- active

### TimeSlot
Represents a scheduling opportunity defined by a day and hour, optionally linked to a field and a week.

Attributes:
- id
- competitionWeekId
- dayOfWeek
- startTime
- endTime
- fieldId
- active

### PreferenceProfile
Represents the preference configuration of one team.

Attributes:
- id
- teamId
- excludedType
- excludedValue
- preferredDays
- secondaryDays
- preferredHours
- secondaryHours

### Schedule
Represents the generated plan for a competition or a specific week.

Attributes:
- id
- competitionId
- generatedAt
- status
- version
- generationNotes

### ScheduleEntry
Represents one assigned match or slot inside a schedule.

Attributes:
- id
- scheduleId
- homeTeamId
- awayTeamId
- fieldId
- timeSlotId
- roundNumber
- validationStatus

### ConstraintRule
Represents a business rule that can be evaluated by the engine.

Attributes:
- id
- competitionId
- ruleType
- severity
- description
- active

### AuditReport
Represents the validation and fairness result of a generated schedule.

Attributes:
- id
- scheduleId
- resultStatus
- summary
- details
- createdAt

### ScheduleVersion
Represents a versioned snapshot of a schedule after generation or manual edit.

Attributes:
- id
- scheduleId
- versionNumber
- createdAt
- createdBy
- changeReason

## Relationships

- A Competition has many CompetitionWeeks.
- A Competition has many Teams.
- A Competition has many Fields.
- A CompetitionWeek has many TimeSlots.
- A Team has one PreferenceProfile.
- A Competition has many Schedule instances.
- A Schedule has many ScheduleEntries.
- A Schedule has many AuditReports.
- A Schedule can have many ScheduleVersions.

## Domain Rules

- Hard constraints must never be violated.
- Soft preferences should be respected as much as possible.
- Fairness must be distributed across the season.
- A generated schedule must always be auditable.
- Manual edits should preserve validation visibility.
- A schedule version must not overwrite previous versions silently.

## States

### Competition status
- draft
- active
- closed

### Schedule status
- draft
- generated
- reviewed
- final

### Audit status
- valid
- valid_with_warnings
- invalid

## Suggested Persistence Rules

- Preference profiles should be stored per team.
- Weekly overrides should be stored separately from global defaults.
- Schedule versions should be immutable snapshots.
- Audit reports should be stored with the generated schedule.

## Next Domain Step

After this model, the next useful document is:
- entity lifecycle,
- rule priorities,
- and schedule generation flow.