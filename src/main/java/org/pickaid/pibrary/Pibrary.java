package org.pickaid.pibrary;

import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.common.Mod;
import org.pickaid.pibrary.runtime.facet.PiChunkFacetDescriptors;
import org.pickaid.pibrary.runtime.core.PibraryRuntimeBootstrap;
import org.pickaid.pibrary.runtime.facet.PiLevelFacetDescriptors;
import org.pickaid.pibrary.runtime.facet.PiLivingFacetDescriptors;
import org.pickaid.pibrary.runtime.sync.PiLivingSyncMessages;
import org.slf4j.Logger;

@Mod(Pibrary.MOD_ID)
public final class Pibrary {
    public static final String MOD_ID = "pibrary";
    public static final Logger LOGGER = LogUtils.getLogger();

    public Pibrary() {
        LOGGER.info("Initializing {}", MOD_ID);
        PibraryRuntimeBootstrap.bootstrap();
        PiLivingFacetDescriptors.bootstrap();
        PiLevelFacetDescriptors.bootstrap();
        PiChunkFacetDescriptors.bootstrap();
        PiLivingSyncMessages.bootstrap();
    }

    public static ResourceLocation id(String path) {
        return new ResourceLocation(MOD_ID, path);
    }
}
