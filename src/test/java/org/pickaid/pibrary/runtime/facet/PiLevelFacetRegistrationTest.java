package org.pickaid.pibrary.runtime.facet;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.pickaid.pibrary.api.facet.PiLevelFacetContext;
import org.pickaid.pibrary.api.facet.PiLevelFacetStorage;
import org.pickaid.pibrary.api.facet.PiLevelFacetStorages;
import org.pickaid.pibrary.api.facet.PiLevelFacetType;
import org.pickaid.pibrary.api.facet.PiLevelFacets;
import org.pickaid.pibrary.api.facet.PiStateLevelFacet;
import org.pickaid.pibrary.dev.example.CounterLevelFacet;
import org.pickaid.pibrary.dev.example.CounterState;

class PiLevelFacetRegistrationTest {
    private static final ResourceLocation DUPLICATE_ID = ResourceLocation.fromNamespaceAndPath("test", "duplicate_level");

    @AfterEach
    void clearRegistry() {
        PiActiveLevelFacetRegistry.clearForTests();
    }

    @Test
    void discoveredFacetIsNotActiveUntilRegistered() {
        assertTrue(PiLevelFacetDescriptors.findGenerated(CounterLevelFacet.class).isPresent());
        assertThrows(IllegalStateException.class, () -> PiLevelFacets.type(CounterLevelFacet.class));
    }

    @Test
    void bindRegisterReturnsStableTypedHandle() {
        PiLevelFacetType<CounterLevelFacet> first = PiLevelFacets.bind(CounterLevelFacet.class).register();
        PiLevelFacetType<CounterLevelFacet> second = PiLevelFacets.bind(CounterLevelFacet.class).register();

        assertSame(first, second);
        assertSame(first, PiLevelFacets.type(CounterLevelFacet.class));
        assertTrue(first.isRegistered());
    }

    @Test
    void duplicateIdRejectionLeavesRegistryClean() {
        DuplicateIdFacetADescriptor firstDescriptor = new DuplicateIdFacetADescriptor();
        DuplicateIdFacetBDescriptor secondDescriptor = new DuplicateIdFacetBDescriptor();

        PiLevelFacetType<DuplicateIdFacetA> registered = PiActiveLevelFacetRegistry.register(firstDescriptor);
        IllegalStateException exception =
                assertThrows(IllegalStateException.class, () -> PiActiveLevelFacetRegistry.register(secondDescriptor));

        assertTrue(exception.getMessage().contains(DUPLICATE_ID.toString()));
        assertSame(registered, PiActiveLevelFacetRegistry.require(DuplicateIdFacetA.class));
        assertTrue(PiActiveLevelFacetRegistry.find(DuplicateIdFacetB.class).isEmpty());
        assertSame(firstDescriptor, PiActiveLevelFacetRegistry.find(DUPLICATE_ID).orElseThrow());
    }

    @Test
    void descriptorFindDelegatesToStorageFindInsteadOfResolve() {
        PiLevelFacetStorage previousStorage = PiLevelFacetStorages.find().orElse(null);
        try {
            CountingLevelStorage storage = new CountingLevelStorage();
            PiLevelFacetStorages.install(storage);

            assertTrue(new DuplicateIdFacetADescriptor().find(null).isEmpty());
            assertTrue(storage.findCalls == 1);
            assertTrue(storage.resolveCalls == 0);
        } finally {
            PiLevelFacetStorages.install(previousStorage);
        }
    }

    private static final class DuplicateIdFacetA extends PiStateLevelFacet<CounterState> {
        private DuplicateIdFacetA(PiLevelFacetContext context) {
            super(context);
        }
    }

    private static final class DuplicateIdFacetB extends PiStateLevelFacet<CounterState> {
        private DuplicateIdFacetB(PiLevelFacetContext context) {
            super(context);
        }
    }

    private static final class DuplicateIdFacetADescriptor extends PiGeneratedLevelFacetDescriptor<DuplicateIdFacetA, CounterState> {
        private DuplicateIdFacetADescriptor() {
            super(DUPLICATE_ID, DuplicateIdFacetA.class, CounterState.class);
        }

        @Override
        public DuplicateIdFacetA create(PiLevelFacetContext context) {
            return new DuplicateIdFacetA(context);
        }
    }

    private static final class DuplicateIdFacetBDescriptor extends PiGeneratedLevelFacetDescriptor<DuplicateIdFacetB, CounterState> {
        private DuplicateIdFacetBDescriptor() {
            super(DUPLICATE_ID, DuplicateIdFacetB.class, CounterState.class);
        }

        @Override
        public DuplicateIdFacetB create(PiLevelFacetContext context) {
            return new DuplicateIdFacetB(context);
        }
    }

    private static final class CountingLevelStorage implements PiLevelFacetStorage {
        private int findCalls;
        private int resolveCalls;

        @Override
        public <T extends PiStateLevelFacet<?>> java.util.Optional<T> find(
                net.minecraft.server.level.ServerLevel level,
                org.pickaid.pibrary.api.facet.PiLevelFacetDescriptor<T, ?> descriptor
        ) {
            findCalls++;
            return java.util.Optional.empty();
        }

        @Override
        public <T extends PiStateLevelFacet<?>> T resolve(
                net.minecraft.server.level.ServerLevel level,
                org.pickaid.pibrary.api.facet.PiLevelFacetDescriptor<T, ?> descriptor
        ) {
            resolveCalls++;
            return descriptor.create(new PiLevelFacetContext(level, org.pickaid.pibrary.api.core.PibraryServices.create()));
        }
    }
}
