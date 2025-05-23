package org.pickaid.pibrary.content.context.action.entity.motion;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.pickaid.pibrary.api.fastprojectileapi.entity.ProjectileMovement;
import org.pickaid.pibrary.content.context.action.engine.context.EngineContext;
import org.pickaid.pibrary.content.context.variable.DoubleVariable;
import org.pickaid.pibrary.content.context.action.entity.core.Motion;
import org.pickaid.pibrary.content.context.action.entity.core.MotionType;
import org.pickaid.pibrary.init.LibraryObjects;
import net.minecraft.world.phys.Vec3;

public record SimpleMotion(
		DoubleVariable friction,
		DoubleVariable gravity
) implements Motion<SimpleMotion> {

	public static final Codec<SimpleMotion> CODEC = RecordCodecBuilder.create(i -> i.group(
			DoubleVariable.optionalCodec("friction", e -> e.friction),
			DoubleVariable.optionalCodec("gravity", e -> e.gravity)
	).apply(i, (f, g) -> new SimpleMotion(
			f.orElse(DoubleVariable.ZERO),
			g.orElse(DoubleVariable.ZERO)
	)));

	public static final SimpleMotion ZERO = new SimpleMotion(DoubleVariable.ZERO, DoubleVariable.ZERO);
	public static final SimpleMotion DUST = new SimpleMotion(DoubleVariable.ofVerified("0.02"), DoubleVariable.ZERO);
	public static final SimpleMotion BREAKING = new SimpleMotion(DoubleVariable.ofVerified("0.02"), DoubleVariable.ofVerified("0.04"));

	@Override
	public MotionType<SimpleMotion> type() {
		return LibraryObjects.SIMPLE_MOTION.get();
	}

	@Override
	public ProjectileMovement move(EngineContext ctx, Vec3 vec, Vec3 pos) {
		double f = friction.eval(ctx);
		double g = gravity.eval(ctx);
		return ProjectileMovement.of(vec.scale(1 - f).add(0, -g, 0));
	}

}
