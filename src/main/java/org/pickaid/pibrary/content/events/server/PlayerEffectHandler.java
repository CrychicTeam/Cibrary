package org.pickaid.pibrary.content.events.server;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ItemStackedOnOtherEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingChangeTargetEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.pickaid.pibrary.Pibrary;
import org.pickaid.pibrary.api.common.ServerKeyManager;
import org.pickaid.pibrary.api.effect.IPlayerEffect;
import org.pickaid.pibrary.api.event.ConfiguredKeyEvent;
import org.pickaid.pibrary.api.event.ItemDamageEvent;
import org.pickaid.pibrary.api.event.ItemHurtEffectResult;
import org.pickaid.pibrary.api.event.StandOnFluidEvent;
import org.pickaid.pibrary.content.key.KeyData;

import java.util.Map;

public class PlayerEffectHandler {

    @SubscribeEvent
    public void onEntityFinishUsingItem(LivingEntityUseItemEvent.Finish event) {
        for (Map.Entry<LivingEntity, IPlayerEffect> entry : Pibrary.activeEffects.entrySet()) {
            if (!entry.getKey().equals(event.getEntity())) continue;
            entry.getValue().releaseEffect(entry.getKey());
        }
    }

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.player instanceof ServerPlayer player) {
            for (Map.Entry<LivingEntity, IPlayerEffect> entry : Pibrary.activeEffects.entrySet()) {
                if (!entry.getKey().equals(player)) continue;
                if (player.isSprinting()) {
                    entry.getValue().sprintingEffect(player);
                } else {
                    entry.getValue().normalTickingEffect(player);
                }
                if (!player.isBlocking()) {
                    entry.getValue().blockingEffect(player);
                }
                if (!player.isCrouching()) {
                    entry.getValue().crouchingEffect(player);
                }
            }
            if (!ServerKeyManager.chargingKeys.isEmpty()) {
                for (ResourceLocation resourceLocation : ServerKeyManager.chargingKeys) {
                    if (ServerKeyManager.isCharging(player, resourceLocation)) {
                        float power = ServerKeyManager.getPower(player, resourceLocation);
                        KeyData keyData = ServerKeyManager.getKeyData(player, resourceLocation);
                        MinecraftForge.EVENT_BUS.post(new ConfiguredKeyEvent.Charging(player, keyData));
                    }
                }
                for (ResourceLocation resourceLocation : ServerKeyManager.heldClickKeys) {
                    if (ServerKeyManager.isHeld(player, resourceLocation)) {
                        float power = ServerKeyManager.getPower(player, resourceLocation);
                        KeyData keyData = ServerKeyManager.getKeyData(player, resourceLocation);
                        MinecraftForge.EVENT_BUS.post(new ConfiguredKeyEvent.Held(player, keyData));
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void onItemHurt(ItemDamageEvent<LivingEntity> event) {
        LivingEntity entity = event.getEntity();
        if (entity instanceof Player player) {
            for (Map.Entry<LivingEntity, IPlayerEffect> entry : Pibrary.activeEffects.entrySet()) {
                if (!entry.getKey().equals(entity)) continue;
                ItemStack itemStack = event.getItemStack();
                int damage = event.getDamage();
                ItemHurtEffectResult result = entry.getValue().itemHurtEffect(player, itemStack, damage);
                if (result.cancelled()) {
                    event.setCanceled(true);
                } else if (result.damage() != event.getDamage()) {
                    event.setDamage(result.damage());
                }
                if (result.destroyed()) {
                    event.getItemStack().setCount(0);
                }
            }
        }
    }

    @SubscribeEvent
    public void onLivingTargetChange(LivingChangeTargetEvent event) {
        LivingEntity target = event.getNewTarget();
        if (target instanceof Player player) {
            for (Map.Entry<LivingEntity, IPlayerEffect> entry : Pibrary.activeEffects.entrySet()) {
                if (!entry.getKey().equals(player)) continue;
                entry.getValue().onTargetedEffect(player, event);
            }
        }
    }

    @SubscribeEvent
    public void onStandOnLiquid(StandOnFluidEvent event) {
        if (event.getEntity() instanceof Player player) {
            for (Map.Entry<LivingEntity, IPlayerEffect> entry : Pibrary.activeEffects.entrySet()) {
                if (!entry.getKey().equals(player)) continue;
                entry.getValue().onStandOnFluidEffect(player, event);
            }
        }
    }

    @SubscribeEvent
    public void stackedOnOther(ItemStackedOnOtherEvent event) {
        for (Map.Entry<LivingEntity, IPlayerEffect> entry : Pibrary.activeEffects.entrySet()) {
            if (!entry.getKey().equals(event.getPlayer())) continue;
            entry.getValue().stackedOnOther(event.getPlayer(), event);
        }
    }
}