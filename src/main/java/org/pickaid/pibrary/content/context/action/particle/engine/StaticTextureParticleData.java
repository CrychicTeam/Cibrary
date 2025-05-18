package org.pickaid.pibrary.content.context.action.particle.engine;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.pickaid.pibrary.content.context.action.engine.context.EngineContext;
import org.pickaid.pibrary.content.context.action.particle.render.FixedParticleSprite;
import org.pickaid.pibrary.content.context.action.particle.render.ParticleRenderer;
import org.pickaid.pibrary.init.LibraryRegistries;
import net.minecraft.core.particles.ParticleType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.ForgeRegistries;

public record StaticTextureParticleData(
		RenderTypePreset renderType, ParticleType<?> particle,
		int age, int total
) implements ParticleRenderData<StaticTextureParticleData> {

	public static final Codec<StaticTextureParticleData> CODEC = RecordCodecBuilder.create(i -> i.group(
			RenderTypePreset.CODEC.fieldOf("renderType").forGetter(e -> e.renderType),
			ForgeRegistries.PARTICLE_TYPES.getCodec().fieldOf("particle").forGetter(e -> e.particle),
			Codec.INT.fieldOf("age").forGetter(e -> e.age),
			Codec.INT.fieldOf("total").forGetter(e -> e.total)
	).apply(i, StaticTextureParticleData::new));

	@Override
	public ParticleRenderType<StaticTextureParticleData> type() {
		return LibraryRegistries.STATIC_RENDER.get();
	}

	@OnlyIn(Dist.CLIENT)
	@Override
	public ParticleRenderer resolve(EngineContext ctx) {
		var rl = ForgeRegistries.PARTICLE_TYPES.getKey(particle);
		assert rl != null;
		return new FixedParticleSprite(renderType, rl, age, total);
	}

}
