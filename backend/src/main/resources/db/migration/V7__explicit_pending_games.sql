-- V1-V6 are intentionally not included or modified by this patch.
ALTER TABLE public.fixture_game
    ADD COLUMN deferred BOOLEAN NOT NULL DEFAULT false,
    ADD COLUMN deferred_reason VARCHAR(500);
