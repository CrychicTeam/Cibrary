package org.pickaid.pibrary.tools.helper;

import com.endertech.minecraft.forge.blocks.ForgeBlock;
import com.endertech.minecraft.forge.math.GameMath;
import com.endertech.minecraft.forge.math.Vect3d;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.common.Tags;

import java.util.Optional;

public class LevelReaderUtils {
    public static boolean isBlockLoaded(LevelReader level, BlockPos pos) {
        return level.hasChunkAt(pos);
    }

    public static boolean isAirBlock(LevelReader level, BlockPos pos) {
        return level.getBlockState(pos).isAir();
    }

    public static boolean isGlassBlock(LevelReader level, BlockPos pos) {
        return ForgeBlock.isGlass(level.getBlockState(pos));
    }

    public static boolean isLiquidBlock(LevelReader level, BlockPos pos) {
        return ForgeBlock.isLiquid(level.getBlockState(pos));
    }

    public static boolean isWaterBlock(LevelReader level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return ForgeBlock.isLiquid(state) && state.getFluidState().is(FluidTags.WATER);
    }

    public static boolean isWaterSource(LevelReader level, BlockPos pos) {
        FluidState state = level.getFluidState(pos);
        return state.isSource() && state.is(FluidTags.WATER);
    }

    public static boolean isLavaBlock(LevelReader level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return ForgeBlock.isLiquid(state) && state.getFluidState().is(FluidTags.LAVA);
    }

    public static boolean isLavaSource(LevelReader level, BlockPos pos) {
        FluidState state = level.getFluidState(pos);
        return state.isSource() && state.is(FluidTags.LAVA);
    }

    public static boolean isOreBlock(LevelReader level, BlockPos pos, BlockState state) {
        return state.is(Tags.Blocks.ORES);
    }

    public static boolean isServerSide(LevelReader level) {
        return !level.isClientSide();
    }

    public static boolean isClientSide(LevelReader level) {
        return level.isClientSide();
    }

    public static Optional<Entity> getEntity(Level level, int id) {
        return level != null ? Optional.ofNullable(level.getEntity(id)) : Optional.empty();
    }

    public static Vect3d getBlockCenter(BlockPos pos) {
        return Vect3d.from(pos).add(GameMath.getBBCenter(ForgeBlock.FULL_BLOCK_AABB));
    }

    public static void spawnParticle(Level level, Vect3d pos, Vect3d motion, ParticleOptions particleData) {
        level.addParticle(particleData, pos.x, pos.y, pos.z, motion.x, motion.y, motion.z);
    }

    public static Optional<BlockHitResult> rayTraceBlocks(Level level, Vect3d start, Vect3d end, ClipContext.Block blockMode, ClipContext.Fluid fluidMode, Entity entity) {
        ClipContext context = new ClipContext(start.toVector3d(), end.toVector3d(), blockMode, fluidMode, entity);
        BlockHitResult hit = level.clip(context);
        return hit != null && hit.getType() != HitResult.Type.MISS ? Optional.of(hit) : Optional.empty();
    }

    public static boolean isWithinBuildHeight(LevelReader level, BlockPos pos) {
        return pos.getY() >= level.getMinBuildHeight() && pos.getY() < level.getMaxBuildHeight();
    }

    public static int getLightLevel(LevelReader level, BlockPos pos) {
        return level.getMaxLocalRawBrightness(pos);
    }

    public static boolean canSeeSky(LevelReader level, BlockPos pos) {
        return level.canSeeSky(pos);
    }

    public static Holder<Biome> getBiome(LevelReader level, BlockPos pos) {
        return level.getBiome(pos);
    }

    public static boolean isAreaLoaded(LevelReader level, BlockPos center, int range) {
        return level.hasChunksAt(center.offset(-range, -range, -range), center.offset(range, range, range));
    }

    public static int getHeightmapTop(LevelReader level, Heightmap.Types heightmapType, int x, int z) {
        return level.getHeight(heightmapType, x, z);
    }

    public static boolean isOcean(LevelReader level, BlockPos pos) {
        return level.getFluidState(pos).is(FluidTags.WATER) && level.getBiome(pos).is(BiomeTags.IS_OCEAN);
    }

    public static int getSeaLevel(LevelReader level) {
        return level.getSeaLevel();
    }

    public static boolean isBelowSeaLevel(LevelReader level, BlockPos pos) {
        return pos.getY() < level.getSeaLevel();
    }

    public static boolean inDimension(Player player, String dimensionName) {
        return player.level().dimension().location().equals(new ResourceLocation(dimensionName));
    }
}
