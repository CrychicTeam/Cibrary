package org.pickaid.pibrary.api.math;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Helpers for Minecraft's {@link AABB}.
 *
 * <p>The methods here cover the common production cases that otherwise become
 * hand-written in every mod: block faces, edge outlines, unions, block
 * iteration, and cheap multi-box approximations for round preview volumes.</p>
 */
public final class PiAabbs {
    public static final AABB EMPTY = new AABB(0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D);
    public static final AABB FULL_BLOCK = new AABB(0.0D, 0.0D, 0.0D, 1.0D, 1.0D, 1.0D);

    private static final double ROUND_APPROX_RATE = 1.56D;

    private PiAabbs() {
    }

    /**
     * Creates an {@code AABB} centered on a point.
     *
     * @param center center point
     * @param width x size
     * @param height y size
     * @param depth z size
     * @return centered box
     */
    public static AABB centered(Vec3 center, double width, double height, double depth) {
        Objects.requireNonNull(center, "center");
        if (width < 0.0D || height < 0.0D || depth < 0.0D) {
            throw new IllegalArgumentException("box sizes must be >= 0");
        }
        double halfWidth = width * 0.5D;
        double halfHeight = height * 0.5D;
        double halfDepth = depth * 0.5D;
        return new AABB(
                center.x - halfWidth,
                center.y - halfHeight,
                center.z - halfDepth,
                center.x + halfWidth,
                center.y + halfHeight,
                center.z + halfDepth
        );
    }

    /**
     * Returns the geometric center of a box.
     *
     * @param box input box
     * @return center point
     */
    public static Vec3 center(AABB box) {
        Objects.requireNonNull(box, "box");
        return new Vec3(
                (box.minX + box.maxX) * 0.5D,
                (box.minY + box.maxY) * 0.5D,
                (box.minZ + box.maxZ) * 0.5D
        );
    }

    /**
     * Computes the box volume.
     *
     * @param box input box
     * @return volume
     */
    public static double volume(AABB box) {
        Objects.requireNonNull(box, "box");
        return Math.max(0.0D, box.maxX - box.minX)
                * Math.max(0.0D, box.maxY - box.minY)
                * Math.max(0.0D, box.maxZ - box.minZ);
    }

    /**
     * Expands a box so that it contains the given point.
     *
     * @param box input box
     * @param point point to include
     * @return minimal box containing both
     */
    public static AABB include(AABB box, Vec3 point) {
        Objects.requireNonNull(box, "box");
        Objects.requireNonNull(point, "point");
        return new AABB(
                Math.min(box.minX, point.x),
                Math.min(box.minY, point.y),
                Math.min(box.minZ, point.z),
                Math.max(box.maxX, point.x),
                Math.max(box.maxY, point.y),
                Math.max(box.maxZ, point.z)
        );
    }

    /**
     * Expands a box so that it includes the full block cube at {@code pos}.
     *
     * @param box input box
     * @param pos block position
     * @return minimal box containing both
     */
    public static AABB include(AABB box, BlockPos pos) {
        Objects.requireNonNull(pos, "pos");
        return include(include(box, Vec3.atLowerCornerOf(pos)), Vec3.atLowerCornerOf(pos).add(1.0D, 1.0D, 1.0D));
    }

    /**
     * Returns one box containing every input box.
     *
     * @param boxes boxes to union
     * @return union, or {@link #EMPTY} when the list is empty
     */
    public static AABB union(List<AABB> boxes) {
        Objects.requireNonNull(boxes, "boxes");
        if (boxes.isEmpty()) {
            return EMPTY;
        }
        AABB result = boxes.get(0);
        for (int i = 1; i < boxes.size(); i++) {
            result = result.minmax(boxes.get(i));
        }
        return result;
    }

    /**
     * Scales a box around its center.
     *
     * @param box input box
     * @param scale scale factor
     * @return scaled box
     */
    public static AABB scale(AABB box, double scale) {
        Objects.requireNonNull(box, "box");
        if (scale < 0.0D) {
            throw new IllegalArgumentException("scale must be >= 0");
        }
        Vec3 center = center(box);
        return centered(center, (box.maxX - box.minX) * scale, (box.maxY - box.minY) * scale, (box.maxZ - box.minZ) * scale);
    }

    /**
     * Builds a thin face inside a unit block.
     *
     * <p>Use this for simple voxel shapes, preview boxes, and block-side hit
     * helpers. The returned box is in block-local coordinates.</p>
     *
     * @param direction face direction
     * @param thickness thickness in block units, exclusive range {@code (0, 1)}
     * @return block-local face box
     */
    public static AABB face(Direction direction, double thickness) {
        Objects.requireNonNull(direction, "direction");
        requireUnitThickness(thickness);
        double other = 1.0D - thickness;
        return switch (direction) {
            case DOWN -> new AABB(0.0D, 0.0D, 0.0D, 1.0D, thickness, 1.0D);
            case UP -> new AABB(0.0D, other, 0.0D, 1.0D, 1.0D, 1.0D);
            case NORTH -> new AABB(0.0D, 0.0D, 0.0D, 1.0D, 1.0D, thickness);
            case SOUTH -> new AABB(0.0D, 0.0D, other, 1.0D, 1.0D, 1.0D);
            case WEST -> new AABB(0.0D, 0.0D, 0.0D, thickness, 1.0D, 1.0D);
            case EAST -> new AABB(other, 0.0D, 0.0D, 1.0D, 1.0D, 1.0D);
        };
    }

    /**
     * Builds the twelve local edge boxes of a unit block.
     *
     * @param thickness edge thickness in block units, exclusive range
     * {@code (0, 1)}
     * @return edge boxes
     */
    public static List<AABB> unitEdges(double thickness) {
        requireUnitThickness(thickness);
        double other = 1.0D - thickness;
        AABB x = new AABB(0.0D, 0.0D, 0.0D, 1.0D, thickness, thickness);
        AABB y = new AABB(0.0D, 0.0D, 0.0D, thickness, 1.0D, thickness);
        AABB z = new AABB(0.0D, 0.0D, 0.0D, thickness, thickness, 1.0D);
        return List.of(
                x,
                x.move(0.0D, other, 0.0D),
                x.move(0.0D, 0.0D, other),
                x.move(0.0D, other, other),
                y,
                y.move(other, 0.0D, 0.0D),
                y.move(0.0D, 0.0D, other),
                y.move(other, 0.0D, other),
                z,
                z.move(other, 0.0D, 0.0D),
                z.move(0.0D, other, 0.0D),
                z.move(other, other, 0.0D)
        );
    }

    /**
     * Iterates every block position touched by a box.
     *
     * <p>The mutable position passed to the consumer is reused. Call
     * {@code immutable()} inside the consumer when you need to store it.</p>
     *
     * @param box input box
     * @param consumer receiver for touched block positions
     */
    public static void forEachTouchedBlock(AABB box, Consumer<BlockPos.MutableBlockPos> consumer) {
        Objects.requireNonNull(box, "box");
        Objects.requireNonNull(consumer, "consumer");
        int minX = (int) Math.floor(box.minX);
        int minY = (int) Math.floor(box.minY);
        int minZ = (int) Math.floor(box.minZ);
        int maxX = (int) Math.floor(Math.nextAfter(box.maxX, Double.NEGATIVE_INFINITY));
        int maxY = (int) Math.floor(Math.nextAfter(box.maxY, Double.NEGATIVE_INFINITY));
        int maxZ = (int) Math.floor(Math.nextAfter(box.maxZ, Double.NEGATIVE_INFINITY));
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    consumer.accept(pos.set(x, y, z));
                }
            }
        }
    }

    /**
     * Collects every block position touched by a box.
     *
     * @param box input box
     * @return immutable block positions
     */
    public static List<BlockPos> touchedBlocks(AABB box) {
        List<BlockPos> positions = new ArrayList<>();
        forEachTouchedBlock(box, pos -> positions.add(pos.immutable()));
        return positions;
    }

    /**
     * Builds five boxes approximating a vertical cylinder.
     *
     * <p>This is not a mathematically exact cylinder. It is a cheap broad-phase
     * approximation that works well for area targeting, preview outlines, and
     * collision prefiltering before a narrow-phase distance check.</p>
     *
     * @param baseCenter center of the bottom face
     * @param radius cylinder radius
     * @param height cylinder height
     * @return broad-phase boxes
     */
    public static List<AABB> approximateVerticalCylinder(Vec3 baseCenter, double radius, double height) {
        Objects.requireNonNull(baseCenter, "baseCenter");
        if (radius <= 0.0D || height <= 0.0D) {
            throw new IllegalArgumentException("radius and height must be > 0");
        }
        Vec3 center = baseCenter.add(0.0D, height * 0.5D, 0.0D);
        double core = radius * ROUND_APPROX_RATE;
        double sideLength = radius - core * 0.5D;
        double sideWidth = (radius * radius * Math.PI - core * core) / 4.0D / sideLength;
        return List.of(
                AABB.ofSize(center, core, height, core),
                AABB.ofSize(center.add(radius - sideLength * 0.5D, 0.0D, 0.0D), sideLength, height, sideWidth),
                AABB.ofSize(center.add(-radius + sideLength * 0.5D, 0.0D, 0.0D), sideLength, height, sideWidth),
                AABB.ofSize(center.add(0.0D, 0.0D, radius - sideLength * 0.5D), sideWidth, height, sideLength),
                AABB.ofSize(center.add(0.0D, 0.0D, -radius + sideLength * 0.5D), sideWidth, height, sideLength)
        );
    }

    /**
     * Builds seven boxes approximating a sphere.
     *
     * @param center sphere center
     * @param radius sphere radius
     * @return broad-phase boxes
     */
    public static List<AABB> approximateSphere(Vec3 center, double radius) {
        Objects.requireNonNull(center, "center");
        if (radius <= 0.0D) {
            throw new IllegalArgumentException("radius must be > 0");
        }
        double core = radius * ROUND_APPROX_RATE;
        double sideLength = radius - core * 0.5D;
        double sideWidth = (radius * radius * Math.PI - core * core) / 4.0D / sideLength;
        return List.of(
                AABB.ofSize(center, core, core, core),
                AABB.ofSize(center.add(radius - sideLength * 0.5D, 0.0D, 0.0D), sideLength, sideWidth, sideWidth),
                AABB.ofSize(center.add(-radius + sideLength * 0.5D, 0.0D, 0.0D), sideLength, sideWidth, sideWidth),
                AABB.ofSize(center.add(0.0D, radius - sideLength * 0.5D, 0.0D), sideWidth, sideLength, sideWidth),
                AABB.ofSize(center.add(0.0D, -radius + sideLength * 0.5D, 0.0D), sideWidth, sideLength, sideWidth),
                AABB.ofSize(center.add(0.0D, 0.0D, radius - sideLength * 0.5D), sideWidth, sideWidth, sideLength),
                AABB.ofSize(center.add(0.0D, 0.0D, -radius + sideLength * 0.5D), sideWidth, sideWidth, sideLength)
        );
    }

    /**
     * Checks whether a box intersects at least one candidate box.
     *
     * @param box input box
     * @param candidates boxes to test
     * @return whether any intersection exists
     */
    public static boolean intersectsAny(AABB box, Iterable<AABB> candidates) {
        Objects.requireNonNull(box, "box");
        Objects.requireNonNull(candidates, "candidates");
        for (AABB candidate : candidates) {
            if (box.intersects(candidate)) {
                return true;
            }
        }
        return false;
    }

    private static void requireUnitThickness(double thickness) {
        if (!(thickness > 0.0D && thickness < 1.0D)) {
            throw new IllegalArgumentException("thickness must be in the exclusive range (0, 1)");
        }
    }
}
