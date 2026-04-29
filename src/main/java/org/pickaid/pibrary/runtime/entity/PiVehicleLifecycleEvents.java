package org.pickaid.pibrary.runtime.entity;

import net.minecraftforge.event.entity.EntityMountEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.pickaid.pibrary.Pibrary;
import org.pickaid.pibrary.api.entity.PiVehicleLifecycles;

/**
 * Forge event bridge for vehicle mount and dismount callbacks.
 */
@Mod.EventBusSubscriber(modid = Pibrary.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class PiVehicleLifecycleEvents {
    private PiVehicleLifecycleEvents() {
    }

    /**
     * Bridges {@link EntityMountEvent} into the installed vehicle lifecycle registry.
     *
     * @param event Forge mount event
     */
    @SubscribeEvent
    public static void onEntityMount(EntityMountEvent event) {
        PiVehicleLifecycles.find().ifPresent(lifecycle -> {
            if (event.isMounting()) {
                lifecycle.onMount(event.getEntityMounting(), event.getEntityBeingMounted(), event.getLevel());
            } else {
                lifecycle.onDismount(event.getEntityMounting(), event.getEntityBeingMounted(), event.getLevel());
            }
        });
    }
}
