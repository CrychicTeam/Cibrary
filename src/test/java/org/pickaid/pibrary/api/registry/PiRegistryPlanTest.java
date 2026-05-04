package org.pickaid.pibrary.api.registry;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;
import org.pickaid.pibrary.testsupport.PiTestRegistryKeys;

class PiRegistryPlanTest {
    private static final ResourceKey<Registry<String>> STRINGS =
            PiTestRegistryKeys.registry("strings");
    private static final ResourceKey<Registry<Object>> OBJECTS =
            PiTestRegistryKeys.registry("objects");

    @Test
    void familyBuildsNamespacedRequestsWithDefaultPhase() {
        PiRegistryFamily<String> family = PiRegistries.family(STRINGS, "example");
        PiRegistryRequest<String> request = family.entry("wand", () -> "wand");

        assertEquals(STRINGS, request.registryKey());
        assertEquals(new ResourceLocation("example", "wand"), request.id());
        assertEquals(PiRegistryPhase.MOD_EVENT_REGISTRATION, request.phase());
    }

    @Test
    void planKeepsContributionOrderAndPhaseBuckets() {
        PiRegistryFamily<String> strings = PiRegistries.family(STRINGS, "example");
        PiRegistryFamily<Object> objects = PiRegistries.family(OBJECTS, "example")
                .withDefaultPhase(PiRegistryPhase.STATIC_BOOTSTRAP);

        PiRegistryPlan plan = PiRegistries.collect(
                sink -> sink.register(strings.entry("wand", () -> "wand")),
                sink -> sink.register(objects.entry("altar", Object::new))
        );

        assertEquals(2, plan.requests().size());
        assertEquals(new ResourceLocation("example", "wand"), plan.requests().get(0).id());
        assertEquals(1, plan.requests(PiRegistryPhase.MOD_EVENT_REGISTRATION).size());
        assertEquals(1, plan.requests(PiRegistryPhase.STATIC_BOOTSTRAP).size());
        assertTrue(plan.contains(STRINGS.location(), new ResourceLocation("example", "wand")));
        assertTrue(plan.contains(STRINGS, new ResourceLocation("example", "wand")));
        assertEquals(1, plan.requests(STRINGS).size());
        assertEquals(1, plan.requests(OBJECTS, PiRegistryPhase.STATIC_BOOTSTRAP).size());
        assertEquals("wand", plan.requests(STRINGS).get(0).factory().get());
    }

    @Test
    void duplicateIdsFailFastWithinSameRegistry() {
        PiRegistryFamily<String> strings = PiRegistries.family(STRINGS, "example");
        PiRegistryPlan plan = PiRegistries.plan();

        plan.register(strings.entry("wand", () -> "first"));

        assertThrows(IllegalStateException.class, () -> plan.register(strings.entry("wand", () -> "second")));
    }

    @Test
    void sameIdCanExistInDifferentRegistries() {
        PiRegistryFamily<String> strings = PiRegistries.family(STRINGS, "example");
        PiRegistryFamily<Object> objects = PiRegistries.family(OBJECTS, "example");
        PiRegistryPlan plan = PiRegistries.collect(
                sink -> sink.register(strings.entry("shared", () -> "shared")),
                sink -> sink.register(objects.entry("shared", Object::new))
        );

        assertEquals(2, plan.requests().size());
    }

    @Test
    void requestListsAreImmutableSnapshots() {
        PiRegistryFamily<String> strings = PiRegistries.family(STRINGS, "example");
        PiRegistryPlan plan = PiRegistries.collect(sink -> sink.register(strings.entry("wand", () -> "wand")));
        List<PiRegistryRequest<?>> snapshot = plan.requests();

        assertThrows(UnsupportedOperationException.class, () -> snapshot.add(strings.entry("other", () -> "other")));
        assertSame(snapshot.get(0), plan.requests().get(0));
    }

    @Test
    void planMergesOtherPlansInOrderAndKeepsDuplicateGuards() {
        PiRegistryFamily<String> strings = PiRegistries.family(STRINGS, "example");
        PiRegistryPlan base = PiRegistries.collect(sink -> sink.register(strings.entry("wand", () -> "wand")));
        PiRegistryPlan extra = PiRegistries.collect(sink -> sink.register(strings.entry("staff", () -> "staff")));

        PiRegistryPlan merged = PiRegistries.plan().merge(base).merge(extra);

        assertEquals(List.of(
                new ResourceLocation("example", "wand"),
                new ResourceLocation("example", "staff")
        ), merged.requests().stream().map(PiRegistryRequest::id).toList());
        assertThrows(IllegalStateException.class, () -> merged.merge(base));
    }

    @Test
    void mergeFailureDoesNotPartiallyMutatePlan() {
        PiRegistryFamily<String> strings = PiRegistries.family(STRINGS, "example");
        PiRegistryPlan target = PiRegistries.collect(sink -> sink.register(strings.entry("wand", () -> "wand")));
        PiRegistryPlan incoming = PiRegistries.collect(
                sink -> sink.register(strings.entry("staff", () -> "staff")),
                sink -> sink.register(strings.entry("wand", () -> "duplicate"))
        );

        assertThrows(IllegalStateException.class, () -> target.merge(incoming));
        assertEquals(List.of(new ResourceLocation("example", "wand")),
                target.requests().stream().map(PiRegistryRequest::id).toList());
    }

    @Test
    void planReportsNamespacesAndCanRequireSingleNamespace() {
        PiRegistryFamily<String> example = PiRegistries.family(STRINGS, "example");
        PiRegistryFamily<String> addon = PiRegistries.family(STRINGS, "addon");
        PiRegistryPlan plan = PiRegistries.collect(
                sink -> sink.register(example.entry("wand", () -> "wand")),
                sink -> sink.register(addon.entry("staff", () -> "staff"))
        );

        assertEquals(List.of("example", "addon"), plan.namespaces());
        assertEquals(1, plan.requests("example").size());
        assertThrows(IllegalStateException.class, () -> plan.requireNamespace("example"));
        PiRegistryPlan singleNamespace = PiRegistries.collect(sink -> sink.register(example.entry("only", () -> "only")));
        assertSame(singleNamespace, singleNamespace.requireNamespace("example"));
    }

    @Test
    void planCanGroupRequestsWithoutWeakeningDuplicateGuards() {
        PiRegistryFamily<String> machines = PiRegistries.family(STRINGS, "example")
                .withDefaultGroup("machines");
        PiRegistryFamily<String> materials = PiRegistries.family(STRINGS, "example")
                .withDefaultGroup("materials");
        PiRegistryPlan plan = PiRegistries.collect(
                sink -> sink.register(machines.entry("relay", () -> "relay")),
                sink -> sink.register(materials.entry("ingot", () -> "ingot"))
        );

        assertEquals(List.of("machines", "materials"), plan.groups());
        assertEquals(List.of(new ResourceLocation("example", "relay")),
                plan.requestsInGroup("machines").stream().map(PiRegistryRequest::id).toList());
        assertEquals(1, plan.summary().countGroup("machines"));
        assertThrows(IllegalStateException.class, () -> plan.register(materials.entry("relay", () -> "duplicate")));
    }

    @Test
    void summaryReportsPlanShapeForDiagnostics() {
        PiRegistryFamily<String> strings = PiRegistries.family(STRINGS, "example");
        PiRegistryFamily<Object> objects = PiRegistries.family(OBJECTS, "example")
                .withDefaultPhase(PiRegistryPhase.DATA_GENERATION);
        PiRegistryPlan plan = PiRegistries.collect(
                sink -> sink.register(strings.entry("wand", () -> "wand")),
                sink -> sink.register(objects.entry("altar", Object::new))
        );

        PiRegistryPlanSummary summary = plan.summary();

        assertEquals(2, summary.requestCount());
        assertEquals(List.of("example"), summary.namespaces());
        assertEquals(List.of(STRINGS.location(), OBJECTS.location()), summary.registries());
        assertEquals(1, summary.count(PiRegistryPhase.MOD_EVENT_REGISTRATION));
        assertEquals(1, summary.count(PiRegistryPhase.DATA_GENERATION));
        assertEquals(1, summary.count(STRINGS.location()));
        assertEquals(Map.of(
                PiRegistryPhase.MOD_EVENT_REGISTRATION, 1,
                PiRegistryPhase.DATA_GENERATION, 1
        ), summary.phaseCounts());
    }
}
