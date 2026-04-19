package org.pickaid.pibrary.runtime.chunk;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.pickaid.pibrary.api.registrate.PiRegistrate;
import org.pickaid.pibrary.api.registrate.PiRegistrateTestSupport;
import org.pickaid.pibrary.api.service.PiChunkServiceType;
import org.pickaid.pibrary.api.service.PiChunkServices;
import org.pickaid.pibrary.dev.example.CounterState;
import org.pickaid.pibrary.dev.example.CounterChunkService;

class PiChunkServiceRegistrationTest {
    @BeforeEach
    void resetRegistryBeforeTest() {
        PiActiveChunkServiceRegistry.clearForTests();
    }

    @AfterEach
    void clearRegistry() {
        PiActiveChunkServiceRegistry.clearForTests();
    }

    @Test
    void discoveredServiceIsNotActiveUntilRegistered() {
        assertTrue(PiChunkServiceDescriptors.findGenerated(CounterChunkService.class).isPresent());
        assertThrows(IllegalStateException.class, () -> PiChunkServices.type(CounterChunkService.class));
    }

    @Test
    void hostRegisterReturnsStableTypedHandle() {
        PiChunkServiceType<CounterChunkService> first = PiChunkServices.host(CounterChunkService.class).register();
        PiChunkServiceType<CounterChunkService> second = PiChunkServices.host(CounterChunkService.class).register();

        assertSame(first, second);
        assertSame(first, PiChunkServices.type(CounterChunkService.class));
        assertTrue(first.isRegistered());
    }

    @Test
    void registrateChunkEntryReturnsStableTypedHandle() {
        PiRegistrate registrate = PiRegistrateTestSupport.create("pibrary");

        PiChunkServiceType<CounterChunkService> first = registrate
                .chunkService("counter_chunk", CounterState.class, CounterChunkService::new)
                .trackingSync()
                .persisted()
                .register(CounterChunkService.class);
        PiChunkServiceType<CounterChunkService> second = registrate
                .chunkService("counter_chunk", CounterState.class, CounterChunkService::new)
                .register(CounterChunkService.class);

        assertSame(first, second);
        assertSame(first, PiChunkServices.type(CounterChunkService.class));
        assertTrue(first.isRegistered());
    }

    @Test
    void lateRegistrationIsRejectedOnceCapabilityRegistrationCloses() {
        PiChunkCapabilityRegistration.onRegisterCapabilities(null);

        IllegalStateException exception =
                assertThrows(IllegalStateException.class, () -> PiChunkServices.host(CounterChunkService.class).register());

        assertTrue(exception.getMessage().contains("closed"));
        assertTrue(PiActiveChunkServiceRegistry.find(CounterChunkService.class).isEmpty());
    }
}
