package com.example.wallet.architecture;

import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAPackage;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAnyPackage;
import static com.tngtech.archunit.core.domain.properties.CanBeAnnotated.Predicates.annotatedWith;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.library.dependencies.SliceAssignment;
import com.tngtech.archunit.library.dependencies.SliceIdentifier;
import java.util.Set;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;

/**
 * Vocabulary shared by the architecture tests: which classes form each Clean Architecture layer and how the code is
 * cut into vertical slices.
 */
final class Architecture {

    static final String ROOT = "com.example.wallet";

    static final Set<String> MODULES = Set.of("users", "wallets");

    /** Sub-packages of a module that hold code shared by its slices; every other sub-package is a feature slice. */
    static final Set<String> MODULE_SUPPORT_PACKAGES = Set.of("domain", "persistence", "security", "web");

    static final String[] FRAMEWORK_PACKAGES = {
        "org.springframework..", "jakarta..", "org.hibernate..", "tools.jackson..", "com.fasterxml.."
    };

    /** Entities: aggregates, value objects, domain exceptions and the output ports they own. */
    static final DescribedPredicate<JavaClass> DOMAIN = resideInAPackage("..domain..").as("domain");

    /**
     * Use cases: input port, its command or query, the interactor, and the output ports and values that belong to a
     * single slice (the login {@code TokenIssuer} and {@code AccessToken}).
     */
    static final DescribedPredicate<JavaClass> USE_CASES = DescribedPredicate.describe(
            "use cases",
            javaClass -> isInFeatureSlice(javaClass)
                    && (topLevelNameEndsWith(javaClass, "UseCase", "Interactor", "Command", "Query")
                            || Set.of("TokenIssuer", "AccessToken").contains(topLevelSimpleName(javaClass))));

    /** Frameworks and drivers: Spring Boot entry point, wiring classes and the security configuration. */
    static final DescribedPredicate<JavaClass> FRAMEWORKS = annotatedWith(SpringBootApplication.class)
            .or(annotatedWith(Configuration.class))
            .or(resideInAPackage(ROOT + ".shared.security.."))
            .as("frameworks and drivers");

    /** Interface adapters: everything else (controllers, request/response records, persistence and crypto adapters). */
    static final DescribedPredicate<JavaClass> ADAPTERS = resideInAPackage(ROOT + "..")
            .and(DescribedPredicate.not(DOMAIN))
            .and(DescribedPredicate.not(USE_CASES))
            .and(DescribedPredicate.not(FRAMEWORKS))
            .as("interface adapters");

    static final DescribedPredicate<JavaClass> FEATURE_SLICE =
            DescribedPredicate.describe("a feature slice", Architecture::isInFeatureSlice);

    static final DescribedPredicate<JavaClass> OUTSIDE_APPLICATION =
            DescribedPredicate.not(resideInAnyPackage(ROOT + ".."));

    /** Assigns every class of a feature slice (e.g. {@code wallets.deposit}) to that slice and ignores the rest. */
    static final SliceAssignment FEATURE_SLICES = new SliceAssignment() {
        @Override
        public SliceIdentifier getIdentifierOf(JavaClass javaClass) {
            return isInFeatureSlice(javaClass)
                    ? SliceIdentifier.of(moduleOf(javaClass) + "." + slicePackageOf(javaClass))
                    : SliceIdentifier.ignore();
        }

        @Override
        public String getDescription() {
            return "feature slices " + ROOT + ".<module>.<slice>";
        }
    };

    private Architecture() {}

    static boolean isInFeatureSlice(JavaClass javaClass) {
        String[] parts = relativePackage(javaClass);
        return parts.length >= 2 && MODULES.contains(parts[0]) && !MODULE_SUPPORT_PACKAGES.contains(parts[1]);
    }

    static String moduleOf(JavaClass javaClass) {
        return relativePackage(javaClass)[0];
    }

    static String slicePackageOf(JavaClass javaClass) {
        return relativePackage(javaClass)[1];
    }

    static String topLevelSimpleName(JavaClass javaClass) {
        String nested = javaClass.getName().substring(javaClass.getPackageName().length() + 1);
        return nested.split("\\$")[0];
    }

    private static boolean topLevelNameEndsWith(JavaClass javaClass, String... suffixes) {
        String name = topLevelSimpleName(javaClass);
        for (String suffix : suffixes) {
            if (name.endsWith(suffix)) {
                return true;
            }
        }
        return false;
    }

    private static String[] relativePackage(JavaClass javaClass) {
        String pkg = javaClass.getPackageName();
        if (!pkg.startsWith(ROOT + ".")) {
            return new String[0];
        }
        return pkg.substring(ROOT.length() + 1).split("\\.");
    }
}
