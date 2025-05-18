package org.pickaid.pibrary.content.context.action.engine.core;

import com.mojang.serialization.Codec;
import org.pickaid.pibrary.content.context.action.engine.context.EngineContext;
import org.pickaid.pibrary.init.LibraryRegistries;
import net.minecraft.world.entity.LivingEntity;

import java.util.Collection;

public interface EntityProcessor<T extends Record & EntityProcessor<T>> extends Verifiable {

	Codec<EntityProcessor<?>> CODEC = LibraryRegistries.PROCESSOR.codec()
			.dispatch(EntityProcessor::type, ProcessorType::codec);

	ProcessorType<T> type();

	void process(Collection<LivingEntity> le, EngineContext ctx);

}
