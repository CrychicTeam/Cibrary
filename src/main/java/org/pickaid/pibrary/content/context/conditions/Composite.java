package org.pickaid.pibrary.content.context.conditions;

import java.util.List;

public abstract class Composite implements ContextPredicate {
	protected final List<ContextPredicate> terms;
	
	public Composite(List<ContextPredicate> terms) {
		this.terms = terms;
	}
	
	public final List<ContextPredicate> getTerms() {
		return terms;
	}
	
}
