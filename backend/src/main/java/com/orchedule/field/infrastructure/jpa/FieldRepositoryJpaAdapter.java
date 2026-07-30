package com.orchedule.field.infrastructure.jpa;

import com.orchedule.field.domain.Field;
import com.orchedule.field.domain.FieldRepository;
import com.orchedule.field.domain.FieldStatus;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class FieldRepositoryJpaAdapter implements FieldRepository {

    private final SpringDataFieldRepository springDataFieldRepository;

    public FieldRepositoryJpaAdapter(SpringDataFieldRepository springDataFieldRepository) {
        this.springDataFieldRepository = springDataFieldRepository;
    }

    @Override
    public Field save(Field field) {
        FieldEntity entity = new FieldEntity(
                field.getId(), field.getVenueId(), field.getName(),
                field.getStatus(), field.getCreatedAt(), field.getUpdatedAt());
        FieldEntity saved = springDataFieldRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<Field> findById(UUID id) {
        return springDataFieldRepository.findById(id).map(this::toDomain);
    }

    @Override
    public List<Field> findByVenueId(UUID venueId) {
        return springDataFieldRepository.findByVenueId(venueId).stream()
                .map(this::toDomain).toList();
    }

    @Override
    public List<Field> findAllActive() {
        return springDataFieldRepository.findByStatus(FieldStatus.ACTIVE).stream()
                .map(this::toDomain).toList();
    }

    @Override
    public boolean existsByVenueIdAndName(UUID venueId, String name) {
        return springDataFieldRepository.existsByVenueIdAndName(venueId, name);
    }

    private Field toDomain(FieldEntity entity) {
        return new Field(entity.getId(), entity.getVenueId(), entity.getName(),
                entity.getStatus(), entity.getCreatedAt(), entity.getUpdatedAt());
    }
}
