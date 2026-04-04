package org.pickaid.pibrary.api.jei;

import java.util.Objects;
import java.util.function.Supplier;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;

public record PiJeiTransferSpec<C extends AbstractContainerMenu, R>(
        Class<? extends C> menuClass,
        Supplier<? extends MenuType<C>> menuTypeSupplier,
        PiJeiRecipeTypeKey<R> recipeType,
        int recipeSlotStart,
        int recipeSlotCount,
        int inventorySlotStart,
        int inventorySlotCount
) {
    public PiJeiTransferSpec {
        Objects.requireNonNull(menuClass, "menuClass");
        Objects.requireNonNull(menuTypeSupplier, "menuTypeSupplier");
        Objects.requireNonNull(recipeType, "recipeType");
        if (recipeSlotStart < 0 || inventorySlotStart < 0) {
            throw new IllegalArgumentException("slot starts must be >= 0");
        }
        if (recipeSlotCount < 1 || inventorySlotCount < 1) {
            throw new IllegalArgumentException("slot counts must be >= 1");
        }
    }
}
