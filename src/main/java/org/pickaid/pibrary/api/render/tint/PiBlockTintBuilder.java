package org.pickaid.pibrary.api.render.tint;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.ToIntFunction;
import net.minecraft.client.color.block.BlockColor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Readable builder for block tint providers with multiple tint layers.
 */
public final class PiBlockTintBuilder {
    private final Map<Integer, BlockColor> layers = new LinkedHashMap<>();

    private PiBlockTintBuilder() {
    }

    public static PiBlockTintBuilder create() {
        return new PiBlockTintBuilder();
    }

    public Layer layer(int layer) {
        PiBlockTints.requireLayer(layer);
        return new Layer(layer);
    }

    public PiBlockTintBuilder provider(int layer, BlockColor color) {
        PiBlockTints.requireLayer(layer);
        layers.put(layer, Objects.requireNonNull(color, "color"));
        return this;
    }

    public BlockColor build() {
        return PiBlockTints.layers(layers);
    }

    public final class Layer {
        private final int layer;

        private Layer(int layer) {
            this.layer = layer;
        }

        public PiBlockTintBuilder constant(int color) {
            return provider(PiBlockTints.constant(color));
        }

        public PiBlockTintBuilder provider(BlockColor color) {
            return PiBlockTintBuilder.this.provider(layer, color);
        }

        public PiBlockTintBuilder grass() {
            return provider(PiBlockTints.grass());
        }

        public PiBlockTintBuilder foliage() {
            return provider(PiBlockTints.foliage());
        }

        public PiBlockTintBuilder water() {
            return provider(PiBlockTints.water());
        }

        public PiBlockTintBuilder state(ToIntFunction<BlockState> color) {
            return provider(PiBlockTints.state(color));
        }

        public <T extends BlockEntity> PiBlockTintBuilder blockEntity(
                Class<T> type,
                ToIntFunction<? super T> color,
                int fallbackColor
        ) {
            return provider(PiBlockTints.blockEntity(type, color, fallbackColor));
        }

        public PiBlockTintBuilder delegateToState(
                Function<BlockState, BlockState> targetState,
                int fallbackColor
        ) {
            return provider(PiBlockTints.delegateToState(targetState, fallbackColor));
        }

        public PiBlockTintBuilder multiply(BlockColor first, BlockColor second) {
            return provider(PiBlockTints.multiply(first, second));
        }
    }
}
