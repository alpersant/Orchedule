package com.orchedule.season.application;
import com.orchedule.season.domain.SeasonRepository;
import com.orchedule.season.application.exception.SeasonNotFoundException;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
@Service
public class DeleteSeasonService {
    private final SeasonRepository repository;
    public DeleteSeasonService(SeasonRepository repository) { this.repository = repository; }
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZER')")
    public void delete(UUID id) {
        if (!repository.deleteById(id)) throw new SeasonNotFoundException(id);
    }
}
