package org.pickaid.pibrary.content.context.action.engine.core;

import com.mojang.serialization.Codec;

public record EngineType<T extends Record & ConfiguredEngine<T>>(Codec<T> codec) {
	public static <T extends Record & ConfiguredEngine<T>> EngineType<T> of(Codec<T> codec) {
        return new EngineType<>(codec);
    }
}
