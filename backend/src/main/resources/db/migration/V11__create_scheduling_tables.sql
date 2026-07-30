-- Scheduling module: generated rounds and per-team assignments

CREATE TABLE schedule_rounds (
    id UUID PRIMARY KEY,
    season_id UUID NOT NULL REFERENCES seasons(id),
    week_number INT NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT uq_schedule_round_season_week UNIQUE (season_id, week_number)
);

CREATE INDEX idx_schedule_rounds_season_id ON schedule_rounds (season_id);

CREATE TABLE schedule_team_assignments (
    id UUID PRIMARY KEY,
    round_id UUID NOT NULL REFERENCES schedule_rounds(id) ON DELETE CASCADE,
    team_id UUID NOT NULL,
    day VARCHAR(15) NOT NULL,
    hour TIME NOT NULL,
    field_id UUID NOT NULL
);

CREATE INDEX idx_schedule_assignments_round_id ON schedule_team_assignments (round_id);
CREATE INDEX idx_schedule_assignments_team_id ON schedule_team_assignments (team_id);

CREATE TABLE team_rotation_history (
    team_id UUID PRIMARY KEY,
    recent_days VARCHAR(100),
    recent_hours VARCHAR(100)
);
