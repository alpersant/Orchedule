-- =========================================================
-- Season-to-competition relationship
-- The season table is empty when this migration is applied.
-- =========================================================

ALTER TABLE season
    ADD COLUMN competition_id UUID NOT NULL;

ALTER TABLE season
    ADD CONSTRAINT fk_season_competition
        FOREIGN KEY (competition_id)
            REFERENCES competition (id)
            ON DELETE CASCADE;

CREATE INDEX idx_season_competition_id
    ON season (competition_id);

-- =========================================================
-- Unique season name within each competition
-- =========================================================

ALTER TABLE season
    DROP CONSTRAINT uk_season_name;

ALTER TABLE season
    ADD CONSTRAINT uk_season_competition_name
        UNIQUE (competition_id, name);

CREATE UNIQUE INDEX uk_season_competition_name_ci
    ON season (competition_id, lower(name));

-- =========================================================
-- JPA concurrency control
-- =========================================================

ALTER TABLE season
    ADD COLUMN version BIGINT NOT NULL DEFAULT 0;

-- =========================================================
-- Delete matches when their season is deleted
-- =========================================================

ALTER TABLE match_game
    DROP CONSTRAINT fk_match_season;

ALTER TABLE match_game
    ADD CONSTRAINT fk_match_season
        FOREIGN KEY (season_id)
            REFERENCES season (id)
            ON DELETE CASCADE;