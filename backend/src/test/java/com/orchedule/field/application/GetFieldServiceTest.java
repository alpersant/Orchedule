package com.orchedule.field.application;

import com.orchedule.field.application.exception.FieldNotFoundException;
import com.orchedule.field.domain.Field;
import com.orchedule.field.domain.FieldRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetFieldServiceTest {

    @Mock
    private FieldRepository fieldRepository;

    private GetFieldService getFieldService;

    @BeforeEach
    void setUp() {
        getFieldService = new GetFieldService(fieldRepository);
    }

    @Test
    void shouldReturnFieldWhenFound() {
        UUID id = UUID.randomUUID();
        Field field = Field.create(UUID.randomUUID(), "Field 1");
        when(fieldRepository.findById(id)).thenReturn(Optional.of(field));

        Field result = getFieldService.getById(id);

        assertThat(result).isEqualTo(field);
    }

    @Test
    void shouldThrowWhenFieldNotFound() {
        UUID id = UUID.randomUUID();
        when(fieldRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> getFieldService.getById(id))
                .isInstanceOf(FieldNotFoundException.class);
    }

    @Test
    void shouldReturnFieldsByVenue() {
        UUID venueId = UUID.randomUUID();
        Field field = Field.create(venueId, "Field 1");
        when(fieldRepository.findByVenueId(venueId)).thenReturn(List.of(field));

        List<Field> result = getFieldService.getByVenue(venueId);

        assertThat(result).containsExactly(field);
    }

    @Test
    void shouldReturnAllActiveFields() {
        Field field = Field.create(UUID.randomUUID(), "Field 1");
        when(fieldRepository.findAllActive()).thenReturn(List.of(field));

        List<Field> result = getFieldService.getAllActive();

        assertThat(result).containsExactly(field);
    }
}
