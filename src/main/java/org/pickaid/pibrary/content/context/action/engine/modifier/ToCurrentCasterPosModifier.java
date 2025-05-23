package org.pickaid.pibrary.content.context.action.engine.modifier;

import com.mojang.serialization.Codec;
import org.pickaid.pibrary.content.context.action.engine.context.EngineContext;
import org.pickaid.pibrary.content.context.action.engine.context.LocationContext;
import org.pickaid.pibrary.content.context.action.engine.core.Modifier;
import org.pickaid.pibrary.content.context.action.engine.core.ModifierType;
import org.pickaid.pibrary.init.LibraryObjects;

public record ToCurrentCasterPosModifier() implements Modifier<ToCurrentCasterPosModifier> {

	public static Codec<ToCurrentCasterPosModifier> CODEC = Codec.unit(new ToCurrentCasterPosModifier());

	@Override
	public ModifierType<ToCurrentCasterPosModifier> type() {
		return LibraryObjects.TO_CASTER_POS.get();
	}

	@Override
	public LocationContext modify(EngineContext ctx) {
		return ctx.loc().with(ctx.user().user().position());
	}

}
