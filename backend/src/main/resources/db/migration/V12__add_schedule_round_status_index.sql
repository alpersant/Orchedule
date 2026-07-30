-- Supports ScheduleRoundRepository.findBySeasonIdAndStatus, used by
-- TeamPreferencesChangedListener on every team preference change.
-- Without this index, that query falls back to a full scan of
-- schedule_rounds filtered by season_id (idx_schedule_rounds_season_id
-- already exists) plus an in-memory filter on status — acceptable at
-- small scale, but this composite index keeps it efficient as seasons
-- and round counts grow.

CREATE INDEX idx_schedule_rounds_season_status ON schedule_rounds (season_id, status);
