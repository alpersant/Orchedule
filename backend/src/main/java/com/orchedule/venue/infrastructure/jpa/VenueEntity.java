package com.orchedule.venue.infrastructure.jpa;

import com.orchedule.venue.domain.Venue;
import com.orchedule.venue.domain.VenueStatus;
import jakarta.persistence.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "venue",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_venue_name", columnNames = "name")
        },
        indexes = {
                @Index(name = "idx_venue_status", columnList = "status"),
                @Index(name = "idx_venue_active", columnList = "active"),
                @Index(name = "idx_venue_city", columnList = "city")
        }
)
public class VenueEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "name", nullable = false, length = 120)
    private String name;

    @Column(name = "city", nullable = false, length = 100)
    private String city;

    @Column(name = "address", length = 255)
    private String address;

    @Column(name = "capacity")
    private Integer capacity;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private VenueStatus status;

    @Column(name = "active", nullable = false)
    private boolean active;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    protected VenueEntity() {
    }

    public static VenueEntity create(String name,
                                     String city,
                                     String address,
                                     Integer capacity,
                                     VenueStatus status,
                                     boolean active) {
        VenueEntity entity = new VenueEntity();
        OffsetDateTime now = OffsetDateTime.now();

        entity.id = UUID.randomUUID();
        entity.name = name;
        entity.city = city;
        entity.address = address;
        entity.capacity = capacity;
        entity.status = status;
        entity.active = active;
        entity.createdAt = now;
        entity.updatedAt = now;

        return entity;
    }

    public void update(String name,
                       String city,
                       String address,
                       Integer capacity,
                       VenueStatus status,
                       boolean active) {
        this.name = name;
        this.city = city;
        this.address = address;
        this.capacity = capacity;
        this.status = status;
        this.active = active;
        this.updatedAt = OffsetDateTime.now();
    }

    public Venue toDomain() {
        return new Venue(
                id,
                name,
                city,
                address,
                capacity,
                status,
                active,
                createdAt,
                updatedAt
        );
    }

    public UUID getId() {
        return id;
    }
}
