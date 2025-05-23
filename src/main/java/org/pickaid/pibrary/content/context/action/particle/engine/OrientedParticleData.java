package org.pickaid.pibrary.content.context.action.particle.engine;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.pickaid.pibrary.content.context.action.engine.context.BuilderContext;
import org.pickaid.pibrary.content.context.action.engine.context.EngineContext;
import org.pickaid.pibrary.content.context.variable.DoubleVariable;
import org.pickaid.pibrary.content.context.action.particle.render.OrientedSpriteRenderer;
import org.pickaid.pibrary.content.context.action.particle.render.ParticleRenderer;
import org.pickaid.pibrary.init.LibraryObjects;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;
import java.util.Set;

public record OrientedParticleData(
		ParticleRenderData<?> inner,
		DoubleVariable roll,
		boolean fixFacing
) implements ParticleRenderData<OrientedParticleData> {

	public static final Codec<OrientedParticleData> CODEC = RecordCodecBuilder.create(i -> i.group(
			ParticleRenderData.CODEC.fieldOf("inner").forGetter(e -> e.inner),
			DoubleVariable.optionalCodec("roll", e -> e.roll),
			Codec.BOOL.optionalFieldOf("fixFacing").forGetter(e -> Optional.of(e.fixFacing))
	).apply(i, (c, r, f) -> new OrientedParticleData(c, r.orElse(DoubleVariable.ZERO), f.orElse(false))));

	@Override
	public ParticleRenderType<OrientedParticleData> type() {
		return LibraryObjects.ORIENTED_RENDER.get();
	}

	@Override
	public ParticleRenderer resolve(EngineContext ctx) {
		return new OrientedSpriteRenderer(inner().resolve(ctx),
				fixFacing ? ctx.loc().dir() : Vec3.ZERO,
				t -> roll.eval(ctx.withParam("Age", t)));
	}

	@Override
	public boolean verify(BuilderContext ctx) {
		return inner.verify(ctx.of("inner")) & roll.verify(ctx.of("roll", Set.of("Age")));
	}
}
