package org.pickaid.pibrary.api.render.tint;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.ToIntFunction;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.world.item.ItemStack;

/**
 * Readable builder for item tint providers with multiple tint layers.
 */
public final class PiItemTintBuilder {
    private final Map<Integer, ItemColor> layers = new LinkedHashMap<>();

    private PiItemTintBuilder() {
    }

    public static PiItemTintBuilder create() {
        return new PiItemTintBuilder();
    }

    public Layer layer(int layer) {
        PiBlockTints.requireLayer(layer);
        return new Layer(layer);
    }

    public PiItemTintBuilder provider(int layer, ItemColor color) {
        PiBlockTints.requireLayer(layer);
        layers.put(layer, Objects.requireNonNull(color, "color"));
        return this;
    }

    public ItemColor build() {
        return PiItemTints.layers(layers);
    }

    public final class Layer {
        private final int layer;

        private Layer(int layer) {
            this.layer = layer;
        }

        public PiItemTintBuilder constant(int color) {
            return provider(PiItemTints.constant(color));
        }

        public PiItemTintBuilder provider(ItemColor color) {
            return PiItemTintBuilder.this.provider(layer, color);
        }

        public PiItemTintBuilder stack(ToIntFunction<ItemStack> color) {
            return provider(PiItemTints.stack(color));
        }

        public PiItemTintBuilder durability(int brokenColor, int fullColor) {
            return provider(PiItemTints.durabilityLayer(layer, brokenColor, fullColor));
        }

        public PiItemTintBuilder blockItem() {
            return provider(PiItemTints.blockItemLayer(layer));
        }

        public PiItemTintBuilder multiply(ItemColor first, ItemColor second) {
            return provider(PiItemTints.multiply(first, second));
        }
    }
}
