package org.pickaid.pibrary.api.facet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;
import org.pickaid.pinet.api.sync.model.PiSyncEnvelope;
import org.pickaid.pinet.api.sync.model.PiSyncEnvelopeKind;
import org.pickaid.pinet.api.sync.model.PiSyncRoute;
import org.pickaid.pinet.api.sync.model.PiSyncTarget;
import org.pickaid.pibrary.api.core.PibraryServiceContext;
import org.pickaid.pibrary.api.core.PibraryServiceKey;
import org.pickaid.pibrary.api.core.PibraryServices;
import org.pickaid.pibrary.dev.example.CounterState;
import org.pickaid.piserializekit.api.schema.PiDecodeContext;

class PiStateLevelFacetTest {
    private static final PibraryServiceKey<String> GREETING =
            new PibraryServiceKey<>(ResourceLocation.fromNamespaceAndPath("test", "greeting"), String.class);

    @Test
    void persistentDataUsesPersistedProjection() {
        TestLevelFacet source = newFacet();
        source.increment();
        source.gainEnergy(3);
        source.setSessionGlow(14);

        CompoundTag persisted = source.savePersistentData();

        assertTrue(persisted.contains("count"));
        assertTrue(persisted.contains("energy"));
        assertFalse(persisted.contains("session_glow"));

        TestLevelFacet restored = newFacet();
        restored.setSessionGlow(99);
        restored.loadPersistentData(persisted, PiDecodeContext.strict());

        assertEquals(1, restored.count());
        assertEquals(3, restored.energy());
        assertEquals(99, restored.sessionGlow());
    }

    @Test
    void syncPayloadRespectsRouteVisibility() {
        TestLevelFacet facet = newFacet();
        facet.increment();
        facet.gainEnergy(2);

        CompoundTag ownerPayload = facet.buildSyncPayload(PiSyncEnvelopeKind.DELTA, PiSyncRoute.OWNER);
        CompoundTag trackingPayload = facet.buildSyncPayload(PiSyncEnvelopeKind.DELTA, PiSyncRoute.TRACKING);

        assertTrue(ownerPayload.contains("count"));
        assertTrue(ownerPayload.contains("energy"));
        assertTrue(trackingPayload.contains("count"));
        assertFalse(trackingPayload.contains("energy"));
    }

    @Test
    void syncEnvelopeUsesStateSchemaIdAndExplicitTarget() {
        TestLevelFacet facet = newFacet();
        facet.increment();

        PiSyncTarget target = PiSyncTarget.of(
                ResourceLocation.fromNamespaceAndPath("pibrary", "level_facet"),
                "counter_level/global"
        );

        PiSyncEnvelope envelope = facet.buildSyncEnvelope(PiSyncEnvelopeKind.DELTA, PiSyncRoute.GLOBAL, target);

        assertEquals(PiSyncEnvelopeKind.DELTA, envelope.kind());
        assertEquals(PiSyncRoute.GLOBAL, envelope.route());
        assertEquals(ResourceLocation.fromNamespaceAndPath("pibrary", "counter_state"), envelope.schemaId());
        assertEquals(target, envelope.target());
    }

    @Test
    void applySyncPayloadLoadsAnotherLevelFacetInstance() {
        TestLevelFacet source = newFacet();
        source.increment();
        source.gainEnergy(4);

        CompoundTag ownerPayload = source.buildSyncPayload(PiSyncEnvelopeKind.DELTA, PiSyncRoute.OWNER);
        CompoundTag trackingPayload = source.buildSyncPayload(PiSyncEnvelopeKind.DELTA, PiSyncRoute.TRACKING);

        TestLevelFacet ownerFacet = newFacet();
        ownerFacet.applySyncPayload(PiSyncEnvelopeKind.DELTA, ownerPayload, PiDecodeContext.strict());
        assertEquals(1, ownerFacet.count());
        assertEquals(4, ownerFacet.energy());

        TestLevelFacet trackingFacet = newFacet();
        trackingFacet.applySyncPayload(PiSyncEnvelopeKind.DELTA, trackingPayload, PiDecodeContext.strict());
        assertEquals(1, trackingFacet.count());
        assertEquals(0, trackingFacet.energy());
    }

    @Test
    void clearDirtyByRouteKeepsOwnerOnlyFieldsPending() {
        TestLevelFacet facet = newFacet();
        facet.increment();
        facet.gainEnergy(5);

        assertTrue(facet.hasDirty(PiSyncRoute.OWNER));
        assertTrue(facet.hasDirty(PiSyncRoute.TRACKING));

        facet.clearDirty(PiSyncRoute.TRACKING);

        assertTrue(facet.hasDirty(PiSyncRoute.OWNER));
        assertFalse(facet.hasDirty(PiSyncRoute.TRACKING));
    }

    @Test
    void facetContextsUseLocalChildScopesWithSharedLevelFallback() {
        PibraryServiceContext shared = PibraryServices.create();
        TestLevelFacet left = new TestLevelFacet(new PiLevelFacetContext(null, shared));
        TestLevelFacet right = new TestLevelFacet(new PiLevelFacetContext(null, shared));

        left.putShared("shared");
        assertEquals("shared", left.greeting());
        assertEquals("shared", right.greeting());

        left.putLocal("left");
        assertEquals("left", left.greeting());
        assertEquals("shared", right.greeting());
        assertEquals("shared", shared.require(GREETING));
    }

    private static final class TestLevelFacet extends PiStateLevelFacet<CounterState> {
        private TestLevelFacet(PiLevelFacetContext context) {
            super(context);
        }

        private void increment() {
            updateState(state -> state.count++);
        }

        private void gainEnergy(int value) {
            updateState(state -> state.energy += value);
        }

        private void setSessionGlow(int value) {
            updateState(state -> state.sessionGlow = value);
        }

        private void putShared(String value) {
            sharedServices().register(GREETING, value);
        }

        private void putLocal(String value) {
            services().register(GREETING, value);
        }

        private String greeting() {
            return requireService(GREETING);
        }

        private int count() {
            return viewState().count;
        }

        private int energy() {
            return viewState().energy;
        }

        private int sessionGlow() {
            return viewState().sessionGlow;
        }
    }

    private static TestLevelFacet newFacet() {
        return new TestLevelFacet(new PiLevelFacetContext(null, PibraryServices.create()));
    }
}
