# MVP Definition

## Product Goal

Orchedule is a cross-platform sports scheduling application that helps organizers assign match times and fields in a fair and optimized way.

The MVP focuses on the core scheduling problem: generating practical competition schedules when field availability is limited and team preferences must be respected as much as possible.

## Problem to Solve

Sports competitions often need to be scheduled with:
- Limited fields.
- Limited time slots.
- Weekly changes in availability.
- Multiple teams with different preferences.
- A requirement for fair rotation across the season.

Manual scheduling is slow, hard to balance, and often perceived as unfair. The MVP should reduce that effort and produce schedules that are easier to trust and validate.

## MVP Scope

### In scope
- Create a competition.
- Register teams.
- Register fields.
- Define available days and hours.
- Capture team preferences.
- Generate a schedule.
- Review the generated result.
- Detect basic conflicts and constraint violations.
- Show a calendar view of the schedule.
- Allow manual edits to generated assignments.
- Use templates to speed up setup.

### Out of scope for now
- Multi-organization management.
- Advanced analytics dashboards.
- AI-assisted recommendations.
- Import/export integrations.
- Public sharing links.
- Complex role and permission management.
- Full template marketplace.

## MVP Success Criteria

The MVP will be considered successful if it can:
- Generate a valid schedule from real competition data.
- Respect preferences whenever possible.
- Rotate undesirable slots fairly over time.
- Be understandable by non-technical users.
- Allow experts to review and adjust the output.

## Main User Flow

1. Create a competition.
2. Add teams.
3. Add fields.
4. Define available days and hours.
5. Set team preferences.
6. Generate the schedule.
7. Review the result.
8. Edit if needed.
9. Save or publish the final version.

## Product Risks

- Too many configuration options too early.
- Scheduling rules becoming too complex for the first version.
- Users not understanding why a slot was assigned.
- Manual changes breaking fairness.
- Poor UX for first-time users.

## MVP Guiding Principles

- Keep the first version focused.
- Prefer understandable rules over hidden complexity.
- Make fairness visible.
- Separate hard constraints from soft preferences.
- Design for non-technical users first.