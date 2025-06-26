package org.pickaid.pibrary.content.context.variable;

import org.objecthunter.exp4j.Expression;
import org.objecthunter.exp4j.ExpressionBuilder;
import org.pickaid.pibrary.content.context.action.engine.context.BuilderContext;
import org.pickaid.pibrary.content.context.action.engine.context.EngineContext;
import org.pickaid.pibrary.content.context.action.engine.core.Verifiable;
import net.minecraft.util.RandomSource;

import java.util.Set;

public class ExpressionHolder implements Verifiable {

	public static ExpressionHolder of(String str) {
		return new ExpressionHolder(str);
	}

	public static ExpressionHolder ofVerified(String str) {
		return new ExpressionHolder(str);
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

		if (exp == null) {
			buildExpressionAtRuntime(ctx);
		}

		if (exp == null) {
			throw new IllegalStateException("Failed to build expression: " + str);
		}

		return evalImpl(ctx);
	}

	private synchronized double evalImpl(EngineContext ctx) {
		random = ctx.rand();
		double ans = ctx.eval(exp);
		random = null;
		return ans;
	}

	private void buildExpressionAtRuntime(EngineContext ctx) {
		try {
			var builder = new ExpressionBuilder(str)
					.operator(DefaultOperators.OPERATORS)
					.functions(DefaultFunctions.FUNCTIONS)
					.function(DefaultFunctions.rand(this::random));

			if (ctx.parameters() != null && !ctx.parameters().isEmpty()) {
				builder.variables(ctx.parameters().keySet());
			}

			exp = builder.build();
			if (exp.validate(true).isValid()) {
				try {
					constant = exp.evaluate();
					isConstant = true;
				} catch (Exception ignored) {
					isConstant = false;
				}
			}
		} catch (Exception e) {
			throw new RuntimeException("Failed to build expression: " + str, e);
		}
	}

	private double random() {
		if (random == null)
			throw new IllegalStateException("Random must be specified when performing calculations");
		return random.nextDouble();
	}

	@Override
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

	@Override
	public Set<String> verificationParameters() {
		try {
			var tempBuilder = new ExpressionBuilder(str)
					.operator(DefaultOperators.OPERATORS)
					.functions(DefaultFunctions.FUNCTIONS)
					.function(DefaultFunctions.rand(() -> 0.0));

			try {
				var tempExp = tempBuilder.build();
				return tempExp.getVariableNames();
			} catch (Exception e) {
				return extractVariableFromError(e.getMessage());
			}
		} catch (Exception e) {
			return Set.of();
		}
	}

	private Set<String> extractVariableFromError(String errorMessage) {
		if (errorMessage != null && errorMessage.contains("Unknown function or variable")) {
			int start = errorMessage.indexOf("'");
			int end = errorMessage.indexOf("'", start + 1);
			if (start != -1 && end != -1 && end > start) {
				String varName = errorMessage.substring(start + 1, end);
				return Set.of(varName);
			}
		}
		return Set.of();
	}

	public String getStr() {
		return str;
	}
}