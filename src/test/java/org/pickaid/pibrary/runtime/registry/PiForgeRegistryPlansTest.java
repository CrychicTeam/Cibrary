package org.pickaid.pibrary.runtime.registry;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;
import org.pickaid.pibrary.api.registry.PiRegistries;
import org.pickaid.pibrary.api.registry.PiRegistryFamily;
import org.pickaid.pibrary.api.registry.PiRegistryPhase;
import org.pickaid.pibrary.api.registry.PiRegistryPlan;
import org.pickaid.pibrary.testsupport.PiTestRegisterEvents;
import org.pickaid.pibrary.testsupport.PiTestRegistryKeys;

class PiForgeRegistryPlansTest {
    private static final ResourceKey<Registry<String>> STRINGS = PiTestRegistryKeys.registry("strings");
    private static final ResourceKey<Registry<Object>> OBJECTS = PiTestRegistryKeys.registry("objects");

    @Test
    void appliesOnlyMatchingModRegistrationRequestsForCurrentForgeRegisterEvent() {
        PiRegistryFamily<String> strings = PiRegistries.family(STRINGS, "example");
        PiRegistryFamily<Object> objects = PiRegistries.family(OBJECTS, "example");
        PiRegistryPlan plan = PiRegistries.collect(
                sink -> sink.register(strings.entry("wand", () -> "wand")),
                sink -> sink.register(strings.entry("datagen_only", () -> "skip", PiRegistryPhase.DATA_GENERATION)),
                sink -> sink.register(objects.entry("altar", Object::new))
        );

        int applied = PiForgeRegistryPlans.apply(PiTestRegisterEvents.registerEvent(STRINGS), plan);

        assertEquals(1, applied);
    }

    @Test
    void detailedApplyReportsMatchedIdsAndSkippedRequestCount() {
        PiRegistryFamily<String> strings = PiRegistries.family(STRINGS, "example")
                .withDefaultGroup("materials");
        PiRegistryFamily<Object> objects = PiRegistries.family(OBJECTS, "example")
                .withDefaultGroup("machines");
        PiRegistryPlan plan = PiRegistries.collect(
                sink -> sink.register(strings.entry("wand", () -> "wand")),
                sink -> sink.register(strings.entry("datagen_only", () -> "skip", PiRegistryPhase.DATA_GENERATION)),
                sink -> sink.register(objects.entry("altar", Object::new))
        );

        PiRegistryApplyReport report = PiForgeRegistryPlans.applyDetailed(PiTestRegisterEvents.registerEvent(STRINGS), plan);

        assertEquals(STRINGS.location(), report.registry());
        assertEquals(PiRegistryPhase.MOD_EVENT_REGISTRATION, report.phase());
        assertEquals(List.of(ResourceLocation.fromNamespaceAndPath("example", "wand")), report.appliedIds());
        assertEquals(List.of("materials"), report.appliedGroups());
        assertEquals(2, report.skippedRequests());
    }

    @Test
    void namespaceCheckedApplyRejectsForeignNamespaceBeforeRegistering() {
        PiRegistryFamily<String> example = PiRegistries.family(STRINGS, "example");
        PiRegistryFamily<String> addon = PiRegistries.family(STRINGS, "addon");
        PiRegistryPlan plan = PiRegistries.collect(
                sink -> sink.register(example.entry("wand", () -> "wand")),
                sink -> sink.register(addon.entry("staff", () -> "staff"))
        );

        assertThrows(IllegalStateException.class,
                () -> PiForgeRegistryPlans.apply(PiTestRegisterEvents.registerEvent(STRINGS), plan, "example"));
    }
}
