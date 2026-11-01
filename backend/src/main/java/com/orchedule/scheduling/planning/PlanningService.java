package com.orchedule.scheduling.planning;

import com.orchedule.scheduling.application.port.*;
import com.orchedule.scheduling.domain.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import java.sql.Timestamp;
import java.time.*;
import java.util.*;

/** PostgreSQL application adapter. All mutations are atomic and serialized with legacy match writers. */
@Service
public class PlanningService {
    private final NamedParameterJdbcTemplate db;
    private final SeasonContextPort seasons;
    private final TeamPreferencesPort preferences;
    private final FieldAvailabilityPort fields;
    public PlanningService(NamedParameterJdbcTemplate db,SeasonContextPort seasons,
        @Qualifier("schedulingTeamPreferencesAdapter") TeamPreferencesPort preferences,FieldAvailabilityPort fields){
        this.db=db;this.seasons=seasons;this.preferences=preferences;this.fields=fields;
    }
    private static ResponseStatusException conflict(String message){return new ResponseStatusException(HttpStatus.CONFLICT,message);}
    private Map<String,Object> one(String sql,Map<String,?> p){
        var result=db.queryForList(sql,p);
        if(result.isEmpty()) throw new ResponseStatusException(HttpStatus.NOT_FOUND,"No se encuentra el recurso");
        return result.getFirst();
    }
    private void lock(UUID season){
        db.getJdbcTemplate().execute("SELECT pg_advisory_xact_lock(813051031)");
        one("SELECT id FROM season WHERE id=:s FOR UPDATE",Map.of("s",season));
    }
    private void audit(UUID season,String action){
        String actor=SecurityContextHolder.getContext().getAuthentication().getName();
        db.update("INSERT INTO fixture_audit(id,season_id,action,actor) VALUES(:id,:s,:a,:u)",
            Map.of("id",UUID.randomUUID(),"s",season,"a",action,"u",actor));
    }
    @Transactional(readOnly=true)
    public Map<String,Object> read(UUID season){
        var p=Map.of("s",season);
        var seasonRow=one("SELECT id,name,start_date,end_date FROM season WHERE id=:s",p);
        Map<String,Object> result=new LinkedHashMap<>();
        result.put("canManage",SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
            .anyMatch(a->Set.of("ROLE_ADMIN","ROLE_ORGANIZER").contains(a.getAuthority())));
        result.put("seasonId",season);result.put("seasonName",seasonRow.get("name"));
        result.put("startDate",seasonRow.get("start_date").toString());result.put("endDate",seasonRow.get("end_date").toString());
        var configurations=db.queryForList("SELECT mode,zone_id,duration_minutes FROM fixture_plan WHERE season_id=:s",p);
        result.put("configuration",configurations);
        result.put("teams",db.queryForList("SELECT t.id,t.name FROM team t JOIN season s ON s.id=:s JOIN team_preference p ON p.team_id=t.id AND p.competition_id=s.competition_id WHERE t.active ORDER BY t.name,t.id",p));
        result.put("legacyRounds",db.queryForList("SELECT id,week_number,status FROM schedule_round WHERE season_id=:s ORDER BY week_number",p));
        result.put("legacyAssignments",db.queryForList("SELECT a.round_id,t.name AS team_name,a.day,a.hour,f.name AS field_name FROM schedule_team_assignment a JOIN schedule_round r ON r.id=a.round_id LEFT JOIN team t ON t.id=a.team_id LEFT JOIN field f ON f.id=a.field_id WHERE r.season_id=:s ORDER BY a.day,a.hour",p));
        result.put("rounds",db.queryForList("SELECT r.*,t.name AS bye_name FROM fixture_round r LEFT JOIN team t ON t.id=r.bye_team_id WHERE r.season_id=:s ORDER BY round_number",p));
        var games=db.queryForList("""
            SELECT g.*,h.name AS home_name,a.name AS away_name,f.name AS field_name,v.name AS venue_name,r.round_number
            FROM fixture_game g JOIN fixture_round r ON r.id=g.round_id
            JOIN team h ON h.id=g.home_team_id JOIN team a ON a.id=g.away_team_id
            LEFT JOIN field f ON f.id=g.field_id LEFT JOIN venue v ON v.id=f.venue_id
            WHERE r.season_id=:s ORDER BY r.round_number,g.scheduled_at NULLS LAST,h.name
            """,p);
        ZoneId zone=configurations.isEmpty()?ZoneOffset.UTC:ZoneId.of((String)configurations.getFirst().get("zone_id"));
        for(var game:games) if(game.get("scheduled_at") instanceof Timestamp ts){
            game.put("local_start",ts.toInstant().atZone(zone).toLocalDateTime().toString());
            game.put("scheduled_at",ts.toInstant().toString());
        }
        result.put("games",games);result.put("fairness",fairness(season,zone));
        result.put("audit",db.queryForList("SELECT action,actor,created_at FROM fixture_audit WHERE season_id=:s ORDER BY created_at DESC LIMIT 100",p));
        return result;
    }
    @Transactional
    public void configure(UUID season,PlanningApi.Configure config){
        lock(season);seasons.assertSeasonIsSchedulable(season);
        try{ZoneId.of(config.zoneId());}catch(DateTimeException e){throw new IllegalArgumentException("Zona horaria IANA inválida");}
        Set<UUID> selected=new LinkedHashSet<>(config.teamIds());
        if(selected.size()!=config.teamIds().size() || selected.size()<2) throw new IllegalArgumentException("Equipos repetidos o insuficientes");
        var eligible=preferences.getProfilesForSeason(season).stream().map(TeamScheduleProfile::getTeamId).toList();
        var active=db.queryForList("SELECT id FROM team WHERE active",Map.of()).stream().map(t->(UUID)t.get("id")).toList();
        if(!active.containsAll(selected) || !eligible.containsAll(selected)) throw new IllegalArgumentException("Hay participantes inactivos o sin preferencias para esta competición");
        if(!db.queryForList("SELECT season_id FROM fixture_plan WHERE season_id=:s",Map.of("s",season)).isEmpty())
            throw conflict("Ya existe un calendario. No se sustituyen participantes ni cruces silenciosamente.");
        List<FixtureEngine.Round> rounds;
        if(config.mode()==PlanningApi.Mode.MANUAL){
            if(config.rounds()==null || config.rounds().isEmpty()) throw new IllegalArgumentException("Introduce o importa las jornadas");
            rounds=new ArrayList<>();
            for(var input:config.rounds()){
                Set<UUID> used=new HashSet<>();List<FixtureEngine.Pair> games=new ArrayList<>();
                for(var pair:input.games()){
                    if(!selected.contains(pair.homeTeamId()) || !selected.contains(pair.awayTeamId()) ||
                        !used.add(pair.homeTeamId()) || !used.add(pair.awayTeamId()))
                        throw new IllegalArgumentException("Equipos desconocidos, duplicados o un enfrentamiento contra sí mismo");
                    games.add(new FixtureEngine.Pair(pair.homeTeamId(),pair.awayTeamId()));
                }
                Set<UUID> absent=new HashSet<>(selected);absent.removeAll(used);
                if(selected.size()%2==0){
                    if(!absent.isEmpty() || input.byeTeamId()!=null) throw new IllegalArgumentException("Con equipos pares deben jugar todos, sin descanso");
                }else if(absent.size()!=1 || !absent.contains(input.byeTeamId()))
                    throw new IllegalArgumentException("Con equipos impares debe descansar exactamente un equipo por jornada");
                rounds.add(new FixtureEngine.Round(List.copyOf(games),input.byeTeamId()));
            }
        }else{
            if(config.rounds()!=null && !config.rounds().isEmpty()) throw new IllegalArgumentException("Los cruces manuales solo se aceptan en modalidad manual");
            rounds=FixtureEngine.league(config.teamIds(),config.mode()==PlanningApi.Mode.DOUBLE);
        }
        var period=one("SELECT start_date,end_date FROM season WHERE id=:s",Map.of("s",season));
        LocalDate start=LocalDate.parse(period.get("start_date").toString()),end=LocalDate.parse(period.get("end_date").toString());
        if(FixtureEngine.monday(start,rounds.size()).isAfter(end)) throw new IllegalArgumentException("Las jornadas no caben en la temporada: amplía las fechas");
        db.update("INSERT INTO fixture_plan(season_id,mode,zone_id,duration_minutes) VALUES(:s,:m,:z,:d)",
            Map.of("s",season,"m",config.mode().name(),"z",config.zoneId(),"d",config.durationMinutes()));
        for(UUID team:selected) db.update("INSERT INTO fixture_participant VALUES(:s,:t)",Map.of("s",season,"t",team));
        for(int i=0;i<rounds.size();i++){
            var r=rounds.get(i);UUID id=UUID.randomUUID();Map<String,Object> p=new HashMap<>();
            p.put("id",id);p.put("s",season);p.put("n",i+1);p.put("bye",r.bye());
            db.update("INSERT INTO fixture_round(id,season_id,round_number,week_number,status,bye_team_id) VALUES(:id,:s,:n,:n,'UNSCHEDULED',:bye)",p);
            for(var pair:r.games()) db.update("INSERT INTO fixture_game(id,round_id,home_team_id,away_team_id) VALUES(:id,:r,:h,:a)",
                Map.of("id",UUID.randomUUID(),"r",id,"h",pair.home(),"a",pair.away()));
        }
        audit(season,"CONFIGURE_"+config.mode());
    }
    @Transactional
    public void action(UUID season,int number,String action,long version){
        lock(season);seasons.assertSeasonIsSchedulable(season);
        db.getJdbcTemplate().execute("SELECT set_config('orchedule.fixture_writer','on',true)");
        var params=Map.of("s",season,"n",number);
        var round=one("SELECT * FROM fixture_round WHERE season_id=:s AND round_number=:n FOR UPDATE",params);
        UUID id=(UUID)round.get("id");String status=(String)round.get("status");
        if(((Number)round.get("version")).longValue()!=version) throw conflict("La jornada ha cambiado. Actualiza antes de continuar.");
        if(!db.queryForList("SELECT id FROM fixture_round WHERE season_id=:s AND round_number>:n AND status='CONFIRMED'",params).isEmpty())
            throw conflict("Cancela primero las jornadas posteriores confirmadas, en orden inverso");
        if(!action.equals("cancel") && !db.queryForList("SELECT id FROM fixture_round WHERE season_id=:s AND round_number<:n AND status<>'CONFIRMED'",params).isEmpty())
            throw conflict("Confirma las jornadas anteriores para evaluar la rotación sobre el histórico real");
        var config=one("SELECT p.*,s.start_date,s.end_date FROM fixture_plan p JOIN season s ON s.id=p.season_id WHERE season_id=:s",Map.of("s",season));
        int week=((Number)round.get("week_number")).intValue();
        if(action.equals("schedule") || action.equals("regenerate")){
            if(status.equals("CONFIRMED")) throw conflict("Cancela primero la jornada confirmada");
            if(action.equals("schedule") && !status.equals("UNSCHEDULED")) throw conflict("Usa regenerar para una jornada existente");
            if(!db.queryForList("SELECT id FROM schedule_round WHERE season_id=:s AND week_number=:w AND status<>'CANCELLED'",Map.of("s",season,"w",week)).isEmpty())
                throw conflict("Hay una planificación antigua activa en esta semana. Cancélala explícitamente antes de asignar horarios a los partidos.");
            var allocation=FixtureEngine.allocate(candidates(season,id,number,week,config),((Number)config.get("duration_minutes")).intValue(),200000);
            for(var e:allocation.entrySet()) db.update("UPDATE fixture_game SET field_id=:f,scheduled_at=:at WHERE id=:id",
                Map.of("f",e.getValue().field(),"at",Timestamp.from(e.getValue().at()),"id",e.getKey()));
            status="DRAFT";
        }else if(action.equals("confirm")){
            if(!status.equals("DRAFT")) throw conflict("Solo se confirma un borrador planificado");
            var choices=candidates(season,id,number,week,config);
            for(var game:db.queryForList("SELECT * FROM fixture_game WHERE round_id=:r",Map.of("r",id))){
                if(game.get("scheduled_at")==null) throw conflict("Faltan horarios");
                Instant at=((Timestamp)game.get("scheduled_at")).toInstant();
                if(choices.get((UUID)game.get("id")).stream().noneMatch(s->s.field().equals(game.get("field_id")) && s.at().equals(at)))
                    throw conflict("La disponibilidad ha cambiado. Regenera el borrador.");
            }
            for(var game:db.queryForList("SELECT g.*,f.venue_id FROM fixture_game g JOIN field f ON f.id=g.field_id WHERE round_id=:r",Map.of("r",id))){
                Map<String,Object> p=new HashMap<>(game);p.put("s",season);p.put("n",number);
                db.update("""
                    INSERT INTO match_game(id,season_id,home_team_id,away_team_id,venue_id,scheduled_at,status,round_number,notes,active,created_at,updated_at)
                    VALUES(:id,:s,:home_team_id,:away_team_id,:venue_id,:scheduled_at,'CONFIRMED',:n,'Fixture planner',true,now(),now())
                    ON CONFLICT(id) DO UPDATE SET venue_id=EXCLUDED.venue_id,scheduled_at=EXCLUDED.scheduled_at,status='CONFIRMED',active=true,updated_at=now()
                    """,p);
            }
            status="CONFIRMED";
        }else if(action.equals("cancel")){
            if(status.equals("CANCELLED")) throw conflict("La jornada ya está cancelada");
            db.update("UPDATE match_game SET status='CANCELLED',active=false,updated_at=now() WHERE id IN(SELECT id FROM fixture_game WHERE round_id=:r)",Map.of("r",id));
            status="CANCELLED";
        }else throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Acción desconocida");
        db.update("UPDATE fixture_round SET status=:st,version=version+1 WHERE id=:id",Map.of("st",status,"id",id));
        audit(season,action.toUpperCase()+"_"+number);
    }
    private Map<UUID,List<FixtureEngine.Slot>> candidates(UUID season,UUID round,int number,int week,Map<String,Object> config){
        ZoneId zone=ZoneId.of((String)config.get("zone_id"));int duration=((Number)config.get("duration_minutes")).intValue();
        LocalDate start=LocalDate.parse(config.get("start_date").toString()),end=LocalDate.parse(config.get("end_date").toString());
        LocalDate monday=FixtureEngine.monday(start,week);
        Map<UUID,TeamScheduleProfile> profiles=new HashMap<>();
        preferences.getProfilesForSeason(season).forEach(p->profiles.put(p.getTeamId(),p));
        var slots=fields.getSchedulingContext(seasons.getCompetitionIdForSeason(season),season,week).slots();
        var games=db.queryForList("SELECT * FROM fixture_game WHERE round_id=:r ORDER BY id",Map.of("r",round));
        Map<String,Object> range=Map.of("r",round,"from",Timestamp.from(monday.minusDays(1).atStartOfDay(zone).toInstant()),"to",Timestamp.from(monday.plusWeeks(1).atStartOfDay(zone).toInstant()));
        var bookings=db.queryForList("""
            SELECT g.field_id,g.scheduled_at,g.home_team_id,g.away_team_id,p.duration_minutes
            FROM fixture_game g JOIN fixture_round r ON r.id=g.round_id JOIN fixture_plan p ON p.season_id=r.season_id
            WHERE r.status IN ('DRAFT','CONFIRMED') AND r.id<>:r AND g.scheduled_at>=:from AND g.scheduled_at<:to
            """,range);
        var external=db.queryForList("""
            SELECT m.* FROM match_game m WHERE m.active AND m.status<>'CANCELLED'
            AND NOT EXISTS(SELECT 1 FROM fixture_game fg WHERE fg.id=m.id)
            AND m.scheduled_at>=:from AND m.scheduled_at<:to
            """,range);
        Map<UUID,UUID> venues=new HashMap<>();
        db.queryForList("SELECT id,venue_id FROM field",Map.of()).forEach(f->venues.put((UUID)f.get("id"),(UUID)f.get("venue_id")));
        var exclusions=db.queryForList("SELECT p.team_id,p.excluded_day,p.excluded_hour FROM team_preference p JOIN season s ON s.competition_id=p.competition_id WHERE s.id=:s",Map.of("s",season));
        var blocks=db.queryForList("SELECT scope,reference_id,start_at,end_at FROM availability_rule WHERE active AND status='BLOCKED' AND start_at<:to AND end_at>:from",range);
        Map<UUID,List<Map<String,Object>>> history=new HashMap<>();
        db.queryForList("""
            SELECT g.home_team_id,g.away_team_id,g.scheduled_at,r.round_number
            FROM fixture_game g JOIN fixture_round r ON r.id=g.round_id
            WHERE r.season_id=:s AND r.status='CONFIRMED' AND r.round_number<:n ORDER BY r.round_number DESC
            """,Map.of("s",season,"n",number)).forEach(h->{
                for(String key:List.of("home_team_id","away_team_id")) history.computeIfAbsent((UUID)h.get(key),k->new ArrayList<>()).add(h);
            });
        Map<UUID,List<FixtureEngine.Slot>> result=new LinkedHashMap<>();
        for(var game:games){
            UUID home=(UUID)game.get("home_team_id"),away=(UUID)game.get("away_team_id");
            TeamScheduleProfile hp=profiles.get(home),ap=profiles.get(away);
            if(hp==null || ap==null) throw new IllegalArgumentException("Faltan preferencias de un participante");
            List<FixtureEngine.Slot> possible=new ArrayList<>();
            for(var slot:slots){
                LocalDate date=monday.plusDays(slot.day().getValue()-1L);
                if(date.isBefore(start) || date.isAfter(end)) continue;
                LocalDateTime local=date.atTime(slot.hour());var offsets=zone.getRules().getValidOffsets(local);
                if(offsets.size()!=1) continue;
                Instant at=local.toInstant(offsets.getFirst());
                if(at.isBefore(Instant.now()) || at.plusSeconds(duration*60L).isAfter(date.plusDays(1).atStartOfDay(zone).toInstant())) continue;
                boolean covered=true;
                for(int minute=0;minute<duration;minute+=60){
                    LocalTime needed=slot.hour().plusMinutes(minute);
                    if(slots.stream().noneMatch(s->s.fieldId().equals(slot.fieldId()) && s.day()==slot.day() && s.hour().equals(needed))){covered=false;break;}
                }
                if(!covered || !allowed(hp,slot.day(),slot.hour()) || !allowed(ap,slot.day(),slot.hour())) continue;
                if(overused(history.get(home),slot.day(),slot.hour(),zone) || overused(history.get(away),slot.day(),slot.hour(),zone)) continue;
                boolean blocked=false;
                for(var ex:exclusions) if(List.of(home,away).contains(ex.get("team_id"))){
                    String hour=Objects.toString(ex.get("excluded_hour"),"").replace("H","").replace("_",":");
                    if(slot.day().name().equals(ex.get("excluded_day")) || slot.hour().toString().equals(hour)){blocked=true;break;}
                }
                if(blocked) continue;
                for(var rule:blocks){
                    boolean affected=("TEAM".equals(rule.get("scope")) && List.of(home,away).contains(rule.get("reference_id"))) ||
                        ("VENUE".equals(rule.get("scope")) && Objects.equals(venues.get(slot.fieldId()),rule.get("reference_id")));
                    if(affected && at.isBefore(((Timestamp)rule.get("end_at")).toInstant()) && ((Timestamp)rule.get("start_at")).toInstant().isBefore(at.plusSeconds(duration*60L))){blocked=true;break;}
                }
                if(blocked) continue;
                for(var booking:bookings){
                    boolean resource=slot.fieldId().equals(booking.get("field_id")) || List.of(home,away).contains(booking.get("home_team_id")) || List.of(home,away).contains(booking.get("away_team_id"));
                    if(resource && FixtureEngine.overlaps(at,duration,((Timestamp)booking.get("scheduled_at")).toInstant(),((Number)booking.get("duration_minutes")).intValue())){blocked=true;break;}
                }
                if(blocked) continue;
                for(var ext:external){
                    boolean resource=Objects.equals(venues.get(slot.fieldId()),ext.get("venue_id")) || List.of(home,away).contains(ext.get("home_team_id")) || List.of(home,away).contains(ext.get("away_team_id"));
                    if(resource && FixtureEngine.overlaps(at,duration,((Timestamp)ext.get("scheduled_at")).toInstant(),240)){blocked=true;break;}
                }
                if(blocked) continue;
                int penalty=penalty(hp,slot.day(),slot.hour())+penalty(ap,slot.day(),slot.hour());
                for(UUID team:List.of(home,away)) for(var h:history.getOrDefault(team,List.of())){
                    var old=((Timestamp)h.get("scheduled_at")).toInstant().atZone(zone);
                    if(old.getDayOfWeek()==slot.day()) penalty+=2;
                    if(old.toLocalTime().equals(slot.hour())) penalty+=2;
                }
                possible.add(new FixtureEngine.Slot(slot.fieldId(),at,penalty));
            }
            if(possible.isEmpty()) throw new IllegalArgumentException("Sin franja compatible para "+home+" contra "+away+". Revisa exclusiones, rotación, campos y fechas.");
            result.put((UUID)game.get("id"),possible);
        }
        return result;
    }
    private static boolean allowed(TeamScheduleProfile p,DayOfWeek day,LocalTime hour){
        return p.getDayOptions().stream().anyMatch(o->o.day()==day) && p.getHourOptions().stream().anyMatch(o->o.hour().equals(hour));
    }
    private static int penalty(TeamScheduleProfile p,DayOfWeek day,LocalTime hour){
        return (p.daysWithPriority(SlotPriority.FIRST).contains(day)?0:10)+(p.hoursWithPriority(SlotPriority.FIRST).contains(hour)?0:10);
    }
    private static boolean overused(List<Map<String,Object>> history,DayOfWeek day,LocalTime hour,ZoneId zone){
        if(history==null || history.size()<2) return false;
        var a=((Timestamp)history.get(0).get("scheduled_at")).toInstant().atZone(zone);
        var b=((Timestamp)history.get(1).get("scheduled_at")).toInstant().atZone(zone);
        return (a.getDayOfWeek()==day && b.getDayOfWeek()==day) || (a.toLocalTime().equals(hour) && b.toLocalTime().equals(hour));
    }
    private List<Map<String,Object>> fairness(UUID season,ZoneId zone){
        Map<UUID,Map<String,Object>> report=new LinkedHashMap<>();
        Map<UUID,DayOfWeek> lastDay=new HashMap<>();Map<UUID,LocalTime> lastHour=new HashMap<>();
        Map<UUID,Integer> dayRun=new HashMap<>(),hourRun=new HashMap<>();
        var entries=db.queryForList("""
            SELECT g.home_team_id,g.away_team_id,g.scheduled_at,h.name AS home_name,a.name AS away_name
            FROM fixture_game g JOIN fixture_round r ON r.id=g.round_id
            JOIN team h ON h.id=g.home_team_id JOIN team a ON a.id=g.away_team_id
            WHERE r.season_id=:s AND r.status='CONFIRMED' ORDER BY r.round_number
            """,Map.of("s",season));
        for(var game:entries){
            var at=((Timestamp)game.get("scheduled_at")).toInstant().atZone(zone);
            for(String side:List.of("home","away")){
                UUID id=(UUID)game.get(side+"_team_id");
                var row=report.computeIfAbsent(id,k->{
                    Map<String,Object> value=new LinkedHashMap<>();value.put("teamName",game.get(side+"_name"));
                    value.put("games",0);value.put("maxDayStreak",0);value.put("maxHourStreak",0);
                    value.put("days",new TreeMap<String,Integer>());value.put("hours",new TreeMap<String,Integer>());return value;
                });
                row.put("games",((Number)row.get("games")).intValue()+1);
                @SuppressWarnings("unchecked") var days=(Map<String,Integer>)row.get("days");
                @SuppressWarnings("unchecked") var hours=(Map<String,Integer>)row.get("hours");
                days.merge(at.getDayOfWeek().name(),1,Integer::sum);hours.merge(at.toLocalTime().toString(),1,Integer::sum);
                int dr=lastDay.get(id)==at.getDayOfWeek()?dayRun.getOrDefault(id,0)+1:1;
                int hr=Objects.equals(lastHour.get(id),at.toLocalTime())?hourRun.getOrDefault(id,0)+1:1;
                dayRun.put(id,dr);hourRun.put(id,hr);lastDay.put(id,at.getDayOfWeek());lastHour.put(id,at.toLocalTime());
                row.put("maxDayStreak",Math.max(dr,((Number)row.get("maxDayStreak")).intValue()));
                row.put("maxHourStreak",Math.max(hr,((Number)row.get("maxHourStreak")).intValue()));
            }
        }
        return new ArrayList<>(report.values());
    }
}
