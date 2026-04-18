package org.pickaid.pibrary.runtime.sync;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.pickaid.pibrary.Pibrary;

/**
 * Client-side event bridge used to flush queued living sync packets once entities exist.
 */
@Mod.EventBusSubscriber(modid = Pibrary.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class PiLivingClientEvents {
    private PiLivingClientEvents() {
    }

    /**
     * Applies queued sync packets when a client-side living entity finishes joining the level.
     *
     * @param event Forge entity join event
     */
    @SubscribeEvent
    public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (!event.getLevel().isClientSide()) {
            return;
        }
        Entity entity = event.getEntity();
        if (entity instanceof LivingEntity living) {
            PiPendingLivingSyncs.applyTo(living);
        }
    }

    /**
     * Clears queued sync packets when the local client disconnects.
     *
     * @param event Forge client logout event
     */
    @SubscribeEvent
    public static void onClientLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        PiPendingLivingSyncs.clear();
    }
}
