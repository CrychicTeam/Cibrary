# Tint 染色

Tint 只在模型 JSON 或生成模型里存在 `tintindex` 时生效。Pibrary 不接管渲染，只把常见 `BlockColor` / `ItemColor` 声明集中到 Registrate 入口上。

普通染色应该很短：

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

多 layer 或 stack 相关颜色再使用 builder：

```java
REGISTRATE
        .tintBlock(MANA_RELAY, tint -> tint
                .layer(0).constant(0x44AAFF)
                .layer(1).blockEntity(ManaRelayBlockEntity.class, ManaRelayBlockEntity::color, 0xFFFFFF))
        .tintDurability(SPELL_WAND, 1, 0xAA2222, 0x22AAFF)
        .tintItem(SPELL_SCROLL, tint -> tint
                .layer(0).stack(SpellScrollItem::spellColor));
```

常用颜色来源：

- 固定 RGB。
- 指定 tint layer。
- 草、树叶、水的 biome tint。
- `BlockState` 或 `BlockEntity`。
- `ItemStack`。
- 耐久渐变。
- block item 继承 block tint。

颜色计算放在 `PiColors`：`rgb`、`mix`、`multiply`、`hsv`、`pulse`。
