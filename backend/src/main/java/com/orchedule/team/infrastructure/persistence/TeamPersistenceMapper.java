package com.orchedule.team.infrastructure.persistence;

import com.orchedule.team.domain.DayPreference;
import com.orchedule.team.domain.Team;
import com.orchedule.team.domain.TeamPreference;
import com.orchedule.team.domain.TimePreference;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

public final class TeamPersistenceMapper {

    private TeamPersistenceMapper() {
    }

    public static TeamJpaEntity toNewEntity(String name) {
        OffsetDateTime now = OffsetDateTime.now();
        TeamJpaEntity entity = new TeamJpaEntity();
        entity.setName(name);
        entity.setActive(true);
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);
        return entity;
    }

    public static Team toDomain(TeamJpaEntity entity) {
        return new Team(
                entity.getId(),
                entity.getName(),
                entity.isActive(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    public static TeamPreferenceJpaEntity toEntity(TeamPreference preference) {
        TeamPreferenceJpaEntity entity = new TeamPreferenceJpaEntity();
        entity.setTeamId(preference.teamId());
        entity.setRestrictionType(preference.restrictionType());
        entity.setExcludedDay(preference.excludedDay());
        entity.setExcludedHour(preference.excludedHour());
        entity.setCreatedAt(preference.createdAt());
        entity.setUpdatedAt(preference.updatedAt());

        List<TeamPreferenceDayJpaEntity> dayEntities = new ArrayList<>();
        for (DayPreference dayPreference : preference.dayPreferences()) {
            TeamPreferenceDayJpaEntity item = new TeamPreferenceDayJpaEntity();
            item.setTeamPreference(entity);
            item.setDay(dayPreference.day());
            item.setPriority(dayPreference.priority());
            dayEntities.add(item);
        }
        entity.setDayPreferences(dayEntities);

        List<TeamPreferenceHourJpaEntity> hourEntities = new ArrayList<>();
        for (TimePreference timePreference : preference.timePreferences()) {
            TeamPreferenceHourJpaEntity item = new TeamPreferenceHourJpaEntity();
            item.setTeamPreference(entity);
            item.setHour(timePreference.hour());
            item.setPriority(timePreference.priority());
            hourEntities.add(item);
        }
        entity.setHourPreferences(hourEntities);

        return entity;
    }

    public static TeamPreference toDomain(TeamPreferenceJpaEntity entity) {
        return new TeamPreference(
                entity.getTeamId(),
                entity.getRestrictionType(),
                entity.getExcludedDay(),
                entity.getExcludedHour(),
                entity.getDayPreferences().stream()
                        .map(p -> new DayPreference(p.getDay(), p.getPriority()))
                        .toList(),
                entity.getHourPreferences().stream()
                        .map(p -> new TimePreference(p.getHour(), p.getPriority()))
                        .toList(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}