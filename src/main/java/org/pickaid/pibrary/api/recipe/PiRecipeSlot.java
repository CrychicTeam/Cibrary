package org.pickaid.pibrary.api.recipe;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import net.minecraft.network.chat.Component;

/**
 * One viewer-facing slot in a normalized recipe layout.
 *
 * <p>The coordinates are relative to the recipe display area, not the screen.
 * A concrete JEI/EMI/REI adapter can translate {@link #role()} to the target
 * viewer's role enum and {@link #value()} to the target viewer's ingredient
 * API.</p>
 *
 * @param role slot purpose
 * @param x left coordinate in the recipe display
 * @param y top coordinate in the recipe display
 * @param values item stacks, ingredients, ids, or project-owned display values
 * @param visibility how a viewer should expose this slot
 * @param focusGroup optional focus-link group id
 * @param tooltips extra tooltip lines as vanilla components
 * @param <T> value type
 */
public record PiRecipeSlot<T>(
        PiRecipeRole role,
        int x,
        int y,
        List<T> values,
        Visibility visibility,
        Optional<String> focusGroup,
        List<Component> tooltips
) {
    public PiRecipeSlot {
        Objects.requireNonNull(role, "role");
        values = List.copyOf(Objects.requireNonNull(values, "values"));
        Objects.requireNonNull(visibility, "visibility");
        focusGroup = Objects.requireNonNull(focusGroup, "focusGroup")
                .map(String::trim)
                .filter(value -> !value.isEmpty());
        tooltips = List.copyOf(Objects.requireNonNull(tooltips, "tooltips"));
        if (values.isEmpty()) {
            throw new IllegalArgumentException("slot values must not be empty");
        }
        values.forEach(value -> Objects.requireNonNull(value, "slot value"));
        if (x < 0 || y < 0) {
            throw new IllegalArgumentException("slot coordinates must be >= 0");
        }
    }

    public PiRecipeSlot(PiRecipeRole role, int x, int y, T value) {
        this(role, x, y, List.of(value), Visibility.VISIBLE, Optional.empty(), List.of());
    }

    public T value() {
        return values.get(0);
    }

    public static Builder builder(PiRecipeRole role, int x, int y) {
        return new Builder(role, x, y);
    }

    public enum Visibility {
        VISIBLE,
        HIDDEN_LOOKUP,
        RENDER_ONLY
    }

    public static final class Builder {
        private final PiRecipeRole role;
        private final int x;
        private final int y;
        private final List<Object> values = new ArrayList<>();
        private final List<Component> tooltips = new ArrayList<>();
        private Visibility visibility = Visibility.VISIBLE;
        private String focusGroup;

        private Builder(PiRecipeRole role, int x, int y) {
            this.role = Objects.requireNonNull(role, "role");
            this.x = x;
            this.y = y;
        }

        public Builder value(Object value) {
            values.add(Objects.requireNonNull(value, "value"));
            return this;
        }

        public Builder values(Iterable<?> values) {
            Objects.requireNonNull(values, "values");
            for (Object value : values) {
                value(value);
            }
            return this;
        }

        public Builder hiddenLookup() {
            visibility = Visibility.HIDDEN_LOOKUP;
            return this;
        }

        public Builder renderOnly() {
            visibility = Visibility.RENDER_ONLY;
            return this;
        }

        public Builder focusGroup(String focusGroup) {
            this.focusGroup = Objects.requireNonNull(focusGroup, "focusGroup");
            return this;
        }

        public Builder tooltip(Component tooltip) {
            tooltips.add(Objects.requireNonNull(tooltip, "tooltip"));
            return this;
        }

        public PiRecipeSlot<Object> build() {
            return new PiRecipeSlot<>(
                    role,
                    x,
                    y,
                    values,
                    visibility,
                    Optional.ofNullable(focusGroup),
                    tooltips);
        }
    }
}
