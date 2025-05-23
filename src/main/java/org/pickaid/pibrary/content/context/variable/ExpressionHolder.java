package org.pickaid.pibrary.content.context.variable;

import org.objecthunter.exp4j.Expression;
import org.objecthunter.exp4j.ExpressionBuilder;
import org.pickaid.pibrary.content.context.action.engine.context.BuilderContext;
import org.pickaid.pibrary.content.context.action.engine.context.EngineContext;
import net.minecraft.util.RandomSource;

public class ExpressionHolder {

	public static ExpressionHolder of(String str) {
		return new ExpressionHolder(str);
	}

	public static ExpressionHolder ofVerified(String str) {
		var ans = new ExpressionHolder(str);
		ans.verify(BuilderContext.STATIC);
		return ans;
	}

	private final String str;

	private Expression exp;
	private boolean isConstant;
	private double constant;

	private RandomSource random;

	public ExpressionHolder(String str) {
		this.str = str;
	}


	public double eval(EngineContext ctx) {
		if (isConstant) {
			return constant;
		}
		verify(BuilderContext.STATIC);
		if (exp == null) throw new IllegalStateException("Must call verify before fetching expression");
		return evalImpl(ctx);
	}

	private synchronized double evalImpl(EngineContext ctx) {
		random = ctx.rand();
		double ans = ctx.eval(exp);
		random = null;
		return ans;
	}

	private double random() {
		if (random == null)
			throw new IllegalStateException("Random must be specified when performing calculations");
		return random.nextDouble();
	}

	public boolean verify(BuilderContext ctx) {
		random = null;
		exp = null;
		isConstant = false;
		constant = 0;
		try {
			exp = new ExpressionBuilder(str)
					.operator(DefaultOperators.OPERATORS)
					.functions(DefaultFunctions.FUNCTIONS)
					.function(DefaultFunctions.rand(this::random))
					.variables(ctx.params())
					.build();
		} catch (Exception e) {
			ctx.error(str, e);
			return false;
		}
		if (exp.validate(true).isValid()) {
			try {
				constant = exp.evaluate();
				isConstant = true;
			} catch (Exception ignored) {
				isConstant = false;
			}
		}
		return true;
	}

}
