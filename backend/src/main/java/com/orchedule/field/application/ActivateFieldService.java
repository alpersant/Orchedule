package com.orchedule.field.application;

import com.orchedule.field.domain.Field;
import com.orchedule.field.domain.FieldRepository;
import com.orchedule.field.application.exception.FieldNotFoundException;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class ActivateFieldService {

    private final FieldRepository fieldRepository;

    public ActivateFieldService(FieldRepository fieldRepository) {
        this.fieldRepository = fieldRepository;
    }

    public Field activate(UUID id) {
        Field field = fieldRepository.findById(id)
                .orElseThrow(() -> new FieldNotFoundException(id));
        field.activate();
        return fieldRepository.save(field);
    }
}
