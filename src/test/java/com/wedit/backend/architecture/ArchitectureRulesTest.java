package com.wedit.backend.architecture;

import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RestController;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

@DisplayName("아키텍처 가드레일 테스트")
class ArchitectureRulesTest {

    private final com.tngtech.archunit.core.domain.JavaClasses importedClasses = new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages("com.wedit.backend");

    @Test
    @DisplayName("controller는 repository에 직접 의존하지 않는다")
    void controllersShouldNotDependOnRepositories() {
        noClasses()
                .that().resideInAPackage("com.wedit.backend..controller..")
                .should().dependOnClassesThat().resideInAPackage("com.wedit.backend..repository..")
                .check(importedClasses);
    }

    @Test
    @DisplayName("service와 repository 외 패키지는 repository에 직접 의존하지 않는다")
    void onlyServicesAndRepositoriesShouldDependOnRepositories() {
        noClasses()
                .that().resideOutsideOfPackages("..service..", "..repository..")
                .should().dependOnClassesThat().resideInAPackage("com.wedit.backend..repository..")
                .check(importedClasses);
    }

    @Test
    @DisplayName("repository 패키지의 타입은 @Repository를 사용한다")
    void repositoriesShouldBeAnnotated() {
        classes()
                .that().resideInAPackage("..repository..")
                .should().beAnnotatedWith(Repository.class)
                .check(importedClasses);
    }

    @Test
    @DisplayName("service 패키지의 타입은 @Service를 사용한다")
    void servicesShouldBeAnnotated() {
        classes()
                .that().resideInAPackage("..service..")
                .should().beAnnotatedWith(Service.class)
                .check(importedClasses);
    }

    @Test
    @DisplayName("controller 패키지의 타입은 @RestController를 사용한다")
    void controllersShouldBeAnnotated() {
        classes()
                .that().resideInAPackage("..controller..")
                .should().beAnnotatedWith(RestController.class)
                .check(importedClasses);
    }

    @Test
    @DisplayName("entity는 controller를 참조하지 않는다")
    void entitiesShouldNotDependOnControllers() {
        noClasses()
                .that().resideInAPackage("com.wedit.backend..entity..")
                .should().dependOnClassesThat().resideInAPackage("com.wedit.backend..controller..")
                .check(importedClasses);
    }
}
