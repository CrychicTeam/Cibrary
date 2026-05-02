# JEI 集成结构

Pibrary 主 API 不直接导入 JEI。项目里保持这个拆分：

```text
src/main/java/com/example/init/ExampleRecipeViewerModule.java
src/main/java/com/example/compat/jei/ExampleJeiPlugin.java
src/main/java/com/example/compat/jei/ExampleJeiCategory.java
```

`ExampleRecipeViewerModule` 只使用 Pibrary 类。`ExampleJeiPlugin` 和
`ExampleJeiCategory` 才导入 `mezz.jei.*`。

## project.toml

下游项目要发布 JEI 插件时，启用模板里的 JEI feature：

```toml
[features]
jei = true
```

模板会把 JEI common / Forge API 加到 compile-only classpath，并把 JEI Forge jar
加到本地 runtime。Pibrary 自己应保持关闭。

## Pibrary 侧

游戏逻辑暴露 category、source 和 layout，不出现 JEI 类：

```java
public final class ExampleRecipeViewerModule implements PiJeiModule {
    public static final PiJeiRecipeTypeKey<CounterRecipe> COUNTER_CHARGING =
            new PiJeiRecipeTypeKey<>(id("counter_charging"), CounterRecipe.class);

    public static final PiJeiCategorySpec<CounterRecipe> CATEGORY =
            new PiJeiCategorySpec<>(
                    COUNTER_CHARGING,
                    Component.literal("Counter Charging"),
                    116,
                    54,
                    0,
                    view -> PiRecipeLayout.builder(view)
                            .input(18, 18, view.recipe().input())
                            .thenOutput(64, view.recipe().output())
                            .display(52, 18, view.recipe().ticks())
                            .build());

    public static final PiJeiRecipeSourceSpec<CounterRecipe> SOURCE =
            new PiJeiRecipeSourceSpec<>(COUNTER_CHARGING, CounterRecipes.SOURCE);

    @Override
    public void contribute(PiJeiBridge bridge) {
        bridge.registerCategory(CATEGORY);
        bridge.registerRecipeSource(SOURCE);
    }
}
```

重点是机器逻辑、recipe cache 和 JEI 显示都指向同一个 `PiRecipeSource`。

## JEI Plugin

JEI plugin 应该很薄。它收集 Pibrary spec，构建 catalog，然后注册 JEI category 和 display object。

```java
@JeiPlugin
public final class ExampleJeiPlugin implements IModPlugin {
    private static final ResourceLocation UID = id("jei");

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static final RecipeType<PiJeiRecipeDisplay> COUNTER_CHARGING_TYPE =
            new RecipeType(ExampleRecipeViewerModule.COUNTER_CHARGING.id(), PiJeiRecipeDisplay.class);

    private final PiJeiBootstrap bootstrap = new PiJeiBootstrap();
    private final PiJeiRecipeCatalog catalog;

    public ExampleJeiPlugin() {
        bootstrap.registerModule(new ExampleRecipeViewerModule());
        catalog = PiJeiRecipeCatalog.from(bootstrap);
    }

    @Override
    public ResourceLocation getPluginUid() {
        return UID;
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(new ExampleJeiCategory<>(
                COUNTER_CHARGING_TYPE,
                ExampleRecipeViewerModule.CATEGORY));
    }

    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    public void registerRecipes(IRecipeRegistration registration) {
        Level level = Minecraft.getInstance().level;
        List<PiJeiRecipeDisplay> displays =
                (List) catalog.displays(ExampleRecipeViewerModule.COUNTER_CHARGING, level);
        registration.addRecipes(COUNTER_CHARGING_TYPE, displays);
    }
}
```

多个 category 时，维护一张小表，把每个 `PiJeiRecipeTypeKey<?>` 对应到 JEI 的
`RecipeType<PiJeiRecipeDisplay>`，然后循环注册。不要把 recipe 查询逻辑复制到 JEI plugin 里。

## JEI Category Adapter

adapter 负责把 `PiRecipeSlot` 翻译成 JEI slot：

```java
public final class ExampleJeiCategory<R> implements IRecipeCategory<PiJeiRecipeDisplay<R>> {
    private final RecipeType<PiJeiRecipeDisplay<R>> recipeType;
    private final PiJeiCategorySpec<R> spec;

    public ExampleJeiCategory(
            RecipeType<PiJeiRecipeDisplay<R>> recipeType,
            PiJeiCategorySpec<R> spec
    ) {
        this.recipeType = recipeType;
        this.spec = spec;
    }

    @Override
    public RecipeType<PiJeiRecipeDisplay<R>> getRecipeType() {
        return recipeType;
    }

    @Override
    public Component getTitle() {
        return spec.title();
    }

    @Override
    public int getWidth() {
        return spec.width();
    }

    @Override
    public int getHeight() {
        return spec.height();
    }

    @Override
    public IDrawable getIcon() {
        return null;
    }

    @Override
    public void setRecipe(
            IRecipeLayoutBuilder builder,
            PiJeiRecipeDisplay<R> display,
            IFocusGroup focuses
    ) {
        Map<String, List<IIngredientAcceptor<?>>> focusGroups = new LinkedHashMap<>();
        for (PiRecipeSlot<?> slot : display.layout().slots()) {
            if (slot.visibility() == PiRecipeSlot.Visibility.RENDER_ONLY) {
                continue;
            }
            if (slot.visibility() == PiRecipeSlot.Visibility.HIDDEN_LOOKUP) {
                addIngredients(builder.addInvisibleIngredients(role(slot.role())), slot.values());
                continue;
            }

            IRecipeSlotBuilder jeiSlot = builder.addSlot(role(slot.role()), slot.x(), slot.y());
            slot.name().ifPresent(jeiSlot::setSlotName);
            switch (slot.background()) {
                case STANDARD -> jeiSlot.setStandardSlotBackground();
                case OUTPUT -> jeiSlot.setOutputSlotBackground();
                case NONE -> {
                }
            }
            slot.fluidRenderer().ifPresent(hint -> jeiSlot.setFluidRenderer(
                    hint.capacity(),
                    hint.showCapacity(),
                    hint.width(),
                    hint.height()));
            addIngredients(jeiSlot, slot.values());
            slot.tooltips().forEach(line -> jeiSlot.addRichTooltipCallback((recipeSlotView, tooltip) ->
                    tooltip.add(line)));
            slot.focusGroup().ifPresent(group ->
                    focusGroups.computeIfAbsent(group, ignored -> new ArrayList<>()).add(jeiSlot));
        }
        focusGroups.values().forEach(group ->
                builder.createFocusLink(group.toArray(IIngredientAcceptor[]::new)));
    }
}
```

项目自己的 display value 仍然可以放在 `PiRecipeSlot.values()` 里。只需要在
`addIngredient(...)` 里教 adapter 怎么把它转成 JEI ingredient。

## 复杂配方

复杂配方优先用 slot hint，不要急着做更大的 layout engine：

```java
view -> PiRecipeLayout.builder(view)
        .slot(PiRecipeSlot.builder(PiRecipeRole.INPUT, 18, 18)
                .name("alloy_input")
                .standardBackground()
                .values(view.recipe().acceptedInputs())
                .focusGroup("alloy")
                .tooltip(PiTexts.literal("Any matching alloy input"))
                .build())
        .slot(PiRecipeSlot.builder(PiRecipeRole.OUTPUT, 82, 18)
                .name("alloy_output")
                .outputBackground()
                .values(view.recipe().possibleOutputs())
                .focusGroup("alloy")
                .build())
        .slot(PiRecipeSlot.builder(PiRecipeRole.INPUT, 42, 18)
                .name("fluid_input")
                .fluidRenderer(1000, true, 16, 48)
                .value(view.recipe().fluidInput())
                .build())
        .slot(PiRecipeSlot.builder(PiRecipeRole.INPUT, 0, 0)
                .values(view.recipe().lookupOnlyInputs())
                .hiddenLookup()
                .build())
        .build()
```

这能覆盖命名 slot、流体槽、原版 slot 背景、轮换 ingredient、隐藏查询 ingredient、
输入输出联动和额外 tooltip。JEI-only 的 drawable 代码仍然留在 JEI adapter。

## 其他 JEI Hook

同一个 plugin 可以继续翻译 `PiJeiBootstrap` 里的 catalyst、click area 和 transfer：

```java
@Override
public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
    for (PiJeiCatalystSpec catalyst : bootstrap.catalysts()) {
        RecipeType<?> type = jeiType(catalyst.recipeType());
        for (ItemStack stack : catalyst.itemStacks()) {
            registration.addRecipeCatalyst(stack, type);
        }
    }
}

@Override
public void registerGuiHandlers(IGuiHandlerRegistration registration) {
    for (PiJeiClickArea area : bootstrap.clickAreas()) {
        registration.addRecipeClickArea(
                screenClass(area.screenClassName()),
                area.x(),
                area.y(),
                area.width(),
                area.height(),
                area.recipeTypes().stream().map(this::jeiType).toArray(RecipeType[]::new));
    }
}

@Override
public void registerRecipeTransferHandlers(IRecipeTransferRegistration registration) {
    for (PiJeiTransferSpec<?, ?> transfer : bootstrap.transfers()) {
        registration.addRecipeTransferHandler(
                transfer.menuClass(),
                transfer.menuTypeSupplier().get(),
                jeiType(transfer.recipeType()),
                transfer.recipeSlotStart(),
                transfer.recipeSlotCount(),
                transfer.inventorySlotStart(),
                transfer.inventorySlotCount());
    }
}
```

adapter 管 JEI class loading 和 JEI-specific 转换。Pibrary 管稳定的 recipe 数据、layout、校验和查询路径。
