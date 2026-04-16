package org.pickaid.pibrary.runtime.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.util.List;
import java.util.Optional;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import org.junit.jupiter.api.Test;
import org.pickaid.pinet.api.sync.model.PiSyncEnvelopeKind;
import org.pickaid.pinet.api.sync.model.PiSyncRoute;
import org.pickaid.pibrary.api.core.PibraryServiceContext;
import org.pickaid.pibrary.api.core.PibraryServices;
import org.pickaid.pibrary.api.service.PiAttachedLivingService;
import org.pickaid.pibrary.api.service.PiCloneAwareLivingService;
import org.pickaid.pibrary.api.service.PiLivingServiceContext;
import org.pickaid.pibrary.api.service.PiLivingServiceHost;
import org.pickaid.pibrary.api.service.PiStateLivingEntityService;
import org.pickaid.pibrary.api.service.PiSyncAwareLivingService;
import org.pickaid.pibrary.api.service.PiTickingLivingService;
import org.pickaid.pibrary.dev.example.CounterState;
import org.pickaid.piserializekit.api.schema.PiDecodeContext;

class PiLivingServiceLifecycleTest {
    private static final PibraryServiceContext DETACHED_SERVICES = PibraryServices.create();

    private static final PiLivingServiceHost DETACHED_HOST = new PiLivingServiceHost() {
        @Override
        public LivingEntity living() {
            return null;
        }

        @Override
        public PibraryServiceContext services() {
            return DETACHED_SERVICES;
        }

        @Override
        public <T extends PiStateLivingEntityService<?>> T get(Class<T> serviceType) {
            throw new UnsupportedOperationException("Detached host does not resolve sibling services");
        }
    };

    @Test
    void generatedDescriptorSnapshotIsStableAfterLoad() {
        List<PiGeneratedLivingServiceDescriptor<?, ?>> first = PiLivingServiceDescriptors.generatedDescriptors();
        List<PiGeneratedLivingServiceDescriptor<?, ?>> second = PiLivingServiceDescriptors.generatedDescriptors();

        assertSame(first, second);
    }

    @Test
    void lifecycleDispatcherInvokesOnlyImplementedHooks() {
        LifecycleService service = new LifecycleService(new PiLivingServiceContext(null, DETACHED_HOST));

        PiLivingServiceLifecycles.onAttached(service);
        PiLivingServiceLifecycles.tick(service, false);
        PiLivingServiceLifecycles.tick(service, true);
        PiLivingServiceLifecycles.onSyncApplied(service, PiSyncEnvelopeKind.DELTA, PiSyncRoute.OWNER);
        PiLivingServiceLifecycles.onCloned(service, null, true);

        assertEquals(1, service.attachedCalls);
        assertEquals(1, service.serverTickCalls);
        assertEquals(1, service.clientTickCalls);
        assertEquals(1, service.syncCalls);
        assertEquals(PiSyncEnvelopeKind.DELTA, service.lastSyncKind);
        assertEquals(PiSyncRoute.OWNER, service.lastSyncRoute);
        assertEquals(1, service.cloneCalls);
        assertEquals(1, service.cloneDeathCalls);
    }

    @Test
    void instanceProviderInvokesAttachedHookWhenServiceIsCreated() {
        LifecycleDescriptor descriptor = new LifecycleDescriptor(null);
        PiAttachedLivingHost host = new PiAttachedLivingHost(null);

        PiLivingServiceInstanceProvider<LifecycleService, CounterState> provider =
                new PiLivingServiceInstanceProvider<>(null, descriptor, host);

        assertEquals(1, provider.service().attachedCalls);
        assertSame(provider.service(), host.get(LifecycleService.class));
    }

    @Test
    void descriptorApplySyncPayloadInvokesSyncHookAfterApplyingState() {
        LifecycleService source = new LifecycleService(new PiLivingServiceContext(null, DETACHED_HOST));
        source.increment();
        source.gainEnergy(3);

        LifecycleService target = new LifecycleService(new PiLivingServiceContext(null, DETACHED_HOST));
        LifecycleDescriptor descriptor = new LifecycleDescriptor(target);

        CompoundTag payload = source.buildSyncPayload(PiSyncEnvelopeKind.DELTA, PiSyncRoute.OWNER);
        descriptor.applySyncPayload(null, PiSyncEnvelopeKind.DELTA, PiSyncRoute.OWNER, payload);

        assertEquals(1, target.viewState().count);
        assertEquals(3, target.viewState().energy);
        assertEquals(1, target.syncCalls);
        assertEquals(PiSyncEnvelopeKind.DELTA, target.lastSyncKind);
        assertEquals(PiSyncRoute.OWNER, target.lastSyncRoute);
    }

    private static final class LifecycleDescriptor extends PiGeneratedLivingServiceDescriptor<LifecycleService, CounterState> {
        private final LifecycleService fixedService;

        private LifecycleDescriptor(LifecycleService fixedService) {
            super(ResourceLocation.fromNamespaceAndPath("test", "lifecycle"), LifecycleService.class, CounterState.class);
            this.fixedService = fixedService;
        }

        @Override
        public Capability<LifecycleService> capability() {
            return CapabilityManager.get(new CapabilityToken<>() {
            });
        }

        @Override
        public LifecycleService create(PiLivingServiceContext context) {
            return fixedService == null ? new LifecycleService(context) : fixedService;
        }

        @Override
        public Optional<LifecycleService> find(LivingEntity living) {
            return Optional.ofNullable(fixedService);
        }
    }

    private static final class LifecycleService extends PiStateLivingEntityService<CounterState>
            implements PiAttachedLivingService, PiTickingLivingService, PiSyncAwareLivingService, PiCloneAwareLivingService {
        private int attachedCalls;
        private int serverTickCalls;
        private int clientTickCalls;
        private int syncCalls;
        private int cloneCalls;
        private int cloneDeathCalls;
        private PiSyncEnvelopeKind lastSyncKind;
        private PiSyncRoute lastSyncRoute;

        private LifecycleService(PiLivingServiceContext context) {
            super(context);
        }

        private void increment() {
            updateState(state -> state.count++);
        }

        private void gainEnergy(int value) {
            updateState(state -> state.energy += value);
        }

        @Override
        public void onAttached() {
            attachedCalls++;
        }

        @Override
        public void serverTick() {
            serverTickCalls++;
        }

        @Override
        public void clientTick() {
            clientTickCalls++;
        }

        @Override
        public void onSyncApplied(PiSyncEnvelopeKind kind, PiSyncRoute route, CompoundTag payload, PiDecodeContext context) {
            syncCalls++;
            lastSyncKind = kind;
            lastSyncRoute = route;
        }

        @Override
        public void onCloned(LivingEntity original, boolean wasDeath) {
            cloneCalls++;
            if (wasDeath) {
                cloneDeathCalls++;
            }
        }
    }
}
