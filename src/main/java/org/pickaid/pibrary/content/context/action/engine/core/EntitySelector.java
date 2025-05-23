package org.pickaid.pibrary.content.context.action.engine.core;

import com.mojang.serialization.Codec;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.LivingEntity;
import org.pickaid.pibrary.content.context.action.engine.context.EngineContext;
import org.pickaid.pibrary.content.context.action.engine.selector.SelectionType;
import org.pickaid.pibrary.init.LibraryRegistries;

import java.util.LinkedHashSet;

public interface EntitySelector<T extends Record & EntitySelector<T>> extends Verifiable {

	Codec<EntitySelector<?>> CODEC = ExtraCodecs.lazyInitializedCodec(() -> LibraryRegistries.SELECTOR_TYPE.get().getCodec()
			.dispatch(EntitySelector::type, SelectorType::codec));

	SelectorType<T> type();
	LinkedHashSet<LivingEntity> find(ServerLevel sl, EngineContext ctx, SelectionType type);
}
