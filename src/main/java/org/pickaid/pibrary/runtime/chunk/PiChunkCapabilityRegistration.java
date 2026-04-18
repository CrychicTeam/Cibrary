package org.pickaid.pibrary.runtime.chunk;

import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.pickaid.pibrary.Pibrary;

@Mod.EventBusSubscriber(modid = Pibrary.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class PiChunkCapabilityRegistration {
    private PiChunkCapabilityRegistration() {
    }

    @SubscribeEvent
    public static void onRegisterCapabilities(RegisterCapabilitiesEvent event) {
        try {
            if (event != null) {
                for (var descriptor : PiActiveChunkServiceRegistry.activeDescriptors()) {
                    descriptor.registerCapability(event);
                }
            }
        } finally {
            PiActiveChunkServiceRegistry.closeRegistration();
        }
    }
}
