package org.pickaid.pibrary.api.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;

import net.minecraft.world.damagesource.CombatRules;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import org.junit.jupiter.api.Test;

class PiDamagesTest {
    @Test
    void zeroPenetrationMatchesVanillaArmorFormula() {
        float damage = 10.0F;
        float armor = 20.0F;
        float toughness = 8.0F;

        assertEquals(
                CombatRules.getDamageAfterAbsorb(damage, armor, toughness),
                PiDamages.afterArmorPenetration(damage, armor, toughness, PiArmorPenetration.none()),
                0.0001F);
    }

    @Test
    void flatPenetrationUsesReducedArmor() {
        float damage = 12.0F;
        float armor = 18.0F;
        float toughness = 4.0F;

        assertEquals(
                CombatRules.getDamageAfterAbsorb(damage, 12.0F, toughness),
                PiDamages.afterArmorPenetration(damage, armor, toughness, PiArmorPenetration.flat(6.0F)),
                0.0001F);
    }

    @Test
    void ratioAndFlatPenetrationApplyBeforeVanillaArmorFormula() {
        PiArmorPenetration penetration = PiArmorPenetration.of(5.0F, 0.5F);

        assertEquals(5.0F, penetration.reduceArmor(20.0F), 0.0001F);
        assertEquals(
                CombatRules.getDamageAfterAbsorb(10.0F, 5.0F, 2.0F),
                PiDamages.afterArmorPenetration(10.0F, 20.0F, 2.0F, penetration),
                0.0001F);
    }

    @Test
    void penetrationAppliedInLivingHurtEventCompensatesForLaterVanillaArmor() {
        LivingHurtEvent event = new LivingHurtEvent(null, null, 10.0F);

        float writtenAmount = PiDamages.applyArmorPenetration(event, 20.0F, 0.0F, PiArmorPenetration.flat(20.0F));

        assertEquals(writtenAmount, event.getAmount(), 0.0001F);
        assertEquals(10.0F, CombatRules.getDamageAfterAbsorb(event.getAmount(), 20.0F, 0.0F), 0.0001F);
    }

    @Test
    void partialPenetrationCompensationMatchesReducedArmorResultAfterVanillaArmorPass() {
        float damage = 14.0F;
        float armor = 22.0F;
        float toughness = 6.0F;
        PiArmorPenetration penetration = PiArmorPenetration.of(3.0F, 0.35F);
        float expectedFinalDamage = PiDamages.afterArmorPenetration(damage, armor, toughness, penetration);

        LivingHurtEvent event = new LivingHurtEvent(null, null, damage);
        PiDamages.applyArmorPenetration(event, armor, toughness, penetration);

        assertEquals(
                expectedFinalDamage,
                CombatRules.getDamageAfterAbsorb(event.getAmount(), armor, toughness),
                0.0001F);
    }
}
