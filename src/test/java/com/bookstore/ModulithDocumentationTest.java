package com.bookstore;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;
import org.springframework.modulith.docs.Documenter;

public class ModulithDocumentationTest {

    ApplicationModules modules = ApplicationModules.of(BookstoreApplication.class);

    @Test
    void generateDocumentation(){
        new Documenter(modules)
                .writeModulesAsPlantUml(Documenter.DiagramOptions.defaults())
                .writeIndividualModulesAsPlantUml(Documenter.DiagramOptions.defaults())
                .writeModuleCanvases(Documenter.CanvasOptions.defaults());
    }
}
