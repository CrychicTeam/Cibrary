package org.pickaid.pibrary.runtime.capability;

import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.pickaid.pibrary.Pibrary;
import org.pickaid.pibrary.runtime.facet.PiActiveChunkFacetRegistry;
import org.pickaid.pibrary.runtime.facet.PiAttachedChunkFacetContainer;

/**
 * Forge event bridge for generated chunk-facet capability attachment.
 */
@Mod.EventBusSubscriber(modid = Pibrary.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class PiChunkCapabilityEvents {
    private PiChunkCapabilityEvents() {
    }

    @SubscribeEvent
    public static void onAttachCapabilities(AttachCapabilitiesEvent<LevelChunk> event) {
        LevelChunk chunk = event.getObject();
        PiAttachedChunkFacetContainer container = new PiAttachedChunkFacetContainer(chunk);
        for (var descriptor : PiActiveChunkFacetRegistry.activeDescriptors()) {
            var provider = descriptor.createProvider(chunk, container);
            event.addCapability(descriptor.id(), provider);
            if (provider instanceof org.pickaid.pibrary.runtime.facet.PiChunkFacetInstanceProvider<?, ?> instanceProvider) {
                event.addListener(instanceProvider::invalidate);
            }
        }
    }
}
