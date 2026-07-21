package com.orchedule.team.domain;

import java.time.OffsetDateTime;
import java.util.UUID;

public record Team(
        UUID id,
        String name,
        boolean active,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {}