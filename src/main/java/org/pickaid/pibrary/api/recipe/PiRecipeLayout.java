package org.pickaid.pibrary.api.recipe;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * Viewer-facing layout for one {@link PiRecipeView}.
 *
 * <p>This is intentionally not a JEI class. Gameplay code can expose stable
 * recipe data, and a separate compat module can translate these neutral slots
 * into the concrete recipe viewer installed by the user.</p>
 *
 * @param view normalized recipe data
 * @param slots positioned display slots
 * @param <R> backing recipe type
 */
public record PiRecipeLayout<R>(
        PiRecipeView<R> view,
        List<PiRecipeSlot<?>> slots
) {
    public PiRecipeLayout {
        Objects.requireNonNull(view, "view");
        slots = List.copyOf(Objects.requireNonNull(slots, "slots"));
        slots.forEach(slot -> Objects.requireNonNull(slot, "slot"));
    }

    public Stream<PiRecipeSlot<?>> byRole(PiRecipeRole role) {
        Objects.requireNonNull(role, "role");
        return slots.stream().filter(slot -> slot.role() == role);
    }

    public static <R> Builder<R> builder(PiRecipeView<R> view) {
        return new Builder<>(view);
    }

    public static final class Builder<R> {
        private final PiRecipeView<R> view;
        private final List<PiRecipeSlot<?>> slots = new ArrayList<>();
        private PiRecipeSlot<?> lastSlot;

        private Builder(PiRecipeView<R> view) {
            this.view = Objects.requireNonNull(view, "view");
        }

        public Builder<R> input(int x, int y, Object value) {
            return slot(PiRecipeRole.INPUT, x, y, value);
        }

        public Builder<R> output(int x, int y, Object value) {
            return slot(PiRecipeRole.OUTPUT, x, y, value);
        }

        public Builder<R> display(int x, int y, Object value) {
            return slot(PiRecipeRole.DISPLAY, x, y, value);
        }

        public Builder<R> slot(PiRecipeRole role, int x, int y, Object value) {
            lastSlot = new PiRecipeSlot<>(role, x, y, value);
            slots.add(lastSlot);
            return this;
        }

        public Builder<R> slot(PiRecipeSlot<?> slot) {
            lastSlot = Objects.requireNonNull(slot, "slot");
            slots.add(lastSlot);
            return this;
        }

        public Builder<R> thenOutput(int dx, Object value) {
            PiRecipeSlot<?> anchor = requireLastSlot();
            return output(anchor.x() + dx, anchor.y(), value);
        }

        public Builder<R> displayBelow(int dy, Object value) {
            PiRecipeSlot<?> anchor = requireLastSlot();
            return display(anchor.x(), anchor.y() + 18 + dy, value);
        }

        public Builder<R> row(PiRecipeRole role, int x, int y, int spacing, Iterable<?> values) {
            Objects.requireNonNull(values, "values");
            if (spacing < 0) {
                throw new IllegalArgumentException("spacing must be >= 0");
            }
            int index = 0;
            for (Object value : values) {
                slot(role, x + spacing * index, y, value);
                index++;
            }
            return this;
        }

        public PiRecipeLayout<R> build() {
            return new PiRecipeLayout<>(view, slots);
        }

        private PiRecipeSlot<?> requireLastSlot() {
            if (lastSlot == null) {
                throw new IllegalStateException("relative slot requires an earlier slot");
            }
            return lastSlot;
        }
    }
}
