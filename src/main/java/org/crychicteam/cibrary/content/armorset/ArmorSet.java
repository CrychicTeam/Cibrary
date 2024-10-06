package org.crychicteam.cibrary.content.armorset;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import dev.xkmc.l2damagetracker.contents.attack.AttackCache;
import dev.xkmc.l2damagetracker.contents.attack.CreateSourceEvent;
import dev.xkmc.l2damagetracker.contents.attack.PlayerAttackCache;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.CriticalHitEvent;
import org.crychicteam.cibrary.content.armorset.integration.CuriosIntegration;

import java.util.*;
import java.util.function.BiConsumer;

public class ArmorSet {
    private final String identifier;
    private final Map<MobEffect, Integer> effects;
    private final Multimap<Attribute, AttributeModifier> attributes;
    private final SetEffect effect;
    private final Map<EquipmentSlot, Set<Item>> equipmentItems;
    private final Map<String, Integer> requiredCurios;

    public ArmorSet(String identifier, SetEffect effect) {
        this.identifier = identifier;
        this.effect = effect;
        this.effects = new HashMap<>();
        this.attributes = HashMultimap.create();
        this.equipmentItems = new EnumMap<>(EquipmentSlot.class);
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            this.equipmentItems.put(slot, new HashSet<>());
        }
        this.requiredCurios = new HashMap<>();
    }

    public void addEquipmentItem(EquipmentSlot slot, Item item) {
        equipmentItems.get(slot).add(item);
    }

    public void addRequiredCurio(String curioType, int count) {
        requiredCurios.put(curioType, count);
    }

    public void addEffect(MobEffect effect, int amplifier) {
        effects.put(effect, amplifier);
    }

    public void addAttribute(Attribute attribute, String name, double amount, AttributeModifier.Operation operation) {
        AttributeModifier modifier = new AttributeModifier(UUID.randomUUID(), name, amount, operation);
        attributes.put(attribute, modifier);
    }

    public String getIdentifier() {
        return identifier;
    }

    public Map<MobEffect, Integer> getEffects() {
        return effects;
    }

    public Multimap<Attribute, AttributeModifier> getAttributes() {
        return attributes;
    }

    public SetEffect getEffect() {
        return effect;
    }

    public Map<EquipmentSlot, Set<Item>> getEquipmentItems() {
        return Collections.unmodifiableMap(equipmentItems);
    }

    public boolean matches(LivingEntity entity) {
        for (Map.Entry<EquipmentSlot, Set<Item>> entry : equipmentItems.entrySet()) {
            ItemStack equippedItem = entity.getItemBySlot(entry.getKey());
            if (!entry.getValue().isEmpty() && (equippedItem.isEmpty() || !entry.getValue().contains(equippedItem.getItem()))) {
                return false;
            }
        }
        return CuriosIntegration.matchesCurioRequirements(entity, requiredCurios);
    }

    public void onPlayerAttack(PlayerAttackCache cache) {}

    public boolean onCriticalHit(PlayerAttackCache cache, CriticalHitEvent event) {
        return false;
    }

    public void setupProfile(AttackCache attackCache, BiConsumer<LivingEntity, ItemStack> setupProfile) {}

    public void onAttack(AttackCache cache, ItemStack weapon) {}

    public void postAttack(AttackCache cache, LivingAttackEvent event, ItemStack weapon) {}

    public void onHurt(AttackCache cache, ItemStack weapon) {}

    public void onHurtMaximized(AttackCache cache, ItemStack weapon) {}

    public void postHurt(AttackCache cache, LivingHurtEvent event, ItemStack weapon) {}

    public void onDamage(AttackCache cache, ItemStack weapon) {}

    public void onDamageFinalized(AttackCache cache, ItemStack weapon) {}

    public void onCreateSource(CreateSourceEvent event) {}

    public void applyEffectsAndAttributes(LivingEntity entity) {
        for (Map.Entry<MobEffect, Integer> entry : effects.entrySet()) {
            entity.addEffect(new MobEffectInstance(entry.getKey(), Integer.MAX_VALUE, entry.getValue(), false, false));
        }

        for (Map.Entry<Attribute, AttributeModifier> entry : attributes.entries()) {
            entity.getAttribute(entry.getKey()).addTransientModifier(entry.getValue());
        }
    }

    public void removeEffectsAndAttributes(LivingEntity entity) {
        for (MobEffect effect : effects.keySet()) {
            entity.removeEffect(effect);
        }

        for (Map.Entry<Attribute, AttributeModifier> entry : attributes.entries()) {
            Objects.requireNonNull(entity.getAttribute(entry.getKey())).removeModifier(entry.getValue());
        }
    }
}