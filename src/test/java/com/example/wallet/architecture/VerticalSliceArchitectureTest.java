package com.example.wallet.architecture;

import static com.example.wallet.architecture.Architecture.FEATURE_SLICE;
import static com.example.wallet.architecture.Architecture.FEATURE_SLICES;
import static com.example.wallet.architecture.Architecture.ROOT;
import static com.tngtech.archunit.lang.conditions.ArchConditions.be;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import com.tngtech.archunit.library.dependencies.Slice;
import java.util.function.Predicate;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RestController;

/**
 * Vertical Slice Architecture: every endpoint is a self-contained package {@code <module>.<slice>} with its own input
 * port, interactor, controller and wiring, and slices never reach into one another.
 */
@AnalyzeClasses(packages = ROOT, importOptions = ImportOption.DoNotIncludeTests.class)
class VerticalSliceArchitectureTest {

    @ArchTest
    static final ArchRule slicesAreIndependentOfEachOther = slices()
            .assignedFrom(FEATURE_SLICES)
            .should().notDependOnEachOther()
            .because("a slice can change, be tested or be removed without touching another slice");

    @ArchTest
    static final ArchRule everySliceIsACompleteUseCase = slices()
            .assignedFrom(FEATURE_SLICES)
            .should(containTheFullStackOfOneUseCase());

    @ArchTest
    static final ArchRule usersModuleDoesNotDependOnWallets = noClasses()
            .that().resideInAPackage(ROOT + ".users..")
            .should().dependOnClassesThat().resideInAPackage(ROOT + ".wallets..");

    @ArchTest
    static final ArchRule walletsModuleDoesNotDependOnUsers = noClasses()
            .that().resideInAPackage(ROOT + ".wallets..")
            .should().dependOnClassesThat().resideInAPackage(ROOT + ".users..")
            .because("wallets know their owner only by id, taken from the JWT subject");

    @ArchTest
    static final ArchRule sharedKernelDoesNotDependOnFeatures = noClasses()
            .that().resideInAPackage(ROOT + ".shared..")
            .should().dependOnClassesThat().resideInAnyPackage(ROOT + ".users..", ROOT + ".wallets..")
            .because("shared holds only what every module needs, and must stay minimal");

    @ArchTest
    static final ArchRule moduleSupportCodeDoesNotDependOnSlices = noClasses()
            .that().resideInAnyPackage("..domain..", "..persistence..", "..web..", "..security..")
            .should().dependOnClassesThat(FEATURE_SLICE)
            .because("slices build on the module's domain and adapters, never the other way round");

    @ArchTest
    static final ArchRule controllersLiveInTheirSlice = classes()
            .that().areAnnotatedWith(RestController.class)
            .should(be(FEATURE_SLICE))
            .andShould().haveSimpleNameEndingWith("Controller");

    @ArchTest
    static final ArchRule sliceWiringIsPackagePrivate = classes()
            .that(FEATURE_SLICE)
            .and().areAnnotatedWith(Configuration.class)
            .should().notBePublic()
            .andShould().haveSimpleNameEndingWith("Config")
            .because("only the slice itself knows how its interactor is assembled");

    @ArchTest
    static final ArchRule packagesAreFreeOfCycles = slices()
            .matching(ROOT + ".(**)")
            .should().beFreeOfCycles();

    private static ArchCondition<Slice> containTheFullStackOfOneUseCase() {
        return new ArchCondition<>("contain one *UseCase port, its *Interactor, a @RestController and a @Configuration") {
            @Override
            public void check(Slice slice, ConditionEvents events) {
                check(slice, events, "exactly one *UseCase interface", count(slice, c -> c.isInterface()
                        && c.getSimpleName().endsWith("UseCase")) == 1);
                check(slice, events, "exactly one *Interactor", count(slice, c -> c.getSimpleName()
                        .endsWith("Interactor")) == 1);
                check(slice, events, "a @RestController", count(slice, c -> c.isAnnotatedWith(RestController.class))
                        >= 1);
                check(slice, events, "a @Configuration", count(slice, c -> c.isAnnotatedWith(Configuration.class))
                        == 1);
            }

            private void check(Slice slice, ConditionEvents events, String requirement, boolean satisfied) {
                String message = "slice %s %s %s".formatted(
                        slice.getDescription(), satisfied ? "has" : "does not have", requirement);
                events.add(new SimpleConditionEvent(slice, satisfied, message));
            }

            private long count(Slice slice, Predicate<JavaClass> predicate) {
                return slice.stream().filter(predicate).count();
            }
        };
    }
}
