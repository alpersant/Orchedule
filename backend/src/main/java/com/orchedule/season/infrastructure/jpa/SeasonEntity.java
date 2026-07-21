package com.orchedule.season.infrastructure.jpa;

import com.orchedule.season.domain.Season;
import com.orchedule.season.domain.SeasonStatus;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "season",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_season_name", columnNames = "name")
        },
        indexes = {
                @Index(name = "idx_season_status", columnList = "status"),
                @Index(name = "idx_season_active", columnList = "active"),
                @Index(name = "idx_season_start_date", columnList = "start_date"),
                @Index(name = "idx_season_end_date", columnList = "end_date")
        }
)
public class SeasonEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "name", nullable = false, length = 120)
    private String name;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private SeasonStatus status;

    @Column(name = "active", nullable = false)
    private boolean active;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    protected SeasonEntity() {
    }

    public static SeasonEntity create(String name,
                                      LocalDate startDate,
                                      LocalDate endDate,
                                      SeasonStatus status,
                                      boolean active) {
        SeasonEntity entity = new SeasonEntity();
        OffsetDateTime now = OffsetDateTime.now();

        entity.id = UUID.randomUUID();
        entity.name = name;
        entity.startDate = startDate;
        entity.endDate = endDate;
        entity.status = status;
        entity.active = active;
        entity.createdAt = now;
        entity.updatedAt = now;

        return entity;
    }

    public void update(String name,
                       LocalDate startDate,
                       LocalDate endDate,
                       SeasonStatus status,
                       boolean active) {
        this.name = name;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = status;
        this.active = active;
        this.updatedAt = OffsetDateTime.now();
    }

    public Season toDomain() {
        return new Season(
                id,
                name,
                startDate,
                endDate,
                status,
                active,
                createdAt,
                updatedAt
        );
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}