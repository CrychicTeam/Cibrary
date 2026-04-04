package org.pickaid.pibrary.api.jei;

import java.util.List;
import java.util.Objects;
import net.minecraft.world.item.ItemStack;

public record PiJeiCatalystSpec(
        PiJeiRecipeTypeKey<?> recipeType,
        List<ItemStack> itemStacks
) {
    public PiJeiCatalystSpec {
        Objects.requireNonNull(recipeType, "recipeType");
        Objects.requireNonNull(itemStacks, "itemStacks");
        itemStacks = itemStacks.stream().map(ItemStack::copy).toList();
        if (itemStacks.isEmpty()) {
            throw new IllegalArgumentException("itemStacks must not be empty");
        }
    }
}
