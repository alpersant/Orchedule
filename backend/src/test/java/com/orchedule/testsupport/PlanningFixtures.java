package com.orchedule.testsupport;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.sql.Timestamp;
import java.sql.Types;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Map;
import java.util.UUID;

/** Datos de prueba mínimos y válidos para el esquema real de Orchedule. */
public final class PlanningFixtures {

    public static final ZoneId ZONE = ZoneId.of("Europe/Madrid");

    private final NamedParameterJdbcTemplate db;

    public PlanningFixtures(NamedParameterJdbcTemplate db) {
        this.db = db;
    }

    public UUID competition() {
        UUID id = UUID.randomUUID();
        db.update("""
                INSERT INTO competition(id,name,description,status,default_field_count,preference_policy,created_at,updated_at)
                VALUES(:id,:name,NULL,'ACTIVE',1,'ONE_RESTRICTION',now(),now())
                """, Map.of("id", id, "name", "Liga " + id));
        return id;
    }

    public UUID season(UUID competition, LocalDate start, LocalDate end) {
        UUID id = UUID.randomUUID();
        db.update("""
                INSERT INTO season(id,name,start_date,end_date,status,active,created_at,updated_at,competition_id)
                VALUES(:id,:name,:startDate,:endDate,'ACTIVE',true,now(),now(),:competition)
                """, Map.of("id", id, "name", "Temporada " + id, "startDate", start,
                "endDate", end, "competition", competition));
        return id;
    }

    public void plan(UUID season) {
        db.update("""
                INSERT INTO fixture_plan(season_id,mode,zone_id,duration_minutes)
                VALUES(:season,'DOUBLE','Europe/Madrid',60)
                """, Map.of("season", season));
    }

    public UUID team(String label, boolean active) {
        UUID id = UUID.randomUUID();
        db.update("""
                INSERT INTO team(id,name,active,created_at,updated_at)
                VALUES(:id,:name,:active,now(),now())
                """, Map.of("id", id, "name", label + "-" + id, "active", active));
        return id;
    }

    public void preference(UUID team, UUID competition, String excludedDay, String excludedHour) {
        db.update("""
                INSERT INTO team_preference(team_id,competition_id,restriction_type,excluded_day,excluded_hour,created_at,updated_at)
                VALUES(:team,:competition,'ONE_RESTRICTION',:day,:hour,now(),now())
                """, new MapSqlParameterSource()
                .addValue("team", team)
                .addValue("competition", competition)
                .addValue("day", excludedDay, Types.VARCHAR)
                .addValue("hour", excludedHour, Types.VARCHAR));
    }

    public void dayPreference(UUID team, UUID competition, DayOfWeek day, String priority) {
        db.update("""
                INSERT INTO team_preference_day(team_id,competition_id,day,priority)
                VALUES(:team,:competition,:day,:priority)
                """, Map.of("team", team, "competition", competition, "day", day.name(), "priority", priority));
    }

    public void hourPreference(UUID team, UUID competition, String hour, String priority) {
        db.update("""
                INSERT INTO team_preference_hour(team_id,competition_id,hour,priority)
                VALUES(:team,:competition,:hour,:priority)
                """, Map.of("team", team, "competition", competition, "hour", hour, "priority", priority));
    }

    /** Equipo activo que acepta martes/miércoles a las 19:00 y 20:00 como primera opción. */
    public UUID fullyAvailableTeam(UUID competition, String label) {
        UUID team = team(label, true);
        preference(team, competition, null, null);
        dayPreference(team, competition, DayOfWeek.TUESDAY, "FIRST");
        dayPreference(team, competition, DayOfWeek.WEDNESDAY, "FIRST");
        hourPreference(team, competition, "19:00", "FIRST");
        hourPreference(team, competition, "H20_00", "FIRST");
        return team;
    }

    public UUID venue() {
        UUID id = UUID.randomUUID();
        db.update("""
                INSERT INTO venue(id,name,city,address,capacity,status,active,created_at,updated_at)
                VALUES(:id,:name,'Madrid',NULL,NULL,'ACTIVE',true,now(),now())
                """, Map.of("id", id, "name", "Sede " + id));
        return id;
    }

    public UUID field(UUID venue) {
        UUID id = UUID.randomUUID();
        db.update("""
                INSERT INTO field(id,venue_id,name,status,open_days,open_hours,created_at,updated_at)
                VALUES(:id,:venue,:name,'ACTIVE','TUESDAY,WEDNESDAY','19:00,20:00',now(),now())
                """, Map.of("id", id, "venue", venue, "name", "Campo " + id));
        return id;
    }

    public UUID round(UUID season, int number, int week, String status) {
        UUID id = UUID.randomUUID();
        db.update("""
                INSERT INTO fixture_round(id,season_id,round_number,week_number,status)
                VALUES(:id,:season,:number,:week,:status)
                """, Map.of("id", id, "season", season, "number", number, "week", week, "status", status));
        return id;
    }

    public UUID game(UUID round, UUID home, UUID away) {
        UUID id = UUID.randomUUID();
        db.update("""
                INSERT INTO fixture_game(id,round_id,home_team_id,away_team_id)
                VALUES(:id,:round,:home,:away)
                """, Map.of("id", id, "round", round, "home", home, "away", away));
        return id;
    }

    public UUID scheduledGame(UUID round, UUID home, UUID away, UUID field, Instant at) {
        UUID id = UUID.randomUUID();
        db.update("""
                INSERT INTO fixture_game(id,round_id,home_team_id,away_team_id,field_id,scheduled_at)
                VALUES(:id,:round,:home,:away,:field,:at)
                """, Map.of("id", id, "round", round, "home", home, "away", away,
                "field", field, "at", Timestamp.from(at)));
        return id;
    }

    public void blockRule(String scope, UUID reference, Instant start, Instant end, String status, boolean active) {
        db.update("""
                INSERT INTO availability_rule(id,scope,reference_id,start_at,end_at,status,reason,active,created_at,updated_at)
                VALUES(:id,:scope,:reference,:start,:end,:status,NULL,:active,now(),now())
                """, new MapSqlParameterSource()
                .addValue("id", UUID.randomUUID())
                .addValue("scope", scope)
                .addValue("reference", reference)
                .addValue("start", Timestamp.from(start))
                .addValue("end", Timestamp.from(end))
                .addValue("status", status)
                .addValue("active", active));
    }

    public long roundVersion(UUID round) {
        Long version = db.queryForObject("SELECT version FROM fixture_round WHERE id=:id",
                Map.of("id", round), Long.class);
        return version == null ? 0L : version;
    }
}
