-- Scheduling module: generated rounds and per-team assignments

CREATE TABLE schedule_round (
    id UUID PRIMARY KEY,
    season_id UUID NOT NULL REFERENCES season(id),
    week_number INT NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT uq_schedule_round_season_week UNIQUE (season_id, week_number)
);

CREATE INDEX idx_schedule_round_season_id ON schedule_round (season_id);

CREATE TABLE schedule_team_assignment (
    id UUID PRIMARY KEY,
    round_id UUID NOT NULL REFERENCES schedule_round(id) ON DELETE CASCADE,
    team_id UUID NOT NULL,
    day VARCHAR(15) NOT NULL,
    hour TIME NOT NULL,
    field_id UUID NOT NULL
);

CREATE INDEX idx_schedule_assignment_round_id ON schedule_team_assignment (round_id);
CREATE INDEX idx_schedule_assignment_team_id ON schedule_team_assignment (team_id);

CREATE TABLE team_rotation_history (
    team_id UUID PRIMARY KEY,
    recent_days VARCHAR(100),
    recent_hours VARCHAR(100)
);
