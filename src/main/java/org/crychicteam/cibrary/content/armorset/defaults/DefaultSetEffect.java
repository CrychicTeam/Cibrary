package org.crychicteam.cibrary.content.armorset.defaults;

import net.minecraft.world.entity.LivingEntity;
import org.crychicteam.cibrary.content.armorset.SetEffect;

public class DefaultSetEffect implements SetEffect {
    public DefaultSetEffect() {
    }

    @Override
    public void applyEffect(LivingEntity entity) {

    }

    @Override
    public void removeEffect(LivingEntity entity) {

    }

    @Override
    public String getIdentifier() {
        return "defaultSetEffect";
    }
}
