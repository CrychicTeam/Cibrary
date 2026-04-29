# Pibrary

[English](README_EN.md)

`Pibrary` 是 PickAID 系列的根基础模组。它放最常用、最稳定、每个下游项目都会反复需要的那部分能力：状态挂载、注册辅助、实体与投掷物工具、数学工具、配置入口、诊断入口和 recipe-viewer 中立契约。

当前阶段的目标很明确：

1. 下游模组能直接依赖 `pibrary` 开始写生产代码；
2. 常用逻辑尽量变成短、稳、可迁移的 API；
3. 1.20.1 到后续版本迁移时，项目代码尽量只碰少量兼容层；
4. PiNet 和 PiSerializeKit 这两个底层能力在 Pibrary 里形成好用的上层入口。

## 当前定位

`Pibrary` 现在负责以下长期核心能力：

1. 基础依赖定位与低层共享注册表；
2. 配置、诊断、状态、目标选择、注册、数学等通用契约；
3. 实体生命周期、实体选择与高性能 projectile trace 的公共边界；
4. 面向可选 recipe-viewer compat 的中立模块 / bootstrap / spec 契约；
5. 作为 Pi 系列公共依赖的最小稳定根；
6. 为独立基础库和 engine 模组提供共同语言。

这份 README 只写当前已经能直接使用的核心 API。更重的 runtime、兼容实现和玩法逻辑可以围绕这些契约扩展，不需要先塞回根模组。

## 现在怎么用

如果你要给玩家或生物挂一份持久化状态，写一个 Facet。Facet 会被挂到实体上，可以保存、同步，也可以提供 tick、clone、显示刷新等钩子。

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

如果状态属于某个 `ServerLevel`，写 Level Facet。它默认负责持久化；要不要同步由更高层自己决定，避免 level 级状态被默认广播成网络负担。

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

如果状态属于已经加载的 chunk，写 Chunk Facet。它走 chunk capability 持久化，不会主动加载或生成 chunk；用 `findLoaded(...)` 查询时，chunk 没加载就返回空。

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

你的模组初始化时调用一次 `ExampleLivingFacets.register()` / `ExampleLevelFacets.register()` / `ExampleChunkFacets.register()`，让这些静态 handle 被加载。之后业务代码只拿 typed handle，不手写 capability key，也不手动找 generated descriptor。

配置先写成 `PiConfigSpec`。这不是文件格式，也不是强行替代 Forge config；它只是把 id、作用域、默认值、注释和校验放在一个稳定位置。之后无论你用 Forge config、datapack JSON、客户端本地文件，还是 Pibrary 以后自己的 loader，都能吃同一份定义。

```java
public final class ExampleConfigs {
    public static final PiConfigEntry<Integer> MAX_ENERGY = PiConfigEntry
            .builder(id("gameplay/max_energy"), Codec.INT, PiConfigScope.COMMON_BOOTSTRAP, 100)
            .comment("Maximum energy stored by the counter system.")
            .alsoValidate(PiConfigValidators.intRange(1, 10_000))
            .build();

    public static final PiConfigEntry<String> STARTER_SPELL = PiConfigEntry
            .builder(id("gameplay/starter_spell"), Codec.STRING, PiConfigScope.COMMON_BOOTSTRAP, "example:fireball")
            .comment("Spell id given to a new player.")
            .alsoValidate(PiConfigValidators.notBlank("starter spell must not be blank"))
            .alsoValidate(value -> value.contains(":")
                    ? Optional.empty()
                    : Optional.of("starter spell must include namespace"))
            .build();

    public static final PiConfigSpec GAMEPLAY = PiConfigSpec
            .builder(REGISTRATE.loc("gameplay"), PiConfigScope.COMMON_BOOTSTRAP)
            .entry(MAX_ENERGY)
            .entry(STARTER_SPELL)
            .build();

    private ExampleConfigs() {
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath("example", path);
    }
}
```

`PiConfigSpec` 会在构建时检查重复 entry、作用域混用和非法默认值。这样配置写错会在启动阶段尽早暴露，不会等到某个机器、facet 或 packet 真用到时才变成奇怪的运行时问题。

loader 读完文件后可以放进 `PiConfigValues`，业务代码继续用 typed entry 取值：

```java
PiConfigValues values = PiConfigValues.builder(ExampleConfigs.GAMEPLAY)
        .set(ExampleConfigs.MAX_ENERGY, 250)
        .build();

int maxEnergy = values.get(ExampleConfigs.MAX_ENERGY);
```

方块、物品、方块实体、模型、loot、recipe 继续按 Registrate 的写法来。`tag(...)`、`properties(...)`、`simpleItem()`、`defaultLoot()`、`defaultModel()` 这些 Registrate 已经有的能力，直接写通常更清楚。Pibrary 在这条链上主要补 id 路径、创造栏 section / NBT 变体、tint 声明，以及少数确实能减少重复的组合操作。

如果你的项目只是正常注册方块和物品，直接用 `PiRegistrate`：

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
                // 这里可以接自己的全局缓存、运行时索引或调试统计。
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
                // 这不是 block/item，也能使用 Registrate 自带的注册回调。
            })
            .register();

    private ExampleEntries() {
    }

    public static void register() {
    }
}
```

如果要整理创造栏，先注册一个 tab，再在普通 `item(...)` / `block(...)` 链上写 section。`section` 是 Pibrary 自己保存的分组顺序，不是原版多出来的新概念；Forge 需要填充创造栏时，Pibrary 会按这个顺序把内容写进去。

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

`variant(...)` 适合单个 NBT 变体；`variants(...)` 适合从列表或运行时数据源展开一批变体，例如同一个 scroll 对应多种 spell、同一个工具对应多个充能等级。传 `List` 时内容会马上固定；传 `Supplier<Iterable<...>>` 时会等创造栏刷新时再读取，适合依赖配置或自定义 registry 的内容。

上面的 `STARTER_SPELLS` 只是普通列表：

```java
private static final List<String> STARTER_SPELLS =
        List.of("fireball", "frostbolt", "blink");
```

如果 spell 本身就是注册项，不需要再绕 `PiRegistryPlan`。把你项目里真实存在的 registry 句柄传进来，在创造栏刷新时读取它：

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

这里的 `SpellType`、`enabled()` 和 registry 变量都属于你的项目，不是 Pibrary 规定的 API。Pibrary 只负责把 `Supplier` 给出的真实数据源展开成创造栏 stack。

`searchOnly()` 让内容只出现在搜索页，`parentOnly()` 只放在当前 tab，`hidden()` 则完全不显示。需要把内容放进别的 tab 时，用 `section(tabKey, "section_name")`。

`section("materials")` 使用默认创造栏；默认创造栏可以在同一个注册类里稍后声明，Pibrary 会等到注册事件真正处理内容时再解析它。多个 tab 混用时不要依赖默认值，直接写 `section(tabKey, "materials")`。

如果你的项目有自己的核心概念，例如 spell、trait、machine、module，不要把这些东西塞进 Pibrary 的通用 helper。直接做一个自己的 Registrate 入口，把项目里的词写成方法：

```java
public final class ExampleRegistrate extends PiBaseRegistrate<ExampleRegistrate> {
    private static final ResourceKey<Registry<SpellType>> SPELLS =
            ResourceKey.createRegistryKey(ResourceLocation.fromNamespaceAndPath("example", "spells"));

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

这条规则很简单：Pibrary 提供通用薄工具，项目自己的词留在项目自己的 `Registrate` 子类里。

这里有四层，不要混在一起：

1. Registrate 原生链负责注册主流程：`properties(...)`、`tag(...)`、`item()`、`model(...)`、`loot(...)`、`onRegister(...)` 能直接表达清楚时就直接用。
2. Pibrary 的 builder 方法负责少数 Registrate 不够顺手的地方，例如 `section(...)`、`variants(...)`、`blockTags(...)`、`itemTags(...)`、`blockAndItemTag(...)`。
3. `REGISTRATE.tintFoliage(...)` / `tintBlock(...)` / `tintItem(...)` 是客户端 tint 层，常用颜色直接用短方法，复杂颜色再交给 builder。
4. 复杂 blockstate、复杂 loot、特殊 renderer、复杂 block entity 继续直接写 Registrate 原生链，避免包装成“看起来统一但实际很别扭”的假架构。

如果模型 JSON 里用了 `tintindex`，可以用 `api/render/tint` 提供的颜色 helper。声明放在同一个 `REGISTRATE` 下面，Pibrary 会在客户端颜色事件里统一消化：

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

普通用法应该像上面这样短。更复杂的染色才进入 builder：

```java
REGISTRATE
        .tintBlock(RELAY_CORE, tint -> tint
                .layer(0).constant(0x44AAFF)
                .layer(1).foliage())
        .tintDurability(COPPER_WAND, 1, 0xAA2222, 0x22AAFF)
        .tintItem(SPELL_SCROLL, tint -> tint
                .layer(0).stack(SpellScrollItem::spellColor));
```

`BlockEntity` 颜色读取只适合少数动态方块，例如一个模型仍然是普通 baked model，但颜色来自方块实体里的运行时数据。需要时可以放在 builder 里：

```java
REGISTRATE.tintBlock(MANA_POOL, tint -> tint
        .layer(0).blockEntity(ManaPoolBlockEntity.class, ManaPoolBlockEntity::color, 0xFFFFFF));
```

常用颜色来源已经有短方法：固定色、指定 layer、方块状态、方块实体、草/叶/水 biome tint、物品 stack、耐久渐变、block item 继承方块 tint。颜色计算放在 `PiColors`，例如 `mix`、`multiply`、`hsv`、`pulse`。

普通注册不要先想 `PiRegistryPlan`。绝大多数 block、item、sound、particle、自定义 registry entry 都应该直接走 `REGISTRATE.block(...)`、`item(...)` 或 `generic(...)`。

`api/registry` 只保留给少数高级场景：多个模块先收集一份简单 registry 清单、提前查重复 id，或者同一份清单要被不同版本的 Forge / NeoForge adapter 消化。它不是主入口，也不是复杂方块、物品、实体的工厂。

如果机器逻辑和 recipe viewer 都要读取同一批数据，先把数据整理成 `PiRecipeView`。机器用 `PiRecipeLookupCache` 查找和缓存，viewer compat 用同一个 `PiRecipeSource` 生成显示内容。

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

方块实体里不用每 tick 扫一遍全部 recipe。保存一个 cache，输入改变或 recipe reload 时清掉它：

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

如果这个方块实体有菜单，`PiMenuData` 可以直接接原版 `addDataSlots(...)`。服务端读 getter，客户端收到数据后跑 setter；如果方块实体实现了 `PiPresentationSource`，screen projection 会按 `refreshOnMenuData(...)` 自动失效。

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

同一份 source 也可以交给 recipe-viewer compat。`api/jei` 这里仍然不导入 JEI 类；真正的插件代码以后只需要把这些中立 spec 转成对应版本的 viewer API。

```java
PiJeiRecipeTypeKey<CounterRecipes.CounterRecipe> type =
        new PiJeiRecipeTypeKey<>(
                PiIds.id("example", "counter_charging"),
                CounterRecipes.CounterRecipe.class);

PiJeiCategorySpec<CounterRecipes.CounterRecipe> category =
        new PiJeiCategorySpec<>(type, Component.literal("Counter Charging"), 116, 54, 0);

PiJeiRecipeSourceSpec<CounterRecipes.CounterRecipe> source =
        new PiJeiRecipeSourceSpec<>(type, CounterRecipes.SOURCE);
```

如果你需要通用数学 helper，直接使用 `api/math`。这部分沿用原版 `Vec3` / `AABB`，所以可以和实体、碰撞、渲染、targeting 代码直接配合：

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

这类 helper 适合 targeting、projectile、HUD marker、world preview、简单动画曲线和权重选择。复杂视觉系统可以把这些函数作为底层积木继续组合。

## 现阶段代码状态

这个仓库现在按职责拆开：

1. `src/main/java/org/pickaid/pibrary/api/**`
   给下游直接使用的稳定入口。
2. `src/main/java/org/pickaid/pibrary/runtime/**`
   Forge 1.20.1 下的落地实现，例如 capability、sync、creative tab、recipe cache 和 projectile trace。
3. `src/main/java/org/pickaid/pibrary/mixin/**`
   只放确实需要碰原版内部结构的访问点。
4. `src/devExample/java/**`
   编译参与测试，但不会进入生产 jar 的示例代码。

历史单体快照保留在 `restore/legacy-monolith-20260331`，只用于回看旧实现和迁移灵感，不作为当前 API 文档。

## 可用 API

当前已经开始明确这些基础面：

1. `api/core`
   底层 key 与共享注册表，用于少数需要全局替换实现的基础能力。
2. `api/config`
   配置条目、分组 spec、作用域、注释与默认值校验。
3. `api/diagnostics`
   调试/诊断输出统一入口。
4. `api/registry`
   少数高级场景使用的简单 registry request / plan / adapter 边界，不作为常规注册入口。
5. `api/state`
   通用状态 key 与同步策略。
6. `api/targeting`
   目标选择查询与 resolver 契约。
7. `api/facet`
   living / level / chunk facet 注解、typed handle、descriptor bootstrap 和状态基类。
8. `api/blockentity`
   基于 PiSerializeKit state 的方块实体持久化、客户端 update tag 和菜单数据入口。
9. `api/entity`
   实体生命周期与受击处理边界。
10. `api/projectile`
   面向性能优化的 projectile trace / impact 契约。
11. `api/menu`
   基于原版 `ContainerData` 的菜单 int 同步桥。
12. `api/jei`
   JEI 中立的 recipe-viewer 契约、模块收集面与 GUI / alias / transfer 规格。
13. `api/recipe`
    机器逻辑和 recipe-viewer compat 共用的 recipe view、source、match 和 reload cache 契约。
14. `api/math`
    基于原版 `Vec3` / `AABB` 的核心数学、曲线、权重、视口投影和几何 helper。
15. `api/render/tint`
    方块/物品模型 tint 的颜色 provider、Registrate tint 方法和颜色计算 helper。

## Maven

如果项目使用 Pi 系列模板，在 `project.toml` 里加 Maven 仓库和依赖即可：

```toml
[repositories]
mihono = "https://maven.mihono.cn/repository/pickaid1201/"

[dependencies.deobf_implementation]
pibrary = "com.mihono.pickaid:pibrary:0.0.7-dev"
```

如果项目还在直接写 Gradle，可以这样导入：

```gradle
repositories {
    maven { url = 'https://maven.mihono.cn/repository/pickaid1201/' }
}

dependencies {
    implementation fg.deobf("com.mihono.pickaid:pibrary:0.0.7-dev")
}
```
