package org.pickaid.pibrary.runtime.chunk;

import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.pickaid.pibrary.api.service.PiChunkServices;
import org.pickaid.pibrary.dev.example.CounterChunkService;
import org.pickaid.pibrary.dev.example.CounterChunkServices;

class CounterChunkServicesTest {
    @AfterEach
    void clearRegistry() {
        PiActiveChunkServiceRegistry.clearForTests();
    }

    @Test
    void registerActivatesExampleTypedHandle() {
        CounterChunkServices.register();

        assertSame(CounterChunkServices.COUNTER_CHUNK, PiChunkServices.type(CounterChunkService.class));
    }
}
