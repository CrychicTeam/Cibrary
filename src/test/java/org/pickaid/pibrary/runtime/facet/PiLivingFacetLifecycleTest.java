package org.pickaid.pibrary.runtime.facet;

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
import org.pickaid.pibrary.api.core.PibraryScope;
import org.pickaid.pibrary.api.core.PibraryScopes;
import org.pickaid.pibrary.api.facet.PiAttachedLivingFacet;
import org.pickaid.pibrary.api.facet.PiCloneAwareLivingFacet;
import org.pickaid.pibrary.api.facet.PiLivingFacetContext;
import org.pickaid.pibrary.api.facet.PiLivingFacetContainer;
import org.pickaid.pibrary.api.facet.PiStateLivingEntityFacet;
import org.pickaid.pibrary.api.facet.PiSyncAwareLivingFacet;
import org.pickaid.pibrary.api.facet.PiTickingLivingFacet;
import org.pickaid.pibrary.dev.example.CounterState;
import org.pickaid.piserializekit.api.schema.PiDecodeContext;

class PiLivingFacetLifecycleTest {
    private static final PibraryScope DETACHED_SCOPE = PibraryScopes.create();

    private static final PiLivingFacetContainer DETACHED_CONTAINER = new PiLivingFacetContainer() {
        @Override
        public LivingEntity living() {
            return null;
        }

        @Override
        public PibraryScope scope() {
            return DETACHED_SCOPE;
        }

        @Override
        public <T extends PiStateLivingEntityFacet<?>> T get(Class<T> facetClass) {
            throw new UnsupportedOperationException("Detached container does not resolve sibling facets");
        }
    };

    @Test
    void generatedDescriptorSnapshotIsStableAfterLoad() {
        List<PiGeneratedLivingFacetDescriptor<?, ?>> first = PiLivingFacetDescriptors.generatedDescriptors();
        List<PiGeneratedLivingFacetDescriptor<?, ?>> second = PiLivingFacetDescriptors.generatedDescriptors();

        assertSame(first, second);
    }

    @Test
    void lifecycleDispatcherInvokesOnlyImplementedHooks() {
        LifecycleFacet facet = new LifecycleFacet(new PiLivingFacetContext(null, DETACHED_CONTAINER));

        PiLivingFacetLifecycles.onAttached(facet);
        PiLivingFacetLifecycles.tick(facet, false);
        PiLivingFacetLifecycles.tick(facet, true);
        PiLivingFacetLifecycles.onSyncApplied(facet, PiSyncEnvelopeKind.DELTA, PiSyncRoute.OWNER);
        PiLivingFacetLifecycles.onCloned(facet, null, true);

        assertEquals(1, facet.attachedCalls);
        assertEquals(1, facet.serverTickCalls);
        assertEquals(1, facet.clientTickCalls);
        assertEquals(1, facet.syncCalls);
        assertEquals(PiSyncEnvelopeKind.DELTA, facet.lastSyncKind);
        assertEquals(PiSyncRoute.OWNER, facet.lastSyncRoute);
        assertEquals(1, facet.cloneCalls);
        assertEquals(1, facet.cloneDeathCalls);
    }

    @Test
    void instanceProviderInvokesAttachedHookWhenFacetIsCreated() {
        LifecycleDescriptor descriptor = new LifecycleDescriptor(null);
        PiAttachedLivingFacetContainer container = new PiAttachedLivingFacetContainer(null);

        PiLivingFacetInstanceProvider<LifecycleFacet, CounterState> provider =
                new PiLivingFacetInstanceProvider<>(null, descriptor, container);

        assertEquals(1, provider.facet().attachedCalls);
        assertSame(provider.facet(), container.get(LifecycleFacet.class));
    }

    @Test
    void descriptorApplySyncPayloadInvokesSyncHookAfterApplyingState() {
        LifecycleFacet source = new LifecycleFacet(new PiLivingFacetContext(null, DETACHED_CONTAINER));
        source.increment();
        source.gainEnergy(3);

        LifecycleFacet target = new LifecycleFacet(new PiLivingFacetContext(null, DETACHED_CONTAINER));
        LifecycleDescriptor descriptor = new LifecycleDescriptor(target);

        CompoundTag payload = source.buildSyncPayload(PiSyncEnvelopeKind.DELTA, PiSyncRoute.OWNER);
        descriptor.applySyncPayload(null, PiSyncEnvelopeKind.DELTA, PiSyncRoute.OWNER, payload);

        assertEquals(1, target.viewState().count);
        assertEquals(3, target.viewState().energy);
        assertEquals(1, target.syncCalls);
        assertEquals(PiSyncEnvelopeKind.DELTA, target.lastSyncKind);
        assertEquals(PiSyncRoute.OWNER, target.lastSyncRoute);
    }

    private static final class LifecycleDescriptor extends PiGeneratedLivingFacetDescriptor<LifecycleFacet, CounterState> {
        private final LifecycleFacet fixedFacet;

        private LifecycleDescriptor(LifecycleFacet fixedFacet) {
            super(new ResourceLocation("test", "lifecycle"), LifecycleFacet.class, CounterState.class);
            this.fixedFacet = fixedFacet;
        }

        @Override
        public Capability<LifecycleFacet> capability() {
            return CapabilityManager.get(new CapabilityToken<>() {
            });
        }

        @Override
        public LifecycleFacet create(PiLivingFacetContext context) {
            return fixedFacet == null ? new LifecycleFacet(context) : fixedFacet;
        }

        @Override
        public Optional<LifecycleFacet> find(LivingEntity living) {
            return Optional.ofNullable(fixedFacet);
        }
    }

    private static final class LifecycleFacet extends PiStateLivingEntityFacet<CounterState>
            implements PiAttachedLivingFacet, PiTickingLivingFacet, PiSyncAwareLivingFacet, PiCloneAwareLivingFacet {
        private int attachedCalls;
        private int serverTickCalls;
        private int clientTickCalls;
        private int syncCalls;
        private int cloneCalls;
        private int cloneDeathCalls;
        private PiSyncEnvelopeKind lastSyncKind;
        private PiSyncRoute lastSyncRoute;

        private LifecycleFacet(PiLivingFacetContext context) {
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
