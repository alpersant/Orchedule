CREATE TABLE match_game (
    id UUID NOT NULL,
    season_id UUID NOT NULL,
    home_team_id UUID NOT NULL,
    away_team_id UUID NOT NULL,
    venue_id UUID NOT NULL,
    scheduled_at TIMESTAMP WITH TIME ZONE NOT NULL,
    status VARCHAR(20) NOT NULL,
    round_number INTEGER,
    notes VARCHAR(500),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT pk_match_game PRIMARY KEY (id),
    CONSTRAINT chk_match_status CHECK (status IN ('SCHEDULED', 'CONFIRMED', 'PLAYED', 'CANCELLED')),
    CONSTRAINT chk_match_teams CHECK (home_team_id <> away_team_id),
    CONSTRAINT chk_match_round CHECK (round_number IS NULL OR round_number >= 1)
);

CREATE UNIQUE INDEX uk_match_season_teams_schedule ON match_game(season_id, home_team_id, away_team_id, scheduled_at);
CREATE INDEX idx_match_season_id ON match_game(season_id);
CREATE INDEX idx_match_status ON match_game(status);
CREATE INDEX idx_match_scheduled_at ON match_game(scheduled_at);
