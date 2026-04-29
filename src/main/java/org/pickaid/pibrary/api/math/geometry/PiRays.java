package org.pickaid.pibrary.api.math.geometry;

import java.util.Objects;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Helpers for simple line and segment geometry.
 *
 * <p>Use this class for the small broad-phase calculations that appear before a
 * real collision query or a real projection step. Common cases are:</p>
 * <ul>
 *     <li>building coarse query bounds around a melee sweep or beam trace;</li>
 *     <li>finding which entities or chunks are even worth checking for a
 *     segment-based action;</li>
 *     <li>preparing a safe bounds box for later debug rendering.</li>
 * </ul>
 *
 * <p>Phase 1 keeps the surface intentionally small and focused on the reusable
 * broad-phase helpers that show up first in real gameplay code.</p>
 */
public final class PiRays {
    private PiRays() {
    }

    /**
     * Creates an axis-aligned bounds box that contains a line segment.
     *
     * <p>The returned box contains both endpoints. If {@code inflate} is
     * positive, the box expands equally in every direction afterward. That is
     * useful when the real effect has thickness, such as a sword arc, beam,
     * hook line, or loose projectile tunnel.</p>
     *
     * @param from the start point of the segment
     * @param to the end point of the segment
     * @param inflate the amount to expand the bounds in every direction; must
     *     be finite and greater than or equal to {@code 0.0}
     * @return an axis-aligned bounds box for the segment
     * @throws NullPointerException if {@code from} or {@code to} is
     *     {@code null}
     * @throws IllegalArgumentException if {@code inflate} is negative or not
     *     finite
     */
    public static AABB segmentBounds(Vec3 from, Vec3 to, double inflate) {
        Objects.requireNonNull(from, "from");
        Objects.requireNonNull(to, "to");
        if (!Double.isFinite(inflate) || inflate < 0.0D) {
            throw new IllegalArgumentException("inflate must be finite and >= 0");
        }
        return new AABB(
            Math.min(from.x, to.x), Math.min(from.y, to.y), Math.min(from.z, to.z),
            Math.max(from.x, to.x), Math.max(from.y, to.y), Math.max(from.z, to.z)
        ).inflate(inflate);
    }
}
