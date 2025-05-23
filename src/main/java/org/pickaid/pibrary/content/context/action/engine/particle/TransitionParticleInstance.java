package org.pickaid.pibrary.content.context.action.engine.particle;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.pickaid.pibrary.content.context.action.engine.context.EngineContext;
import org.pickaid.pibrary.content.context.action.engine.core.EngineType;
import org.pickaid.pibrary.content.context.variable.ColorVariable;
import org.pickaid.pibrary.content.context.variable.DoubleVariable;
import org.pickaid.pibrary.content.context.variable.IntVariable;
import org.pickaid.pibrary.content.context.action.entity.motion.SimpleMotion;
import org.pickaid.pibrary.content.context.action.particle.core.ClientParticleData;
import org.pickaid.pibrary.content.context.action.particle.core.PiGenericParticleOption;
import org.pickaid.pibrary.content.context.action.particle.engine.RenderTypePreset;
import org.pickaid.pibrary.content.context.action.particle.render.RandomColorParticle;
import org.pickaid.pibrary.content.context.action.particle.render.TransitionParticleSprite;
import org.pickaid.pibrary.init.LibraryObjects;
import net.minecraft.core.particles.ParticleOptions;

public record TransitionParticleInstance(
		ColorVariable start, ColorVariable end,
		DoubleVariable scale, DoubleVariable speed,
		IntVariable life)
		implements ParticleInstance<TransitionParticleInstance>, RandomColorParticle {

	public static final Codec<TransitionParticleInstance> CODEC = RecordCodecBuilder.create(i -> i.group(
			ColorVariable.CODEC.fieldOf("start").forGetter(e -> e.start),
			ColorVariable.CODEC.fieldOf("end").forGetter(e -> e.end),
			DoubleVariable.codec("scale", TransitionParticleInstance::scale),
			DoubleVariable.codec("speed", ParticleInstance::speed),
			IntVariable.codec("life", e -> e.life)
	).apply(i, TransitionParticleInstance::new));

	@Override
	public EngineType<TransitionParticleInstance> type() {
		return LibraryObjects.TRANSITION_PARTICLE.get();
	}

	@Override
	public ParticleOptions particle(EngineContext ctx) {
		int life = life().eval(ctx);
		float scale = (float) scale().eval(ctx) * ClientParticleData.randSize(ctx);
		return new PiGenericParticleOption(new ClientParticleData(life, true, scale,
				ctx, SimpleMotion.DUST, new TransitionParticleSprite(
				RenderTypePreset.NORMAL,
				randomizeColor(ctx.rand(), start.eval(ctx)),
				randomizeColor(ctx.rand(), end.eval(ctx))
		)));
	}

}
