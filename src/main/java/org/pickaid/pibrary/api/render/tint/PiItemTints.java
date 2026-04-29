package org.pickaid.pibrary.api.render.tint;

import java.util.Objects;
import java.util.Map;
import java.util.function.ToIntFunction;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;

/**
 * Factories for common {@link ItemColor} patterns.
 *
 * <p>Use these when an item model has tint indices. The helpers cover the
 * common production cases: static layer tint, stack-dependent tint, durability
 * gradients, and block-item tint delegation.</p>
 */
public final class PiItemTints {
    private PiItemTints() {
    }

    public static ItemColor constant(int color) {
        return (stack, tintIndex) -> color;
    }

    public static ItemColor layer(int layer, int color) {
        PiBlockTints.requireLayer(layer);
        return (stack, tintIndex) -> tintIndex == layer ? color : PiColors.NO_TINT;
    }

    public static ItemColor layer(int layer, ItemColor color) {
        PiBlockTints.requireLayer(layer);
        Objects.requireNonNull(color, "color");
        return (stack, tintIndex) -> tintIndex == layer ? color.getColor(stack, tintIndex) : PiColors.NO_TINT;
    }

    public static ItemColor layers(Map<Integer, ? extends ItemColor> colors) {
        Objects.requireNonNull(colors, "colors");
        Map<Integer, ? extends ItemColor> copy = Map.copyOf(colors);
        for (Integer layer : copy.keySet()) {
            PiBlockTints.requireLayer(Objects.requireNonNull(layer, "layer"));
            Objects.requireNonNull(copy.get(layer), "color");
        }
        return (stack, tintIndex) -> {
            ItemColor color = copy.get(tintIndex);
            return color == null ? PiColors.NO_TINT : color.getColor(stack, tintIndex);
        };
    }

    public static ItemColor multiply(ItemColor first, ItemColor second) {
        Objects.requireNonNull(first, "first");
        Objects.requireNonNull(second, "second");
        return (stack, tintIndex) -> {
            int a = first.getColor(stack, tintIndex);
            int b = second.getColor(stack, tintIndex);
            if (a == PiColors.NO_TINT || b == PiColors.NO_TINT) {
                return PiColors.NO_TINT;
            }
            return PiColors.multiply(a, b);
        };
    }

    public static ItemColor stack(ToIntFunction<ItemStack> color) {
        Objects.requireNonNull(color, "color");
        return (stack, tintIndex) -> color.applyAsInt(stack);
    }

    public static ItemColor stackLayer(int layer, ToIntFunction<ItemStack> color) {
        PiBlockTints.requireLayer(layer);
        Objects.requireNonNull(color, "color");
        return (stack, tintIndex) -> tintIndex == layer ? color.applyAsInt(stack) : PiColors.NO_TINT;
    }

    /**
     * Colors a layer by item durability.
     *
     * @param layer tint layer
     * @param brokenColor color at zero durability
     * @param fullColor color at full durability
     * @return item color provider
     */
    public static ItemColor durabilityLayer(int layer, int brokenColor, int fullColor) {
        PiBlockTints.requireLayer(layer);
        return (stack, tintIndex) -> {
            if (tintIndex != layer) {
                return PiColors.NO_TINT;
            }
            if (!stack.isDamageableItem() || stack.getMaxDamage() <= 0) {
                return fullColor;
            }
            double remaining = 1.0D - (double) stack.getDamageValue() / (double) stack.getMaxDamage();
            return PiColors.mix(brokenColor, fullColor, remaining);
        };
    }

    /**
     * Delegates a block item's color to Minecraft's block color registry.
     *
     * <p>This mirrors the usual pattern for tinted block items: register the
     * block tint once, then make the item sample the block's default state for
     * the matching layer.</p>
     *
     * @param layer layer to tint
     * @return item color provider
     */
    public static ItemColor blockItemLayer(int layer) {
        PiBlockTints.requireLayer(layer);
        return (stack, tintIndex) -> {
            if (tintIndex != layer || !(stack.getItem() instanceof BlockItem blockItem)) {
                return PiColors.NO_TINT;
            }
            return Minecraft.getInstance().getBlockColors()
                    .getColor(blockItem.getBlock().defaultBlockState(), null, null, tintIndex);
        };
    }
}
