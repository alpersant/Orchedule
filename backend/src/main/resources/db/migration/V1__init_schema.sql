CREATE EXTENSION IF NOT EXISTS "pgcrypto";

CREATE TABLE competition (
                             id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                             name VARCHAR(120) NOT NULL,
                             description VARCHAR(1000),
                             start_date DATE NOT NULL,
                             end_date DATE NOT NULL,
                             status VARCHAR(20) NOT NULL,
                             created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                             updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                             CONSTRAINT ck_competition_name_not_blank CHECK (length(trim(name)) >= 3),
                             CONSTRAINT ck_competition_status CHECK (status IN ('DRAFT', 'ACTIVE', 'CLOSED')),
                             CONSTRAINT ck_competition_dates CHECK (start_date <= end_date)
);

CREATE TABLE competition_default_day (
                                         competition_id UUID NOT NULL,
                                         day_of_week SMALLINT NOT NULL,
                                         PRIMARY KEY (competition_id, day_of_week),
                                         CONSTRAINT fk_competition_default_day_competition
                                             FOREIGN KEY (competition_id) REFERENCES competition(id) ON DELETE CASCADE,
                                         CONSTRAINT ck_competition_default_day_of_week
                                             CHECK (day_of_week BETWEEN 1 AND 7)
);

CREATE TABLE competition_week (
                                  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                  competition_id UUID NOT NULL,
                                  week_number INTEGER NOT NULL,
                                  start_date DATE NOT NULL,
                                  end_date DATE NOT NULL,
                                  uses_default_availability BOOLEAN NOT NULL DEFAULT TRUE,
                                  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                                  updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                                  CONSTRAINT fk_competition_week_competition
                                      FOREIGN KEY (competition_id) REFERENCES competition(id) ON DELETE CASCADE,
                                  CONSTRAINT uq_competition_week_number UNIQUE (competition_id, week_number),
                                  CONSTRAINT ck_competition_week_number_positive CHECK (week_number > 0),
                                  CONSTRAINT ck_competition_week_dates CHECK (start_date <= end_date)
);

CREATE TABLE field (
                       id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                       competition_id UUID NOT NULL,
                       name VARCHAR(120) NOT NULL,
                       location VARCHAR(255),
                       capacity INTEGER,
                       active BOOLEAN NOT NULL DEFAULT TRUE,
                       created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                       updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                       CONSTRAINT fk_field_competition
                           FOREIGN KEY (competition_id) REFERENCES competition(id) ON DELETE CASCADE,
                       CONSTRAINT uq_field_name_per_competition UNIQUE (competition_id, name),
                       CONSTRAINT ck_field_name_not_blank CHECK (length(trim(name)) >= 2),
                       CONSTRAINT ck_field_capacity_positive CHECK (capacity IS NULL OR capacity > 0)
);

CREATE TABLE team (
                      id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                      competition_id UUID NOT NULL,
                      name VARCHAR(120) NOT NULL,
                      category VARCHAR(80),
                      active BOOLEAN NOT NULL DEFAULT TRUE,
                      created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                      updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                      CONSTRAINT fk_team_competition
                          FOREIGN KEY (competition_id) REFERENCES competition(id) ON DELETE CASCADE,
                      CONSTRAINT uq_team_name_per_competition UNIQUE (competition_id, name),
                      CONSTRAINT ck_team_name_not_blank CHECK (length(trim(name)) >= 2)
);

CREATE TABLE week_day_availability (
                                       id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                       competition_week_id UUID NOT NULL,
                                       day_of_week SMALLINT NOT NULL,
                                       enabled BOOLEAN NOT NULL DEFAULT TRUE,
                                       CONSTRAINT fk_week_day_availability_week
                                           FOREIGN KEY (competition_week_id) REFERENCES competition_week(id) ON DELETE CASCADE,
                                       CONSTRAINT uq_week_day_availability UNIQUE (competition_week_id, day_of_week),
                                       CONSTRAINT ck_week_day_of_week CHECK (day_of_week BETWEEN 1 AND 7)
);

CREATE TABLE week_field_availability (
                                         id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                         competition_week_id UUID NOT NULL,
                                         field_id UUID NOT NULL,
                                         enabled BOOLEAN NOT NULL DEFAULT TRUE,
                                         CONSTRAINT fk_week_field_availability_week
                                             FOREIGN KEY (competition_week_id) REFERENCES competition_week(id) ON DELETE CASCADE,
                                         CONSTRAINT fk_week_field_availability_field
                                             FOREIGN KEY (field_id) REFERENCES field(id) ON DELETE RESTRICT,
                                         CONSTRAINT uq_week_field_availability UNIQUE (competition_week_id, field_id)
);

CREATE TABLE week_hour_slot (
                                id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                competition_week_id UUID NOT NULL,
                                day_of_week SMALLINT NOT NULL,
                                start_time TIME NOT NULL,
                                enabled BOOLEAN NOT NULL DEFAULT TRUE,
                                CONSTRAINT fk_week_hour_slot_week
                                    FOREIGN KEY (competition_week_id) REFERENCES competition_week(id) ON DELETE CASCADE,
                                CONSTRAINT uq_week_hour_slot UNIQUE (competition_week_id, day_of_week, start_time),
                                CONSTRAINT ck_week_hour_slot_day_of_week CHECK (day_of_week BETWEEN 1 AND 7),
                                CONSTRAINT ck_week_hour_slot_time_range CHECK (start_time >= TIME '09:00' AND start_time <= TIME '22:00')
);

CREATE INDEX idx_competition_status ON competition(status);
CREATE INDEX idx_competition_week_competition_id ON competition_week(competition_id);
CREATE INDEX idx_field_competition_id ON field(competition_id);
CREATE INDEX idx_team_competition_id ON team(competition_id);
CREATE INDEX idx_week_day_availability_week_id ON week_day_availability(competition_week_id);
CREATE INDEX idx_week_field_availability_week_id ON week_field_availability(competition_week_id);
CREATE INDEX idx_week_field_availability_field_id ON week_field_availability(field_id);
CREATE INDEX idx_week_hour_slot_week_id ON week_hour_slot(competition_week_id);
CREATE INDEX idx_week_hour_slot_day_time ON week_hour_slot(competition_week_id, day_of_week, start_time);