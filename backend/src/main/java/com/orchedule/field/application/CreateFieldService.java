package com.orchedule.field.application;

import com.orchedule.field.domain.Field;
import com.orchedule.field.domain.FieldRepository;
import com.orchedule.field.domain.FieldValidator;
import com.orchedule.field.application.exception.InvalidFieldException;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class CreateFieldService {

    private final FieldRepository fieldRepository;

    public CreateFieldService(FieldRepository fieldRepository) {
        this.fieldRepository = fieldRepository;
    }

    public Field create(UUID venueId, String name) {
        FieldValidator.validateName(name);
        if (fieldRepository.existsByVenueIdAndName(venueId, name)) {
            throw new InvalidFieldException("A field with this name already exists in the venue");
        }
        Field field = Field.create(venueId, name);
        return fieldRepository.save(field);
    }
}
