# JEI Integration Shape

Pibrary does not import JEI in its main API. Keep the normal code split:

```text
src/main/java/com/example/init/ExampleRecipeViewerModule.java
src/main/java/com/example/compat/jei/ExampleJeiPlugin.java
src/main/java/com/example/compat/jei/ExampleJeiCategory.java
```

`ExampleRecipeViewerModule` uses Pibrary classes only. `ExampleJeiPlugin` and
`ExampleJeiCategory` are the only classes that import `mezz.jei.*`.

## project.toml

For a downstream mod that ships a JEI plugin, enable the curated JEI feature:

```toml
[features]
jei = true
```

The template adds the JEI common/Forge API as compile-only dependencies and the
JEI Forge jar as a local runtime dependency. Pibrary itself should keep this off.

## Pibrary Side

The game code exposes category, source, and layout without JEI classes:

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

The useful part is that machine logic, recipe cache, and JEI all point at the
same `PiRecipeSource`.

## JEI Plugin

The JEI plugin is thin. It collects Pibrary specs, builds a catalog, then
registers JEI categories and display objects.

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

For multiple categories, keep a small table that pairs each
`PiJeiRecipeTypeKey<?>` with its JEI `RecipeType<PiJeiRecipeDisplay>`, then loop
over the table. Do not duplicate recipe lookup code inside the JEI plugin.

## JEI Category Adapter

This class translates `PiRecipeSlot` into JEI slots:

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

    private static RecipeIngredientRole role(PiRecipeRole role) {
        return switch (role) {
            case INPUT, FUEL -> RecipeIngredientRole.INPUT;
            case OUTPUT -> RecipeIngredientRole.OUTPUT;
            case CATALYST -> RecipeIngredientRole.CATALYST;
            case DISPLAY -> RecipeIngredientRole.RENDER_ONLY;
        };
    }

    private static void addIngredients(IIngredientAcceptor<?> slot, List<?> values) {
        for (Object value : values) {
            addIngredient(slot, value);
        }
    }

    private static void addIngredient(IIngredientAcceptor<?> slot, Object value) {
        if (value instanceof ItemStack stack) {
            slot.addItemStack(stack);
        } else if (value instanceof ItemLike item) {
            slot.addItemLike(item);
        } else if (value instanceof Ingredient ingredient) {
            slot.addIngredients(ingredient);
        } else {
            throw new IllegalArgumentException("Unsupported JEI ingredient value: " + value.getClass().getName());
        }
    }
}
```

Project-specific display values are still possible. Keep them in
`PiRecipeSlot.values()`, then teach `addIngredient(...)` how to translate that
type for JEI.

For complex recipes, prefer slot hints over a bigger abstraction:

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

That covers named slots for draw-time lookup, fluid tanks, vanilla slot
backgrounds, rotating ingredients, hidden lookup ingredients, linked input/output
variants, and extra tooltips while keeping JEI-only drawable code in the JEI
adapter.

## Other JEI Hooks

The same plugin can translate the rest of `PiJeiBootstrap`:

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

The adapter owns class loading and JEI-specific conversions. Pibrary owns the
stable recipe data, layout, validation, and lookup path.
