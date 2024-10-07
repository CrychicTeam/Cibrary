package org.crychicteam.cibrary.api.registry.armorset;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.Item;
import org.crychicteam.cibrary.content.armorset.ArmorSet;
import org.crychicteam.cibrary.content.armorset.SetEffect;

public class ArmorSetBuilder {
    private final ArmorSet armorSet;

    private ArmorSetBuilder(ArmorSet armorSet) {
        this.armorSet =  armorSet;
    }

    private ArmorSetBuilder(ArmorSet armorSet, SetEffect effect) {
        this.armorSet = armorSet != null ? armorSet : new ArmorSet("default", effect);
    }

    public static ArmorSetBuilder create(String identifier) {
        return new ArmorSetBuilder(new ArmorSet(identifier));
    }

    public static ArmorSetBuilder create(String identifier, SetEffect effect) {
        return new ArmorSetBuilder(new ArmorSet(identifier, effect), effect);
    }

    public static ArmorSetBuilder create(ArmorSet armorSet) {
        return new ArmorSetBuilder(armorSet);
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

    public ArmorSetBuilder addRequiredCurio(Item item, int count) {
        armorSet.addCurioItem(item, count);
        return this;
    }

    public ArmorSet build() {
        return ArmorSetRegistry.register(armorSet);
    }
}
