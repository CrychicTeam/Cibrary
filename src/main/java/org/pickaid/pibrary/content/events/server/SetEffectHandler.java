package org.pickaid.pibrary.content.events.server;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ItemStackedOnOtherEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingChangeTargetEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.pickaid.pibrary.Pibrary;
import org.pickaid.pibrary.api.common.ServerKeyManager;
import org.pickaid.pibrary.api.event.ConfiguredKeyEvent;
import org.pickaid.pibrary.api.event.ItemDamageEvent;
import org.pickaid.pibrary.api.event.ItemHurtEffectResult;
import org.pickaid.pibrary.api.event.StandOnFluidEvent;
import org.pickaid.pibrary.content.armorset.ArmorSet;
import org.pickaid.pibrary.content.armorset.ISetEffect;
import org.pickaid.pibrary.content.armorset.common.ArmorSetManager;
import org.pickaid.pibrary.content.armorset.integration.CuriosIntegration;
import org.pickaid.pibrary.kubejs.ConfiguredKeyEventHelper;

import java.util.Map;
import java.util.Set;

import static org.pickaid.pibrary.content.events.server.ArmorSetHandler.syncArmorSet;

public class SetEffectHandler {

    @SubscribeEvent
    public void onEntityFinishUsingItem(LivingEntityUseItemEvent.Finish event) {
        LivingEntity entity = event.getEntity();
        if (entity instanceof Player player) {
            ArmorSet activeSet = ArmorSetManager.getActiveArmorSet(player);
            activeSet.getEffect().releaseEffect(player);
        }
    }

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.player instanceof ServerPlayer player) {
            ArmorSet set = ArmorSetManager.getActiveArmorSet(player);
            ISetEffect oldEffect = set.getEffect();
            ISetEffect newEffect = set.checkEffect(player);
            if (oldEffect != newEffect) {
                oldEffect.removeEffect(player);
                set.setEffect(newEffect);
                newEffect.applyEffect(player);
            }
            if (player.isSprinting()) {
                set.getEffect().sprintingEffect(player);
            } else {
                set.getEffect().normalTickingEffect(player);
            }
            if (!player.isBlocking()) {
                set.getEffect().blockingEffect(player);
            }
            if (!player.isCrouching()) {
                set.getEffect().crouchingEffect(player);
            }

            if (ServerKeyManager.isCharging(player, Pibrary.source("armor_set_skill"))) {
                var keyData = ServerKeyManager.getKeyData(player, Pibrary.source("armor_set_skill"));
                var charging_event = new ConfiguredKeyEvent.Charging(player, keyData);
                MinecraftForge.EVENT_BUS.post(charging_event);
                if (Pibrary.isLoaded("kubejs")) {
                    ConfiguredKeyEventHelper.charging(player, keyData);
                }
                set.getEffect().onSkillCharging(player, keyData);

            }
            if (set.getSkillCooldown() > 0) {
                set.setSkillCooldown(set.getSkillCooldown() - 1);
            }
        }
    }

    @SubscribeEvent
    public void onItemHurt(ItemDamageEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity instanceof Player player) {
            ArmorSet activeSet = ArmorSetManager.getActiveArmorSet(player);
            ItemStack damagedItem = event.getItemStack();
            boolean isSetItem = false;
            for (Map.Entry<EquipmentSlot, Set<Item>> entry : activeSet.getEquipmentItems().entrySet()) {
                if (entry.getValue().contains(damagedItem.getItem())) {
                    isSetItem = true;
                    break;
                }
            }
            if (!isSetItem && CuriosIntegration.isCuriosLoaded) {
                isSetItem = activeSet.getCurioItems().containsKey(damagedItem.getItem());
            }
            if (!isSetItem) {
                isSetItem = damagedItem == player.getMainHandItem() || damagedItem == player.getOffhandItem();
            }
            if (isSetItem) {
                ItemHurtEffectResult result = activeSet.getEffect().itemHurtEffect(player, damagedItem, event.getDamage());
                if (result.cancelled()) {
                    event.setCanceled(true);
                } else if (result.damage() != event.getDamage()) {
                    event.setDamage(result.damage());
                }
                if (result.destroyed()) {
                    damagedItem.setCount(0);
                }
                if (player instanceof ServerPlayer serverPlayer) {
                    syncArmorSet(serverPlayer);
                }
            }
        }
    }

    @SubscribeEvent
    public void onLivingTargetChange(LivingChangeTargetEvent event) {
        LivingEntity target = event.getNewTarget();
        if (target instanceof Player player) {
            ArmorSet activeSet = ArmorSetManager.getActiveArmorSet(player);
            activeSet.getEffect().onTargetedEffect(player, event);
        }
    }

    @SubscribeEvent
    public void onStandOnLiquid(StandOnFluidEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity instanceof Player player) {
            ArmorSet activeSet = ArmorSetManager.getActiveArmorSet(player);
            activeSet.getEffect().onStandOnFluidEffect(player, event);
        }
    }

    @SubscribeEvent
    public void stackedOnOther(ItemStackedOnOtherEvent event) {
        ArmorSet activeSet = ArmorSetManager.getActiveArmorSet(event.getPlayer());
        activeSet.getEffect().stackedOnOther(event.getPlayer(), event);
    }
}