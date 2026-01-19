package com.dfbs.app;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

public class MasterDataReadOnlyRulesTest {

    private static final String BASE = "com.dfbs.app..";

    // 主数据 Repo 的“类名正则”（用最兼容的方式）
    private static final String MASTERDATA_REPO_NAME_REGEX =
            "com\\.dfbs\\.app\\.modules\\.(customer|contract|product|machine|iccid)\\..*Repo";

    @Test
    void interfaces_must_not_depend_on_any_repo() {
        JavaClasses classes = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages(BASE);

        ArchRule rule = noClasses()
                .that().resideInAnyPackage("com.dfbs.app.interfaces..")
                .should().dependOnClassesThat().haveNameMatching(".*Repo");

        rule.check(classes);
    }

    @Test
    void only_masterdata_application_packages_may_depend_on_masterdata_repos() {
        JavaClasses classes = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages(BASE);

        // 允许依赖主数据 Repo 的包（只允许各自的 application 包）
        String[] allowed = new String[] {
                "com.dfbs.app.application.customer..",
                "com.dfbs.app.application.contract..",
                "com.dfbs.app.application.product..",
                "com.dfbs.app.application.machine..",
                "com.dfbs.app.application.iccid.."
        };

        // 禁止：除了 allowed 以外，任何地方依赖主数据 Repo（用正则匹配到 5 个 Repo）
        ArchRule rule = noClasses()
                .that().resideOutsideOfPackages(allowed)
                .should().dependOnClassesThat().haveNameMatching(MASTERDATA_REPO_NAME_REGEX);

        rule.check(classes);
    }
}
