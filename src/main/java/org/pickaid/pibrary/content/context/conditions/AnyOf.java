package org.pickaid.pibrary.content.context.conditions;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.pickaid.pibrary.content.context.Context;

import java.util.List;

public class AnyOf extends Composite {
	public static final Codec<AnyOf> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			ContextPredicate.DIRECT_CODEC.listOf().optionalFieldOf("terms", List.of()).forGetter(AnyOf::getTerms)
	).apply(instance, AnyOf::new));
	
	public AnyOf(List<ContextPredicate> terms) {
		super(terms);
	}
	
	@Override
	public boolean test(Context context) {
		for (ContextPredicate predicate : this.terms) {
			if (predicate.test(context)) {
				return true;
			}
		}
		return false;
	}
	
	@Override
	public ContextConditionType<?> type() {
		return ContextConditionType.ANY_OF.get();
	}
}
