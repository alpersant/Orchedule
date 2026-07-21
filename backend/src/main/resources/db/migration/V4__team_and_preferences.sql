CREATE TABLE team (
                      id UUID PRIMARY KEY,
                      name VARCHAR(120) NOT NULL,
                      active BOOLEAN NOT NULL DEFAULT TRUE,
                      created_at TIMESTAMPTZ NOT NULL,
                      updated_at TIMESTAMPTZ NOT NULL,
                      CONSTRAINT uk_team_name UNIQUE (name)
);

CREATE TABLE team_preference (
                                 team_id UUID PRIMARY KEY,
                                 restriction_type VARCHAR(30) NOT NULL,
                                 excluded_day VARCHAR(20),
                                 excluded_hour VARCHAR(20),
                                 created_at TIMESTAMPTZ NOT NULL,
                                 updated_at TIMESTAMPTZ NOT NULL,
                                 CONSTRAINT fk_team_preference_team FOREIGN KEY (team_id) REFERENCES team(id) ON DELETE CASCADE,
                                 CONSTRAINT chk_team_preference_exclusion CHECK (
                                     (restriction_type = 'EXCLUDED_DAY' AND excluded_day IS NOT NULL AND excluded_hour IS NULL)
                                         OR
                                     (restriction_type = 'EXCLUDED_HOUR' AND excluded_hour IS NOT NULL AND excluded_day IS NULL)
                                     )
);

CREATE TABLE team_preference_day (
                                     team_preference_id UUID NOT NULL,
                                     day VARCHAR(20) NOT NULL,
                                     priority VARCHAR(20) NOT NULL,
                                     PRIMARY KEY (team_preference_id, day),
                                     CONSTRAINT fk_team_preference_day_parent FOREIGN KEY (team_preference_id) REFERENCES team_preference(team_id) ON DELETE CASCADE
);

CREATE TABLE team_preference_hour (
                                      team_preference_id UUID NOT NULL,
                                      hour VARCHAR(20) NOT NULL,
                                      priority VARCHAR(20) NOT NULL,
                                      PRIMARY KEY (team_preference_id, hour),
                                      CONSTRAINT fk_team_preference_hour_parent FOREIGN KEY (team_preference_id) REFERENCES team_preference(team_id) ON DELETE CASCADE
);

CREATE INDEX idx_team_active ON team(active);
CREATE INDEX idx_team_preference_day_priority ON team_preference_day(priority);
CREATE INDEX idx_team_preference_hour_priority ON team_preference_hour(priority);