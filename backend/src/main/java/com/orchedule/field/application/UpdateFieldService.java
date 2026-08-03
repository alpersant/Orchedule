package com.orchedule.field.application;

import com.orchedule.field.domain.Field;
import com.orchedule.field.domain.FieldRepository;
import com.orchedule.field.domain.FieldValidator;
import com.orchedule.field.application.exception.FieldNotFoundException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class UpdateFieldService {

    private final FieldRepository fieldRepository;

    public UpdateFieldService(FieldRepository fieldRepository) {
        this.fieldRepository = fieldRepository;
    }

    @Transactional
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZER')")
    public Field rename(UUID id, String newName) {
        FieldValidator.validateName(newName);
        Field field = fieldRepository.findById(id)
                .orElseThrow(() -> new FieldNotFoundException(id));
        field.rename(newName);
        return fieldRepository.save(field);
    }
}
