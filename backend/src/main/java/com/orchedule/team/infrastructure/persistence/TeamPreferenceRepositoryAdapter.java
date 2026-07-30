package com.orchedule.team.infrastructure.persistence;

import com.orchedule.team.domain.DayPreference;
import com.orchedule.team.domain.TeamPreference;
import com.orchedule.team.domain.TeamPreferenceRepository;
import com.orchedule.team.domain.TimePreference;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;


@Repository
public class TeamPreferenceRepositoryAdapter implements TeamPreferenceRepository {

    private final SpringDataTeamPreferenceRepository repository;

    public TeamPreferenceRepositoryAdapter(SpringDataTeamPreferenceRepository repository) {
        this.repository = repository;
    }

    @Override
    public TeamPreference save(TeamPreference preference) {
        TeamPreferenceEntity entity = repository
                .findByTeamIdAndCompetitionId(preference.getTeamId(), preference.getCompetitionId())
                .map(existing -> {
                    existing.applyUpdate(preference.getRestrictionType(), preference.getExcludedDay(),
                            preference.getExcludedHour(), OffsetDateTime.now());
                    return existing;
                })
                .orElseGet(() -> new TeamPreferenceEntity(preference.getTeamId(), preference.getCompetitionId(),
                        preference.getRestrictionType(), preference.getExcludedDay(), preference.getExcludedHour(),
                        preference.getCreatedAt(), preference.getUpdatedAt()));

        List<TeamPreferenceDayEntity> dayEntities = preference.getDayPreferences().stream()
                .map(d -> new TeamPreferenceDayEntity(d.day(), d.priority()))
                .toList();
        entity.replaceDayPreferences(dayEntities);

        List<TeamPreferenceHourEntity> hourEntities = preference.getTimePreferences().stream()
                .map(h -> new TeamPreferenceHourEntity(h.hour(), h.priority()))
                .toList();
        entity.replaceHourPreferences(hourEntities);

        return toDomain(repository.save(entity));
    }

    @Override
    public Optional<TeamPreference> findByTeamIdAndCompetitionId(UUID teamId, UUID competitionId) {
        return repository.findByTeamIdAndCompetitionId(teamId, competitionId).map(this::toDomain);
    }

    @Override
    public List<TeamPreference> findByCompetitionId(UUID competitionId) {
        return repository.findByCompetitionId(competitionId).stream().map(this::toDomain).toList();
    }

    @Override
    public boolean existsByTeamIdAndCompetitionId(UUID teamId, UUID competitionId) {
        return repository.existsByTeamIdAndCompetitionId(teamId, competitionId);
    }

    private TeamPreference toDomain(TeamPreferenceEntity entity) {
        List<DayPreference> days = entity.getDayPreferences().stream()
                .map(d -> new DayPreference(d.getDay(), d.getPriority()))
                .toList();
        List<TimePreference> hours = entity.getHourPreferences().stream()
                .map(h -> new TimePreference(h.getHour(), h.getPriority()))
                .toList();
        return new TeamPreference(entity.getTeamId(), entity.getTeamId(), entity.getCompetitionId(),
                entity.getRestrictionType(), entity.getExcludedDay(), entity.getExcludedHour(),
                days, hours, entity.getCreatedAt(), entity.getUpdatedAt());
    }
}
