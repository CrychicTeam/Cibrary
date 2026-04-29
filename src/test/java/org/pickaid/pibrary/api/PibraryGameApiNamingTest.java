package org.pickaid.pibrary.api;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

class PibraryGameApiNamingTest {
    @Test
    void gameFacingApiNamesDoNotExposeServiceTerminology() throws IOException {
        assertDoesNotThrow(() -> Class.forName("org.pickaid.pibrary.api.entity.PiEntitySpatialIndexes"));
        assertDoesNotThrow(() -> Class.forName("org.pickaid.pibrary.api.targeting.PiTargeting"));
        assertDoesNotThrow(() -> Class.forName("org.pickaid.pibrary.api.targeting.PiTargetingResolver"));
        assertDoesNotThrow(() -> Class.forName("org.pickaid.pibrary.api.projectile.PiProjectileTraces"));
        assertDoesNotThrow(() -> Class.forName("org.pickaid.pibrary.api.projectile.PiProjectileTracer"));
        assertDoesNotThrow(() -> Class.forName("org.pickaid.pibrary.api.projectile.PiProjectileLifecycles"));
        assertDoesNotThrow(() -> Class.forName("org.pickaid.pibrary.api.projectile.PiProjectileLifecycleRegistry"));
        assertDoesNotThrow(() -> Class.forName("org.pickaid.pibrary.api.projectile.PiProjectileImpacts"));
        assertDoesNotThrow(() -> Class.forName("org.pickaid.pibrary.api.projectile.PiProjectileImpactRegistry"));
        assertDoesNotThrow(() -> Class.forName("org.pickaid.pibrary.runtime.core.PibraryRuntimeBootstrap"));
        assertDoesNotThrow(() -> Class.forName("org.pickaid.pibrary.api.core.PibraryScope"));
        assertDoesNotThrow(() -> Class.forName("org.pickaid.pibrary.api.core.PibraryScopeKey"));
        assertDoesNotThrow(() -> Class.forName("org.pickaid.pibrary.api.core.PibraryScopeRegistry"));
        assertDoesNotThrow(() -> Class.forName("org.pickaid.pibrary.api.core.PibraryScopes"));

        try (Stream<Path> files = Files.walk(Path.of("src/main/java/org/pickaid/pibrary/api/core"))) {
            assertFalse(files.anyMatch(PibraryGameApiNamingTest::hasServiceInFileName));
        }
        try (Stream<Path> files = Files.walk(Path.of("src/main/java/org/pickaid/pibrary/api/entity"))) {
            assertFalse(files.anyMatch(PibraryGameApiNamingTest::hasServiceInFileName));
        }
        try (Stream<Path> files = Files.walk(Path.of("src/main/java/org/pickaid/pibrary/api/targeting"))) {
            assertFalse(files.anyMatch(PibraryGameApiNamingTest::hasServiceInFileName));
        }
        try (Stream<Path> files = Files.walk(Path.of("src/main/java/org/pickaid/pibrary/api/projectile"))) {
            assertFalse(files.anyMatch(PibraryGameApiNamingTest::hasServiceInFileName));
        }
        try (Stream<Path> files = Files.walk(Path.of("src/main/java/org/pickaid/pibrary/runtime/core"))) {
            assertFalse(files.anyMatch(PibraryGameApiNamingTest::hasServiceInFileName));
        }
        try (Stream<Path> files = Files.walk(Path.of("src/main/java/org/pickaid/pibrary/runtime/entity"))) {
            assertFalse(files.anyMatch(PibraryGameApiNamingTest::hasServiceInFileName));
        }
        try (Stream<Path> files = Files.walk(Path.of("src/main/java/org/pickaid/pibrary/runtime/projectile"))) {
            assertFalse(files.anyMatch(PibraryGameApiNamingTest::hasServiceInFileName));
        }
        try (Stream<Path> files = Files.walk(Path.of("src/main/java/org/pickaid/pibrary/runtime/targeting"))) {
            assertFalse(files.anyMatch(PibraryGameApiNamingTest::hasServiceInFileName));
        }
    }

    private static boolean hasServiceInFileName(Path path) {
        return Files.isRegularFile(path) && path.getFileName().toString().contains("Service");
    }
}
