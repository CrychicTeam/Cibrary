package org.pickaid.pibrary.content.context.action.engine.iterator;

import org.pickaid.pibrary.content.context.action.engine.core.ConfiguredEngine;

import javax.annotation.Nullable;
import java.util.Set;

public interface Iterator<T extends Record & Iterator<T>> extends ConfiguredEngine<T> {

	ConfiguredEngine<?> child();

	@Nullable
	String index();

	@Override
	@Nullable
	default Set<String> verificationParameters() {
		String str = index();
		return str == null ? Set.of() : Set.of(str);
	}

}
