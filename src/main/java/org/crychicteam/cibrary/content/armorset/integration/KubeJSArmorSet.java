package org.crychicteam.cibrary.content.armorset.integration;

import dev.xkmc.l2damagetracker.contents.attack.AttackCache;
import dev.xkmc.l2damagetracker.contents.attack.CreateSourceEvent;
import dev.xkmc.l2damagetracker.contents.attack.PlayerAttackCache;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.CriticalHitEvent;
import org.crychicteam.cibrary.content.armorset.ArmorSet;
import org.crychicteam.cibrary.content.armorset.SetEffect;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class KubeJSArmorSet extends ArmorSet {

    private Consumer<CreateSourceEvent> createSourceEventConsumer = event -> {};
    private Consumer<PlayerAttackCache> playerAttackCacheConsumer = cache -> {};
    private BiConsumer<PlayerAttackCache, CriticalHitEvent> criticalHitConsumer = (cache, event) -> {};
    private BiConsumer<AttackCache, BiConsumer<LivingEntity, ItemStack>> setupProfileConsumer = (cache, setupProfile) -> {};
    private BiConsumer<AttackCache, ItemStack> attackConsumer = (cache, weapon) -> {};
    private BiConsumer<AttackCache, LivingAttackEvent> postAttackConsumer = (cache, event) -> {};
    private BiConsumer<AttackCache, ItemStack> hurtConsumer = (cache, weapon) -> {};
    private BiConsumer<AttackCache, ItemStack> hurtMaximizedConsumer = (cache, weapon) -> {};
    private BiConsumer<AttackCache, LivingHurtEvent> postHurtConsumer = (cache, event) -> {};
    private BiConsumer<AttackCache, ItemStack> damageConsumer = (cache, weapon) -> {};
    private BiConsumer<AttackCache, ItemStack> damageFinalizedConsumer = (cache, weapon) -> {};

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public void setEffect(SetEffect effect) {
        this.effect = effect;
    }

    public void setCreateSourceEventConsumer(Consumer<CreateSourceEvent> consumer) {
        this.createSourceEventConsumer = consumer;
    }

    public void setPlayerAttackCacheConsumer(Consumer<PlayerAttackCache> consumer) {
        this.playerAttackCacheConsumer = consumer;
    }

    public void setCriticalHitConsumer(BiConsumer<PlayerAttackCache, CriticalHitEvent> consumer) {
        this.criticalHitConsumer = consumer;
    }

    public void setSetupProfileConsumer(BiConsumer<AttackCache, BiConsumer<LivingEntity, ItemStack>> consumer) {
        this.setupProfileConsumer = consumer;
    }

    public void setAttackConsumer(BiConsumer<AttackCache, ItemStack> consumer) {
        this.attackConsumer = consumer;
    }

    public void setPostAttackConsumer(BiConsumer<AttackCache, LivingAttackEvent> consumer) {
        this.postAttackConsumer = consumer;
    }

    public void setHurtConsumer(BiConsumer<AttackCache, ItemStack> consumer) {
        this.hurtConsumer = consumer;
    }

    public void setHurtMaximizedConsumer(BiConsumer<AttackCache, ItemStack> consumer) {
        this.hurtMaximizedConsumer = consumer;
    }

    public void setPostHurtConsumer(BiConsumer<AttackCache, LivingHurtEvent> consumer) {
        this.postHurtConsumer = consumer;
    }

    public void setDamageConsumer(BiConsumer<AttackCache, ItemStack> consumer) {
        this.damageConsumer = consumer;
    }

    public void setDamageFinalizedConsumer(BiConsumer<AttackCache, ItemStack> consumer) {
        this.damageFinalizedConsumer = consumer;
    }

    @Override
    public void onCreateSource(CreateSourceEvent event) {
        createSourceEventConsumer.accept(event);
    }

    @Override
    public void onPlayerAttack(PlayerAttackCache cache) {
        playerAttackCacheConsumer.accept(cache);
    }

    @Override
    public boolean onCriticalHit(PlayerAttackCache cache, CriticalHitEvent event) {
        criticalHitConsumer.accept(cache, event);
        return false;
    }

    @Override
    public void setupProfile(AttackCache cache, BiConsumer<LivingEntity, ItemStack> setupProfile) {
        setupProfileConsumer.accept(cache, setupProfile);
    }

    @Override
    public void onAttack(AttackCache cache, ItemStack weapon) {
        attackConsumer.accept(cache, weapon);
    }

    @Override
    public void postAttack(AttackCache cache, LivingAttackEvent event, ItemStack weapon) {
        postAttackConsumer.accept(cache, event);
    }

    @Override
    public void onHurt(AttackCache cache, ItemStack weapon) {
        hurtConsumer.accept(cache, weapon);
    }

    @Override
    public void onHurtMaximized(AttackCache cache, ItemStack weapon) {
        hurtMaximizedConsumer.accept(cache, weapon);
    }

    @Override
    public void postHurt(AttackCache cache, LivingHurtEvent event, ItemStack weapon) {
        postHurtConsumer.accept(cache, event);
    }

    @Override
    public void onDamage(AttackCache cache, ItemStack weapon) {
        damageConsumer.accept(cache, weapon);
    }

    @Override
    public void onDamageFinalized(AttackCache cache, ItemStack weapon) {
        damageFinalizedConsumer.accept(cache, weapon);
    }
}
