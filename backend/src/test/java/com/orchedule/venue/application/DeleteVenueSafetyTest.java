package com.orchedule.venue.application;

import com.orchedule.venue.application.port.VenueDeletionGuard;
import com.orchedule.venue.application.exception.VenueInUseException;
import com.orchedule.venue.domain.VenueRepository;
import org.junit.jupiter.api.Test;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DeleteVenueSafetyTest {
    @Test
    void aReferencedVenueIsNotDeleted() {
        var repo = mock(VenueRepository.class);
        var guard = mock(VenueDeletionGuard.class);
        var id = UUID.randomUUID();
        doThrow(new VenueInUseException()).when(guard).lockAndAssertDeletable(id);
        assertThrows(VenueInUseException.class, () -> new DeleteVenueService(repo, guard).delete(id));
        verifyNoInteractions(repo);
    }
}
