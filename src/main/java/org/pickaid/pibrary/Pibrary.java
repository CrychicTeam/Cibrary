package org.pickaid.pibrary;

import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.common.Mod;
import org.pickaid.pibrary.dev.example.CounterLivingServices;
import org.pickaid.pibrary.runtime.service.PiLivingServiceDescriptors;
import org.pickaid.pibrary.runtime.sync.PiLivingSyncMessages;
import org.slf4j.Logger;

@Mod(Pibrary.MOD_ID)
public final class Pibrary {
    public static final String MOD_ID = "pibrary";
    public static final Logger LOGGER = LogUtils.getLogger();

    public Pibrary() {
        LOGGER.info("Initializing {}", MOD_ID);
        PiLivingServiceDescriptors.bootstrap();
        CounterLivingServices.register();
        PiLivingSyncMessages.bootstrap();
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }
}
