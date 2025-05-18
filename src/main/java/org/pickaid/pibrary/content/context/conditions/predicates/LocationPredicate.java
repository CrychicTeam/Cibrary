package org.pickaid.pibrary.content.context.action.conditions.predicates;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import org.pickaid.pibrary.content.context.action.conditions.predicates.params.BiomePredicate;
import org.pickaid.pibrary.content.context.action.conditions.predicates.params.LightPredicate;
import org.pickaid.pibrary.content.context.action.conditions.predicates.params.PositionPredicate;
import org.pickaid.pibrary.content.context.action.conditions.predicates.params.StructurePredicate;

public record LocationPredicate(PositionPredicate position, BiomePredicate biome, StructurePredicate structure, DimensionPredicate dimension, LightPredicate light, BlockPredicate block, FluidPredicate fluid) {
	public static final LocationPredicate ANY = new LocationPredicate(PositionPredicate.ANY, BiomePredicate.ANY, StructurePredicate.ANY, DimensionPredicate.ANY, LightPredicate.ANY, BlockPredicate.ANY, FluidPredicate.ANY);
	public static final Codec<LocationPredicate> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			PositionPredicate.CODEC.optionalFieldOf("position", PositionPredicate.ANY).forGetter(LocationPredicate::position),
			BiomePredicate.CODEC.optionalFieldOf("biomes", BiomePredicate.ANY).forGetter(LocationPredicate::biome),
			StructurePredicate.CODEC.optionalFieldOf("structures", StructurePredicate.ANY).forGetter(LocationPredicate::structure),
			DimensionPredicate.CODEC.optionalFieldOf("dimension", DimensionPredicate.ANY).forGetter(LocationPredicate::dimension),
			LightPredicate.CODEC.optionalFieldOf("light", LightPredicate.ANY).forGetter(LocationPredicate::light),
			BlockPredicate.CODEC.optionalFieldOf("block", BlockPredicate.ANY).forGetter(LocationPredicate::block),
			FluidPredicate.CODEC.optionalFieldOf("fluid", FluidPredicate.ANY).forGetter(LocationPredicate::fluid)
	).apply(instance, LocationPredicate::new));
	
	public boolean matches(ServerLevel level, double x, double y, double z) {
		if (this == ANY) {
			return true;
		}
		BlockPos pos = BlockPos.containing(x, y, z);
		if (!level.isLoaded(pos)) {
			return false;
		}
		if (!position.matches(x, y, z)) {
			return false;
		}
		if (!biome.matches(level, pos)) {
			return false;
		}
		if (!structure.matches(level, pos)) {
			return false;
		}
		if (!dimension.matches(level)) {
			return false;
		}
		if (!block.matches(level, pos)) {
			return false;
		}
		if (!fluid.matches(level, pos)) {
			return false;
		}
		return true;
	}
	
}
