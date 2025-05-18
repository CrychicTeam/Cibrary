package org.pickaid.pibrary.content.context.action.conditions;

import org.pickaid.pibrary.content.context.action.Context;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public class Inverted implements ContextPredicate {
	public static final Codec<Inverted> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			ContextPredicate.DIRECT_CODEC.fieldOf("term").forGetter(Inverted::getTerm)
	).apply(instance, Inverted::new));
	private final ContextPredicate term;
	
	public Inverted(ContextPredicate term) {
		this.term = term;
	}
	
	public ContextPredicate getTerm() {
		return term;
	}
	
	@Override
	public boolean test(Context context) {
		return !this.term.test(context);
	}
	
	@Override
	public ContextConditionType<?> type() {
		return ContextConditionType.INVERTED.get();
	}
	
}
