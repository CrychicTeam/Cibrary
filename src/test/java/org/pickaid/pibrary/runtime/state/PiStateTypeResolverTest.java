package org.pickaid.pibrary.runtime.state;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.pickaid.pibrary.api.service.PiLevelServiceContext;
import org.pickaid.pibrary.api.service.PiStateLevelService;
import org.pickaid.pibrary.api.service.PiChunkServiceContext;
import org.pickaid.pibrary.api.service.PiStateChunkService;
import org.pickaid.pibrary.api.service.PiLivingServiceContext;
import org.pickaid.pibrary.api.service.PiStateLivingEntityService;
import org.pickaid.pibrary.dev.example.CounterChunkService;
import org.pickaid.pibrary.dev.example.CounterLevelService;
import org.pickaid.pibrary.dev.example.CounterPlayerService;
import org.pickaid.pibrary.dev.example.CounterState;

class PiStateTypeResolverTest {
    @Test
    void resolvesDirectPlayerServiceStateType() {
        assertEquals(CounterState.class, PiStateTypeResolver.livingServiceStateType(CounterPlayerService.class));
    }

    @Test
    void resolvesStateTypeThroughIntermediateSuperclass() {
        assertEquals(CounterState.class, PiStateTypeResolver.livingServiceStateType(DerivedCounterService.class));
    }

    @Test
    void resolvesStateTypeAcrossGenericIntermediateSuperclass() {
        assertEquals(CounterState.class, PiStateTypeResolver.livingServiceStateType(GenericDerivedCounterService.class));
    }

    @Test
    void resolvesDirectLevelServiceStateType() {
        assertEquals(CounterState.class, PiStateTypeResolver.levelServiceStateType(CounterLevelService.class));
    }

    @Test
    void resolvesDirectChunkServiceStateType() {
        assertEquals(CounterState.class, PiStateTypeResolver.chunkServiceStateType(CounterChunkService.class));
    }

    @Test
    void resolvesLevelStateTypeThroughIntermediateSuperclass() {
        assertEquals(CounterState.class, PiStateTypeResolver.levelServiceStateType(DerivedLevelService.class));
    }

    @Test
    void resolvesChunkStateTypeAcrossGenericIntermediateSuperclass() {
        assertEquals(CounterState.class, PiStateTypeResolver.chunkServiceStateType(GenericDerivedChunkService.class));
    }

    @Test
    void resolvesLevelStateTypeAcrossGenericIntermediateSuperclass() {
        assertEquals(CounterState.class, PiStateTypeResolver.levelServiceStateType(GenericDerivedLevelService.class));
    }

    private abstract static class BaseCounterService extends PiStateLivingEntityService<CounterState> {
        private BaseCounterService(PiLivingServiceContext context) {
            super(context);
        }
    }

    private static final class DerivedCounterService extends BaseCounterService {
        private DerivedCounterService(PiLivingServiceContext context) {
            super(context);
        }
    }

    private abstract static class GenericBaseCounterService<S> extends PiStateLivingEntityService<S> {
        private GenericBaseCounterService(PiLivingServiceContext context) {
            super(context);
        }
    }

    private abstract static class GenericBridgeCounterService<S> extends GenericBaseCounterService<S> {
        private GenericBridgeCounterService(PiLivingServiceContext context) {
            super(context);
        }
    }

    private static final class GenericDerivedCounterService extends GenericBridgeCounterService<CounterState> {
        private GenericDerivedCounterService(PiLivingServiceContext context) {
            super(context);
        }
    }

    private abstract static class BaseLevelService extends PiStateLevelService<CounterState> {
        private BaseLevelService(PiLevelServiceContext context) {
            super(context);
        }
    }

    private static final class DerivedLevelService extends BaseLevelService {
        private DerivedLevelService(PiLevelServiceContext context) {
            super(context);
        }
    }

    private abstract static class GenericBaseLevelService<S> extends PiStateLevelService<S> {
        private GenericBaseLevelService(PiLevelServiceContext context) {
            super(context);
        }
    }

    private abstract static class GenericBridgeLevelService<S> extends GenericBaseLevelService<S> {
        private GenericBridgeLevelService(PiLevelServiceContext context) {
            super(context);
        }
    }

    private static final class GenericDerivedLevelService extends GenericBridgeLevelService<CounterState> {
        private GenericDerivedLevelService(PiLevelServiceContext context) {
            super(context);
        }
    }

    private abstract static class BaseChunkService extends PiStateChunkService<CounterState> {
        private BaseChunkService(PiChunkServiceContext context) {
            super(context);
        }
    }

    private static final class DerivedChunkService extends BaseChunkService {
        private DerivedChunkService(PiChunkServiceContext context) {
            super(context);
        }
    }

    private abstract static class GenericBaseChunkService<S> extends PiStateChunkService<S> {
        private GenericBaseChunkService(PiChunkServiceContext context) {
            super(context);
        }
    }

    private abstract static class GenericBridgeChunkService<S> extends GenericBaseChunkService<S> {
        private GenericBridgeChunkService(PiChunkServiceContext context) {
            super(context);
        }
    }

    private static final class GenericDerivedChunkService extends GenericBridgeChunkService<CounterState> {
        private GenericDerivedChunkService(PiChunkServiceContext context) {
            super(context);
        }
    }
}
