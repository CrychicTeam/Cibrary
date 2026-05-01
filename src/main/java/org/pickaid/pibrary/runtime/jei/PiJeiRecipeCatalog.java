package org.pickaid.pibrary.runtime.jei;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import org.pickaid.pibrary.api.jei.PiJeiBootstrap;
import org.pickaid.pibrary.api.jei.PiJeiCategorySpec;
import org.pickaid.pibrary.api.jei.PiJeiClickArea;
import org.pickaid.pibrary.api.jei.PiJeiRecipeDisplay;
import org.pickaid.pibrary.api.jei.PiJeiRecipeSourceSpec;
import org.pickaid.pibrary.api.jei.PiJeiRecipeTypeKey;
import org.pickaid.pibrary.api.jei.PiJeiTransferSpec;
import org.pickaid.pibrary.api.recipe.PiRecipeView;

/**
 * Queryable recipe-source catalog built from JEI-neutral specs.
 *
 * <p>A concrete JEI plugin can collect modules with {@link PiJeiBootstrap}, then
 * use this catalog to feed recipes into the viewer without knowing how each
 * machine stores or caches its recipe data.</p>
 */
public final class PiJeiRecipeCatalog {
    private final Map<ResourceLocation, PiJeiCategorySpec<?>> categoriesByType;
    private final Map<ResourceLocation, List<PiJeiRecipeSourceSpec<?>>> sourcesByType;

    private PiJeiRecipeCatalog(
            Map<ResourceLocation, PiJeiCategorySpec<?>> categoriesByType,
            Map<ResourceLocation, List<PiJeiRecipeSourceSpec<?>>> sourcesByType
    ) {
        this.categoriesByType = Map.copyOf(categoriesByType);
        Map<ResourceLocation, List<PiJeiRecipeSourceSpec<?>>> copy = new LinkedHashMap<>();
        sourcesByType.forEach((id, sources) -> copy.put(id, List.copyOf(sources)));
        this.sourcesByType = Map.copyOf(copy);
    }

    public static PiJeiRecipeCatalog from(PiJeiBootstrap bootstrap) {
        Objects.requireNonNull(bootstrap, "bootstrap");
        Map<ResourceLocation, PiJeiCategorySpec<?>> categoriesByType = new LinkedHashMap<>();
        for (PiJeiCategorySpec<?> category : bootstrap.categories()) {
            PiJeiCategorySpec<?> previous = categoriesByType.putIfAbsent(category.recipeType().id(), category);
            if (previous != null && previous.recipeType().recipeClass() != category.recipeType().recipeClass()) {
                throw new IllegalArgumentException("recipe category " + category.recipeType().id()
                        + " is registered for both "
                        + previous.recipeType().recipeClass().getName()
                        + " and " + category.recipeType().recipeClass().getName());
            }
        }
        Map<ResourceLocation, List<PiJeiRecipeSourceSpec<?>>> sourcesByType = new LinkedHashMap<>();
        for (PiJeiRecipeSourceSpec<?> source : bootstrap.recipeSources()) {
            requireCategory(categoriesByType, source.recipeType(), "recipe source");
            sourcesByType.computeIfAbsent(source.recipeType().id(), ignored -> new ArrayList<>()).add(source);
        }
        for (PiJeiClickArea area : bootstrap.clickAreas()) {
            for (PiJeiRecipeTypeKey<?> type : area.recipeTypes()) {
                requireCategory(categoriesByType, type, "recipe click area");
            }
        }
        for (PiJeiTransferSpec<?, ?> transfer : bootstrap.transfers()) {
            requireCategory(categoriesByType, transfer.recipeType(), "recipe transfer");
        }
        return new PiJeiRecipeCatalog(categoriesByType, sourcesByType);
    }

    public List<PiJeiCategorySpec<?>> categories() {
        return List.copyOf(categoriesByType.values());
    }

    public boolean hasCategory(PiJeiRecipeTypeKey<?> type) {
        Objects.requireNonNull(type, "type");
        return categoriesByType.containsKey(type.id());
    }

    public <R> List<PiRecipeView<R>> views(PiJeiRecipeTypeKey<R> type, Level level) {
        Objects.requireNonNull(type, "type");
        List<PiRecipeView<R>> views = new ArrayList<>();
        for (PiJeiRecipeSourceSpec<?> source : sourcesByType.getOrDefault(type.id(), List.of())) {
            views.addAll(typedSource(type, source).source().views(level));
        }
        return List.copyOf(views);
    }

    public <R> List<R> recipes(PiJeiRecipeTypeKey<R> type, Level level) {
        List<R> recipes = new ArrayList<>();
        for (PiRecipeView<R> view : views(type, level)) {
            recipes.add(view.recipe());
        }
        return List.copyOf(recipes);
    }

    public <R> List<PiJeiRecipeDisplay<R>> displays(PiJeiRecipeTypeKey<R> type, Level level) {
        PiJeiCategorySpec<R> category = typedCategory(type);
        List<PiJeiRecipeDisplay<R>> displays = new ArrayList<>();
        for (PiRecipeView<R> view : views(type, level)) {
            displays.add(new PiJeiRecipeDisplay<>(category, view));
        }
        return List.copyOf(displays);
    }

    public boolean hasSource(PiJeiRecipeTypeKey<?> type) {
        Objects.requireNonNull(type, "type");
        return sourcesByType.containsKey(type.id());
    }

    @SuppressWarnings("unchecked")
    private static <R> PiJeiRecipeSourceSpec<R> typedSource(
            PiJeiRecipeTypeKey<R> requestedType,
            PiJeiRecipeSourceSpec<?> source
    ) {
        Class<?> sourceClass = source.recipeType().recipeClass();
        if (!requestedType.recipeClass().isAssignableFrom(sourceClass)) {
            throw new IllegalArgumentException("recipe source " + source.recipeType().id()
                    + " uses " + sourceClass.getName()
                    + " and cannot be read as " + requestedType.recipeClass().getName());
        }
        return (PiJeiRecipeSourceSpec<R>) source;
    }

    @SuppressWarnings("unchecked")
    private <R> PiJeiCategorySpec<R> typedCategory(PiJeiRecipeTypeKey<R> requestedType) {
        PiJeiCategorySpec<?> category = categoriesByType.get(requestedType.id());
        if (category == null) {
            throw new IllegalArgumentException("recipe category " + requestedType.id() + " is not registered");
        }
        Class<?> categoryClass = category.recipeType().recipeClass();
        if (!requestedType.recipeClass().isAssignableFrom(categoryClass)) {
            throw new IllegalArgumentException("recipe category " + requestedType.id()
                    + " uses " + categoryClass.getName()
                    + " and cannot be read as " + requestedType.recipeClass().getName());
        }
        return (PiJeiCategorySpec<R>) category;
    }

    private static void requireCategory(
            Map<ResourceLocation, PiJeiCategorySpec<?>> categoriesByType,
            PiJeiRecipeTypeKey<?> type,
            String owner
    ) {
        if (!categoriesByType.containsKey(type.id())) {
            throw new IllegalArgumentException(owner + " " + type.id() + " has no registered category");
        }
    }
}
