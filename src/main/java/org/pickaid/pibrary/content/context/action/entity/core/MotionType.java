package org.pickaid.pibrary.content.context.action.entity.core;

import com.mojang.serialization.Codec;

public record MotionType<T extends Record & Motion<T>>(Codec<T> codec) {
	public static <T extends Record & Motion<T>> MotionType<T> of(Codec<T> codec) {
		return new MotionType<>(codec);
	}
}
