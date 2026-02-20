package com.bookstore;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;
import org.springframework.modulith.test.ApplicationModuleTest;

@ApplicationModuleTest
public class ModulithArchitectureTest {

    ApplicationModules modules = ApplicationModules.of(BookstoreApplication.class);

    @Test
    void verifyModules() {
    	// This test will fail if the application context cannot be loaded, which includes checking for module dependencies and configurations.
        modules.verify();
    }
}
