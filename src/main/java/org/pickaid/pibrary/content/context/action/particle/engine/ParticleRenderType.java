package org.pickaid.pibrary.content.context.action.particle.engine;

import com.mojang.serialization.Codec;

public record ParticleRenderType<T extends Record & ParticleRenderData<T>>(Codec<T> codec) {
	public static <T extends Record & ParticleRenderData<T>> ParticleRenderType<T> of(Codec<T> codec) {
        return new ParticleRenderType<>(codec);
    }
}
