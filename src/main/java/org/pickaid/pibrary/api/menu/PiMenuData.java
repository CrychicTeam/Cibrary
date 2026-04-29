package org.pickaid.pibrary.api.menu;

import java.util.List;
import java.util.Objects;
import net.minecraft.world.inventory.ContainerData;
import org.pickaid.pibrary.api.presentation.PiPresentationSource;
import org.pickaid.pibrary.api.presentation.PiPresentations;

/**
 * Small {@link ContainerData} adapter for menus backed by explicit int slots.
 */
public final class PiMenuData implements ContainerData {
    private final List<PiMenuDataSlot> slots;
    private final Runnable afterSet;

    private PiMenuData(List<PiMenuDataSlot> slots, Runnable afterSet) {
        this.slots = List.copyOf(slots);
        this.afterSet = Objects.requireNonNull(afterSet, "afterSet");
    }

    /**
     * Creates menu data without presentation invalidation.
     *
     * @param slots synced menu slots
     * @return container data bridge
     */
    public static PiMenuData create(PiMenuDataSlot... slots) {
        return new PiMenuData(List.of(slots), () -> {
        });
    }

    /**
     * Creates menu data that invalidates screen projections marked with
     * {@code refreshOnMenuData(...)} whenever the client receives a slot update.
     *
     * @param source presentation source owning the menu state
     * @param slots synced menu slots
     * @return container data bridge
     */
    public static PiMenuData refreshing(PiPresentationSource source, PiMenuDataSlot... slots) {
        Objects.requireNonNull(source, "source");
        return new PiMenuData(List.of(slots), () -> PiPresentations.invalidateMenuData(source));
    }

    @Override
    public int get(int index) {
        return slot(index).get();
    }

    @Override
    public void set(int index, int value) {
        slot(index).set(value);
        afterSet.run();
    }

    @Override
    public int getCount() {
        return slots.size();
    }

    private PiMenuDataSlot slot(int index) {
        if (index < 0 || index >= slots.size()) {
            throw new IndexOutOfBoundsException("Menu data slot " + index + " outside 0.." + (slots.size() - 1));
        }
        return slots.get(index);
    }
}
