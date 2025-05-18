package org.pickaid.pibrary.content.context.action.engine.core;

import com.mojang.serialization.Codec;

public record ProcessorType<T extends Record & EntityProcessor<T>>(Codec<T> codec) {
	public static <T extends Record & EntityProcessor<T>> ProcessorType<T> of(Codec<T> codec) {
		return new ProcessorType<>(codec);
	}
}
