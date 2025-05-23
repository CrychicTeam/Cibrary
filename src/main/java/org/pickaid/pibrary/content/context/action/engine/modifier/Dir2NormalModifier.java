package org.pickaid.pibrary.content.context.action.engine.modifier;

import com.mojang.serialization.Codec;
import org.pickaid.pibrary.content.context.action.engine.context.EngineContext;
import org.pickaid.pibrary.content.context.action.engine.context.LocationContext;
import org.pickaid.pibrary.content.context.action.engine.core.Modifier;
import org.pickaid.pibrary.content.context.action.engine.core.ModifierType;
import org.pickaid.pibrary.init.LibraryObjects;

public record Dir2NormalModifier() implements Modifier<Dir2NormalModifier> {

	public static Codec<Dir2NormalModifier> CODEC = Codec.unit(new Dir2NormalModifier());

	@Override
	public ModifierType<Dir2NormalModifier> type() {
		return LibraryObjects.DIR_2_NORMAL.get();
	}

	@Override
	public LocationContext modify(EngineContext ctx) {
		return ctx.loc().setNormal(ctx.loc().dir());
	}

}
