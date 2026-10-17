package com.orchedule.venue.application.exception;
import com.orchedule.shared.exception.ConflictException;

public class VenueInUseException extends ConflictException {
    public VenueInUseException() {
        super("VENUE_IN_USE", "The venue contains fields or matches; deactivate it instead");
    }
}
