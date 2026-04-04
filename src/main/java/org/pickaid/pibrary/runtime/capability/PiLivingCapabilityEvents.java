package org.pickaid.pibrary.runtime.capability;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.pickaid.pinet.api.sync.PiSyncRoute;
import org.pickaid.pibrary.Pibrary;
import org.pickaid.pibrary.runtime.service.PiLivingServiceDescriptors;
import org.pickaid.pibrary.runtime.sync.PiLivingSyncMessages;

@Mod.EventBusSubscriber(modid = Pibrary.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class PiLivingCapabilityEvents {
    private PiLivingCapabilityEvents() {
    }

    @SubscribeEvent
    public static void onAttachCapabilities(AttachCapabilitiesEvent<Entity> event) {
        if (!(event.getObject() instanceof LivingEntity living)) {
            return;
        }
        for (var descriptor : PiLivingServiceDescriptors.generatedDescriptors()) {
            var provider = descriptor.createProvider(living);
            event.addCapability(descriptor.id(), provider);
            if (provider instanceof org.pickaid.pibrary.runtime.service.PiLivingServiceInstanceProvider<?, ?> instanceProvider) {
                event.addListener(instanceProvider::invalidate);
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        event.getOriginal().reviveCaps();
        try {
            for (var descriptor : PiLivingServiceDescriptors.generatedDescriptors()) {
                descriptor.copy(event.getOriginal(), event.getEntity());
            }
        } finally {
            event.getOriginal().invalidateCaps();
        }
        if (event.getEntity() instanceof ServerPlayer player) {
            PiLivingSyncMessages.sendFull(player, player, PiSyncRoute.OWNER);
        }
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            PiLivingSyncMessages.sendFull(player, player, PiSyncRoute.OWNER);
        }
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            PiLivingSyncMessages.sendFull(player, player, PiSyncRoute.OWNER);
        }
    }

    @SubscribeEvent
    public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            PiLivingSyncMessages.sendFull(player, player, PiSyncRoute.OWNER);
        }
    }

    @SubscribeEvent
    public static void onStartTracking(PlayerEvent.StartTracking event) {
        if (event.getEntity() instanceof ServerPlayer tracker && event.getTarget() instanceof LivingEntity living) {
            PiLivingSyncMessages.sendFull(living, tracker, PiSyncRoute.TRACKING);
        }
    }

    @SubscribeEvent
    public static void onLivingTick(LivingEvent.LivingTickEvent event) {
        LivingEntity living = event.getEntity();
        if (living.level().isClientSide()) {
            return;
        }
        PiLivingSyncMessages.flush(living);
    }
}
