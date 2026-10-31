package com.orchedule.season.application;

import com.orchedule.season.application.exception.InvalidSeasonException;
import com.orchedule.season.application.exception.SeasonNotFoundException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Deletes several seasons while preserving a partial-success result.
 *
 * <p>Each deletion is delegated to {@link DeleteSeasonService}, which runs in its
 * own transaction. Therefore, a failure for one season does not roll back the
 * seasons that were deleted successfully.</p>
 */
@Service
public class BatchDeleteSeasonsService {

    private static final int MAX_SEASON_IDS = 100;

    private final DeleteSeasonService deleteSeasonService;

    public BatchDeleteSeasonsService(DeleteSeasonService deleteSeasonService) {
        this.deleteSeasonService = deleteSeasonService;
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZER')")
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public BatchDeleteSeasonsResult delete(List<UUID> ids) {
        validate(ids);

        List<UUID> deletedIds = new ArrayList<>();
        List<BatchDeleteSeasonsResult.Failure> failures = new ArrayList<>();

        for (UUID id : uniqueIds(ids)) {
            try {
                deleteSeasonService.delete(id);
                deletedIds.add(id);
            } catch (SeasonNotFoundException exception) {
                failures.add(new BatchDeleteSeasonsResult.Failure(
                        id,
                        "SEASON_NOT_FOUND"
                ));
            } catch (InvalidSeasonException exception) {
                failures.add(new BatchDeleteSeasonsResult.Failure(
                        id,
                        "AVAILABILITY_DEPENDENCY"
                ));
            } catch (DataIntegrityViolationException exception) {
                failures.add(new BatchDeleteSeasonsResult.Failure(
                        id,
                        "DEPENDENCY_CONFLICT"
                ));
            }
        }

        return new BatchDeleteSeasonsResult(
                List.copyOf(deletedIds),
                List.copyOf(failures)
        );
    }

    private void validate(List<UUID> ids) {
        if (ids == null || ids.isEmpty() || ids.size() > MAX_SEASON_IDS) {
            throw new InvalidSeasonException(
                    "Provide between 1 and " + MAX_SEASON_IDS + " non-null IDs"
            );
        }

        boolean containsNull = ids.stream().anyMatch(java.util.Objects::isNull);

        if (containsNull) {
            throw new InvalidSeasonException(
                    "Provide between 1 and " + MAX_SEASON_IDS + " non-null IDs"
            );
        }
    }

    private Set<UUID> uniqueIds(List<UUID> ids) {
        return new LinkedHashSet<>(ids);
    }
}
