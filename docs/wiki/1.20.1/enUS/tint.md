# Tint Helpers

Tint only applies when a model JSON or generated model has `tintindex`. Pibrary does not take over rendering; it keeps common `BlockColor` / `ItemColor` declarations near the Registrate entry point.

The common path should stay short:

```java
public static final BlockEntry<Block> GLOWING_LEAVES = REGISTRATE
        .block("glowing_leaves", Block::new)
        .initialProperties(PiBlockProps::wood)
        .register();

public static void registerClientTints() {
    REGISTRATE
            .tintFoliage(GLOWING_LEAVES)
            .tintBlockItem(GLOWING_LEAVES);
}
```

Use the builders for multi-layer or stack-dependent colors:

```java
REGISTRATE
        .tintBlock(MANA_RELAY, tint -> tint
                .layer(0).constant(0x44AAFF)
                .layer(1).blockEntity(ManaRelayBlockEntity.class, ManaRelayBlockEntity::color, 0xFFFFFF))
        .tintDurability(SPELL_WAND, 1, 0xAA2222, 0x22AAFF)
        .tintItem(SPELL_SCROLL, tint -> tint
                .layer(0).stack(SpellScrollItem::spellColor));
```

Covered sources:

- Constant RGB.
- Selected tint layer.
- Grass, foliage, and water biome tint.
- `BlockState` or `BlockEntity`.
- `ItemStack`.
- Durability gradient.
- Block-item delegation to block tint.

Color math lives in `PiColors`: `rgb`, `mix`, `multiply`, `hsv`, and `pulse`.
