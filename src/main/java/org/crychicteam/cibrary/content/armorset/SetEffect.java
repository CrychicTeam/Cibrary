package org.crychicteam.cibrary.content.armorset;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.entity.living.LivingChangeTargetEvent;
import org.crychicteam.cibrary.content.event.ItemHurtEffectResult;

public interface SetEffect {
    /**
     * Trigger when Apply this kind of  Armor Set.
     * @param entity
     */
    void applyEffect(LivingEntity entity);

    /**
     * Trigger when Remove this kind of Armor Set.
     * @param entity
     */
    void removeEffect(LivingEntity entity);

    /**
     * For the user to set up a default skill of this kind of ArmorSet.
     *
     * @param entity
     */
    default void skillEffect(LivingEntity entity) {}

    /**
     * Trigger when the entity finish using an item with this kind of ArmorSet.
     *
     * @param entity
     */
    default void releaseEffect(LivingEntity entity) {}

    /**
     * Trigger when entity's armors/curios/items in hands are damaged.
     * Can set the result using this effect.
     * @param entity
     */
    default ItemHurtEffectResult itemHurtEffect(LivingEntity entity, ItemStack item, int originalDamage) { return ItemHurtEffectResult.unmodified(); }

    /**
     * Trigger when the entity start sprinting.
     * @param entity
     */
    default void startSprintingEffect(LivingEntity entity) {}

    /**
     * Trigger when the entity stop sprinting.
     * @param entity
     */
    default void stopSprintingEffect(LivingEntity entity) {}

    default void sprintingEffect(LivingEntity entity) {}

    default void workingEffect(LivingEntity entity) {}

    /**
     * Trigger when the entity jump.
     * @param entity
     */
    default void jumpEffect(LivingEntity entity) {}

    /**
     * Trigger when the entity land.
     * @param entity
     * @param distance
     * @param landingBlock
     * @param pos
     */
    default void landEffect(LivingEntity entity, double distance, BlockState landingBlock, BlockPos pos) {}

    /**
     * Trigger when the entity jump with sprinting.
     * @param entity
     */
    default void sprintingJumpEffect(LivingEntity entity) {}

    /**
     * Trigger when the entity is targeted.
     * @param entity
     * @param changer
     */
    default void onTargetedEffect(LivingEntity entity, LivingChangeTargetEvent changer) {}

    String getIdentifier();
}