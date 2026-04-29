package org.pickaid.pibrary.runtime.registry;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;
import org.pickaid.pibrary.api.registry.PiRegistries;
import org.pickaid.pibrary.api.registry.PiRegistryFamily;
import org.pickaid.pibrary.api.registry.PiRegistryPlan;
import org.pickaid.pibrary.testsupport.PiTestRegisterEvents;
import org.pickaid.pibrary.testsupport.PiTestRegistryKeys;

class PiForgeRegistryBootstrapTest {
    private static final ResourceKey<Registry<String>> STRINGS = PiTestRegistryKeys.registry("strings");

    @Test
    void bootstrapHoldsNamespaceCheckedPlanAndHandlesForgeRegisterEvents() {
        PiRegistryFamily<String> strings = PiRegistries.family(STRINGS, "example")
                .withDefaultGroup("materials");
        PiRegistryPlan plan = PiRegistries.collect(sink -> sink.register(strings.entry("wand", () -> "wand")));
        PiForgeRegistryBootstrap bootstrap = PiForgeRegistryBootstrap.create(plan, "example");

        PiRegistryApplyReport report = bootstrap.handle(PiTestRegisterEvents.registerEvent(STRINGS));

        assertSame(plan, bootstrap.plan());
        assertEquals("example", bootstrap.namespace());
        assertEquals(1, bootstrap.summary().requestCount());
        assertEquals(1, bootstrap.summary().countGroup("materials"));
        assertEquals(STRINGS.location(), report.registry());
        assertEquals(1, report.appliedRequests());
        assertEquals(java.util.List.of("materials"), report.appliedGroups());
        assertEquals(ResourceLocation.fromNamespaceAndPath("example", "wand"), report.appliedIds().get(0));
    }

    @Test
    void bootstrapRejectsForeignNamespaceAtCreation() {
        PiRegistryFamily<String> strings = PiRegistries.family(STRINGS, "other");
        PiRegistryPlan plan = PiRegistries.collect(sink -> sink.register(strings.entry("wand", () -> "wand")));

        assertThrows(IllegalStateException.class, () -> PiForgeRegistryBootstrap.create(plan, "example"));
    }
}
