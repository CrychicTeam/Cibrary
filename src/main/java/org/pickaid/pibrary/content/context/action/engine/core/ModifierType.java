package org.pickaid.pibrary.content.context.action.engine.core;

import com.mojang.serialization.Codec;

public record ModifierType<T extends Record & Modifier<T>>(Codec<T> codec) {

	public static <T extends Record & Modifier<T>> ModifierType<T> of(Codec<T> codec) {
		return new ModifierType<>(codec);
	}
}
