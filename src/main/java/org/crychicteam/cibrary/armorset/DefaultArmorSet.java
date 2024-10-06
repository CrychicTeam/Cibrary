package org.crychicteam.cibrary.armorset;

import net.minecraft.world.entity.LivingEntity;

public class DefaultArmorSet extends ArmorSet {
    public static final String DEFAULT_IDENTIFIER = "default_armor_set";

    public DefaultArmorSet() {
        super(DEFAULT_IDENTIFIER, new DefaultSetEffect());
    }

    @Override
    public boolean matches(LivingEntity entity) {
        return false;
    }
}