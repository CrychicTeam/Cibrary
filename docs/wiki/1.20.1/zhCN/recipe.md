# Recipe Runtime

Pibrary 的 recipe 层不是替代原版 recipe，也不是 JEI layout engine。它做一件更小的事：让机器逻辑和 recipe-viewer compat 使用同一份整理后的 recipe view。

```java
public record SpellInfusionRecipe(String input, String output, int manaCost) {
}

public final class SpellInfusionRecipes {
    public static final PiRecipeSource<SpellInfusionRecipe> SOURCE =
            level -> level.getRecipeManager().getAllRecipesFor(SPELL_INFUSION_TYPE.get())
                    .stream()
                    .map(SpellInfusionRecipes::view)
                    .toList();

    public static PiRecipeView<SpellInfusionRecipe> view(SpellInfusionRecipe recipe) {
        return new PiRecipeView<>(
                new ResourceLocation("example", "spell_infusion/" + recipe.output()),
                recipe,
                List.of(
                        new PiRecipeIngredient<>(PiRecipeRole.INPUT, recipe.input()),
                        new PiRecipeIngredient<>(PiRecipeRole.OUTPUT, recipe.output()),
                        new PiRecipeIngredient<>(PiRecipeRole.DISPLAY, recipe.manaCost())
                ));
    }
}
```

机器里不要每 tick 扫完整 recipe 列表。保存一个 cache，输入变化或 reload 后清掉：

```java
public final class SpellInfuserBlockEntity extends BlockEntity {
    private final PiRecipeLookupCache<SpellInfusionRecipe> recipes =
            new PiRecipeLookupCache<>(new PiRecipeLookup<>(SpellInfusionRecipes.SOURCE));

    public Optional<PiRecipeMatch<SpellInfusionRecipe>> findRecipe(Level level, String input) {
        return recipes.find(level, view -> view.recipe().input().equals(input));
    }

    public void onInventoryChanged() {
        recipes.clear();
    }
}
```

JEI 或其他 recipe viewer 只读取同一份 `PiRecipeView`，不要再单独整理一套显示数据。
