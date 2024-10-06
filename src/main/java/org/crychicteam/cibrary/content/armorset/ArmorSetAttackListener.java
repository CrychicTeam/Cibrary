package org.crychicteam.cibrary.content.armorset;

import dev.xkmc.l2damagetracker.contents.attack.AttackCache;
import dev.xkmc.l2damagetracker.contents.attack.AttackListener;
import dev.xkmc.l2damagetracker.contents.attack.CreateSourceEvent;
import dev.xkmc.l2damagetracker.contents.attack.PlayerAttackCache;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.CriticalHitEvent;
import org.crychicteam.cibrary.Cibrary;

import java.util.function.BiConsumer;

public class ArmorSetAttackListener implements AttackListener {

    private final ArmorSetManager armorSetManager;

    public ArmorSetAttackListener(ArmorSetManager armorSetManager) {
        this.armorSetManager = armorSetManager;
    }

    @Override
    public void onCreateSource(CreateSourceEvent event) {
        if (event.getAttacker() instanceof Player) {
            ArmorSet activeSet = armorSetManager.getActiveArmorSet((Player) event.getAttacker());
            if (activeSet != null) {
                activeSet.onCreateSource(event);
            }
        }
    }

    @Override
    public void onPlayerAttack(PlayerAttackCache cache) {
        cache.getAttacker();
        if (!(cache.getAttacker() instanceof Player player)) {
            return;
        }
        ArmorSet activeSet = armorSetManager.getActiveArmorSet(player);
        if (activeSet != null) {
            activeSet.onPlayerAttack(cache);
        }
        Cibrary.LOGGER.info("Player attack");
    }

    @Override
    public boolean onCriticalHit(PlayerAttackCache cache, CriticalHitEvent event) {
        Player player = (Player) cache.getAttacker();
        ArmorSet activeSet = armorSetManager.getActiveArmorSet(player);
        return activeSet != null && activeSet.onCriticalHit(cache, event);
    }

    @Override
    public void setupProfile(AttackCache cache, BiConsumer<LivingEntity, ItemStack> setupProfile) {
        LivingEntity attacker = cache.getAttacker();
        LivingEntity target = cache.getAttackTarget();
        if (attacker instanceof Player) {
            ArmorSet activeSet = armorSetManager.getActiveArmorSet((Player) attacker);
            if (activeSet != null) {
                activeSet.setupProfile(cache, setupProfile);
            }
        } else if (target instanceof Player) {
            ArmorSet activeSet = armorSetManager.getActiveArmorSet((Player) target);
            if (activeSet != null) {
                activeSet.setupProfile(cache, setupProfile);
            }
        }
    }

    @Override
    public void onAttack(AttackCache cache, ItemStack weapon) {
        LivingEntity attacker = cache.getAttacker();
        LivingEntity target = cache.getAttackTarget();
        if (attacker instanceof Player) {
            ArmorSet activeSet = armorSetManager.getActiveArmorSet((Player) attacker);
            if (activeSet != null) {
                activeSet.onAttack(cache, weapon);
            }
        } else if (target instanceof Player) {
            ArmorSet activeSet = armorSetManager.getActiveArmorSet((Player) target);
            if (activeSet != null) {
                activeSet.onAttack(cache, weapon);
            }
        }
    }

    @Override
    public void postAttack(AttackCache cache, LivingAttackEvent event, ItemStack weapon) {
        LivingEntity attacker = cache.getAttacker();
        LivingEntity target = cache.getAttackTarget();
        if (attacker instanceof Player) {
            ArmorSet activeSet = armorSetManager.getActiveArmorSet((Player) attacker);
            if (activeSet != null) {
                activeSet.postAttack(cache, event, weapon);
            }
        } else if (target instanceof Player) {
            ArmorSet activeSet = armorSetManager.getActiveArmorSet((Player) target);
            if (activeSet != null) {
                activeSet.postAttack(cache, event, weapon);
            }
        }
    }

    @Override
    public void onHurt(AttackCache cache, ItemStack weapon) {
        LivingEntity attacker = cache.getAttacker();
        LivingEntity target = cache.getAttackTarget();
        if (attacker instanceof Player) {
            ArmorSet activeSet = armorSetManager.getActiveArmorSet((Player) attacker);
            if (activeSet != null) {
                activeSet.onHurt(cache, weapon);
            }
        } else if (target instanceof Player) {
            ArmorSet activeSet = armorSetManager.getActiveArmorSet((Player) target);
            if (activeSet != null) {
                activeSet.onHurt(cache, weapon);
            }
        }
    }

    @Override
    public void onHurtMaximized(AttackCache cache, ItemStack weapon) {
        LivingEntity attacker = cache.getAttacker();
        LivingEntity target = cache.getAttackTarget();
        if (attacker instanceof Player) {
            ArmorSet activeSet = armorSetManager.getActiveArmorSet((Player) attacker);
            if (activeSet != null) {
                activeSet.onHurtMaximized(cache, weapon);
            }
        } else if (target instanceof Player) {
            ArmorSet activeSet = armorSetManager.getActiveArmorSet((Player) target);
            if (activeSet != null) {
                activeSet.onHurtMaximized(cache, weapon);
            }
        }
    }

    @Override
    public void postHurt(AttackCache cache, LivingHurtEvent event, ItemStack weapon) {
        LivingEntity attacker = cache.getAttacker();
        LivingEntity target = cache.getAttackTarget();
        if (attacker instanceof Player) {
            ArmorSet activeSet = armorSetManager.getActiveArmorSet((Player) attacker);
            if (activeSet != null) {
                activeSet.postHurt(cache, event, weapon);
            }
        } else if (target instanceof Player) {
            ArmorSet activeSet = armorSetManager.getActiveArmorSet((Player) target);
            if (activeSet != null) {
                activeSet.postHurt(cache, event, weapon);
            }
        }
    }

    @Override
    public void onDamage(AttackCache cache, ItemStack weapon) {
        LivingEntity attacker = cache.getAttacker();
        LivingEntity target = cache.getAttackTarget();
        if (attacker instanceof Player) {
            ArmorSet activeSet = armorSetManager.getActiveArmorSet((Player) attacker);
            if (activeSet != null) {
                activeSet.onDamage(cache, weapon);
            }
        } else if (target instanceof Player) {
            ArmorSet activeSet = armorSetManager.getActiveArmorSet((Player) target);
            if (activeSet != null) {
                activeSet.onDamage(cache, weapon);
            }
        }
    }

    @Override
    public void onDamageFinalized(AttackCache cache, ItemStack weapon) {
        LivingEntity attacker = cache.getAttacker();
        LivingEntity target = cache.getAttackTarget();
        if (attacker instanceof Player) {
            ArmorSet activeSet = armorSetManager.getActiveArmorSet((Player) attacker);
            if (activeSet != null) {
                activeSet.onDamageFinalized(cache, weapon);
            }
        } else if (target instanceof Player) {
            ArmorSet activeSet = armorSetManager.getActiveArmorSet((Player) target);
            if (activeSet != null) {
                activeSet.onDamageFinalized(cache, weapon);
            }
        }
    }
}