package com.orchedule.team.infrastructure.persistence;

import com.orchedule.team.domain.DayPreference;
import com.orchedule.team.domain.Team;
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

}