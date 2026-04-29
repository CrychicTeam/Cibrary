package org.pickaid.pibrary.api.math.geometry;

import java.util.Objects;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

/**
 * Small helpers for the most common {@link Vec3} jobs.
 *
 * <p>Use this class when you already have Mojang vectors and need a small
 * semantic helper instead of rewriting the same vector math. Common cases are:</p>
 * <ul>
 *     <li>turn a look vector into a safe normalized direction;</li>
 *     <li>find the point a projectile, beam, or reach ray should end at after
 *     moving forward by a distance;</li>
 *     <li>avoid repeating zero-vector handling in gameplay code.</li>
 * </ul>
 */
public final class PiVectors {
    private PiVectors() {
    }

    /**
     * Normalizes a vector, or returns {@link Vec3#ZERO} when the vector has no
     * length.
     *
     * <p>Use this when a direction may legally be zero and you do not want that
     * edge case to leak into every call site. This is common for optional aim
     * directions, temporary velocity deltas, or AI vectors that are sometimes
     * unset.</p>
     *
     * <p>Example:</p>
     * <pre>{@code
     * Vec3 look = PiVectors.normalizeOrZero(rawLookVector);
     * }</pre>
     *
     * @param value the vector to normalize
     * @return the normalized vector, or {@link Vec3#ZERO} if {@code value} has
     *     zero length
     * @throws NullPointerException if {@code value} is {@code null}
     */
    public static Vec3 normalizeOrZero(Vec3 value) {
        Objects.requireNonNull(value, "value");
        return value.lengthSqr() == 0.0D ? Vec3.ZERO : value.normalize();
    }

    /**
     * Returns the center point of one block position.
     *
     * <p>Use this when later math wants a {@link Vec3} anchor instead of a
     * grid corner: particles from block center, traces from block center, or
     * projection work that should land on the visual middle of a block.</p>
     *
     * @param pos the block position to center
     * @return the block-center vector
     * @throws NullPointerException if {@code pos} is {@code null}
     */
    public static Vec3 centerOf(BlockPos pos) {
        Objects.requireNonNull(pos, "pos");
        return new Vec3(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D);
    }

    /**
     * Projects a point from an origin along a direction by a distance.
     *
     * <p>Use this when you have "start here, move forward this far". Typical
     * cases are trace endpoints, projectile preview points, and targeting
     * anchors. The direction is normalized before use, so the distance means
     * actual distance, not "distance times the direction vector length".</p>
     *
     * <p>Example:</p>
     * <pre>{@code
     * Vec3 end = PiVectors.project(origin, lookDirection, 5.0);
     * }</pre>
     *
     * @param origin the starting point
     * @param direction the direction to move in
     * @param distance the distance to travel along the normalized direction
     * @return the projected point
     * @throws NullPointerException if {@code origin} or {@code direction} is
     *     {@code null}
     */
    public static Vec3 project(Vec3 origin, Vec3 direction, double distance) {
        Objects.requireNonNull(origin, "origin");
        Objects.requireNonNull(direction, "direction");
        return origin.add(normalizeOrZero(direction).scale(distance));
    }
}
