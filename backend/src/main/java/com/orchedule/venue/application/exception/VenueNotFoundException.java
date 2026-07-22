package com.orchedule.venue.application.exception;

import com.orchedule.shared.exception.NotFoundException;

import java.util.UUID;

public class VenueNotFoundException extends NotFoundException {

    public VenueNotFoundException(UUID venueId) {
        super("VENUE_NOT_FOUND", "Venue not found: " + venueId);
    }
}
