package com.dfbs.app;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

public class ArchitectureRulesTest {

    private static final String BASE = "com.dfbs.app..";

    @Test
    void interfaces_must_not_be_accessed_by_application_or_modules() {
        JavaClasses classes = new ClassFileImporter().importPackages(BASE);

        ArchRule rule1 = noClasses()
                .that().resideInAnyPackage("com.dfbs.app.application..", "com.dfbs.app.modules..", "com.dfbs.app.platform..")
                .should().dependOnClassesThat().resideInAnyPackage("com.dfbs.app.interfaces..");

        rule1.check(classes);
    }

    @Test
    void application_must_not_be_accessed_by_modules_or_platform() {
        JavaClasses classes = new ClassFileImporter().importPackages(BASE);

        ArchRule rule = noClasses()
                .that().resideInAnyPackage("com.dfbs.app.modules..", "com.dfbs.app.platform..")
                .should().dependOnClassesThat().resideInAnyPackage("com.dfbs.app.application..");

        rule.check(classes);
    }

    @Test
    void modules_must_not_depend_on_platform_interfaces_application() {
        JavaClasses classes = new ClassFileImporter().importPackages(BASE);

        ArchRule rule = noClasses()
                .that().resideInAnyPackage("com.dfbs.app.modules..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        "com.dfbs.app.interfaces..",
                        "com.dfbs.app.application.."
                );

        rule.check(classes);
    }
}
