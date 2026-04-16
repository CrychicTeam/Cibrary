package org.pickaid.pibrary.runtime.state;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.pickaid.pibrary.api.service.PiLivingServiceContext;
import org.pickaid.pibrary.api.service.PiStateLivingEntityService;
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
}
