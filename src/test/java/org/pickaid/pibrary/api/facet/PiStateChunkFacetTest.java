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
import org.pickaid.pibrary.api.core.PibraryScope;
import org.pickaid.pibrary.api.core.PibraryScopeKey;
import org.pickaid.pibrary.api.core.PibraryScopes;
import org.pickaid.pibrary.dev.example.CounterState;
import org.pickaid.piserializekit.api.schema.PiDecodeContext;

class PiStateChunkFacetTest {
    private static final PibraryScopeKey<String> GREETING =
            new PibraryScopeKey<>(ResourceLocation.fromNamespaceAndPath("test", "greeting"), String.class);

    @Test
    void persistentDataUsesPersistedProjection() {
        TestChunkFacet source = newFacet();
        source.increment();
        source.gainEnergy(3);
        source.setSessionGlow(14);

        CompoundTag persisted = source.savePersistentData();

        assertTrue(persisted.contains("count"));
        assertTrue(persisted.contains("energy"));
        assertFalse(persisted.contains("session_glow"));

        TestChunkFacet restored = newFacet();
        restored.setSessionGlow(99);
        restored.loadPersistentData(persisted, PiDecodeContext.strict());

        assertEquals(1, restored.count());
        assertEquals(3, restored.energy());
        assertEquals(99, restored.sessionGlow());
    }

    @Test
    void syncPayloadRespectsRouteVisibility() {
        TestChunkFacet facet = newFacet();
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
        TestChunkFacet facet = newFacet();
        facet.increment();

        PiSyncTarget target = PiSyncTarget.of(
                ResourceLocation.fromNamespaceAndPath("pibrary", "chunk_facet"),
                "0,0"
        );

        PiSyncEnvelope envelope = facet.buildSyncEnvelope(PiSyncEnvelopeKind.DELTA, PiSyncRoute.TRACKING, target);

        assertEquals(PiSyncEnvelopeKind.DELTA, envelope.kind());
        assertEquals(PiSyncRoute.TRACKING, envelope.route());
        assertEquals(ResourceLocation.fromNamespaceAndPath("pibrary", "counter_state"), envelope.schemaId());
        assertEquals(target, envelope.target());
    }

    @Test
    void contextsUseLocalChildScopesWithSharedChunkFallback() {
        PibraryScope shared = PibraryScopes.create();
        TestChunkFacet left = new TestChunkFacet(new PiChunkFacetContext(null, new DetachedChunkContainer(shared)));
        TestChunkFacet right = new TestChunkFacet(new PiChunkFacetContext(null, new DetachedChunkContainer(shared)));

        left.putShared("shared");
        assertEquals("shared", left.greeting());
        assertEquals("shared", right.greeting());

        left.putLocal("left");
        assertEquals("left", left.greeting());
        assertEquals("shared", right.greeting());
        assertEquals("shared", shared.require(GREETING));
    }

    private static final class TestChunkFacet extends PiStateChunkFacet<CounterState> {
        private TestChunkFacet(PiChunkFacetContext context) {
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
            sharedScope().register(GREETING, value);
        }

        private void putLocal(String value) {
            scope().register(GREETING, value);
        }

        private String greeting() {
            return requireScoped(GREETING);
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

    private static TestChunkFacet newFacet() {
        return new TestChunkFacet(new PiChunkFacetContext(null, new DetachedChunkContainer(PibraryScopes.create())));
    }

    private record DetachedChunkContainer(PibraryScope scope) implements PiChunkFacetContainer {
        @Override
        public net.minecraft.world.level.chunk.LevelChunk chunk() {
            return null;
        }

        @Override
        public <T extends PiStateChunkFacet<?>> T get(Class<T> facetClass) {
            throw new IllegalStateException("No attached chunk facets in detached test container");
        }
    }
}
