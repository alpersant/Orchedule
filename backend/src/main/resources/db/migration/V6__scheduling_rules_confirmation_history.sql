-- V6: scheduling rules, individual confirmation, week alternatives and history.
-- Additive migration. Do not alter V1-V5.

ALTER TABLE public.fixture_plan
    ADD COLUMN default_min_rest_hours INT NOT NULL DEFAULT 48,
    ADD COLUMN default_max_matches_per_week INT NOT NULL DEFAULT 2;

ALTER TABLE public.fixture_plan
    ADD CONSTRAINT ck_fixture_plan_rest_hours
        CHECK (default_min_rest_hours >= 0),
    ADD CONSTRAINT ck_fixture_plan_matches_per_week
        CHECK (default_max_matches_per_week >= 1);

CREATE TABLE public.fixture_team_rule (
                                          season_id UUID NOT NULL,
                                          team_id UUID NOT NULL,
                                          min_rest_hours INT NOT NULL,
                                          max_matches_per_week INT NOT NULL,
                                          created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
                                          updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
                                          PRIMARY KEY (season_id, team_id),
                                          CONSTRAINT fk_fixture_team_rule_plan
                                              FOREIGN KEY (season_id)
                                                  REFERENCES public.fixture_plan (season_id)
                                                  ON DELETE CASCADE,
                                          CONSTRAINT fk_fixture_team_rule_team
                                              FOREIGN KEY (team_id)
                                                  REFERENCES public.team (id)
                                                  ON DELETE CASCADE,
                                          CONSTRAINT ck_fixture_team_rule_rest
                                              CHECK (min_rest_hours >= 0),
                                          CONSTRAINT ck_fixture_team_rule_week
                                              CHECK (max_matches_per_week >= 1)
);

CREATE INDEX idx_fixture_team_rule_team
    ON public.fixture_team_rule (team_id);

ALTER TABLE public.fixture_game
    ADD COLUMN confirmation_status VARCHAR(20) NOT NULL DEFAULT 'PROVISIONAL',
    ADD COLUMN confirmed_at TIMESTAMPTZ,
    ADD COLUMN confirmed_by VARCHAR(180);

ALTER TABLE public.fixture_game
    ADD CONSTRAINT ck_fixture_game_confirmation
        CHECK (confirmation_status IN ('PROVISIONAL', 'CONFIRMED'));

CREATE INDEX idx_fixture_game_confirmation
    ON public.fixture_game (confirmation_status);

CREATE TABLE public.fixture_game_alternative (
                                                 id UUID PRIMARY KEY,
                                                 game_id UUID NOT NULL,
                                                 alternative_week INT NOT NULL,
                                                 proposed_day VARCHAR(20) NOT NULL,
                                                 proposed_time TIME NOT NULL,
                                                 status VARCHAR(20) NOT NULL DEFAULT 'PROPOSED',
                                                 reason VARCHAR(500),
                                                 created_by VARCHAR(180) NOT NULL,
                                                 created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
                                                 decided_by VARCHAR(180),
                                                 decided_at TIMESTAMPTZ,
                                                 CONSTRAINT fk_fixture_game_alternative_game
                                                     FOREIGN KEY (game_id)
                                                         REFERENCES public.fixture_game (id)
                                                         ON DELETE CASCADE,
                                                 CONSTRAINT ck_fixture_game_alternative_status
                                                     CHECK (status IN ('PROPOSED', 'ACCEPTED', 'REJECTED')),
                                                 CONSTRAINT ck_fixture_game_alternative_week
                                                     CHECK (alternative_week >= 1)
);

CREATE INDEX idx_fixture_game_alternative_game
    ON public.fixture_game_alternative (game_id);

CREATE INDEX idx_fixture_game_alternative_status
    ON public.fixture_game_alternative (status);

ALTER TABLE public.fixture_audit
    ADD COLUMN fixture_game_id UUID,
    ADD COLUMN operation VARCHAR(100),
    ADD COLUMN result VARCHAR(20),
    ADD COLUMN previous_value JSONB,
    ADD COLUMN new_value JSONB,
    ADD COLUMN detail TEXT;

ALTER TABLE public.fixture_audit
    ADD CONSTRAINT fk_fixture_audit_game
        FOREIGN KEY (fixture_game_id)
            REFERENCES public.fixture_game (id)
            ON DELETE SET NULL,
    ADD CONSTRAINT ck_fixture_audit_result
        CHECK (result IN ('SUCCESS', 'REJECTED', 'ERROR'));

CREATE INDEX idx_fixture_audit_game
    ON public.fixture_audit (fixture_game_id);

CREATE INDEX idx_fixture_audit_season_created
    ON public.fixture_audit (season_id, created_at DESC);