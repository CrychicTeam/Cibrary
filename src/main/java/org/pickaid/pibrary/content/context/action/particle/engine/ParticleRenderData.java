package org.pickaid.pibrary.content.context.action.particle.engine;

import com.mojang.serialization.Codec;
import org.pickaid.pibrary.content.context.action.engine.context.EngineContext;
import org.pickaid.pibrary.content.context.action.engine.core.Verifiable;
import org.pickaid.pibrary.content.context.action.particle.render.ParticleRenderer;
import org.pickaid.pibrary.init.LibraryRegistries;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public interface ParticleRenderData<T extends Record & ParticleRenderData<T>> extends Verifiable {

	Codec<ParticleRenderData<?>> CODEC = LibraryRegistries.PARTICLE_RENDERER.codec()
			.dispatch(ParticleRenderData::type, ParticleRenderType::codec);

	ParticleRenderType<T> type();

	@OnlyIn(Dist.CLIENT)
	ParticleRenderer resolve(EngineContext ctx);

}
