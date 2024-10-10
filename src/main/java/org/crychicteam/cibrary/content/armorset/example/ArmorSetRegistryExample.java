package org.crychicteam.cibrary.content.armorset.example;

import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Items;
import org.crychicteam.cibrary.api.registry.armorset.ArmorSetBuilder;
import org.crychicteam.cibrary.content.armorset.ArmorSet;

public class ArmorSetRegistryExample {
    static {
        ArmorSet DIAMOND = ArmorSetBuilder.create("diamond", new ExampleSetEffect())
                .addEquipment(EquipmentSlot.HEAD, Items.DIAMOND_HELMET)
                .addEquipment(EquipmentSlot.CHEST, Items.DIAMOND_CHESTPLATE)
                .addEquipment(EquipmentSlot.LEGS, Items.DIAMOND_LEGGINGS)
                .addEquipment(EquipmentSlot.FEET, Items.AIR)
                .addRequiredCurio(Items.APPLE, 1)
                .addEffect(MobEffects.DAMAGE_BOOST, 1)
                .addEffect(MobEffects.MOVEMENT_SPEED, 0)
                .addAttribute(Attributes.MAX_HEALTH, "diamond_health_boost", 10.0, AttributeModifier.Operation.ADDITION)
                .addAttribute(Attributes.ATTACK_DAMAGE, "diamond_damage_boost", 0.2, AttributeModifier.Operation.MULTIPLY_TOTAL)
                .build();
    }

    /**
     * You should use ArmorSetExample.init() to register this Armor Set.
     */
    public static void init() {}
}