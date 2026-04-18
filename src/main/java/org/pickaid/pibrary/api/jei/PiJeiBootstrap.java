package org.pickaid.pibrary.api.jei;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import net.minecraft.world.inventory.AbstractContainerMenu;

/**
 * In-memory collector used by JEI-neutral modules before a concrete compat runtime
 * translates the specs into JEI registrations.
 */
public final class PiJeiBootstrap implements PiJeiBridge {
    private final List<PiJeiCategorySpec<?>> categories = new ArrayList<>();
    private final List<PiJeiCatalystSpec> catalysts = new ArrayList<>();
    private final List<PiJeiClickArea> clickAreas = new ArrayList<>();
    private final List<PiJeiExtraArea> extraAreas = new ArrayList<>();
    private final List<PiJeiTransferSpec<?, ?>> transfers = new ArrayList<>();
    private final List<PiJeiAliasSpec> aliases = new ArrayList<>();

    @Override
    public <R> void registerCategory(PiJeiCategorySpec<R> category) {
        categories.add(Objects.requireNonNull(category, "category"));
    }

    @Override
    public void registerCatalyst(PiJeiCatalystSpec catalyst) {
        catalysts.add(Objects.requireNonNull(catalyst, "catalyst"));
    }

    @Override
    public void registerClickArea(PiJeiClickArea area) {
        clickAreas.add(Objects.requireNonNull(area, "area"));
    }

    @Override
    public void registerExtraArea(PiJeiExtraArea area) {
        extraAreas.add(Objects.requireNonNull(area, "area"));
    }

    @Override
    public <C extends AbstractContainerMenu, R> void registerTransfer(PiJeiTransferSpec<C, R> transfer) {
        transfers.add(Objects.requireNonNull(transfer, "transfer"));
    }

    @Override
    public void registerAlias(PiJeiAliasSpec alias) {
        aliases.add(Objects.requireNonNull(alias, "alias"));
    }

    /**
     * Returns collected category specs.
     *
     * @return immutable category list
     */
    public List<PiJeiCategorySpec<?>> categories() {
        return List.copyOf(categories);
    }

    /**
     * Returns collected catalyst specs.
     *
     * @return immutable catalyst list
     */
    public List<PiJeiCatalystSpec> catalysts() {
        return List.copyOf(catalysts);
    }

    /**
     * Returns collected click-area specs.
     *
     * @return immutable click-area list
     */
    public List<PiJeiClickArea> clickAreas() {
        return List.copyOf(clickAreas);
    }

    /**
     * Returns collected extra-area specs.
     *
     * @return immutable extra-area list
     */
    public List<PiJeiExtraArea> extraAreas() {
        return List.copyOf(extraAreas);
    }

    /**
     * Returns collected transfer specs.
     *
     * @return immutable transfer list
     */
    public List<PiJeiTransferSpec<?, ?>> transfers() {
        return List.copyOf(transfers);
    }

    /**
     * Returns collected alias specs.
     *
     * @return immutable alias list
     */
    public List<PiJeiAliasSpec> aliases() {
        return List.copyOf(aliases);
    }
}
