package org.pickaid.pibrary.content.events.server;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingEquipmentChangeEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.pickaid.pibrary.content.armorset.ArmorSet;
import org.pickaid.pibrary.content.armorset.capability.ArmorSetCapability;
import org.pickaid.pibrary.content.armorset.common.ArmorSetManager;
import org.pickaid.pibrary.content.armorset.common.ArmorSetUpdater;
import org.pickaid.pibrary.network.PibraryNetworkHandler;

public class ArmorSetHandler {

    @SubscribeEvent
    public void onLivingEquipmentChange(LivingEquipmentChangeEvent event) {
        ArmorSetUpdater.updateEntityISetEffect(event.getEntity());
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            syncArmorSet(serverPlayer);
        }
    }

    @SubscribeEvent
    public void onAttachCapabilities(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof Player) {
            event.addCapability(ArmorSetCapability.ARMOR_SET_CAPABILITY_ID, new ArmorSetCapability.Provider());
        }
    }

    @SubscribeEvent
    public void potionEffectUpdate(TickEvent.PlayerTickEvent event) {
        LivingEntity entity = event.player;
        if (entity instanceof ServerPlayer serverPlayer) {
            ArmorSet armorSet = ArmorSetManager.getActiveArmorSet(serverPlayer);
            armorSet.applyMobEffects(serverPlayer);
        }
    }

    @SubscribeEvent
    public void onPlayerClone(PlayerEvent.Clone event) {
        Player oldPlayer = event.getOriginal();
        Player newPlayer = event.getEntity();
        oldPlayer.getCapability(ArmorSetCapability.ARMOR_SET_CAPABILITY).ifPresent(oldCap -> {
            newPlayer.getCapability(ArmorSetCapability.ARMOR_SET_CAPABILITY).ifPresent(newCap -> {
                newCap.setActiveSet(oldCap.getActiveSet());
                newCap.setState(oldCap.getState());
                newCap.setSkillState(oldCap.getSkillState());
                ArmorSetUpdater.updateEntityISetEffect(newPlayer);
                if (newPlayer instanceof ServerPlayer serverPlayer) {
                    syncArmorSet(serverPlayer);
                }
            });
        });
    }

    @SubscribeEvent
    public void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            syncArmorSet(serverPlayer);
        }
    }

    @SubscribeEvent
    public void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            syncArmorSet(serverPlayer);
        }
    }

    @SubscribeEvent
    public void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            syncArmorSet(serverPlayer);
        }
    }

    public static void syncArmorSet(ServerPlayer player) {
        player.getCapability(ArmorSetCapability.ARMOR_SET_CAPABILITY).ifPresent(cap -> {
            PibraryNetworkHandler.sendArmorSetSync(player, cap);
        });
    }
}