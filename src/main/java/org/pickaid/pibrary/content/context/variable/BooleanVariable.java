package org.pickaid.pibrary.content.context.variable;

import com.mojang.serialization.Codec;
import org.pickaid.pibrary.content.context.action.engine.context.EngineContext;

public record BooleanVariable(String str, ExpressionHolder exp) implements NumericVariable {

	public static final Codec<BooleanVariable> CODEC = Codec.STRING.xmap(BooleanVariable::of, BooleanVariable::str);

	public static BooleanVariable of(String str) {
		return new BooleanVariable(str, ExpressionHolder.ofVerified(str));
	}

	public boolean eval(EngineContext ctx) {
		return exp.eval(ctx) > 0.5;
	}

}
