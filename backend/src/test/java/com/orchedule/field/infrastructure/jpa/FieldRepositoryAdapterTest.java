package com.orchedule.field.infrastructure.jpa;

import com.orchedule.field.domain.Field;
import com.orchedule.field.domain.FieldStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class FieldRepositoryAdapterTest {

    @Autowired
    private SpringDataFieldRepository springDataFieldRepository;

    private FieldRepositoryJpaAdapter adapter;
    private UUID venueId;

    @BeforeEach
    void setUp() {
        adapter = new FieldRepositoryJpaAdapter(springDataFieldRepository);
        venueId = UUID.randomUUID();
    }

    @Test
    void shouldSaveAndRetrieveFieldById() {
        Field field = Field.create(venueId, "Field 1");

        Field saved = adapter.save(field);
        Optional<Field> found = adapter.findById(saved.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Field 1");
        assertThat(found.get().getVenueId()).isEqualTo(venueId);
    }

    @Test
    void shouldFindFieldsByVenueId() {
        adapter.save(Field.create(venueId, "Field 1"));
        adapter.save(Field.create(venueId, "Field 2"));
        adapter.save(Field.create(UUID.randomUUID(), "Field Other Venue"));

        List<Field> result = adapter.findByVenueId(venueId);

        assertThat(result).hasSize(2);
    }

    @Test
    void shouldFindOnlyActiveFields() {
        Field active = Field.create(venueId, "Active Field");
        Field inactive = Field.create(venueId, "Inactive Field");
        inactive.deactivate();

        adapter.save(active);
        adapter.save(inactive);

        List<Field> result = adapter.findAllActive();

        assertThat(result).extracting(Field::getName).containsExactly("Active Field");
        assertThat(result).allMatch(f -> f.getStatus() == FieldStatus.ACTIVE);
    }

    @Test
    void shouldDetectExistingNameInVenue() {
        adapter.save(Field.create(venueId, "Field 1"));

        boolean exists = adapter.existsByVenueIdAndName(venueId, "Field 1");
        boolean notExists = adapter.existsByVenueIdAndName(venueId, "Field 2");

        assertThat(exists).isTrue();
        assertThat(notExists).isFalse();
    }
}
