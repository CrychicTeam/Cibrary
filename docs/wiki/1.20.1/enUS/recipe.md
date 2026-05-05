# Recipe Runtime

Pibrary's recipe layer does not replace vanilla recipes and does not try to become a layout engine. It does one smaller job: machine logic and recipe-viewer compat can consume the same normalized recipe view.

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

A machine should not scan all recipes every tick. Keep a cache and clear it when inputs or recipe data change:

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

JEI or another recipe viewer can read the same `PiRecipeView` instead of building a second display-only data model.
