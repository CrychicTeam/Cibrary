package org.pickaid.pibrary.content.context.action.conditions.predicates.params;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.critereon.MinMaxBounds;
import org.pickaid.pibrary.tools.utils.CodecUtils;

public record PositionPredicate(MinMaxBounds.Doubles x, MinMaxBounds.Doubles y, MinMaxBounds.Doubles z)  {
	public static final PositionPredicate ANY = new PositionPredicate(MinMaxBounds.Doubles.ANY, MinMaxBounds.Doubles.ANY, MinMaxBounds.Doubles.ANY);
	public static final Codec<PositionPredicate> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			CodecUtils.BOUNDS_DOUBLES.optionalFieldOf("x", MinMaxBounds.Doubles.ANY).forGetter(PositionPredicate::x),
			CodecUtils.BOUNDS_DOUBLES.optionalFieldOf("y", MinMaxBounds.Doubles.ANY).forGetter(PositionPredicate::y),
			CodecUtils.BOUNDS_DOUBLES.optionalFieldOf("z", MinMaxBounds.Doubles.ANY).forGetter(PositionPredicate::z)
	).apply(instance, PositionPredicate::new));
	
	public boolean matches(double x, double y, double z) {
		return this.x.matches(x) && this.y.matches(y) && this.z.matches(z);
	}
}
