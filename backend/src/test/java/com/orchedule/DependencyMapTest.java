package com.orchedule;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;

class DependencyMapTest {

    @Test
    void printDependencyMap() {
        ApplicationModules modules = ApplicationModules.of(OrcheduleBackendApplication.class);

        modules.forEach(module -> {
            System.out.println("=== " + module.getIdentifier() + " ===");
            module.getDirectDependencies(modules).stream()
                    .forEach(dep -> System.out.println("  -> " + dep));
        });
    }
}