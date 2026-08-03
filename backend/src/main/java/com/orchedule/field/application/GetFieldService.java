package com.orchedule.field.application;

import com.orchedule.field.domain.Field;
import com.orchedule.field.domain.FieldRepository;
import com.orchedule.field.application.exception.FieldNotFoundException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class GetFieldService {

    private final FieldRepository fieldRepository;

    public GetFieldService(FieldRepository fieldRepository) {
        this.fieldRepository = fieldRepository;
    }

    @PreAuthorize("isAuthenticated()")
    public Field getById(UUID id) {
        return fieldRepository.findById(id)
                .orElseThrow(() -> new FieldNotFoundException(id));
    }

    @PreAuthorize("isAuthenticated()")
    public List<Field> getByVenue(UUID venueId) {
        return fieldRepository.findByVenueId(venueId);
    }

    @PreAuthorize("isAuthenticated()")
    public List<Field> getAllActive() {
        return fieldRepository.findAllActive();
    }
}
