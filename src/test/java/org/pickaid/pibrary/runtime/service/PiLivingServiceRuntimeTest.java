package org.pickaid.pibrary.runtime.service;

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
import org.pickaid.pibrary.api.service.PiLivingServiceContext;
import org.pickaid.pibrary.api.service.PiLivingServiceHost;
import org.pickaid.pibrary.api.service.PiStateLivingEntityService;
import org.pickaid.pibrary.dev.example.CounterPlayerService;
import org.pickaid.pibrary.dev.example.CounterState;
import org.pickaid.piserializekit.api.schema.PiDecodeContext;

class PiLivingServiceRuntimeTest {
    private static final PibraryServiceKey<String> GREETING =
            new PibraryServiceKey<>(ResourceLocation.fromNamespaceAndPath("test", "greeting"), String.class);
    private static final PibraryServiceContext DETACHED_SERVICES = PibraryServices.create();

    private static final PiLivingServiceHost DETACHED_HOST = new PiLivingServiceHost() {
        @Override
        public net.minecraft.world.entity.LivingEntity living() {
            return null;
        }

        @Override
        public PibraryServiceContext services() {
            return DETACHED_SERVICES;
        }

        @Override
        public <T extends org.pickaid.pibrary.api.service.PiStateLivingEntityService<?>> T get(Class<T> serviceType) {
            throw new UnsupportedOperationException("Detached host does not resolve services");
        }
    };

    private static CounterPlayerService newService() {
        return new CounterPlayerService(new PiLivingServiceContext(null, DETACHED_HOST));
    }

    @Test
    void generatedExampleDescriptorLoadsThroughServiceLoader() {
        PiGeneratedLivingServiceDescriptor<CounterPlayerService, ?> descriptor =
                PiLivingServiceDescriptors.requireGenerated(CounterPlayerService.class);

        assertEquals(CounterPlayerService.class, descriptor.serviceType());
        assertEquals(ResourceLocation.fromNamespaceAndPath("pibrary", "counter_player"), descriptor.id());
        assertTrue(PiLivingServiceDescriptors.findGenerated(descriptor.id()).isPresent());
    }

    @Test
    void serviceBuildsOwnerAndTrackingPayloadsWithDifferentFieldScopes() {
        CounterPlayerService service = newService();
        service.increment();
        service.gainEnergy(2);

        var ownerPayload = service.buildSyncPayload(PiSyncEnvelopeKind.DELTA, PiSyncRoute.OWNER);
        var trackingPayload = service.buildSyncPayload(PiSyncEnvelopeKind.DELTA, PiSyncRoute.TRACKING);

        assertTrue(ownerPayload.contains("count"));
        assertTrue(ownerPayload.contains("energy"));
        assertTrue(trackingPayload.contains("count"));
        assertFalse(trackingPayload.contains("energy"));
    }

    @Test
    void serviceAppliesDeltaPayloadIntoAnotherServiceInstance() {
        CounterPlayerService sourceService = newService();
        sourceService.increment();
        sourceService.gainEnergy(4);

        var ownerPayload = sourceService.buildSyncPayload(PiSyncEnvelopeKind.DELTA, PiSyncRoute.OWNER);
        var trackingPayload = sourceService.buildSyncPayload(PiSyncEnvelopeKind.DELTA, PiSyncRoute.TRACKING);

        CounterPlayerService ownerService = newService();
        ownerService.applySyncPayload(PiSyncEnvelopeKind.DELTA, ownerPayload, PiDecodeContext.strict());
        assertEquals(1, ownerService.getCount());
        assertEquals(4, ownerService.energy());

        CounterPlayerService trackingService = newService();
        trackingService.applySyncPayload(PiSyncEnvelopeKind.DELTA, trackingPayload, PiDecodeContext.strict());
        assertEquals(1, trackingService.getCount());
        assertEquals(0, trackingService.energy());
    }

    @Test
    void serviceBuildsSyncEnvelopeWithStateSchemaId() {
        CounterPlayerService service = newService();
        service.increment();
        service.gainEnergy(2);

        PiSyncEnvelope envelope = service.buildSyncEnvelope(PiSyncEnvelopeKind.DELTA, PiSyncRoute.OWNER);

        assertEquals(PiSyncEnvelopeKind.DELTA, envelope.kind());
        assertEquals(PiSyncRoute.OWNER, envelope.route());
        assertEquals(ResourceLocation.fromNamespaceAndPath("pibrary", "counter_state"), envelope.schemaId());
        assertTrue(envelope.payload().contains("count"));
        assertTrue(envelope.payload().contains("energy"));
    }

    @Test
    void serviceBuildsSyncEnvelopeWithExplicitTransportTarget() {
        CounterPlayerService service = newService();
        service.increment();

        PiSyncTarget target = PiSyncTarget.of(
                ResourceLocation.fromNamespaceAndPath("pibrary", "living_service"),
                "counter_player/owner"
        );

        PiSyncEnvelope envelope = service.buildSyncEnvelope(PiSyncEnvelopeKind.DELTA, PiSyncRoute.OWNER, target);

        assertEquals(target, envelope.target());
        assertEquals(ResourceLocation.fromNamespaceAndPath("pibrary", "counter_state"), envelope.schemaId());
        assertTrue(envelope.payload().contains("count"));
    }

    @Test
    void serviceContextsUseLocalChildScopesWithSharedHostFallback() {
        TestLivingHost host = new TestLivingHost();
        TestContextService left = new TestContextService(new PiLivingServiceContext(null, host));
        TestContextService right = new TestContextService(new PiLivingServiceContext(null, host));

        left.putShared("shared");
        assertEquals("shared", left.greeting());
        assertEquals("shared", right.greeting());

        left.putLocal("left");
        assertEquals("left", left.greeting());
        assertEquals("shared", right.greeting());
        assertEquals("shared", host.services().require(GREETING));
    }

    @Test
    void instanceProviderRegistersServiceIntoAttachedHost() {
        PiGeneratedLivingServiceDescriptor<CounterPlayerService, ?> descriptor =
                PiLivingServiceDescriptors.requireGenerated(CounterPlayerService.class);
        PiAttachedLivingHost host = new PiAttachedLivingHost(null);

        descriptor.createProvider(null, host);

        assertTrue(host.get(CounterPlayerService.class) instanceof CounterPlayerService);
    }

    @Test
    void servicePersistentDataUsesPersistedProjection() {
        CounterPlayerService source = newService();
        source.increment();
        source.gainEnergy(3);
        source.setSessionGlow(14);

        CompoundTag persisted = source.savePersistentData();

        assertTrue(persisted.contains("count"));
        assertTrue(persisted.contains("energy"));
        assertFalse(persisted.contains("session_glow"));

        CounterPlayerService restored = newService();
        restored.setSessionGlow(99);
        restored.loadPersistentData(persisted, PiDecodeContext.strict());

        assertEquals(1, restored.getCount());
        assertEquals(3, restored.energy());
        assertEquals(99, restored.sessionGlow());
    }

    private static final class TestLivingHost implements PiLivingServiceHost {
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
        public <T extends PiStateLivingEntityService<?>> T get(Class<T> serviceType) {
            throw new UnsupportedOperationException("Test host does not resolve sibling services");
        }
    }

    private static final class TestContextService extends PiStateLivingEntityService<CounterState> {
        private TestContextService(PiLivingServiceContext context) {
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
