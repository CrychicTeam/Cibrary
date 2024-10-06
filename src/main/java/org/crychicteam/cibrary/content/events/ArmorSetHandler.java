package org.crychicteam.cibrary.content.events;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.living.LivingEquipmentChangeEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.crychicteam.cibrary.Cibrary;
import org.crychicteam.cibrary.content.armorset.ArmorSet;
import org.crychicteam.cibrary.content.armorset.capability.ArmorSetCapability;

@Mod.EventBusSubscriber(modid = Cibrary.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ArmorSetHandler {

    @SubscribeEvent
    public void onLivingEquipmentChange(LivingEquipmentChangeEvent event) {
        Cibrary.ARMOR_SET_MANAGER.updateEntitySetEffect(event.getEntity());
    }

    @SubscribeEvent
    public void onAttachCapabilities(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof LivingEntity) {
            event.addCapability(ArmorSetCapability.ARMOR_SET_CAPABILITY_ID, new ArmorSetCapability());
        }
    }

    @SubscribeEvent
    public void onEntityFinishUsingItem(LivingEntityUseItemEvent.Finish event) {
        LivingEntity entity = event.getEntity();
        if (entity instanceof Player player) {
            player.getCapability(ArmorSetCapability.ARMOR_SET_CAPABILITY).ifPresent(capability-> {
                ArmorSet activeset = capability.getActiveSet();
                activeset.getEffect().releaseEffect(player);
            });
        }
    }

    @SubscribeEvent
    public void onPlayerClone(PlayerEvent.Clone event) {
        Player oldPlayer = event.getOriginal();
        Player newPlayer = event.getEntity();

        oldPlayer.getCapability(ArmorSetCapability.ARMOR_SET_CAPABILITY).ifPresent(oldCap -> {
            newPlayer.getCapability(ArmorSetCapability.ARMOR_SET_CAPABILITY).ifPresent(newCap -> {
                Cibrary.ARMOR_SET_MANAGER.updateEntitySetEffect(newPlayer);
            });
        });
    }

    @SubscribeEvent
    public void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        Cibrary.ARMOR_SET_MANAGER.syncCapability(event.getEntity());
    }

    @SubscribeEvent
    public void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        Cibrary.ARMOR_SET_MANAGER.syncCapability(event.getEntity());
    }

    @SubscribeEvent
    public void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        Cibrary.ARMOR_SET_MANAGER.syncCapability(event.getEntity());
    }
}
