package com.orchedule.scheduling.infrastructure.jpa;

import com.orchedule.scheduling.domain.ScheduleRoundStatus;
import jakarta.persistence.*;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "schedule_rounds",
       uniqueConstraints = @UniqueConstraint(columnNames = {"season_id", "week_number"}))
public class ScheduleRoundEntity {

    @Id
    private UUID id;

    @Column(name = "season_id", nullable = false)
    private UUID seasonId;

    @Column(name = "week_number", nullable = false)
    private int weekNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ScheduleRoundStatus status;

    @OneToMany(mappedBy = "round", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<TeamAssignmentEntity> assignments = new ArrayList<>();

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    protected ScheduleRoundEntity() {}

    public ScheduleRoundEntity(UUID id, UUID seasonId, int weekNumber, ScheduleRoundStatus status,
                                OffsetDateTime createdAt, OffsetDateTime updatedAt) {
        this.id = id;
        this.seasonId = seasonId;
        this.weekNumber = weekNumber;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public void replaceAssignments(List<TeamAssignmentEntity> newAssignments) {
        this.assignments.clear();
        for (TeamAssignmentEntity assignment : newAssignments) {
            assignment.setRound(this);
            this.assignments.add(assignment);
        }
    }

    public UUID getId() { return id; }
    public UUID getSeasonId() { return seasonId; }
    public int getWeekNumber() { return weekNumber; }
    public ScheduleRoundStatus getStatus() { return status; }
    public List<TeamAssignmentEntity> getAssignments() { return assignments; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }

    public void setStatus(ScheduleRoundStatus status) { this.status = status; }
    public void setUpdatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; }
}
