package org.pickaid.pibrary.content.context.action.engine.modifier;

import com.mojang.serialization.Codec;
import org.pickaid.pibrary.content.context.action.engine.context.EngineContext;
import org.pickaid.pibrary.content.context.action.engine.context.LocationContext;
import org.pickaid.pibrary.content.context.action.engine.core.Modifier;
import org.pickaid.pibrary.content.context.action.engine.core.ModifierType;
import org.pickaid.pibrary.init.LibraryRegistries;

public record Normal2DirModifier() implements Modifier<Normal2DirModifier> {

	public static Codec<Normal2DirModifier> CODEC = Codec.unit(new Normal2DirModifier());

	@Override
	public ModifierType<Normal2DirModifier> type() {
		return LibraryRegistries.NORMAL_2_DIR.get();
	}

	@Override
	public LocationContext modify(EngineContext ctx) {
		return ctx.loc().setDir(ctx.loc().normal());
	}

}
