ALTER TABLE fixture_round ADD COLUMN history_provisional BOOLEAN NOT NULL DEFAULT false;
ALTER TABLE fixture_round ADD COLUMN search_limited BOOLEAN NOT NULL DEFAULT false;
CREATE TABLE fixture_game_issue (
 game_id UUID PRIMARY KEY REFERENCES fixture_game(id) ON DELETE CASCADE,
 code VARCHAR(64) NOT NULL,
 detail VARCHAR(700) NOT NULL,
 total_slots INTEGER NOT NULL CHECK(total_slots>=0),
 date_slots INTEGER NOT NULL CHECK(date_slots>=0),
 compatible_slots INTEGER NOT NULL CHECK(compatible_slots>=0),
 rotation_slots INTEGER NOT NULL CHECK(rotation_slots>=0),
 free_slots INTEGER NOT NULL CHECK(free_slots>=0),
 updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_fixture_game_resource_time ON fixture_game(field_id,scheduled_at)
 WHERE scheduled_at IS NOT NULL;
