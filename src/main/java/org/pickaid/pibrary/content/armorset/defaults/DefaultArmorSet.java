package org.pickaid.pibrary.content.armorset.defaults;

import net.minecraft.world.entity.LivingEntity;
import org.pickaid.pibrary.content.armorset.ArmorSet;

public class DefaultArmorSet extends ArmorSet {

    public DefaultArmorSet() {
        super(new DefaultSetEffect());
    }

    @Override
    public boolean matches(LivingEntity entity) {
        return false;
    }
}