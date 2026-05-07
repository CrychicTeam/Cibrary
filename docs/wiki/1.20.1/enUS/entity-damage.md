# Entity Damage Helpers

`PiDamages` covers two common damage tasks: calculating vanilla-compatible armor penetration and temporarily bypassing `invulnerableTime` for one hit.

## Armor Penetration

`PiArmorPenetration` applies ratio penetration first, then flat penetration.

```java
PiArmorPenetration penetration = PiArmorPenetration.of(5.0F, 0.5F);

float finalDamage = PiDamages.afterArmorPenetration(
        rawDamage,
        target.getArmorValue(),
        (float) target.getAttributeValue(Attributes.ARMOR_TOUGHNESS),
        penetration);
```

For a target with 20 armor, `of(5, 0.5)` turns effective armor into `20 * 0.5 - 5 = 5`. The final calculation still uses vanilla `CombatRules.getDamageAfterAbsorb`, so armor toughness and vanilla scaling stay intact.

## Use In `LivingHurtEvent`

`LivingHurtEvent` fires before vanilla armor reduction. Do not write the desired final piercing damage directly into the event amount, because vanilla armor will reduce it again afterwards.

Use `PiDamages.applyArmorPenetration`; it writes a compensated pre-armor amount back to the event. Vanilla then runs its normal armor pass, and the post-armor result matches the value calculated with reduced effective armor.

```java
@SubscribeEvent
public static void onHurt(LivingHurtEvent event) {
    if (!(event.getSource().getEntity() instanceof Player player)) {
        return;
    }
    if (!hasPiercingSet(player)) {
        return;
    }

    PiDamages.applyArmorPenetration(event, PiArmorPenetration.of(4.0F, 0.25F));
}
```

If you only need the numbers, calculate them directly:

```java
float finalDamage = PiDamages.afterArmorPenetration(
        event.getAmount(),
        target.getArmorValue(),
        (float) target.getAttributeValue(Attributes.ARMOR_TOUGHNESS),
        PiArmorPenetration.ratio(0.35F));

float eventAmount = PiDamages.beforeArmorForFinalDamage(
        finalDamage,
        target.getArmorValue(),
        (float) target.getAttributeValue(Attributes.ARMOR_TOUGHNESS));
```

## Bypass Hurt Cooldown

Some multi-hit projectiles or entity effects need to hit the same target repeatedly. `hurtIgnoringCooldown` temporarily sets `invulnerableTime` to 0, calls `hurt`, then restores the previous value.

```java
PiDamages.hurtIgnoringCooldown(target, source, amount);
```

This only bypasses the hurt cooldown for that one `hurt` call. It does not change the damage type and does not automatically bypass armor.
