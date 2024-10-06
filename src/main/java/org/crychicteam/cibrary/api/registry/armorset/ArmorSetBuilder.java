package org.crychicteam.cibrary.api.registry.armorset;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.Item;
import org.crychicteam.cibrary.armorset.ArmorSet;
import org.crychicteam.cibrary.armorset.SetEffect;

public class ArmorSetBuilder {
    private final ArmorSet armorSet;

    private ArmorSetBuilder(String identifier, SetEffect effect) {
        this.armorSet = new ArmorSet(identifier, effect);
    }

    public static ArmorSetBuilder create(String identifier, SetEffect effect) {
        return new ArmorSetBuilder(identifier, effect);
    }

    public ArmorSetBuilder addEquipment(EquipmentSlot slot, Item item) {
        armorSet.addEquipmentItem(slot, item);
        return this;
    }

    public ArmorSetBuilder addEffect(MobEffect effect, int amplifier) {
        armorSet.addEffect(effect, amplifier);
        return this;
    }

    public ArmorSetBuilder addAttribute(Attribute attribute, String name, double amount, AttributeModifier.Operation operation) {
        armorSet.addAttribute(attribute, name, amount, operation);
        return this;
    }

    public ArmorSetBuilder addRequiredCurio(String curioType, int count) {
        armorSet.addRequiredCurio(curioType, count);
        return this;
    }

    public ArmorSet build() {
        return ArmorSetRegistry.register(armorSet);
    }
}