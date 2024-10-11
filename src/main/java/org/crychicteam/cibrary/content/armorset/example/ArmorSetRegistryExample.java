package org.crychicteam.cibrary.content.armorset.example;

import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Items;
import org.crychicteam.cibrary.api.registry.armorset.ArmorSetBuilder;
import org.crychicteam.cibrary.content.armorset.ArmorSet;
import org.crychicteam.cibrary.content.armorset.defaults.DefaultSetEffect;
import org.crychicteam.cibrary.content.armorset.integration.KubeJSSetEffect;

public class ArmorSetRegistryExample {
    public static ArmorSet DIAMOND;
    static {
        var effect = new KubeJSSetEffect();
        effect.setApplyEffectConsumer(entity -> {
            entity.setSprinting(true);
        });
        effect.setIdentifier("test_effect");
        DIAMOND = ArmorSetBuilder.create("diamond", new ExampleSetEffect())
                .addEquipment(EquipmentSlot.HEAD, Items.DIAMOND_HELMET)
                .addEquipment(EquipmentSlot.CHEST, Items.DIAMOND_CHESTPLATE)
                .addEquipment(EquipmentSlot.LEGS, Items.DIAMOND_LEGGINGS)
                .addEquipment(EquipmentSlot.FEET, Items.AIR)
                .addEffect(MobEffects.DAMAGE_BOOST, 1)
                .addEffect(MobEffects.MOVEMENT_SPEED, 0)
                .addAttribute(Attributes.MAX_HEALTH, "diamond_set_health_boost", 20.0, AttributeModifier.Operation.ADDITION)
                .addAttribute(Attributes.ATTACK_DAMAGE, "diamond_set_damage_boost", 0.3, AttributeModifier.Operation.MULTIPLY_TOTAL)
                .build();
    }

    /**
     * You should use ArmorSetExample.init() to register this Armor Set.
     */
    public static void init() {}
}