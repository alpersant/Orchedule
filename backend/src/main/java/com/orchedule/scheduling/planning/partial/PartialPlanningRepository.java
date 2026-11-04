package com.orchedule.scheduling.planning.partial;
import org.springframework.jdbc.core.namedparam.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import java.sql.Timestamp;
import java.time.*;
import java.util.*;
import static com.orchedule.scheduling.planning.partial.PlanningData.*;

@Repository
public class PartialPlanningRepository {
    private final NamedParameterJdbcTemplate db;
    public PartialPlanningRepository(NamedParameterJdbcTemplate db){this.db=db;}
    private Map<String,Object> one(String sql,Map<String,?> parameters){
        var rows=db.queryForList(sql,parameters);
        if(rows.isEmpty())throw new ResponseStatusException(HttpStatus.NOT_FOUND,"No se encuentra la temporada o jornada");
        return rows.getFirst();
    }
    static Instant instant(Object value){
        if(value==null)return null;
        if(value instanceof Timestamp t)return t.toInstant();
        if(value instanceof OffsetDateTime t)return t.toInstant();
        if(value instanceof Instant t)return t;
        throw new IllegalStateException("Formato de fecha SQL no compatible");
    }
    public void lock(UUID season){
        if(!TransactionSynchronizationManager.isActualTransactionActive())throw new IllegalStateException("La planificación necesita transacción");
        db.getJdbcTemplate().execute("SELECT pg_advisory_xact_lock(813051031)");
        one("SELECT id FROM season WHERE id=:s FOR UPDATE",Map.of("s",season));
    }
    public Round round(UUID season,int number){
        var r=one("""
            SELECT r.*,s.start_date,s.end_date,p.zone_id,p.duration_minutes
            FROM fixture_round r JOIN season s ON s.id=r.season_id JOIN fixture_plan p ON p.season_id=r.season_id
            WHERE r.season_id=:s AND r.round_number=:n FOR UPDATE OF r
            """,Map.of("s",season,"n",number));
        return new Round((UUID)r.get("id"),season,number,com.orchedule.shared.jdbc.JdbcValues.requiredInt(r, "week_number"),com.orchedule.shared.jdbc.JdbcValues.requiredLong(r, "version"),
            (String)r.get("status"),LocalDate.parse(r.get("start_date").toString()),LocalDate.parse(r.get("end_date").toString()),ZoneId.of((String)r.get("zone_id")),com.orchedule.shared.jdbc.JdbcValues.requiredInt(r, "duration_minutes"));
    }
    public List<Game> games(Round round){
        return db.queryForList("SELECT g.*,h.name AS home_name,a.name AS away_name FROM fixture_game g JOIN team h ON h.id=g.home_team_id JOIN team a ON a.id=g.away_team_id WHERE round_id=:r ORDER BY g.id",Map.of("r",round.id())).stream()
            .map(g->new Game((UUID)g.get("id"),(UUID)g.get("home_team_id"),(UUID)g.get("away_team_id"),(String)g.get("home_name"),(String)g.get("away_name"),(UUID)g.get("field_id"),instant(g.get("scheduled_at")))).toList();
    }
    public boolean earlierIncomplete(Round r){return Boolean.TRUE.equals(db.queryForObject("SELECT EXISTS(SELECT 1 FROM fixture_round WHERE season_id=:s AND round_number<:n AND status<>'CONFIRMED')",Map.of("s",r.season(),"n",r.number()),Boolean.class));}
    public boolean laterConfirmed(Round r){return Boolean.TRUE.equals(db.queryForObject("SELECT EXISTS(SELECT 1 FROM fixture_round WHERE season_id=:s AND round_number>:n AND status='CONFIRMED')",Map.of("s",r.season(),"n",r.number()),Boolean.class));}
    public boolean activeLegacy(Round r){return Boolean.TRUE.equals(db.queryForObject("SELECT EXISTS(SELECT 1 FROM schedule_round WHERE season_id=:s AND week_number=:w AND status<>'CANCELLED')",Map.of("s",r.season(),"w",r.week()),Boolean.class));}
    public Map<UUID,UUID> venues(Set<UUID> ids){
        Map<UUID,UUID> result=new HashMap<>();if(ids.isEmpty())return result;db.queryForList("SELECT id,venue_id FROM field WHERE id IN (:ids)",Map.of("ids",ids)).forEach(row->result.put((UUID)row.get("id"),(UUID)row.get("venue_id")));return result;
    }
    public void lockFieldInputs(Round r){
        db.queryForList("SELECT f.id FROM field f JOIN field_weekly_availability w ON w.field_id=f.id WHERE w.season_id=:s AND w.week_number=:w AND w.enabled FOR SHARE OF f",Map.of("s",r.season(),"w",r.week()));
    }
    public Map<UUID,Preferences> preferences(UUID competition,Set<UUID> teams){
        if(teams.isEmpty())return Map.of();
        var p=Map.of("c",competition,"teams",teams);
        var base=db.queryForList("SELECT p.*,t.active FROM team_preference p JOIN team t ON t.id=p.team_id WHERE p.competition_id=:c AND p.team_id IN (:teams) FOR SHARE OF p,t",p);
        var days=db.queryForList("SELECT team_id,day,priority FROM team_preference_day WHERE competition_id=:c AND team_id IN (:teams) FOR SHARE",p);
        var hours=db.queryForList("SELECT team_id,hour,priority FROM team_preference_hour WHERE competition_id=:c AND team_id IN (:teams) FOR SHARE",p);
        Map<UUID,Preferences> result=new HashMap<>();
        for(var row:base){
            UUID id=(UUID)row.get("team_id");Set<DayOfWeek> allowedDays=new HashSet<>(),firstDays=new HashSet<>();
            Set<LocalTime> allowedHours=new HashSet<>(),firstHours=new HashSet<>();boolean valid=Boolean.TRUE.equals(row.get("active"));
            try{
                for(var d:days)if(id.equals(d.get("team_id"))){
                    String priority=d.get("priority").toString();if(!Set.of("FIRST","SECOND").contains(priority))throw new IllegalArgumentException();
                    var day=DayOfWeek.valueOf(d.get("day").toString());allowedDays.add(day);if(priority.equals("FIRST"))firstDays.add(day);
                }
                for(var h:hours)if(id.equals(h.get("team_id"))){
                    String priority=h.get("priority").toString();if(!Set.of("FIRST","SECOND").contains(priority))throw new IllegalArgumentException();
                    var hour=parseHour(h.get("hour").toString());allowedHours.add(hour);if(priority.equals("FIRST"))firstHours.add(hour);
                }
                if(row.get("excluded_day")!=null){var excluded=DayOfWeek.valueOf(row.get("excluded_day").toString());allowedDays.remove(excluded);firstDays.remove(excluded);}
                if(row.get("excluded_hour")!=null){var excluded=parseHour(row.get("excluded_hour").toString());allowedHours.remove(excluded);firstHours.remove(excluded);}
            }catch(IllegalArgumentException e){valid=false;}
            valid=valid&&!allowedDays.isEmpty()&&!allowedHours.isEmpty();
            result.put(id,new Preferences(Set.copyOf(allowedDays),Set.copyOf(allowedHours),Set.copyOf(firstDays),Set.copyOf(firstHours),valid));
        }
        return result;
    }
    static LocalTime parseHour(String raw){String value=raw.replace("H","").replace('_',':');if(value.matches("\\d{4}"))value=value.substring(0,2)+":"+value.substring(2);return LocalTime.parse(value);}
    public List<History> history(Round r){
        List<History> result=new ArrayList<>();
        db.queryForList("SELECT g.home_team_id,g.away_team_id,g.scheduled_at,r.round_number FROM fixture_game g JOIN fixture_round r ON r.id=g.round_id WHERE r.season_id=:s AND r.round_number<:n AND r.status IN ('DRAFT','CONFIRMED') AND g.scheduled_at IS NOT NULL ORDER BY r.round_number DESC",Map.of("s",r.season(),"n",r.number())).forEach(h->{
            int n=com.orchedule.shared.jdbc.JdbcValues.requiredInt(h, "round_number");Instant at=instant(h.get("scheduled_at"));result.add(new History((UUID)h.get("home_team_id"),at,n));result.add(new History((UUID)h.get("away_team_id"),at,n));
        });return result;
    }
    public List<Booking> bookings(Round r,Instant from,Instant to,Set<UUID> teams,Set<UUID> fields,Set<UUID> venues){
        Map<String,Object> p=Map.of("r",r.id(),"from",Timestamp.from(from),"to",Timestamp.from(to),"teams",teams,"fields",fields,"venues",venues);List<Booking> result=new ArrayList<>();
        db.queryForList("SELECT g.home_team_id,g.away_team_id,g.field_id,COALESCE(g.scheduled_at,g.proposed_at) AS scheduled_at,f.venue_id,p.duration_minutes FROM fixture_game g JOIN fixture_round r ON r.id=g.round_id JOIN fixture_plan p ON p.season_id=r.season_id LEFT JOIN field f ON f.id=g.field_id WHERE r.status IN ('DRAFT','CONFIRMED') AND r.id<>:r AND COALESCE(g.scheduled_at,g.proposed_at)>=:from AND COALESCE(g.scheduled_at,g.proposed_at)<:to AND (g.field_id IN (:fields) OR g.home_team_id IN (:teams) OR g.away_team_id IN (:teams))",p).forEach(g->result.add(new Booking((UUID)g.get("home_team_id"),(UUID)g.get("away_team_id"),(UUID)g.get("field_id"),(UUID)g.get("venue_id"),instant(g.get("scheduled_at")),com.orchedule.shared.jdbc.JdbcValues.requiredInt(g, "duration_minutes"))));
        // Legacy matches expose venue but not field/duration: preserve the conservative compatibility policy.
        db.queryForList("SELECT m.* FROM match_game m WHERE m.active AND m.status<>'CANCELLED' AND NOT EXISTS(SELECT 1 FROM fixture_game fg WHERE fg.id=m.id) AND m.scheduled_at>=:from AND m.scheduled_at<:to AND (m.venue_id IN (:venues) OR m.home_team_id IN (:teams) OR m.away_team_id IN (:teams))",p).forEach(g->result.add(new Booking((UUID)g.get("home_team_id"),(UUID)g.get("away_team_id"),null,(UUID)g.get("venue_id"),instant(g.get("scheduled_at")),240)));
        return result;
    }
    public List<Block> blocks(Instant from,Instant to){
        return db.queryForList("SELECT scope,reference_id,start_at,end_at FROM availability_rule WHERE active AND status='BLOCKED' AND start_at<:to AND end_at>:from",Map.of("from",Timestamp.from(from),"to",Timestamp.from(to))).stream()
            .map(b->new Block((String)b.get("scope"),(UUID)b.get("reference_id"),instant(b.get("start_at")),instant(b.get("end_at")))).toList();
    }
    public void saveDraft(Round r,List<Game> games,Map<UUID,PartialPlanningEngine.Slot> assigned,Map<UUID,Issue> issues,boolean provisional,boolean limited){
        List<SqlParameterSource> updates=new ArrayList<>();
        for(Game g:games){var slot=assigned.get(g.id());updates.add(new MapSqlParameterSource().addValue("id",g.id()).addValue("r",r.id()).addValue("f",slot==null?null:slot.field()).addValue("at",slot==null?null:Timestamp.from(slot.at())));}
        db.batchUpdate("UPDATE fixture_game SET field_id=:f,scheduled_at=:at WHERE id=:id AND round_id=:r AND NOT deferred AND NOT manual_assignment AND confirmation_status<>'CONFIRMED'",updates.toArray(SqlParameterSource[]::new));
        db.update("DELETE FROM fixture_game_issue WHERE game_id IN(SELECT id FROM fixture_game WHERE round_id=:r AND NOT deferred AND NOT manual_assignment)",Map.of("r",r.id()));
        List<SqlParameterSource> pending=new ArrayList<>();
        issues.forEach((id,issue)->{Counts c=issue.counts();pending.add(new MapSqlParameterSource().addValue("id",id).addValue("code",issue.code()).addValue("detail",issue.detail()).addValue("total",c.total()).addValue("dates",c.dates()).addValue("compatible",c.compatible()).addValue("rotation",c.rotation()).addValue("free",c.free()));});
        if(!pending.isEmpty())db.batchUpdate("INSERT INTO fixture_game_issue(game_id,code,detail,total_slots,date_slots,compatible_slots,rotation_slots,free_slots) VALUES(:id,:code,:detail,:total,:dates,:compatible,:rotation,:free)",pending.toArray(SqlParameterSource[]::new));
        updateStatus(r,"DRAFT",provisional,limited);
    }
    public void updateStatus(Round r,String status,boolean provisional,boolean limited){
        int updated=db.update("UPDATE fixture_round SET status=:status,version=version+1,history_provisional=:p,search_limited=:l WHERE id=:r AND version=:v",Map.of("status",status,"p",provisional,"l",limited,"r",r.id(),"v",r.version()));
        if(updated!=1)throw new ResponseStatusException(HttpStatus.CONFLICT,"La jornada ha cambiado. Actualiza antes de continuar.");
    }
    public void publish(Round r){
        db.getJdbcTemplate().execute("SELECT set_config('orchedule.fixture_writer','on',true)");
        List<SqlParameterSource> params=new ArrayList<>();
        for(var g:db.queryForList("SELECT g.*,f.venue_id FROM fixture_game g JOIN field f ON f.id=g.field_id WHERE round_id=:r",Map.of("r",r.id()))){var p=new MapSqlParameterSource(g);p.addValue("s",r.season());p.addValue("n",r.number());params.add(p);}
        db.batchUpdate("""
            INSERT INTO match_game(id,season_id,home_team_id,away_team_id,venue_id,scheduled_at,status,round_number,notes,active,created_at,updated_at)
            VALUES(:id,:s,:home_team_id,:away_team_id,:venue_id,:scheduled_at,'CONFIRMED',:n,'Fixture planner',true,now(),now())
            ON CONFLICT(id) DO UPDATE SET venue_id=EXCLUDED.venue_id,scheduled_at=EXCLUDED.scheduled_at,status='CONFIRMED',active=true,updated_at=now()
            """,params.toArray(SqlParameterSource[]::new));
        db.update("UPDATE fixture_game SET confirmation_status='CONFIRMED',confirmed_at=COALESCE(confirmed_at,now()),confirmed_by=COALESCE(confirmed_by,:actor) WHERE round_id=:r",Map.of("r",r.id(),"actor",SecurityContextHolder.getContext().getAuthentication().getName()));
    }
    public void cancelPublished(Round r){db.getJdbcTemplate().execute("SELECT set_config('orchedule.fixture_writer','on',true)");db.update("UPDATE match_game SET status='CANCELLED',active=false,updated_at=now() WHERE id IN(SELECT id FROM fixture_game WHERE round_id=:r)",Map.of("r",r.id()));}
    public void audit(Round r,String action){
        var auth=SecurityContextHolder.getContext().getAuthentication();if(auth==null||!auth.isAuthenticated())throw new AccessDeniedException("Autenticación requerida");
        String actor=auth.getName();try{UUID.fromString(actor);}catch(IllegalArgumentException e){throw new IllegalStateException("La identidad de auditoría debe ser el UUID autenticado");}
        db.update("""
            INSERT INTO fixture_audit(id,season_id,action,actor,result,detail)
            SELECT :id,:s,:a,:actor,'SUCCESS',
                count(*) FILTER (WHERE scheduled_at IS NOT NULL AND field_id IS NOT NULL)::text
                || ' partidos con horario; ' ||
                count(*) FILTER (WHERE scheduled_at IS NULL OR field_id IS NULL)::text || ' pendientes. Estado de jornada: '
                || (SELECT status FROM fixture_round WHERE id=:r)
            FROM fixture_game WHERE round_id=:r
            """,Map.of("id",UUID.randomUUID(),"s",r.season(),"r",r.id(),"a",action+"_"+r.number(),"actor",actor));
    }
    public boolean isManual(UUID game){
        return Boolean.TRUE.equals(db.queryForObject("SELECT manual_assignment FROM fixture_game WHERE id=:g",Map.of("g",game),Boolean.class));
    }
    public List<Game> proposals(Round r){
        return db.queryForList("SELECT id,home_team_id,away_team_id,proposed_at FROM fixture_game WHERE round_id=:r AND proposed_at IS NOT NULL",Map.of("r",r.id())).stream()
            .map(g->new Game((UUID)g.get("id"),(UUID)g.get("home_team_id"),(UUID)g.get("away_team_id"),"","",null,instant(g.get("proposed_at")))).toList();
    }
    public boolean isIndividuallyConfirmed(UUID game){
        return Boolean.TRUE.equals(db.queryForObject("SELECT confirmation_status='CONFIRMED' FROM fixture_game WHERE id=:g",Map.of("g",game),Boolean.class));
    }
    public boolean isDeferred(UUID game){
        return Boolean.TRUE.equals(db.queryForObject("SELECT deferred FROM fixture_game WHERE id=:g",Map.of("g",game),Boolean.class));
    }
    public boolean hasConfirmedGames(Round r){
        return Boolean.TRUE.equals(db.queryForObject("SELECT EXISTS(SELECT 1 FROM fixture_game WHERE round_id=:r AND confirmation_status='CONFIRMED')",Map.of("r",r.id()),Boolean.class));
    }
    public List<Map<String,Object>> issues(UUID season){return db.queryForList("SELECT i.* FROM fixture_game_issue i JOIN fixture_game g ON g.id=i.game_id JOIN fixture_round r ON r.id=g.round_id WHERE r.season_id=:s",Map.of("s",season));}
}


