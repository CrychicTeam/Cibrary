package org.pickaid.pibrary.api.math.geometry;

import java.util.List;
import java.util.Objects;
import net.minecraft.core.Direction;

/**
 * Small helpers for {@link Direction} and {@link Direction.Axis}.
 *
 * <p>Use this class when block, entity, or trace logic needs one of the common
 * "all faces except the main axis" patterns. Common cases are:</p>
 * <ul>
 *     <li>checking the four side faces around a pipe, cable, or beam;</li>
 *     <li>spreading particles or effects around a main travel axis;</li>
 *     <li>iterating all non-forward attachment directions without rewriting the
 *     same lists.</li>
 * </ul>
 */
public final class PiDirections {
    private PiDirections() {
    }

    /**
     * Returns the four horizontal directions in a stable order.
     *
     * <p>Use this for side-face checks, neighbor scans on the XZ plane, and
     * any other logic that should explicitly ignore vertical directions.</p>
     *
     * @return an immutable list containing north, south, west, and east
     */
    public static List<Direction> horizontal() {
        return List.of(Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST);
    }

    /**
     * Returns the two vertical directions in a stable order.
     *
     * <p>Use this when code should only look above and below: vertical support
     * checks, ceiling/floor interactions, or vertical-only spread logic.</p>
     *
     * @return an immutable list containing down and up
     */
    public static List<Direction> vertical() {
        return List.of(Direction.DOWN, Direction.UP);
    }

    /**
     * Returns the four directions that do not lie on the given axis.
     *
     * <p>Use this when one axis is already "special" and you need the remaining
     * side directions. Example: if a contraption or projectile is moving along
     * the X axis, the returned directions are the four orthogonal faces you can
     * use for side checks or side effects.</p>
     *
     * @param axis the axis to exclude
     * @return an immutable list of the four directions not aligned with
     *     {@code axis}
     * @throws NullPointerException if {@code axis} is {@code null}
     */
    public static List<Direction> excludingAxis(Direction.Axis axis) {
        Objects.requireNonNull(axis, "axis");
        return switch (axis) {
            case X -> List.of(Direction.NORTH, Direction.SOUTH, Direction.UP, Direction.DOWN);
            case Y -> List.of(Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST);
            case Z -> List.of(Direction.EAST, Direction.WEST, Direction.UP, Direction.DOWN);
        };
    }
}
