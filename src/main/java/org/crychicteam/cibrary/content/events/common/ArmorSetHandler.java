package org.crychicteam.cibrary.content.events.common;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MilkBucketItem;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingChangeTargetEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.living.LivingEquipmentChangeEvent;
import net.minecraftforge.event.entity.living.MobEffectEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.crychicteam.cibrary.Cibrary;
import org.crychicteam.cibrary.content.armorset.ArmorSet;
import org.crychicteam.cibrary.content.armorset.ArmorSetManager;
import org.crychicteam.cibrary.content.armorset.capability.ArmorSetCapability;
import org.crychicteam.cibrary.content.armorset.integration.CuriosIntegration;
import org.crychicteam.cibrary.content.event.ItemDamageEvent;
import org.crychicteam.cibrary.content.event.ItemHurtEffectResult;
import org.crychicteam.cibrary.network.CibraryNetworkHandler;

import java.util.Map;
import java.util.Set;

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
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.player instanceof ServerPlayer player && player.isSprinting()) {
            player.getCapability(ArmorSetCapability.ARMOR_SET_CAPABILITY).ifPresent(cap-> {
                ArmorSet set = cap.getActiveSet();
                set.getEffect().sprintingEffect(player);
            });
        } else if (event.player instanceof ServerPlayer player && !player.isSprinting()){
            player.getCapability(ArmorSetCapability.ARMOR_SET_CAPABILITY).ifPresent(cap-> {
                ArmorSet set = cap.getActiveSet();
                set.getEffect().workingEffect(player);
            });
        }
    }

    @SubscribeEvent
    public void onItemHurt(ItemDamageEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity instanceof Player player) {
            player.getCapability(ArmorSetCapability.ARMOR_SET_CAPABILITY).ifPresent(capability -> {
                ArmorSet activeSet = capability.getActiveSet();
                ItemStack damagedItem = event.getItemStack();
                boolean isSetItem = false;
                for (Map.Entry<EquipmentSlot, Item> entry : activeSet.getEquipmentItems().entrySet()) {
                    if (entry.getValue() == damagedItem.getItem()) {
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
            });
        }
    }

    @SubscribeEvent
    public void onLivingTargetChange(LivingChangeTargetEvent event) {
        LivingEntity target = event.getNewTarget();
        if (target instanceof Player player) {
            player.getCapability(ArmorSetCapability.ARMOR_SET_CAPABILITY).ifPresent(capability -> {
                ArmorSet activeSet = capability.getActiveSet();
                activeSet.getEffect().onTargetedEffect(player, event);
            });
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
    public void onPotionEffectExpire(MobEffectEvent.Expired event) {
        LivingEntity entity = event.getEntity();
        if (entity instanceof ServerPlayer serverPlayer) {
            ArmorSet armorSet = ArmorSetManager.getInstance().getActiveArmorSet(serverPlayer);
            if (armorSet != null) {
                armorSet.removeEffects(serverPlayer);
                armorSet.applyMobEffects(serverPlayer);
            }
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