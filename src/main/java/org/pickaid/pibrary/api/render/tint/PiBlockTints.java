package org.pickaid.pibrary.api.render.tint;

import java.util.Objects;
import java.util.function.Function;
import java.util.function.ToIntFunction;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockColor;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.FoliageColor;
import net.minecraft.world.level.GrassColor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Factories for common {@link BlockColor} patterns.
 *
 * <p>Use these when the block model has tint indices in its JSON or generated
 * model. The returned color is applied only by Minecraft's normal block color
 * system; this class does not render anything itself.</p>
 */
public final class PiBlockTints {
    private static final int DEFAULT_GRASS = GrassColor.get(0.5D, 1.0D);
    private static final int DEFAULT_FOLIAGE = FoliageColor.getDefaultColor();
    private static final int DEFAULT_WATER = 0x3F76E4;

    private PiBlockTints() {
    }

    public static BlockColor constant(int color) {
        return (state, level, pos, tintIndex) -> color;
    }

    public static BlockColor layer(int layer, int color) {
        requireLayer(layer);
        return (state, level, pos, tintIndex) -> tintIndex == layer ? color : PiColors.NO_TINT;
    }

    public static BlockColor layer(int layer, BlockColor color) {
        requireLayer(layer);
        Objects.requireNonNull(color, "color");
        return (state, level, pos, tintIndex) ->
                tintIndex == layer ? color.getColor(state, level, pos, tintIndex) : PiColors.NO_TINT;
    }

    public static BlockColor layers(Map<Integer, ? extends BlockColor> colors) {
        Objects.requireNonNull(colors, "colors");
        Map<Integer, ? extends BlockColor> copy = Map.copyOf(colors);
        for (Integer layer : copy.keySet()) {
            requireLayer(Objects.requireNonNull(layer, "layer"));
            Objects.requireNonNull(copy.get(layer), "color");
        }
        return (state, level, pos, tintIndex) -> {
            BlockColor color = copy.get(tintIndex);
            return color == null ? PiColors.NO_TINT : color.getColor(state, level, pos, tintIndex);
        };
    }

    public static BlockColor multiply(BlockColor first, BlockColor second) {
        Objects.requireNonNull(first, "first");
        Objects.requireNonNull(second, "second");
        return (state, level, pos, tintIndex) -> {
            int a = first.getColor(state, level, pos, tintIndex);
            int b = second.getColor(state, level, pos, tintIndex);
            if (a == PiColors.NO_TINT || b == PiColors.NO_TINT) {
                return PiColors.NO_TINT;
            }
            return PiColors.multiply(a, b);
        };
    }

    public static BlockColor state(ToIntFunction<BlockState> color) {
        Objects.requireNonNull(color, "color");
        return (state, level, pos, tintIndex) -> color.applyAsInt(state);
    }

    public static BlockColor stateLayer(int layer, ToIntFunction<BlockState> color) {
        requireLayer(layer);
        Objects.requireNonNull(color, "color");
        return (state, level, pos, tintIndex) -> tintIndex == layer ? color.applyAsInt(state) : PiColors.NO_TINT;
    }

    public static BlockColor grass() {
        return grass(DEFAULT_GRASS);
    }

    public static BlockColor grass(int fallbackColor) {
        return (state, level, pos, tintIndex) ->
                level != null && pos != null ? BiomeColors.getAverageGrassColor(level, pos) : fallbackColor;
    }

    public static BlockColor foliage() {
        return foliage(DEFAULT_FOLIAGE);
    }

    public static BlockColor foliage(int fallbackColor) {
        return (state, level, pos, tintIndex) ->
                level != null && pos != null ? BiomeColors.getAverageFoliageColor(level, pos) : fallbackColor;
    }

    public static BlockColor water() {
        return water(DEFAULT_WATER);
    }

    public static BlockColor water(int fallbackColor) {
        return (state, level, pos, tintIndex) ->
                level != null && pos != null ? BiomeColors.getAverageWaterColor(level, pos) : fallbackColor;
    }

    /**
     * Reads color from a block entity at the rendered position.
     *
     * @param type expected block entity type
     * @param color color extractor
     * @param fallbackColor color used for inventory/world-missing contexts
     * @param <T> block entity type
     * @return block color provider
     */
    public static <T extends BlockEntity> BlockColor blockEntity(
            Class<T> type,
            ToIntFunction<? super T> color,
            int fallbackColor
    ) {
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(color, "color");
        return (state, level, pos, tintIndex) -> {
            if (level == null || pos == null) {
                return fallbackColor;
            }
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (!type.isInstance(blockEntity)) {
                return fallbackColor;
            }
            return color.applyAsInt(type.cast(blockEntity));
        };
    }

    /**
     * Delegates tint lookup to another block state.
     *
     * <p>This covers copycat/camouflage style blocks: your block owns the model,
     * but the tint should come from the material it is imitating.</p>
     *
     * @param targetState resolves the state to sample
     * @param fallbackColor color used when no world position is available
     * @return delegating color provider
     */
    public static BlockColor delegateToState(Function<BlockState, BlockState> targetState, int fallbackColor) {
        Objects.requireNonNull(targetState, "targetState");
        return (state, level, pos, tintIndex) -> {
            BlockState target = targetState.apply(state);
            if (target == null) {
                return fallbackColor;
            }
            if (level == null || pos == null) {
                return Minecraft.getInstance().getBlockColors().getColor(target, null, null, tintIndex);
            }
            return Minecraft.getInstance().getBlockColors().getColor(target, level, pos, tintIndex);
        };
    }

    static void requireLayer(int layer) {
        if (layer < 0) {
            throw new IllegalArgumentException("tint layer must be >= 0");
        }
    }
}
