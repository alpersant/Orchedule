package com.orchedule.season.application;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.dao.DataIntegrityViolationException;
import com.orchedule.season.application.exception.*;
@Service
public class BatchDeleteSeasonsService {
    private final DeleteSeasonService deleteService;
    public BatchDeleteSeasonsService(DeleteSeasonService deleteService) { this.deleteService = deleteService; }
    @Transactional(propagation=Propagation.NOT_SUPPORTED)
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZER')")
    public BatchDeleteSeasonsResult delete(List<UUID> ids) {
        if (ids == null || ids.isEmpty() || ids.size()>100 || ids.contains(null)) {
            throw new InvalidSeasonException("Provide between 1 and 100 non-null IDs");
        }
        var deleted = new ArrayList<UUID>();
        var failures = new ArrayList<BatchDeleteSeasonsResult.Failure>();
        for (UUID id : new LinkedHashSet<>(ids)) {
            try { deleteService.delete(id); deleted.add(id); }
            catch (SeasonNotFoundException ex) { failures.add(new BatchDeleteSeasonsResult.Failure(id,"SEASON_NOT_FOUND")); }
            catch (InvalidSeasonException ex) { failures.add(new BatchDeleteSeasonsResult.Failure(id,"AVAILABILITY_DEPENDENCY")); }
            catch (DataIntegrityViolationException ex) { failures.add(new BatchDeleteSeasonsResult.Failure(id,"DEPENDENCY_CONFLICT")); }
        }
        return new BatchDeleteSeasonsResult(List.copyOf(deleted), List.copyOf(failures));
    }
}
