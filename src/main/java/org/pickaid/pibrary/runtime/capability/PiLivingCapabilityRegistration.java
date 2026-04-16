package org.pickaid.pibrary.runtime.capability;

import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.pickaid.pibrary.Pibrary;
import org.pickaid.pibrary.runtime.service.PiActiveLivingServiceRegistry;

/**
 * Forge MOD-bus bridge that registers every generated living-service capability.
 */
@Mod.EventBusSubscriber(modid = Pibrary.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class PiLivingCapabilityRegistration {
    private PiLivingCapabilityRegistration() {
    }

    /**
     * Registers generated living-service capabilities with Forge.
     *
     * @param event Forge capability registration event
     */
    @SubscribeEvent
    public static void onRegisterCapabilities(RegisterCapabilitiesEvent event) {
        try {
            for (var descriptor : PiActiveLivingServiceRegistry.activeDescriptors()) {
                descriptor.registerCapability(event);
            }
        } finally {
            PiActiveLivingServiceRegistry.closeRegistration();
        }
    }
}
