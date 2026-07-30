-- Competition module: global scheduling defaults (days, hours, field count)

CREATE TABLE competitions (
    id UUID PRIMARY KEY,
    name VARCHAR(150) NOT NULL UNIQUE,
    description VARCHAR(1000),
    status VARCHAR(20) NOT NULL,
    default_field_count INT NOT NULL DEFAULT 1,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_competitions_status ON competitions (status);

CREATE TABLE competition_default_days (
    competition_id UUID NOT NULL REFERENCES competitions(id) ON DELETE CASCADE,
    day VARCHAR(20) NOT NULL,
    PRIMARY KEY (competition_id, day)
);

CREATE TABLE competition_default_hours (
    competition_id UUID NOT NULL REFERENCES competitions(id) ON DELETE CASCADE,
    hour VARCHAR(10) NOT NULL,
    PRIMARY KEY (competition_id, hour)
);
