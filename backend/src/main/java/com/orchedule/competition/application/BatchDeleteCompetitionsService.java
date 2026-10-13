package com.orchedule.competition.application;

import com.orchedule.competition.api.dto.BatchDeleteCompetitionsResponse;
import com.orchedule.competition.application.exception.CompetitionNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.UUID;

@Service
public class BatchDeleteCompetitionsService {

    private final DeleteCompetitionService deleteCompetitionService;

    public BatchDeleteCompetitionsService(DeleteCompetitionService deleteCompetitionService) {
        this.deleteCompetitionService = deleteCompetitionService;
    }

    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public BatchDeleteCompetitionsResponse deleteAll(List<UUID> requestedIds) {
        List<UUID> uniqueIds = new ArrayList<>(new LinkedHashSet<>(requestedIds));

        List<UUID> deletedIds = new ArrayList<>();
        List<BatchDeleteCompetitionsResponse.BatchDeleteFailure> failures = new ArrayList<>();

        for (UUID id : uniqueIds) {
            try {
                deleteCompetitionService.delete(id);
                deletedIds.add(id);
            } catch (CompetitionNotFoundException exception) {
                failures.add(new BatchDeleteCompetitionsResponse.BatchDeleteFailure(
                        id, "COMPETITION_NOT_FOUND"));
            }
        }

        return new BatchDeleteCompetitionsResponse(deletedIds, failures);
    }
}
