package org.crychicteam.cibrary.armorset;

import dev.xkmc.l2damagetracker.contents.attack.PlayerAttackCache;
import net.minecraftforge.event.entity.player.CriticalHitEvent;

public class TestArmorSet extends ArmorSet{
    public TestArmorSet(String identifier, SetEffect effect) {
        super(identifier, effect);
    }

    @Override
    public boolean onCriticalHit(PlayerAttackCache cache, CriticalHitEvent event) {
        event.setResult(CriticalHitEvent.Result.DENY);
        return super.onCriticalHit(cache, event);
    }

    @Override
    public void onPlayerAttack(PlayerAttackCache cache) {
        super.onPlayerAttack(cache);
        if (cache.getPlayerAttackEntityEvent().getTarget() != null) {
            cache.getPlayerAttackEntityEvent().getTarget().kill();
        }
    }
}
