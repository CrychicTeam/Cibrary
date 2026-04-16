package org.pickaid.pibrary.runtime.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import net.minecraft.world.entity.LivingEntity;
import org.junit.jupiter.api.Test;
import org.pickaid.pinet.api.sync.model.PiSyncEnvelopeKind;
import org.pickaid.pinet.api.sync.model.PiSyncRoute;
import org.pickaid.pibrary.api.core.PibraryServiceContext;
import org.pickaid.pibrary.api.core.PibraryServices;
import org.pickaid.pibrary.api.presentation.PiPresentationContext;
import org.pickaid.pibrary.api.presentation.PiPresentationHost;
import org.pickaid.pibrary.api.presentation.PiPresentationScope;
import org.pickaid.pibrary.api.presentation.PiPresentations;
import org.pickaid.pibrary.api.service.PiLivingServiceContext;
import org.pickaid.pibrary.api.service.PiLivingServiceHost;
import org.pickaid.pibrary.api.service.PiStateLivingEntityService;
import org.pickaid.pibrary.api.service.PiStatePlayerService;
import org.pickaid.pibrary.dev.example.CounterHudModel;
import org.pickaid.pibrary.dev.example.CounterState;

class PiLivingPresentationRuntimeTest {
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
    void syncAppliedInvalidatesServiceHudProjection() {
        TestCounterPlayerService service = new TestCounterPlayerService(new PiLivingServiceContext(null, DETACHED_HOST));

        CounterHudModel first = PiPresentations.hud().resolve(service, CounterHudModel.class, 0.0F);

        service.incrementEnergy();
        PiLivingServiceLifecycles.onSyncApplied(service, PiSyncEnvelopeKind.DELTA, PiSyncRoute.OWNER);

        CounterHudModel second = PiPresentations.hud().resolve(service, CounterHudModel.class, 0.0F);

        assertEquals(0, first.energy());
        assertEquals(1, second.energy());
    }

    private static final class TestCounterPlayerService extends PiStatePlayerService<CounterState>
            implements PiPresentationHost {
        private TestCounterPlayerService(PiLivingServiceContext context) {
            super(context);
        }

        private void incrementEnergy() {
            updateState(state -> state.energy++);
        }

        @Override
        public void contributePresentation(PiPresentationContext context) {
            context.hud().snapshot(
                    CounterHudModel.class,
                    PiPresentationScope.OWNER,
                    partialTick -> CounterHudModel.from(viewState())
            );
            context.hud().refreshOnClientApply(CounterHudModel.class);
        }
    }
}
