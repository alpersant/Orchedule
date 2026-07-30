ALTER TABLE competition
    ADD COLUMN preference_policy VARCHAR(30) NOT NULL DEFAULT 'FLEXIBLE';

ALTER TABLE competition
    ADD CONSTRAINT ck_competition_preference_policy
        CHECK (preference_policy IN ('FLEXIBLE', 'STRICT_SINGLE_ELIMINATION'));
