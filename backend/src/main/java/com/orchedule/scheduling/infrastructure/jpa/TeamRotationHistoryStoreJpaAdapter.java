package com.orchedule.scheduling.infrastructure.jpa;

import com.orchedule.scheduling.application.TeamRotationHistoryStore;
import com.orchedule.scheduling.domain.TeamRotationHistory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Persists TeamRotationHistory as simple CSV columns ("MONDAY,TUESDAY" /
 * "18:00,19:00"), most-recent-first. This lightweight encoding is
 * sufficient because the domain object only needs to answer "was this
 * day/hour played in the last 2 rounds?" — a normalized join table would
 * be overkill for a bounded 3-entry rolling window.
 *
 * Parsing is defensive: any corrupted or unexpected stored value is logged
 * and treated as an empty history rather than propagated as a runtime
 * exception, so a single bad row can never block schedule generation for
 * the whole season.
 */
@Component
public class TeamRotationHistoryStoreJpaAdapter implements TeamRotationHistoryStore {

    private static final Logger log = LoggerFactory.getLogger(TeamRotationHistoryStoreJpaAdapter.class);
    private static final String SEPARATOR = ",";

    private final SpringDataTeamRotationHistoryRepository repository;

    public TeamRotationHistoryStoreJpaAdapter(SpringDataTeamRotationHistoryRepository repository) {
        this.repository = repository;
    }

    @Override
    public TeamRotationHistory getOrCreate(UUID teamId) {
        return repository.findById(teamId)
                .map(entity -> toDomain(teamId, entity))
                .orElseGet(() -> new TeamRotationHistory(teamId));
    }

    @Override
    public void save(TeamRotationHistory history) {
        String daysCsv = joinDays(history.getRecentDays());
        String hoursCsv = joinHours(history.getRecentHours());

        TeamRotationHistoryEntity entity = repository.findById(history.getTeamId())
                .orElseGet(() -> new TeamRotationHistoryEntity(history.getTeamId(), "", ""));
        entity.setRecentDaysCsv(daysCsv);
        entity.setRecentHoursCsv(hoursCsv);
        repository.save(entity);
    }

    private TeamRotationHistory toDomain(UUID teamId, TeamRotationHistoryEntity entity) {
        List<DayOfWeek> days = parseDays(teamId, entity.getRecentDaysCsv());
        List<LocalTime> hours = parseHours(teamId, entity.getRecentHoursCsv());
        return TeamRotationHistory.restore(teamId, days, hours);
    }

    private String joinDays(List<DayOfWeek> days) {
        return days.stream().map(Enum::name).reduce((a, b) -> a + SEPARATOR + b).orElse("");
    }

    private String joinHours(List<LocalTime> hours) {
        return hours.stream().map(LocalTime::toString).reduce((a, b) -> a + SEPARATOR + b).orElse("");
    }

    private List<DayOfWeek> parseDays(UUID teamId, String csv) {
        List<DayOfWeek> result = new ArrayList<>();
        if (csv == null || csv.isBlank()) {
            return result;
        }
        for (String token : csv.split(SEPARATOR)) {
            try {
                result.add(DayOfWeek.valueOf(token.trim()));
            } catch (IllegalArgumentException ex) {
                log.warn("Corrupted rotation history day '{}' for team {}, skipping entry", token, teamId);
            }
        }
        return result;
    }

    private List<LocalTime> parseHours(UUID teamId, String csv) {
        List<LocalTime> result = new ArrayList<>();
        if (csv == null || csv.isBlank()) {
            return result;
        }
        for (String token : csv.split(SEPARATOR)) {
            try {
                result.add(LocalTime.parse(token.trim()));
            } catch (Exception ex) {
                log.warn("Corrupted rotation history hour '{}' for team {}, skipping entry", token, teamId);
            }
        }
        return result;
    }
}
