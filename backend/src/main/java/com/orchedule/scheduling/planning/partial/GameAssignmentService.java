package com.orchedule.scheduling.planning.partial;

import com.orchedule.scheduling.application.port.SeasonContextPort;
import com.orchedule.scheduling.planning.FixtureEngine;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.namedparam.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.sql.Timestamp;
import java.time.*;
import java.util.*;
import static com.orchedule.scheduling.planning.partial.PlanningData.*;

@Service
public class GameAssignmentService {
    private final NamedParameterJdbcTemplate db;
    private final PartialPlanningRepository repo;
    private final PartialSlotResolver resolver;
    private final SeasonContextPort seasons;
    public GameAssignmentService(NamedParameterJdbcTemplate db,PartialPlanningRepository repo,
                                 PartialSlotResolver resolver,SeasonContextPort seasons){
        this.db=db;this.repo=repo;this.resolver=resolver;this.seasons=seasons;
    }
    private static ResponseStatusException conflict(String s){return new ResponseStatusException(HttpStatus.CONFLICT,s);}
    private static Instant instant(Object value){return PartialPlanningRepository.instant(value);}
    private record State(Round round,Game game,Map<String,Object> row){}

    private State load(UUID season,UUID id,Long version){
        repo.lock(season);seasons.assertSeasonIsSchedulable(season);
        var rows=db.queryForList("""
            SELECT g.*,r.round_number,r.status AS round_status
            FROM fixture_game g JOIN fixture_round r ON r.id=g.round_id
            WHERE g.id=:g AND r.season_id=:s FOR UPDATE OF g,r
            """,Map.of("g",id,"s",season));
        if(rows.isEmpty())throw new ResponseStatusException(HttpStatus.NOT_FOUND,"No se encuentra el partido en esta temporada");
        var row=rows.getFirst();
        Round r=repo.round(season,com.orchedule.shared.jdbc.JdbcValues.requiredInt(row,"round_number"));
        if(version!=null&&r.version()!=version)throw conflict("La jornada ha cambiado. Actualiza antes de guardar.");
        if(r.status().equals("CANCELLED"))throw conflict("Prepara de nuevo la jornada cancelada antes de asignar partidos.");
        Game g=repo.games(r).stream().filter(x->x.id().equals(id)).findFirst().orElseThrow();
        return new State(r,g,row);
    }
    private UUID seasonFor(UUID id){
        var rows=db.queryForList("SELECT r.season_id FROM fixture_game g JOIN fixture_round r ON r.id=g.round_id WHERE g.id=:g",Map.of("g",id));
        if(rows.isEmpty())throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Partido no encontrado");
        return (UUID)rows.getFirst().get("season_id");
    }
    private static void editable(State s){
        if("CONFIRMED".equals(s.row().get("confirmation_status"))||s.round().status().equals("CONFIRMED"))
            throw conflict("Desconfirma el partido antes de modificarlo.");
    }
    private Instant localInstant(State s,String local){
        final LocalDateTime value;
        try{value=LocalDateTime.parse(local);}catch(DateTimeException|NullPointerException e){
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,"Introduce una fecha y hora local válida.");
        }
        var offsets=s.round().zone().getRules().getValidOffsets(value);
        if(offsets.size()!=1)throw conflict("Hora ambigua o inexistente por cambio de hora. Elige otra franja.");
        return value.toInstant(offsets.getFirst());
    }
    private static boolean team(Game g,Map<String,Object> row){
        return Set.of(g.home(),g.away()).contains(row.get("home_team_id"))||Set.of(g.home(),g.away()).contains(row.get("away_team_id"));
    }

    // Validation for manual assignments, including reservations without a field.
    private void validate(State s,Instant at,UUID field){
        Round r=s.round();Game g=s.game();ZonedDateTime local=at.atZone(r.zone());
        LocalDate monday=FixtureEngine.monday(r.start(),r.week());
        if(at.isBefore(Instant.now())||local.toLocalDate().isBefore(r.start())||local.toLocalDate().isAfter(r.end())
           ||local.toLocalDate().isBefore(monday)||!local.toLocalDate().isBefore(monday.plusWeeks(1)))
            throw conflict("La fecha debe ser futura y estar dentro de la semana deportiva y las fechas de temporada. Las alternativas entre semanas se añadirán en otro bloque.");
        if(local.getSecond()!=0||local.getNano()!=0||local.toLocalTime().plusMinutes(r.duration()).isBefore(local.toLocalTime())
           ||!at.plusSeconds(r.duration()*60L).atZone(r.zone()).toLocalDate().equals(local.toLocalDate()))
            throw conflict("La duración debe caber en el mismo día y la hora debe indicarse con precisión de minutos.");
        var profiles=repo.preferences(seasons.getCompetitionIdForSeason(r.season()),Set.of(g.home(),g.away()));
        for(UUID id:List.of(g.home(),g.away())){
            Preferences p=profiles.get(id);
            if(p==null||!p.valid()||!p.days().contains(local.getDayOfWeek())||!p.hours().contains(local.toLocalTime()))
                throw conflict("La fecha y hora no respetan las preferencias o exclusiones de ambos equipos.");
        }
        var others=db.queryForList("""
            SELECT g.id,g.home_team_id,g.away_team_id,g.field_id,
                COALESCE(g.scheduled_at,g.proposed_at) AS at,p.duration_minutes,r.round_number,r.season_id
            FROM fixture_game g JOIN fixture_round r ON r.id=g.round_id
            JOIN fixture_plan p ON p.season_id=r.season_id
            WHERE g.id<>:g AND r.status<>'CANCELLED'
              AND COALESCE(g.scheduled_at,g.proposed_at) IS NOT NULL
              AND (g.home_team_id IN (:teams) OR g.away_team_id IN (:teams) OR g.field_id=:field)
            """,new MapSqlParameterSource().addValue("g",g.id()).addValue("teams",List.of(g.home(),g.away())).addValue("field",field,java.sql.Types.OTHER));
        others=new ArrayList<>(others);
        others.addAll(db.queryForList("""
            SELECT m.id,m.home_team_id,m.away_team_id,NULL::uuid AS field_id,m.scheduled_at AS at,
                   240 AS duration_minutes,NULL::integer AS round_number,NULL::uuid AS season_id
            FROM match_game m WHERE m.active AND m.status<>'CANCELLED'
            AND NOT EXISTS(SELECT 1 FROM fixture_game f WHERE f.id=m.id)
            AND (m.home_team_id IN (:teams) OR m.away_team_id IN (:teams))
            """,Map.of("teams",List.of(g.home(),g.away()))));
        Instant from=monday.atStartOfDay(r.zone()).toInstant(),to=monday.plusWeeks(1).atStartOfDay(r.zone()).toInstant();
        for(UUID id:List.of(g.home(),g.away())){
            var rule=db.queryForMap("""
                SELECT COALESCE(t.min_rest_hours,p.default_min_rest_hours) AS rest,
                       COALESCE(t.max_matches_per_week,p.default_max_matches_per_week) AS maximum
                FROM fixture_plan p LEFT JOIN fixture_team_rule t ON t.season_id=p.season_id AND t.team_id=:t
                WHERE p.season_id=:s
                """,Map.of("s",r.season(),"t",id));
            int rest=com.orchedule.shared.jdbc.JdbcValues.requiredInt(rule,"rest");
            int maximum=com.orchedule.shared.jdbc.JdbcValues.requiredInt(rule,"maximum");
            long weekly=0;
            List<Map.Entry<Integer,ZonedDateTime>> rotation=new ArrayList<>();
            for(var other:others){
                if(!id.equals(other.get("home_team_id"))&&!id.equals(other.get("away_team_id")))continue;
                Instant otherAt=instant(other.get("at"));
                int duration=com.orchedule.shared.jdbc.JdbcValues.requiredInt(other,"duration_minutes");
                Duration gap=otherAt.isBefore(at)?Duration.between(otherAt.plusSeconds(duration*60L),at):Duration.between(at.plusSeconds(r.duration()*60L),otherAt);
                if(gap.compareTo(Duration.ofHours(rest))<0)throw conflict("Descanso insuficiente para "+(id.equals(g.home())?g.homeName():g.awayName())+": se requieren "+rest+" horas desde el final del partido anterior.");
                if(!otherAt.isBefore(from)&&otherAt.isBefore(to))weekly++;
                if(r.season().equals(other.get("season_id")))rotation.add(Map.entry(com.orchedule.shared.jdbc.JdbcValues.requiredInt(other,"round_number"),otherAt.atZone(r.zone())));
            }
            if(weekly>=maximum)throw conflict("Se supera el máximo de "+maximum+" partidos por semana para uno de los equipos.");
            rotation.add(Map.entry(r.number(),local));rotation.sort(Map.Entry.comparingByKey());
            for(int i=0;i+2<rotation.size();i++){
                var triple=rotation.subList(i,i+3);
                if(triple.stream().noneMatch(e->e.getKey()==r.number()))continue;
                var first=triple.getFirst().getValue();
                if(triple.stream().allMatch(e->e.getValue().getDayOfWeek()==first.getDayOfWeek())
                    ||triple.stream().allMatch(e->e.getValue().toLocalTime().equals(first.toLocalTime())))
                    throw conflict("La propuesta repite el mismo día u hora por tercera jornada consecutiva para un equipo.");
            }
        }
        for(var other:others){
            boolean affected=team(g,other)||field!=null&&field.equals(other.get("field_id"));
            if(affected&&FixtureEngine.overlaps(at,r.duration(),instant(other.get("at")),com.orchedule.shared.jdbc.JdbcValues.requiredInt(other,"duration_minutes")))
                throw conflict("La propuesta coincide con otra reserva de equipo o campo.");
        }
        for(Block block:repo.blocks(at,at.plusSeconds(r.duration()*60L))){
            if(block.scope().equals("TEAM")&&Set.of(g.home(),g.away()).contains(block.reference()))throw conflict("Un equipo está bloqueado en esa franja.");
        }
        // Legacy reservations: preserve conservative 240-minute occupancy used by the existing planner.
        var legacy=db.queryForList("""
            SELECT m.* FROM match_game m WHERE m.active AND m.status<>'CANCELLED'
            AND NOT EXISTS(SELECT 1 FROM fixture_game f WHERE f.id=m.id)
            AND (m.home_team_id IN (:teams) OR m.away_team_id IN (:teams))
            """,Map.of("teams",List.of(g.home(),g.away())));
        for(var other:legacy){
            Instant otherAt=instant(other.get("scheduled_at"));
            if(FixtureEngine.overlaps(at,r.duration(),otherAt,240))throw conflict("Hay una reserva anterior de un equipo en esa franja.");
        }
        if(field!=null){
            Options options=resolver.resolve(r,List.of(g));
            boolean found=options.candidates().getOrDefault(g.id(),List.of()).stream().anyMatch(x->x.field().equals(field)&&x.at().equals(at));
            if(!found)throw conflict("El campo no está disponible o la franja no supera apertura, preferencias, disponibilidad y rotación.");
        }
    }
    @Transactional(timeout=8)
    @PreAuthorize("hasAnyRole('ADMIN','ORGANIZER')")
    public void assign(UUID season,UUID id,long version,String localStart,UUID field){
        State s=load(season,id,version);editable(s);Instant at=localInstant(s,localStart);validate(s,at,field);
        var p=new MapSqlParameterSource().addValue("g",id).addValue("field",field,java.sql.Types.OTHER)
            .addValue("at",Timestamp.from(at));
        db.update(field==null?
            "UPDATE fixture_game SET proposed_at=:at,scheduled_at=NULL,field_id=NULL,manual_assignment=true,deferred=false,deferred_reason=NULL WHERE id=:g":
            "UPDATE fixture_game SET scheduled_at=:at,field_id=:field,proposed_at=NULL,manual_assignment=true,deferred=false,deferred_reason=NULL WHERE id=:g",p);
        db.update("DELETE FROM fixture_game_issue WHERE game_id=:g",Map.of("g",id));
        bump(s.round(),false);
        String fieldLabel="Campo pendiente";
        if(field!=null)fieldLabel=db.queryForObject("SELECT f.name || ' · ' || v.name FROM field f JOIN venue v ON v.id=f.venue_id WHERE f.id=:f",Map.of("f",field),String.class);
        audit(s,field==null?"PROPOSE_TIME":"ASSIGN_FIELD",field==null?"Horario provisional guardado":"Fecha y campo asignados",
            localStart+" ("+s.round().zone()+") · "+fieldLabel);
    }
    @Transactional(timeout=8)
    @PreAuthorize("hasAnyRole('ADMIN','ORGANIZER')")
    public void clear(UUID season,UUID id,long version){
        State s=load(season,id,version);editable(s);
        db.update("UPDATE fixture_game SET proposed_at=NULL,scheduled_at=NULL,field_id=NULL,manual_assignment=false WHERE id=:g",Map.of("g",id));
        bump(s.round(),false);audit(s,"CLEAR_ASSIGNMENT","Asignación retirada","El partido vuelve a estar disponible para la planificación automática.");
    }
    @Transactional(timeout=8)
    @PreAuthorize("hasAnyRole('ADMIN','ORGANIZER')")
    public List<Map<String,Object>> fields(UUID season,UUID id,long version,String localStart){
        State s=load(season,id,version);Instant at=localInstant(s,localStart);validate(s,at,null);
        Options options=resolver.resolve(s.round(),List.of(s.game()));
        Set<UUID> ids=new LinkedHashSet<>();
        for(var slot:options.candidates().getOrDefault(id,List.of()))if(slot.at().equals(at)){
            try{validate(s,at,slot.field());ids.add(slot.field());}catch(ResponseStatusException e){if(e.getStatusCode().value()!=409)throw e;}
        }
        if(ids.isEmpty())return List.of();
        return db.queryForList("SELECT f.id,f.name,v.name AS venue_name FROM field f JOIN venue v ON v.id=f.venue_id WHERE f.id IN (:ids) ORDER BY v.name,f.name",Map.of("ids",ids));
    }
    @Transactional(timeout=8)
    @PreAuthorize("hasAnyRole('ADMIN','ORGANIZER')")
    public void confirm(UUID season,UUID id,Long version){
        State s=load(season,id,version);
        if("CONFIRMED".equals(s.row().get("confirmation_status")))return;
        if(s.game().field()==null||s.game().at()==null)throw conflict("Selecciona fecha, hora y campo antes de confirmar el partido.");
        validate(s,s.game().at(),s.game().field());
        db.getJdbcTemplate().execute("SELECT set_config('orchedule.fixture_writer','on',true)");
        db.update("""
            INSERT INTO match_game(id,season_id,home_team_id,away_team_id,venue_id,scheduled_at,status,round_number,notes,active,created_at,updated_at)
            SELECT g.id,r.season_id,g.home_team_id,g.away_team_id,f.venue_id,g.scheduled_at,'CONFIRMED',r.round_number,'Fixture planner: individual',true,now(),now()
            FROM fixture_game g JOIN fixture_round r ON r.id=g.round_id JOIN field f ON f.id=g.field_id WHERE g.id=:g
            ON CONFLICT(id) DO UPDATE SET venue_id=EXCLUDED.venue_id,scheduled_at=EXCLUDED.scheduled_at,status='CONFIRMED',active=true,updated_at=now()
            """,Map.of("g",id));
        db.update("UPDATE fixture_game SET confirmation_status='CONFIRMED',confirmed_at=now(),confirmed_by=:actor,manual_assignment=true WHERE id=:g",Map.of("g",id,"actor",actor()));
        bump(s.round(),true);audit(s,"CONFIRM_GAME","Partido confirmado","Publicado y protegido frente a replanificaciones.");
    }
    @Transactional(timeout=8)
    @PreAuthorize("hasAnyRole('ADMIN','ORGANIZER')")
    public void unconfirm(UUID season,UUID id,Long version){
        State s=load(season,id,version);
        if(!"CONFIRMED".equals(s.row().get("confirmation_status")))return;
        db.getJdbcTemplate().execute("SELECT set_config('orchedule.fixture_writer','on',true)");
        db.update("UPDATE match_game SET status='CANCELLED',active=false,updated_at=now() WHERE id=:g",Map.of("g",id));
        db.update("UPDATE fixture_game SET confirmation_status='PROVISIONAL',confirmed_at=NULL,confirmed_by=NULL WHERE id=:g",Map.of("g",id));
        bump(s.round(),false);audit(s,"UNCONFIRM_GAME","Partido devuelto a borrador","Retirado de publicación; se conserva la asignación manual.");
    }
    @Transactional(timeout=8)
    @PreAuthorize("hasAnyRole('ADMIN','ORGANIZER')")
    public void confirmLegacy(UUID id){confirm(seasonFor(id),id,null);}
    @Transactional(timeout=8)
    @PreAuthorize("hasAnyRole('ADMIN','ORGANIZER')")
    public void unconfirmLegacy(UUID id){unconfirm(seasonFor(id),id,null);}
    void validateForPublication(Round r,List<Game> games){
        for(Game g:games)validate(new State(r,g,Map.of()),g.at(),g.field());
    }
    private void bump(Round r,boolean autoConfirm){
        db.update("""
            UPDATE fixture_round SET version=version+1,status=CASE
              WHEN :auto AND NOT EXISTS(SELECT 1 FROM fixture_game WHERE round_id=:r AND confirmation_status<>'CONFIRMED')
              THEN 'CONFIRMED' ELSE 'DRAFT' END WHERE id=:r
            """,Map.of("auto",autoConfirm,"r",r.id()));
    }
    private String actor(){return SecurityContextHolder.getContext().getAuthentication().getName();}
    private void audit(State s,String action,String operation,String detail){
        db.update("""
            INSERT INTO fixture_audit(id,season_id,fixture_game_id,action,operation,result,actor,detail,new_value)
            SELECT :id,:s,:g,:a,:op,'SUCCESS',:actor,:detail,to_jsonb(g) FROM fixture_game g WHERE g.id=:g
            """,Map.of("id",UUID.randomUUID(),"s",s.round().season(),"g",s.game().id(),"a",action,"op",operation,
            "actor",actor(),"detail",s.game().homeName()+" — "+s.game().awayName()+": "+detail));
    }
}
