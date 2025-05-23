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
import org.pickaid.pibrary.content.context.action.particle.render.DustParticleSprite;
import org.pickaid.pibrary.content.context.action.particle.render.RandomColorParticle;
import org.pickaid.pibrary.init.LibraryObjects;
import net.minecraft.core.particles.ParticleOptions;

public record DustParticleInstance(
		ColorVariable color,
		DoubleVariable scale,
		DoubleVariable speed,
		IntVariable life
) implements ParticleInstance<DustParticleInstance>, RandomColorParticle {

	public static final Codec<DustParticleInstance> CODEC = RecordCodecBuilder.create(i -> i.group(
			ColorVariable.CODEC.fieldOf("color").forGetter(e -> e.color),
			DoubleVariable.codec("scale", DustParticleInstance::scale),
			DoubleVariable.codec("speed", ParticleInstance::speed),
			IntVariable.codec("life", e -> e.life)
	).apply(i, DustParticleInstance::new));

	@Override
	public EngineType<DustParticleInstance> type() {
		return LibraryObjects.DUST_PARTICLE.get();
	}

	@Override
	public ParticleOptions particle(EngineContext ctx) {
		int life = life().eval(ctx);
		float scale = (float) scale().eval(ctx) * ClientParticleData.randSize(ctx);
		return new PiGenericParticleOption(new ClientParticleData(life, true, scale,
				ctx, SimpleMotion.DUST, new DustParticleSprite(RenderTypePreset.NORMAL,
				randomizeColor(ctx.rand(), color.eval(ctx)))));
	}

}
