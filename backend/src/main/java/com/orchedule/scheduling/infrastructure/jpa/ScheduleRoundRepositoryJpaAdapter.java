package com.orchedule.scheduling.infrastructure.jpa;

import com.orchedule.scheduling.domain.*;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class ScheduleRoundRepositoryJpaAdapter implements ScheduleRoundRepository {

    private final SpringDataScheduleRoundRepository springDataRepository;

    public ScheduleRoundRepositoryJpaAdapter(SpringDataScheduleRoundRepository springDataRepository) {
        this.springDataRepository = springDataRepository;
    }

    @Override
    public ScheduleRound save(ScheduleRound round) {
        ScheduleRoundEntity entity = springDataRepository.findById(round.getId())
                .orElseGet(() -> new ScheduleRoundEntity(round.getId(), round.getSeasonId(), round.getWeekNumber(),
                        round.getStatus(), round.getCreatedAt(), round.getUpdatedAt()));

        entity.setStatus(round.getStatus());
        entity.setUpdatedAt(round.getUpdatedAt());

        List<TeamAssignmentEntity> assignmentEntities = round.getAssignments().stream()
                .map(a -> new TeamAssignmentEntity(UUID.randomUUID(), a.teamId(),
                        a.slot().day(), a.slot().hour(), a.slot().fieldId()))
                .toList();
        entity.replaceAssignments(assignmentEntities);

        ScheduleRoundEntity saved = springDataRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<ScheduleRound> findById(UUID id) {
        return springDataRepository.findById(id).map(this::toDomain);
    }

    @Override
    public Optional<ScheduleRound> findBySeasonIdAndWeekNumber(UUID seasonId, int weekNumber) {
        return springDataRepository.findBySeasonIdAndWeekNumber(seasonId, weekNumber).map(this::toDomain);
    }

    @Override
    public List<ScheduleRound> findBySeasonId(UUID seasonId) {
        return springDataRepository.findBySeasonId(seasonId).stream().map(this::toDomain).toList();
    }

    @Override
    public List<ScheduleRound> findBySeasonIdAndStatus(UUID seasonId, ScheduleRoundStatus status) {
        return springDataRepository.findBySeasonIdAndStatus(seasonId, status).stream().map(this::toDomain).toList();
    }

    @Override
    public boolean existsBySeasonIdAndWeekNumber(UUID seasonId, int weekNumber) {
        return springDataRepository.existsBySeasonIdAndWeekNumber(seasonId, weekNumber);
    }

    private ScheduleRound toDomain(ScheduleRoundEntity entity) {
        List<TeamAssignment> assignments = entity.getAssignments().stream()
                .map(a -> new TeamAssignment(a.getTeamId(), new ScheduleSlot(a.getDay(), a.getHour(), a.getFieldId())))
                .toList();
        return new ScheduleRound(entity.getId(), entity.getSeasonId(), entity.getWeekNumber(),
                entity.getStatus(), assignments, entity.getCreatedAt(), entity.getUpdatedAt());
    }
}
