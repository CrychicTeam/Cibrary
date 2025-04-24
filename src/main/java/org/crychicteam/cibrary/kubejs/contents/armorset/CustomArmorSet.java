package org.crychicteam.cibrary.kubejs.contents.armorset;

import com.google.common.collect.Multimap;
import dev.latvian.mods.kubejs.registry.BuilderBase;
import dev.latvian.mods.kubejs.registry.RegistryInfo;
import dev.latvian.mods.kubejs.typings.Info;
import dev.xkmc.l2damagetracker.contents.attack.AttackCache;
import dev.xkmc.l2damagetracker.contents.attack.CreateSourceEvent;
import dev.xkmc.l2damagetracker.contents.attack.PlayerAttackCache;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.CriticalHitEvent;
import org.crychicteam.cibrary.content.armorset.ArmorSet;
import org.crychicteam.cibrary.content.armorset.ISetEffect;
import org.crychicteam.cibrary.content.armorset.defaults.DefaultSetEffect;
import org.crychicteam.cibrary.kubejs.CiJSPlugin;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Custom Armor Set implementation for KubeJS integration.
 */
public class CustomArmorSet extends ArmorSet {
    private final Builder builder;
    private final ISetEffect effect; // Single effect p
    private final int skillCooldown;

    public CustomArmorSet(Builder builder) {
        this.builder = builder;
        this.effect = builder.effect;
        this.skillCooldown = builder.skillCooldown;
        if (builder.equipmentItems != null) {
            for (Map.Entry<EquipmentSlot, Set<Item>> entry : builder.equipmentItems.entrySet()) {
                for (Item item : entry.getValue()) {
                    if (item != ArmorSet.EMPTY_SLOT_MARKER) {
                        this.addEquipmentItem(entry.getKey(), item);
                    }
                }
            }
        }
        if (builder.curioItems != null) {
            for (Map.Entry<Item, Integer> entry : builder.curioItems.entrySet()) {
                this.addCurioItem(entry.getKey(), entry.getValue());
            }
        }
        if (builder.effects != null) {
            for (Map.Entry<MobEffect, Integer> entry : builder.effects.entrySet()) {
                this.addEffect(entry.getKey(), entry.getValue());
            }
        }
        if (builder.attributes != null) {
            for (Map.Entry<Attribute, AttributeModifier> entry : builder.attributes.entries()) {
                AttributeModifier modifier = entry.getValue();
                this.addAttribute(entry.getKey(), modifier.getName(), modifier.getAmount(), modifier.getOperation());
            }
        }
    }

    @Override
    public int getSkillCooldown() {
        return skillCooldown;
    }

    @Override
    public ISetEffect checkEffect(Player player) {
        if (builder.checkEffectCallback != null) {
            return builder.checkEffectCallback.apply(player);
        }
        return super.checkEffect(player);
    }

    @Override
    public Component getDescription(Player player, ItemStack itemStack) {
        if (builder.getDescriptionCallback != null) {
            return builder.getDescriptionCallback.apply(player, itemStack);
        }
        return super.getDescription(player, itemStack);
    }

    @Override
    public boolean checkDescription(Player player, ItemStack itemStack) {
        if (builder.checkDescriptionCallback != null) {
            return builder.checkDescriptionCallback.apply(player, itemStack);
        }
        return super.checkDescription(player, itemStack);
    }

    @Override
    public void onCreateSource(CreateSourceEvent event) {
        if (builder.onCreateSourceCallback != null) {
            builder.onCreateSourceCallback.apply(event);
        }
    }

    @Override
    public void onPlayerAttack(PlayerAttackCache cache) {
        if (builder.onPlayerAttackCallback != null) {
            builder.onPlayerAttackCallback.apply(cache);
        }
    }

    @Override
    public boolean attackerOnCriticalHit(PlayerAttackCache cache, CriticalHitEvent event) {
        if (builder.attackerOnCriticalHitCallback != null) {
            return builder.attackerOnCriticalHitCallback.apply(cache, event);
        }
        return false;
    }

    @Override
    public boolean targetOnCriticalHit(PlayerAttackCache cache, CriticalHitEvent event) {
        if (builder.targetOnCriticalHitCallback != null) {
            return builder.targetOnCriticalHitCallback.apply(cache, event);
        }
        return false;
    }

    @Override
    public void attackerSetupProfile(AttackCache cache, BiConsumer<LivingEntity, ItemStack> setupProfile) {
        if (builder.attackerSetupProfileCallback != null) {
            builder.attackerSetupProfileCallback.apply(cache, setupProfile);
        }
    }

    @Override
    public void targetSetupProfile(AttackCache cache, BiConsumer<LivingEntity, ItemStack> setupProfile) {
        if (builder.targetSetupProfileCallback != null) {
            builder.targetSetupProfileCallback.apply(cache, setupProfile);
        }
    }

    @Override
    public void attackerOnAttack(AttackCache cache, ItemStack weapon) {
        if (builder.attackerOnAttackCallback != null) {
            builder.attackerOnAttackCallback.apply(cache, weapon);
        }
    }

    @Override
    public void targetOnAttack(AttackCache cache, ItemStack weapon) {
        if (builder.targetOnAttackCallback != null) {
            builder.targetOnAttackCallback.apply(cache, weapon);
        }
    }

    @Override
    public void attackerPostAttack(AttackCache cache, LivingAttackEvent event, ItemStack weapon) {
        if (builder.attackerPostAttackCallback != null) {
            builder.attackerPostAttackCallback.apply(cache, event, weapon);
        }
    }

    @Override
    public void targetPostAttack(AttackCache cache, LivingAttackEvent event, ItemStack weapon) {
        if (builder.targetPostAttackCallback != null) {
            builder.targetPostAttackCallback.apply(cache, event, weapon);
        }
    }

    @Override
    public void attackerOnHurt(AttackCache cache, ItemStack weapon) {
        if (builder.attackerOnHurtCallback != null) {
            builder.attackerOnHurtCallback.apply(cache, weapon);
        }
    }

    @Override
    public void targetOnHurt(AttackCache cache, ItemStack weapon) {
        if (builder.targetOnHurtCallback != null) {
            builder.targetOnHurtCallback.apply(cache, weapon);
        }
    }

    @Override
    public void attackerOnHurtMaximized(AttackCache cache, ItemStack weapon) {
        if (builder.attackerOnHurtMaximizedCallback != null) {
            builder.attackerOnHurtMaximizedCallback.apply(cache, weapon);
        }
    }

    @Override
    public void targetOnHurtMaximized(AttackCache cache, ItemStack weapon) {
        if (builder.targetOnHurtMaximizedCallback != null) {
            builder.targetOnHurtMaximizedCallback.apply(cache, weapon);
        }
    }

    @Override
    public void attackerPostHurt(AttackCache cache, LivingHurtEvent event, ItemStack weapon) {
        if (builder.attackerPostHurtCallback != null) {
            builder.attackerPostHurtCallback.apply(cache, event, weapon);
        }
    }

    @Override
    public void targetPostHurt(AttackCache cache, LivingHurtEvent event, ItemStack weapon) {
        if (builder.targetPostHurtCallback != null) {
            builder.targetPostHurtCallback.apply(cache, event, weapon);
        }
    }

    @Override
    public void attackerOnDamage(AttackCache cache, ItemStack weapon) {
        if (builder.attackerOnDamageCallback != null) {
            builder.attackerOnDamageCallback.apply(cache, weapon);
        }
    }

    @Override
    public void targetOnDamage(AttackCache cache, ItemStack weapon) {
        if (builder.targetOnDamageCallback != null) {
            builder.targetOnDamageCallback.apply(cache, weapon);
        }
    }

    @Override
    public void attackerOnDamageFinalized(AttackCache cache, ItemStack weapon) {
        if (builder.attackerOnDamageFinalizedCallback != null) {
            builder.attackerOnDamageFinalizedCallback.apply(cache, weapon);
        }
    }

    @Override
    public void targetOnDamageFinalized(AttackCache cache, ItemStack weapon) {
        if (builder.targetOnDamageFinalizedCallback != null) {
            builder.targetOnDamageFinalizedCallback.apply(cache, weapon);
        }
    }

    public interface CheckEffectCallback {
        ISetEffect apply(Player player);
    }

    public interface GetDescriptionCallback {
        Component apply(Player player, ItemStack stack);
    }

    public interface CheckDescriptionCallback {
        boolean apply(Player player, ItemStack stack);
    }

    @Info("Callback interface for handling attack source creation")
    public interface OnCreateSourceCallback {
        void apply(CreateSourceEvent event);
    }

    @Info("Callback interface for handling player attack events")
    public interface OnPlayerAttackCallback {
        void apply(PlayerAttackCache cache);
    }

    @Info("Callback interface for handling attacker critical hit events")
    public interface AttackerOnCriticalHitCallback {
        boolean apply(PlayerAttackCache cache, CriticalHitEvent event);
    }

    @Info("Callback interface for handling target critical hit events")
    public interface TargetOnCriticalHitCallback {
        boolean apply(PlayerAttackCache cache, CriticalHitEvent event);
    }

    @Info("Callback interface for handling attacker profile setup")
    public interface AttackerSetupProfileCallback {
        void apply(AttackCache cache, BiConsumer<LivingEntity, ItemStack> setupProfile);
    }

    @Info("Callback interface for handling target profile setup")
    public interface TargetSetupProfileCallback {
        void apply(AttackCache cache, BiConsumer<LivingEntity, ItemStack> setupProfile);
    }

    @Info("Callback interface for handling attacker on attack events")
    public interface AttackerOnAttackCallback {
        void apply(AttackCache cache, ItemStack weapon);
    }

    @Info("Callback interface for handling target on attack events")
    public interface TargetOnAttackCallback {
        void apply(AttackCache cache, ItemStack weapon);
    }

    @Info("Callback interface for handling attacker post attack events")
    public interface AttackerPostAttackCallback {
        void apply(AttackCache cache, LivingAttackEvent event, ItemStack weapon);
    }

    @Info("Callback interface for handling target post attack events")
    public interface TargetPostAttackCallback {
        void apply(AttackCache cache, LivingAttackEvent event, ItemStack weapon);
    }

    @Info("Callback interface for handling attacker on hurt events")
    public interface AttackerOnHurtCallback {
        void apply(AttackCache cache, ItemStack weapon);
    }

    @Info("Callback interface for handling target on hurt events")
    public interface TargetOnHurtCallback {
        void apply(AttackCache cache, ItemStack weapon);
    }

    @Info("Callback interface for handling attacker on hurt maximized events")
    public interface AttackerOnHurtMaximizedCallback {
        void apply(AttackCache cache, ItemStack weapon);
    }

    @Info("Callback interface for handling target on hurt maximized events")
    public interface TargetOnHurtMaximizedCallback {
        void apply(AttackCache cache, ItemStack weapon);
    }

    @Info("Callback interface for handling attacker post hurt events")
    public interface AttackerPostHurtCallback {
        void apply(AttackCache cache, LivingHurtEvent event, ItemStack weapon);
    }

    @Info("Callback interface for handling target post hurt events")
    public interface TargetPostHurtCallback {
        void apply(AttackCache cache, LivingHurtEvent event, ItemStack weapon);
    }

    @Info("Callback interface for handling attacker on damage events")
    public interface AttackerOnDamageCallback {
        void apply(AttackCache cache, ItemStack weapon);
    }

    @Info("Callback interface for handling target on damage events")
    public interface TargetOnDamageCallback {
        void apply(AttackCache cache, ItemStack weapon);
    }

    @Info("Callback interface for handling attacker on damage finalized events")
    public interface AttackerOnDamageFinalizedCallback {
        void apply(AttackCache cache, ItemStack weapon);
    }

    @Info("Callback interface for handling target on damage finalized events")
    public interface TargetOnDamageFinalizedCallback {
        void apply(AttackCache cache, ItemStack weapon);
    }

    public static class Builder extends BuilderBase<CustomArmorSet> {
        private Map<MobEffect, Integer> effects;
        private Multimap<Attribute, AttributeModifier> attributes;
        private Map<EquipmentSlot, Set<Item>> equipmentItems;
        private Map<Item, Integer> curioItems;

        private int skillCooldown = 0;
        private ISetEffect effect = new DefaultSetEffect();

        private CheckEffectCallback checkEffectCallback = null;
        private GetDescriptionCallback getDescriptionCallback = null;
        private CheckDescriptionCallback checkDescriptionCallback = null;

        private OnCreateSourceCallback onCreateSourceCallback;
        private OnPlayerAttackCallback onPlayerAttackCallback;
        private AttackerOnCriticalHitCallback attackerOnCriticalHitCallback;
        private TargetOnCriticalHitCallback targetOnCriticalHitCallback;
        private AttackerSetupProfileCallback attackerSetupProfileCallback;
        private TargetSetupProfileCallback targetSetupProfileCallback;
        private AttackerOnAttackCallback attackerOnAttackCallback;
        private TargetOnAttackCallback targetOnAttackCallback;
        private AttackerPostAttackCallback attackerPostAttackCallback;
        private TargetPostAttackCallback targetPostAttackCallback;
        private AttackerOnHurtCallback attackerOnHurtCallback;
        private TargetOnHurtCallback targetOnHurtCallback;
        private AttackerOnHurtMaximizedCallback attackerOnHurtMaximizedCallback;
        private TargetOnHurtMaximizedCallback targetOnHurtMaximizedCallback;
        private AttackerPostHurtCallback attackerPostHurtCallback;
        private TargetPostHurtCallback targetPostHurtCallback;
        private AttackerOnDamageCallback attackerOnDamageCallback;
        private TargetOnDamageCallback targetOnDamageCallback;
        private AttackerOnDamageFinalizedCallback attackerOnDamageFinalizedCallback;
        private TargetOnDamageFinalizedCallback targetOnDamageFinalizedCallback;

        public Builder(ResourceLocation id) {
            super(id);
        }

        public Builder addEquipment(EquipmentSlot slot, Item item) {
            if (item == Items.AIR) {
                equipmentItems.get(slot).clear();
                equipmentItems.get(slot).add(EMPTY_SLOT_MARKER);
            } else {
                equipmentItems.get(slot).add(item);
            }
            return this;
        }

        public Builder addCurio(Item item, int count) {
            curioItems.put(item, count);
            return this;
        }

        public Builder addEffect(MobEffect effect, int amplifier) {
            effects.put(effect, amplifier);
            return this;
        }

        public Builder addAttribute(Attribute attribute, String name, double amount, AttributeModifier.Operation operation) {
            AttributeModifier modifier = new AttributeModifier(UUID.randomUUID(), name, amount, operation);
            attributes.put(attribute, modifier);
            return this;
        }

        public Builder checkEffetct(CheckEffectCallback callback) {
            this.checkEffectCallback = callback;
            return this;
        }

        public Builder getDescription(GetDescriptionCallback callback) {
            this.getDescriptionCallback = callback;
            return this;
        }

        public Builder checkDescription(CheckDescriptionCallback callback) {
            this.checkDescriptionCallback = callback;
            return this;
        }

        @Info("Sets the cooldown period for the armor set's skill in ticks")
        public Builder skillCooldown(int cooldown) {
            this.skillCooldown = cooldown;
            return this;
        }

        @Info("Sets the effect for this armor set (only one effect per armor set is allowed)")
        public Builder setEffect(ISetEffect effect) {
            this.effect = effect;
            return this;
        }

        @Info("Creates and sets a CustomSetEffect using the provided consumer")
        public Builder setCustomEffect(String effectId, Consumer<CustomSetEffect.Builder> consumer) {
            CustomSetEffect.Builder effectBuilder = new CustomSetEffect.Builder(effectId);
            consumer.accept(effectBuilder);
            this.effect = effectBuilder.build();
            return this;
        }

        @Info("Sets a callback that triggers when an attack source is created")
        public Builder onCreateSource(OnCreateSourceCallback callback) {
            this.onCreateSourceCallback = callback;
            return this;
        }

        @Info("Sets a callback that triggers when a player attacks")
        public Builder onPlayerAttack(OnPlayerAttackCallback callback) {
            this.onPlayerAttackCallback = callback;
            return this;
        }

        @Info("Sets a callback that triggers when an attacker lands a critical hit")
        public Builder attackerOnCriticalHit(AttackerOnCriticalHitCallback callback) {
            this.attackerOnCriticalHitCallback = callback;
            return this;
        }

        @Info("Sets a callback that triggers when a target receives a critical hit")
        public Builder targetOnCriticalHit(TargetOnCriticalHitCallback callback) {
            this.targetOnCriticalHitCallback = callback;
            return this;
        }

        @Info("Sets a callback that sets up the attacker's attack profile")
        public Builder attackerSetupProfile(AttackerSetupProfileCallback callback) {
            this.attackerSetupProfileCallback = callback;
            return this;
        }

        @Info("Sets a callback that sets up the target's attack profile")
        public Builder targetSetupProfile(TargetSetupProfileCallback callback) {
            this.targetSetupProfileCallback = callback;
            return this;
        }

        @Info("Sets a callback that triggers when an attacker attacks")
        public Builder attackerOnAttack(AttackerOnAttackCallback callback) {
            this.attackerOnAttackCallback = callback;
            return this;
        }

        @Info("Sets a callback that triggers when a target is attacked")
        public Builder targetOnAttack(TargetOnAttackCallback callback) {
            this.targetOnAttackCallback = callback;
            return this;
        }

        @Info("Sets a callback that triggers after an attacker attacks")
        public Builder attackerPostAttack(AttackerPostAttackCallback callback) {
            this.attackerPostAttackCallback = callback;
            return this;
        }

        @Info("Sets a callback that triggers after a target is attacked")
        public Builder targetPostAttack(TargetPostAttackCallback callback) {
            this.targetPostAttackCallback = callback;
            return this;
        }

        @Info("Sets a callback that triggers when an attacker hurts a target")
        public Builder attackerOnHurt(AttackerOnHurtCallback callback) {
            this.attackerOnHurtCallback = callback;
            return this;
        }

        @Info("Sets a callback that triggers when a target is hurt")
        public Builder targetOnHurt(TargetOnHurtCallback callback) {
            this.targetOnHurtCallback = callback;
            return this;
        }

        @Info("Sets a callback that triggers when attacker damage is maximized")
        public Builder attackerOnHurtMaximized(AttackerOnHurtMaximizedCallback callback) {
            this.attackerOnHurtMaximizedCallback = callback;
            return this;
        }

        @Info("Sets a callback that triggers when target damage is maximized")
        public Builder targetOnHurtMaximized(TargetOnHurtMaximizedCallback callback) {
            this.targetOnHurtMaximizedCallback = callback;
            return this;
        }

        @Info("Sets a callback that triggers after an attacker hurts a target")
        public Builder attackerPostHurt(AttackerPostHurtCallback callback) {
            this.attackerPostHurtCallback = callback;
            return this;
        }

        @Info("Sets a callback that triggers after a target is hurt")
        public Builder targetPostHurt(TargetPostHurtCallback callback) {
            this.targetPostHurtCallback = callback;
            return this;
        }

        @Info("Sets a callback that triggers when an attacker calculates damage")
        public Builder attackerOnDamage(AttackerOnDamageCallback callback) {
            this.attackerOnDamageCallback = callback;
            return this;
        }

        @Info("Sets a callback that triggers when a target calculates damage")
        public Builder targetOnDamage(TargetOnDamageCallback callback) {
            this.targetOnDamageCallback = callback;
            return this;
        }

        @Info("Sets a callback that triggers when attacker damage is finalized")
        public Builder attackerOnDamageFinalized(AttackerOnDamageFinalizedCallback callback) {
            this.attackerOnDamageFinalizedCallback = callback;
            return this;
        }

        @Info("Sets a callback that triggers when target damage is finalized")
        public Builder targetOnDamageFinalized(TargetOnDamageFinalizedCallback callback) {
            this.targetOnDamageFinalizedCallback = callback;
            return this;
        }

        @Override
        public RegistryInfo getRegistryType() {
            return CiJSPlugin.ARMOR_SET;
        }

        @Override
        public CustomArmorSet createObject() {
            return new CustomArmorSet(this);
        }
    }
}