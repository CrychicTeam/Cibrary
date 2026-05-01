package org.pickaid.pibrary.api.jei;

import net.minecraft.world.inventory.AbstractContainerMenu;

/**
 * Neutral recipe-viewer bridge that can be implemented by JEI or another compat runtime.
 */
public interface PiJeiBridge {
    /**
     * Registers one category spec.
     *
     * @param category category spec
     * @param <R> recipe type
     */
    <R> void registerCategory(PiJeiCategorySpec<R> category);

    /**
     * Registers one recipe source for a viewer category.
     *
     * @param source recipe source spec
     * @param <R> recipe type
     */
    <R> void registerRecipeSource(PiJeiRecipeSourceSpec<R> source);

    /**
     * Registers one catalyst spec.
     *
     * @param catalyst catalyst spec
     */
    void registerCatalyst(PiJeiCatalystSpec catalyst);

    /**
     * Registers one clickable recipe area.
     *
     * @param area click area spec
     */
    void registerClickArea(PiJeiClickArea area);

    /**
     * Registers one extra GUI exclusion area.
     *
     * @param area extra area spec
     */
    void registerExtraArea(PiJeiExtraArea area);

    /**
     * Registers one transfer spec.
     *
     * @param transfer transfer spec
     * @param <C> menu type
     * @param <R> recipe type
     */
    <C extends AbstractContainerMenu, R> void registerTransfer(PiJeiTransferSpec<C, R> transfer);

    /**
     * Registers one alias spec.
     *
     * @param alias alias spec
     */
    void registerAlias(PiJeiAliasSpec alias);

    /**
     * Convenience helper that lets one module contribute all of its specs.
     *
     * @param module JEI-neutral module
     */
    default void registerModule(PiJeiModule module) {
        module.contribute(this);
    }
}
