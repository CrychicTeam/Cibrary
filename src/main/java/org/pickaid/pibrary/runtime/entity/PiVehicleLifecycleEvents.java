package org.pickaid.pibrary.runtime.entity;

import net.minecraftforge.event.entity.EntityMountEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.pickaid.pibrary.Pibrary;
import org.pickaid.pibrary.api.entity.PiVehicleLifecycleServices;

/**
 * Forge event bridge for vehicle mount and dismount callbacks.
 */
@Mod.EventBusSubscriber(modid = Pibrary.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class PiVehicleLifecycleEvents {
    private PiVehicleLifecycleEvents() {
    }

    /**
     * Bridges {@link EntityMountEvent} into the installed vehicle lifecycle service.
     *
     * @param event Forge mount event
     */
    @SubscribeEvent
    public static void onEntityMount(EntityMountEvent event) {
        PiVehicleLifecycleServices.find().ifPresent(service -> {
            if (event.isMounting()) {
                service.onMount(event.getEntityMounting(), event.getEntityBeingMounted(), event.getLevel());
            } else {
                service.onDismount(event.getEntityMounting(), event.getEntityBeingMounted(), event.getLevel());
            }
        });
    }
}
