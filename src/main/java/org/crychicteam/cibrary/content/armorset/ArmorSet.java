package org.crychicteam.cibrary.content.armorset;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import dev.xkmc.l2damagetracker.contents.attack.AttackCache;
import dev.xkmc.l2damagetracker.contents.attack.CreateSourceEvent;
import dev.xkmc.l2damagetracker.contents.attack.PlayerAttackCache;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
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
    private final Map<Item, Integer> curioItems;
    private static final Item EMPTY_SLOT_MARKER = null;

    public ArmorSet(String identifier, SetEffect effect) {
        this.identifier = identifier;
        this.effect = effect;
        this.effects = new HashMap<>();
        this.attributes = HashMultimap.create();
        this.equipmentItems = new EnumMap<>(EquipmentSlot.class);
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            this.equipmentItems.put(slot, new HashSet<>());
        }
        this.curioItems = new HashMap<>();
    }

    public ArmorSet(String identifier) {
        this(identifier, new DefaultSetEffect());
    }

    public void addEquipmentItem(EquipmentSlot slot, Item item) {
        if (item == Items.AIR) {
            equipmentItems.get(slot).clear();
            equipmentItems.get(slot).add(EMPTY_SLOT_MARKER);
        } else {
            equipmentItems.get(slot).add(item);
        }
    }

    public void addCurioItem(Item item, int count) {
        curioItems.put(item, count);
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
            if (entry.getValue().contains(EMPTY_SLOT_MARKER)) {
                if (!equippedItem.isEmpty()) {
                    return false;
                }
            } else if (!entry.getValue().isEmpty() && (equippedItem.isEmpty() || !entry.getValue().contains(equippedItem.getItem()))) {
                return false;
            }
        }
        return CuriosIntegration.matchesCurioRequirements(entity, curioItems);
    }

    public Map<EquipmentSlot, ItemStack> getEquippedItems(ServerPlayer entity) {
        Map<EquipmentSlot, ItemStack> equippedItems = new EnumMap<>(EquipmentSlot.class);
        for (Map.Entry<EquipmentSlot, Set<Item>> entry : equipmentItems.entrySet()) {
            ItemStack equippedItem = entity.getItemBySlot(entry.getKey());
            if (entry.getValue().contains(equippedItem.getItem())) {
                equippedItems.put(entry.getKey(), equippedItem);
            }
        }
        return equippedItems;
    }

    public List<ItemStack> getEquippedCurioItems(ServerPlayer entity) {
        List<ItemStack> equippedCurios = new ArrayList<>();
        if (CuriosIntegration.isCuriosLoaded) {
            List<ItemStack> allCurios = CuriosIntegration.getAllItems(entity);
            for (ItemStack curioStack : allCurios) {
                if (curioItems.containsKey(curioStack.getItem())) {
                    equippedCurios.add(curioStack);
                }
            }
        }
        return equippedCurios;
    }

    public Map<EquipmentSlot, ItemStack> getAllEquippedItems(ServerPlayer entity) {
        Map<EquipmentSlot, ItemStack> allEquipped = getEquippedItems(entity);
        List<ItemStack> curios = getEquippedCurioItems(entity);
        if (!curios.isEmpty()) {
            allEquipped.put(EquipmentSlot.OFFHAND, curios.get(0));
        }
        return allEquipped;
    }

    public void applyMobEffects(LivingEntity entity) {
        for (Map.Entry<MobEffect, Integer> entry : effects.entrySet()) {
            entity.addEffect(new MobEffectInstance(entry.getKey(), 600, entry.getValue(), false, false));
        }
    }

    public void applyAttributes(LivingEntity entity) {
        for (Map.Entry<Attribute, AttributeModifier> entry : attributes.entries()) {
            Objects.requireNonNull(entity.getAttribute(entry.getKey())).addTransientModifier(entry.getValue());
        }
    }

    public void applyEffectsAndAttributes(LivingEntity entity) {
        applyMobEffects(entity);
        applyAttributes(entity);
    }

    public void removeEffects(LivingEntity entity) {
        for (MobEffect effect : effects.keySet()) {
            entity.removeEffect(effect);
        }
    }

    public void removeAttributes(LivingEntity entity) {
        for (Map.Entry<Attribute, AttributeModifier> entry : attributes.entries()) {
            Objects.requireNonNull(entity.getAttribute(entry.getKey())).removeModifier(entry.getValue());
        }
    }

    public void removeEffectsAndAttributes(LivingEntity entity) {
        removeEffects(entity);
        removeAttributes(entity);
    }

    public Map<Item, Integer> getCurioItems() {
        return Collections.unmodifiableMap(curioItems);
    }

    public void onCreateSource(CreateSourceEvent event) {}

    public void onPlayerAttack(PlayerAttackCache cache) {}

    public boolean onCriticalHit(PlayerAttackCache cache, CriticalHitEvent event) {
        return false;
    }

    public void setupProfile(AttackCache cache, BiConsumer<LivingEntity, ItemStack> setupProfile) {}

    public void onAttack(AttackCache cache, ItemStack weapon) {}

    public void postAttack(AttackCache cache, LivingAttackEvent event, ItemStack weapon) {}

    public void onHurt(AttackCache cache, ItemStack weapon) {}

    public void onHurtMaximized(AttackCache cache, ItemStack weapon) {}

    public void postHurt(AttackCache cache, LivingHurtEvent event, ItemStack weapon) {}

    public void onDamage(AttackCache cache, ItemStack weapon) {}

    public void onDamageFinalized(AttackCache cache, ItemStack weapon) {}
}