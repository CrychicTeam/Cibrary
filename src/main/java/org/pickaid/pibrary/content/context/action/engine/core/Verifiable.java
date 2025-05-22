package org.pickaid.pibrary.content.context.action.engine.core;

import org.jetbrains.annotations.Nullable;
import org.pickaid.pibrary.content.context.action.engine.context.BuilderContext;
import org.pickaid.pibrary.content.context.action.engine.helper.EngineHelper;

import java.util.Set;

public interface Verifiable {

	@Nullable
	default Set<String> verificationParameters() {
		return null;
	}

	default boolean verify(BuilderContext ctx) {
		EngineHelper.verifyFields(this, ctx, this.getClass());
		return true;
	}
}
