package org.pickaid.pibrary.runtime.capability;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.pickaid.pinet.api.sync.model.PiSyncRoute;
import org.pickaid.pibrary.Pibrary;
import org.pickaid.pibrary.runtime.service.PiActiveLivingServiceRegistry;
import org.pickaid.pibrary.runtime.service.PiAttachedLivingHost;
import org.pickaid.pibrary.runtime.service.PiLivingServiceLifecycles;
import org.pickaid.pibrary.runtime.sync.PiLivingSyncMessages;

/**
 * Forge event bridge for generated living-service capability attachment, clone
 * handling, ticking, and sync fan-out.
 */
@Mod.EventBusSubscriber(modid = Pibrary.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class PiLivingCapabilityEvents {
    private PiLivingCapabilityEvents() {
    }

    /**
     * Attaches generated living-service capability providers to every living entity.
     *
     * @param event Forge capability attachment event
     */
    @SubscribeEvent
    public static void onAttachCapabilities(AttachCapabilitiesEvent<Entity> event) {
        if (!(event.getObject() instanceof LivingEntity living)) {
            return;
        }
        PiAttachedLivingHost host = new PiAttachedLivingHost(living);
        for (var descriptor : PiActiveLivingServiceRegistry.activeDescriptors()) {
            var provider = descriptor.createProvider(living, host);
            event.addCapability(descriptor.id(), provider);
            if (provider instanceof org.pickaid.pibrary.runtime.service.PiLivingServiceInstanceProvider<?, ?> instanceProvider) {
                event.addListener(instanceProvider::invalidate);
            }
        }
    }

    /**
     * Copies generated service state across player clone events and resends full sync.
     *
     * @param event Forge player clone event
     */
    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        event.getOriginal().reviveCaps();
        try {
            for (var descriptor : PiActiveLivingServiceRegistry.activeDescriptors()) {
                descriptor.copy(event.getOriginal(), event.getEntity(), event.isWasDeath());
            }
        } finally {
            event.getOriginal().invalidateCaps();
        }
        if (event.getEntity() instanceof ServerPlayer player) {
            PiLivingSyncMessages.sendPlayerLifecycleFull(player);
        }
    }

    /**
     * Sends initial full sync to a player after login.
     *
     * @param event Forge login event
     */
    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            PiLivingSyncMessages.sendPlayerLifecycleFull(player);
        }
    }

    /**
     * Resends full sync to a player after respawn.
     *
     * @param event Forge respawn event
     */
    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            PiLivingSyncMessages.sendPlayerLifecycleFull(player);
        }
    }

    /**
     * Resends full sync to a player after dimension travel.
     *
     * @param event Forge dimension change event
     */
    @SubscribeEvent
    public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            PiLivingSyncMessages.sendPlayerLifecycleFull(player);
        }
    }

    /**
     * Sends tracking sync when a player starts tracking a living entity.
     *
     * @param event Forge start-tracking event
     */
    @SubscribeEvent
    public static void onStartTracking(PlayerEvent.StartTracking event) {
        if (event.getEntity() instanceof ServerPlayer tracker && event.getTarget() instanceof LivingEntity living) {
            PiLivingSyncMessages.sendFull(living, tracker, PiSyncRoute.TRACKING);
        }
    }

    /**
     * Runs ticking hooks and flushes server-side dirty sync state.
     *
     * @param event Forge living tick event
     */
    @SubscribeEvent
    public static void onLivingTick(LivingEvent.LivingTickEvent event) {
        LivingEntity living = event.getEntity();
        for (var descriptor : PiActiveLivingServiceRegistry.activeDescriptors()) {
            descriptor.find(living).ifPresent(service -> PiLivingServiceLifecycles.tick(service, living.level().isClientSide()));
        }
        if (living.level().isClientSide()) {
            return;
        }
        PiLivingSyncMessages.flush(living);
    }
}
