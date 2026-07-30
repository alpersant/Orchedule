package com.orchedule.scheduling.application;

import com.orchedule.scheduling.domain.ScheduleRound;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Application facade combining input assembly (cross-module ACL reads) and
 * the actual generation. This is the entry point the REST controller
 * should call — it never needs to know about ports or commands directly.
 */
@Service
public class GenerateRoundForSeasonUseCase {

    private final ScheduleInputAssembler scheduleInputAssembler;
    private final GenerateScheduleForSeasonService generateScheduleForSeasonService;

    public GenerateRoundForSeasonUseCase(ScheduleInputAssembler scheduleInputAssembler,
                                          GenerateScheduleForSeasonService generateScheduleForSeasonService) {
        this.scheduleInputAssembler = scheduleInputAssembler;
        this.generateScheduleForSeasonService = generateScheduleForSeasonService;
    }

    public ScheduleRound execute(UUID seasonId, int weekNumber) {
        GenerateRoundCommand command = scheduleInputAssembler.assembleFor(seasonId, weekNumber);
        return generateScheduleForSeasonService.generate(command);
    }
}
