package com.orchedule;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;
import org.springframework.modulith.docs.Documenter;

import static org.assertj.core.api.Assertions.assertThat;

class ModularityTest {

    ApplicationModules modules = ApplicationModules.of(OrcheduleBackendApplication.class);

    @Test
    void verifiesModularStructure() {
        modules.verify();

        assertThat(modules.stream().count()).isPositive();
    }

    @Test
    void documentsModules() {
        new Documenter(modules).writeDocumentation();

        assertThat(modules.stream().count()).isPositive();
    }
}