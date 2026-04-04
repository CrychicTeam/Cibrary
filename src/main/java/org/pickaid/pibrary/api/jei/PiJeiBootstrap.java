package org.pickaid.pibrary.api.jei;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import net.minecraft.world.inventory.AbstractContainerMenu;

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

    public List<PiJeiCategorySpec<?>> categories() {
        return List.copyOf(categories);
    }

    public List<PiJeiCatalystSpec> catalysts() {
        return List.copyOf(catalysts);
    }

    public List<PiJeiClickArea> clickAreas() {
        return List.copyOf(clickAreas);
    }

    public List<PiJeiExtraArea> extraAreas() {
        return List.copyOf(extraAreas);
    }

    public List<PiJeiTransferSpec<?, ?>> transfers() {
        return List.copyOf(transfers);
    }

    public List<PiJeiAliasSpec> aliases() {
        return List.copyOf(aliases);
    }
}
