package com.orchedule;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;
import org.springframework.modulith.docs.Documenter;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ModularityTest {

    ApplicationModules modules = ApplicationModules.of(OrcheduleBackendApplication.class);

    @Test
    void verifiesModularStructure() {
        modules.verify();
    }

    @Test
    void documentsModules() {
        new Documenter(modules).writeDocumentation();

        assertTrue(modules.stream().count() > 0,
                "La aplicación debe contener al menos un módulo documentado");
    }
}