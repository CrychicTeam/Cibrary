package org.pickaid.pibrary.content.context.action.particle.engine;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.pickaid.pibrary.content.context.action.engine.context.BuilderContext;
import org.pickaid.pibrary.content.context.action.engine.context.EngineContext;
import org.pickaid.pibrary.content.context.action.engine.core.EngineType;
import org.pickaid.pibrary.content.context.action.engine.particle.ParticleInstance;
import org.pickaid.pibrary.content.context.variable.DoubleVariable;
import org.pickaid.pibrary.content.context.variable.IntVariable;
import org.pickaid.pibrary.content.context.action.entity.core.Motion;
import org.pickaid.pibrary.content.context.action.entity.motion.SimpleMotion;
import org.pickaid.pibrary.content.context.action.particle.core.ClientParticleData;
import org.pickaid.pibrary.content.context.action.particle.core.PiGenericParticleOption;
import org.pickaid.pibrary.init.LibraryObjects;
import net.minecraft.core.particles.ParticleOptions;

import java.util.Optional;
import java.util.Set;

public record CustomParticleInstance(
		DoubleVariable speed, DoubleVariable scale, IntVariable life, boolean collide,
		Motion<?> motion, ParticleRenderData<?> renderer
) implements ParticleInstance<CustomParticleInstance> {

	public static final Codec<CustomParticleInstance> CODEC = RecordCodecBuilder.create(i -> i.group(
			DoubleVariable.codec("speed", ParticleInstance::speed),
			DoubleVariable.optionalCodec("scale", e -> e.scale),
			IntVariable.optionalCodec("life", e -> e.life),
			Codec.BOOL.optionalFieldOf("collide").forGetter(e -> Optional.of(e.collide)),
			Motion.CODEC.optionalFieldOf("motion").forGetter(e -> Optional.of(e.motion)),
			ParticleRenderData.CODEC.fieldOf("renderer").forGetter(e -> e.renderer)
	).apply(i, (a, b, c, d, e, f) -> new CustomParticleInstance(
			a, b.orElse(DoubleVariable.of("rand(0.1,0.2)")),
			c.orElse(IntVariable.of("40/rand(1,10)")),
			d.orElse(true),
			e.orElse(SimpleMotion.DUST), f)));

	@Override
	public EngineType<CustomParticleInstance> type() {
		return LibraryObjects.CUSTOM_PARTICLE.get();
	}

	@Override
	public ParticleOptions particle(EngineContext ctx) {
		return new PiGenericParticleOption(new ClientParticleData(
				life.eval(ctx), collide, (float) scale.eval(ctx), ctx,
				motion, renderer.resolve(ctx)
		));
	}

	@Override
	public boolean verify(BuilderContext ctx) {
		speed.verify(ctx.of("speed"));
		scale.verify(ctx.of("scale"));
		life.verify(ctx.of("life"));
		motion.verify(ctx.of("motion", Set.of("TickCount")));
		renderer.verify(ctx.of("renderer"));
		return true;
	}
}
