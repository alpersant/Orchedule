package com.orchedule.scheduling.planning;

import com.orchedule.field.domain.FieldRepository;
import com.orchedule.field.application.SetFieldOpeningHoursService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import java.time.*;
import java.util.*;

@RestController
@RequestMapping("/api/v1/planning/fields")
public class PlanningFieldsApi {
    private final FieldRepository fields;
    private final SetFieldOpeningHoursService openings;
    private final NamedParameterJdbcTemplate db;
    public PlanningFieldsApi(FieldRepository fields,SetFieldOpeningHoursService openings,NamedParameterJdbcTemplate db){
        this.fields=fields;this.openings=openings;this.db=db;
    }
    public record FieldView(UUID id,String name,String venueName,Set<DayOfWeek> days,Set<LocalTime> hours) {}
    public record Opening(@NotEmpty @Size(max=7) Set<@NotNull DayOfWeek> days,
                          @NotEmpty @Size(max=48) Set<@NotNull LocalTime> hours) {}
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Transactional(readOnly=true)
    public List<FieldView> read(){
        Map<UUID,String> venues=new HashMap<>();
        db.queryForList("SELECT id,name FROM venue",Map.of()).forEach(v->venues.put((UUID)v.get("id"),(String)v.get("name")));
        return fields.findAllActive().stream().sorted(Comparator.comparing(com.orchedule.field.domain.Field::getName))
            .map(f->new FieldView(f.getId(),f.getName(),venues.getOrDefault(f.getVenueId(),"Instalación"),f.getOpenDays(),f.getOpenHours())).toList();
    }
    @PutMapping("/{fieldId}/opening")
    @PreAuthorize("hasAnyRole('ADMIN','ORGANIZER')")
    @Transactional
    public void update(@PathVariable("fieldId") UUID fieldId,@Valid @RequestBody Opening body){
        db.getJdbcTemplate().execute("SELECT pg_advisory_xact_lock(813051031)");
        var field=fields.findById(fieldId).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"No se encuentra el campo"));
        Boolean booked=db.queryForObject("""
            SELECT EXISTS(SELECT 1 FROM fixture_game g JOIN fixture_round r ON r.id=g.round_id
              WHERE g.field_id=:f AND r.status='CONFIRMED' AND g.scheduled_at>=now())
            OR EXISTS(SELECT 1 FROM match_game m WHERE m.venue_id=:v AND m.active AND m.status<>'CANCELLED'
              AND m.scheduled_at>=now() AND NOT EXISTS(SELECT 1 FROM fixture_game g WHERE g.id=m.id))
            """,Map.of("f",fieldId,"v",field.getVenueId()),Boolean.class);
        if(Boolean.TRUE.equals(booked))throw new ResponseStatusException(HttpStatus.CONFLICT,"Hay partidos futuros confirmados. Cancela o revisa esas jornadas antes de cambiar la apertura general del campo.");
        openings.setOpeningHours(fieldId,body.days(),body.hours());
    }
}
