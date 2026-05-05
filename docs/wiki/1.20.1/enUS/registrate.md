# PiRegistrate And Creative Tabs

Blocks, items, block entities, models, loot, and recipes still follow Registrate style. Pibrary adds only the pieces that reduce repeated production code: id helpers, creative-tab sections, NBT variants, tint declaration, and a few composition helpers.

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
}
```

`section` is Pibrary's fill-order metadata for creative tabs. It is not a vanilla feature. Pibrary uses it when Forge asks the tab to populate contents.

`variant(...)` is for one NBT variant. `variants(...)` expands a list or runtime source into multiple creative-tab entries.
