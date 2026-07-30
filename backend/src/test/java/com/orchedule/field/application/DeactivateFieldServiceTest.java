package com.orchedule.field.application;

import com.orchedule.field.application.exception.FieldNotFoundException;
import com.orchedule.field.domain.Field;
import com.orchedule.field.domain.FieldRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeactivateFieldServiceTest {

    @Mock
    private FieldRepository fieldRepository;

    private DeactivateFieldService deactivateFieldService;

    @BeforeEach
    void setUp() {
        deactivateFieldService = new DeactivateFieldService(fieldRepository);
    }

    @Test
    void shouldDeactivateActiveField() {
        UUID id = UUID.randomUUID();
        Field field = Field.create(UUID.randomUUID(), "Field 1");
        when(fieldRepository.findById(id)).thenReturn(Optional.of(field));
        when(fieldRepository.save(any(Field.class))).thenAnswer(inv -> inv.getArgument(0));

        Field result = deactivateFieldService.deactivate(id);

        assertThat(result.isActive()).isFalse();
    }

    @Test
    void shouldThrowWhenFieldNotFound() {
        UUID id = UUID.randomUUID();
        when(fieldRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> deactivateFieldService.deactivate(id))
                .isInstanceOf(FieldNotFoundException.class);
    }
}
