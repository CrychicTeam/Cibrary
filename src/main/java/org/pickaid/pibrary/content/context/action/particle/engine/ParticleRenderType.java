package org.pickaid.pibrary.content.context.action.particle.engine;

import com.mojang.serialization.Codec;

public interface ParticleRenderType<T extends Record & ParticleRenderData<T>> {

	Codec<T> codec();

}
