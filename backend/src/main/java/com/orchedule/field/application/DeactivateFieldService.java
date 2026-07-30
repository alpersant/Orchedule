package com.orchedule.field.application;

import com.orchedule.field.domain.Field;
import com.orchedule.field.domain.FieldRepository;
import com.orchedule.field.application.exception.FieldNotFoundException;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class DeactivateFieldService {

    private final FieldRepository fieldRepository;

    public DeactivateFieldService(FieldRepository fieldRepository) {
        this.fieldRepository = fieldRepository;
    }

    public Field deactivate(UUID id) {
        Field field = fieldRepository.findById(id)
                .orElseThrow(() -> new FieldNotFoundException("Field not found: " + id));
        field.deactivate();
        return fieldRepository.save(field);
    }
}
