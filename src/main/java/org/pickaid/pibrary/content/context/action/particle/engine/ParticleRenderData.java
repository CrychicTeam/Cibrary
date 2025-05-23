package org.pickaid.pibrary.content.context.action.particle.engine;

import com.mojang.serialization.Codec;
import net.minecraft.util.ExtraCodecs;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.pickaid.pibrary.content.context.action.engine.context.EngineContext;
import org.pickaid.pibrary.content.context.action.engine.core.Verifiable;
import org.pickaid.pibrary.content.context.action.particle.render.ParticleRenderer;
import org.pickaid.pibrary.init.LibraryRegistries;

public interface ParticleRenderData<T extends Record & ParticleRenderData<T>> extends Verifiable {

	Codec<ParticleRenderData<?>> CODEC = ExtraCodecs.lazyInitializedCodec(() -> LibraryRegistries.PARTICLE_RENDERER_TYPE.get().getCodec()
			.dispatch(ParticleRenderData::type, ParticleRenderType::codec));

	ParticleRenderType<T> type();

	@OnlyIn(Dist.CLIENT)
	ParticleRenderer resolve(EngineContext ctx);

}
