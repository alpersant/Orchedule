package com.orchedule.scheduling.planning;
import com.orchedule.scheduling.planning.partial.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.*;
import java.time.ZoneId;
import java.util.*;

@RestController
@RequestMapping("/api/v1/planning/seasons/{seasonId}")
public class PlanningApi {
    private final PlanningService core;private final PartialRoundService rounds;private final PlanningReadFacade read;
    private final String defaultZone;private final int defaultDuration;
    public PlanningApi(PlanningService core,PartialRoundService rounds,PlanningReadFacade read,
        @Value("${orchedule.planning.zone-id:Europe/Madrid}") String zone,
        @Value("${orchedule.planning.duration-minutes:60}") int duration){
        this.core=core;this.rounds=rounds;this.read=read;this.defaultZone=ZoneId.of(zone).getId();
        if(duration<15||duration>240)throw new IllegalArgumentException("Invalid server match duration");this.defaultDuration=duration;
    }
    public enum Mode {MANUAL,SINGLE,DOUBLE}
    public record PairInput(@NotNull UUID homeTeamId,@NotNull UUID awayTeamId){}
    public record RoundInput(@NotEmpty List<@NotNull @Valid PairInput> games,UUID byeTeamId){}
    public record Configure(@NotNull Mode mode,@NotEmpty @Size(max=64) List<@NotNull UUID> teamIds,
        @Size(max=80) String zoneId,@Min(15) @Max(240) Integer durationMinutes,@Size(max=126) List<@NotNull @Valid RoundInput> rounds){}
    public record Revision(@Min(0) long version){}
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public Map<String,Object> read(@PathVariable("seasonId") UUID id){return read.read(id);}
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN','ORGANIZER')")
    public Map<String,Object> configure(@PathVariable("seasonId") UUID id,@Valid @RequestBody Configure body){
        core.configure(id,new Configure(body.mode(),body.teamIds(),defaultZone,body.durationMinutes()==null?defaultDuration:body.durationMinutes(),body.rounds()));
        return read.read(id);
    }
    @PostMapping("/rounds/{number}/{action}")
    @PreAuthorize("hasAnyRole('ADMIN','ORGANIZER')")
    public Map<String,Object> action(@PathVariable("seasonId") UUID id,@PathVariable("number") int number,
        @PathVariable("action") String action,@Valid @RequestBody Revision body){
        rounds.execute(id,number,action,body.version());return read.read(id);
    }
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String,String>> status(ResponseStatusException e){return ResponseEntity.status(e.getStatusCode()).body(Map.of("message",Objects.toString(e.getReason(),"No se pudo completar la operación")));}
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String,String>> invalid(IllegalArgumentException e){return ResponseEntity.unprocessableEntity().body(Map.of("message",Objects.toString(e.getMessage(),"Datos inválidos")));}
}
