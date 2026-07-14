package com.orchedule.identity.api.error;

import java.time.OffsetDateTime;

public record ApiError(
        String code,
        String message,
        OffsetDateTime timestamp
) {}