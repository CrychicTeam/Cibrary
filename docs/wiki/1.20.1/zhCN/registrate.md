# PiRegistrate 和创造栏

方块、物品、方块实体、模型、loot、recipe 继续按 Registrate 的写法来。Pibrary 只补确实减少重复且有运行时意义的能力：id 路径、创造栏 section / NBT 变体、tint 声明和少数组合操作。

```java
public final class ExampleEntries {
    public static final PiRegistrate REGISTRATE = PiRegistrate.create("example");

    public static final BlockEntry<Block> RELAY_CORE = REGISTRATE
            .block("relay_core", Block::new)
            .initialProperties(PiBlockProps::metal)
            .properties(properties -> properties.noOcclusion())
            .blockTags(BlockTags.MINEABLE_WITH_PICKAXE, BlockTags.NEEDS_IRON_TOOL)
            .section("machines")
            .register();

    public static final ItemEntry<Item> COPPER_WAND = REGISTRATE
            .item("copper_wand", Item::new)
            .properties(properties -> properties.stacksTo(1).fireResistant())
            .section("materials")
            .register();

    public static void register() {
    }
}
```

## 创造栏 section

`section` 是 Pibrary 保存的分组顺序，不是原版多出来的新概念。Forge 填充创造栏时，Pibrary 按这个顺序写入内容。

```java
public static final RegistryEntry<CreativeModeTab> MAIN_TAB = REGISTRATE
        .creativeTab("main", tab -> tab
                .title("itemGroup.example.main")
                .iconStack(ExampleEntries::tabIcon)
                .sections("materials", "scrolls", "machines"))
        .register();
```

## NBT 变体

`variant(...)` 适合单个 NBT 变体；`variants(...)` 适合从列表或运行时数据源展开一批变体。

```java
public static final ItemEntry<Item> SPELL_SCROLL = REGISTRATE
        .item("spell_scroll", Item::new)
        .section("scrolls")
        .variants(STARTER_SPELLS, spell -> spell, (stack, spell) ->
                stack.getOrCreateTag().putString("spell", spell))
        .searchOnly()
        .register();
```

如果 spell 本身就是注册项，直接从真实 registry 句柄读取，不需要绕一层抽象分组系统。
