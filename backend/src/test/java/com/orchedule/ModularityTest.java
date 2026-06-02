package com.orchedule;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;
import org.springframework.modulith.docs.Documenter;

class ModularityTest {

    ApplicationModules modules = ApplicationModules.of(OrcheduleBackendApplication.class);

    @Test
    void verifiesModularStructure() {
        modules.verify();
    }

    @Test
    void documentsModules() {
        new Documenter(modules).writeDocumentation();
    }
}