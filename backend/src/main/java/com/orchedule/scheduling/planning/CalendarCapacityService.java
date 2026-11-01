package com.orchedule.scheduling.planning;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.*;
import java.util.*;

@Service
public class CalendarCapacityService {
    private static final Logger log=LoggerFactory.getLogger(CalendarCapacityService.class);
    private final NamedParameterJdbcTemplate db;
    public CalendarCapacityService(NamedParameterJdbcTemplate db){this.db=db;}
    public record Preview(UUID seasonId,String seasonName,LocalDate startDate,LocalDate endDate,
        int selectedTeams,int availableWeeks,Integer requiredRounds,Integer missingWeeks,Boolean fits,
        LocalDate earliestEndDate,LocalDate suggestedEndDate,boolean alreadyConfigured,
        boolean canConfigureBase,boolean withinSupportedRange,boolean canContinue,boolean hasPastDates,String message) {}
    @Transactional(readOnly=true,timeout=2)
    public Preview check(UUID id,PlanningApi.Mode mode,List<UUID> teamIds,Integer manualRounds){
        long started=System.nanoTime();
        try{
            // Local to THIS PostgreSQL transaction: do not mutate the shared JdbcTemplate timeout.
            db.getJdbcTemplate().execute("SET LOCAL statement_timeout = '1800ms'");
            if(new HashSet<>(teamIds).size()!=teamIds.size())throw new IllegalArgumentException("Hay equipos repetidos en la selección");
            var rows=db.queryForList("SELECT s.name,s.start_date,s.end_date,s.status,s.active,c.status AS competition_status FROM season s JOIN competition c ON c.id=s.competition_id WHERE s.id=:id",Map.of("id",id));
            if(rows.isEmpty())throw new ResponseStatusException(HttpStatus.NOT_FOUND,"No se encuentra la temporada");
            var row=rows.getFirst();
            LocalDate start=LocalDate.parse(row.get("start_date").toString()),end=LocalDate.parse(row.get("end_date").toString());
            Integer required=teamIds.size()<2?null:CalendarCapacity.roundCount(mode,teamIds.size(),manualRounds);
            var capacity=CalendarCapacity.evaluate(start,end,required);
            boolean configured=Boolean.TRUE.equals(db.queryForObject("SELECT EXISTS(SELECT 1 FROM fixture_plan WHERE season_id=:id)",Map.of("id",id),Boolean.class));
            boolean active=Boolean.TRUE.equals(row.get("active"))&&"ACTIVE".equals(row.get("status"))&&"ACTIVE".equals(row.get("competition_status"));
            boolean base=active&&!configured,supported=capacity.requiredRounds()==null||capacity.requiredRounds()<=60;
            boolean canContinue=base&&supported&&Boolean.TRUE.equals(capacity.fits());String message;
            if(configured)message="La temporada ya tiene un calendario creado. Actualiza para consultarlo.";
            else if(!active)message="La competición y la temporada deben estar activas antes de crear el calendario.";
            else if(!supported)message="Este formato necesita más de 60 semanas. El módulo actual de disponibilidad admite 60; cambia el formato o los participantes.";
            else if(teamIds.size()<2)message="Selecciona al menos dos equipos para calcular las jornadas necesarias.";
            else if(capacity.requiredRounds()==null)message="Las fechas se comprobarán cuando añadas o importes las jornadas manuales.";
            else if(!Boolean.TRUE.equals(capacity.fits()))message="Necesitas "+capacity.requiredRounds()+" jornadas, pero la temporada contiene "+capacity.availableWeeks()+" semanas. Faltan "+capacity.missingWeeks()+" semanas con una jornada por semana.";
            else message="Las "+capacity.requiredRounds()+" jornadas caben en las "+capacity.availableWeeks()+" semanas. Campos y preferencias se comprobarán al asignar horarios.";
            return new Preview(id,(String)row.get("name"),start,end,teamIds.size(),capacity.availableWeeks(),capacity.requiredRounds(),capacity.missingWeeks(),capacity.fits(),
                capacity.earliestEndDate(),capacity.suggestedEndDate(),configured,base,supported,canContinue,start.isBefore(LocalDate.now(ZoneOffset.UTC)),message);
        }finally{log.debug("calendar-preflight database/calculation elapsed={} ms",(System.nanoTime()-started)/1_000_000);}
    }
}
