package com.orchedule.field.domain;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class FieldTest {

    @Test
    void shouldCreateFieldAsActiveByDefault() {
        UUID venueId = UUID.randomUUID();
        Field field = Field.create(venueId, "Field 1");

        assertThat(field.getId()).isNotNull();
        assertThat(field.getVenueId()).isEqualTo(venueId);
        assertThat(field.getName()).isEqualTo("Field 1");
        assertThat(field.getStatus()).isEqualTo(FieldStatus.ACTIVE);
        assertThat(field.isActive()).isTrue();
        assertThat(field.getCreatedAt()).isNotNull();
        assertThat(field.getUpdatedAt()).isNotNull();
    }

    @Test
    void shouldDeactivateAndReactivateField() {
        Field field = Field.create(UUID.randomUUID(), "Field 1");

        field.deactivate();
        assertThat(field.getStatus()).isEqualTo(FieldStatus.INACTIVE);
        assertThat(field.isActive()).isFalse();

        field.activate();
        assertThat(field.getStatus()).isEqualTo(FieldStatus.ACTIVE);
        assertThat(field.isActive()).isTrue();
    }

    @Test
    void shouldRenameFieldAndUpdateTimestamp() {
        Field field = Field.create(UUID.randomUUID(), "Field 1");
        var originalUpdatedAt = field.getUpdatedAt();

        field.rename("Field Renamed");

        assertThat(field.getName()).isEqualTo("Field Renamed");
        assertThat(field.getUpdatedAt()).isAfterOrEqualTo(originalUpdatedAt);
    }
}
