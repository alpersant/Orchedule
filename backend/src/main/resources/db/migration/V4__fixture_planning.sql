CREATE TABLE fixture_plan (
 season_id UUID PRIMARY KEY REFERENCES season(id) ON DELETE CASCADE,
 mode VARCHAR(20) NOT NULL CHECK(mode IN ('MANUAL','SINGLE','DOUBLE')),
 zone_id VARCHAR(80) NOT NULL,
 duration_minutes INT NOT NULL CHECK(duration_minutes BETWEEN 15 AND 240),
 created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE TABLE fixture_participant (
 season_id UUID NOT NULL REFERENCES fixture_plan(season_id) ON DELETE CASCADE,
 team_id UUID NOT NULL REFERENCES team(id) ON DELETE RESTRICT,
 PRIMARY KEY(season_id,team_id)
);
CREATE TABLE fixture_round (
 id UUID PRIMARY KEY,
 season_id UUID NOT NULL REFERENCES fixture_plan(season_id) ON DELETE CASCADE,
 round_number INT NOT NULL CHECK(round_number>0),
 week_number INT NOT NULL CHECK(week_number>0),
 status VARCHAR(20) NOT NULL CHECK(status IN ('UNSCHEDULED','DRAFT','CONFIRMED','CANCELLED')),
 bye_team_id UUID REFERENCES team(id) ON DELETE RESTRICT,
 version BIGINT NOT NULL DEFAULT 0,
 UNIQUE(season_id,round_number), UNIQUE(season_id,week_number)
);
CREATE TABLE fixture_game (
 id UUID PRIMARY KEY,
 round_id UUID NOT NULL REFERENCES fixture_round(id) ON DELETE CASCADE,
 home_team_id UUID NOT NULL REFERENCES team(id) ON DELETE RESTRICT,
 away_team_id UUID NOT NULL REFERENCES team(id) ON DELETE RESTRICT,
 field_id UUID REFERENCES field(id) ON DELETE RESTRICT,
 scheduled_at TIMESTAMPTZ,
 CHECK(home_team_id<>away_team_id),
 CHECK((field_id IS NULL)=(scheduled_at IS NULL))
);
CREATE INDEX idx_fixture_game_round ON fixture_game(round_id);
CREATE INDEX idx_fixture_game_time ON fixture_game(scheduled_at) WHERE scheduled_at IS NOT NULL;
CREATE TABLE fixture_audit (
 id UUID PRIMARY KEY,
 season_id UUID NOT NULL REFERENCES fixture_plan(season_id) ON DELETE CASCADE,
 action VARCHAR(40) NOT NULL,
 actor VARCHAR(180) NOT NULL,
 created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- Legacy writers share the planner's advisory lock. Published fixtures cannot
-- be edited independently from their round through the old match endpoints.
CREATE FUNCTION guard_fixture_match_write() RETURNS trigger LANGUAGE plpgsql AS $$
BEGIN
 PERFORM pg_advisory_xact_lock(813051031);
 IF current_setting('orchedule.fixture_writer',true) IS DISTINCT FROM 'on' THEN
   IF EXISTS(SELECT 1 FROM fixture_game WHERE id=NEW.id)
      OR EXISTS(SELECT 1 FROM fixture_plan WHERE season_id=NEW.season_id) THEN
     RAISE EXCEPTION 'Use the fixture planner for this season' USING ERRCODE='23514';
   END IF;
   IF NEW.active AND NEW.status<>'CANCELLED' AND EXISTS(
      SELECT 1 FROM fixture_game g JOIN fixture_round r ON r.id=g.round_id
      JOIN fixture_plan p ON p.season_id=r.season_id JOIN field f ON f.id=g.field_id
      WHERE r.status IN ('DRAFT','CONFIRMED')
      AND (f.venue_id=NEW.venue_id OR g.home_team_id IN (NEW.home_team_id,NEW.away_team_id)
           OR g.away_team_id IN (NEW.home_team_id,NEW.away_team_id))
      AND g.scheduled_at < NEW.scheduled_at+interval '4 hours'
      AND NEW.scheduled_at < g.scheduled_at+p.duration_minutes*interval '1 minute'
   ) THEN
     RAISE EXCEPTION 'Conflict with fixture reservation' USING ERRCODE='23514';
   END IF;
 END IF;
 RETURN NEW;
END $$;
CREATE TRIGGER guard_fixture_match_write BEFORE INSERT OR UPDATE ON match_game
FOR EACH ROW EXECUTE FUNCTION guard_fixture_match_write();
CREATE FUNCTION guard_legacy_schedule_write() RETURNS trigger LANGUAGE plpgsql AS $$
BEGIN
 PERFORM pg_advisory_xact_lock(813051031);
 IF NEW.status<>'CANCELLED' AND EXISTS(SELECT 1 FROM fixture_plan WHERE season_id=NEW.season_id) THEN
   RAISE EXCEPTION 'Use the fixture planner for this season' USING ERRCODE='23514';
 END IF;
 RETURN NEW;
END $$;
CREATE TRIGGER guard_legacy_schedule_write BEFORE INSERT OR UPDATE ON schedule_round
FOR EACH ROW EXECUTE FUNCTION guard_legacy_schedule_write();
