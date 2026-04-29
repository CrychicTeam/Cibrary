package org.pickaid.pibrary.runtime.entity;

import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.pickaid.pibrary.Pibrary;
import org.pickaid.pibrary.api.entity.PiEntityLifecycles;

/**
 * Forge event bridge for entity lifecycle callbacks.
 */
@Mod.EventBusSubscriber(modid = Pibrary.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class PiEntityLifecycleEvents {
    private PiEntityLifecycleEvents() {
    }

    /**
     * Bridges {@link EntityJoinLevelEvent} into the installed entity lifecycle registry.
     *
     * @param event Forge entity join event
     */
    @SubscribeEvent
    public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        PiEntityLifecycles.find().ifPresent(lifecycle ->
                lifecycle.onJoinLevel(event.getEntity(), event.getLevel(), event.loadedFromDisk()));
    }

    /**
     * Bridges entity section transitions into the installed lifecycle registry.
     *
     * @param event Forge entity section event
     */
    @SubscribeEvent
    public static void onEntityEnterSection(EntityEvent.EnteringSection event) {
        PiEntityLifecycles.find().ifPresent(lifecycle ->
                lifecycle.onEnterSection(event.getEntity(), event.getOldPos(), event.getNewPos()));
    }
}
