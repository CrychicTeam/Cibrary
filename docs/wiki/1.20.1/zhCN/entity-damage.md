# Entity Damage Helper

`PiDamages` 处理两类常见伤害问题：按原版公式计算穿甲后的最终伤害，以及临时绕过 `invulnerableTime` 造成伤害。

## 穿甲计算

`PiArmorPenetration` 先按比例忽略护甲，再减去固定护甲值。

```java
PiArmorPenetration penetration = PiArmorPenetration.of(5.0F, 0.5F);

float finalDamage = PiDamages.afterArmorPenetration(
        rawDamage,
        target.getArmorValue(),
        (float) target.getAttributeValue(Attributes.ARMOR_TOUGHNESS),
        penetration);
```

如果目标有 20 点护甲，`of(5, 0.5)` 会把有效护甲变成 `20 * 0.5 - 5 = 5`。之后仍然调用原版 `CombatRules.getDamageAfterAbsorb`，所以护甲韧性和原版公式保持一致。

## 在 `LivingHurtEvent` 中使用

`LivingHurtEvent` 发生在原版护甲计算之前。事件里不能直接把 amount 改成“穿甲后的最终伤害”，否则后续还会被原版护甲再削一次。

使用 `PiDamages.applyArmorPenetration`，它会反推一个 pre-armor amount 并写回事件。之后原版照常执行护甲计算，护甲计算结束后的数值会等于“有效护甲被降低后”的结果。

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

如果只想先计算数值，不改事件，可以直接使用：

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

## 绕过受伤冷却

有些连续命中的投掷物或实体特效需要像多段攻击一样命中同一个目标。`hurtIgnoringCooldown` 会临时把 `invulnerableTime` 置 0，调用 `hurt`，然后恢复原值。

```java
PiDamages.hurtIgnoringCooldown(target, source, amount);
```

这个方法只绕过本次 `hurt` 的受伤冷却，不会修改 damage type，也不会让伤害自动穿甲。
