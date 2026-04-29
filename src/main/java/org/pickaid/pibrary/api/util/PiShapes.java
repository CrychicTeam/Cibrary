package org.pickaid.pibrary.api.util;

import java.util.Objects;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Small VoxelShape helpers for block classes and datagen-adjacent code.
 *
 * <p>Methods ending in {@code 16} use model coordinates. A value of
 * {@code 16} means one full block. This keeps shape declarations close to JSON
 * model coordinates and avoids repeating {@code / 16.0D} in every block.</p>
 */
public final class PiShapes {
    private PiShapes() {
    }

    public static VoxelShape box16(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
        requireRange("minX", minX);
        requireRange("minY", minY);
        requireRange("minZ", minZ);
        requireRange("maxX", maxX);
        requireRange("maxY", maxY);
        requireRange("maxZ", maxZ);
        if (minX > maxX || minY > maxY || minZ > maxZ) {
            throw new IllegalArgumentException("shape min values must be <= max values");
        }
        return Shapes.box(minX / 16.0D, minY / 16.0D, minZ / 16.0D, maxX / 16.0D, maxY / 16.0D, maxZ / 16.0D);
    }

    public static VoxelShape face16(Direction direction, double thickness) {
        Objects.requireNonNull(direction, "direction");
        requireThickness(thickness);
        double min = 16.0D - thickness;
        return switch (direction) {
            case DOWN -> box16(0.0D, 0.0D, 0.0D, 16.0D, thickness, 16.0D);
            case UP -> box16(0.0D, min, 0.0D, 16.0D, 16.0D, 16.0D);
            case NORTH -> box16(0.0D, 0.0D, 0.0D, 16.0D, 16.0D, thickness);
            case SOUTH -> box16(0.0D, 0.0D, min, 16.0D, 16.0D, 16.0D);
            case WEST -> box16(0.0D, 0.0D, 0.0D, thickness, 16.0D, 16.0D);
            case EAST -> box16(min, 0.0D, 0.0D, 16.0D, 16.0D, 16.0D);
        };
    }

    public static VoxelShape centerColumn16(double radius, double minY, double maxY) {
        requireRange("radius", radius);
        if (radius > 8.0D) {
            throw new IllegalArgumentException("radius must be <= 8");
        }
        double min = 8.0D - radius;
        double max = 8.0D + radius;
        return box16(min, minY, min, max, maxY, max);
    }

    public static VoxelShape fromAabbs(AABB first, AABB... rest) {
        Objects.requireNonNull(first, "first");
        VoxelShape result = Shapes.create(first);
        Objects.requireNonNull(rest, "rest");
        for (AABB box : rest) {
            result = Shapes.or(result, Shapes.create(Objects.requireNonNull(box, "box")));
        }
        return result;
    }

    public static VoxelShape or(VoxelShape first, VoxelShape second, VoxelShape... rest) {
        Objects.requireNonNull(first, "first");
        Objects.requireNonNull(second, "second");
        VoxelShape result = Shapes.or(first, second);
        Objects.requireNonNull(rest, "rest");
        for (VoxelShape shape : rest) {
            result = Shapes.or(result, Objects.requireNonNull(shape, "shape"));
        }
        return result;
    }

    private static void requireRange(String name, double value) {
        if (!Double.isFinite(value) || value < 0.0D || value > 16.0D) {
            throw new IllegalArgumentException(name + " must be finite and inside [0, 16]");
        }
    }

    private static void requireThickness(double value) {
        if (!Double.isFinite(value) || value <= 0.0D || value > 16.0D) {
            throw new IllegalArgumentException("thickness must be finite and inside (0, 16]");
        }
    }
}
