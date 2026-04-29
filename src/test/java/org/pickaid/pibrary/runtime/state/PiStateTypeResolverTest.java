package org.pickaid.pibrary.runtime.state;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.pickaid.pibrary.api.facet.PiLevelFacetContext;
import org.pickaid.pibrary.api.facet.PiStateLevelFacet;
import org.pickaid.pibrary.api.facet.PiLivingFacetContext;
import org.pickaid.pibrary.api.facet.PiStateLivingEntityFacet;
import org.pickaid.pibrary.dev.example.CounterLevelFacet;
import org.pickaid.pibrary.dev.example.CounterPlayerFacet;
import org.pickaid.pibrary.dev.example.CounterState;

class PiStateTypeResolverTest {
    @Test
    void resolvesDirectPlayerFacetStateType() {
        assertEquals(CounterState.class, PiStateTypeResolver.livingFacetStateType(CounterPlayerFacet.class));
    }

    @Test
    void resolvesStateTypeThroughIntermediateSuperclass() {
        assertEquals(CounterState.class, PiStateTypeResolver.livingFacetStateType(DerivedCounterFacet.class));
    }

    @Test
    void resolvesStateTypeAcrossGenericIntermediateSuperclass() {
        assertEquals(CounterState.class, PiStateTypeResolver.livingFacetStateType(GenericDerivedCounterFacet.class));
    }

    @Test
    void resolvesDirectLevelFacetStateType() {
        assertEquals(CounterState.class, PiStateTypeResolver.levelFacetStateType(CounterLevelFacet.class));
    }

    @Test
    void resolvesLevelStateTypeThroughIntermediateSuperclass() {
        assertEquals(CounterState.class, PiStateTypeResolver.levelFacetStateType(DerivedLevelFacet.class));
    }

    @Test
    void resolvesLevelStateTypeAcrossGenericIntermediateSuperclass() {
        assertEquals(CounterState.class, PiStateTypeResolver.levelFacetStateType(GenericDerivedLevelFacet.class));
    }

    private abstract static class BaseCounterFacet extends PiStateLivingEntityFacet<CounterState> {
        private BaseCounterFacet(PiLivingFacetContext context) {
            super(context);
        }
    }

    private static final class DerivedCounterFacet extends BaseCounterFacet {
        private DerivedCounterFacet(PiLivingFacetContext context) {
            super(context);
        }
    }

    private abstract static class GenericBaseCounterFacet<S> extends PiStateLivingEntityFacet<S> {
        private GenericBaseCounterFacet(PiLivingFacetContext context) {
            super(context);
        }
    }

    private abstract static class GenericBridgeCounterFacet<S> extends GenericBaseCounterFacet<S> {
        private GenericBridgeCounterFacet(PiLivingFacetContext context) {
            super(context);
        }
    }

    private static final class GenericDerivedCounterFacet extends GenericBridgeCounterFacet<CounterState> {
        private GenericDerivedCounterFacet(PiLivingFacetContext context) {
            super(context);
        }
    }

    private abstract static class BaseLevelFacet extends PiStateLevelFacet<CounterState> {
        private BaseLevelFacet(PiLevelFacetContext context) {
            super(context);
        }
    }

    private static final class DerivedLevelFacet extends BaseLevelFacet {
        private DerivedLevelFacet(PiLevelFacetContext context) {
            super(context);
        }
    }

    private abstract static class GenericBaseLevelFacet<S> extends PiStateLevelFacet<S> {
        private GenericBaseLevelFacet(PiLevelFacetContext context) {
            super(context);
        }
    }

    private abstract static class GenericBridgeLevelFacet<S> extends GenericBaseLevelFacet<S> {
        private GenericBridgeLevelFacet(PiLevelFacetContext context) {
            super(context);
        }
    }

    private static final class GenericDerivedLevelFacet extends GenericBridgeLevelFacet<CounterState> {
        private GenericDerivedLevelFacet(PiLevelFacetContext context) {
            super(context);
        }
    }
}
