package com.orchedule.field.application;

import com.orchedule.field.application.exception.InvalidFieldException;
import com.orchedule.field.domain.Field;
import com.orchedule.field.domain.FieldRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateFieldServiceTest {

    @Mock
    private FieldRepository fieldRepository;

    private CreateFieldService createFieldService;

    @BeforeEach
    void setUp() {
        createFieldService = new CreateFieldService(fieldRepository);
    }

    @Test
    void shouldCreateFieldWhenNameIsUniqueInVenue() {
        UUID venueId = UUID.randomUUID();
        when(fieldRepository.existsByVenueIdAndName(venueId, "Field 1")).thenReturn(false);
        when(fieldRepository.save(any(Field.class))).thenAnswer(inv -> inv.getArgument(0));

        Field result = createFieldService.create(venueId, "Field 1");

        assertThat(result.getName()).isEqualTo("Field 1");
        assertThat(result.getVenueId()).isEqualTo(venueId);

        ArgumentCaptor<Field> captor = ArgumentCaptor.forClass(Field.class);
        verify(fieldRepository).save(captor.capture());
        assertThat(captor.getValue().isActive()).isTrue();
    }

    @Test
    void shouldRejectDuplicateNameInSameVenue() {
        UUID venueId = UUID.randomUUID();
        when(fieldRepository.existsByVenueIdAndName(venueId, "Field 1")).thenReturn(true);

        assertThatThrownBy(() -> createFieldService.create(venueId, "Field 1"))
                .isInstanceOf(InvalidFieldException.class)
                .hasMessageContaining("already exists");

        verify(fieldRepository, never()).save(any());
    }

    @Test
    void shouldRejectBlankName() {
        UUID venueId = UUID.randomUUID();

        assertThatThrownBy(() -> createFieldService.create(venueId, "  "))
                .isInstanceOf(InvalidFieldException.class);

        verify(fieldRepository, never()).save(any());
    }
}
