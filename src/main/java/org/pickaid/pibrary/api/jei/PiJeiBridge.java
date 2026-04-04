package org.pickaid.pibrary.api.jei;

import net.minecraft.world.inventory.AbstractContainerMenu;

public interface PiJeiBridge {
    <R> void registerCategory(PiJeiCategorySpec<R> category);

    void registerCatalyst(PiJeiCatalystSpec catalyst);

    void registerClickArea(PiJeiClickArea area);

    void registerExtraArea(PiJeiExtraArea area);

    <C extends AbstractContainerMenu, R> void registerTransfer(PiJeiTransferSpec<C, R> transfer);

    void registerAlias(PiJeiAliasSpec alias);

    default void registerModule(PiJeiModule module) {
        module.contribute(this);
    }
}
