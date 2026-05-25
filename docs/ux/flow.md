# UX Flow

## Purpose

This document describes the ideal user journey in Orchedule, from creating a competition to generating, reviewing, and finalizing a schedule.

The UX must be simple enough for first-time users and flexible enough for advanced users who need detailed control.

## UX Principles

- Keep the default path short.
- Reveal advanced options progressively.
- Use plain language.
- Show the impact of each configuration immediately.
- Make fairness and conflicts visible.
- Preserve consistency across screens.
- Avoid clutter in calendar views.

## Primary Journey

### 1. Start from dashboard
The user opens the application and sees:
- recent competitions,
- draft schedules,
- quick actions,
- and template shortcuts.

### 2. Create competition
The user starts a new competition by entering:
- competition name,
- season or date range,
- basic description,
- and status.

### 3. Configure availability
The user defines the scheduling framework:
- active days,
- active hours,
- available fields,
- and whether availability is global or weekly.

### 4. Load template or configure manually
The user can:
- start from a blank configuration,
- or load a predefined template.

Templates should reduce repetitive work for similar competitions.

### 5. Add teams
The user registers the participating teams.

For each team, the user can:
- set the team name,
- assign category or group,
- define active status,
- configure preferences.

### 6. Define team preferences
The user sets:
- excluded day or excluded hour,
- preferred days,
- preferred hours,
- secondary options.

The interface should clearly distinguish:
- first-choice,
- second-choice,
- excluded.

### 7. Review configuration summary
Before generating the schedule, the user sees a summary of:
- number of teams,
- fields,
- active days,
- active hours,
- preference coverage,
- and detected conflicts.

### 8. Generate schedule
The user clicks generate and the system creates one or more schedule candidates.

If applicable, the user should be able to:
- generate a single result,
- or compare several alternatives.

### 9. Review calendar view
The user sees the generated plan in:
- calendar format,
- list format,
- or weekly view.

The calendar should show:
- teams,
- field,
- hour,
- day,
- status indicators.

### 10. Open audit panel
The user reviews:
- hard constraint validation,
- fairness distribution,
- preference satisfaction,
- repeated-slot warnings,
- explainability notes.

### 11. Edit schedule
If needed, the user can manually modify a match assignment.

After editing:
- validation must run again,
- fairness must be recalculated,
- audit must update.

### 12. Save or publish
The user saves the schedule as:
- draft,
- final,
- or versioned result.

## Secondary Journeys

### Quick setup flow
Used by experienced users:
1. Create competition.
2. Load template.
3. Add teams.
4. Generate schedule.
5. Review audit.
6. Publish.

### Guided setup flow
Used by first-time users:
1. Step-by-step wizard.
2. Explain each configuration group.
3. Validate at every step.
4. Generate only when everything is ready.

### Advanced editing flow
Used when the first result is close but not final:
1. Open schedule.
2. Inspect fairness.
3. Modify one or more assignments.
4. Revalidate.
5. Save new version.

## Screen List

- Home / dashboard.
- Competition list.
- New competition wizard.
- Availability editor.
- Template selector.
- Teams manager.
- Team preference editor.
- Schedule generator.
- Schedule calendar.
- Schedule list.
- Audit panel.
- Version comparison.
- Settings.

## Screen Behavior

### Dashboard
- Show the most relevant actions first.
- Allow fast access to recent work.
- Avoid exposing too many advanced controls.

### Availability editor
- Use clear toggles for days and hours.
- Highlight defaults.
- Let the user apply global or weekly rules.

### Team preference editor
- Use a simple visual selector.
- Mark excluded options clearly.
- Separate preferred and secondary options.
- Prevent contradictory input.

### Schedule calendar
- Keep the display readable.
- Use color only with a clear meaning.
- Make repeated patterns easy to spot.
- Allow switching between views.

### Audit panel
- Summarize validation results first.
- Expand details only when needed.
- Explain why a slot was assigned.
- Show fairness in a way that is understandable at a glance.

## Error and Conflict UX

The system must handle conflicts gracefully:
- Show what is invalid.
- Explain why it is invalid.
- Suggest what to change.
- Prevent finalization of invalid schedules.

## Accessibility Goals

- Clear typography.
- Strong visual hierarchy.
- Keyboard and touch support.
- Color should not be the only signal.
- Labels must be explicit and easy to understand.

## Mobile First Considerations

Because the product is multiplatform:
- critical actions should be reachable on small screens,
- calendar views must remain usable on mobile,
- advanced panels should collapse cleanly,
- forms should be short and step-based when possible.

## Empty States

The app should guide the user when nothing exists yet:
- no competitions created,
- no teams added,
- no templates loaded,
- no schedule generated yet.

Empty states should always tell the user what to do next.

## Success Criteria

The UX will be considered successful if:
- a beginner understands how to create a schedule without external help,
- an advanced user can configure complex cases quickly,
- the audit is easy to read,
- and the calendar remains clear even with dense data.