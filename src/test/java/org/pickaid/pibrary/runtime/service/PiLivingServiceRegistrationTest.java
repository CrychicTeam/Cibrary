package org.pickaid.pibrary.runtime.service;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.pickaid.pibrary.api.service.PiLivingServiceType;
import org.pickaid.pibrary.api.service.PiLivingServices;
import org.pickaid.pibrary.dev.example.CounterPlayerService;

class PiLivingServiceRegistrationTest {
    @AfterEach
    void clearRegistry() {
        PiActiveLivingServiceRegistry.clearForTests();
    }

    @Test
    void discoveredServiceIsNotActiveUntilRegistered() {
        assertTrue(PiLivingServiceDescriptors.findGenerated(CounterPlayerService.class).isPresent());
        assertThrows(IllegalStateException.class, () -> PiLivingServices.type(CounterPlayerService.class));
        assertSame(Optional.empty(), PiLivingServices.find(null, CounterPlayerService.class));
    }

    @Test
    void hostRegisterReturnsStableTypedHandle() {
        PiLivingServiceType<CounterPlayerService> first = PiLivingServices.host(CounterPlayerService.class).register();
        PiLivingServiceType<CounterPlayerService> second = PiLivingServices.host(CounterPlayerService.class).register();

        assertSame(first, second);
        assertSame(first, PiLivingServices.type(CounterPlayerService.class));
        assertTrue(first.isRegistered());
    }
}
