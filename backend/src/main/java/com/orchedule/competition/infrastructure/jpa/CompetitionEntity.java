package com.orchedule.competition.infrastructure.jpa;

import com.orchedule.competition.domain.CompetitionDay;
import com.orchedule.competition.domain.CompetitionHour;
import com.orchedule.competition.domain.CompetitionStatus;
import jakarta.persistence.*;

import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "competitions")
public class CompetitionEntity {

    @Id
    private UUID id;

    @Column(nullable = false, unique = true, length = 150)
    private String name;

    @Column(length = 1000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CompetitionStatus status;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "competition_default_days", joinColumns = @JoinColumn(name = "competition_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "day", nullable = false, length = 20)
    private Set<CompetitionDay> defaultDays = new HashSet<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "competition_default_hours", joinColumns = @JoinColumn(name = "competition_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "hour", nullable = false, length = 10)
    private Set<CompetitionHour> defaultHours = new HashSet<>();

    @Column(name = "default_field_count", nullable = false)
    private int defaultFieldCount;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    protected CompetitionEntity() {}

    public CompetitionEntity(UUID id, String name, String description, CompetitionStatus status,
                              Set<CompetitionDay> defaultDays, Set<CompetitionHour> defaultHours,
                              int defaultFieldCount, OffsetDateTime createdAt, OffsetDateTime updatedAt) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.status = status;
        this.defaultDays = new HashSet<>(defaultDays);
        this.defaultHours = new HashSet<>(defaultHours);
        this.defaultFieldCount = defaultFieldCount;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public UUID getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public CompetitionStatus getStatus() { return status; }
    public Set<CompetitionDay> getDefaultDays() { return defaultDays; }
    public Set<CompetitionHour> getDefaultHours() { return defaultHours; }
    public int getDefaultFieldCount() { return defaultFieldCount; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }

    public void setName(String name) { this.name = name; }
    public void setDescription(String description) { this.description = description; }
    public void setStatus(CompetitionStatus status) { this.status = status; }
    public void setDefaultDays(Set<CompetitionDay> defaultDays) { this.defaultDays = new HashSet<>(defaultDays); }
    public void setDefaultHours(Set<CompetitionHour> defaultHours) { this.defaultHours = new HashSet<>(defaultHours); }
    public void setDefaultFieldCount(int defaultFieldCount) { this.defaultFieldCount = defaultFieldCount; }
    public void setUpdatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; }
}
