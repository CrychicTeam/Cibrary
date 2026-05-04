# Pibrary

[中文版](README.MD)

`Pibrary` is the root foundation mod for the PickAID stack. It keeps the stable pieces that every downstream mod is likely to reuse: attached state, registry helpers, entity and projectile utilities, math helpers, config entry points, diagnostics, and recipe-viewer-neutral contracts.

The current goal is practical:
1. downstream mods can depend on `pibrary` and start writing production code;
2. repeated modding code becomes short, stable, migration-friendly API;
3. moving from 1.20.1 to later versions should mostly touch compatibility layers, not gameplay code;
4. PiNet and PiSerializeKit become convenient building blocks through Pibrary-level entry points.

Detailed guides:

- [Text markup and visual components](docs/enUS/text-markup.md)
- [JEI integration shape](docs/enUS/jei-integration.md)
- [Config and datapack data](docs/enUS/config.md)
- [Math tutorial](docs/enUS/math.md)

## Credits

Pibrary's config and Registrate design was written after studying lcy's code in the L2 project family, especially L2Core and L2Hostility. The implementation here is independent, but the direction of keeping registered content, default datapack config, config-type loading, and datagen collection on one chain comes from that work.

## Use It Today

If you want persistent state attached to a player or mob, write a Facet. A Facet is attached to the entity, can persist state, can sync state, and can opt into hooks such as tick, clone, and display refresh.

```java
@PiLivingFacet(namespace = "example", path = "counter_player")
public final class CounterPlayerFacet extends PiStatePlayerFacet<CounterState> {
    public CounterPlayerFacet(PiLivingFacetContext context) {
        super(context);
    }
}

public final class ExampleLivingFacets {
    public static final PiLivingFacetType<CounterPlayerFacet> COUNTER_PLAYER =
            PiLivingFacets.bind(CounterPlayerFacet.class).register();

    private ExampleLivingFacets() {
    }

    public static void register() {
    }
}

CounterPlayerFacet facet = ExampleLivingFacets.COUNTER_PLAYER.get(player);
facet.gainEnergy(2);
```

Call `ExampleLivingFacets.register()` during common bootstrap. The empty method is there to force class loading, so the typed handle registers exactly once in a place you control.

If the state belongs to a `ServerLevel`, write a Level Facet. Level facets persist by default; sync is explicit so level-wide state does not accidentally become a network cost.

```java
@PiLevelFacet(namespace = "example", path = "counter_level")
public final class CounterLevelFacet extends PiStateLevelFacet<CounterState> {
    public CounterLevelFacet(PiLevelFacetContext context) {
        super(context);
    }
}

public final class ExampleLevelFacets {
    public static final PiLevelFacetType<CounterLevelFacet> COUNTER_LEVEL =
            PiLevelFacets.bind(CounterLevelFacet.class).register();

    private ExampleLevelFacets() {
    }

    public static void register() {
    }
}

CounterLevelFacet facet = ExampleLevelFacets.COUNTER_LEVEL.get(level);
facet.increment();
```

If the state belongs to a loaded chunk, write a Chunk Facet. It uses chunk capability persistence and does not load or generate chunks by itself. `findLoaded(...)` returns empty when the chunk is not already loaded.

```java
@PiChunkFacet(namespace = "example", path = "ore_memory")
public final class OreMemoryFacet extends PiStateChunkFacet<OreMemoryState> {
    public OreMemoryFacet(PiChunkFacetContext context) {
        super(context);
    }
}

public final class ExampleChunkFacets {
    public static final PiChunkFacetType<OreMemoryFacet> ORE_MEMORY =
            PiChunkFacets.bind(OreMemoryFacet.class).register();

    private ExampleChunkFacets() {
    }

    public static void register() {
    }
}

Optional<OreMemoryFacet> memory =
        PiChunkFacets.findLoaded(serverLevel, chunkPos, OreMemoryFacet.class);
```

Call `ExampleLevelFacets.register()` and `ExampleChunkFacets.register()` during your own mod bootstrap. After that, gameplay code uses typed handles and does not need to touch capability keys or generated descriptors directly.

For blocks, items, block entities, models, loot, and recipes, keep the normal Registrate chain. Registrate already has clear direct methods such as `tag(...)`, `properties(...)`, `simpleItem()`, `defaultLoot()`, and `defaultModel()`. Use those directly when they read well. Pibrary mainly adds id helpers, creative-tab sections, NBT variants, tint declarations, and direct builder methods for common block/item tag cases.

If the project only needs ordinary block and item registration, use `PiRegistrate`:

```java
public final class ExampleEntries {
    public static final PiRegistrate REGISTRATE = PiRegistrate.create("example");

    public static final BlockEntry<Block> RELAY_CORE = REGISTRATE
            .block("relay_core", Block::new)
            .initialProperties(PiBlockProps::metal)
            .properties(properties -> properties
                    .noOcclusion()
                    .isValidSpawn((state, level, pos, type) -> false))
            .blockAndItemTag(Tags.Blocks.STORAGE_BLOCKS_IRON, Tags.Items.STORAGE_BLOCKS_IRON)
            .blockTags(BlockTags.MINEABLE_WITH_PICKAXE, BlockTags.NEEDS_IRON_TOOL)
            .section("machines")
            .onRegister(block -> {
                // Add runtime indexes, debug tracking, or project-level caches here.
            })
            .register();

    public static final ItemEntry<Item> COPPER_WAND = REGISTRATE
            .item("copper_wand", Item::new)
            .properties(properties -> properties.stacksTo(1).fireResistant())
            .itemTags(Tags.Items.INGOTS_COPPER)
            .section("materials")
            .register();

    public static final RegistryEntry<SoundEvent> RELAY_OPEN = REGISTRATE
            .generic(
                    "relay_open",
                    Registries.SOUND_EVENT,
                    () -> SoundEvent.createVariableRangeEvent(REGISTRATE.loc("relay_open")))
            .onRegister(sound -> {
                // This is not a block or item, but it can still use Registrate's registration callback.
            })
            .register();

    private ExampleEntries() {
    }

    public static void register() {
    }
}
```

For creative tabs, register the tab once, then put section names directly on the normal `item(...)` and `block(...)` chains. `section` is Pibrary's own ordering data. It is not a vanilla section system. When Forge asks for creative-tab contents, Pibrary writes the entries in that order.

```java
public final class ExampleEntries {
    public static final PiRegistrate REGISTRATE = PiRegistrate.create("example");
    private static final List<String> STARTER_SPELLS = List.of("fireball", "frostbolt", "blink");

    public static final RegistryEntry<CreativeModeTab> MAIN_TAB = REGISTRATE
            .creativeTab("main", tab -> tab
                    .title("itemGroup.example.main")
                    .iconStack(ExampleEntries::tabIcon)
                    .sections("materials", "scrolls", "machines"))
            .register();

    public static final BlockEntry<Block> CHARGING_TABLE = REGISTRATE
            .block("charging_table", Block::new)
            .section("machines")
            .initialProperties(PiBlockProps::metal)
            .simpleItem()
            .register();

    public static final ItemEntry<Item> MANA_DUST = REGISTRATE
            .item("mana_dust", Item::new)
            .section("materials")
            .register();

    public static final ItemEntry<Item> SPELL_SCROLL = REGISTRATE
            .item("spell_scroll", Item::new)
            .section("scrolls")
            .variants(STARTER_SPELLS, spell -> spell, (stack, spell) ->
                    stack.getOrCreateTag().putString("spell", spell))
            .searchOnly()
            .register();

    private static ItemStack tabIcon() {
        return new ItemStack(CHARGING_TABLE.get());
    }
}
```

Use `variant(...)` for one NBT variant and `variants(...)` when a list or runtime source should expand into many stacks, such as one scroll item for several spells or one charged tool for several levels. Passing a `List` snapshots the values immediately. Passing `Supplier<Iterable<...>>` reads the values when the creative tab is rebuilt, which is useful for config- or registry-driven content.

The `STARTER_SPELLS` value above is just a normal list:

```java
private static final List<String> STARTER_SPELLS =
        List.of("fireball", "frostbolt", "blink");
```

If spells are registry entries in your project, do not route them through `PiRegistryPlan` just to build creative variants. Pass the real registry handle from your project and read it when the creative tab is rebuilt:

```java
static ItemBuilder<Item, PiRegistrate> spellScrollFromRegistry(
        PiRegistrate registrate,
        IForgeRegistry<SpellType> spells
) {
    return registrate.item("spell_scroll", Item::new)
            .section("scrolls")
            .variants(
                    () -> spells.getValues().stream()
                            .filter(SpellType::enabled)
                            .sorted(Comparator.comparing(spell -> spellId(spells, spell).toString()))
                            .toList(),
                    spell -> spellId(spells, spell).getPath(),
                    (stack, spell) -> stack.getOrCreateTag()
                            .putString("spell", spellId(spells, spell).toString()))
            .searchOnly()
            .properties(properties -> properties.stacksTo(1));
}

private static ResourceLocation spellId(IForgeRegistry<SpellType> spells, SpellType spell) {
    ResourceLocation id = spells.getKey(spell);
    if (id == null) {
        throw new IllegalStateException("Spell is not registered: " + spell);
    }
    return id;
}
```

`SpellType`, `enabled()`, and the registry variable belong to your mod. They are not Pibrary APIs. Pibrary only expands the real source passed to `Supplier` into creative-tab stacks.

`searchOnly()` puts the entry only in search, `parentOnly()` keeps it only in the current tab, and `hidden()` keeps it out of creative output. To place content in another tab, call `section(tabKey, "section_name")`.

`section("materials")` uses the default creative tab. The tab may be declared later in the same registration class; Pibrary resolves it when creative contents are registered. If a class uses several tabs, prefer `section(tabKey, "materials")`.

If the project has its own first-class concepts, such as spells, schools, runes, or modules, do not put them into Pibrary's shared helpers. Give the project its own Registrate entry point:

```java
public final class ExampleRegistrate extends PiBaseRegistrate<ExampleRegistrate> {
    private static final ResourceKey<Registry<SpellType>> SPELLS =
            ResourceKey.createRegistryKey(new ResourceLocation("example", "spells"));

    private ExampleRegistrate(String modid) {
        super(modid);
    }

    public static ExampleRegistrate create(String modid, IEventBus modBus) {
        ExampleRegistrate registrate = new ExampleRegistrate(modid);
        return registrate.registerTo(modBus);
    }

    public NoConfigBuilder<SpellType, SpellType, ExampleRegistrate> spell(
            String name,
            NonNullSupplier<SpellType> factory
    ) {
        return generic(name, SPELLS, factory);
    }
}

public final class ExampleSpells {
    public static final ExampleRegistrate REGISTRATE =
            ExampleRegistrate.create("example", MOD_BUS);

    public static final RegistryEntry<SpellType> FIREBALL = REGISTRATE
            .spell("fireball", FireballSpell::new)
            .onRegister(SpellRuntime::index)
            .register();
}
```

The rule is simple: Pibrary provides thin shared tools, while each mod keeps its own gameplay words in its own `Registrate` subclass.

There are four layers here:

1. The native Registrate chain owns the main registration flow. Use `properties(...)`, `tag(...)`, `item()`, `model(...)`, `loot(...)`, and `onRegister(...)` directly when they say what you mean.
2. Pibrary builder methods cover the few places Registrate is not direct enough, such as `section(...)`, `variants(...)`, `blockTags(...)`, `itemTags(...)`, and `blockAndItemTag(...)`.
3. `REGISTRATE.tintFoliage(...)`, `tintBlock(...)`, and `tintItem(...)` are the client tint layer. Common colors use short methods; complex colors use a small builder.
4. Custom blockstates, custom loot, renderers, and complex block entities should stay as native Registrate code. Keep the hard parts explicit instead of hiding them behind a wrapper that becomes awkward in real projects.

If a model JSON uses `tintindex`, use the helpers in `api/render/tint`. Put the declarations under the same `REGISTRATE`; Pibrary consumes them from the client color events:

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

The common path should stay that short. Use the builder only when a model has several tint layers or stack-dependent colors:

```java
REGISTRATE
        .tintBlock(RELAY_CORE, tint -> tint
                .layer(0).constant(0x44AAFF)
                .layer(1).foliage())
        .tintDurability(COPPER_WAND, 1, 0xAA2222, 0x22AAFF)
        .tintItem(SPELL_SCROLL, tint -> tint
                .layer(0).stack(SpellScrollItem::spellColor));
```

Block entity tinting is an advanced case, not the default path. Use it when the model is still a normal baked model but the color comes from runtime block entity data:

```java
REGISTRATE.tintBlock(MANA_POOL, tint -> tint
        .layer(0).blockEntity(ManaPoolBlockEntity.class, ManaPoolBlockEntity::color, 0xFFFFFF));
```

Common sources are covered: constant colors, selected layers, block state, block entity, grass/foliage/water biome tint, item stack, durability gradient, and block-item delegation. Color math lives in `PiColors`, including `mix`, `multiply`, `hsv`, and `pulse`.

Do not start normal registration with `PiRegistryPlan`. Most blocks, items, sounds, particles, and custom registry entries should use `REGISTRATE.block(...)`, `item(...)`, or `generic(...)` directly.

`api/registry` stays for narrow advanced cases: several modules collecting a simple registry list before application, early duplicate-id checks, or one list being consumed by different Forge / NeoForge adapters. It is not the main entry point, and it is not a factory for complex blocks, items, or entities.

Text stays close to vanilla `Component`. Use `PiTexts.literal(...)` and `PiTexts.translatable(...)` for normal text. Use `PiTexts.markup(...)` or `PiTexts.markupTranslatable(...)` when a tooltip, guide page, or screen label needs rich text.

```java
MutableComponent line = PiTexts.markupTranslatable(
        "tooltip.example.spell.fireball",
        "**{name}** ![image]({icon}#size=18x18)\\nCost: [mana]({spell})",
        scope,
        PiTextArgs.of()
                .component("name", spell.getDisplayName())
                .resource("icon", spell.icon())
                .resource("spell", spell.id()));
```

The full guide, including `{name}` arguments, images, animated images, custom inline rules, language datagen, and renderer registration, lives in [docs/enUS/text-markup.md](docs/enUS/text-markup.md).

When machine logic and a recipe viewer need the same data, normalize it once as `PiRecipeView`. Machine code can query and cache it with `PiRecipeLookupCache`. Viewer compat code can consume the same `PiRecipeSource`.

```java
public final class CounterRecipes {
    public static final CounterRecipe CHARGE =
            new CounterRecipe("counter_dust", "counter_core", 40);

    public static final PiRecipeSource<CounterRecipe> SOURCE =
            level -> List.of(view(CHARGE));

    private CounterRecipes() {
    }

    public static PiRecipeView<CounterRecipe> view(CounterRecipe recipe) {
        return new PiRecipeView<>(
                PiIds.id("example", "counter_charge"),
                recipe,
                List.of(
                        new PiRecipeIngredient<>(PiRecipeRole.INPUT, recipe.input()),
                        new PiRecipeIngredient<>(PiRecipeRole.OUTPUT, recipe.output()),
                        new PiRecipeIngredient<>(PiRecipeRole.DISPLAY, recipe.ticks())
                ));
    }

    public record CounterRecipe(String input, String output, int ticks) {
    }
}
```

A block entity does not need to scan every recipe on every tick. Keep a cache and clear it when inputs or recipe data change:

```java
public final class CounterBlockEntity extends BlockEntity {
    private final PiRecipeLookupCache<CounterRecipes.CounterRecipe> recipes =
            new PiRecipeLookupCache<>(new PiRecipeLookup<>(CounterRecipes.SOURCE));

    public Optional<PiRecipeMatch<CounterRecipes.CounterRecipe>> findRecipe(Level level, String input) {
        return recipes.find(level, view -> view.recipe().input().equals(input));
    }

    public void onInventoryChanged() {
        recipes.clear();
    }
}
```

If that block entity has a menu, `PiMenuData` can go straight into vanilla `addDataSlots(...)`. The server reads getters and the client receives values through setters. If the block entity is also a `PiPresentationSource`, screen projections marked with `refreshOnMenuData(...)` are invalidated automatically.

```java
public final class CounterBlockEntity extends PiStateBlockEntity<CounterState>
        implements PiPresentationSource {
    public PiMenuData menuData() {
        return menuData(
                PiMenuDataSlot.mutable(
                        () -> viewState().count,
                        value -> updateState(state -> state.count = value)),
                PiMenuDataSlot.mutable(
                        () -> viewState().energy,
                        value -> updateState(state -> state.energy = value))
        );
    }

    @Override
    public void contributePresentation(PiPresentationContext context) {
        context.screens().snapshot(CounterScreenModel.class,
                partialTick -> CounterScreenModel.from(viewState()));
        context.screens().refreshOnMenuData(CounterScreenModel.class);
    }
}
```

The same source can feed recipe-viewer compat. `api/jei` still imports no JEI classes; the real plugin layer only has to translate these neutral specs into the target viewer API.

```java
PiJeiRecipeTypeKey<CounterRecipes.CounterRecipe> type =
        new PiJeiRecipeTypeKey<>(
                PiIds.id("example", "counter_charging"),
                CounterRecipes.CounterRecipe.class);

PiJeiCategorySpec<CounterRecipes.CounterRecipe> category =
        new PiJeiCategorySpec<>(type, Component.literal("Counter Charging"), 116, 54, 0,
                view -> PiRecipeLayout.builder(view)
                        .input(18, 18, view.recipe().input())
                        .thenOutput(64, view.recipe().output())
                        .build());

PiJeiRecipeSourceSpec<CounterRecipes.CounterRecipe> source =
        new PiJeiRecipeSourceSpec<>(type, CounterRecipes.SOURCE);

PiJeiBootstrap bootstrap = new PiJeiBootstrap();
bootstrap.registerCategory(category);
bootstrap.registerRecipeSource(source);

PiJeiRecipeCatalog catalog = PiJeiRecipeCatalog.from(bootstrap);
List<PiJeiRecipeDisplay<CounterRecipes.CounterRecipe>> displays = catalog.displays(type, level);
```

`PiJeiRecipeCatalog.from(...)` checks that recipe sources, click areas, and transfer specs point at registered categories. If the same recipe type id is registered with two different recipe classes, it fails there instead of later in the concrete compat layer.

The concrete JEI compat class can register `PiJeiRecipeDisplay` as the JEI recipe object, then translate `PiRecipeSlot` into JEI slots from `IRecipeCategory#setRecipe(...)`. See [docs/enUS/jei-integration.md](docs/enUS/jei-integration.md) for the full structure.

For complex recipes, do not turn this into a large layout engine. Use slot hints for multiple candidate inputs, hidden lookup ingredients, and linked input/output variants:

```java
PiRecipeLayout.builder(view)
        .slot(PiRecipeSlot.builder(PiRecipeRole.INPUT, 18, 18)
                .name("input")
                .standardBackground()
                .values(view.recipe().acceptedInputs())
                .focusGroup("variant")
                .tooltip(PiTexts.literal("Any matching input"))
                .build())
        .slot(PiRecipeSlot.builder(PiRecipeRole.OUTPUT, 82, 18)
                .name("output")
                .outputBackground()
                .values(view.recipe().possibleOutputs())
                .focusGroup("variant")
                .build())
        .slot(PiRecipeSlot.builder(PiRecipeRole.INPUT, 50, 18)
                .name("fluid")
                .fluidRenderer(1000, true, 16, 48)
                .value(view.recipe().fluidInput())
                .build())
        .slot(PiRecipeSlot.builder(PiRecipeRole.INPUT, 0, 0)
                .values(view.recipe().lookupOnlyInputs())
                .hiddenLookup()
                .build())
        .build();
```

For shared math helpers, use `api/math` directly. The APIs use vanilla `Vec3` and `AABB`, so they fit entity, collision, render, and targeting code without conversion:

```java
Vec3 direction = PiVectors.safeNormalize(target.subtract(origin), new Vec3(0, 0, 1));
AABB preview = PiAabbs.centered(origin.add(direction.scale(4)), 2, 2, 2);

PiCameraFrame frame = new PiCameraFrame(
        cameraPos,
        cameraForward,
        cameraUp,
        70.0D,
        0.05D,
        1000.0D,
        new PiViewport(windowWidth, windowHeight)
);

PiProjectedPoint marker = frame.project(target);
PiProjectedPoint.ScreenPoint edge = marker.edgeClamped(frame.viewport(), 8.0D, true);
```

These helpers are meant for targeting, projectile math, HUD markers, world previews, simple animation curves, and weighted choices. Larger visual systems can build on them as low-level pieces.

For the complete Math guide, including scalar/range helpers, Vec3/AABB helpers, curves, weights, camera projection, and edge indicators, see [docs/enUS/math.md](docs/enUS/math.md).

## Current Code State

This repository is split by responsibility:

1. `src/main/java/org/pickaid/pibrary/api/**`
   stable entry points for downstream mods.
2. `src/main/java/org/pickaid/pibrary/runtime/**`
   Forge 1.20.1 implementations such as capability, sync, creative-tab, recipe-cache, and projectile-trace wiring.
3. `src/main/java/org/pickaid/pibrary/mixin/**`
   access seams for vanilla internals that cannot be reached cleanly through public APIs.
4. `src/devExample/java/**`
   examples compiled by tests but excluded from the production jar.

The preserved legacy snapshot remains at `restore/legacy-monolith-20260331` for migration reference only. It is not the current API documentation.

## Usable Now

In the current committed tree, the most usable public packages are:
1. `api/core`
   low-level keys and shared registry contracts for the few systems that need global replacement.
2. `api/config`
   config entries, grouped specs, scopes, comments, and default-value validation.
3. `api/diagnostics`
   unified debug and diagnostics sinks.
4. `api/registry`
   simple registry request / plan / adapter boundaries for narrow advanced cases, not the normal registration path.
5. `api/state`
   shared state keys and sync policy.
6. `api/facet`
   living / level / chunk facet annotations, typed handles, descriptor bootstrap, and state-backed base classes.
7. `api/blockentity`
   PiSerializeKit-state-backed block-entity persistence, client update tags, and menu data entry points.
8. `api/targeting`
   target query contracts and resolvers.
9. `api/entity`
   current entity lifecycle, spatial index, and living-hurt boundaries.
10. `api/projectile`
   the current projectile tracing contracts.
11. `api/menu`
   vanilla `ContainerData` bridges for menu int synchronization.
12. `api/jei`
   JEI-neutral recipe-viewer contracts, module bootstrap surfaces, and GUI / alias / transfer specs.
13. `api/recipe`
    recipe views, sources, matches, and reload-cache hooks shared by machines and recipe-viewer compat.
14. `api/math`
    core numeric, curve, weight, projection, and geometry helpers built around vanilla `Vec3` and `AABB`.
15. `api/render/tint`
    block/item model tint providers, Registrate tint methods, and color math helpers.

## Maven

If the downstream project uses the Pi template, add the Maven repository and dependency in `project.toml`:

```toml
[repositories]
mihono = "https://maven.mihono.cn/repository/pickaid1201/"

[dependencies.deobf_implementation]
pibrary = "com.mihono.pickaid:pibrary:0.0.8-dev"
```

If the downstream project still writes Gradle directly, use:

```gradle
repositories {
    maven { url = 'https://maven.mihono.cn/repository/pickaid1201/' }
}

dependencies {
    implementation fg.deobf("com.mihono.pickaid:pibrary:0.0.8-dev")
}
```
