package com.orchedule.field.domain;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class FieldWeeklyAvailabilityTest {

    @Test
    void shouldCreateEnabledByDefaultWhenRequested() {
        UUID seasonId = UUID.randomUUID();
        UUID fieldId = UUID.randomUUID();

        FieldWeeklyAvailability availability = FieldWeeklyAvailability.create(seasonId, 3, fieldId, true);

        assertThat(availability.getId()).isNotNull();
        assertThat(availability.getSeasonId()).isEqualTo(seasonId);
        assertThat(availability.getWeekNumber()).isEqualTo(3);
        assertThat(availability.getFieldId()).isEqualTo(fieldId);
        assertThat(availability.isEnabled()).isTrue();
    }

    @Test
    void shouldToggleEnabledState() {
        FieldWeeklyAvailability availability =
                FieldWeeklyAvailability.create(UUID.randomUUID(), 1, UUID.randomUUID(), true);

        availability.disable();
        assertThat(availability.isEnabled()).isFalse();

        availability.enable();
        assertThat(availability.isEnabled()).isTrue();
    }
}
