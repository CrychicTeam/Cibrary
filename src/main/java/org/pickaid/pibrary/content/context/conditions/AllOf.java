package org.pickaid.pibrary.content.context.conditions;

import org.pickaid.pibrary.content.context.Context;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.List;

public class AllOf extends Composite {
	public static final Codec<AllOf> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			ContextPredicate.DIRECT_CODEC.listOf().optionalFieldOf("terms", List.of()).forGetter(AllOf::getTerms)
	).apply(instance, AllOf::new));
	
	public AllOf(List<ContextPredicate> terms) {
		super(terms);
	}
	
	@Override
	public boolean test(Context context) {
		for (ContextPredicate predicate : this.terms) {
			if (!predicate.test(context)) {
				return false;
			}
		}
		return true;
	}
	
	@Override
	public ContextConditionType<?> type() {
		return ContextConditionType.INVERTED.get();
	}
}
