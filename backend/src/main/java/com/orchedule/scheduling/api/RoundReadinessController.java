package com.orchedule.scheduling.api;

import com.orchedule.scheduling.api.dto.RoundReadinessResponse;
import com.orchedule.scheduling.application.ValidateRoundReadinessService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/schedules")
@Validated
public class RoundReadinessController {

    private final ValidateRoundReadinessService validateRoundReadinessService;

    public RoundReadinessController(ValidateRoundReadinessService validateRoundReadinessService) {
        this.validateRoundReadinessService = validateRoundReadinessService;
    }

    @GetMapping("/seasons/{seasonId}/rounds/{weekNumber}/readiness")
    public ResponseEntity<RoundReadinessResponse> getReadiness(
            @PathVariable UUID seasonId,
            @PathVariable @Min(1) @Max(60) int weekNumber
    ) {
        return ResponseEntity.ok(
                RoundReadinessResponse.from(validateRoundReadinessService.validate(seasonId, weekNumber))
        );
    }
}
