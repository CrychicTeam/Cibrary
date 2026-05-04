package org.pickaid.pibrary.runtime.facet;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.pickaid.pibrary.api.facet.PiChunkFacetContext;
import org.pickaid.pibrary.api.facet.PiChunkFacetType;
import org.pickaid.pibrary.api.facet.PiChunkFacets;
import org.pickaid.pibrary.api.facet.PiStateChunkFacet;
import org.pickaid.pibrary.dev.example.CounterChunkFacet;
import org.pickaid.pibrary.dev.example.CounterState;

class PiChunkFacetRegistrationTest {
    private static final ResourceLocation DUPLICATE_ID = new ResourceLocation("test", "duplicate_chunk");

    @AfterEach
    void clearRegistry() {
        PiActiveChunkFacetRegistry.clearForTests();
    }

    @Test
    void discoveredFacetIsNotActiveUntilRegistered() {
        assertTrue(PiChunkFacetDescriptors.findGenerated(CounterChunkFacet.class).isPresent());
        assertThrows(IllegalStateException.class, () -> PiChunkFacets.type(CounterChunkFacet.class));
    }

    @Test
    void bindRegisterReturnsStableTypedHandle() {
        PiChunkFacetType<CounterChunkFacet> first = PiChunkFacets.bind(CounterChunkFacet.class).register();
        PiChunkFacetType<CounterChunkFacet> second = PiChunkFacets.bind(CounterChunkFacet.class).register();

        assertSame(first, second);
        assertSame(first, PiChunkFacets.type(CounterChunkFacet.class));
        assertTrue(first.isRegistered());
    }

    @Test
    void duplicateIdRejectionLeavesRegistryClean() {
        DuplicateIdFacetADescriptor firstDescriptor = new DuplicateIdFacetADescriptor();
        DuplicateIdFacetBDescriptor secondDescriptor = new DuplicateIdFacetBDescriptor();

        PiChunkFacetType<DuplicateIdFacetA> registered = PiActiveChunkFacetRegistry.register(firstDescriptor);
        IllegalStateException exception =
                assertThrows(IllegalStateException.class, () -> PiActiveChunkFacetRegistry.register(secondDescriptor));

        assertTrue(exception.getMessage().contains(DUPLICATE_ID.toString()));
        assertSame(registered, PiActiveChunkFacetRegistry.require(DuplicateIdFacetA.class));
        assertTrue(PiActiveChunkFacetRegistry.find(DuplicateIdFacetB.class).isEmpty());
        assertSame(firstDescriptor, PiActiveChunkFacetRegistry.find(DUPLICATE_ID).orElseThrow());
    }

    private static final class DuplicateIdFacetA extends PiStateChunkFacet<CounterState> {
        private DuplicateIdFacetA(PiChunkFacetContext context) {
            super(context);
        }
    }

    private static final class DuplicateIdFacetB extends PiStateChunkFacet<CounterState> {
        private DuplicateIdFacetB(PiChunkFacetContext context) {
            super(context);
        }
    }

    private static final class DuplicateIdFacetADescriptor extends PiGeneratedChunkFacetDescriptor<DuplicateIdFacetA, CounterState> {
        private DuplicateIdFacetADescriptor() {
            super(DUPLICATE_ID, DuplicateIdFacetA.class, CounterState.class);
        }

        @Override
        public Capability<DuplicateIdFacetA> capability() {
            return null;
        }

        @Override
        public DuplicateIdFacetA create(PiChunkFacetContext context) {
            return new DuplicateIdFacetA(context);
        }
    }

    private static final class DuplicateIdFacetBDescriptor extends PiGeneratedChunkFacetDescriptor<DuplicateIdFacetB, CounterState> {
        private DuplicateIdFacetBDescriptor() {
            super(DUPLICATE_ID, DuplicateIdFacetB.class, CounterState.class);
        }

        @Override
        public Capability<DuplicateIdFacetB> capability() {
            return null;
        }

        @Override
        public DuplicateIdFacetB create(PiChunkFacetContext context) {
            return new DuplicateIdFacetB(context);
        }
    }
}
