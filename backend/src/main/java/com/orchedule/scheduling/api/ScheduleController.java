package com.orchedule.scheduling.api;

import com.orchedule.scheduling.api.dto.ScheduleRoundResponse;
import com.orchedule.scheduling.application.CancelRoundService;
import com.orchedule.scheduling.application.ConfirmRoundService;
import com.orchedule.scheduling.application.GenerateRoundForSeasonUseCase;
import com.orchedule.scheduling.application.GetScheduleService;
import com.orchedule.scheduling.domain.ScheduleRound;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Note: request payloads no longer carry raw team/field/day/hour data —
 * generation now pulls everything through the ACL ports based on
 * seasonId + weekNumber alone. This removes an entire class of client-side
 * tampering risk (a caller could previously submit arbitrary field ids or
 * fabricated team preferences directly in the request body).
 */
@RestController
@RequestMapping("/api/schedules")
@Validated
public class ScheduleController {

    private final GenerateRoundForSeasonUseCase generateRoundForSeasonUseCase;
    private final GetScheduleService getScheduleService;
    private final ConfirmRoundService confirmRoundService;
    private final CancelRoundService cancelRoundService;

    public ScheduleController(GenerateRoundForSeasonUseCase generateRoundForSeasonUseCase,
                               GetScheduleService getScheduleService,
                               ConfirmRoundService confirmRoundService,
                               CancelRoundService cancelRoundService) {
        this.generateRoundForSeasonUseCase = generateRoundForSeasonUseCase;
        this.getScheduleService = getScheduleService;
        this.confirmRoundService = confirmRoundService;
        this.cancelRoundService = cancelRoundService;
    }

    @PostMapping("/seasons/{seasonId}/rounds/{weekNumber}")
    public ResponseEntity<ScheduleRoundResponse> generateRound(
            @PathVariable @NotNull UUID seasonId,
            @PathVariable @Min(1) int weekNumber) {
        ScheduleRound round = generateRoundForSeasonUseCase.execute(seasonId, weekNumber);
        return ResponseEntity.status(HttpStatus.CREATED).body(ScheduleRoundResponse.from(round));
    }

    @GetMapping("/rounds/{id}")
    public ResponseEntity<ScheduleRoundResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ScheduleRoundResponse.from(getScheduleService.getById(id)));
    }

    @GetMapping("/rounds")
    public ResponseEntity<?> getBySeasonAndWeek(
            @RequestParam UUID seasonId, @RequestParam(required = false) Integer weekNumber) {
        if (weekNumber != null) {
            ScheduleRound round = getScheduleService.getBySeasonAndWeek(seasonId, weekNumber);
            return ResponseEntity.ok(ScheduleRoundResponse.from(round));
        }
        List<ScheduleRoundResponse> rounds = getScheduleService.getBySeason(seasonId).stream()
                .map(ScheduleRoundResponse::from)
                .toList();
        return ResponseEntity.ok(rounds);
    }

    @PostMapping("/rounds/{id}/confirm")
    public ResponseEntity<ScheduleRoundResponse> confirm(@PathVariable UUID id) {
        return ResponseEntity.ok(ScheduleRoundResponse.from(confirmRoundService.confirm(id)));
    }

    @PostMapping("/rounds/{id}/cancel")
    public ResponseEntity<ScheduleRoundResponse> cancel(@PathVariable UUID id) {
        return ResponseEntity.ok(ScheduleRoundResponse.from(cancelRoundService.cancel(id)));
    }
}
