package com.orchedule.scheduling.infrastructure.jpa;

import jakarta.persistence.*;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.UUID;

@Entity
@Table(name = "schedule_team_assignment")
public class TeamAssignmentEntity {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "round_id", nullable = false)
    private ScheduleRoundEntity round;

    @Column(name = "team_id", nullable = false)
    private UUID teamId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private DayOfWeek day;

    @Column(nullable = false)
    private LocalTime hour;

    @Column(name = "field_id", nullable = false)
    private UUID fieldId;

    protected TeamAssignmentEntity() {}

    public TeamAssignmentEntity(UUID id, UUID teamId, DayOfWeek day, LocalTime hour, UUID fieldId) {
        this.id = id;
        this.teamId = teamId;
        this.day = day;
        this.hour = hour;
        this.fieldId = fieldId;
    }

    public UUID getId() { return id; }
    public UUID getTeamId() { return teamId; }
    public DayOfWeek getDay() { return day; }
    public LocalTime getHour() { return hour; }
    public UUID getFieldId() { return fieldId; }
    public ScheduleRoundEntity getRound() { return round; }
    public void setRound(ScheduleRoundEntity round) { this.round = round; }
}
