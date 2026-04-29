package org.pickaid.pibrary.runtime.facet;

import static org.junit.jupiter.api.Assertions.assertEquals;

import net.minecraft.world.entity.LivingEntity;
import org.junit.jupiter.api.Test;
import org.pickaid.pinet.api.sync.model.PiSyncEnvelopeKind;
import org.pickaid.pinet.api.sync.model.PiSyncRoute;
import org.pickaid.pibrary.api.core.PibraryServiceContext;
import org.pickaid.pibrary.api.core.PibraryServices;
import org.pickaid.pibrary.api.presentation.PiPresentationContext;
import org.pickaid.pibrary.api.presentation.PiPresentationSource;
import org.pickaid.pibrary.api.presentation.PiPresentationScope;
import org.pickaid.pibrary.api.presentation.PiPresentations;
import org.pickaid.pibrary.api.facet.PiLivingFacetContext;
import org.pickaid.pibrary.api.facet.PiLivingFacetContainer;
import org.pickaid.pibrary.api.facet.PiStateLivingEntityFacet;
import org.pickaid.pibrary.api.facet.PiStatePlayerFacet;
import org.pickaid.pibrary.dev.example.CounterHudModel;
import org.pickaid.pibrary.dev.example.CounterState;

class PiLivingPresentationRuntimeTest {
    private static final PibraryServiceContext DETACHED_SERVICES = PibraryServices.create();

    private static final PiLivingFacetContainer DETACHED_CONTAINER = new PiLivingFacetContainer() {
        @Override
        public LivingEntity living() {
            return null;
        }

        @Override
        public PibraryServiceContext services() {
            return DETACHED_SERVICES;
        }

        @Override
        public <T extends PiStateLivingEntityFacet<?>> T get(Class<T> facetClass) {
            throw new UnsupportedOperationException("Detached container does not resolve sibling facets");
        }
    };

    @Test
    void syncAppliedInvalidatesFacetHudProjection() {
        TestCounterPlayerFacet facet = new TestCounterPlayerFacet(new PiLivingFacetContext(null, DETACHED_CONTAINER));

        CounterHudModel first = PiPresentations.hud().resolve(facet, CounterHudModel.class, 0.0F);

        facet.incrementEnergy();
        PiLivingFacetLifecycles.onSyncApplied(facet, PiSyncEnvelopeKind.DELTA, PiSyncRoute.OWNER);

        CounterHudModel second = PiPresentations.hud().resolve(facet, CounterHudModel.class, 0.0F);

        assertEquals(0, first.energy());
        assertEquals(1, second.energy());
    }

    private static final class TestCounterPlayerFacet extends PiStatePlayerFacet<CounterState>
            implements PiPresentationSource {
        private TestCounterPlayerFacet(PiLivingFacetContext context) {
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
