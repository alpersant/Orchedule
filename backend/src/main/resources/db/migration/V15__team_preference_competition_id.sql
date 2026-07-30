-- team_preference currently has team_id as sole PK, contradicting the
-- domain model where a team has one preference PER competition.
ALTER TABLE team_preference ADD COLUMN competition_id UUID;

-- Backfill is not possible generically (no source of truth for which
-- competition existing rows belong to) — for a fresh/dev database this
-- is fine; for a populated one, backfill manually before running.
ALTER TABLE team_preference ALTER COLUMN competition_id SET NOT NULL;

ALTER TABLE team_preference DROP CONSTRAINT team_preference_pkey;
ALTER TABLE team_preference ADD PRIMARY KEY (team_id, competition_id);

-- Children tables reference team_preference by team_preference_id (FK to
-- the old single-column PK). If team_preference_day/hour use a surrogate
-- FK column (team_preference_id UUID referencing team_id only), this
-- still works IF that FK becomes (team_id, competition_id) composite.
-- Verify actual child table structure before running in a real DB;
-- this migration assumes team_preference_day/hour need the same change:
ALTER TABLE team_preference_day ADD COLUMN team_preference_competition_id UUID;
ALTER TABLE team_preference_hour ADD COLUMN team_preference_competition_id UUID;
-- NOTE: FK constraints on children must be dropped/recreated to
-- reference the composite parent key. Adjust to your actual FK names.
