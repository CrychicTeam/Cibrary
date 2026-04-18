package org.pickaid.pibrary.runtime.chunk;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.pickaid.pibrary.api.service.PiChunkServiceType;
import org.pickaid.pibrary.api.service.PiChunkServices;
import org.pickaid.pibrary.dev.example.CounterChunkService;

class PiChunkServiceRegistrationTest {
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
    void lateRegistrationIsRejectedOnceCapabilityRegistrationCloses() {
        PiChunkCapabilityRegistration.onRegisterCapabilities(null);

        IllegalStateException exception =
                assertThrows(IllegalStateException.class, () -> PiChunkServices.host(CounterChunkService.class).register());

        assertTrue(exception.getMessage().contains("closed"));
        assertTrue(PiActiveChunkServiceRegistry.find(CounterChunkService.class).isEmpty());
    }
}
