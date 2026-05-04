# Config And Datapack Data

Choose the config shape first. Pibrary has four common paths:

1. Ordinary player/server config: `PiConfigSpec` + `PiForgeConfigBinding`.
2. Already-loaded JSON: `PiConfigJson`.
3. One fixed datapack file, such as `gameplay.json`: `PiConfigSpec` + `PiConfigResourceReloader`.
4. One datapack file per registered entry, such as one file per spell: `PiDataConfigType`.

## Forge Config

The first path is the usual Forge config case: define a spec, then bind it to Forge config.

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
        return new ResourceLocation("example", path);
    }
}
```

Register once from the mod constructor:

```java
ModLoadingContext.get().registerConfig(
        ExampleConfigs.GAMEPLAY_CONFIG.suggestedType(),
        ExampleConfigs.GAMEPLAY_CONFIG.forgeSpec());
```

Gameplay code reads through typed entries:

```java
int maxEnergy = ExampleConfigs.GAMEPLAY_CONFIG.get(ExampleConfigs.MAX_ENERGY);
```

`PiConfigSpec` checks duplicate entries, mixed scopes, and invalid defaults when the spec is built. A bad config definition fails near bootstrap instead of turning into a runtime bug when a machine, facet, or packet eventually reads it.

## JSON Read And Write

If something else already loaded a `JsonObject`, use `PiConfigJson` only for Codec read/write:

```java
PiConfigValues values = PiConfigJson.read(ExampleConfigs.GAMEPLAY, jsonObject);
int maxEnergy = values.get(ExampleConfigs.MAX_ENERGY);
JsonObject defaults = PiConfigJson.write(PiConfigValues.defaults(ExampleConfigs.GAMEPLAY));
```

## Fixed Datapack File

For one fixed datapack file, keep the spec and change the scope to `SERVER_DATA_PACK`, then register it with a reloader:

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

This reads `data/example/example_config/gameplay.json`.

## One Config File Per Registered Entry

For config that belongs to registered content, use `PiDataConfigType`. This shape fits spell systems well: one registered spell, one datapack-overridable value file.

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

It reads files like these:

```text
data/example/example_config/spell/fireball.json
data/example/example_config/spell/frostbolt.json
```

## Datagen

Datagen uses a collector instead of hand-written paths. The most direct manual version adds each entry from the provider:

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
        return new ResourceLocation("example", path);
    }
}
```

You can also create a `PiDataConfigEntry` explicitly:

```java
PiDataConfigEntry<SpellRules> fireball =
        ExampleDataConfigs.SPELLS.entry(id("fireball"), new SpellRules(8, 120, 4));

collector.add(fireball);
```

Then attach the provider from `GatherDataEvent`:

```java
@SubscribeEvent
public static void gatherData(GatherDataEvent event) {
    event.getGenerator().addProvider(
            event.includeServer(),
            new ExampleConfigGen(event.getGenerator().getPackOutput()));
}
```

If a custom Registrate builder already registered default config through `dataConfig(...)`, the provider can collect those entries too:

```java
@Override
protected void add(PiDataConfigCollector collector) {
    ExampleEntries.REGISTRATE.collectDataConfigs(collector);
    collector.add(ExampleDataConfigs.SPELLS, id("blink"), new SpellRules(10, 40, 8));
}
```

## Keep Registration And Default Config Together

A custom Registrate entry point can keep registration and default config together. Registering the spell also registers a datagen task, and the data provider writes everything in one pass:

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

The important part is the chain: registration, default data, datapack overrides, and runtime lookup all point at the same typed handles. Spell code only needs `SpellType` and `SpellRules`; datapacks can change mana, cooldown, range, or similar values without changing gameplay code.

## Combined Lookup

If runtime code needs a combined lookup, such as "fireball uses different values on zombies", put the override files in another datapack config type and cache the lookup with `PiDataConfigView`:

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

Base files can live at `spell/fireball.json`; entity override files can live at `spell_entity/fireball_zombie.json`. After resource reload, the cached view is invalidated and rebuilt on the next query. Normal gameplay queries do not scan every JSON file.
