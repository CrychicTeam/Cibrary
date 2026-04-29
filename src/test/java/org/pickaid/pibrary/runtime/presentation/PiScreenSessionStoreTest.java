package org.pickaid.pibrary.runtime.presentation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.pickaid.pibrary.api.presentation.PiPresentationContext;
import org.pickaid.pibrary.api.presentation.PiPresentationSource;
import org.pickaid.pibrary.api.presentation.PiPresentations;
import org.pickaid.pibrary.dev.example.CounterScreenModel;
import org.pickaid.pibrary.dev.example.CounterScreenSession;

class PiScreenSessionsTest {
    @Test
    void screenModelsRefreshWithoutResettingLocalSessionState() {
        TestScreenHost source = new TestScreenHost();
        Object leftOwner = new Object();
        Object rightOwner = new Object();

        CounterScreenModel firstModel = PiPresentations.screens().resolve(source, CounterScreenModel.class, 0.0F);
        CounterScreenSession firstSession = PiPresentations.screens().session(leftOwner, source, CounterScreenSession.class);
        CounterScreenSession secondSession = PiPresentations.screens().session(leftOwner, source, CounterScreenSession.class);
        CounterScreenSession thirdSession = PiPresentations.screens().session(rightOwner, source, CounterScreenSession.class);

        firstSession.selectedTab = 2;
        source.count = 4;
        PiPresentations.invalidateMenuData(source);

        CounterScreenModel secondModel = PiPresentations.screens().resolve(source, CounterScreenModel.class, 0.0F);

        assertEquals(0, firstModel.count());
        assertEquals(4, secondModel.count());
        assertSame(firstSession, secondSession);
        assertEquals(2, secondSession.selectedTab);
        assertNotSame(firstSession, thirdSession);
        assertEquals(0, thirdSession.selectedTab);
    }

    @Test
    void missingScreenSessionRegistrationThrows() {
        MissingSessionHost source = new MissingSessionHost();

        assertThrows(
                IllegalStateException.class,
                () -> PiPresentations.screens().session(new Object(), source, CounterScreenSession.class)
        );
    }

    private static final class TestScreenHost implements PiPresentationSource {
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

    private static final class MissingSessionHost implements PiPresentationSource {
        @Override
        public void contributePresentation(PiPresentationContext context) {
            context.screens().snapshot(
                    CounterScreenModel.class,
                    partialTick -> new CounterScreenModel("Counter", 0, 0)
            );
        }
    }
}
