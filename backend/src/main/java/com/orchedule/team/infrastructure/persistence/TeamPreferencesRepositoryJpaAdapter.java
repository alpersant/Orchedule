package com.orchedule.team.infrastructure.persistence;

import com.orchedule.team.domain.TeamDayPreference;
import com.orchedule.team.domain.TeamHourPreference;
import com.orchedule.team.domain.TeamPreference;
import com.orchedule.team.domain.TeamPreferenceRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class TeamPreferencesRepositoryJpaAdapter implements TeamPreferenceRepository {

    private final SpringDataTeamPreferenceRepository springDataRepository;

    public TeamPreferencesRepositoryJpaAdapter(SpringDataTeamPreferenceRepository springDataRepository) {
        this.springDataRepository = springDataRepository;
    }

    @Override
    public TeamPreference save(TeamPreference preferences) {
        TeamPreferenceEntity entity = springDataRepository.findById(preferences.getId())
                .orElseGet(() -> new TeamPreferenceEntity(preferences.getId(), preferences.getTeamId(),
                        preferences.getCompetitionId(), preferences.getCreatedAt(), preferences.getUpdatedAt()));

        entity.setUpdatedAt(preferences.getUpdatedAt());

        List<TeamDayPreferenceEntity> dayEntities = preferences.getDayPreferences().stream()
                .map(p -> new TeamDayPreferenceEntity(UUID.randomUUID(), p.day(), p.primary()))
                .toList();
        entity.replaceDayPreferences(dayEntities);

        List<TeamHourPreferenceEntity> hourEntities = preferences.getHourPreferences().stream()
                .map(p -> new TeamHourPreferenceEntity(UUID.randomUUID(), p.hour(), p.primary()))
                .toList();
        entity.replaceHourPreferences(hourEntities);

        TeamPreferenceEntity saved = springDataRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<TeamPreference> findByTeamIdAndCompetitionId(UUID teamId, UUID competitionId) {
        return springDataRepository.findByTeamIdAndCompetitionId(teamId, competitionId).map(this::toDomain);
    }

    @Override
    public List<TeamPreference> findByCompetitionId(UUID competitionId) {
        return springDataRepository.findByCompetitionId(competitionId).stream().map(this::toDomain).toList();
    }

    @Override
    public boolean existsByTeamIdAndCompetitionId(UUID teamId, UUID competitionId) {
        return springDataRepository.existsByTeamIdAndCompetitionId(teamId, competitionId);
    }

    private TeamPreference toDomain(TeamPreferenceEntity entity) {
        List<TeamDayPreference> days = entity.getDayPreferences().stream()
                .map(d -> new TeamDayPreference(d.getDay(), d.isPrimary()))
                .toList();
        List<TeamHourPreference> hours = entity.getHourPreferences().stream()
                .map(h -> new TeamHourPreference(h.getHour(), h.isPrimary()))
                .toList();
        return new TeamPreference(entity.getId(), entity.getTeamId(), entity.getCompetitionId(),
                days, hours, entity.getCreatedAt(), entity.getUpdatedAt());
    }
}
