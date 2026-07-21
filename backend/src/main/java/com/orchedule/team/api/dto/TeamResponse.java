package com.orchedule.team.api.dto;

import java.util.UUID;

public record TeamResponse(
        UUID id,
        String name,
        boolean active
) {}