package com.orchedule.scheduling.planning.audit;

import com.orchedule.scheduling.planning.audit.AuditModel.AuditReport;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

/** Informe de auditoría y equidad de una temporada. Solo lectura; restringido a gestores. */
@RestController
@RequestMapping("/api/v1/planning/seasons/{seasonId}/audit")
public class SeasonAuditApi {

    private final SeasonAuditService service;

    public SeasonAuditApi(SeasonAuditService service) {
        this.service = service;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','ORGANIZER')")
    public AuditReport report(
            @PathVariable("seasonId") UUID seasonId,
            @RequestParam(name = "includeDraft", defaultValue = "false") boolean includeDraft
    ) {
        return service.audit(seasonId, includeDraft);
    }

    @ExceptionHandler(SeasonCalendarNotFoundException.class)
    public ResponseEntity<Map<String, String>> notFound(SeasonCalendarNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("message", "La temporada no tiene un calendario generado"));
    }
}
