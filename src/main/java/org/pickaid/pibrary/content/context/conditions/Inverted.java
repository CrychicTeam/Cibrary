package org.pickaid.pibrary.content.context.conditions;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.pickaid.pibrary.content.context.Context;

public record Inverted(ContextPredicate term) implements ContextPredicate {
	public static final Codec<Inverted> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			ContextPredicate.DIRECT_CODEC.fieldOf("term").forGetter(Inverted::term)
	).apply(instance, Inverted::new));


	@Override
	public boolean test(Context context) {
		return !this.term.test(context);
	}

	@Override
	public ContextConditionType<?> type() {
		return ContextConditionType.INVERTED.get();
	}
}
