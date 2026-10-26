package com.example.wallet.architecture;

import static com.example.wallet.architecture.Architecture.ADAPTERS;
import static com.example.wallet.architecture.Architecture.DOMAIN;
import static com.example.wallet.architecture.Architecture.FRAMEWORKS;
import static com.example.wallet.architecture.Architecture.FRAMEWORK_PACKAGES;
import static com.example.wallet.architecture.Architecture.ROOT;
import static com.example.wallet.architecture.Architecture.USE_CASES;
import static com.example.wallet.architecture.Architecture.topLevelSimpleName;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAPackage;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noMethods;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;

import com.example.wallet.shared.domain.DomainException;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import jakarta.persistence.Entity;
import org.springframework.data.repository.Repository;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;

/**
 * Clean Architecture: the dependency rule (source code dependencies point inwards only) and the conventions that keep
 * the entities and use cases free of frameworks.
 */
@AnalyzeClasses(packages = ROOT, importOptions = ImportOption.DoNotIncludeTests.class)
class CleanArchitectureTest {

    @ArchTest
    static final ArchRule dependencyRulePointsInwards = layeredArchitecture()
            .consideringOnlyDependenciesInLayers()
            .ensureAllClassesAreContainedInArchitecture()
            .layer("Entities").definedBy(DOMAIN)
            .layer("Use Cases").definedBy(USE_CASES)
            .layer("Interface Adapters").definedBy(ADAPTERS)
            .layer("Frameworks & Drivers").definedBy(FRAMEWORKS)
            .whereLayer("Frameworks & Drivers").mayOnlyBeAccessedByLayers("Interface Adapters")
            .whereLayer("Interface Adapters").mayOnlyBeAccessedByLayers("Frameworks & Drivers")
            .whereLayer("Use Cases").mayOnlyBeAccessedByLayers("Interface Adapters", "Frameworks & Drivers")
            .whereLayer("Entities")
            .mayOnlyBeAccessedByLayers("Use Cases", "Interface Adapters", "Frameworks & Drivers")
            .because("in Clean Architecture source code dependencies may only point inwards");

    @ArchTest
    static final ArchRule entitiesDependOnlyOnTheDomainAndTheJdk = classes()
            .that().resideInAPackage("..domain..")
            .should().onlyDependOnClassesThat().resideInAnyPackage("..domain..", "java..")
            .because("entities are the innermost circle and must not know any other code");

    @ArchTest
    static final ArchRule useCasesAreFreeOfFrameworks = noClasses()
            .that(USE_CASES)
            .should().dependOnClassesThat().resideInAnyPackage(FRAMEWORK_PACKAGES)
            .because("use cases are plain Java and are wired by @Configuration classes");

    @ArchTest
    static final ArchRule interactorsDependOnPortsNotOnAdapters = noClasses()
            .that().haveSimpleNameEndingWith("Interactor")
            .should().dependOnClassesThat().resideInAnyPackage("..persistence..", "..security..", "..web..")
            .orShould().dependOnClassesThat().areAnnotatedWith(org.springframework.stereotype.Repository.class)
            .orShould().dependOnClassesThat().areAnnotatedWith(Component.class)
            .because("interactors reach the outside world only through output ports (dependency inversion)");

    @ArchTest
    static final ArchRule interactorsImplementTheirInputPort = classes()
            .that().haveSimpleNameEndingWith("Interactor")
            .should(implementTheUseCaseOfTheirSlice())
            .andShould().notBeAnnotatedWith(Component.class)
            .andShould().notBeAnnotatedWith(Service.class);

    @ArchTest
    static final ArchRule outputPortsAreInterfaces = classes()
            .that().resideInAPackage("..domain..")
            .and().haveSimpleNameEndingWith("Repository")
            .or().haveSimpleName("PasswordHasher")
            .or().haveSimpleName("TransactionHistory")
            .or().haveSimpleName("TokenIssuer")
            .should().beInterfaces()
            .because("output ports are abstractions owned by the inner circles and implemented by adapters");

    @ArchTest
    static final ArchRule controllersTalkToInputPortsOnly = noClasses()
            .that().areAnnotatedWith(RestController.class)
            .should().dependOnClassesThat().haveSimpleNameEndingWith("Interactor")
            .orShould().dependOnClassesThat().resideInAPackage("..persistence..")
            .orShould().dependOnClassesThat().areAssignableTo(Repository.class)
            .because("a controller only translates HTTP into a command and calls the input port");

    @ArchTest
    static final ArchRule controllersDoNotExposeEntities = methods()
            .that().areDeclaredInClassesThat().areAnnotatedWith(RestController.class)
            .should().notHaveRawReturnType(resideInAPackage("..domain.."))
            .because("the HTTP contract is made of response records, never of domain entities");

    @ArchTest
    static final ArchRule jpaEntitiesStayInsideThePersistenceAdapter = classes()
            .that().areAnnotatedWith(Entity.class)
            .should().resideInAPackage("..persistence..")
            .andShould().notBePublic()
            .because("persistence models are mapped to and from the domain inside the adapter");

    @ArchTest
    static final ArchRule springDataRepositoriesStayInsideThePersistenceAdapter = classes()
            .that().areInterfaces()
            .and().areAssignableTo(Repository.class)
            .should().resideInAPackage("..persistence..")
            .andShould().notBePublic();

    @ArchTest
    static final ArchRule transactionsAreAnAdapterConcern = noClasses()
            .that().resideOutsideOfPackage("..persistence..")
            .should().beAnnotatedWith(Transactional.class)
            .because("transaction boundaries belong to the persistence adapter, not to use cases");

    @ArchTest
    static final ArchRule noTransactionalMethodsOutsidePersistence = noMethods()
            .that().areDeclaredInClassesThat().resideOutsideOfPackage("..persistence..")
            .should().beAnnotatedWith(Transactional.class)
            .because("transaction boundaries belong to the persistence adapter, not to use cases");

    @ArchTest
    static final ArchRule domainErrorsAreDomainExceptions = classes()
            .that().resideInAPackage("..domain..")
            .and().areAssignableTo(RuntimeException.class)
            .should().beAssignableTo(DomainException.class)
            .because("GlobalExceptionHandler maps the DomainException categories to HTTP statuses");

    private static ArchCondition<JavaClass> implementTheUseCaseOfTheirSlice() {
        return new ArchCondition<>("implement a *UseCase interface of the same slice") {
            @Override
            public void check(JavaClass interactor, ConditionEvents events) {
                String expected = topLevelSimpleName(interactor).replaceFirst("Interactor$", "UseCase");
                boolean implementsPort = interactor.getRawInterfaces().stream()
                        .anyMatch(port -> port.getSimpleName().equals(expected)
                                && port.getPackageName().equals(interactor.getPackageName()));
                String message = "%s %s %s.%s".formatted(
                        interactor.getName(),
                        implementsPort ? "implements" : "does not implement",
                        interactor.getPackageName(),
                        expected);
                events.add(new SimpleConditionEvent(interactor, implementsPort, message));
            }
        };
    }
}
