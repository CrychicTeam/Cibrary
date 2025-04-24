package org.crychicteam.cibrary.content.armorset.example;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingChangeTargetEvent;
import org.crychicteam.cibrary.Cibrary;
import org.crychicteam.cibrary.content.armorset.ArmorSet;
import org.crychicteam.cibrary.content.armorset.ISetEffect;
import org.crychicteam.cibrary.content.armorset.capability.ArmorSetCapability;
import org.crychicteam.cibrary.api.event.ItemHurtEffectResult;
import org.crychicteam.cibrary.content.armorset.common.ArmorSetManager;
import org.crychicteam.cibrary.content.key.KeyData;

public class ExampleSetEffect implements ISetEffect {
    @Override
    public void applyEffect(LivingEntity entity) {
        if (entity.level() instanceof ServerLevel level) {
            var lighting = new LightningBolt(EntityType.LIGHTNING_BOLT, level);
            lighting.setPos(entity.position());
            level.addFreshEntity(lighting);
        }
        if (entity instanceof ServerPlayer player) {
            player.getCapability(ArmorSetCapability.ARMOR_SET_CAPABILITY).ifPresent(cap ->{
                ArmorSet activateSet = cap.getActiveSet();
                activateSet.getEquippedItems(player).forEach((slot,item)->{
                    if (item.is(Items.DIAMOND_HELMET)) {
                        player.getCooldowns().addCooldown(item.getItem(), 20);
                    }
                });
            });
        }
    }

    @Override
    public void landEffect(LivingEntity entity, double distance, BlockState landingBlock, BlockPos pos) {
        if (entity.getPersistentData().getBoolean("skillCharging")) {
            entity.level().explode(entity,entity.getX(),entity.getY(),entity.getZ(),10,false, Level.ExplosionInteraction.NONE);
            entity.getPersistentData().putBoolean("skillCharging", false);
        }
    }

    @Override
    public void onTargetedEffect(LivingEntity entity, LivingChangeTargetEvent changer) {
        changer.setCanceled(true);
    }

    @Override
    public ItemHurtEffectResult itemHurtEffect(LivingEntity entity, ItemStack item, int originalDamage) {
        if (item.getMaxDamage() <= originalDamage + item.getDamageValue()) {
            item.setDamageValue(item.getMaxDamage()-1);
            return ItemHurtEffectResult.cancel();
        }
        return ItemHurtEffectResult.unmodified();
    }

    @Override
    public void onSkillPress(ServerPlayer player, KeyData keyData) {}

    @Override
    public void onSkillCharging(ServerPlayer player, KeyData data) {
        var set = ArmorSetManager.getActiveArmorSet(player);
        if (!checkCooldownForSkillCharging(player)) {
            return;
        }
        double scale = data.power > 3.0 ? 0.2 : 0.4;
        double y;
        if (data.power > 3.0 && data.power < 3.2) {
            y = 0.3;
        } else if (data.power > 3.2 && player.fallDistance > 0.1) {
            y = - data.power + 2.0;
            player.getPersistentData().putBoolean("skillCharging", true);
        } else {
            y = player.getDeltaMovement().y;
        }
        player.setDeltaMovement(new Vec3(player.getViewVector(2).scale(scale).x, y , player.getViewVector(2).scale(scale).z));
        player.hurtMarked = true;
    }

    @Override
    public void onSkillRelease(ServerPlayer player, KeyData data) {
//        player.level().explode(player,player.getX(),player.getY(),player.getZ(),10,false, Level.ExplosionInteraction.NONE);
        ArmorSetManager.getActiveArmorSet(player).setSkillCooldown(100);
    }

    @Override
    public void removeEffect(LivingEntity entity) {}

    @Override
    public String getIdentifier() {
        return "example_armor_set_effect";
    }
}
