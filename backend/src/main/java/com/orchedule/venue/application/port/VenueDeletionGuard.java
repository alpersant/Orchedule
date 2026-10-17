package com.orchedule.venue.application.port;
import java.util.UUID;

public interface VenueDeletionGuard {
    void lockAndAssertDeletable(UUID venueId);
}
