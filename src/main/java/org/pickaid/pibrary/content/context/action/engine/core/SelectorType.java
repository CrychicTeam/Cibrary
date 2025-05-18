package org.pickaid.pibrary.content.context.action.engine.core;

import com.mojang.serialization.Codec;

public record SelectorType<T extends Record & EntitySelector<T>> (Codec<T> codec) {
	public static <T extends Record & EntitySelector<T>> SelectorType<T> of(Codec<T> codec) {
		return new SelectorType<>(codec);
	}
}
