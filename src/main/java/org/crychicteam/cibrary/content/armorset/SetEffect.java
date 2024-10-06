package org.crychicteam.cibrary.content.armorset;

import net.minecraft.world.entity.LivingEntity;

public interface SetEffect {
    /**
     * Trigger when Apply and Remove this kind of  Armor Set.
     * @param entity
     */
    void applyEffect(LivingEntity entity);
    void removeEffect(LivingEntity entity);

    /**
     * For the user to set up a default skill of this kind of ArmorSet.
     * @param entity
     */
    void skillEffect(LivingEntity entity);

    /**
     * Trigger when the entity finish using an item with this kind of ArmorSet.
     * @param entity
     */
    void releaseEffect(LivingEntity entity);
    String getIdentifier();
}