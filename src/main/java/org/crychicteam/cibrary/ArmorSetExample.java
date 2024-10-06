package org.crychicteam.cibrary;

import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Items;
import org.crychicteam.cibrary.api.registry.armorset.ArmorSetBuilder;
import org.crychicteam.cibrary.armorset.ArmorSet;
import org.crychicteam.cibrary.armorset.DefaultSetEffect;

public class ArmorSetExample {
    static {
        ArmorSet DIMOND = ArmorSetBuilder.create("diamond", new DefaultSetEffect())
                .addEquipment(EquipmentSlot.HEAD, Items.DIAMOND_HELMET)
                .addEquipment(EquipmentSlot.CHEST, Items.DIAMOND_CHESTPLATE)
                .addEquipment(EquipmentSlot.LEGS, Items.DIAMOND_LEGGINGS)
                .addEquipment(EquipmentSlot.FEET, Items.DIAMOND_BOOTS)
                .addEffect(MobEffects.DAMAGE_BOOST, 1)
                .addEffect(MobEffects.MOVEMENT_SPEED, 0)
                .addAttribute(Attributes.MAX_HEALTH, "dragon_health_boost", 10.0, AttributeModifier.Operation.ADDITION)
                .addAttribute(Attributes.ATTACK_DAMAGE, "dragon_damage_boost", 0.2, AttributeModifier.Operation.MULTIPLY_TOTAL)
                .build();
    }

    /**
     * You should use ArmorSetExample.init() to register this Armor Set.
     */
    public static void init() {}
}