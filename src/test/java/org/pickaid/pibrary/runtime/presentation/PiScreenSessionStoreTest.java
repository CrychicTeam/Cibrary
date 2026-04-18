package org.pickaid.pibrary.runtime.presentation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.pickaid.pibrary.api.presentation.PiPresentationContext;
import org.pickaid.pibrary.api.presentation.PiPresentationHost;
import org.pickaid.pibrary.api.presentation.PiPresentations;
import org.pickaid.pibrary.dev.example.CounterScreenModel;
import org.pickaid.pibrary.dev.example.CounterScreenSession;

class PiScreenSessionStoreTest {
    @Test
    void screenModelsRefreshWithoutResettingLocalSessionState() {
        TestScreenHost host = new TestScreenHost();
        Object leftOwner = new Object();
        Object rightOwner = new Object();

        CounterScreenModel firstModel = PiPresentations.screens().resolve(host, CounterScreenModel.class, 0.0F);
        CounterScreenSession firstSession = PiPresentations.screens().session(leftOwner, host, CounterScreenSession.class);
        CounterScreenSession secondSession = PiPresentations.screens().session(leftOwner, host, CounterScreenSession.class);
        CounterScreenSession thirdSession = PiPresentations.screens().session(rightOwner, host, CounterScreenSession.class);

        firstSession.selectedTab = 2;
        host.count = 4;
        PiPresentations.invalidateMenuData(host);

        CounterScreenModel secondModel = PiPresentations.screens().resolve(host, CounterScreenModel.class, 0.0F);

        assertEquals(0, firstModel.count());
        assertEquals(4, secondModel.count());
        assertSame(firstSession, secondSession);
        assertEquals(2, secondSession.selectedTab);
        assertNotSame(firstSession, thirdSession);
        assertEquals(0, thirdSession.selectedTab);
    }

    @Test
    void missingScreenSessionRegistrationThrows() {
        MissingSessionHost host = new MissingSessionHost();

        assertThrows(
                IllegalStateException.class,
                () -> PiPresentations.screens().session(new Object(), host, CounterScreenSession.class)
        );
    }

    private static final class TestScreenHost implements PiPresentationHost {
        private int count;

        @Override
        public void contributePresentation(PiPresentationContext context) {
            context.screens().snapshot(
                    CounterScreenModel.class,
                    partialTick -> new CounterScreenModel("Counter", count, 0)
            );
            context.screens().session(CounterScreenSession.class, CounterScreenSession::new);
            context.screens().refreshOnMenuData(CounterScreenModel.class);
        }
    }

    private static final class MissingSessionHost implements PiPresentationHost {
        @Override
        public void contributePresentation(PiPresentationContext context) {
            context.screens().snapshot(
                    CounterScreenModel.class,
                    partialTick -> new CounterScreenModel("Counter", 0, 0)
            );
        }
    }
}
