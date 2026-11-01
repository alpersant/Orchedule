package com.orchedule.scheduling.planning;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.*;

@RestController
@RequestMapping("/api/v1/planning/seasons/{seasonId}")
public class PlanningApi {
    private final PlanningService service;
    public PlanningApi(PlanningService service){this.service=service;}
    public enum Mode {MANUAL,SINGLE,DOUBLE}
    public record PairInput(@NotNull UUID homeTeamId,@NotNull UUID awayTeamId) {}
    public record RoundInput(@NotEmpty List<@NotNull @Valid PairInput> games,UUID byeTeamId) {}
    public record Configure(@NotNull Mode mode,@NotEmpty @Size(max=64) List<@NotNull UUID> teamIds,
        @NotBlank @Size(max=80) String zoneId,@Min(15) @Max(240) int durationMinutes,
        @Size(max=126) List<@NotNull @Valid RoundInput> rounds) {}
    public record Revision(@Min(0) long version) {}
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public Map<String,Object> read(@PathVariable("seasonId") UUID seasonId){return service.read(seasonId);}
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN','ORGANIZER')")
    public Map<String,Object> configure(@PathVariable("seasonId") UUID seasonId,@Valid @RequestBody Configure body){
        service.configure(seasonId,body);return service.read(seasonId);
    }
    @PostMapping("/rounds/{number}/{action}")
    @PreAuthorize("hasAnyRole('ADMIN','ORGANIZER')")
    public Map<String,Object> action(@PathVariable("seasonId") UUID seasonId,@PathVariable("number") int number,
        @PathVariable("action") String action,@Valid @RequestBody Revision body){
        service.action(seasonId,number,action,body.version());return service.read(seasonId);
    }
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String,String>> status(ResponseStatusException e){
        return ResponseEntity.status(e.getStatusCode()).body(Map.of("message",Objects.toString(e.getReason(),"No se pudo completar la operación")));
    }
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String,String>> invalid(IllegalArgumentException e){
        return ResponseEntity.unprocessableEntity().body(Map.of("message",Objects.toString(e.getMessage(),"Datos inválidos")));
    }
}
