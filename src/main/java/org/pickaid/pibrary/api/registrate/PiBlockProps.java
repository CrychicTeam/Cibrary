package org.pickaid.pibrary.api.registrate;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

/**
 * Base blocks intended for Registrate {@code initialProperties(...)} calls.
 *
 * <p>Registrate copies properties from an existing block first, then lets
 * {@code properties(...)} and {@code transform(...)} apply local changes.</p>
 */
public final class PiBlockProps {
    private PiBlockProps() {
    }

    public static Block stone() {
        return Blocks.STONE;
    }

    public static Block andesite() {
        return Blocks.ANDESITE;
    }

    public static Block wood() {
        return Blocks.OAK_PLANKS;
    }

    public static Block softMetal() {
        return Blocks.GOLD_BLOCK;
    }

    public static Block metal() {
        return Blocks.IRON_BLOCK;
    }

    public static Block copperMetal() {
        return Blocks.COPPER_BLOCK;
    }

    public static Block glass() {
        return Blocks.GLASS;
    }
}
