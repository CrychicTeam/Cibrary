package org.pickaid.pibrary.tools.utils;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import org.pickaid.pibrary.tools.registry.TagsHelper;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class StructureUtils {

    /**
     * Checks if the specified position is within any structure
     *
     * @param world The server world instance
     * @param pos The block position to check
     * @return true if the position is within any structure, false otherwise
     */
    public static boolean isPointInStructure(ServerLevel world, BlockPos pos) {
        return world.structureManager().hasAnyStructureAt(pos);
    }

    /**
     * Checks if the specified position is within a specific part of a specific structure
     *
     * @param world The server world instance
     * @param pos The block position to check
     * @param structureID The resource ID of the structure
     * @param pieceIndex The structure piece index, if negative it checks all pieces
     * @return true if the position is within the specified structure, false otherwise
     */
    public static boolean isPointInStructure(ServerLevel world, BlockPos pos, ResourceLocation structureID, int pieceIndex) {
        Structure structure = TagsHelper.getStructure(world, structureID);
        if (structure == null) {
            return false;
        }
        return world.structureManager().getAllStructuresAt(pos).entrySet().stream()
                .filter(entry -> structure == entry.getKey())
                .anyMatch(entry -> {
                    StructureStart start = world.structureManager().getStructureAt(pos, entry.getKey());
                    if (start == null) {
                        return false;
                    }
                    if (start.getPieces().isEmpty()) {
                        return true;
                    }
                    if (pieceIndex < 0) {
                        return start.getPieces().stream()
                                .anyMatch(piece -> piece.getBoundingBox().isInside(pos));
                    } else if (pieceIndex < start.getPieces().size()) {
                        return start.getPieces().get(pieceIndex).getBoundingBox().isInside(pos);
                    }

                    return false;
                });
    }

    /**
     * Checks if the specified position is within any structure that belongs to a specific structure tag
     *
     * @param world The server world instance
     * @param pos The block position to check
     * @param structureTagID The resource ID of the structure tag
     * @return true if the position is within any structure matching the tag, false otherwise
     */
    public static boolean isPointInAnyStructure(ServerLevel world, BlockPos pos, ResourceLocation structureTagID) {
        List<Holder<Structure>> taggedStructures = TagsHelper.getStructureContents(world, structureTagID);
        if (taggedStructures.isEmpty()) {
            return false;
        }

        return world.structureManager().getAllStructuresAt(pos).entrySet().stream()
                .filter(entry -> taggedStructures.stream()
                        .anyMatch(structure -> structure.value() == entry.getKey()))
                .anyMatch(entry -> {
                    StructureStart start = world.structureManager().getStructureAt(pos, entry.getKey());
                    if (start == null) {
                        return false;
                    }
                    if (start.getPieces().isEmpty()) {
                        return true;
                    }
                    return start.getPieces().stream()
                            .anyMatch(piece -> piece.getBoundingBox().isInside(pos));
                });
    }

    /**
     * Gets the bounding box of a specific structure at the specified position
     *
     * @param world The server world instance
     * @param pos The block position
     * @param structureID The resource ID of the structure
     * @param subPieceIndex The structure piece index, if -1 returns the bounding box of the entire structure
     * @return The bounding box of the structure or specified piece, or null if not found
     */
    @Nullable
    public static BoundingBox getStructureBoundingBoxAt(ServerLevel world, BlockPos pos, ResourceLocation structureID, int subPieceIndex) {
        Structure structure = TagsHelper.getStructure(world, structureID);
        if (structure == null) {
            return null;
        }

        StructureStart start = world.structureManager().getStructureWithPieceAt(pos, structure);
        if (start == null || start.getPieces().isEmpty()) {
            return null;
        }

        if (subPieceIndex > -1 && subPieceIndex < start.getPieces().size()) {
            return start.getPieces().get(subPieceIndex).getBoundingBox();
        } else {
            return start.getBoundingBox();
        }
    }

    /**
     * Gets all available structure holders in the world
     *
     * @param world The server world instance
     * @return A list of all structure holders
     */
    public static List<Holder<Structure>> getAllStructures(ServerLevel world) {
        List<Holder<Structure>> result = new ArrayList();
        Registry<Structure> registry = world.registryAccess().registryOrThrow(Registries.STRUCTURE);
        registry.asHolderIdMap().forEach(result::add);
        return result;
    }

    /**
     * Gets all available structure IDs in the world
     *
     * @param world The server world instance
     * @return A list of all structure IDs
     */
    public static List<ResourceLocation> getAllStructureIDs(ServerLevel world) {
        Registry<Structure> registry = world.registryAccess().registryOrThrow(Registries.STRUCTURE);
        return registry.entrySet().stream()
                .map(entry -> entry.getKey().location())
                .collect(Collectors.toList());
    }

    /**
     * Gets the resource ID corresponding to a structure holder
     *
     * @param world The server world instance
     * @param structure The structure holder
     * @return The corresponding resource ID, or null if not bound
     */
    @Nullable
    public static ResourceLocation getStructureID(ServerLevel world, Holder<Structure> structure) {
        if (!structure.isBound()) {
            return null;
        }

        Registry<Structure> registry = world.registryAccess().registryOrThrow(Registries.STRUCTURE);
        return registry.getKey(structure.get());
    }
}