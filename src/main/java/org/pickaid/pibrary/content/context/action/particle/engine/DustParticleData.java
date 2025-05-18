package org.pickaid.pibrary.content.context.action.particle.engine;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.pickaid.pibrary.content.context.action.engine.context.EngineContext;
import org.pickaid.pibrary.content.context.variable.ColorVariable;
import org.pickaid.pibrary.content.context.action.particle.render.DustParticleSprite;
import org.pickaid.pibrary.content.context.action.particle.render.ParticleRenderer;
import org.pickaid.pibrary.init.LibraryRegistries;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public record DustParticleData(
		RenderTypePreset renderType, ColorVariable color
) implements ParticleRenderData<DustParticleData> {

	public static final Codec<DustParticleData> CODEC = RecordCodecBuilder.create(i -> i.group(
			RenderTypePreset.CODEC.fieldOf("renderType").forGetter(e -> e.renderType),
			ColorVariable.CODEC.fieldOf("color").forGetter(e -> e.color)
	).apply(i, DustParticleData::new));

	@Override
	public ParticleRenderType<DustParticleData> type() {
		return LibraryRegistries.COLOR_RENDER.get();
	}

	@OnlyIn(Dist.CLIENT)
	@Override
	public ParticleRenderer resolve(EngineContext ctx) {
		return new DustParticleSprite(renderType, color.eval(ctx));
	}

}
