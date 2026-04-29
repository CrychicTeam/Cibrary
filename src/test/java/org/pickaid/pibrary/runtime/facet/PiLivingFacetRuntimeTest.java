package org.pickaid.pibrary.runtime.facet;

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
import org.pickaid.pibrary.api.facet.PiLivingFacetContext;
import org.pickaid.pibrary.api.facet.PiLivingFacetContainer;
import org.pickaid.pibrary.api.facet.PiStateLivingEntityFacet;
import org.pickaid.pibrary.dev.example.CounterPlayerFacet;
import org.pickaid.pibrary.dev.example.CounterState;
import org.pickaid.piserializekit.api.schema.PiDecodeContext;

class PiLivingFacetRuntimeTest {
    private static final PibraryServiceKey<String> GREETING =
            new PibraryServiceKey<>(ResourceLocation.fromNamespaceAndPath("test", "greeting"), String.class);
    private static final PibraryServiceContext DETACHED_SERVICES = PibraryServices.create();

    private static final PiLivingFacetContainer DETACHED_CONTAINER = new PiLivingFacetContainer() {
        @Override
        public net.minecraft.world.entity.LivingEntity living() {
            return null;
        }

        @Override
        public PibraryServiceContext services() {
            return DETACHED_SERVICES;
        }

        @Override
        public <T extends org.pickaid.pibrary.api.facet.PiStateLivingEntityFacet<?>> T get(Class<T> facetClass) {
            throw new UnsupportedOperationException("Detached container does not resolve facets");
        }
    };

    private static CounterPlayerFacet newFacet() {
        return new CounterPlayerFacet(new PiLivingFacetContext(null, DETACHED_CONTAINER));
    }

    @Test
    void generatedExampleDescriptorLoadsThroughServiceLoader() {
        PiGeneratedLivingFacetDescriptor<CounterPlayerFacet, ?> descriptor =
                PiLivingFacetDescriptors.requireGenerated(CounterPlayerFacet.class);

        assertEquals(CounterPlayerFacet.class, descriptor.facetClass());
        assertEquals(ResourceLocation.fromNamespaceAndPath("pibrary", "counter_player"), descriptor.id());
        assertTrue(PiLivingFacetDescriptors.findGenerated(descriptor.id()).isPresent());
    }

    @Test
    void facetBuildsOwnerAndTrackingPayloadsWithDifferentFieldScopes() {
        CounterPlayerFacet facet = newFacet();
        facet.increment();
        facet.gainEnergy(2);

        var ownerPayload = facet.buildSyncPayload(PiSyncEnvelopeKind.DELTA, PiSyncRoute.OWNER);
        var trackingPayload = facet.buildSyncPayload(PiSyncEnvelopeKind.DELTA, PiSyncRoute.TRACKING);

        assertTrue(ownerPayload.contains("count"));
        assertTrue(ownerPayload.contains("energy"));
        assertTrue(trackingPayload.contains("count"));
        assertFalse(trackingPayload.contains("energy"));
    }

    @Test
    void facetAppliesDeltaPayloadIntoAnotherFacetInstance() {
        CounterPlayerFacet sourceFacet = newFacet();
        sourceFacet.increment();
        sourceFacet.gainEnergy(4);

        var ownerPayload = sourceFacet.buildSyncPayload(PiSyncEnvelopeKind.DELTA, PiSyncRoute.OWNER);
        var trackingPayload = sourceFacet.buildSyncPayload(PiSyncEnvelopeKind.DELTA, PiSyncRoute.TRACKING);

        CounterPlayerFacet ownerFacet = newFacet();
        ownerFacet.applySyncPayload(PiSyncEnvelopeKind.DELTA, ownerPayload, PiDecodeContext.strict());
        assertEquals(1, ownerFacet.getCount());
        assertEquals(4, ownerFacet.energy());

        CounterPlayerFacet trackingFacet = newFacet();
        trackingFacet.applySyncPayload(PiSyncEnvelopeKind.DELTA, trackingPayload, PiDecodeContext.strict());
        assertEquals(1, trackingFacet.getCount());
        assertEquals(0, trackingFacet.energy());
    }

    @Test
    void facetBuildsSyncEnvelopeWithStateSchemaId() {
        CounterPlayerFacet facet = newFacet();
        facet.increment();
        facet.gainEnergy(2);

        PiSyncEnvelope envelope = facet.buildSyncEnvelope(PiSyncEnvelopeKind.DELTA, PiSyncRoute.OWNER);

        assertEquals(PiSyncEnvelopeKind.DELTA, envelope.kind());
        assertEquals(PiSyncRoute.OWNER, envelope.route());
        assertEquals(ResourceLocation.fromNamespaceAndPath("pibrary", "counter_state"), envelope.schemaId());
        assertTrue(envelope.payload().contains("count"));
        assertTrue(envelope.payload().contains("energy"));
    }

    @Test
    void facetBuildsSyncEnvelopeWithExplicitTransportTarget() {
        CounterPlayerFacet facet = newFacet();
        facet.increment();

        PiSyncTarget target = PiSyncTarget.of(
                ResourceLocation.fromNamespaceAndPath("pibrary", "living_facet"),
                "counter_player/owner"
        );

        PiSyncEnvelope envelope = facet.buildSyncEnvelope(PiSyncEnvelopeKind.DELTA, PiSyncRoute.OWNER, target);

        assertEquals(target, envelope.target());
        assertEquals(ResourceLocation.fromNamespaceAndPath("pibrary", "counter_state"), envelope.schemaId());
        assertTrue(envelope.payload().contains("count"));
    }

    @Test
    void facetContextsUseLocalChildScopesWithSharedContainerFallback() {
        TestLivingContainer container = new TestLivingContainer();
        TestContextFacet left = new TestContextFacet(new PiLivingFacetContext(null, container));
        TestContextFacet right = new TestContextFacet(new PiLivingFacetContext(null, container));

        left.putShared("shared");
        assertEquals("shared", left.greeting());
        assertEquals("shared", right.greeting());

        left.putLocal("left");
        assertEquals("left", left.greeting());
        assertEquals("shared", right.greeting());
        assertEquals("shared", container.services().require(GREETING));
    }

    @Test
    void instanceProviderRegistersFacetIntoAttachedContainer() {
        PiGeneratedLivingFacetDescriptor<CounterPlayerFacet, ?> descriptor =
                PiLivingFacetDescriptors.requireGenerated(CounterPlayerFacet.class);
        PiAttachedLivingFacetContainer container = new PiAttachedLivingFacetContainer(null);

        descriptor.createProvider(null, container);

        assertTrue(container.get(CounterPlayerFacet.class) instanceof CounterPlayerFacet);
    }

    @Test
    void facetPersistentDataUsesPersistedProjection() {
        CounterPlayerFacet source = newFacet();
        source.increment();
        source.gainEnergy(3);
        source.setSessionGlow(14);

        CompoundTag persisted = source.savePersistentData();

        assertTrue(persisted.contains("count"));
        assertTrue(persisted.contains("energy"));
        assertFalse(persisted.contains("session_glow"));

        CounterPlayerFacet restored = newFacet();
        restored.setSessionGlow(99);
        restored.loadPersistentData(persisted, PiDecodeContext.strict());

        assertEquals(1, restored.getCount());
        assertEquals(3, restored.energy());
        assertEquals(99, restored.sessionGlow());
    }

    private static final class TestLivingContainer implements PiLivingFacetContainer {
        private final PibraryServiceContext services = PibraryServices.create();

        @Override
        public net.minecraft.world.entity.LivingEntity living() {
            return null;
        }

        @Override
        public PibraryServiceContext services() {
            return services;
        }

        @Override
        public <T extends PiStateLivingEntityFacet<?>> T get(Class<T> facetClass) {
            throw new UnsupportedOperationException("Test container does not resolve sibling facets");
        }
    }

    private static final class TestContextFacet extends PiStateLivingEntityFacet<CounterState> {
        private TestContextFacet(PiLivingFacetContext context) {
            super(context);
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
    }
}
