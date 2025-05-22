package org.pickaid.pibrary.content.context.conditions;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.phys.Vec3;
import org.pickaid.pibrary.content.context.Context;
import org.pickaid.pibrary.content.context.conditions.predicates.LocationPredicate;
import org.pickaid.pibrary.content.context.params.ContextParam;

import java.util.Optional;

/**
 * Used for checking the location.
 * Context Set {@link ContextParamSet#LOCATION}
 *
 * @param offset   offset
 * @param predicate location predicate
 */
public record LocationCheck(BlockPos offset, Optional<LocationPredicate> predicate) implements ContextPredicate {
	private static final MapCodec<BlockPos> OFFSET_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
			Codec.INT.optionalFieldOf("offsetX", 0).forGetter(Vec3i::getX),
			Codec.INT.optionalFieldOf("offsetY", 0).forGetter(Vec3i::getY),
			Codec.INT.optionalFieldOf("offsetZ", 0).forGetter(Vec3i::getZ)
	).apply(instance, BlockPos::new));
	
	public static final Codec<LocationCheck> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			OFFSET_CODEC.forGetter(LocationCheck::offset),
			LocationPredicate.CODEC.optionalFieldOf("predicate").forGetter(LocationCheck::predicate)
	).apply(instance, LocationCheck::new));
	
	@Override
	public boolean test(Context context) {
		Vec3 vec3 = context.getParameterOrNull(ContextParam.LOCATION);
		return vec3 != null && (this.predicate.isEmpty() || this.predicate.get().matches(context.getLevel(), vec3.x() + this.offset.getX(), vec3.y() + this.offset.getY(), vec3.z() + this.offset.getZ()));
	}
	
	@Override
	public ContextConditionType<?> type() {
		return ContextConditionType.LOCATION_CHECK.get();
	}
	
}
