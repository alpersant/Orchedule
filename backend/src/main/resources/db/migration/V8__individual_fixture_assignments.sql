-- Keep V1-V7 unchanged. A proposal without field is stored separately from a reservation.
ALTER TABLE public.fixture_game
    ADD COLUMN proposed_at TIMESTAMPTZ,
    ADD COLUMN manual_assignment BOOLEAN NOT NULL DEFAULT false;
ALTER TABLE public.fixture_game
    ADD CONSTRAINT ck_fixture_game_proposal CHECK (
        proposed_at IS NULL OR (scheduled_at IS NULL AND field_id IS NULL)
    );
CREATE INDEX idx_fixture_game_proposed_at ON public.fixture_game(proposed_at)
    WHERE proposed_at IS NOT NULL;
-- Preserve the published status of existing rounds.
UPDATE public.fixture_game g SET confirmation_status='CONFIRMED',
    confirmed_at=COALESCE(g.confirmed_at,now()),confirmed_by=COALESCE(g.confirmed_by,'migration-v8')
FROM public.fixture_round r WHERE r.id=g.round_id AND r.status='CONFIRMED';

CREATE FUNCTION public.guard_confirmed_fixture_assignment() RETURNS trigger
LANGUAGE plpgsql AS $$
BEGIN
    PERFORM pg_advisory_xact_lock(813051031);
    IF OLD.confirmation_status='CONFIRMED' AND
       ROW(OLD.round_id,OLD.home_team_id,OLD.away_team_id,OLD.field_id,OLD.scheduled_at,OLD.proposed_at)
       IS DISTINCT FROM
       ROW(NEW.round_id,NEW.home_team_id,NEW.away_team_id,NEW.field_id,NEW.scheduled_at,NEW.proposed_at)
    THEN
        RAISE EXCEPTION 'Desconfirma el partido antes de cambiar su asignacion' USING ERRCODE='23514';
    END IF;
    RETURN NEW;
END;
$$;
CREATE TRIGGER guard_confirmed_fixture_assignment BEFORE UPDATE ON public.fixture_game
FOR EACH ROW EXECUTE FUNCTION public.guard_confirmed_fixture_assignment();
