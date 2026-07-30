package com.orchedule.field.infrastructure.jpa;

import com.orchedule.field.domain.FieldWeeklyAvailability;
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
class FieldWeeklyAvailabilityRepositoryAdapterTest {

    @Autowired
    private SpringDataFieldWeeklyAvailabilityRepository springDataRepository;

    private FieldWeeklyAvailabilityRepositoryJpaAdapter adapter;
    private UUID seasonId;
    private UUID fieldId;

    @BeforeEach
    void setUp() {
        adapter = new FieldWeeklyAvailabilityRepositoryJpaAdapter(springDataRepository);
        seasonId = UUID.randomUUID();
        fieldId = UUID.randomUUID();
    }

    @Test
    void shouldSaveAndFindByIdComponents() {
        FieldWeeklyAvailability availability = FieldWeeklyAvailability.create(seasonId, 3, fieldId, true);

        adapter.save(availability);
        Optional<FieldWeeklyAvailability> found =
                adapter.findBySeasonIdAndWeekNumberAndFieldId(seasonId, 3, fieldId);

        assertThat(found).isPresent();
        assertThat(found.get().isEnabled()).isTrue();
    }

    @Test
    void shouldFindAllAvailabilityForSeasonAndWeek() {
        UUID fieldB = UUID.randomUUID();
        adapter.save(FieldWeeklyAvailability.create(seasonId, 1, fieldId, true));
        adapter.save(FieldWeeklyAvailability.create(seasonId, 1, fieldB, false));
        adapter.save(FieldWeeklyAvailability.create(seasonId, 2, fieldId, true));

        List<FieldWeeklyAvailability> weekOne = adapter.findBySeasonIdAndWeekNumber(seasonId, 1);

        assertThat(weekOne).hasSize(2);
    }

    @Test
    void shouldFindAllAvailabilityForSeason() {
        adapter.save(FieldWeeklyAvailability.create(seasonId, 1, fieldId, true));
        adapter.save(FieldWeeklyAvailability.create(seasonId, 2, fieldId, true));

        List<FieldWeeklyAvailability> result = adapter.findBySeasonId(seasonId);

        assertThat(result).hasSize(2);
    }
}
