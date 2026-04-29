package org.pickaid.pibrary.runtime.recipe;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

class PiRecipeCachesTest {
    @Test
    void invalidatesRegisteredListenersAndAllowsUnregister() throws Exception {
        PiRecipeCaches caches = new PiRecipeCaches();
        AtomicInteger calls = new AtomicInteger();

        AutoCloseable registration = caches.register(calls::incrementAndGet);
        caches.invalidateAll();
        assertEquals(1, calls.get());

        registration.close();
        caches.invalidateAll();
        assertEquals(1, calls.get());
    }
}
