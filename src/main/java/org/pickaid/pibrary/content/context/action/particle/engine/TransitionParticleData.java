package org.pickaid.pibrary.content.context.action.particle.engine;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.pickaid.pibrary.content.context.action.engine.context.EngineContext;
import org.pickaid.pibrary.content.context.variable.ColorVariable;
import org.pickaid.pibrary.content.context.action.particle.render.ParticleRenderer;
import org.pickaid.pibrary.content.context.action.particle.render.TransitionParticleSprite;
import org.pickaid.pibrary.init.LibraryRegistries;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public record TransitionParticleData(
		RenderTypePreset renderType, ColorVariable start, ColorVariable end
) implements ParticleRenderData<TransitionParticleData> {

	public static final Codec<TransitionParticleData> CODEC = RecordCodecBuilder.create(i -> i.group(
			RenderTypePreset.CODEC.fieldOf("renderType").forGetter(e -> e.renderType),
			ColorVariable.CODEC.fieldOf("start").forGetter(e -> e.start),
			ColorVariable.CODEC.fieldOf("end").forGetter(e -> e.end)
	).apply(i, TransitionParticleData::new));

	@Override
	public ParticleRenderType<TransitionParticleData> type() {
		return LibraryRegistries.TRANSITION_RENDER.get();
	}

	@OnlyIn(Dist.CLIENT)
	@Override
	public ParticleRenderer resolve(EngineContext ctx) {
		return new TransitionParticleSprite(renderType, start.eval(ctx), end.eval(ctx));
	}

}
