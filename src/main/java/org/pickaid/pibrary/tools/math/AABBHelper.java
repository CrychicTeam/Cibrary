package org.pickaid.pibrary.tools.math;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Provides utility methods for working with Minecraft Axis-Aligned Bounding Boxes (AABB)
 * and spatial calculations for blocks and entities.
 */
public class AABBHelper {
    public static final AABB EMPTY_BOUNDS = new AABB(0.0, 0.0, 0.0, 0.0, 0.0, 0.0);
    public static final AABB FULL_BLOCK_BOUNDS = new AABB(0.0, 0.0, 0.0, 1.0, 1.0, 1.0);
    public static final int STANDARD_BLOCK_SIZE = 1;

    public static final double THIN_FACE = 0.0625;
    public static final double MEDIUM_FACE = 0.125;
    public static final double THICK_FACE = 0.25;

    /**
     * Generates a list of AABBs for the 12 edges of a block
     *
     * @param thickness Edge thickness (0-1)
     * @return List of AABBs representing edges
     */
    public static List<AABB> generateEdgeBounds(double thickness) {
        if (thickness <= 0 || thickness >= 1) {
            throw new IllegalArgumentException("Thickness must be between 0 and 1");
        }

        double d1 = 1.0 - thickness;

        AABB xAxisEdge = new AABB(0.0, 0.0, 0.0, 1.0, thickness, thickness);
        AABB yAxisEdge = new AABB(0.0, 0.0, 0.0, thickness, 1.0, thickness);
        AABB zAxisEdge = new AABB(0.0, 0.0, 0.0, thickness, thickness, 1.0);

        List<AABB> allEdges = new ArrayList<>(12);

        allEdges.add(xAxisEdge);
        allEdges.add(xAxisEdge.move(0, d1, 0));
        allEdges.add(xAxisEdge.move(0, 0, d1));
        allEdges.add(xAxisEdge.move(0, d1, d1));

        allEdges.add(yAxisEdge);
        allEdges.add(yAxisEdge.move(d1, 0, 0));
        allEdges.add(yAxisEdge.move(0, 0, d1));
        allEdges.add(yAxisEdge.move(d1, 0, d1));

        allEdges.add(zAxisEdge);
        allEdges.add(zAxisEdge.move(0, d1, 0));
        allEdges.add(zAxisEdge.move(d1, 0, 0));
        allEdges.add(zAxisEdge.move(d1, d1, 0));

        return Collections.unmodifiableList(allEdges);
    }

    /**
     * Creates a bounding box for a face in a specific direction
     *
     * @param thickness Face thickness
     * @param facing Direction the face is pointing
     * @return AABB for the corresponding face
     */
    public static AABB createFaceBounds(double thickness, Direction facing) {
        if (thickness <= 0 || thickness >= 1) {
            throw new IllegalArgumentException("Thickness must be between 0 and 1");
        }

        if (facing == null) {
            return EMPTY_BOUNDS;
        }

        double d1 = 1.0 - thickness;

        return switch (facing) {
            case DOWN -> new AABB(0.0, 0.0, 0.0, 1.0, thickness, 1.0);
            case UP -> new AABB(0.0, d1, 0.0, 1.0, 1.0, 1.0);
            case WEST -> new AABB(0.0, 0.0, 0.0, thickness, 1.0, 1.0);
            case EAST -> new AABB(d1, 0.0, 0.0, 1.0, 1.0, 1.0);
            case NORTH -> new AABB(0.0, 0.0, 0.0, 1.0, 1.0, thickness);
            case SOUTH -> new AABB(0.0, 0.0, d1, 1.0, 1.0, 1.0);
        };
    }

    /**
     * Creates a list of bounding boxes for faces in multiple directions
     *
     * @param thickness Face thickness
     * @param facings Array of directions
     * @return List of AABBs for all requested direction faces
     */
    public static List<AABB> createFaceBounds(double thickness, Direction... facings) {
        if (facings == null || facings.length == 0) {
            return Collections.emptyList();
        }

        return Arrays.stream(facings)
                .map(facing -> createFaceBounds(thickness, facing))
                .toList();
    }

    /**
     * Creates bounding boxes for the four horizontal walls
     *
     * @param thickness Wall thickness
     * @return List of AABBs for the four horizontal direction walls
     */
    public static List<AABB> createWallBounds(double thickness) {
        return createFaceBounds(thickness,
                Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST);
    }

    /**
     * Checks if an AABB fully blocks movement along a specified axis
     *
     * @param bounds AABB to check
     * @param axis Axis to check
     * @return true if the AABB fully blocks movement along the specified axis
     */
    public static boolean isFullyBlockingOnAxis(AABB bounds, Direction.Axis axis) {
        if (bounds == null) {
            return false;
        }

        return switch (axis) {
            case X -> (bounds.maxY - bounds.minY >= 1.0) && (bounds.maxZ - bounds.minZ >= 1.0);
            case Y -> (bounds.maxX - bounds.minX >= 1.0) && (bounds.maxZ - bounds.minZ >= 1.0);
            case Z -> (bounds.maxX - bounds.minX >= 1.0) && (bounds.maxY - bounds.minY >= 1.0);
        };
    }

    /**
     * Checks if an AABB fully blocks movement in a specified direction
     *
     * @param bounds AABB to check
     * @param facing Direction to check
     * @return true if the AABB fully blocks movement in the specified direction
     */
    public static boolean isFullyBlockingFace(AABB bounds, Direction facing) {
        if (bounds == null || facing == null) {
            return false;
        }

        if (!isFullyBlockingOnAxis(bounds, facing.getAxis())) {
            return false;
        }

        boolean isNegative = facing.getAxisDirection() == Direction.AxisDirection.NEGATIVE;

        return switch (facing.getAxis()) {
            case X -> isNegative ? bounds.minX <= 0.0001 : bounds.maxX >= 0.9999;
            case Y -> isNegative ? bounds.minY <= 0.0001 : bounds.maxY >= 0.9999;
            case Z -> isNegative ? bounds.minZ <= 0.0001 : bounds.maxZ >= 0.9999;
        };
    }

    /**
     * Merges multiple AABBs into a single AABB containing all original AABBs
     *
     * @param bounds Collection of AABBs to merge
     * @return Minimal AABB containing all input AABBs
     */
    public static AABB unionBounds(List<AABB> bounds) {
        if (bounds == null || bounds.isEmpty()) {
            return EMPTY_BOUNDS;
        }

        AABB result = bounds.get(0);
        for (int i = 1; i < bounds.size(); i++) {
            result = result.minmax(bounds.get(i));
        }

        return result;
    }

    /**
     * Creates a scaled AABB
     *
     * @param bounds Original AABB
     * @param scale Scale factor (0-1 for shrinking, >1 for expansion)
     * @return Scaled AABB
     */
    public static AABB scaleBounds(AABB bounds, double scale) {
        if (bounds == null) {
            return EMPTY_BOUNDS;
        }

        double centerX = (bounds.minX + bounds.maxX) / 2;
        double centerY = (bounds.minY + bounds.maxY) / 2;
        double centerZ = (bounds.minZ + bounds.maxZ) / 2;

        double sizeX = (bounds.maxX - bounds.minX) * scale / 2;
        double sizeY = (bounds.maxY - bounds.minY) * scale / 2;
        double sizeZ = (bounds.maxZ - bounds.minZ) * scale / 2;

        return new AABB(
                centerX - sizeX, centerY - sizeY, centerZ - sizeZ,
                centerX + sizeX, centerY + sizeY, centerZ + sizeZ
        );
    }

    /**
     * Creates an AABB around an entity with specified padding
     *
     * @param entity The entity to create bounds around
     * @param padding Extra space to add in all directions
     * @return AABB surrounding the entity with padding
     */
    public static AABB createEntityBounds(Entity entity, double padding) {
        if (entity == null) {
            return EMPTY_BOUNDS;
        }

        AABB entityBounds = entity.getBoundingBox();
        if (padding == 0) {
            return entityBounds;
        }

        return entityBounds.inflate(padding, padding, padding);
    }

    /**
     * Creates an AABB at a specified Vec3 position with given dimensions
     *
     * @param position Center position for the AABB
     * @param width Width (x-axis) of the AABB
     * @param height Height (y-axis) of the AABB
     * @param depth Depth (z-axis) of the AABB
     * @return Newly created AABB
     */
    public static AABB createBoundsFromVec3(Vec3 position, double width, double height, double depth) {
        if (position == null) {
            return EMPTY_BOUNDS;
        }

        double halfWidth = width / 2;
        double halfHeight = height / 2;
        double halfDepth = depth / 2;

        return new AABB(
                position.x - halfWidth, position.y - halfHeight, position.z - halfDepth,
                position.x + halfWidth, position.y + halfHeight, position.z + halfDepth
        );
    }

    /**
     * Converts a Vec3 position to a block-aligned AABB
     *
     * @param position The Vec3 position to convert
     * @return Block-sized AABB at the position
     */
    public static AABB createBlockBoundsAtVec3(Vec3 position) {
        if (position == null) {
            return EMPTY_BOUNDS;
        }

        int x = (int)Math.floor(position.x);
        int y = (int)Math.floor(position.y);
        int z = (int)Math.floor(position.z);

        return new AABB(x, y, z, x + 1, y + 1, z + 1);
    }

    /**
     * Gets the center position of an AABB as a Vec3
     *
     * @param bounds The AABB to find the center of
     * @return Vec3 at the center of the AABB
     */
    public static Vec3 getBoundsCenter(AABB bounds) {
        if (bounds == null) {
            return Vec3.ZERO;
        }

        return new Vec3(
                (bounds.minX + bounds.maxX) / 2,
                (bounds.minY + bounds.maxY) / 2,
                (bounds.minZ + bounds.maxZ) / 2
        );
    }

    /**
     * Transforms an entity's rotation into a Direction
     *
     * @param entity The entity to get direction from
     * @return The closest cardinal direction the entity is facing
     */
    public static Direction getEntityFacingDirection(Entity entity) {
        int facingDirection = Mth.floor((entity.getYRot() * 4F) / 360F + 0.5D) & 3;
        return Direction.from2DDataValue(facingDirection);
    }

    /**
     * Gets all block positions contained within the AABB bounds
     *
     * @param bounds The AABB to get block positions from
     * @return List of all BlockPos within the AABB
     */
    public static List<BlockPos> getAllBlockPositionsWithin(AABB bounds) {
        if (bounds == null) {
            return Collections.emptyList();
        }

        List<BlockPos> positions = new ArrayList<>();
        iterateBlockPositions(bounds, positions::add);
        return positions;
    }

    /**
     * Gets all block positions within the AABB bounds that match the predicate
     *
     * @param bounds The AABB to get block positions from
     * @param filter Predicate to filter positions
     * @return List of matching BlockPos within the AABB
     */
    public static List<BlockPos> getFilteredBlockPositions(AABB bounds, Predicate<BlockPos> filter) {
        if (bounds == null || filter == null) {
            return Collections.emptyList();
        }

        List<BlockPos> positions = new ArrayList<>();
        iterateBlockPositions(bounds, pos -> {
            if (filter.test(pos)) {
                positions.add(pos.immutable());
            }
        });
        return positions;
    }

    /**
     * Efficiently iterates through all block positions within an AABB without creating intermediate collections
     *
     * @param bounds The AABB to iterate through
     * @param consumer Consumer that will receive each BlockPos
     */
    public static void iterateBlockPositions(AABB bounds, Consumer<BlockPos> consumer) {
        if (bounds == null || consumer == null) {
            return;
        }

        int minX = (int)Math.floor(bounds.minX);
        int minY = (int)Math.floor(bounds.minY);
        int minZ = (int)Math.floor(bounds.minZ);
        int maxX = (int)Math.floor(bounds.maxX);
        int maxY = (int)Math.floor(bounds.maxY);
        int maxZ = (int)Math.floor(bounds.maxZ);

        BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    consumer.accept(mutablePos.set(x, y, z));
                }
            }
        }
    }

    /**
     * Gets block positions in a specified direction from a source position
     *
     * @param source The source block position
     * @param direction The direction to get blocks in
     * @param distance How many blocks to include in that direction
     * @return List of BlockPos in the specified direction
     */
    public static List<BlockPos> getBlocksInDirection(BlockPos source, Direction direction, int distance) {
        if (source == null || direction == null || distance <= 0) {
            return Collections.emptyList();
        }

        List<BlockPos> positions = new ArrayList<>(distance);
        BlockPos.MutableBlockPos current = new BlockPos.MutableBlockPos();
        current.set(source);

        for (int i = 1; i <= distance; i++) {
            current.move(direction);
            positions.add(current.immutable());
        }

        return positions;
    }

    /**
     * Gets block positions in a spiral pattern (useful for searching outward)
     *
     * @param center The center block position
     * @param radius Maximum radius of the spiral
     * @return List of BlockPos in spiral order
     */
    public static List<BlockPos> getSpiralPositions(BlockPos center, int radius) {
        if (center == null || radius <= 0) {
            return Collections.emptyList();
        }

        List<BlockPos> positions = new ArrayList<>();
        positions.add(center);

        for (int r = 1; r <= radius; r++) {
            for (int x = -r + 1; x <= r; x++) {
                positions.add(center.offset(x, 0, -r));
            }
            for (int z = -r + 1; z <= r; z++) {
                positions.add(center.offset(r, 0, z));
            }
            for (int x = r - 1; x >= -r; x--) {
                positions.add(center.offset(x, 0, r));
            }
            for (int z = r - 1; z >= -r + 1; z--) {
                positions.add(center.offset(-r, 0, z));
            }
        }

        return positions;
    }

    /**
     * Gets block positions in a hollow shape (just the outer shell of the AABB)
     *
     * @param bounds The AABB to get the shell blocks from
     * @return List of BlockPos forming the outer shell
     */
    public static List<BlockPos> getShellBlockPositions(AABB bounds) {
        if (bounds == null) {
            return Collections.emptyList();
        }

        List<BlockPos> positions = new ArrayList<>();

        int minX = (int)Math.floor(bounds.minX);
        int minY = (int)Math.floor(bounds.minY);
        int minZ = (int)Math.floor(bounds.minZ);
        int maxX = (int)Math.floor(bounds.maxX);
        int maxY = (int)Math.floor(bounds.maxY);
        int maxZ = (int)Math.floor(bounds.maxZ);

        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                positions.add(pos.set(x, minY, z).immutable());
                if (minY != maxY) {
                    positions.add(pos.set(x, maxY, z).immutable());
                }
            }
        }

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY + 1; y <= maxY - 1; y++) {
                positions.add(pos.set(x, y, minZ).immutable());
                if (minZ != maxZ) {
                    positions.add(pos.set(x, y, maxZ).immutable());
                }
            }
        }

        for (int z = minZ + 1; z <= maxZ - 1; z++) {
            for (int y = minY + 1; y <= maxY - 1; y++) {
                positions.add(pos.set(minX, y, z).immutable());
                if (minX != maxX) {
                    positions.add(pos.set(maxX, y, z).immutable());
                }
            }
        }

        return positions;
    }

    /**
     * Creates a sphere of block positions around a center point
     *
     * @param center The center position
     * @param radius The radius of the sphere
     * @param hollow Whether to make a hollow sphere (shell only)
     * @return List of BlockPos forming the sphere
     */
    public static List<BlockPos> createSphere(BlockPos center, double radius, boolean hollow) {
        if (center == null || radius <= 0) {
            return Collections.emptyList();
        }

        List<BlockPos> positions = new ArrayList<>();
        int intRadius = (int)Math.ceil(radius);

        AABB bounds = new AABB(
                center.getX() - intRadius, center.getY() - intRadius, center.getZ() - intRadius,
                center.getX() + intRadius, center.getY() + intRadius, center.getZ() + intRadius
        );

        Vec3 centerVec = new Vec3(center.getX() + 0.5, center.getY() + 0.5, center.getZ() + 0.5);
        double radiusSq = radius * radius;
        double innerRadiusSq = hollow ? (radius - 1) * (radius - 1) : -1;

        iterateBlockPositions(bounds, pos -> {
            double distSq = distanceSquared(
                    centerVec,
                    new Vec3(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5)
            );

            if (distSq <= radiusSq && (!hollow || distSq >= innerRadiusSq)) {
                positions.add(pos.immutable());
            }
        });

        return positions;
    }

    /**
     * Creates a cylinder of block positions
     *
     * @param center The center position
     * @param radius The radius of the cylinder
     * @param height The height of the cylinder
     * @param axis The axis the cylinder extends along
     * @return List of BlockPos forming the cylinder
     */
    public static List<BlockPos> createCylinder(BlockPos center, double radius, int height, Direction.Axis axis) {
        if (center == null || radius <= 0 || height <= 0) {
            return Collections.emptyList();
        }

        List<BlockPos> positions = new ArrayList<>();
        int intRadius = (int)Math.ceil(radius);
        int halfHeight = height / 2;

        AABB bounds;
        switch (axis) {
            case X:
                bounds = new AABB(
                        center.getX() - halfHeight, center.getY() - intRadius, center.getZ() - intRadius,
                        center.getX() + halfHeight, center.getY() + intRadius, center.getZ() + intRadius
                );
                break;
            case Y:
                bounds = new AABB(
                        center.getX() - intRadius, center.getY() - halfHeight, center.getZ() - intRadius,
                        center.getX() + intRadius, center.getY() + halfHeight, center.getZ() + intRadius
                );
                break;
            case Z:
                bounds = new AABB(
                        center.getX() - intRadius, center.getY() - intRadius, center.getZ() - halfHeight,
                        center.getX() + intRadius, center.getY() + intRadius, center.getZ() + halfHeight
                );
                break;
            default:
                return Collections.emptyList();
        }

        double radiusSq = radius * radius;

        iterateBlockPositions(bounds, pos -> {
            double distSq;
            switch (axis) {
                case X:
                    distSq = distanceSquared2D(
                            center.getY() + 0.5, center.getZ() + 0.5,
                            pos.getY() + 0.5, pos.getZ() + 0.5
                    );
                    break;
                case Y:
                    distSq = distanceSquared2D(
                            center.getX() + 0.5, center.getZ() + 0.5,
                            pos.getX() + 0.5, pos.getZ() + 0.5
                    );
                    break;
                case Z:
                    distSq = distanceSquared2D(
                            center.getX() + 0.5, center.getY() + 0.5,
                            pos.getX() + 0.5, pos.getY() + 0.5
                    );
                    break;
                default:
                    return;
            }

            if (distSq <= radiusSq) {
                positions.add(pos.immutable());
            }
        });

        return positions;
    }

    private static double distanceSquared(Vec3 pos1, Vec3 pos2) {
        double dx = pos1.x - pos2.x;
        double dy = pos1.y - pos2.y;
        double dz = pos1.z - pos2.z;
        return dx * dx + dy * dy + dz * dz;
    }

    private static double distanceSquared2D(double x1, double y1, double x2, double y2) {
        double dx = x1 - x2;
        double dy = y1 - y2;
        return dx * dx + dy * dy;
    }
}