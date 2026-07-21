package com.orchedule.shared.api;

public record ApiValidationError(
        String field,
        String message
) {
}