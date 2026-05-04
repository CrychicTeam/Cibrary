package org.pickaid.pibrary.runtime.facet;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.pickaid.pibrary.api.facet.PiLivingFacetType;
import org.pickaid.pibrary.api.facet.PiLivingFacetContext;
import org.pickaid.pibrary.api.facet.PiLivingFacets;
import org.pickaid.pibrary.dev.example.CounterPlayerFacet;
import org.pickaid.pibrary.dev.example.CounterState;
import org.pickaid.pibrary.runtime.capability.PiLivingCapabilityRegistration;

class PiLivingFacetRegistrationTest {
    private static final ResourceLocation DUPLICATE_ID = new ResourceLocation("test", "duplicate");

    @AfterEach
    void clearRegistry() {
        PiActiveLivingFacetRegistry.clearForTests();
    }

    @Test
    void discoveredFacetIsNotActiveUntilRegistered() {
        assertTrue(PiLivingFacetDescriptors.findGenerated(CounterPlayerFacet.class).isPresent());
        assertThrows(IllegalStateException.class, () -> PiLivingFacets.type(CounterPlayerFacet.class));
        assertTrue(PiLivingFacets.find(null, CounterPlayerFacet.class).isEmpty());
    }

    @Test
    void bindRegisterReturnsStableTypedHandle() {
        PiLivingFacetType<CounterPlayerFacet> first = PiLivingFacets.bind(CounterPlayerFacet.class).register();
        PiLivingFacetType<CounterPlayerFacet> second = PiLivingFacets.bind(CounterPlayerFacet.class).register();

        assertSame(first, second);
        assertSame(first, PiLivingFacets.type(CounterPlayerFacet.class));
        assertTrue(first.isRegistered());
    }

    @Test
    void duplicateIdRejectionLeavesRegistryClean() {
        DuplicateIdFacetADescriptor firstDescriptor = new DuplicateIdFacetADescriptor();
        DuplicateIdFacetBDescriptor secondDescriptor = new DuplicateIdFacetBDescriptor();

        PiLivingFacetType<DuplicateIdFacetA> registered = PiActiveLivingFacetRegistry.register(firstDescriptor);
        IllegalStateException exception =
                assertThrows(IllegalStateException.class, () -> PiActiveLivingFacetRegistry.register(secondDescriptor));

        assertTrue(exception.getMessage().contains(DUPLICATE_ID.toString()));
        assertSame(registered, PiActiveLivingFacetRegistry.require(DuplicateIdFacetA.class));
        assertTrue(PiActiveLivingFacetRegistry.find(DuplicateIdFacetB.class).isEmpty());
        assertSame(firstDescriptor, PiActiveLivingFacetRegistry.find(DUPLICATE_ID).orElseThrow());
        assertTrue(PiActiveLivingFacetRegistry.activeDescriptors().size() == 1);
        assertSame(firstDescriptor, PiActiveLivingFacetRegistry.activeDescriptors().get(0));
    }

    @Test
    void lateRegistrationIsRejectedOnceCapabilityRegistrationCloses() {
        PiLivingCapabilityRegistration.onRegisterCapabilities(null);

        IllegalStateException exception =
                assertThrows(IllegalStateException.class, () -> PiLivingFacets.bind(CounterPlayerFacet.class).register());

        assertTrue(exception.getMessage().contains("closed"));
        assertTrue(PiActiveLivingFacetRegistry.find(CounterPlayerFacet.class).isEmpty());
    }

    private static final class DuplicateIdFacetA extends org.pickaid.pibrary.api.facet.PiStateLivingEntityFacet<CounterState> {
        private DuplicateIdFacetA(PiLivingFacetContext context) {
            super(context);
        }
    }

    private static final class DuplicateIdFacetB extends org.pickaid.pibrary.api.facet.PiStateLivingEntityFacet<CounterState> {
        private DuplicateIdFacetB(PiLivingFacetContext context) {
            super(context);
        }
    }

    private static final class DuplicateIdFacetADescriptor extends PiGeneratedLivingFacetDescriptor<DuplicateIdFacetA, CounterState> {
        private static final class CapabilityHolder {
            private static final Capability<DuplicateIdFacetA> VALUE = CapabilityManager.get(new CapabilityToken<>() {
            });
        }

        private DuplicateIdFacetADescriptor() {
            super(DUPLICATE_ID, DuplicateIdFacetA.class, CounterState.class);
        }

        @Override
        public Capability<DuplicateIdFacetA> capability() {
            return CapabilityHolder.VALUE;
        }

        @Override
        public DuplicateIdFacetA create(PiLivingFacetContext context) {
            return new DuplicateIdFacetA(context);
        }
    }

    private static final class DuplicateIdFacetBDescriptor extends PiGeneratedLivingFacetDescriptor<DuplicateIdFacetB, CounterState> {
        private static final class CapabilityHolder {
            private static final Capability<DuplicateIdFacetB> VALUE = CapabilityManager.get(new CapabilityToken<>() {
            });
        }

        private DuplicateIdFacetBDescriptor() {
            super(DUPLICATE_ID, DuplicateIdFacetB.class, CounterState.class);
        }

        @Override
        public Capability<DuplicateIdFacetB> capability() {
            return CapabilityHolder.VALUE;
        }

        @Override
        public DuplicateIdFacetB create(PiLivingFacetContext context) {
            return new DuplicateIdFacetB(context);
        }
    }
}
