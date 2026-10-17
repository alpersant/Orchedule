package com.orchedule.scheduling.infrastructure.integration;

import com.orchedule.competition.application.GetCompetitionService;
import com.orchedule.competition.domain.Competition;
import com.orchedule.competition.domain.CompetitionStatus;
import com.orchedule.scheduling.application.exception.InvalidScheduleException;
import com.orchedule.scheduling.application.port.SeasonContextPort;
import com.orchedule.season.api.dto.SeasonResponse;
import com.orchedule.season.application.GetSeasonService;
import com.orchedule.season.domain.SeasonStatus;
import org.springframework.stereotype.Component;

import java.util.UUID;

/** Resolves the real season-to-competition relationship for scheduling. */
@Component
public class SeasonContextAdapter implements SeasonContextPort {

    private final GetSeasonService getSeasonService;
    private final GetCompetitionService getCompetitionService;

    public SeasonContextAdapter(GetSeasonService getSeasonService,
                                GetCompetitionService getCompetitionService) {
        this.getSeasonService = getSeasonService;
        this.getCompetitionService = getCompetitionService;
    }

    @Override
    public UUID getCompetitionIdForSeason(UUID seasonId) {
        return getSeasonService.getById(seasonId).competitionId();
    }

    @Override
    public void assertSeasonIsSchedulable(UUID seasonId) {
        SeasonResponse season = getSeasonService.getById(seasonId);

        if (!season.active() || season.status() != SeasonStatus.ACTIVE) {
            throw new InvalidScheduleException(
                    "Season " + seasonId + " must be active to generate schedule rounds");
        }

        Competition competition = getCompetitionService.getById(season.competitionId());

        if (competition.getStatus() != CompetitionStatus.ACTIVE) {
            throw new InvalidScheduleException(
                    "Competition " + competition.getId() + " must be active to generate schedule rounds");
        }
    }
}
