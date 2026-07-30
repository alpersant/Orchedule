-- Field module: physical fields per venue and their weekly availability

CREATE TABLE field (
    id UUID PRIMARY KEY,
    venue_id UUID NOT NULL REFERENCES venue(id),
    name VARCHAR(100) NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT uq_field_venue_name UNIQUE (venue_id, name)
);

CREATE INDEX idx_field_venue_id ON field (venue_id);
CREATE INDEX idx_field_status ON field (status);

CREATE TABLE field_weekly_availability (
    id UUID PRIMARY KEY,
    season_id UUID NOT NULL REFERENCES season(id),
    week_number INT NOT NULL,
    field_id UUID NOT NULL REFERENCES field(id),
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT uq_field_weekly_availability UNIQUE (season_id, week_number, field_id)
);

CREATE INDEX idx_field_weekly_availability_season_week
    ON field_weekly_availability (season_id, week_number);
