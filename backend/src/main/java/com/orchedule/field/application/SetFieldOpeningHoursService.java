package com.orchedule.field.application;

import com.orchedule.field.application.exception.FieldNotFoundException;
import com.orchedule.field.domain.Field;
import com.orchedule.field.domain.FieldRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Set;
import java.util.UUID;

@Service
public class SetFieldOpeningHoursService {

    private final FieldRepository fieldRepository;

    public SetFieldOpeningHoursService(FieldRepository fieldRepository) {
        this.fieldRepository = fieldRepository;
    }

    @Transactional
    @PreAuthorize("hasAnyRole('ADMIN', 'ORGANIZER')")
    public Field setOpeningHours(UUID fieldId, Set<DayOfWeek> openDays, Set<LocalTime> openHours) {
        Field field = fieldRepository.findById(fieldId)
                .orElseThrow(() -> new FieldNotFoundException(fieldId));
        field.changeOpeningHours(openDays, openHours);
        return fieldRepository.save(field);
    }
}
