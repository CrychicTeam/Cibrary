package org.pickaid.pibrary.content.context.variable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.objecthunter.exp4j.Expression;
import org.objecthunter.exp4j.ExpressionBuilder;
import org.pickaid.pibrary.Pibrary;
import org.pickaid.pibrary.content.context.action.engine.context.EngineContext;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public record DoubleVariable(String str, ExpressionHolder exp) implements NumericVariable {

	public static final Codec<DoubleVariable> CODEC = Codec.STRING.xmap(DoubleVariable::of, DoubleVariable::str);

	public static final DoubleVariable ZERO = ofVerified("0");

	public static <T> RecordCodecBuilder<T, DoubleVariable> codec(String str, Function<T, DoubleVariable> func) {
		return CODEC.fieldOf(str).forGetter(func);
	}

	public static <T> RecordCodecBuilder<T, Optional<DoubleVariable>> optionalCodec(String str, Function<T, DoubleVariable> func) {
		return CODEC.optionalFieldOf(str).forGetter(e -> Optional.ofNullable(func.apply(e)));
	}

	public static DoubleVariable of(String str) {
		return new DoubleVariable(str, ExpressionHolder.of(str));
	}

	public static DoubleVariable ofVerified(String str) {
		return new DoubleVariable(str, ExpressionHolder.ofVerified(str));
	}

	/**
	 * 使用EngineContext计算表达式的值
	 * @param ctx 引擎上下文
	 * @return 计算结果
	 */
	public double eval(EngineContext ctx) {
		return exp.eval(ctx);
	}
	
	/**
	 * 使用参数Map计算表达式的值
	 * 这是为了兼容ActionEngine系统的方法
	 * @param parameters 参数Map
	 * @return 计算结果
	 */
	public double apply(Map<String, Object> parameters) {
		try {
			// 构建临时表达式
			Expression tempExp = new ExpressionBuilder(str)
				.operator(DefaultOperators.OPERATORS)
				.functions(DefaultFunctions.FUNCTIONS)
				.build();
			
			// 设置变量值
			for (Map.Entry<String, Object> entry : parameters.entrySet()) {
				if (entry.getValue() instanceof Number number) {
					tempExp.setVariable(entry.getKey(), number.doubleValue());
				}
			}
			
			// 计算结果
			return tempExp.evaluate();
		} catch (Exception e) {
			Pibrary.LOGGER.error("Error evaluating expression: " + str, e);
			return 0.0; // 出错时返回默认值
		}
	}
}
