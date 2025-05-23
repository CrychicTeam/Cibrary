package org.pickaid.pibrary.content.context.action.engine.core;

import com.mojang.serialization.Codec;
import net.minecraft.util.ExtraCodecs;
import org.pickaid.pibrary.content.context.action.engine.context.EngineContext;
import org.pickaid.pibrary.content.context.action.engine.context.LocationContext;
import org.pickaid.pibrary.init.LibraryRegistries;

public interface Modifier<T extends Record & Modifier<T>> extends Verifiable {

	Codec<Modifier<?>> CODEC = ExtraCodecs.lazyInitializedCodec(() -> LibraryRegistries.MODIFIER_TYPE.get().getCodec()
			.dispatch(Modifier::type, ModifierType::codec));

	ModifierType<T> type();

	LocationContext modify(EngineContext ctx);

}
