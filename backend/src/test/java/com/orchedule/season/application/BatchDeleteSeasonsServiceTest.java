package com.orchedule.season.application;

import java.util.List;
import java.util.UUID;
import com.orchedule.season.application.exception.SeasonNotFoundException;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BatchDeleteSeasonsServiceTest {
    @Test void deduplicatesAndReturnsPartialResult() {
        var worker = mock(DeleteSeasonService.class);
        var service = new BatchDeleteSeasonsService(worker);
        UUID first = UUID.randomUUID();
        UUID missing = UUID.randomUUID();
        doThrow(new SeasonNotFoundException(missing)).when(worker).delete(missing);
        var result = service.delete(List.of(first, first, missing));
        assertEquals(List.of(first), result.deletedIds());
        assertEquals("SEASON_NOT_FOUND", result.failures().getFirst().reason());
        verify(worker, times(1)).delete(first);
        verify(worker, times(1)).delete(missing);
    }
    @Test void rejectsOversizedRequest() {
        var service = new BatchDeleteSeasonsService(mock(DeleteSeasonService.class));
        assertThrows(RuntimeException.class,
            () -> service.delete(java.util.Collections.nCopies(101, UUID.randomUUID())));
    }
}
