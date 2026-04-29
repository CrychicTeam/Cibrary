package org.pickaid.pibrary.api.menu;

import java.util.Objects;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;

/**
 * One integer synchronized by a vanilla menu.
 */
public record PiMenuDataSlot(IntSupplier getter, IntConsumer setter) {
    public PiMenuDataSlot {
        Objects.requireNonNull(getter, "getter");
        Objects.requireNonNull(setter, "setter");
    }

    public static PiMenuDataSlot mutable(IntSupplier getter, IntConsumer setter) {
        return new PiMenuDataSlot(getter, setter);
    }

    public static PiMenuDataSlot readOnly(IntSupplier getter) {
        return new PiMenuDataSlot(getter, ignored -> {
        });
    }

    public int get() {
        return getter.getAsInt();
    }

    public void set(int value) {
        setter.accept(value);
    }
}
