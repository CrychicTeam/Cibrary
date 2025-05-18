package org.pickaid.pibrary.content.context.variable;

import org.pickaid.pibrary.content.context.action.engine.context.BuilderContext;

public interface NumericVariable extends Variable {

	ExpressionHolder exp();

	@Override
	default boolean verify(BuilderContext ctx) {
		return exp().verify(ctx);
	}

}
