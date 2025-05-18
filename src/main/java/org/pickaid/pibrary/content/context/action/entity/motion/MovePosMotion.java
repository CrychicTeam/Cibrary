package org.pickaid.pibrary.content.context.action.entity.motion;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.pickaid.pibrary.content.context.action.engine.context.EngineContext;
import org.pickaid.pibrary.content.context.action.engine.context.LocationContext;
import org.pickaid.pibrary.content.context.action.engine.core.Modifier;
import org.pickaid.pibrary.content.context.action.entity.core.MotionType;
import org.pickaid.pibrary.init.LibraryRegistries;

import java.util.List;

public record MovePosMotion(
		List<Modifier<?>> modifiers
) implements SetPosMotion<MovePosMotion> {

	public static final Codec<MovePosMotion> CODEC = RecordCodecBuilder.create(i -> i.group(
			Codec.list(Modifier.CODEC).fieldOf("modifiers").forGetter(e -> e.modifiers)
	).apply(i, MovePosMotion::new));

	@Override
	public MotionType<MovePosMotion> type() {
		return LibraryRegistries.MOVE_MOTION.get();
	}

	@Override
	public LocationContext move(EngineContext ctx) {
		for (var e : modifiers) {
			ctx = ctx.with(e.modify(ctx));
		}
		return ctx.loc();
	}

}
