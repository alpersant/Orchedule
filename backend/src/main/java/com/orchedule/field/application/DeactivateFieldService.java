package com.orchedule.field.application;

import com.orchedule.field.domain.Field;
import com.orchedule.field.domain.FieldRepository;
import com.orchedule.field.application.exception.FieldNotFoundException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class DeactivateFieldService {

    private final FieldRepository fieldRepository;

    public DeactivateFieldService(FieldRepository fieldRepository) {
        this.fieldRepository = fieldRepository;
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZER')")
    @Transactional
    public Field deactivate(UUID id) {
        Field field = fieldRepository.findById(id)
                .orElseThrow(() -> new FieldNotFoundException(id));
        field.deactivate();
        return fieldRepository.save(field);
    }
}
