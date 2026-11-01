package com.orchedule.scheduling.planning;
import com.orchedule.season.application.ExtendSeasonEndDateService;
import com.orchedule.season.api.dto.SeasonResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.dao.QueryTimeoutException;
import org.springframework.transaction.TransactionTimedOutException;
import org.springframework.http.*;
import java.time.LocalDate;
import java.util.*;

@RestController
@RequestMapping("/api/v1/planning/seasons/{seasonId}")
public class CalendarCapacityApi {
    private final CalendarCapacityService capacity;
    private final ExtendSeasonEndDateService extension;
    public CalendarCapacityApi(CalendarCapacityService capacity,ExtendSeasonEndDateService extension){this.capacity=capacity;this.extension=extension;}
    public record Check(@NotNull PlanningApi.Mode mode,@NotNull @Size(max=64) List<@NotNull UUID> teamIds,@Min(0) @Max(126) Integer manualRoundCount) {}
    public record Extend(@NotNull LocalDate expectedStartDate,@NotNull LocalDate expectedEndDate,@NotNull LocalDate endDate) {}
    @PostMapping("/preflight")
    @PreAuthorize("hasAnyRole('ADMIN','ORGANIZER')")
    public CalendarCapacityService.Preview check(@PathVariable("seasonId") UUID id,@Valid @RequestBody Check body){return capacity.check(id,body.mode(),body.teamIds(),body.manualRoundCount());}
    @PatchMapping("/end-date")
    @PreAuthorize("hasAnyRole('ADMIN','ORGANIZER')")
    public SeasonResponse extend(@PathVariable("seasonId") UUID id,@Valid @RequestBody Extend body){return extension.extend(id,body.expectedStartDate(),body.expectedEndDate(),body.endDate());}
    @ExceptionHandler({QueryTimeoutException.class,TransactionTimedOutException.class})
    public ResponseEntity<Map<String,String>> timeout(RuntimeException e){return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(Map.of("message","La comprobación de fechas ha superado su tiempo disponible. Reintenta; no se ha creado ningún calendario."));}
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String,String>> status(ResponseStatusException e){return ResponseEntity.status(e.getStatusCode()).body(Map.of("message",Objects.toString(e.getReason(),"No se pudo completar la operación")));}
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String,String>> invalid(IllegalArgumentException e){return ResponseEntity.unprocessableEntity().body(Map.of("message",Objects.toString(e.getMessage(),"Datos inválidos")));}
}
