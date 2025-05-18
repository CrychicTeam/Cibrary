package org.pickaid.pibrary.content.context.action.engine.core;


import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.pickaid.pibrary.content.context.action.engine.context.EngineContext;
import org.pickaid.pibrary.init.LibraryRegistries;

import java.util.Optional;
import java.util.function.Function;

public interface ConfiguredEngine<T extends Record & ConfiguredEngine<T>> extends Verifiable {

	Codec<ConfiguredEngine<?>> CODEC = LibraryRegistries.ENGINE.codec()
			.dispatch(ConfiguredEngine::type, EngineType::codec);

	static <T> RecordCodecBuilder<T, ConfiguredEngine<?>> codec(String str, Function<T, ConfiguredEngine<?>> func) {
		return CODEC.fieldOf(str).forGetter(func);
	}

	static <T> RecordCodecBuilder<T, Optional<ConfiguredEngine<?>>> optionalCodec(String str, Function<T, ConfiguredEngine<?>> func) {
		return CODEC.optionalFieldOf(str).forGetter(e -> Optional.ofNullable(func.apply(e)));
	}

	void execute(EngineContext ctx);

	EngineType<T> type();

}