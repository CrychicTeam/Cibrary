package org.crychicteam.cibrary.content.events.common;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.MilkBucketItem;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.living.LivingEquipmentChangeEvent;
import net.minecraftforge.event.entity.living.MobEffectEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.crychicteam.cibrary.Cibrary;
import org.crychicteam.cibrary.content.armorset.ArmorSet;
import org.crychicteam.cibrary.content.armorset.capability.ArmorSetCapability;
import org.crychicteam.cibrary.content.armorset.common.ArmorSetManager;
import org.crychicteam.cibrary.network.CibraryNetworkHandler;

import java.util.Objects;

public class ArmorSetHandler {

    @SubscribeEvent
    public void onLivingEquipmentChange(LivingEquipmentChangeEvent event) {
        Cibrary.ARMOR_SET_MANAGER.updateEntitySetEffect(event.getEntity());
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            syncArmorSet(serverPlayer);
        }
    }

    @SubscribeEvent
    public void onAttachCapabilities(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof Player) {
            event.addCapability(ArmorSetCapability.ARMOR_SET_CAPABILITY_ID, new ArmorSetCapability());
        }
    }

    @SubscribeEvent
    public void onMilk(LivingEntityUseItemEvent.Finish event) {
        LivingEntity entity = event.getEntity();
        if (event.getItem().getItem() instanceof MilkBucketItem && entity instanceof ServerPlayer serverPlayer) {
            ArmorSet armorSet = ArmorSetManager.getInstance().getActiveArmorSet(serverPlayer);
            if (armorSet != null) {
                armorSet.removeEffects(serverPlayer);
                armorSet.applyMobEffects(serverPlayer);
            }
        }
    }

    @SubscribeEvent
    public void potionEffectUpdate(TickEvent.PlayerTickEvent event) {
        LivingEntity entity = event.player;
        if (entity instanceof ServerPlayer serverPlayer) {
            ArmorSet armorSet = ArmorSetManager.getInstance().getActiveArmorSet(serverPlayer);
            armorSet.applyMobEffects(serverPlayer);
        }
    }

    @SubscribeEvent
    public void onPlayerClone(PlayerEvent.Clone event) {
        Player oldPlayer = event.getOriginal();
        Player newPlayer = event.getEntity();

        oldPlayer.getCapability(ArmorSetCapability.ARMOR_SET_CAPABILITY).ifPresent(oldCap -> {
            newPlayer.getCapability(ArmorSetCapability.ARMOR_SET_CAPABILITY).ifPresent(newCap -> {
                Cibrary.ARMOR_SET_MANAGER.updateEntitySetEffect(newPlayer);
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
            CibraryNetworkHandler.sendArmorSetSync(player, cap);
        });
    }
}