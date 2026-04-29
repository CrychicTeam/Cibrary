package org.pickaid.pibrary.runtime.capability;

import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.pickaid.pibrary.Pibrary;
import org.pickaid.pibrary.runtime.facet.PiActiveChunkFacetRegistry;

/**
 * Forge MOD-bus bridge that registers every generated chunk-facet capability.
 */
@Mod.EventBusSubscriber(modid = Pibrary.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class PiChunkCapabilityRegistration {
    private PiChunkCapabilityRegistration() {
    }

    @SubscribeEvent
    public static void onRegisterCapabilities(RegisterCapabilitiesEvent event) {
        try {
            for (var descriptor : PiActiveChunkFacetRegistry.activeDescriptors()) {
                descriptor.registerCapability(event);
            }
        } finally {
            PiActiveChunkFacetRegistry.closeRegistration();
        }
    }
}
