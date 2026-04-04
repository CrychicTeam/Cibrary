package org.pickaid.pibrary.runtime.capability;

import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.pickaid.pibrary.Pibrary;
import org.pickaid.pibrary.runtime.service.PiLivingServiceDescriptors;

@Mod.EventBusSubscriber(modid = Pibrary.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class PiLivingCapabilityRegistration {
    private PiLivingCapabilityRegistration() {
    }

    @SubscribeEvent
    public static void onRegisterCapabilities(RegisterCapabilitiesEvent event) {
        for (var descriptor : PiLivingServiceDescriptors.generatedDescriptors()) {
            descriptor.registerCapability(event);
        }
    }
}
