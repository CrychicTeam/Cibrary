package org.pickaid.pibrary.runtime.projectile;

import net.minecraft.world.entity.projectile.Projectile;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.pickaid.pibrary.Pibrary;
import org.pickaid.pibrary.api.projectile.PiProjectileLifecycleContext;
import org.pickaid.pibrary.api.projectile.PiProjectileLifecycleServices;

/**
 * Forge event bridge for projectile join-level lifecycle callbacks.
 */
@Mod.EventBusSubscriber(modid = Pibrary.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class PiProjectileLifecycleEvents {
    private PiProjectileLifecycleEvents() {
    }

    /**
     * Bridges projectile {@link EntityJoinLevelEvent} events into the installed lifecycle service.
     *
     * @param event Forge entity join event
     */
    @SubscribeEvent
    public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (!(event.getEntity() instanceof Projectile projectile)) {
            return;
        }
        PiProjectileLifecycleServices.find().ifPresent(service ->
                service.onJoinLevel(projectile, new PiProjectileLifecycleContext(event.loadedFromDisk())));
    }
}
