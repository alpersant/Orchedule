package com.orchedule.field.application;

import com.orchedule.field.application.exception.FieldNotFoundException;
import com.orchedule.field.application.exception.InvalidFieldException;
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
class UpdateFieldServiceTest {

    @Mock
    private FieldRepository fieldRepository;

    private UpdateFieldService updateFieldService;

    @BeforeEach
    void setUp() {
        updateFieldService = new UpdateFieldService(fieldRepository);
    }

    @Test
    void shouldRenameExistingField() {
        UUID id = UUID.randomUUID();
        Field field = Field.create(UUID.randomUUID(), "Old Name");
        when(fieldRepository.findById(id)).thenReturn(Optional.of(field));
        when(fieldRepository.save(any(Field.class))).thenAnswer(inv -> inv.getArgument(0));

        Field result = updateFieldService.rename(id, "New Name");

        assertThat(result.getName()).isEqualTo("New Name");
    }

    @Test
    void shouldThrowWhenFieldNotFound() {
        UUID id = UUID.randomUUID();
        when(fieldRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> updateFieldService.rename(id, "New Name"))
                .isInstanceOf(FieldNotFoundException.class);
    }

    @Test
    void shouldRejectBlankNewName() {
        UUID id = UUID.randomUUID();

        assertThatThrownBy(() -> updateFieldService.rename(id, ""))
                .isInstanceOf(InvalidFieldException.class);
    }
}
