package org.pickaid.pibrary.api.service;

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

class PiStateLevelServiceTest {
    private static final PibraryServiceKey<String> GREETING =
            new PibraryServiceKey<>(ResourceLocation.fromNamespaceAndPath("test", "greeting"), String.class);

    @Test
    void persistentDataUsesPersistedProjection() {
        TestLevelService source = newService();
        source.increment();
        source.gainEnergy(3);
        source.setSessionGlow(14);

        CompoundTag persisted = source.savePersistentData();

        assertTrue(persisted.contains("count"));
        assertTrue(persisted.contains("energy"));
        assertFalse(persisted.contains("session_glow"));

        TestLevelService restored = newService();
        restored.setSessionGlow(99);
        restored.loadPersistentData(persisted, PiDecodeContext.strict());

        assertEquals(1, restored.count());
        assertEquals(3, restored.energy());
        assertEquals(99, restored.sessionGlow());
    }

    @Test
    void syncPayloadRespectsRouteVisibility() {
        TestLevelService service = newService();
        service.increment();
        service.gainEnergy(2);

        CompoundTag ownerPayload = service.buildSyncPayload(PiSyncEnvelopeKind.DELTA, PiSyncRoute.OWNER);
        CompoundTag trackingPayload = service.buildSyncPayload(PiSyncEnvelopeKind.DELTA, PiSyncRoute.TRACKING);

        assertTrue(ownerPayload.contains("count"));
        assertTrue(ownerPayload.contains("energy"));
        assertTrue(trackingPayload.contains("count"));
        assertFalse(trackingPayload.contains("energy"));
    }

    @Test
    void syncEnvelopeUsesStateSchemaIdAndExplicitTarget() {
        TestLevelService service = newService();
        service.increment();

        PiSyncTarget target = PiSyncTarget.of(
                ResourceLocation.fromNamespaceAndPath("pibrary", "level_service"),
                "counter_level/global"
        );

        PiSyncEnvelope envelope = service.buildSyncEnvelope(PiSyncEnvelopeKind.DELTA, PiSyncRoute.GLOBAL, target);

        assertEquals(PiSyncEnvelopeKind.DELTA, envelope.kind());
        assertEquals(PiSyncRoute.GLOBAL, envelope.route());
        assertEquals(ResourceLocation.fromNamespaceAndPath("pibrary", "counter_state"), envelope.schemaId());
        assertEquals(target, envelope.target());
    }

    @Test
    void applySyncPayloadLoadsAnotherLevelServiceInstance() {
        TestLevelService source = newService();
        source.increment();
        source.gainEnergy(4);

        CompoundTag ownerPayload = source.buildSyncPayload(PiSyncEnvelopeKind.DELTA, PiSyncRoute.OWNER);
        CompoundTag trackingPayload = source.buildSyncPayload(PiSyncEnvelopeKind.DELTA, PiSyncRoute.TRACKING);

        TestLevelService ownerService = newService();
        ownerService.applySyncPayload(PiSyncEnvelopeKind.DELTA, ownerPayload, PiDecodeContext.strict());
        assertEquals(1, ownerService.count());
        assertEquals(4, ownerService.energy());

        TestLevelService trackingService = newService();
        trackingService.applySyncPayload(PiSyncEnvelopeKind.DELTA, trackingPayload, PiDecodeContext.strict());
        assertEquals(1, trackingService.count());
        assertEquals(0, trackingService.energy());
    }

    @Test
    void clearDirtyByRouteKeepsOwnerOnlyFieldsPending() {
        TestLevelService service = newService();
        service.increment();
        service.gainEnergy(5);

        assertTrue(service.hasDirty(PiSyncRoute.OWNER));
        assertTrue(service.hasDirty(PiSyncRoute.TRACKING));

        service.clearDirty(PiSyncRoute.TRACKING);

        assertTrue(service.hasDirty(PiSyncRoute.OWNER));
        assertFalse(service.hasDirty(PiSyncRoute.TRACKING));
    }

    @Test
    void serviceContextsUseLocalChildScopesWithSharedLevelFallback() {
        PibraryServiceContext shared = PibraryServices.create();
        TestLevelService left = new TestLevelService(new PiLevelServiceContext(null, shared));
        TestLevelService right = new TestLevelService(new PiLevelServiceContext(null, shared));

        left.putShared("shared");
        assertEquals("shared", left.greeting());
        assertEquals("shared", right.greeting());

        left.putLocal("left");
        assertEquals("left", left.greeting());
        assertEquals("shared", right.greeting());
        assertEquals("shared", shared.require(GREETING));
    }

    private static final class TestLevelService extends PiStateLevelService<CounterState> {
        private TestLevelService(PiLevelServiceContext context) {
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

    private static TestLevelService newService() {
        return new TestLevelService(new PiLevelServiceContext(null, PibraryServices.create()));
    }
}
