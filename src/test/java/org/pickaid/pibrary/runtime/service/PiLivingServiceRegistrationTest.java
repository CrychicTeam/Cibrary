package org.pickaid.pibrary.runtime.service;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.pickaid.pibrary.api.service.PiLivingServiceType;
import org.pickaid.pibrary.api.service.PiLivingServiceContext;
import org.pickaid.pibrary.api.service.PiLivingServices;
import org.pickaid.pibrary.dev.example.CounterPlayerService;
import org.pickaid.pibrary.dev.example.CounterState;
import org.pickaid.pibrary.runtime.capability.PiLivingCapabilityRegistration;

class PiLivingServiceRegistrationTest {
    private static final ResourceLocation DUPLICATE_ID = ResourceLocation.fromNamespaceAndPath("test", "duplicate");

    @AfterEach
    void clearRegistry() {
        PiActiveLivingServiceRegistry.clearForTests();
    }

    @Test
    void discoveredServiceIsNotActiveUntilRegistered() {
        assertTrue(PiLivingServiceDescriptors.findGenerated(CounterPlayerService.class).isPresent());
        assertThrows(IllegalStateException.class, () -> PiLivingServices.type(CounterPlayerService.class));
        assertTrue(PiLivingServices.find(null, CounterPlayerService.class).isEmpty());
    }

    @Test
    void hostRegisterReturnsStableTypedHandle() {
        PiLivingServiceType<CounterPlayerService> first = PiLivingServices.host(CounterPlayerService.class).register();
        PiLivingServiceType<CounterPlayerService> second = PiLivingServices.host(CounterPlayerService.class).register();

        assertSame(first, second);
        assertSame(first, PiLivingServices.type(CounterPlayerService.class));
        assertTrue(first.isRegistered());
    }

    @Test
    void duplicateIdRejectionLeavesRegistryClean() {
        DuplicateIdServiceADescriptor firstDescriptor = new DuplicateIdServiceADescriptor();
        DuplicateIdServiceBDescriptor secondDescriptor = new DuplicateIdServiceBDescriptor();

        PiLivingServiceType<DuplicateIdServiceA> registered = PiActiveLivingServiceRegistry.register(firstDescriptor);
        IllegalStateException exception =
                assertThrows(IllegalStateException.class, () -> PiActiveLivingServiceRegistry.register(secondDescriptor));

        assertTrue(exception.getMessage().contains(DUPLICATE_ID.toString()));
        assertSame(registered, PiActiveLivingServiceRegistry.require(DuplicateIdServiceA.class));
        assertTrue(PiActiveLivingServiceRegistry.find(DuplicateIdServiceB.class).isEmpty());
        assertSame(firstDescriptor, PiActiveLivingServiceRegistry.find(DUPLICATE_ID).orElseThrow());
        assertTrue(PiActiveLivingServiceRegistry.activeDescriptors().size() == 1);
        assertSame(firstDescriptor, PiActiveLivingServiceRegistry.activeDescriptors().get(0));
    }

    @Test
    void lateRegistrationIsRejectedOnceCapabilityRegistrationCloses() {
        PiLivingCapabilityRegistration.onRegisterCapabilities(null);

        IllegalStateException exception =
                assertThrows(IllegalStateException.class, () -> PiLivingServices.host(CounterPlayerService.class).register());

        assertTrue(exception.getMessage().contains("closed"));
        assertTrue(PiActiveLivingServiceRegistry.find(CounterPlayerService.class).isEmpty());
    }

    private static final class DuplicateIdServiceA extends org.pickaid.pibrary.api.service.PiStateLivingEntityService<CounterState> {
        private DuplicateIdServiceA(PiLivingServiceContext context) {
            super(context);
        }
    }

    private static final class DuplicateIdServiceB extends org.pickaid.pibrary.api.service.PiStateLivingEntityService<CounterState> {
        private DuplicateIdServiceB(PiLivingServiceContext context) {
            super(context);
        }
    }

    private static final class DuplicateIdServiceADescriptor extends PiGeneratedLivingServiceDescriptor<DuplicateIdServiceA, CounterState> {
        private static final class CapabilityHolder {
            private static final Capability<DuplicateIdServiceA> VALUE = CapabilityManager.get(new CapabilityToken<>() {
            });
        }

        private DuplicateIdServiceADescriptor() {
            super(DUPLICATE_ID, DuplicateIdServiceA.class, CounterState.class);
        }

        @Override
        public Capability<DuplicateIdServiceA> capability() {
            return CapabilityHolder.VALUE;
        }

        @Override
        public DuplicateIdServiceA create(PiLivingServiceContext context) {
            return new DuplicateIdServiceA(context);
        }
    }

    private static final class DuplicateIdServiceBDescriptor extends PiGeneratedLivingServiceDescriptor<DuplicateIdServiceB, CounterState> {
        private static final class CapabilityHolder {
            private static final Capability<DuplicateIdServiceB> VALUE = CapabilityManager.get(new CapabilityToken<>() {
            });
        }

        private DuplicateIdServiceBDescriptor() {
            super(DUPLICATE_ID, DuplicateIdServiceB.class, CounterState.class);
        }

        @Override
        public Capability<DuplicateIdServiceB> capability() {
            return CapabilityHolder.VALUE;
        }

        @Override
        public DuplicateIdServiceB create(PiLivingServiceContext context) {
            return new DuplicateIdServiceB(context);
        }
    }
}
