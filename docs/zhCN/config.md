# 配置与 datapack 数据

配置先按使用场景分清楚，不要把几种入口混在一起看：

1. 玩家或服务端管理员会改的普通配置，用 `PiConfigSpec` + `PiForgeConfigBinding`。
2. 已经拿到一份 `JsonObject`，只想按 Pibrary 的 entry 读写，用 `PiConfigJson`。
3. datapack 里只有一个固定文件，比如 `gameplay.json`，用 `PiConfigSpec` + `PiConfigResourceReloader`。
4. 每个注册项都有自己的 datapack 文件，比如每个 spell 一份 JSON，用 `PiDataConfigType`。

## Forge Config

最常见的是第一种：写 spec，然后交给 Forge config。

```java
public final class ExampleConfigs {
    public static final PiConfigEntry<Integer> MAX_ENERGY = PiConfigEntry
            .builder(id("gameplay/max_energy"), Codec.INT, PiConfigScope.COMMON_BOOTSTRAP, 100)
            .comment("Maximum energy stored by the counter system.")
            .alsoValidate(PiConfigValidators.intRange(1, 10_000))
            .build();

    public static final PiConfigEntry<Mode> MODE = PiConfigEntry
            .builder(
                    id("gameplay/mode"),
                    PiSerializers.enumType(Mode.class),
                    PiConfigScope.COMMON_BOOTSTRAP,
                    Mode.NORMAL)
            .comment("Counter mode.")
            .build();

    public static final PiConfigSpec GAMEPLAY = PiConfigSpec
            .builder(REGISTRATE.loc("gameplay"), PiConfigScope.COMMON_BOOTSTRAP)
            .entry(MAX_ENERGY)
            .entry(MODE)
            .build();

    public static final PiForgeConfigBinding GAMEPLAY_CONFIG =
            PiForgeConfigBinding.build(GAMEPLAY);

    public enum Mode {
        NORMAL,
        HARD
    }

    private ExampleConfigs() {
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath("example", path);
    }
}
```

模组构造器里注册一次：

```java
ModLoadingContext.get().registerConfig(
        ExampleConfigs.GAMEPLAY_CONFIG.suggestedType(),
        ExampleConfigs.GAMEPLAY_CONFIG.forgeSpec());
```

业务代码里直接读 typed entry：

```java
int maxEnergy = ExampleConfigs.GAMEPLAY_CONFIG.get(ExampleConfigs.MAX_ENERGY);
```

`PiConfigSpec` 会在构建时检查重复 entry、作用域混用和非法默认值。配置写错会在启动阶段暴露，不会拖到机器、facet 或网络包真正读取时才炸。

## JSON 读写

如果数据已经是 JSON，不需要资源扫描，只做 Codec 读写：

```java
PiConfigValues values = PiConfigJson.read(ExampleConfigs.GAMEPLAY, jsonObject);
int maxEnergy = values.get(ExampleConfigs.MAX_ENERGY);
JsonObject defaults = PiConfigJson.write(PiConfigValues.defaults(ExampleConfigs.GAMEPLAY));
```

## 固定 datapack 文件

固定文件式 datapack 配置也还是 spec，只是 scope 换成 `SERVER_DATA_PACK`，再注册到 reloader：

```java
public final class ExampleDataConfigs {
    public static final PiConfigEntry<Integer> MAX_ENERGY = PiConfigEntry
            .builder(id("gameplay/max_energy"), Codec.INT, PiConfigScope.SERVER_DATA_PACK, 100)
            .alsoValidate(PiConfigValidators.intRange(1, 10_000))
            .build();

    public static final PiConfigSpec GAMEPLAY = PiConfigSpec
            .builder(id("gameplay"), PiConfigScope.SERVER_DATA_PACK)
            .entry(MAX_ENERGY)
            .build();

    public static final PiConfigResourceReloader RELOADER =
            new PiConfigResourceReloader("example_config");

    public static final PiConfigResourceBinding GAMEPLAY_VALUES =
            RELOADER.register(GAMEPLAY);
}

@SubscribeEvent
public static void addReloadListeners(AddReloadListenerEvent event) {
    event.addListener(ExampleDataConfigs.RELOADER);
}

int maxEnergy = ExampleDataConfigs.GAMEPLAY_VALUES.get(ExampleDataConfigs.MAX_ENERGY);
```

这段读取的是 `data/example/example_config/gameplay.json`。

## 每个注册项一份配置

如果配置跟注册内容一一对应，用 `PiDataConfigType`。这种形态很适合 spell：一个 spell 一个注册项，也有一份可被 datapack 改写的数值配置。

```java
public final class ExampleDataConfigs {
    public static final PiDataConfigType<SpellRules> SPELLS =
            PiDataConfigType.create("spell", SpellRules.CODEC);

    public static final PiConfigResourceReloader RELOADER =
            new PiConfigResourceReloader("example_config");

    public static final PiDataConfigBinding<SpellRules> SPELL_VALUES =
            RELOADER.register(SPELLS);

    private ExampleDataConfigs() {
    }
}

@SubscribeEvent
public static void addReloadListeners(AddReloadListenerEvent event) {
    event.addListener(ExampleDataConfigs.RELOADER);
}

SpellRules fireball = ExampleDataConfigs.SPELL_VALUES.requireEntry(id("fireball"));
Map<ResourceLocation, SpellRules> allSpells = ExampleDataConfigs.SPELL_VALUES.entries();
```

它读取的是这一类文件：

```text
data/example/example_config/spell/fireball.json
data/example/example_config/spell/frostbolt.json
```

## Datagen

datagen 不手写路径，统一交给 collector。最直接的手写方式是在 provider 里逐条 `add`：

```java
public final class ExampleConfigGen extends PiDataConfigProvider {
    public ExampleConfigGen(PackOutput output) {
        super(output, "example_config", "Example datapack configs");
    }

    @Override
    protected void add(PiDataConfigCollector collector) {
        collector.add(ExampleDataConfigs.SPELLS, id("fireball"), new SpellRules(8, 120, 4));
        collector.add(ExampleDataConfigs.SPELLS, id("frostbolt"), new SpellRules(10, 90, 6));
        collector.add(ExampleDataConfigs.SPELLS, id("blink"), new SpellRules(10, 40, 8));
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath("example", path);
    }
}
```

也可以显式创建 `PiDataConfigEntry`：

```java
PiDataConfigEntry<SpellRules> fireball =
        ExampleDataConfigs.SPELLS.entry(id("fireball"), new SpellRules(8, 120, 4));

collector.add(fireball);
```

然后在 `GatherDataEvent` 里挂上这个 provider：

```java
@SubscribeEvent
public static void gatherData(GatherDataEvent event) {
    event.getGenerator().addProvider(
            event.includeServer(),
            new ExampleConfigGen(event.getGenerator().getPackOutput()));
}
```

如果自定义 Registrate builder 已经通过 `dataConfig(...)` 登记默认配置，provider 里也可以把这些配置一起收进来：

```java
@Override
protected void add(PiDataConfigCollector collector) {
    ExampleEntries.REGISTRATE.collectDataConfigs(collector);
    collector.add(ExampleDataConfigs.SPELLS, id("blink"), new SpellRules(10, 40, 8));
}
```

## 和 Registrate 放在同一条链

自定义 Registrate 入口可以把注册和默认配置绑在一起。注册 spell 时登记一条 datagen 任务，之后 data provider 统一写文件：

```java
public final class ExampleRegistrate extends PiBaseRegistrate<ExampleRegistrate> {
    public NoConfigBuilder<SpellType, SpellType, ExampleRegistrate> spell(
            String name,
            Supplier<SpellType> factory,
            Function<ResourceLocation, SpellRules> config
    ) {
        ResourceLocation id = loc(name);
        dataConfig(ExampleDataConfigs.SPELLS.entry(id, config.apply(id)));
        return generic(name, SPELLS_REGISTRY, factory::get);
    }
}

public static final RegistryEntry<SpellType> FIREBALL = REGISTRATE.spell(
        "fireball",
        FireballSpell::new,
        id -> new SpellRules(8, 120, 4))
        .register();
```

这类配置的强点在于注册项、默认数据、datapack 覆盖和运行时查询能走同一条链。spell 代码只关心 `SpellType` 和 `SpellRules`，datapack 可以改 mana、cooldown、range 这类数值，业务逻辑只查 typed config。

## 组合查询

如果运行时需要按组合条件查询，例如“fireball 对 zombie 使用另一套数值”，把覆盖配置也做成一类 datapack 文件，然后用 `PiDataConfigView` 缓存索引：

```java
public static final PiDataConfigType<EntitySpellRules> ENTITY_SPELLS =
        PiDataConfigType.create("spell_entity", EntitySpellRules.CODEC);

public static final PiDataConfigBinding<EntitySpellRules> ENTITY_SPELL_VALUES =
        RELOADER.register(ENTITY_SPELLS);

public static final PiDataConfigView<EntitySpellRules, Map<EntitySpellKey, SpellRules>> ENTITY_INDEX =
        ENTITY_SPELL_VALUES.view(values -> ExampleDataConfigs.indexEntityRules(values.values()));

public static SpellRules rulesFor(ResourceLocation spell, ResourceLocation entityType) {
    SpellRules override = ENTITY_INDEX.get().get(new EntitySpellKey(spell, entityType));
    if (override != null) {
        return override;
    }
    return SPELL_VALUES.requireEntry(spell);
}
```

基础文件可以放在 `spell/fireball.json`，实体覆盖文件可以放在 `spell_entity/fireball_zombie.json`。资源 reload 后索引会自动失效，下一次查询时重建；平时查询不会每次遍历全部 JSON。
