package com.orchedule.team.infrastructure.persistence;

import com.orchedule.team.domain.*;
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
        TeamPreferenceEntity entity = repository.findById(preference.getTeamId())
                .map(existing -> {
                    existing.applyUpdate(preference.getRestrictionType(), preference.getExcludedDay(),
                            preference.getExcludedHour(), OffsetDateTime.now());
                    return existing;
                })
                .orElseGet(() -> new TeamPreferenceEntity(preference.getTeamId(), preference.getRestrictionType(),
                        preference.getExcludedDay(), preference.getExcludedHour(),
                        preference.getCreatedAt(), preference.getUpdatedAt()));

        List<TeamPreferenceDayEntity> dayEntities = preference.getDayPreferences().stream()
                .map(d -> new TeamPreferenceDayEntity(d.day(), d.priority()))
                .toList();
        entity.replaceDayPreferences(dayEntities);

        List<TeamPreferenceHourEntity> hourEntities = preference.getHourPreferences().stream()
                .map(h -> new TeamPreferenceHourEntity(h.hour(), h.priority()))
                .toList();
        entity.replaceHourPreferences(hourEntities);

        return toDomain(repository.save(entity));
    }

    @Override
    public Optional<TeamPreference> findByTeamId(UUID teamId) {
        return repository.findByTeamId(teamId).map(this::toDomain);
    }

    @Override
    public boolean existsByTeamId(UUID teamId) {
        return repository.existsByTeamId(teamId);
    }

    private TeamPreference toDomain(TeamPreferenceEntity entity) {
        List<TeamPreferenceDay> days = entity.getDayPreferences().stream()
                .map(d -> new TeamPreferenceDay(d.getDay(), d.getPriority()))
                .toList();
        List<TeamPreferenceHour> hours = entity.getHourPreferences().stream()
                .map(h -> new TeamPreferenceHour(h.getHour(), h.getPriority()))
                .toList();
        return new TeamPreference(entity.getTeamId(), entity.getRestrictionType(), entity.getExcludedDay(),
                entity.getExcludedHour(), days, hours, entity.getCreatedAt(), entity.getUpdatedAt());
    }
}
