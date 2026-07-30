package com.orchedule.scheduling.infrastructure.integration;

import com.orchedule.competition.application.GetCompetitionService;
import com.orchedule.competition.domain.Competition;
import com.orchedule.competition.domain.CompetitionStatus;
import com.orchedule.scheduling.application.exception.InvalidScheduleException;
import com.orchedule.scheduling.application.port.SeasonContextPort;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * ACL adapter abstracting the competition module. NOTE: in the current
 * data model a "season" is represented directly by a Competition
 * aggregate (see the earlier competition module discussion) — if/when a
 * dedicated Season entity is introduced, only this adapter needs to
 * change; scheduling's port contract stays stable.
 */
@Component
public class SeasonContextAdapter implements SeasonContextPort {

    private final GetCompetitionService getCompetitionService;

    public SeasonContextAdapter(GetCompetitionService getCompetitionService) {
        this.getCompetitionService = getCompetitionService;
    }

    @Override
    public UUID getCompetitionIdForSeason(UUID seasonId) {
        // 1:1 mapping today (season == competition id). Kept as an explicit
        // method rather than passing seasonId straight through, so this
        // assumption is documented and isolated to one place.
        return seasonId;
    }

    @Override
    public void assertSeasonIsSchedulable(UUID seasonId) {
        Competition competition = getCompetitionService.getById(seasonId);
        if (competition.getStatus() != CompetitionStatus.ACTIVE) {
            throw new InvalidScheduleException(
                    "Competition " + seasonId + " is " + competition.getStatus()
                            + " and cannot generate schedule rounds — it must be ACTIVE");
        }
    }
}
