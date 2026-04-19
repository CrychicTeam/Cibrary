package org.pickaid.pibrary.runtime.level;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.pickaid.pibrary.api.registrate.PiRegistrate;
import org.pickaid.pibrary.api.registrate.PiRegistrateTestSupport;
import org.pickaid.pibrary.api.service.PiLevelServiceContext;
import org.pickaid.pibrary.api.service.PiLevelServiceStorage;
import org.pickaid.pibrary.api.service.PiLevelServiceStorages;
import org.pickaid.pibrary.api.service.PiLevelServiceType;
import org.pickaid.pibrary.api.service.PiLevelServices;
import org.pickaid.pibrary.api.service.PiStateLevelService;
import org.pickaid.pibrary.dev.example.CounterLevelService;
import org.pickaid.pibrary.dev.example.CounterState;

class PiLevelServiceRegistrationTest {
    private static final ResourceLocation DUPLICATE_ID = ResourceLocation.fromNamespaceAndPath("test", "duplicate_level");

    @BeforeEach
    void resetRegistryBeforeTest() {
        PiActiveLevelServiceRegistry.clearForTests();
    }

    @AfterEach
    void clearRegistry() {
        PiActiveLevelServiceRegistry.clearForTests();
    }

    @Test
    void discoveredServiceIsNotActiveUntilRegistered() {
        assertTrue(PiLevelServiceDescriptors.findGenerated(CounterLevelService.class).isPresent());
        assertThrows(IllegalStateException.class, () -> PiLevelServices.type(CounterLevelService.class));
    }

    @Test
    void hostRegisterReturnsStableTypedHandle() {
        PiLevelServiceType<CounterLevelService> first = PiLevelServices.host(CounterLevelService.class).register();
        PiLevelServiceType<CounterLevelService> second = PiLevelServices.host(CounterLevelService.class).register();

        assertSame(first, second);
        assertSame(first, PiLevelServices.type(CounterLevelService.class));
        assertTrue(first.isRegistered());
    }

    @Test
    void registrateLevelEntryReturnsStableTypedHandle() {
        PiRegistrate registrate = PiRegistrateTestSupport.create("pibrary");

        PiLevelServiceType<CounterLevelService> first = registrate
                .levelService("counter_level", CounterState.class, CounterLevelService::new)
                .noSyncByDefault()
                .persisted()
                .register(CounterLevelService.class);
        PiLevelServiceType<CounterLevelService> second = registrate
                .levelService("counter_level", CounterState.class, CounterLevelService::new)
                .register(CounterLevelService.class);

        assertSame(first, second);
        assertSame(first, PiLevelServices.type(CounterLevelService.class));
        assertTrue(first.isRegistered());
    }

    @Test
    void duplicateIdRejectionLeavesRegistryClean() {
        DuplicateIdServiceADescriptor firstDescriptor = new DuplicateIdServiceADescriptor();
        DuplicateIdServiceBDescriptor secondDescriptor = new DuplicateIdServiceBDescriptor();

        PiLevelServiceType<DuplicateIdServiceA> registered = PiActiveLevelServiceRegistry.register(firstDescriptor);
        IllegalStateException exception =
                assertThrows(IllegalStateException.class, () -> PiActiveLevelServiceRegistry.register(secondDescriptor));

        assertTrue(exception.getMessage().contains(DUPLICATE_ID.toString()));
        assertSame(registered, PiActiveLevelServiceRegistry.require(DuplicateIdServiceA.class));
        assertTrue(PiActiveLevelServiceRegistry.find(DuplicateIdServiceB.class).isEmpty());
        assertSame(firstDescriptor, PiActiveLevelServiceRegistry.find(DUPLICATE_ID).orElseThrow());
    }

    @Test
    void descriptorFindDelegatesToStorageFindInsteadOfResolve() {
        PiLevelServiceStorage previousStorage = PiLevelServiceStorages.find().orElse(null);
        try {
            CountingLevelStorage storage = new CountingLevelStorage();
            PiLevelServiceStorages.install(storage);

            assertTrue(new DuplicateIdServiceADescriptor().find(null).isEmpty());
            assertTrue(storage.findCalls == 1);
            assertTrue(storage.resolveCalls == 0);
        } finally {
            PiLevelServiceStorages.install(previousStorage);
        }
    }

    private static final class DuplicateIdServiceA extends PiStateLevelService<CounterState> {
        private DuplicateIdServiceA(PiLevelServiceContext context) {
            super(context);
        }
    }

    private static final class DuplicateIdServiceB extends PiStateLevelService<CounterState> {
        private DuplicateIdServiceB(PiLevelServiceContext context) {
            super(context);
        }
    }

    private static final class DuplicateIdServiceADescriptor extends PiGeneratedLevelServiceDescriptor<DuplicateIdServiceA, CounterState> {
        private DuplicateIdServiceADescriptor() {
            super(DUPLICATE_ID, DuplicateIdServiceA.class, CounterState.class);
        }

        @Override
        public DuplicateIdServiceA create(PiLevelServiceContext context) {
            return new DuplicateIdServiceA(context);
        }
    }

    private static final class DuplicateIdServiceBDescriptor extends PiGeneratedLevelServiceDescriptor<DuplicateIdServiceB, CounterState> {
        private DuplicateIdServiceBDescriptor() {
            super(DUPLICATE_ID, DuplicateIdServiceB.class, CounterState.class);
        }

        @Override
        public DuplicateIdServiceB create(PiLevelServiceContext context) {
            return new DuplicateIdServiceB(context);
        }
    }

    private static final class CountingLevelStorage implements PiLevelServiceStorage {
        private int findCalls;
        private int resolveCalls;

        @Override
        public <T extends PiStateLevelService<?>> java.util.Optional<T> find(
                net.minecraft.server.level.ServerLevel level,
                org.pickaid.pibrary.api.service.PiLevelServiceDescriptor<T, ?> descriptor
        ) {
            findCalls++;
            return java.util.Optional.empty();
        }

        @Override
        public <T extends PiStateLevelService<?>> T resolve(
                net.minecraft.server.level.ServerLevel level,
                org.pickaid.pibrary.api.service.PiLevelServiceDescriptor<T, ?> descriptor
        ) {
            resolveCalls++;
            return descriptor.create(new PiLevelServiceContext(level, org.pickaid.pibrary.api.core.PibraryServices.create()));
        }
    }
}
