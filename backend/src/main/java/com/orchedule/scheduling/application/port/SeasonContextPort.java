package com.orchedule.scheduling.application.port;

import java.util.UUID;

/**
 * ACL port abstracting the competition/season module. Provides just enough
 * context (competition id, whether the season accepts schedule generation)
 * without leaking Competition's internal status model into scheduling.
 */
public interface SeasonContextPort {

    UUID getCompetitionIdForSeason(UUID seasonId);

    /**
     * @throws com.orchedule.scheduling.application.exception.InvalidScheduleException
     *         if the season is not in a state that allows generating rounds
     *         (e.g. DRAFT or CLOSED competition)
     */
    void assertSeasonIsSchedulable(UUID seasonId);
}
