package org.pickaid.pibrary.runtime.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;
import org.pickaid.pinet.api.sync.PiSyncEnvelopeKind;
import org.pickaid.pinet.api.sync.PiSyncRoute;
import org.pickaid.pibrary.api.service.PiLivingServiceContext;
import org.pickaid.pibrary.api.service.PiLivingServiceHost;
import org.pickaid.pibrary.dev.example.CounterPlayerService;
import org.pickaid.piserializekit.api.schema.PiDecodeContext;

class PiLivingServiceRuntimeTest {
    private static final PiLivingServiceHost DETACHED_HOST = new PiLivingServiceHost() {
        @Override
        public net.minecraft.world.entity.LivingEntity living() {
            return null;
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
}
