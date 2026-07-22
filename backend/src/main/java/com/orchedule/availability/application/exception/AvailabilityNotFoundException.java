package com.orchedule.availability.application.exception;

import com.orchedule.shared.exception.NotFoundException;
import java.util.UUID;

public class AvailabilityNotFoundException extends NotFoundException {
    public AvailabilityNotFoundException(UUID availabilityId) { super("AVAILABILITY_NOT_FOUND", "Availability not found: " + availabilityId); }
}
