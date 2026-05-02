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
 * @param name optional slot name for lookup during category drawing
 * @param focusGroup optional focus-link group id
 * @param background built-in slot background hint
 * @param fluidRenderer optional fluid renderer hint
 * @param tooltips extra tooltip lines as vanilla components
 * @param <T> value type
 */
public record PiRecipeSlot<T>(
        PiRecipeRole role,
        int x,
        int y,
        List<T> values,
        Visibility visibility,
        Optional<String> name,
        Optional<String> focusGroup,
        Background background,
        Optional<PiRecipeFluidRenderHint> fluidRenderer,
        List<Component> tooltips
) {
    public PiRecipeSlot {
        Objects.requireNonNull(role, "role");
        values = List.copyOf(Objects.requireNonNull(values, "values"));
        Objects.requireNonNull(visibility, "visibility");
        name = cleanOptionalString(name, "name");
        focusGroup = Objects.requireNonNull(focusGroup, "focusGroup")
                .map(String::trim)
                .filter(value -> !value.isEmpty());
        Objects.requireNonNull(background, "background");
        fluidRenderer = Objects.requireNonNull(fluidRenderer, "fluidRenderer");
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

    public PiRecipeSlot(
            PiRecipeRole role,
            int x,
            int y,
            List<T> values,
            Visibility visibility,
            Optional<String> focusGroup,
            List<Component> tooltips
    ) {
        this(
                role,
                x,
                y,
                values,
                visibility,
                Optional.empty(),
                focusGroup,
                Background.NONE,
                Optional.empty(),
                tooltips);
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

    public enum Background {
        NONE,
        STANDARD,
        OUTPUT
    }

    public static final class Builder {
        private final PiRecipeRole role;
        private final int x;
        private final int y;
        private final List<Object> values = new ArrayList<>();
        private final List<Component> tooltips = new ArrayList<>();
        private Visibility visibility = Visibility.VISIBLE;
        private String name;
        private String focusGroup;
        private Background background = Background.NONE;
        private PiRecipeFluidRenderHint fluidRenderer;

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

        public Builder name(String name) {
            this.name = Objects.requireNonNull(name, "name");
            return this;
        }

        public Builder focusGroup(String focusGroup) {
            this.focusGroup = Objects.requireNonNull(focusGroup, "focusGroup");
            return this;
        }

        public Builder standardBackground() {
            background = Background.STANDARD;
            return this;
        }

        public Builder outputBackground() {
            background = Background.OUTPUT;
            return this;
        }

        public Builder fluidRenderer(long capacity, boolean showCapacity, int width, int height) {
            this.fluidRenderer = new PiRecipeFluidRenderHint(capacity, showCapacity, width, height);
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
                    Optional.ofNullable(name),
                    Optional.ofNullable(focusGroup),
                    background,
                    Optional.ofNullable(fluidRenderer),
                    tooltips);
        }
    }

    private static Optional<String> cleanOptionalString(Optional<String> value, String name) {
        Objects.requireNonNull(value, name);
        return value.map(String::trim).filter(cleaned -> !cleaned.isEmpty());
    }
}
