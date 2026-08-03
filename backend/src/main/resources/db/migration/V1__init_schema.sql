-- =========================================================
-- V1__init_schema.sql
-- =========================================================

CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- =========================================================
-- Identity
-- =========================================================

CREATE TABLE app_user (
                          id UUID PRIMARY KEY,
                          email VARCHAR(180) NOT NULL UNIQUE,
                          password_hash VARCHAR(255) NOT NULL,
                          full_name VARCHAR(150) NOT NULL,
                          role VARCHAR(30) NOT NULL,
                          active BOOLEAN NOT NULL DEFAULT TRUE,
                          email_verified BOOLEAN NOT NULL DEFAULT FALSE,
                          created_at TIMESTAMPTZ NOT NULL,
                          updated_at TIMESTAMPTZ NOT NULL,
                          CONSTRAINT ck_app_user_role CHECK (role IN ('USER', 'ADMIN', 'ORGANIZER'))
);

CREATE INDEX idx_app_user_email ON app_user(email);

CREATE TABLE refresh_token (
                               id UUID PRIMARY KEY,
                               user_id UUID NOT NULL,
                               token_hash VARCHAR(255) NOT NULL,
                               expires_at TIMESTAMPTZ NOT NULL,
                               revoked BOOLEAN NOT NULL DEFAULT FALSE,
                               created_at TIMESTAMPTZ NOT NULL,
                               updated_at TIMESTAMPTZ NOT NULL,
                               CONSTRAINT fk_refresh_token_user
                                   FOREIGN KEY (user_id) REFERENCES app_user(id) ON DELETE CASCADE
);

CREATE INDEX idx_refresh_token_user_id ON refresh_token(user_id);
CREATE INDEX idx_refresh_token_token_hash ON refresh_token(token_hash);
CREATE INDEX idx_refresh_token_expires_at ON refresh_token(expires_at);

-- =========================================================
-- Competition
-- =========================================================

CREATE TABLE competition (
                             id UUID PRIMARY KEY,
                             name VARCHAR(150) NOT NULL UNIQUE,
                             description VARCHAR(1000),
                             status VARCHAR(20) NOT NULL,
                             default_field_count INT NOT NULL,
                             preference_policy VARCHAR(20) NOT NULL,
                             created_at TIMESTAMPTZ NOT NULL,
                             updated_at TIMESTAMPTZ NOT NULL,
                             CONSTRAINT ck_competition_status CHECK (status IN ('DRAFT', 'ACTIVE', 'CLOSED')),
                             CONSTRAINT ck_competition_default_field_count CHECK (default_field_count > 0)
);

CREATE TABLE competition_default_days (
                                          competition_id UUID NOT NULL,
                                          day VARCHAR(20) NOT NULL,
                                          PRIMARY KEY (competition_id, day),
                                          CONSTRAINT fk_competition_default_days_competition
                                              FOREIGN KEY (competition_id) REFERENCES competition(id) ON DELETE CASCADE
);

CREATE TABLE competition_default_hours (
                                           competition_id UUID NOT NULL,
                                           hour VARCHAR(10) NOT NULL,
                                           PRIMARY KEY (competition_id, hour),
                                           CONSTRAINT fk_competition_default_hours_competition
                                               FOREIGN KEY (competition_id) REFERENCES competition(id) ON DELETE CASCADE
);

-- =========================================================
-- Season
-- =========================================================

CREATE TABLE season (
                        id UUID PRIMARY KEY,
                        name VARCHAR(120) NOT NULL,
                        start_date DATE NOT NULL,
                        end_date DATE NOT NULL,
                        status VARCHAR(20) NOT NULL,
                        active BOOLEAN NOT NULL,
                        created_at TIMESTAMPTZ NOT NULL,
                        updated_at TIMESTAMPTZ NOT NULL,
                        CONSTRAINT uk_season_name UNIQUE (name),
                        CONSTRAINT chk_season_dates CHECK (end_date >= start_date)
);

CREATE INDEX idx_season_status ON season(status);
CREATE INDEX idx_season_active ON season(active);
CREATE INDEX idx_season_start_date ON season(start_date);
CREATE INDEX idx_season_end_date ON season(end_date);

-- =========================================================
-- Team
-- =========================================================

CREATE TABLE team (
                      id UUID PRIMARY KEY,
                      name VARCHAR(120) NOT NULL UNIQUE,
                      active BOOLEAN NOT NULL DEFAULT TRUE,
                      created_at TIMESTAMPTZ NOT NULL,
                      updated_at TIMESTAMPTZ NOT NULL
);

-- =========================================================
-- Team preference
-- =========================================================

CREATE TABLE team_preference (
                                 team_id UUID NOT NULL,
                                 competition_id UUID NOT NULL,
                                 restriction_type VARCHAR(30) NOT NULL,
                                 excluded_day VARCHAR(20),
                                 excluded_hour VARCHAR(20),
                                 created_at TIMESTAMPTZ NOT NULL,
                                 updated_at TIMESTAMPTZ NOT NULL,
                                 PRIMARY KEY (team_id, competition_id),
                                 CONSTRAINT fk_team_preference_team
                                     FOREIGN KEY (team_id) REFERENCES team(id) ON DELETE CASCADE,
                                 CONSTRAINT fk_team_preference_competition
                                     FOREIGN KEY (competition_id) REFERENCES competition(id) ON DELETE CASCADE
);

CREATE TABLE team_preference_day (
                                     team_id UUID NOT NULL,
                                     competition_id UUID NOT NULL,
                                     day VARCHAR(20) NOT NULL,
                                     priority VARCHAR(20) NOT NULL,
                                     PRIMARY KEY (team_id, competition_id, day),
                                     CONSTRAINT fk_team_preference_day_parent
                                         FOREIGN KEY (team_id, competition_id)
                                             REFERENCES team_preference(team_id, competition_id)
                                             ON DELETE CASCADE
);

CREATE TABLE team_preference_hour (
                                      team_id UUID NOT NULL,
                                      competition_id UUID NOT NULL,
                                      hour VARCHAR(20) NOT NULL,
                                      priority VARCHAR(20) NOT NULL,
                                      PRIMARY KEY (team_id, competition_id, hour),
                                      CONSTRAINT fk_team_preference_hour_parent
                                          FOREIGN KEY (team_id, competition_id)
                                              REFERENCES team_preference(team_id, competition_id)
                                              ON DELETE CASCADE
);

-- =========================================================
-- Venue
-- =========================================================

CREATE TABLE venue (
                       id UUID PRIMARY KEY,
                       name VARCHAR(120) NOT NULL,
                       city VARCHAR(100) NOT NULL,
                       address VARCHAR(255),
                       capacity INTEGER,
                       status VARCHAR(20) NOT NULL,
                       active BOOLEAN NOT NULL,
                       created_at TIMESTAMPTZ NOT NULL,
                       updated_at TIMESTAMPTZ NOT NULL,
                       CONSTRAINT uk_venue_name UNIQUE (name)
);

CREATE INDEX idx_venue_status ON venue(status);
CREATE INDEX idx_venue_active ON venue(active);
CREATE INDEX idx_venue_city ON venue(city);

-- =========================================================
-- Field
-- =========================================================

CREATE TABLE field (
                       id UUID PRIMARY KEY,
                       venue_id UUID NOT NULL,
                       name VARCHAR(100) NOT NULL,
                       status VARCHAR(20) NOT NULL,
                       open_days VARCHAR(100) NOT NULL,
                       open_hours VARCHAR(100) NOT NULL,
                       created_at TIMESTAMPTZ NOT NULL,
                       updated_at TIMESTAMPTZ NOT NULL,
                       CONSTRAINT fk_field_venue
                           FOREIGN KEY (venue_id) REFERENCES venue(id) ON DELETE CASCADE,
                       CONSTRAINT uq_field_venue_name UNIQUE (venue_id, name)
);

CREATE TABLE field_weekly_availability (
                                           id UUID PRIMARY KEY,
                                           season_id UUID NOT NULL,
                                           week_number INT NOT NULL,
                                           field_id UUID NOT NULL,
                                           enabled BOOLEAN NOT NULL,
                                           CONSTRAINT fk_field_weekly_availability_season
                                               FOREIGN KEY (season_id) REFERENCES season(id) ON DELETE CASCADE,
                                           CONSTRAINT fk_field_weekly_availability_field
                                               FOREIGN KEY (field_id) REFERENCES field(id) ON DELETE CASCADE,
                                           CONSTRAINT uk_field_weekly_availability UNIQUE (season_id, week_number, field_id)
);

-- =========================================================
-- Availability
-- =========================================================

CREATE TABLE availability_rule (
                                   id UUID PRIMARY KEY,
                                   scope VARCHAR(20) NOT NULL,
                                   reference_id UUID NOT NULL,
                                   start_at TIMESTAMPTZ NOT NULL,
                                   end_at TIMESTAMPTZ NOT NULL,
                                   status VARCHAR(20) NOT NULL,
                                   reason VARCHAR(255),
                                   active BOOLEAN NOT NULL,
                                   created_at TIMESTAMPTZ NOT NULL,
                                   updated_at TIMESTAMPTZ NOT NULL,
                                   CONSTRAINT chk_availability_period CHECK (end_at > start_at)
);

CREATE INDEX idx_availability_scope_reference ON availability_rule(scope, reference_id);
CREATE INDEX idx_availability_start_at ON availability_rule(start_at);
CREATE INDEX idx_availability_end_at ON availability_rule(end_at);
CREATE INDEX idx_availability_active ON availability_rule(active);

-- =========================================================
-- Match
-- =========================================================

CREATE TABLE match_game (
                            id UUID PRIMARY KEY,
                            season_id UUID NOT NULL,
                            home_team_id UUID NOT NULL,
                            away_team_id UUID NOT NULL,
                            venue_id UUID NOT NULL,
                            scheduled_at TIMESTAMPTZ NOT NULL,
                            status VARCHAR(20) NOT NULL,
                            round_number INTEGER,
                            notes VARCHAR(500),
                            active BOOLEAN NOT NULL,
                            created_at TIMESTAMPTZ NOT NULL,
                            updated_at TIMESTAMPTZ NOT NULL,
                            CONSTRAINT fk_match_season
                                FOREIGN KEY (season_id) REFERENCES season(id) ON DELETE RESTRICT,
                            CONSTRAINT fk_match_home_team
                                FOREIGN KEY (home_team_id) REFERENCES team(id) ON DELETE RESTRICT,
                            CONSTRAINT fk_match_away_team
                                FOREIGN KEY (away_team_id) REFERENCES team(id) ON DELETE RESTRICT,
                            CONSTRAINT fk_match_venue
                                FOREIGN KEY (venue_id) REFERENCES venue(id) ON DELETE RESTRICT,
                            CONSTRAINT chk_match_teams CHECK (home_team_id <> away_team_id),
                            CONSTRAINT uk_match_season_teams_schedule
                                UNIQUE (season_id, home_team_id, away_team_id, scheduled_at)
);

CREATE INDEX idx_match_season_id ON match_game(season_id);
CREATE INDEX idx_match_status ON match_game(status);
CREATE INDEX idx_match_scheduled_at ON match_game(scheduled_at);

-- =========================================================
-- Scheduling
-- =========================================================

CREATE TABLE schedule_round (
                                id UUID PRIMARY KEY,
                                season_id UUID NOT NULL,
                                week_number INT NOT NULL,
                                status VARCHAR(20) NOT NULL,
                                created_at TIMESTAMPTZ NOT NULL,
                                updated_at TIMESTAMPTZ NOT NULL,
                                CONSTRAINT fk_schedule_round_season
                                    FOREIGN KEY (season_id) REFERENCES season(id) ON DELETE CASCADE,
                                CONSTRAINT uq_schedule_round_season_week UNIQUE (season_id, week_number)
);

CREATE INDEX idx_schedule_round_season_id ON schedule_round(season_id);
CREATE INDEX idx_schedule_round_season_status ON schedule_round(season_id, status);

CREATE TABLE schedule_team_assignment (
                                          id UUID PRIMARY KEY,
                                          round_id UUID NOT NULL,
                                          team_id UUID NOT NULL,
                                          day VARCHAR(15) NOT NULL,
                                          hour TIME NOT NULL,
                                          field_id UUID NOT NULL,
                                          CONSTRAINT fk_schedule_assignment_round
                                              FOREIGN KEY (round_id) REFERENCES schedule_round(id) ON DELETE CASCADE
);

CREATE INDEX idx_schedule_assignment_round_id ON schedule_team_assignment(round_id);
CREATE INDEX idx_schedule_assignment_team_id ON schedule_team_assignment(team_id);
CREATE INDEX idx_schedule_assignment_field_id ON schedule_team_assignment(field_id);

CREATE TABLE team_rotation_history (
                                       team_id UUID PRIMARY KEY,
                                       recent_days VARCHAR(100),
                                       recent_hours VARCHAR(100)
);

-- =========================================================
-- Spring Modulith - Event publication registry (JDBC-based)
-- =========================================================

CREATE TABLE event_publication (
                                   id UUID NOT NULL,
                                   listener_id TEXT NOT NULL,
                                   event_type TEXT NOT NULL,
                                   serialized_event TEXT NOT NULL,
                                   publication_date TIMESTAMPTZ NOT NULL,
                                   completion_date TIMESTAMPTZ,
                                   PRIMARY KEY (id)
);

CREATE INDEX event_publication_completion_idx
    ON event_publication(completion_date);
