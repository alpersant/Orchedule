-- Improves the weekly availability lookup used by scheduling.
CREATE INDEX IF NOT EXISTS idx_field_weekly_availability_season_week
    ON field_weekly_availability(season_id, week_number);

CREATE INDEX IF NOT EXISTS idx_field_weekly_availability_field_enabled
    ON field_weekly_availability(field_id, enabled);
