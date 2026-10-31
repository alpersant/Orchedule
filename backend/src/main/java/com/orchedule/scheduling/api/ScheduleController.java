package com.orchedule.scheduling.api;

import com.orchedule.scheduling.api.dto.ScheduleRoundResponse;
import com.orchedule.scheduling.application.CancelRoundService;
import com.orchedule.scheduling.application.ConfirmRoundService;
import com.orchedule.scheduling.application.GenerateRoundForSeasonUseCase;
import com.orchedule.scheduling.application.GetScheduleService;
import com.orchedule.scheduling.application.RegenerateRoundService;
import com.orchedule.scheduling.domain.ScheduleRound;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * REST API for schedule round generation, consultation and lifecycle management.
 *
 * <p>The API intentionally accepts only identifiers and primitive scheduling
 * parameters. Team preferences, field availability and opening slots are always
 * resolved server-side through the scheduling ACL.</p>
 */
@RestController
@RequestMapping("/api/schedules")
@Validated
public class ScheduleController {

    private final GenerateRoundForSeasonUseCase generateRoundForSeasonUseCase;
    private final GetScheduleService getScheduleService;
    private final ConfirmRoundService confirmRoundService;
    private final CancelRoundService cancelRoundService;
    private final RegenerateRoundService regenerateRoundService;

    public ScheduleController(
            GenerateRoundForSeasonUseCase generateRoundForSeasonUseCase,
            GetScheduleService getScheduleService,
            ConfirmRoundService confirmRoundService,
            CancelRoundService cancelRoundService,
            RegenerateRoundService regenerateRoundService
    ) {
        this.generateRoundForSeasonUseCase = generateRoundForSeasonUseCase;
        this.getScheduleService = getScheduleService;
        this.confirmRoundService = confirmRoundService;
        this.cancelRoundService = cancelRoundService;
        this.regenerateRoundService = regenerateRoundService;
    }

    @PostMapping("/seasons/{seasonId}/rounds/{weekNumber}")
    public ResponseEntity<ScheduleRoundResponse> generateRound(
            @PathVariable @NotNull UUID seasonId,
            @PathVariable @Min(1) int weekNumber
    ) {
        ScheduleRound round = generateRoundForSeasonUseCase.execute(seasonId, weekNumber);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ScheduleRoundResponse.from(round));
    }

    @GetMapping("/rounds/{id}")
    public ResponseEntity<ScheduleRoundResponse> getById(
            @PathVariable UUID id
    ) {
        ScheduleRound round = getScheduleService.getById(id);
        return ResponseEntity.ok(ScheduleRoundResponse.from(round));
    }

    @GetMapping("/rounds")
    public ResponseEntity<List<ScheduleRoundResponse>> getBySeasonAndWeek(
            @RequestParam UUID seasonId,
            @RequestParam(required = false) Integer weekNumber
    ) {
        if (weekNumber != null) {
            ScheduleRound round = getScheduleService.getBySeasonAndWeek(seasonId, weekNumber);
            return ResponseEntity.ok(List.of(ScheduleRoundResponse.from(round)));
        }

        List<ScheduleRoundResponse> rounds = getScheduleService.getBySeason(seasonId)
                .stream()
                .map(ScheduleRoundResponse::from)
                .toList();

        return ResponseEntity.ok(rounds);
    }

    @PostMapping("/rounds/{id}/confirm")
    public ResponseEntity<ScheduleRoundResponse> confirm(
            @PathVariable UUID id
    ) {
        ScheduleRound round = confirmRoundService.confirm(id);
        return ResponseEntity.ok(ScheduleRoundResponse.from(round));
    }

    @PostMapping("/rounds/{id}/cancel")
    public ResponseEntity<ScheduleRoundResponse> cancel(
            @PathVariable UUID id
    ) {
        ScheduleRound round = cancelRoundService.cancel(id);
        return ResponseEntity.ok(ScheduleRoundResponse.from(round));
    }

    @PostMapping("/rounds/{id}/regenerate")
    public ResponseEntity<ScheduleRoundResponse> regenerate(
            @PathVariable UUID id
    ) {
        ScheduleRound round = regenerateRoundService.regenerate(id);
        return ResponseEntity.ok(ScheduleRoundResponse.from(round));
    }
}
