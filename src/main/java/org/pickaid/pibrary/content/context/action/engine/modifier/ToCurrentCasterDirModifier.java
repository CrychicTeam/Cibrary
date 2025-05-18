package org.pickaid.pibrary.content.context.action.engine.modifier;

import com.mojang.serialization.Codec;
import org.pickaid.pibrary.content.context.action.engine.context.EngineContext;
import org.pickaid.pibrary.content.context.action.engine.context.LocationContext;
import org.pickaid.pibrary.content.context.action.engine.context.ActionContext;
import org.pickaid.pibrary.content.context.action.engine.core.Modifier;
import org.pickaid.pibrary.content.context.action.engine.core.ModifierType;
import org.pickaid.pibrary.init.LibraryRegistries;

public record ToCurrentCasterDirModifier() implements Modifier<ToCurrentCasterDirModifier> {

	public static Codec<ToCurrentCasterDirModifier> CODEC = Codec.unit(new ToCurrentCasterDirModifier());

	@Override
	public ModifierType<ToCurrentCasterDirModifier> type() {
		return LibraryRegistries.TO_CASTER_DIR.get();
	}

	@Override
	public LocationContext modify(EngineContext ctx) {
		return ctx.loc().setDir(ActionContext.getForward(ctx.user().user()));
	}

}
