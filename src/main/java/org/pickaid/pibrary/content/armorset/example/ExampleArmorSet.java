package org.pickaid.pibrary.content.armorset.example;

import dev.xkmc.l2damagetracker.contents.attack.PlayerAttackCache;
import net.minecraftforge.event.entity.player.CriticalHitEvent;
import org.pickaid.pibrary.content.armorset.ArmorSet;
import org.pickaid.pibrary.content.armorset.ISetEffect;

public class ExampleArmorSet extends ArmorSet {
    public ExampleArmorSet(ISetEffect effect) {
        super(effect);
    }

    @Override
    public boolean attackerOnCriticalHit(PlayerAttackCache cache, CriticalHitEvent event) {
        event.setResult(CriticalHitEvent.Result.DENY);
        return super.attackerOnCriticalHit(cache, event);
    }

    @Override
    public void onPlayerAttack(PlayerAttackCache cache) {
        if (cache.getPlayerAttackEntityEvent().getTarget() != null) {
            cache.getPlayerAttackEntityEvent().getTarget().kill();
        }
    }
}
