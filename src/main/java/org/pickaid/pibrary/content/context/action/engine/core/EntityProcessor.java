package org.pickaid.pibrary.content.context.action.engine.core;

import com.mojang.serialization.Codec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.LivingEntity;
import org.pickaid.pibrary.content.context.action.engine.context.EngineContext;
import org.pickaid.pibrary.init.LibraryRegistries;

import java.util.Collection;

public interface EntityProcessor<T extends Record & EntityProcessor<T>> extends Verifiable {

	Codec<EntityProcessor<?>> CODEC = ExtraCodecs.lazyInitializedCodec(() -> LibraryRegistries.PROCESSOR_TYPE.get().getCodec()
			.dispatch(EntityProcessor::type, ProcessorType::codec));

	ProcessorType<T> type();

	void process(Collection<LivingEntity> le, EngineContext ctx);

}
