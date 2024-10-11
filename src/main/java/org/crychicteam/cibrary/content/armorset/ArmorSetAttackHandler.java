package org.crychicteam.cibrary.content.armorset;

import dev.xkmc.l2damagetracker.contents.attack.AttackCache;
import dev.xkmc.l2damagetracker.contents.attack.CreateSourceEvent;
import dev.xkmc.l2damagetracker.contents.attack.PlayerAttackCache;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.CriticalHitEvent;

import java.util.function.BiConsumer;

public class ArmorSetAttackHandler {

    public void onCreateSource(CreateSourceEvent event) {}

    public void onPlayerAttack(PlayerAttackCache cache) {}

    public boolean attackerOnCriticalHit(PlayerAttackCache cache, CriticalHitEvent event) {
        return false;
    }

    public boolean targetOnCriticalHit(PlayerAttackCache cache, CriticalHitEvent event) {
        return false;
    }

    public void attackerSetupProfile(AttackCache cache, BiConsumer<LivingEntity, ItemStack> setupProfile) {}

    public void targetSetupProfile(AttackCache cache, BiConsumer<LivingEntity, ItemStack> setupProfile) {}

    public void attackerOnAttack(AttackCache cache, ItemStack weapon) {}

    public void targetOnAttack(AttackCache cache, ItemStack weapon) {}

    public void attackerPostAttack(AttackCache cache, LivingAttackEvent event, ItemStack weapon) {}

    public void targetPostAttack(AttackCache cache, LivingAttackEvent event, ItemStack weapon) {}

    public void attackerOnHurt(AttackCache cache, ItemStack weapon) {}

    public void targetOnHurt(AttackCache cache, ItemStack weapon) {}

    public void attackerOnHurtMaximized(AttackCache cache, ItemStack weapon) {}

    public void targetOnHurtMaximized(AttackCache cache, ItemStack weapon) {}

    public void attackerPostHurt(AttackCache cache, LivingHurtEvent event, ItemStack weapon) {}

    public void targetPostHurt(AttackCache cache, LivingHurtEvent event, ItemStack weapon) {}

    public void attackerOnDamage(AttackCache cache, ItemStack weapon) {}

    public void targetOnDamage(AttackCache cache, ItemStack weapon) {}

    public void attackerOnDamageFinalized(AttackCache cache, ItemStack weapon) {}

    public void targetOnDamageFinalized(AttackCache cache, ItemStack weapon) {}

}
