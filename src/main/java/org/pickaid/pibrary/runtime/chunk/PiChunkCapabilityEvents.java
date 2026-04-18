package org.pickaid.pibrary.runtime.chunk;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.pickaid.pibrary.Pibrary;

@Mod.EventBusSubscriber(modid = Pibrary.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class PiChunkCapabilityEvents {
    private PiChunkCapabilityEvents() {
    }

    @SubscribeEvent
    public static void onAttachCapabilities(AttachCapabilitiesEvent<LevelChunk> event) {
        LevelChunk chunk = event.getObject();
        if (!(chunk.getLevel() instanceof ServerLevel)) {
            return;
        }
        for (var descriptor : PiActiveChunkServiceRegistry.activeDescriptors()) {
            var provider = descriptor.createProvider(chunk);
            event.addCapability(descriptor.id(), provider);
            if (provider instanceof PiChunkServiceInstanceProvider<?, ?> instanceProvider) {
                event.addListener(instanceProvider::invalidate);
            }
        }
    }
}
