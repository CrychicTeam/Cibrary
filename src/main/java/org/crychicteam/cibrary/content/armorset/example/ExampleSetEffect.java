package org.crychicteam.cibrary.content.armorset.example;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Items;
import org.crychicteam.cibrary.content.armorset.ArmorSet;
import org.crychicteam.cibrary.content.armorset.SetEffect;
import org.crychicteam.cibrary.content.armorset.capability.ArmorSetCapability;

public class ExampleSetEffect implements SetEffect {
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
    public void removeEffect(LivingEntity entity) {}

    @Override
    public void skillEffect(LivingEntity entity) {}

    @Override
    public void releaseEffect(LivingEntity entity) {}

    @Override
    public String getIdentifier() {
        return "Example";
    }
}
