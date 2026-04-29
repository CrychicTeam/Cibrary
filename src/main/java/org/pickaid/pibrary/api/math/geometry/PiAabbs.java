package org.pickaid.pibrary.api.math.geometry;

import java.util.List;
import java.util.Objects;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Helpers for the two most common {@link AABB} tasks: making one and sampling
 * one.
 *
 * <p>Use this class when you want an axis-aligned volume around a point or
 * when you need all eight corners of an existing box. Common cases are:</p>
 * <ul>
 *     <li>query volumes around one position;</li>
 *     <li>simple hit or search areas around an anchor point;</li>
 *     <li>screen-space projection where each corner of the box must be
 *     checked.</li>
 * </ul>
 */
public final class PiAabbs {
    private PiAabbs() {
    }

    /**
     * Creates a cube-shaped box centered on one point.
     *
     * <p>{@code halfExtent} is the distance from the center to each face. Use
     * this when you think in terms of "give me a box around this center" rather
     * than manually subtracting and adding coordinates yourself.</p>
     *
     * @param center the center point of the box
     * @param halfExtent the distance from the center to each face; must be a
     *     finite number greater than or equal to {@code 0.0}
     * @return a centered axis-aligned box
     * @throws NullPointerException if {@code center} is {@code null}
     * @throws IllegalArgumentException if {@code halfExtent} is negative or not
     *     finite
     */
    public static AABB around(Vec3 center, double halfExtent) {
        Objects.requireNonNull(center, "center");
        if (!Double.isFinite(halfExtent) || halfExtent < 0.0D) {
            throw new IllegalArgumentException("halfExtent must be finite and >= 0");
        }
        return new AABB(
            center.x - halfExtent, center.y - halfExtent, center.z - halfExtent,
            center.x + halfExtent, center.y + halfExtent, center.z + halfExtent
        );
    }

    /**
     * Creates the full block-sized box for one block position.
     *
     * <p>This mirrors the common vanilla "block occupies one unit cube from
     * its lower corner" convention. Use it for block-centered overlap checks,
     * projection bounds, or simple spatial queries around one block.</p>
     *
     * @param pos the block position
     * @return the one-block axis-aligned box at {@code pos}
     * @throws NullPointerException if {@code pos} is {@code null}
     */
    public static AABB block(BlockPos pos) {
        Objects.requireNonNull(pos, "pos");
        return new AABB(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1.0D, pos.getY() + 1.0D, pos.getZ() + 1.0D);
    }

    /**
     * Returns the smallest box that contains an existing box and one point.
     *
     * <p>Use this when a query volume or projected bounds needs to grow just
     * enough to include a new sample point without manually re-checking each
     * axis.</p>
     *
     * @param box the existing box
     * @param point the point that must be contained by the result
     * @return a box covering both {@code box} and {@code point}
     * @throws NullPointerException if {@code box} or {@code point} is
     *     {@code null}
     */
    public static AABB include(AABB box, Vec3 point) {
        Objects.requireNonNull(box, "box");
        Objects.requireNonNull(point, "point");
        return new AABB(
            Math.min(box.minX, point.x), Math.min(box.minY, point.y), Math.min(box.minZ, point.z),
            Math.max(box.maxX, point.x), Math.max(box.maxY, point.y), Math.max(box.maxZ, point.z)
        );
    }

    /**
     * Returns the eight corner points of a box.
     *
     * <p>Use this when a later algorithm needs the actual corners instead of
     * just the box itself. The most common case in this math stack is bounds
     * projection, but deterministic debug previews and sampling logic also use
     * it.</p>
     *
     * @param box the box to sample
     * @return an immutable list of the box's eight corner points
     * @throws NullPointerException if {@code box} is {@code null}
     */
    public static List<Vec3> corners(AABB box) {
        Objects.requireNonNull(box, "box");
        return List.of(
            new Vec3(box.minX, box.minY, box.minZ),
            new Vec3(box.minX, box.minY, box.maxZ),
            new Vec3(box.minX, box.maxY, box.minZ),
            new Vec3(box.minX, box.maxY, box.maxZ),
            new Vec3(box.maxX, box.minY, box.minZ),
            new Vec3(box.maxX, box.minY, box.maxZ),
            new Vec3(box.maxX, box.maxY, box.minZ),
            new Vec3(box.maxX, box.maxY, box.maxZ)
        );
    }
}
